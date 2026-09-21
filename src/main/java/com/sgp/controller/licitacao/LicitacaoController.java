package com.sgp.controller.licitacao;

import com.sgp.model.licitacao.*;
import com.sgp.repository.licitacao.FornecedorLicitacaoRepository;
import com.sgp.service.licitacao.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/licitacoes")
public class LicitacaoController {
    private final LicitacaoService service;private final ImportacaoTabularService importacao;private final FornecedorLicitacaoRepository fornecedores;private final ExportacaoCotacaoService exportacao;private final LicitacaoAnexoService anexos;private final PncpService pncp;
    public LicitacaoController(LicitacaoService service,ImportacaoTabularService importacao,FornecedorLicitacaoRepository fornecedores,ExportacaoCotacaoService exportacao,LicitacaoAnexoService anexos,PncpService pncp){this.service=service;this.importacao=importacao;this.fornecedores=fornecedores;this.exportacao=exportacao;this.anexos=anexos;this.pncp=pncp;}

    @GetMapping public String dashboard(@RequestParam(defaultValue="")String busca,@RequestParam(defaultValue="false")boolean arquivadas,Model model){model.addAttribute("dashboard",service.dashboard(busca,arquivadas));model.addAttribute("busca",busca);model.addAttribute("arquivadas",arquivadas);model.addAttribute("etapas",EtapaLicitacao.values());return "licitacoes/dashboard";}
    @GetMapping("/nova") public String nova(Model model){model.addAttribute("licitacao",new Licitacao());return "licitacoes/formulario";}
    @PostMapping public String criar(@ModelAttribute Licitacao licitacao,@RequestParam(required=false)String itensPncpJson,@RequestParam(required=false)String linkItensPncp,RedirectAttributes ra){var linhas=linksIguais(licitacao.getLinkEdital(),linkItensPncp)?pncp.lerItensImportados(itensPncpJson):java.util.List.<ImportacaoTabularService.LinhaImportada>of();Licitacao salva=service.criarComItens(licitacao,linhas);ra.addFlashAttribute("mensagem",linhas.isEmpty()?"Licitação cadastrada. Agora adicione ou importe os itens.":"Licitação cadastrada com "+linhas.size()+" itens do PNCP. Selecione quais seguirão para Cotação.");return "redirect:/licitacoes/"+salva.getId();}
    @GetMapping("/{id}/editar") public String editar(@PathVariable Long id,Model model){model.addAttribute("licitacao",service.obter(id));return "licitacoes/formulario";}
    @PostMapping("/{id}") public String editar(@PathVariable Long id,@ModelAttribute Licitacao licitacao,RedirectAttributes ra){service.editar(id,licitacao);ra.addFlashAttribute("mensagem","Dados atualizados.");return detalhe(id);}
    @GetMapping("/{id}") public String detalhe(@PathVariable Long id,@RequestParam(required=false)String aba,Model model){Licitacao l=service.obter(id);model.addAttribute("licitacao",l);model.addAttribute("abaAtiva",aba==null||aba.isBlank()?l.getEtapaAtual().name().toLowerCase():aba);model.addAttribute("etapas",EtapaLicitacao.values());model.addAttribute("decisoes",DecisaoItem.values());model.addAttribute("resultados",ResultadoItem.values());model.addAttribute("fornecedores",fornecedores.findAllByOrderByNomeAsc());return "licitacoes/detalhes";}
    @PostMapping("/{id}/etapa") public String etapa(@PathVariable Long id,@RequestParam EtapaLicitacao destino,@RequestParam(required=false)String observacoes,@RequestParam(defaultValue="false")boolean selecionarItens,@RequestParam(required=false)java.util.List<Long> itemIds,RedirectAttributes ra){if(selecionarItens&&destino==EtapaLicitacao.COTACAO)service.selecionarItensETransicionarParaCotacao(id,itemIds,observacoes);else service.transicionar(id,destino,observacoes);ra.addFlashAttribute("mensagem","Etapa alterada para "+destino.getDescricao()+".");return detalhe(id);}
    @PostMapping("/{id}/etapa-kanban") @ResponseBody public ResponseEntity<Map<String,String>> etapaKanban(@PathVariable Long id,@RequestParam EtapaLicitacao destino){try{service.transicionar(id,destino,"Movido pelo Kanban");return ResponseEntity.ok(Map.of("mensagem","Etapa alterada."));}catch(IllegalArgumentException|IllegalStateException e){return ResponseEntity.badRequest().body(Map.of("erro",e.getMessage()));}}
    @PostMapping("/{id}/arquivar") public String arquivar(@PathVariable Long id,@RequestParam(defaultValue="true")boolean arquivada,RedirectAttributes ra){service.arquivar(id,arquivada);ra.addFlashAttribute("mensagem",arquivada?"Licitação arquivada.":"Licitação restaurada.");return "redirect:/licitacoes?arquivadas="+arquivada;}
    @PostMapping("/{id}/excluir") @PreAuthorize("hasRole('ADMIN')") public String excluir(@PathVariable Long id,RedirectAttributes ra){service.excluir(id);ra.addFlashAttribute("mensagem","Licitação e dados dependentes excluídos.");return "redirect:/licitacoes";}

