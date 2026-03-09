# Banco de Dados — SGP (Sistema Grupo Prod)

Este documento descreve o modelo de dados baseado nas entidades JPA do sistema.
Banco utilizado: **MySQL (AWS RDS)**.

> Observação: nomes de tabelas seguem os `@Table(...)` quando definidos; quando não definidos, o nome padrão da tabela tende a ser o nome da entidade (pode variar conforme naming strategy do Hibernate).

---

## Visão Geral (Principais Entidades)

Núcleo do sistema:
- **processos** (`Processo`) — entidade central
- **deferimentos** (`Deferimento`) — histórico de deferimentos por processo
- **pessoas** (`Pessoa` + subclasses `Paciente`, `Advogado`, `Medico`) — cadastro polimórfico
- **produtos** (`Produto`) — itens/medicamentos
- **processo_produtos** (`ProcessoProduto`) — itens enviados por processo (produto, data, quantidade)
- **processo_logs** (`ProcessoLog`) — trilha de auditoria de mudanças em campos
- **processos_excluidos** (`ProcessoExcluido`) — “arquivo morto”/histórico após exclusão
- Cadastros complementares: `Hospital`, `Local`, `Doenca`, `Endereco`, `Usuario`
- Apoio: `maintenance_log` (`MaintenanceLog`) — registro interno de manutenção

---

## Tabelas e Campos

### 1) `processos` — Processo (entidade central)
**Tabela:** `processos`

Campos:
- `id` (PK, bigint, auto increment)
- `numero_interno` (varchar, **NOT NULL**, **UNIQUE**) — identificador interno obrigatório
- `numero_processo` (varchar, NULL) — número do processo (opcional)
- `paciente_id` (FK -> `pessoas.id`, NOT NULL) — paciente obrigatório
- `advogado_id` (FK -> `pessoas.id`, NULL) — opcional
- `medico_id` (FK -> `pessoas.id`, NULL) — opcional
- `hospital_id` (FK -> `hospital.id` (ou tabela equivalente), NULL) — opcional
- `doenca_id` (FK -> `doenca.id` (ou tabela equivalente), NOT NULL)
- `local_id` (FK -> `locais.id`, NULL)
- `data_inicio` (date, NOT NULL)
- `status` (varchar enum, NOT NULL) — `StatusProcesso` (ABERTO, EM_ANDAMENTO, CONCLUIDO, SUSPENSO)
- `tipo_hospital` (varchar enum, NULL) — `TipoHospital` (SUS, CONVENIO, PARTICULAR)
- Flags de documentação (boolean, NOT NULL, default false):
  - `cpf_anexado`
  - `comp_residencia_anexado`
  - `comp_renda_anexado`
  - `procuracao_anexado`
  - `declaracao_insuficiencia_anexado`
- `obito` (boolean, NOT NULL, default false) — marcação informativa de óbito do paciente
- `observacao_obito` (TEXT, NULL) — observação opcional do óbito
- Campos de acompanhamento operacional (novos):
  - `ultimo_acesso_em` (datetime, NULL)
  - `ultimo_acesso_por` (varchar, NULL)
  - `ultima_edicao_em` (datetime, NULL)
  - `ultima_edicao_por` (varchar, NULL)
- `obs` (TEXT, NULL)

Relacionamentos:
- 1:N com `processo_produtos` (itens) — cascade + orphanRemoval
- 1:N com `processo_logs` (logs) — cascade + orphanRemoval (fetch LAZY)
- 1:N com `deferimentos` (histórico) — cascade + orphanRemoval

Cuidados:
- Alterar enum `StatusProcesso` impacta: dashboard, relatórios, filtros e contagens.
- `numero_interno` é chave única e usada como referência operacional.

---

### 2) `deferimentos` — Histórico de Deferimentos
**Tabela:** `deferimentos`

Campos:
- `id` (PK, bigint, auto increment)
- `processo_id` (FK -> `processos.id`, NOT NULL)
- `numero_deferimento` (int, NOT NULL)
- `mensagem` (TEXT, NOT NULL)
- `tipo` (varchar enum, NOT NULL) — `TipoDeferimento` (`GRUPO_PROD`, `JUIZ`)
- `data_registro` (datetime, NOT NULL)

Restrições:
- `UNIQUE (processo_id, numero_deferimento)`

Regras:
- `numero_deferimento` é sequencial por processo (gerado no backend: `max + 1`).
- Não pode haver dois deferimentos com mesmo número para o mesmo processo.

---

### 3) `processo_produtos` — Itens do Processo
**Tabela:** `processo_produtos`

