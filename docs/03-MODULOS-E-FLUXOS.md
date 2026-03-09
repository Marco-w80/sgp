# Módulos e Fluxos — SGP (Sistema Grupo Prod)

Este documento descreve os módulos existentes no SGP e os fluxos principais do usuário (colaboradores, gestores e advogados em modo consulta).

O SGP é um sistema interno nacional, com dois pilares:
- **Operacional**: cadastro e acompanhamento de processos
- **BI**: painéis para monitorar volume, gargalos e indicadores (intranet)

---

## 1) Visão Geral dos Módulos

1. **Autenticação**
2. **Intranet / BI (Dashboard)**
3. **Cadastros**
   - Usuários
   - Pessoas (Paciente / Advogado / Médico)
   - Produtos
   - Locais (Fóruns)
   - Hospitais
   - Doenças e Grupos de Doenças
4. **Processos**
   - Criar / Editar / Listar / Excluir
   - Itens de produto por processo (envios/consumo)
   - Logs de alteração (auditoria)
   - Arquivo morto (processos excluídos)
5. **Relatórios**
   - **Relatório de Processos** (único por enquanto)
6. **Manutenção**
   - Registro interno de manutenção (tempo, tipo e responsável)

---

## 2) Autenticação (Login)

### Rotas
- `GET /login` → tela de login (`auth/login`)
- `GET /` → redireciona para `/login`

### Observações
- O sistema usa Spring Security (starter-security + thymeleaf-extras-springsecurity6).
- A entidade `Usuario` tem perfil `ADMIN` e `USUARIO`.

---

## 3) Intranet / BI (Dashboard)

### Rota
- `GET /intranet`

### Objetivo
Painel executivo para acompanhamento de processos, pendências documentais, lead time, perfil dos pacientes e consumo de produtos.

### Filtros do BI
- `anos` (multiselect, pode ser 1+ anos)
  - se selecionar mais de 1 ano, **mês e trimestre ficam desativados**
- `trimestre` (1–4, somente com 1 ano)
- `mes` (1–12, somente com 1 ano)
- `dias` (pendências com “dias+”, default 0)

### Páginas/Slides do BI
- **Visão Geral**
  - KPIs: pendências, contagem por status, lead time médio, lead time P90
  - Série: novos processos por mês
- **Análise de Processos**
  - Documentação completa
  - Produtividade por advogado
  - Lead time médio por doença
- **Perfil dos Pacientes**
  - Sexo
  - Média de idade por sexo
  - Top doenças
  - Distribuição por tipo de hospital
- **Produtos e Pendências**
  - Consumo mensal total (todos produtos)
  - Top produtos por quantidade
  - Tabela de pendências documentais com ações

### Modo Apresentação (TV Mode)
- Botão flutuante ativa modo TV:
  - oculta sidebar/topbar/footer
  - navega automaticamente pelas páginas (slides)
  - controles por teclado: ← → e ESC para sair

---

## 4) Cadastros

### 4.1) Usuários
- `GET /usuarios` → listar
- `GET /usuarios/cadastrar` → form cadastrar
- `POST /usuarios/cadastrar` → cria (senha criptografada)
- `GET /usuarios/editar/{id}` → form editar
- `POST /usuarios/editar/{id}` → atualiza (senha opcional)

Entidade:
- `Usuario` com `perfil` (ADMIN/USUARIO)

---

### 4.2) Pessoas (Paciente / Advogado / Médico)
- `GET /pessoas/cadastrar` → form
- `POST /pessoas/cadastrar` → cria (conforme tipo)
- `GET /pessoas/listar` → lista
- `GET /pessoas/editar/{id}` → form editar
- `POST /pessoas/editar/{id}` → salva edição

Regras:
- Cadastro é polimórfico (`Pessoa` single-table), com subclasses:
  - `Paciente`
  - `Advogado` (OAB única quando preenchida)
  - `Medico` (CRM único quando preenchido)
- Pessoa possui lista de `Endereco` (1:N, cascade + orphanRemoval)
- No editar, existe tratamento para CPF duplicado (retorna erro em querystring).

---

### 4.3) Hospitais
- `GET /hospitais` → listar
- `GET /hospitais/cadastrar` → form cadastrar
- `POST /hospitais/cadastrar` → salvar
- `GET /hospitais/editar/{id}` → form editar
- `POST /hospitais/editar/{id}` → atualizar
- `GET /hospitais/excluir/{id}` → excluir

Entidade:
- `Hospital` (nome, cnpj, endereco, telefone)

---

### 4.4) Locais (Fóruns)
- `GET /locais/cadastrar` → form
- `POST /locais/cadastrar` → salvar
- `GET /locais/listar` → listar
- `GET /locais/editar/{id}` → form editar
- `POST /locais/editar/{id}` → atualizar

Entidade:
- `Local` (comarca, especialidade, vara, codigo, localizacao, obs)

---

### 4.5) Doenças e Grupos
- `GET /doencas/cadastrar?grupoId=...` → cadastro com filtro por grupo
- `POST /doencas/cadastrar` → criar doença no grupo
- `POST /doencas/editar` → editar doença
- `GET /doencas/listar` → lista grupos

