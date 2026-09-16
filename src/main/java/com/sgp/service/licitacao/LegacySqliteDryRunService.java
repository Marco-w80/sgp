package com.sgp.service.licitacao;

import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

@Service
public class LegacySqliteDryRunService {
    public record Relatorio(Path arquivo,Map<String,Long> tabelas,List<String> avisos){}
    public Relatorio analisar(Path arquivo)throws SQLException{
        if(arquivo==null||!arquivo.toFile().isFile())throw new IllegalArgumentException("Banco SQLite não encontrado: "+arquivo);Map<String,Long> contagens=new LinkedHashMap<>();List<String> avisos=new ArrayList<>();
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+arquivo.toAbsolutePath())){try(Statement st=c.createStatement();ResultSet rs=st.executeQuery("select name from sqlite_master where type='table' and name not like 'sqlite_%' order by name")){while(rs.next()){String tabela=rs.getString(1);try(Statement cs=c.createStatement();ResultSet cr=cs.executeQuery("select count(*) from \""+tabela.replace("\"","\"\"")+"\"")){contagens.put(tabela,cr.next()?cr.getLong(1):0);}}}}
        Set<String> esperadas=Set.of("licitacoes","items","itens","quotes","cotacoes","history","historico","attachments","anexos","portfolio","suppliers","fornecedores","supplier_items","fornecedor_itens");if(contagens.keySet().stream().noneMatch(esperadas::contains))avisos.add("Os nomes das tabelas não correspondem ao esquema esperado; ajuste o mapeamento antes de aplicar.");avisos.add("Dry-run somente: nenhum dado foi gravado no MySQL.");return new Relatorio(arquivo,contagens,avisos);
    }
}
