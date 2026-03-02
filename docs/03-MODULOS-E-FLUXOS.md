# Módulos e Fluxos — SGP

## Módulo de Processos — atualização (2026-03-01)

Foi implementado o **histórico de deferimentos** no fluxo de edição de processos.

### O que mudou
- Processo agora possui relacionamento `1:N` com deferimentos.
- Na tela de edição (`/processos/editar/{id}`):
  - exibe deferimentos já cadastrados;
  - botão **Adicionar Deferimento**;
  - campos `tipo` (Grupo Prod / Juiz) e `mensagem`.
- Ao salvar, o sistema gera automaticamente o próximo `numeroDeferimento` do processo.

### Regras
- Numeração sequencial por processo.
- Não permite dois deferimentos com mesmo número no mesmo processo.

### Escopo
- Integrado no módulo de Processos e no relatório de processos (lista simples).
- **Sem integração com BI/dashboard nesta etapa.**