Campos:
- `id` (PK, auto increment)
- `processo_id` (FK -> `processos.id`, NOT NULL)
- `produto_id` (FK -> `produtos.id`, NOT NULL) — fetch EAGER
- `data_envio` (date, NOT NULL)
- `quantidade` (int, NOT NULL)

Objetivo:
- Registrar envios/itens associados a um processo (produto + data + quantidade).

Cuidados:
- Como é `orphanRemoval=true`, remover itens do processo remove registros da tabela.
- Integra diretamente com análises de consumo e controle futuro de entradas/saídas.

---

### 4) `processo_logs` — Auditoria de Alterações
**Tabela:** `processo_logs`

Campos:
- `id` (PK, auto increment)
- `processo_id` (FK -> `processos.id`, NOT NULL)
- `tipo_evento` (varchar, NOT NULL)
  - valores utilizados no sistema: `ACESSO`, `EDICAO`
  - valores de negócio previstos para evolução: `CRIACAO`, `STATUS_ALTERADO`
- `usuario` (varchar, NULL)
- `campo` (varchar, NOT NULL) — nome do campo alterado (ex.: "Advogado")
- `valor_antigo` (TEXT, NULL)
- `valor_novo` (TEXT, NULL)
- `data_hora` (datetime, NOT NULL)

Índices:
- `idx_processo_logs_processo_datahora (processo_id, data_hora)`

Objetivo:
- Registrar histórico de mudanças e interações relevantes feitas em processos.
- Suportar rastreabilidade de **último acesso** e **última edição**.

---

### 5) `processos_excluidos` — Arquivo Morto / Histórico de Exclusões
**Tabela:** `processos_excluidos`

Campos:
- `id` (PK) — **mesmo ID original do processo** (não auto-gerado)
- Cópia “achatada” dos dados do processo:
  - `numeroInterno`, `numeroProcesso`, `dataInicio`, `status`, `tipoHospital`, `obs`
- Flags de documentação:
  - `cpfAnexado`, `compResidenciaAnexado`, `compRendaAnexado`, `procuracaoAnexado`, `declaracaoInsuficienciaAnexado`
- “Snapshot” textual dos relacionamentos:
  - `pacienteNome`, `pacienteCpf`
  - `advogadoNome`, `advogadoOab`
  - `medicoNome`, `medicoCrm`
  - `hospitalNome`
  - `doencaNome`
  - `localDescricao`
- Auditoria da exclusão:
  - `dataExclusao` (datetime)
  - `usuarioExclusao` (varchar)

Objetivo:
- Preservar uma fotografia do processo excluído para auditoria e consulta.

Cuidados:
- Qualquer mudança em `ProcessoService.excluir(...)` deve garantir consistência deste “snapshot”.

---

### 6) `pessoas` — Pessoa (Herança SINGLE_TABLE)
**Tabela:** `pessoas` (`@Table(name="pessoas")`)

Estratégia:
- `@Inheritance(SINGLE_TABLE)`
- `@DiscriminatorColumn(name="tipo_pessoa")`

Campos comuns:
- `id` (PK, auto increment)
- `tipo_pessoa` (discriminator: PACIENTE / ADVOGADO / MEDICO)
- `nome` (NOT NULL)
- `sexo` (enum string `Sexo`: MASCULINO, FEMININO, OUTRO)
- `data_nascimento` (date, NULL)
- `cpf` (varchar(14), NULL) — **não está marcado unique no model** (mas existe validação/erro por CPF duplicado no fluxo)
- `identidade` (varchar, NULL)

Relações:
- 1:N com `enderecos` — cascade + orphanRemoval

Subtipos:
- **Paciente**: sem campos extras
- **Advogado**: campo `oab` (unique, nullable)
- **Medico**: campo `crm` (unique, nullable)

Cuidados:
- Como é SINGLE_TABLE, `oab` e `crm` ficam na mesma tabela (colunas que podem ficar nulas dependendo do tipo).
- `getTipo()` e `getDocumentoEspecial()` são `@Transient` (não persistidos).

---

### 7) `enderecos` — Endereço
**Tabela:** `enderecos`

Campos:
- `id` (PK, auto increment)
- `logradouro`, `numero`, `complemento`, `bairro`, `cidade`, `estado`, `cep` (strings)
- `pessoa_id` (FK -> `pessoas.id`) — many-to-one LAZY

Cuidados:
- Alterações em endereços usam `orphanRemoval=true` via `Pessoa`, então “limpar e adicionar” substitui registros.

---

### 8) `produtos` — Produto
**Tabela:** `produtos`

