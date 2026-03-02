# CHANGELOG — SGP (Sistema Grupo Prod)

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
