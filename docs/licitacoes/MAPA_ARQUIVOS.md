# Mapa de arquivos

## Dashboard / Kanban

- Controller: `controller/licitacao/LicitacaoController.java`
- Regra/DTOs: `service/licitacao/LicitacaoService.java`
- Consulta: `repository/licitacao/LicitacaoRepository.java`
- Template: `templates/licitacoes/dashboard.html`
- Visual/comportamento: `static/css/licitacoes.css`, `static/js/licitacoes.js`

## Cadastro e detalhes

- Entidade: `model/licitacao/Licitacao.java`
- Controller/service: `LicitacaoController.java`, `LicitacaoService.java`
- Templates: `templates/licitacoes/formulario.html`, `detalhes.html`
- Consulta PNCP: `controller/licitacao/PncpController.java`, `service/licitacao/PncpService.java`
- Pré-preenchimento PNCP: `static/js/licitacoes.js`

## Itens, aprovação e participação

- Entidade/enums: `LicitacaoItem.java`, `DecisaoItem.java`, `ResultadoItem.java`
- Repository: `LicitacaoItemRepository.java`
- Regra e batch: `LicitacaoService.java`
- UI: abas Cadastro, Aprovação, Definição e Participação em `detalhes.html`

## Etapas e histórico

- Fluxo: `EtapaLicitacao.java`
- Histórico: `LicitacaoHistorico.java`, `LicitacaoHistoricoRepository.java`
- Validação/snapshot: `LicitacaoService.transicionar`
- UI: stepper, ações e aba Histórico em `detalhes.html`

## Cotações e exportação

- Entidade/repository: `CotacaoLicitacao.java`, `CotacaoLicitacaoRepository.java`
- CRUD: `LicitacaoController.java`, `LicitacaoService.java`
- CSV: `ExportacaoCotacaoService.java`
- UI: aba Cotação em `detalhes.html`

## Importação em massa

- Parser: `ImportacaoTabularService.java`
- Batch de licitação: `LicitacaoService.importarItens`
- Batch de fornecedor: `PortfolioService.importarItens`
- Prévia: `static/js/licitacoes.js`

## Anexos

- Entidade/repository: `LicitacaoAnexo.java`, `LicitacaoAnexoRepository.java`
- Storage: `LicitacaoAnexoService.java`
- Rotas: `LicitacaoController.java`
- UI: aba Anexos em `detalhes.html`

## Portfólio e fornecedores

- Entidades: `PortfolioEstoque.java`, `FornecedorLicitacao.java`, `FornecedorItem.java`
- Repositories homônimos em `repository/licitacao`
- Service/controller: `PortfolioService.java`, `PortfolioController.java`
- Template: `templates/licitacoes/portfolio.html`

## Banco, segurança e operação

- Migration: `resources/db/migration/V2026091501__criar_modulo_licitacoes.sql`
- Configuração: `application.properties`, `.gitignore`, `SecurityConfig.java`
- Usuário autenticado: `UsuarioAtualService.java`
- Menu: `templates/fragments/head.html`
- SQLite dry-run isolado: `LegacySqliteDryRunService.java`, `LegacySqliteDryRunCli.java`

## Testes

- `src/test/java/com/sgp/service/licitacao/ImportacaoTabularServiceTest.java`
- `src/test/java/com/sgp/service/licitacao/LicitacaoServiceTest.java`
- `src/test/java/com/sgp/service/licitacao/LicitacaoAnexoServiceTest.java`
- `src/test/java/com/sgp/service/licitacao/PortfolioServiceTest.java`
- `src/test/java/com/sgp/service/licitacao/LegacySqliteDryRunServiceTest.java`
- `src/test/java/com/sgp/controller/licitacao/LicitacaoControllerTest.java`
- `src/test/java/com/sgp/controller/licitacao/PncpControllerTest.java`
- `src/test/java/com/sgp/service/licitacao/PncpServiceTest.java`
- Ambiente: `src/test/resources/application.properties`