    @PostMapping("/{id}/itens") public String item(@PathVariable Long id,@RequestParam(required=false)Long itemId,@RequestParam(required=false)Integer numeroItem,@RequestParam String descricao,@RequestParam(required=false)BigDecimal quantidade,@RequestParam(required=false)String unidade,@RequestParam(required=false)BigDecimal valorReferencia,RedirectAttributes ra){service.salvarItem(id,itemId,numeroItem,descricao,quantidade,unidade,valorReferencia);ra.addFlashAttribute("mensagem",itemId==null?"Item adicionado.":"Item atualizado.");return detalhe(id,"cadastro");}
    @PostMapping("/{id}/itens/{itemId}/excluir") public String excluirItem(@PathVariable Long id,@PathVariable Long itemId,RedirectAttributes ra){service.excluirItem(id,itemId);ra.addFlashAttribute("mensagem","Item excluído.");return detalhe(id,"cadastro");}
    @PostMapping("/{id}/itens/importar") public String importar(@PathVariable Long id,@RequestParam String dados,@RequestParam ImportacaoTabularService.Delimitador delimitador,@RequestParam(defaultValue="false")boolean cabecalho,@RequestParam int colNumero,@RequestParam int colDescricao,@RequestParam int colQuantidade,@RequestParam int colUnidade,@RequestParam int colValor,RedirectAttributes ra){var linhas=importacao.lerItens(dados,delimitador,cabecalho,colNumero,colDescricao,colQuantidade,colUnidade,colValor);int total=service.importarItens(id,linhas);ra.addFlashAttribute("mensagem",total+" itens importados em lote.");return detalhe(id,"cadastro");}
    @PostMapping("/{id}/itens/{itemId}/decisao") public String decisao(@PathVariable Long id,@PathVariable Long itemId,@RequestParam DecisaoItem decisao,@RequestParam(required=false)BigDecimal precoMaximo,@RequestParam(required=false)BigDecimal percentualDesconto,@RequestParam(required=false)String estrategiaLance,RedirectAttributes ra){service.decidirItem(id,itemId,decisao,precoMaximo,percentualDesconto,estrategiaLance);ra.addFlashAttribute("mensagem","Decisão do item salva.");return detalhe(id,"aprovacao");}
    @PostMapping("/{id}/itens/{itemId}/resultado") public String resultado(@PathVariable Long id,@PathVariable Long itemId,@RequestParam ResultadoItem resultado,@RequestParam(required=false)BigDecimal precoFinal,@RequestParam(required=false)String motivoPerda,RedirectAttributes ra){service.resultado(id,itemId,resultado,precoFinal,motivoPerda);ra.addFlashAttribute("mensagem","Resultado salvo.");return detalhe(id,"participacao");}

    @PostMapping("/{id}/itens/{itemId}/cotacoes") public String cotacao(@PathVariable Long id,@PathVariable Long itemId,@RequestParam(required=false)Long cotacaoId,@RequestParam(required=false)Long fornecedorId,@RequestParam(required=false)String fornecedorNome,@RequestParam BigDecimal valorUnitario,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate dataCotacao,@RequestParam(required=false)String observacoes,RedirectAttributes ra){FornecedorLicitacao f=fornecedorId==null?null:fornecedores.findById(fornecedorId).orElseThrow();service.salvarCotacao(id,itemId,cotacaoId,f,fornecedorNome,valorUnitario,dataCotacao,observacoes);ra.addFlashAttribute("mensagem","Cotação salva.");return detalhe(id,"cotacao");}
    @PostMapping("/{id}/itens/{itemId}/cotacoes/{cotacaoId}/excluir") public String excluirCotacao(@PathVariable Long id,@PathVariable Long itemId,@PathVariable Long cotacaoId,RedirectAttributes ra){service.excluirCotacao(id,itemId,cotacaoId);ra.addFlashAttribute("mensagem","Cotação excluída.");return detalhe(id,"cotacao");}
    @GetMapping("/{id}/cotacoes.csv") public ResponseEntity<byte[]> exportar(@PathVariable Long id){Licitacao l=service.obter(id);return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename("cotacoes-edital-"+l.getNumeroEdital()+".csv",StandardCharsets.UTF_8).build().toString()).contentType(new MediaType("text","csv",StandardCharsets.UTF_8)).body(exportacao.csv(l));}

    @PostMapping("/{id}/anexos") public String upload(@PathVariable Long id,@RequestParam String nome,@RequestParam MultipartFile arquivo,@RequestParam(required=false)Long itemId,@RequestParam(required=false)EtapaLicitacao etapa,RedirectAttributes ra)throws IOException{anexos.salvar(id,nome,itemId,etapa,arquivo);ra.addFlashAttribute("mensagem","Arquivo anexado.");return detalhe(id,"anexos");}
    @GetMapping("/{id}/anexos/{anexoId}") public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable Long id,@PathVariable Long anexoId)throws IOException{var d=anexos.download(anexoId);if(!d.anexo().getLicitacao().getId().equals(id))throw new IllegalArgumentException("Anexo inválido.");return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(d.anexo().getNomeOriginal(),StandardCharsets.UTF_8).build().toString()).contentType(MediaType.parseMediaType(d.anexo().getMimeType())).contentLength(d.anexo().getTamanho()).body(d.recurso());}
    @PostMapping("/{id}/anexos/{anexoId}/excluir") public String excluirAnexo(@PathVariable Long id,@PathVariable Long anexoId,RedirectAttributes ra)throws IOException{anexos.excluir(id,anexoId);ra.addFlashAttribute("mensagem","Anexo excluído.");return detalhe(id,"anexos");}

    private String detalhe(Long id){return "redirect:/licitacoes/"+id;}private String detalhe(Long id,String aba){return detalhe(id)+"?aba="+aba;}
    private boolean linksIguais(String linkEdital,String linkItensPncp){return linkEdital!=null&&linkItensPncp!=null&&linkEdital.trim().equals(linkItensPncp.trim());}
}
