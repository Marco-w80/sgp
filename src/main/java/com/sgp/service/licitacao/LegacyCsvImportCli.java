package com.sgp.service.licitacao;

import com.sgp.SgpApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

import java.nio.file.Path;
import java.util.Map;

/** Importador administrativo isolado. Exige a confirmação explícita --apply. */
public final class LegacyCsvImportCli {
    private LegacyCsvImportCli() { }

    public static void main(String[] args) throws Exception {
        System.setProperty("spring.devtools.restart.enabled", "false");
        System.setProperty("spring.devtools.livereload.enabled", "false");
        String diretorio = null;
        if (args.length == 2 && "--apply".equals(args[1])) diretorio = args[0];
        else if (args.length == 1 && args[0].endsWith(" --apply")) diretorio = args[0].substring(0, args[0].length() - 8).trim();
        else if ("true".equalsIgnoreCase(System.getenv("LEGACY_CSV_APPLY"))) diretorio = System.getenv("LEGACY_CSV_DIR");
        if (diretorio == null || diretorio.isBlank()) {
            System.err.println("Uso: LegacyCsvImportCli <diretório-dos-csvs> --apply");
            System.exit(2);
        }
        SpringApplication aplicacao = new SpringApplication(SgpApplication.class);
        aplicacao.setWebApplicationType(WebApplicationType.SERVLET);
        aplicacao.setAdditionalProfiles("legacy-import");
        aplicacao.setDefaultProperties(Map.of(
                "spring.task.scheduling.enabled", "false",
                "spring.devtools.restart.enabled", "false",
                "spring.devtools.add-properties", "false",
                "spring.devtools.livereload.enabled", "false",
                "spring.jpa.show-sql", "false",
                "logging.level.org.hibernate.SQL", "OFF",
                "server.port", "0",
                "spring.main.banner-mode", "off"));
        try (var contexto = aplicacao.run()) {
            var service = contexto.getBean(LegacyCsvImportService.class);
            var relatorio = service.importar(LegacyCsvImportService.Arquivos.doDiretorio(Path.of(diretorio).toAbsolutePath().normalize()));
            System.out.println("Importação concluída. Novos: " + relatorio.totalImportado()
                    + "; reconciliados: " + relatorio.totalReconciliado() + ".");
            relatorio.avisos().forEach(aviso -> System.out.println("AVISO: " + aviso));
        }
    }
}
