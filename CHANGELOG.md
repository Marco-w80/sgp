# CHANGELOG — SGP (Sistema Grupo Prod)

## [2026-03-08] - Improvement

### Descrição
Otimizada a página `/processos/listar` para melhorar percepção de carregamento e escalabilidade inicial da listagem.

### Detalhes Técnicos
- Mantida a paginação já existente da própria tabela (DataTables), sem adicionar uma segunda paginação.
- Template `processos/listar-processos.html` atualizado com:
  - loading visual inicial (spinner) até a tabela estar pronta;
  - nova coluna **Deferimentos** na listagem, exibindo **número + tipo** (ex.: `#3 - Juiz` / `#2 - Grupo Prod`);
  - ajustes no DataTables (`deferRender`, `processing`, `stateSave`) para melhor experiência.

### Observações
- Mantidas ações existentes da tela (editar, visualizar detalhes em modal e criação de processo).
- A mudança reduz carga inicial e melhora a experiência conforme a base cresce.

## [2026-03-08] - Feature

### Descrição
Adicionado controle de óbito no fluxo de **edição de processo**, com marcação de óbito do paciente e campo opcional de observação.

### Detalhes Técnicos
- Entidade `Processo` atualizada com os campos:
  - `obito` (boolean, default `false`)
  - `observacaoObito` (TEXT, opcional)
- Endpoint `POST /processos/editar/{id}` atualizado para receber e persistir:
  - `obito`
  - `observacaoObito`
- Tela `processos/editar-processo` atualizada com:
  - checkbox “Houve óbito do paciente”
  - textarea “Observação do Óbito (opcional)”

### Observações
- A marcação de óbito é informativa e **não altera** o fluxo de continuidade do processo.
- Não houve alteração de regras de status, documentos, itens e BI.

## [2026-03-01] - Feature

### Descrição
Implementado histórico de deferimentos no módulo de Processos, permitindo múltiplos deferimentos por processo com numeração sequencial automática.

### Detalhes Técnicos
- Nova entidade `Deferimento` com vínculo `ManyToOne` para `Processo`.
- Novo enum `TipoDeferimento` com valores `GRUPO_PROD` e `JUIZ`.
- `Processo` agora possui lista de deferimentos (`cascade = ALL`, `orphanRemoval = true`).
- Regra de sequencial por processo implementada no backend (`max + 1`).
- Garantia de unicidade no banco: `UNIQUE(processo_id, numero_deferimento)`.
- Tela de edição de processo atualizada com:
  - lista de deferimentos registrados
  - botão “Adicionar Deferimento”
  - campos de mensagem e tipo
- Relatório de processos atualizado para exibição simples dos deferimentos.

### Observações
- BI/dashboard não recebeu integração nesta etapa.
- Regras existentes de status, documentos e itens de produto não foram alteradas.
