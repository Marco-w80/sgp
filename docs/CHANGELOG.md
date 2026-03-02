# CHANGELOG — SGP (Sistema Grupo Prod)

Todas as alterações estruturais e funcionais relevantes do sistema devem ser registradas aqui.

Formato:
- Data
- Tipo (Feature, Fix, Refactor, Improvement)
- Descrição objetiva
- Impacto técnico (se houver)

---

## [2026-03-01] - Feature

### Descrição
Implementado histórico de deferimentos no módulo de Processos, permitindo múltiplos deferimentos por processo com numeração sequencial automática.

### Detalhes Técnicos
- Arquivos afetados:
  - `src/main/java/com/sgp/model/Deferimento.java`
  - `src/main/java/com/sgp/model/TipoDeferimento.java`
  - `src/main/java/com/sgp/model/Processo.java`
  - `src/main/java/com/sgp/repository/DeferimentoRepository.java`
  - `src/main/java/com/sgp/controller/ProcessoController.java`
  - `src/main/resources/templates/processos/editar-processo.html`
  - `src/main/resources/templates/relatorios/processo.html`
  - `.mvn/wrapper/docs/03-MODULOS-E-FLUXOS.md`
  - `.mvn/wrapper/docs/05-BANCO-DE-DADOS.md`
- Camadas impactadas:
  - Model, Repository, Controller, Views (Thymeleaf), Documentação
- Banco de dados:
  - Nova tabela `deferimentos`
  - Constraint de unicidade `UNIQUE(processo_id, numero_deferimento)`
  - Geração automática via Hibernate (`ddl-auto=update`)
- Observações importantes:
  - Dashboard/BI não foi alterado
  - Relatório de processos passou a exibir deferimentos em lista simples
  - Regras existentes de status, documentos e itens de produto foram preservadas

## [YYYY-MM-DD] - Tipo

### Descrição
Texto explicando o que foi alterado.

### Detalhes Técnicos
- Arquivos afetados:
- Camadas impactadas:
- Banco de dados:
- Observações importantes:


