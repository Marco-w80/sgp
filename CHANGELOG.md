# CHANGELOG â€” SGP (Sistema Grupo Prod)

## [2026-07-13] - Página amigável de erro

### Funcionalidade
- Criada uma página padrão para erros HTTP e falhas inesperadas, seguindo as cores e o logotipo do SGP.
- A página apresenta uma causa provável em linguagem simples e gera um código único de suporte.
- O usuário pode copiar um resumo seguro contendo código, data, status e página acessada para enviar à equipe responsável.
- Detalhes internos, mensagens SQL e stack traces não são apresentados na interface; a exceção completa permanece registrada no log pelo código de suporte.

## [2026-07-13] - Correção do status Óbito em Processos

### Correções
- Corrigido o erro ao salvar um processo com status `OBITO` na edição.
- Corrigido o filtro da listagem para localizar processos persistidos como `OBITO`.
- Os selects de cadastro, edição e filtro agora enviam explicitamente o nome da constante (`OBITO`), mantendo o rótulo visual `Óbito`.

### Banco de dados
- A lista permitida nas colunas `processos.status` e `processos_excluidos.status` passou a incluir `OBITO`.
- Registros legados `CONCLUIDO`, que já eram apresentados como Óbito pela aplicação, são normalizados para `OBITO` pelo script `docs/sql/2026-07-13-normalizar-status-obito.sql`.
- A constante `CONCLUIDO` permanece no enum Java somente para compatibilidade de leitura durante implantações graduais.

## [2026-04-29] - Ajuste de status e deferimento em Processos

### Descricao
- Incluido o status `OBITO` no enum de processos.
- `CONCLUIDO` deixou de aparecer em cadastro/edicao/filtros de processos e relatorio.
- Compatibilidade historica mantida: registros antigos com `CONCLUIDO` continuam funcionando e sao exibidos como `OBITO` nas telas.
- Campo de deferimento passou a aceitar preenchimento livre sem depender de selecao de tipo na interface.

### Detalhes Tecnicos
- `StatusProcesso` atualizado com `OBITO`, mantendo `CONCLUIDO` para leitura de legado.
- `ProcessoController` passou a montar lista de status de formulario sem `CONCLUIDO`.
- `DashboardService` normaliza contagem de status legado `CONCLUIDO` para `OBITO`.
- `HomeController` atualizado para KPI/CSS com chave `OBITO`.
- Em `editar-processo`, o tipo de deferimento foi removido da entrada manual (campo oculto com valor padrao interno), preservando persistencia do historico.
- Views atualizadas para mostrar `OBITO` com rotulo `ÓBITO`.

### Observacoes
- Nao houve mudanca de schema de banco.
- Nao houve exclusao ou alteracao destrutiva de dados historicos.

## [2026-04-29] - Feature

### Descricao
Separacao do card unico de "Processos pendentes" da intranet em dois cards especificos com contagem e navegacao por tipo de pendencia.

### Detalhes Tecnicos
- Dashboard (`GET /intranet`) agora exibe:
  - `Sem numero de processo` (campo `numeroProcesso` nulo, vazio ou apenas espacos).
  - `Pendencia documental` (faltando ao menos um documento obrigatorio).
- Novas consultas no `ProcessoRepository` para:
  - contar/listar processos sem numero de processo;
  - contar/listar processos com pendencia documental.
- `ProcessoService` recebeu metodos especificos de contagem e listagem de pendencias.
- Novo endpoint MVC:
  - `GET /processos/pendencias?tipo=sem-numero-processo`
  - `GET /processos/pendencias?tipo=documentacao`
- Nova view `processos/pendencias-processos` com tabela dedicada contendo:
  - ID, interno, numero do processo, paciente, advogado, medico, inicio, status, tipo de pendencia e documentos faltantes.

### Observacoes
- Nao houve alteracao de schema/tabelas.
- Mantido padrao visual atual dos cards e tabelas.

## [2026-03-09] - Feature

### DescriÃ§Ã£o
Implementado controle de acompanhamento operacional de processos com registro de **Ãºltimo acesso** e **Ãºltima ediÃ§Ã£o**, incluindo distinÃ§Ã£o entre eventos `ACESSO` e `EDICAO`.

### Detalhes TÃ©cnicos
- Entidade `Processo` atualizada com campos auxiliares:
  - `ultimoAcessoEm`
  - `ultimoAcessoPor`
  - `ultimaEdicaoEm`
  - `ultimaEdicaoPor`
- Entidade `ProcessoLog` atualizada para suportar trilha de interaÃ§Ã£o:
  - `tipoEvento` (`ACESSO`, `EDICAO`, mantendo compatibilidade com logs de alteraÃ§Ã£o)
  - `usuario`
  - `dataHora` (`data_hora`)
  - Ã­ndice `idx_processo_logs_processo_datahora (processo_id, data_hora)`
- Fluxo de processo atualizado:
  - `GET /processos/editar/{id}` registra evento `ACESSO` automaticamente
  - `POST /processos/editar/{id}` registra evento `EDICAO` apÃ³s persistÃªncia
- Tela `processos/editar-processo` passou a exibir:
  - Ãºltimo acesso (data/hora + usuÃ¡rio)
  - Ãºltima ediÃ§Ã£o (data/hora + usuÃ¡rio)
- Nova base para consultas de acompanhamento:
  - `ProcessoAcompanhamentoDTO`
  - consulta em `ProcessoRepository.buscarAcompanhamento(...)`
  - endpoint `GET /api/processos/acompanhamento?diasSemAcesso=&diasSemEdicao=`

### ObservaÃ§Ãµes
- ImplementaÃ§Ã£o mantida no escopo do mÃ³dulo de processos, sem alteraÃ§Ã£o de regras de status/documentaÃ§Ã£o/itens.
- Projeto permanece com `spring.jpa.hibernate.ddl-auto=update`; novas colunas e Ã­ndice sÃ£o aplicados automaticamente no ambiente atual.

### EvoluÃ§Ã£o da listagem operacional (`/processos/listar`)
- Adicionadas colunas de acompanhamento na tabela:
  - **Ãšltimo acesso**
  - **UsuÃ¡rio** (Ãºltimo usuÃ¡rio que acessou)
  - **Dias sem acesso**
- Adicionados filtros no topo da listagem:
  - `diasSemAcesso`
  - `diasSemEdicao`
- Regras de filtro implementadas:
  - quando `diasSemAcesso` Ã© informado, lista processos com `ultimoAcessoEm` menor/igual ao limite de dias **ou sem acesso registrado**;
  - quando `diasSemEdicao` Ã© informado, lista processos com `ultimaEdicaoEm` menor/igual ao limite de dias **ou sem ediÃ§Ã£o registrada**.
- Backend da listagem atualizado para usar query dedicada (`buscarParaListagemComAcompanhamento`) e cÃ¡lculo de `diasSemAcesso` exibido em tela.

### CorreÃ§Ã£o de compatibilidade em produÃ§Ã£o (`processo_logs`)
- Corrigido erro ao acessar/editar processo em bancos com coluna legada obrigatÃ³ria `data_alteracao`.
- `ProcessoLog` agora sincroniza automaticamente `data_hora` e `data_alteracao` no persist/update, evitando falha SQL:
  - `Field 'data_alteracao' doesn't have a default value`
- Ajuste feito sem alterar schema, garantindo compatibilidade com ambientes jÃ¡ existentes.

### Ajuste de identificaÃ§Ã£o de usuÃ¡rio em acompanhamento
- A captura de usuÃ¡rio nos eventos de processo (`ACESSO`/`EDICAO`) passou a priorizar o **nome do usuÃ¡rio** cadastrado (`usuarios.nome`) em vez do login/e-mail.
- Impacto direto nas telas:
  - `/processos/listar` (coluna â€œUsuÃ¡rioâ€ do Ãºltimo acesso)
  - `/processos/editar/{id}` (bloco de â€œÃšltimo acessoâ€ e â€œÃšltima ediÃ§Ã£oâ€).

### Filtro adicional por status na listagem de acompanhamento
- Na tela `/processos/listar`, os filtros de `diasSemAcesso` e `diasSemEdicao` agora podem ser combinados com filtro de `status`.
- Permite cenÃ¡rios operacionais como: â€œprocessos EM_ANDAMENTO sem acesso hÃ¡ X diasâ€.

### Nova guia no Dashboard: Sem Acompanhamento
- Adicionada nova guia na `/intranet` ao lado de â€œProdutos e PendÃªnciasâ€, com foco em processos sem visita hÃ¡ X dias.
- Novo parÃ¢metro de filtro na intranet: `diasSemVisita` (editÃ¡vel pelo usuÃ¡rio, padrÃ£o 7).
- A guia exibe:
  - KPI de total de processos sem visita no critÃ©rio
  - grÃ¡fico por status dos processos sem visita
  - tabela resumida (paciente, nÂº interno, status, Ãºltimo acesso, dias sem visita, aÃ§Ã£o)

## [2026-03-08] - Improvement

### DescriÃ§Ã£o
Otimizada a pÃ¡gina `/processos/listar` para melhorar percepÃ§Ã£o de carregamento e escalabilidade inicial da listagem.

### Detalhes TÃ©cnicos
- Mantida a paginaÃ§Ã£o jÃ¡ existente da prÃ³pria tabela (DataTables), sem adicionar uma segunda paginaÃ§Ã£o.
- Template `processos/listar-processos.html` atualizado com:
  - loading visual inicial (spinner) atÃ© a tabela estar pronta;
  - nova coluna **Deferimentos** na listagem, exibindo **nÃºmero + tipo** (ex.: `#3 - Juiz` / `#2 - Grupo Prod`);
  - ajustes no DataTables (`deferRender`, `processing`, `stateSave`) para melhor experiÃªncia.

### ObservaÃ§Ãµes
- Mantidas aÃ§Ãµes existentes da tela (editar, visualizar detalhes em modal e criaÃ§Ã£o de processo).
- A mudanÃ§a reduz carga inicial e melhora a experiÃªncia conforme a base cresce.

## [2026-03-08] - Feature

### DescriÃ§Ã£o
Adicionado controle de Ã³bito no fluxo de **ediÃ§Ã£o de processo**, com marcaÃ§Ã£o de Ã³bito do paciente e campo opcional de observaÃ§Ã£o.

### Detalhes TÃ©cnicos
- Entidade `Processo` atualizada com os campos:
  - `obito` (boolean, default `false`)
  - `observacaoObito` (TEXT, opcional)
- Endpoint `POST /processos/editar/{id}` atualizado para receber e persistir:
  - `obito`
  - `observacaoObito`
- Tela `processos/editar-processo` atualizada com:
  - checkbox â€œHouve Ã³bito do pacienteâ€
  - textarea â€œObservaÃ§Ã£o do Ã“bito (opcional)â€

### ObservaÃ§Ãµes
- A marcaÃ§Ã£o de Ã³bito Ã© informativa e **nÃ£o altera** o fluxo de continuidade do processo.
- NÃ£o houve alteraÃ§Ã£o de regras de status, documentos, itens e BI.

## [2026-03-01] - Feature

### DescriÃ§Ã£o
Implementado histÃ³rico de deferimentos no mÃ³dulo de Processos, permitindo mÃºltiplos deferimentos por processo com numeraÃ§Ã£o sequencial automÃ¡tica.

### Detalhes TÃ©cnicos
- Nova entidade `Deferimento` com vÃ­nculo `ManyToOne` para `Processo`.
- Novo enum `TipoDeferimento` com valores `GRUPO_PROD` e `JUIZ`.
- `Processo` agora possui lista de deferimentos (`cascade = ALL`, `orphanRemoval = true`).
- Regra de sequencial por processo implementada no backend (`max + 1`).
- Garantia de unicidade no banco: `UNIQUE(processo_id, numero_deferimento)`.
- Tela de ediÃ§Ã£o de processo atualizada com:
  - lista de deferimentos registrados
  - botÃ£o â€œAdicionar Deferimentoâ€
  - campos de mensagem e tipo
- RelatÃ³rio de processos atualizado para exibiÃ§Ã£o simples dos deferimentos.

### ObservaÃ§Ãµes
- BI/dashboard nÃ£o recebeu integraÃ§Ã£o nesta etapa.
- Regras existentes de status, documentos e itens de produto nÃ£o foram alteradas.

## [2026-04-28] - Feature

### Descricao
Implementada exportacao completa de processos em Excel (.xlsx) nas telas /processos/listar e /relatorios/processos, com aplicacao dos filtros ativos e layout formatado.

### Detalhes Tecnicos
- Adicionada dependencia org.apache.poi:poi-ooxml para geracao de planilhas.
- Criado servico ProcessoExcelService para centralizar a montagem do arquivo Excel.
- Novos endpoints:
  - GET /processos/exportar-excel
  - GET /relatorios/processos/exportar-excel
- Ambos os endpoints reutilizam a mesma logica de filtros ja existente em cada tela.
- Templates atualizados com botao Exportar Excel preservando query params de filtro.
- Layout da planilha inclui titulo, cabecalho destacado, bordas, alinhamento, auto filtro e ajuste de colunas.

### Observacoes
- Nao houve alteracao de schema/tabelas.
- Nao houve alteracao de regras de negocio de processos.



## [2026-04-29] - Feature

### Descricao
Implementado envio automatico diario por e-mail, as 09:00 (America/Sao_Paulo), com resumo de processos pendentes sem acesso ha 10 dias ou mais, limitado aos status ABERTO e EM_ANDAMENTO.

### Detalhes Tecnicos
- Scheduling habilitado com `@EnableScheduling` em `SgpApplication`.
- Criado `ProcessoResumoPendenteEmailScheduler` com cron configuravel por propriedade:
  - `app.alertas.email.resumo.cron=0 0 9 * * *`
  - `app.alertas.email.resumo.zone=America/Sao_Paulo`
- Criado `ProcessoResumoPendenteEmailService` para:
  - buscar processos com status `ABERTO` e `EM_ANDAMENTO`;
  - calcular dias sem acesso com base em `ultimoAcessoEm`;
  - quando `ultimoAcessoEm` for nulo, usar `dataInicio` como referencia;
  - identificar pendencias documentais obrigatorias (CPF, Comprovante de Residencia, Comprovante de Renda, Procuracao e Declaracao de Insuficiencia);
  - montar e enviar e-mail HTML tabular;
  - enviar tambem quando nao houver pendencias, com mensagem informativa.
- `AlertaEmailService` ampliado para envio HTML (`enviarEmailHtml`) reaproveitando o SMTP ja configurado.
- `ProcessoRepository` ampliado com `findByStatusInOrderByIdDesc(...)` para filtrar status no banco.
- Novo destinatario de resumo por propriedade: `app.alertas.email.resumo.to`.

### Observacoes
- Falhas de envio sao tratadas com log de erro, sem impacto no fluxo principal do sistema.
- Nao houve alteracao de schema/tabelas.

## [2026-04-29] - Feature (Configuração de alertas)

### Descricao
Adicionada tela administrativa para configurar o resumo diario de processos pendentes por e-mail, com envio manual de teste.

### Detalhes Tecnicos
- Nova entidade `AlertaResumoConfig` (tabela `alerta_resumo_config`) para persistir:
  - emails de destino
  - dias minimos sem acesso
  - flag de rotina ativa
  - flag para enviar mesmo sem resultados
- Novo controller web: `AlertaResumoConfigController`:
  - `GET /intranet/alertas/config`
  - `POST /intranet/alertas/config/salvar`
  - `POST /intranet/alertas/config/enviar-teste`
- Nova tela Thymeleaf: `alertas/config-resumo-diario.html`.
- Menu lateral recebeu item `Alertas` para acesso rapido a configuracao.
- `ProcessoResumoPendenteEmailService` passou a ler configuracao persistida e aplicar filtros dinamicos.

### Observacoes
- A rotina agendada continua as 09:00 (America/Sao_Paulo), mas agora pode ser ativada/desativada via tela.
- Envio manual usa as mesmas regras e o mesmo SMTP da rotina automatica.

### Melhoria visual do e-mail de resumo diario
- O e-mail de resumo diario foi redesenhado com layout HTML mais moderno (cabecalho visual, bloco de resumo e tabela estilizada).
- Inclusao da logo da GrupoProd no cabecalho do e-mail (`static/img/logo.png`) via inline image.
- O envio agora inclui anexo Excel (`.xlsx`) com os processos considerados no resumo do dia.
- Botao visual no corpo do e-mail orienta o download pelo anexo.


### Configuracao de horario na tela de alertas
- A pagina `/intranet/alertas/config` agora permite definir o horario de envio (`HH:mm`) da rotina automatica.
- O scheduler foi ajustado para verificacao a cada minuto e disparo no horario configurado, sem necessidade de alterar properties.
- Incluida protecao para evitar mais de um envio automatico no mesmo dia (`ultimaExecucaoEm`).
