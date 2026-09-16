package com.sgp.service.licitacao;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.sql.DriverManager;
import static org.assertj.core.api.Assertions.assertThat;

class LegacySqliteDryRunServiceTest {
    @Test void contaTabelasSemAlterarOrigem() throws Exception {
        var arquivo=Files.createTempFile("licitacoes-legado-",".db");
        try {
            try(var c=DriverManager.getConnection("jdbc:sqlite:"+arquivo);var st=c.createStatement()){
                st.execute("create table licitacoes(id integer primary key, orgao text)");
                st.execute("insert into licitacoes(orgao) values ('A'),('B')");
            }
            var relatorio=new LegacySqliteDryRunService().analisar(arquivo);
            assertThat(relatorio.tabelas()).containsEntry("licitacoes",2L);
            try(var c=DriverManager.getConnection("jdbc:sqlite:"+arquivo);var st=c.createStatement();var rs=st.executeQuery("select count(*) from licitacoes")){assertThat(rs.getLong(1)).isEqualTo(2);}
        } finally { Files.deleteIfExists(arquivo); }
    }
}