Campos:
- `id` (PK, auto increment)
- `codigo` (varchar, NOT NULL, **UNIQUE**)
- `nome_item` (varchar, NOT NULL)
- `principio_ativo` (varchar, NULL) — no model está como `nomeprincipio`
- `unidade_medida` (varchar, NOT NULL)
- `sigla_unidade` (varchar, NOT NULL)
- `grupo` (enum string `GrupoProduto`: MEDICAMENTOS, OUTROS)
- `data_cadastro` (date, NOT NULL)
- `especificacoes` (TEXT, NULL)
- `fabricante` (varchar, NULL)
- `modelo` (varchar, NULL)

Cuidados:
- `codigo` é único e funciona como identificador principal do produto.

---

### 9) `locais` — Local (Fórum)
**Tabela:** `locais`

Campos:
- `id` (PK)
- `comarca` (NOT NULL)
- `especialidade` (NOT NULL)
- `numero_vara` (NOT NULL)
- `codigo` (int, NULL)
- `localizacao` (NOT NULL)
- `obs` (TEXT, NULL)

Uso:
- Referenciado por `Processo.local_id`
- Filtragem em relatórios

---

### 10) `hospital` (ou equivalente) — Hospital
**Tabela:** (não definida por @Table; provável `hospital` ou `hospital`/`hospitals` dependendo da naming strategy)

Campos:
- `id` (PK)
- `nome`
- `cnpj`
- `endereco`
- `telefone`

Uso:
- Referenciado por `Processo.hospital_id`
- Campo adicional `Processo.tipoHospital` (SUS/CONVENIO/PARTICULAR)

---

### 11) `doenca` e `grupo_doenca` (ou equivalente)
**Tabela `Doenca`:** (não definida por @Table; provável `doenca`/`doencas`)
- `id` (PK)
- `nome`
- `grupo_id` (FK -> GrupoDoenca)

`GrupoDoenca`:
- **Model não enviado** (precisamos do model para documentar nome real da tabela e colunas).

Uso:
- `Processo.doenca_id` é obrigatório.
- UI filtra doenças por grupo em `/doencas/cadastrar?grupoId=...`.

---

### 12) `usuarios` — Usuário do Sistema
**Tabela:** (não definida por @Table; provável `usuario`/`usuarios`)

Campos:
- `id` (PK)
- `email` (string)
- `senha` (string) — armazenada criptografada (PasswordEncoder)
- `nome` (string)
- `perfil` (enum string) — `ADMIN` ou `USUARIO` (default: USUARIO)

Cuidados:
- Perfis impactam visibilidade e autorização (Spring Security + Thymeleaf extras).

---

### 13) `maintenance_log` — Log de Manutenção
**Tabela:** `maintenance_log`

Campos:
- `id` (PK)
- `date` (date, NOT NULL)
- `durationMinutes` (int, NOT NULL)
- `type` (enum string `MaintenanceType`: CORRECTIVE, PREVENTIVE, UPDATE, OTHER)
- `description` (varchar(500), NULL)
- `performedBy` (varchar, NOT NULL) — ex.: "Samuel" ou "Marco"

Uso:
- Tela `/maintenance` calcula:
  - totalMinutes
  - monthlyHours = monthlyMin/60
  - monthlyPercent = monthlyHours/20h * 100 (cap 100)

---

## Enums (Valores Persistidos)

- `StatusProcesso`: ABERTO, EM_ANDAMENTO, CONCLUIDO, SUSPENSO
- `TipoHospital`: SUS, CONVENIO, PARTICULAR
- `Sexo`: MASCULINO, FEMININO, OUTRO
- `GrupoProduto`: MEDICAMENTOS, OUTROS
- `MaintenanceType`: CORRECTIVE, PREVENTIVE, UPDATE, OTHER
- `TipoDeferimento`: GRUPO_PROD, JUIZ
- `Usuario.PerfilUsuario`: ADMIN, USUARIO

---

## Regras de Integridade e Pontos Sensíveis

- `processos.numero_interno` é **UNIQUE** e obrigatório.
- `produtos.codigo` é **UNIQUE** e obrigatório.
- `pessoas` usa herança SINGLE_TABLE, então:
  - cuidado ao migrar/alterar discriminator `tipo_pessoa`
  - colunas específicas (crm/oab) convivem na mesma tabela
- `Advogado.oab` e `Medico.crm` são **UNIQUE** (quando preenchidos).
- Endereços e itens do processo usam `orphanRemoval=true`:
  - limpar listas remove registros do banco.
- Existe estratégia de auditoria:
  - mudanças em processo -> `processo_logs`
  - exclusão de processo -> `processos_excluidos` (snapshot)

---

## Pendências para completar este documento

Para fechar 100%:
1) Enviar o model de `GrupoDoenca` (para tabelas/colunas reais).
2) Confirmar a naming strategy do Hibernate (para saber o nome real das tabelas sem `@Table` como `Hospital`, `Usuario`, `Doenca`).