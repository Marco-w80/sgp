package com.sgp.controller.licitacao;

import com.sgp.service.licitacao.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequestMapping("/licitacoes/portfolio")
public class PortfolioController {
    private final PortfolioService service;private final ImportacaoTabularService importacao;
    public PortfolioController(PortfolioService service,ImportacaoTabularService importacao){this.service=service;this.importacao=importacao;}
    @GetMapping public String pagina(@RequestParam(defaultValue="")String busca,@RequestParam(defaultValue="estoque")String aba,Model model){model.addAttribute("estoque",service.estoque(busca));model.addAttribute("fornecedores",service.fornecedores(busca));model.addAttribute("busca",busca);model.addAttribute("abaAtiva",aba);return "licitacoes/portfolio";}
    @PostMapping("/estoque") public String estoque(@RequestParam(required=false)Long id,@RequestParam String nome,@RequestParam(required=false)String marca,@RequestParam(required=false)String fabricante,@RequestParam(required=false)BigDecimal quantidade,@RequestParam(required=false)BigDecimal preco,RedirectAttributes ra){service.salvarEstoque(id,nome,marca,fabricante,quantidade,preco);ra.addFlashAttribute("mensagem","Item de estoque salvo.");return redirect("estoque");}
    @PostMapping("/estoque/{id}/excluir") public String excluirEstoque(@PathVariable Long id){service.excluirEstoque(id);return redirect("estoque");}
    @PostMapping("/fornecedores") public String fornecedor(@RequestParam(required=false)Long id,@RequestParam String nome,@RequestParam(required=false)String contato,@RequestParam(required=false)String resumo,@RequestParam(required=false)String observacoes,RedirectAttributes ra){service.salvarFornecedor(id,nome,contato,resumo,observacoes);ra.addFlashAttribute("mensagem","Fornecedor salvo.");return redirect("fornecedores");}
    @PostMapping("/fornecedores/{id}/excluir") public String excluirFornecedor(@PathVariable Long id){service.excluirFornecedor(id);return redirect("fornecedores");}
    @PostMapping("/fornecedores/{id}/itens") public String item(@PathVariable Long id,@RequestParam(required=false)Long itemId,@RequestParam String nome,@RequestParam(required=false)String marca,@RequestParam(required=false)BigDecimal preco,RedirectAttributes ra){service.salvarItem(id,itemId,nome,marca,preco);ra.addFlashAttribute("mensagem","Item do fornecedor salvo.");return redirect("fornecedores");}
    @PostMapping("/fornecedores/{id}/itens/{itemId}/excluir") public String excluirItem(@PathVariable Long id,@PathVariable Long itemId){service.excluirItem(id,itemId);return redirect("fornecedores");}
    @PostMapping("/fornecedores/{id}/itens/importar") public String importar(@PathVariable Long id,@RequestParam String dados,@RequestParam ImportacaoTabularService.Delimitador delimitador,@RequestParam(defaultValue="false")boolean cabecalho,@RequestParam int colNome,@RequestParam int colMarca,@RequestParam int colPreco,RedirectAttributes ra){int total=service.importarItens(id,importacao.lerFornecedor(dados,delimitador,cabecalho,colNome,colMarca,colPreco));ra.addFlashAttribute("mensagem",total+" itens importados.");return redirect("fornecedores");}
    private String redirect(String aba){return "redirect:/licitacoes/portfolio?aba="+aba;}
}
