package com.sgp.service.licitacao;

import java.nio.file.Path;

/** CLI isolada: não inicializa Spring, JPA, Hibernate ou o datasource MySQL. */
public final class LegacySqliteDryRunCli {
    private LegacySqliteDryRunCli() {}
    public static void main(String[] args) throws Exception {
        if (args.length != 1 || args[0].isBlank()) {
            System.err.println("Uso: LegacySqliteDryRunCli <caminho/sistema.db>");
            System.exit(2);
        }
        var relatorio = new LegacySqliteDryRunService().analisar(Path.of(args[0]));
        System.out.println("DRY-RUN SQLITE: " + relatorio.arquivo());
        relatorio.tabelas().forEach((tabela, total) -> System.out.println(tabela + ": " + total + " registros"));
        relatorio.avisos().forEach(aviso -> System.out.println("AVISO: " + aviso));
    }
}
