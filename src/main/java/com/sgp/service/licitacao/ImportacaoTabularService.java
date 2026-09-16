package com.sgp.service.licitacao;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ImportacaoTabularService {
    public enum Delimitador { AUTOMATICO, TAB, VIRGULA, PONTO_VIRGULA, ESPACOS }
    public record LinhaImportada(Integer numero,String descricao,BigDecimal quantidade,String unidade,BigDecimal valor,boolean numeroGerado){}
    public record LinhaFornecedor(String nome,String marca,BigDecimal preco){}

    public List<LinhaImportada> lerItens(String texto,Delimitador delimitador,boolean cabecalho,int colNumero,int colDescricao,int colQuantidade,int colUnidade,int colValor){
        List<String[]> linhas=separar(texto,delimitador,cabecalho); List<LinhaImportada> saida=new ArrayList<>(); int sequencia=1;
        for(String[] c:linhas){String descricao=celula(c,colDescricao);if(descricao.isBlank())continue;Integer numero=inteiro(celula(c,colNumero));boolean gerado=numero==null;if(numero==null)numero=sequencia;sequencia=Math.max(sequencia,numero+1);BigDecimal qtd=decimal(celula(c,colQuantidade));if(qtd==null)qtd=BigDecimal.ONE;String unidade=celula(c,colUnidade);if(unidade.isBlank())unidade="UN";saida.add(new LinhaImportada(numero,descricao,qtd,unidade,decimal(celula(c,colValor)),gerado));}
        if(saida.isEmpty())throw new IllegalArgumentException("Nenhum item válido. A descrição é obrigatória."); return saida;
    }
    public List<LinhaFornecedor> lerFornecedor(String texto,Delimitador delimitador,boolean cabecalho,int colNome,int colMarca,int colPreco){
        List<LinhaFornecedor> saida=new ArrayList<>();for(String[] c:separar(texto,delimitador,cabecalho)){String nome=celula(c,colNome);if(!nome.isBlank())saida.add(new LinhaFornecedor(nome,celula(c,colMarca),decimal(celula(c,colPreco))));}if(saida.isEmpty())throw new IllegalArgumentException("Nenhum item válido. O nome é obrigatório.");return saida;
    }
    public BigDecimal decimal(String valor){
        if(valor==null||valor.isBlank())return null;String s=valor.trim().replace("R$","").replace(" ","");
        if(s.matches("[-+]?\\d{1,3}(\\.\\d{3})+(,\\d+)?"))s=s.replace(".","").replace(',', '.');
        else if(s.contains(","))s=s.replace(".","").replace(',', '.');
        else if(s.matches("[-+]?\\d{1,3}(\\.\\d{3})+"))s=s.replace(".","");
        try{return new BigDecimal(s);}catch(NumberFormatException e){throw new IllegalArgumentException("Número inválido: "+valor);}
    }
    private Integer inteiro(String s){if(s==null||s.isBlank())return null;try{return decimal(s).intValueExact();}catch(Exception e){throw new IllegalArgumentException("Número do item inválido: "+s);}}
    private List<String[]> separar(String texto,Delimitador d,boolean cabecalho){
        if(texto==null||texto.isBlank())throw new IllegalArgumentException("Cole ao menos uma linha.");String[] brutas=texto.strip().split("\\R");Delimitador efetivo=d==Delimitador.AUTOMATICO?detectar(brutas):d;List<String[]> r=new ArrayList<>();for(int i=cabecalho?1:0;i<brutas.length;i++){if(!brutas[i].isBlank())r.add(separarLinha(brutas[i],efetivo));}return r;
    }
    private Delimitador detectar(String[] linhas){String l=linhas.length==0?"":linhas[0];if(l.contains("\t"))return Delimitador.TAB;if(l.contains(";"))return Delimitador.PONTO_VIRGULA;if(l.contains(","))return Delimitador.VIRGULA;return Delimitador.ESPACOS;}
    private String[] separarLinha(String linha,Delimitador d){if(d==Delimitador.ESPACOS)return linha.trim().split("\\s{2,}",-1);char separador=switch(d){case TAB->'\t';case VIRGULA->',';case PONTO_VIRGULA->';';case AUTOMATICO->throw new IllegalStateException();case ESPACOS->' ';};List<String> colunas=new ArrayList<>();StringBuilder atual=new StringBuilder();boolean aspas=false;for(int i=0;i<linha.length();i++){char c=linha.charAt(i);if(c=='\"'){if(aspas&&i+1<linha.length()&&linha.charAt(i+1)=='\"'){atual.append('\"');i++;}else aspas=!aspas;}else if(c==separador&&!aspas){colunas.add(atual.toString());atual.setLength(0);}else atual.append(c);}colunas.add(atual.toString());return colunas.toArray(String[]::new);}
    private String celula(String[] c,int i){return i>=0&&i<c.length?c[i].trim():"";}
}
