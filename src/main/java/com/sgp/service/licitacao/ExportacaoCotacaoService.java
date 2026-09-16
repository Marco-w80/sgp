package com.sgp.service.licitacao;

import com.sgp.model.licitacao.*;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
public class ExportacaoCotacaoService {
    public byte[] csv(Licitacao l){StringBuilder s=new StringBuilder("Nº Item;Descrição;Quantidade;Unidade;Valor Referência;Fornecedor;Valor Unitário Cotado;Data Cotação;Observações Cotação\r\n");for(LicitacaoItem i:l.getItens()){if(!i.isSelecionadoCotacao())continue;if(i.getCotacoes().isEmpty())linha(s,i,null);else for(CotacaoLicitacao c:i.getCotacoes())linha(s,i,c);}byte[] corpo=s.toString().getBytes(StandardCharsets.UTF_8);byte[] bom={(byte)0xEF,(byte)0xBB,(byte)0xBF};byte[] saida=new byte[bom.length+corpo.length];System.arraycopy(bom,0,saida,0,bom.length);System.arraycopy(corpo,0,saida,bom.length,corpo.length);return saida;}
    private void linha(StringBuilder s,LicitacaoItem i,CotacaoLicitacao c){campo(s,i.getNumeroItem());campo(s,i.getDescricao());campo(s,i.getQuantidade());campo(s,i.getUnidade());campo(s,i.getValorReferencia());campo(s,c==null?null:c.getFornecedorNome());campo(s,c==null?null:c.getValorUnitario());campo(s,c==null||c.getDataCotacao()==null?null:c.getDataCotacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));ultimo(s,c==null?null:c.getObservacoes());}
    private void campo(StringBuilder s,Object v){s.append(esc(v)).append(';');} private void ultimo(StringBuilder s,Object v){s.append(esc(v)).append("\r\n");}
    private String esc(Object v){if(v==null)return "";String x=String.valueOf(v).replace("\"","\"\"");return (x.contains(";")||x.contains("\"")||x.contains("\n")||x.contains("\r"))?"\""+x+"\"":x;}
}
