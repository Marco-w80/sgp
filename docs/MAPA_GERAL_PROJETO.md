# Mapa geral do projeto

| Módulo | Principais classes/templates |
|---|---|
| Autenticação/usuários | `SecurityConfig`, `AuthController`, `UsuarioController`, `UsuarioService`, `Usuario`, `templates/auth`, `templates/usuarios` |
| Dashboard | `DashboardController`, `DashboardService`, `templates/intranet/dashboard.html` |
| Processos | `ProcessoController`, `ProcessoService`, `ProcessoLogService`, `ProcessoExcelService`, entidades/repositories `Processo*`, `templates/processos` |
| Pessoas | `PessoaController`, `PessoaApiController`, entidades `Pessoa`, `Paciente`, `Advogado`, `Medico`, `templates/pessoas` |
| Produtos | `ProdutoController`, `ProdutoRepository`, `Produto`, `ProcessoProduto`, `templates/produtos` |
| Hospitais/locais | `HospitalController`, `LocalController`, APIs/repositories/entidades correspondentes, `templates/hospitais`, `templates/locais` |
| Doenças | `DoencaController`, `DoencaApiController`, `Doenca`, `GrupoDoenca`, repositories e `templates/doencas` |
| Alertas | `AlertaResumoConfigController`, `AlertaTesteController`, services de e-mail/scheduler, `AlertaResumoConfig` |
| Relatórios | `RelatorioProcessosController`, `ProcessoExcelService`, `templates/relatorios` |
| Manutenção | `MaintenanceLogController`, `MaintenanceLogService`, entidade/repository, `templates/manutencao` |
| Licitações | pacotes `*.licitacao`, `templates/licitacoes`, `docs/licitacoes/MAPA_ARQUIVOS.md` |

Recursos globais: `templates/fragments/head.html` e `foot.html`; identidade/JS/vendor em `resources/static`; configuração em `application.properties`; build em `pom.xml`.