Entidades:
- `Doenca` (nome, grupo_id)
- `GrupoDoenca` (model não detalhado neste documento)

---

### 4.6) Produtos
- CRUD existe (referenciado no módulo processos e cadastro)
- Entidade:
  - `Produto` com `grupo` (MEDICAMENTOS/OUTROS)
  - `codigo` é único
  - suporte a especificações + fabricante/modelo

---

## 5) Processos (módulo principal)

### 5.1) Criar processo
- `GET /processos/cadastrar` → form
- `POST /processos/cadastrar` → cria

Campos importantes:
- Obrigatórios:
  - `numeroInterno` (único)
  - `pacienteId`
  - `dataInicio`
  - `status`
  - `doencaId` e `grupoDoencaId` (grupo usado na UI)
- Opcionais:
  - `numeroProcesso`
  - `advogadoId`, `medicoId`
  - `hospitalId`, `tipoHospital`
  - `localId`
  - `obs`
  - Itens (produtos): `produtoIds[]`, `produtoDatas[]`, `produtoQuantidades[]`
  - Flags de documentação: CPF, residência, renda, procuração, insuficiência

Persistência:
- `Processo` salva itens em `ProcessoProduto` (N itens por processo)

---

### 5.2) Listar processos
- `GET /processos/listar` → lista todos

---

### 5.3) Editar processo
- `GET /processos/editar/{id}` → form edição
- `POST /processos/editar/{id}` → salva edição

Regras e comportamento:
- Itens são “resetados”:
  - `proc.clearItems()` + recriação da lista conforme arrays recebidos
- Campo de óbito disponível na edição:
  - `obito` (checkbox informativo)
  - `observacaoObito` (texto opcional)
  - A marcação de óbito **não interrompe** o andamento do processo e não altera automaticamente status.
- Auditoria:
  - existe `ProcessoLogService.logIfChanged(...)` (exemplo aplicado ao campo Advogado)
  - objetivo: registrar mudanças relevantes em `processo_logs`

#### Histórico de Deferimentos (novo)
- Cada processo agora possui **histórico de deferimentos** (`1:N`).
- Fluxo na tela de edição:
  - Exibe lista dos deferimentos já registrados.
  - Botão **Adicionar Deferimento** abre formulário com:
    - `tipo`: Grupo Prod / Juiz
    - `mensagem`
  - O `numeroDeferimento` é gerado automaticamente no backend ao salvar.
- Regras:
  - Numeração sequencial por processo.
  - Não permite duplicidade de número no mesmo processo (restrição única no banco).
- Escopo atual:
  - Integrado à edição de processo e relatório simples de processo.
  - **Não integrado ao dashboard/BI** nesta etapa.

---

### 5.4) Excluir processo
- `DELETE /processos/excluir/{id}` → remove o processo

Estratégia de auditoria:
- antes da exclusão, sistema mantém um “arquivo morto”:
  - `ProcessoExcluido` (tabela `processos_excluidos`)
  - snapshot textual de paciente/advogado/médico/hospital/doença/local
  - data e usuário da exclusão (auditoria)

---

## 6) Relatórios (atual)

### 6.1) Relatório de Processos (único)
- `GET /relatorios/processos`

Filtros:
- período: `de`, `ate`
- `status`
- paciente (texto)
- `localId`
- flags de documentação:
  - cpfAnexado, compResidenciaAnexado, compRendaAnexado, procuracaoAnexado, declaracaoInsuficienciaAnexado

Retorno:
- lista filtrada + total
- repassa filtros via ParamWrapper para manter o form preenchido
- inclui lista simples de deferimentos por processo (sem filtros específicos de deferimento)

---

## 7) Manutenção

### Rotas
- `GET /maintenance` → página com logs, totais e formulário
- `POST /maintenance/save` → salva um log

Campos:
- date (data)
- durationMinutes
- type (CORRECTIVE, PREVENTIVE, UPDATE, OTHER)
- description
- performedBy (ex.: Samuel, Marco)

KPIs da manutenção (na tela):
- totalMinutes
- monthlyHours = monthlyMinutes / 60
- monthlyPercent = monthlyHours / 20h (cap 100%)

---

## 8) Notas para Cline (importante)

- Fluxos críticos do sistema:
  1) CRUD de Processos (inclui itens + documentos)
  2) BI (/intranet) com período + pendências
  3) Relatório de Processos com filtros
  4) Auditoria: logs + processos_excluidos

- Ao alterar:
  - enums (`StatusProcesso`, `TipoHospital`, etc.)
  - estrutura de `Processo` ou `ProcessoProduto`
  - regras de “documentação completa”
  - queries de repositório (período e agrupamentos)

Sempre atualizar também a documentação correspondente em:
- `/docs/05-BANCO-DE-DADOS.md`
- `/docs/06-DADOS-E-INDICADORES.md`
- `/CHANGELOG.md` (obrigatório)