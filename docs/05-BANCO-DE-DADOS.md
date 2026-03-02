# Banco de Dados — SGP

## Atualização (2026-03-01): tabela `deferimentos`

Nova entidade para histórico de deferimentos de cada processo.

### Tabela: `deferimentos`
- `id` (PK, bigint, auto increment)
- `processo_id` (FK -> `processos.id`, NOT NULL)
- `numero_deferimento` (int, NOT NULL)
- `mensagem` (TEXT, NOT NULL)
- `tipo` (varchar enum, NOT NULL): `GRUPO_PROD`, `JUIZ`
- `data_registro` (datetime, NOT NULL)

### Integridade
- Restrição de unicidade: `UNIQUE(processo_id, numero_deferimento)`.

### Relacionamento com `processos`
- `Processo` possui `List<Deferimento>` com:
  - `cascade = ALL`
  - `orphanRemoval = true`

### Regra de negócio persistida
- `numero_deferimento` é sequencial por processo (calculado no backend como `max + 1`).

### Observação
- Estrutura existente de `processos` não foi alterada além do novo relacionamento.
