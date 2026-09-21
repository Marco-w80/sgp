package com.sgp.service.licitacao;

import com.sgp.model.licitacao.*;
import com.sgp.repository.licitacao.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class LicitacaoAnexoService {
    public static final long LIMITE=20L*1024*1024;
    private static final Set<String> EXTENSOES=Set.of("pdf","doc","docx","xls","xlsx","jpg","jpeg","png");
    private final Path raiz; private final LicitacaoAnexoRepository anexos; private final LicitacaoRepository licitacoes; private final LicitacaoItemRepository itens; private final UsuarioAtualService usuarioAtual;
    public LicitacaoAnexoService(@Value("${app.licitacoes.upload-dir:./data/licitacoes}") String diretorio,LicitacaoAnexoRepository anexos,LicitacaoRepository licitacoes,LicitacaoItemRepository itens,UsuarioAtualService usuarioAtual){this.raiz=Paths.get(diretorio).toAbsolutePath().normalize();this.anexos=anexos;this.licitacoes=licitacoes;this.itens=itens;this.usuarioAtual=usuarioAtual;}
    @Transactional public LicitacaoAnexo salvar(Long licitacaoId,String nome,Long itemId,EtapaLicitacao etapa,MultipartFile arquivo)throws IOException{
        String nomeExibicao=nome==null?"":nome.trim();if(nomeExibicao.isEmpty())throw new IllegalArgumentException("Informe o nome do anexo.");if(nomeExibicao.length()>255)throw new IllegalArgumentException("O nome do anexo deve ter no máximo 255 caracteres.");
        if(arquivo==null||arquivo.isEmpty())throw new IllegalArgumentException("Selecione um arquivo.");if(arquivo.getSize()>LIMITE)throw new IllegalArgumentException("O arquivo excede o limite de 20 MB.");String original=Optional.ofNullable(arquivo.getOriginalFilename()).orElse("arquivo");String seguro=Paths.get(original).getFileName().toString();String ext=extensao(seguro);if(!EXTENSOES.contains(ext))throw new IllegalArgumentException("Formato não permitido. Use PDF, DOC, DOCX, XLS, XLSX, JPEG ou PNG.");
        Licitacao l=licitacoes.findById(licitacaoId).orElseThrow(()->new EntityNotFoundException("Licitação não encontrada"));LicitacaoItem item=null;if(itemId!=null){item=itens.findById(itemId).orElseThrow();if(!item.getLicitacao().getId().equals(licitacaoId))throw new IllegalArgumentException("Item não pertence à licitação.");}
        Path pasta=raiz.resolve(String.valueOf(licitacaoId)).normalize();if(!pasta.startsWith(raiz))throw new SecurityException("Caminho inválido.");Files.createDirectories(pasta);String armazenado=UUID.randomUUID()+"."+ext;Path destino=pasta.resolve(armazenado).normalize();
        LicitacaoAnexo a=new LicitacaoAnexo();a.setLicitacao(l);a.setItem(item);a.setEtapa(etapa);a.setNomeExibicao(nomeExibicao);a.setNomeOriginal(seguro);a.setNomeArmazenado(armazenado);a.setCaminho(raiz.relativize(destino).toString());a.setMimeType(Optional.ofNullable(arquivo.getContentType()).orElse("application/octet-stream"));a.setTamanho(arquivo.getSize());a.setEnviadoPor(usuarioAtual.obter());
        try{Files.copy(arquivo.getInputStream(),destino,StandardCopyOption.REPLACE_EXISTING);return anexos.save(a);}catch(Exception e){Files.deleteIfExists(destino);throw e;}
    }
    @Transactional(readOnly=true) public Download download(Long id)throws IOException{LicitacaoAnexo a=anexos.findById(id).orElseThrow();Path p=resolver(a);Resource r=new UrlResource(p.toUri());if(!r.exists()||!r.isReadable())throw new NoSuchFileException(a.getNomeOriginal());return new Download(a,r);}
    @Transactional public void excluir(Long licitacaoId,Long id)throws IOException{LicitacaoAnexo a=anexos.findById(id).orElseThrow();if(!a.getLicitacao().getId().equals(licitacaoId))throw new IllegalArgumentException("Anexo inválido.");Files.deleteIfExists(resolver(a));anexos.delete(a);}
    @Transactional public void removerArquivosDaLicitacao(Long id){for(LicitacaoAnexo a:anexos.findByLicitacaoId(id))apagarSilencioso(a);}
    @Transactional public void removerArquivosDoItem(Long id){for(LicitacaoAnexo a:anexos.findByItemId(id)){apagarSilencioso(a);anexos.delete(a);}}
    private void apagarSilencioso(LicitacaoAnexo a){try{Files.deleteIfExists(resolver(a));}catch(IOException ignored){}}
    private Path resolver(LicitacaoAnexo a){Path p=raiz.resolve(a.getCaminho()).normalize();if(!p.startsWith(raiz))throw new SecurityException("Caminho de anexo inválido.");return p;}
    private String extensao(String nome){int i=nome.lastIndexOf('.');return i<0?"":nome.substring(i+1).toLowerCase(Locale.ROOT);}
    public record Download(LicitacaoAnexo anexo,Resource recurso){}
}
