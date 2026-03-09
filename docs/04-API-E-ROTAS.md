# Rotas, Endpoints e Navegação — SGP (Sistema Grupo Prod)

Este documento lista as rotas do sistema, separando:
- Rotas de páginas (Thymeleaf / MVC)
- Rotas de API (JSON / REST)

Objetivo: servir como referência para mudanças, evitando quebrar URLs existentes e mantendo consistência de navegação.

---

## 🌐 Rotas de Autenticação

### GET `/login`
- Controller: `AuthController`
- View: `auth/login`
- Observação: rota inicial do sistema (o `/` redireciona para `/login`).

---

## 🏠 Rotas de Home / Intranet (Dashboard principal)

### GET `/`
- Controller: `HomeController`
- Ação: `redirect:/login`

### GET `/intranet`
- Controller: `HomeController`
- View: `intranet/dashboard`

#### Parâmetros (filtros)
- `dias` (Integer, default=0)
- `anos` (List<Integer>, opcional) — permite multi-seleção
- `trimestre` (Integer, opcional, 1..4) — desativado automaticamente quando multi-ano
- `mes` (Integer, opcional, 1..12) — desativado automaticamente quando multi-ano

#### Regras de período (importante)
- Se `anos` tiver mais de 1 item → período vira range de `minAno-01-01` até `maxAno-12-31`.
- Se 1 ano e `mes` informado → período = mês selecionado.
- Se 1 ano e `trimestre` informado → período = trimestre (3 meses).
- Se só 1 ano → período = ano inteiro.

#### Dados gerados no Model
A view recebe diversos objetos/estruturas para gráficos e indicadores, incluindo:
- Contagem por status (formatado)
- Resumo de documentação
- Lead time e produtividade
- Distribuições (tipo hospital, gênero, etc.)
- Séries de novos processos por mês
- Top doenças e lead time médio por doença
- Consumos (mensal e por produto)
- Lista de pendências de documentação (com filtro de dias mínimos)
- CSS por status:
  - ABERTO → `card-theme-secondary`
  - EM ANDAMENTO → `card-theme-primary`
  - CONCLUIDO → `card-theme-success`
  - SUSPENSO → `card-theme-danger`

> Observação técnica: existe sanitização recursiva de Maps/List/arrays via `deepSanitizeForJson()` para evitar chaves null em JSON/JS.

---

## 📊 Endpoints de API — Dashboard (JSON)

Controller: `DashboardController` (`@RestController`)

### GET `/api/dashboard/processos-por-status`
- Retorno: `List<StatusCountDto>`
- Fonte: `processoRepo.countByStatus()`

### GET `/api/dashboard/compliance-documentos`
- Retorno: `List<DocumentComplianceDto>`
- Cálculo: porcentagem de processos que possuem cada documento anexado:
  - CPF
  - Residência
  - Renda
  - Procuração
  - Declaração de insuficiência

### GET `/api/dashboard/processos-por-mes`
- Retorno: `List<MonthCountDto>`
- Fonte: `processoRepo.countByMonth()`

### GET `/api/dashboard/total-pacientes`
- Retorno: `long`
- Fonte: `pacienteRepo.count()`

---

## 🧾 Módulo de Cadastros

### 👤 Usuários
Controller: `UsuarioController`  
Base path: `/usuarios`

- GET `/usuarios` → View `usuarios/listar-usuarios`
- GET `/usuarios/cadastrar` → View `usuarios/cadastrar-usuario`
- POST `/usuarios/cadastrar` → cria usuário (senha é criptografada com `PasswordEncoder`)
- GET `/usuarios/editar/{id}` → View `usuarios/editar-usuario`
- POST `/usuarios/editar/{id}` → atualiza usuário
  - Se senha vier em branco → não altera senha

---

### 🧬 Doenças e Grupos
Controller: `DoencaController`  
Base path: `/doencas`

- GET `/doencas/cadastrar`
  - Query: `grupoId` (opcional)
  - View: `doencas/cadastrar`
  - Se `grupoId` informado:
    - carrega `grupoSelecionado`
    - carrega doenças filtradas por grupo

- POST `/doencas/cadastrar`
  - Cria doença dentro do grupo selecionado
  - Redirect:
    - sucesso: `/doencas/cadastrar?grupoId={grupoId}&sucesso`

- GET `/doencas/listar`
  - View: `doencas/listar`
  - Carrega todos os grupos

- POST `/doencas/editar`
  - Params: `id`, `nome`, `grupoId`
  - Redirect: `/doencas/cadastrar?grupoId={grupoId}&sucesso`

---

### 🏥 Hospitais
Controller: `HospitalController`  
Base path: `/hospitais`

- GET `/hospitais` → View `hospitais/listar`
- GET `/hospitais/cadastrar` → View `hospitais/cadastrar`
- POST `/hospitais/cadastrar` → salva e redirect `/hospitais`
- GET `/hospitais/editar/{id}` → View `hospitais/editar`
- POST `/hospitais/editar/{id}` → atualiza e redirect `/hospitais`
- GET `/hospitais/excluir/{id}` → exclui e redirect `/hospitais`

---

### 📍 Locais
Controller: `LocalController`  
Base path: `/locais`

- GET `/locais/cadastrar` → View `locais/cadastrar-local`
- POST `/locais/cadastrar` → cria e redirect `/locais/listar`
- GET `/locais/listar` → View `locais/listar-locais`
- GET `/locais/editar/{id}` → View `locais/editar-local`
- POST `/locais/editar/{id}` → atualiza e redirect `/locais/listar`

Campos atualizados no edit:
- comarca
- especialidade
- numeroVara
- codigo
- localizacao
- obs

---

### 👥 Pessoas (Paciente / Advogado / Médico)
Controller: `PessoaController`  
Base path: `/pessoas`

- GET `/pessoas/cadastrar`
  - View: `pessoas/cadastrar-pessoa`
  - Popula `PessoaForm` com pelo menos 1 endereço vazio
  - Tipos: `MEDICO`, `ADVOGADO`, `PACIENTE`

- POST `/pessoas/cadastrar`
  - Cria entidade concreta (Medico/Advogado/Paciente) dependendo do tipo
  - Associa lista de endereços (se vier)
  - Redirect: `/pessoas/listar`

- GET `/pessoas/listar`
  - View: `pessoas/listar-pessoas`

- GET `/pessoas/editar/{id}`
  - View: `pessoas/editar-pessoa`
  - Query opcional: `error` (ex.: CPF duplicado)
  - Carrega `PessoaForm` com dados existentes

- POST `/pessoas/editar/{id}`
  - Atualiza campos comuns e específicos (CRM/OAB)
  - Recria lista de endereços (limpa e adiciona)
  - Trata CPF duplicado: redirect com `?error=CPF já cadastrado`
  - Redirect: `/pessoas/listar`

---

## ⚖️ Módulo de Processos

Controller: `ProcessoController`  
Base path: `/processos`

### GET `/processos/cadastrar`
- View: `processos/cadastrar-processo`
- Popula listas:
  - pacientes (Pessoa instanceof Paciente)
  - advogados (Pessoa instanceof Advogado)
  - medicos (Pessoa instanceof Medico)
  - locais
  - produtos (DTO com id + nomeItem)
  - gruposDoenca
  - doencas
  - statusValues (`StatusProcesso.values()`)

### POST `/processos/cadastrar`
Cria processo com campos principais:

Obrigatórios:
- numeroInterno
- pacienteId
- dataInicio
- status (enum)
- doencaId
- grupoDoencaId (vem do form, mas o processo usa doencaId)

Opcionais:
- numeroProcesso (string) — só grava se não for blank
- advogadoId
- medicoId
- localId
- tipoHospital (enum)
- hospitalId
- obs

Produtos (itens do processo) — opcionais:
- produtoIds (List<Long>)
- produtoDatas (List<LocalDate>)
- produtoQuantidades (List<Integer>)
Regra: só processa se listas existirem e tiverem mesmo tamanho.

Flags de documentação:
- cpfAnexado
- compResidenciaAnexado
- compRendaAnexado
- procuracaoAnexado
- declaracaoInsuficienciaAnexado

Redirect: `/processos/listar`

### GET `/processos/listar`
- View: `processos/listar-processos`
- Lista todos processos

### GET `/processos/editar/{id}`
- View: `processos/editar-processo`
- Popula:
  - processo atual
  - pacientes/advogados/medicos
  - locais
  - produtos
  - statusValues
  - hospitais
  - gruposDoenca / doencas
  - tiposHospital (TipoHospital.values())

### POST `/processos/editar/{id}`
- Atualiza campos e itens do processo
- Também recebe os campos de óbito no formulário de edição:
  - `obito` (boolean, default false)
  - `observacaoObito` (string opcional)
- Logs de alteração (implementado exemplo para Advogado):
  - `processoLogService.logIfChanged(proc, "Advogado", antigo, novo)`
- Itens do processo:
  - `proc.clearItems()` e re-adiciona se listas vierem consistentes
- Redirect: `/processos/listar`

### DELETE `/processos/excluir/{id}`
- Retorna ResponseEntity com mensagem
- Usa `processoService.excluir(id)`
- Em erro: HTTP 500 com mensagem

---

## 📑 Módulo de Relatórios

### Relatório de Processos
Controller: `RelatorioProcessosController`  
Base path: `/relatorios/processos`

#### GET `/relatorios/processos`
- View: `relatorios/processo`
- Filtros:
  - `de` (LocalDate)
  - `ate` (LocalDate)
  - `status` (StatusProcesso)
  - `paciente` (String)
  - `localId` (Long)
  - `cpfAnexado` (Boolean)
  - `compResidenciaAnexado` (Boolean)
  - `compRendaAnexado` (Boolean)
  - `procuracaoAnexado` (Boolean)
  - `declaracaoInsuficienciaAnexado` (Boolean)

- Busca: `processoRepository.findByFiltros(...)`
- Também popula filtros:
  - lista de locais
  - enum de status
- A view recebe:
  - `processos`
  - `totalProcessos`
  - `param` (wrapper para manter filtros selecionados no form)

---

## 🛠️ Módulo de Manutenção

Controller: `MaintenanceLogController`  
Base path: `/maintenance`

### GET `/maintenance`
- View: `manutencao/maintenance`
- Model:
  - entries (lista)
  - totalMinutes
  - monthlyHours (monthlyMin / 60.0)
  - monthlyPercent (monthlyHours/20h * 100, limitado a 100)
  - form (MaintenanceLogForm)
  - types (MaintenanceType.values())
  - users: `["Samuel", "Marco"]`

### POST `/maintenance/save`
- Valida form com Bean Validation
- Em erro: redirect para `/maintenance` com flash attributes de erro
- Em sucesso: salva e redirect `/maintenance`

---

## Notas e Cuidados para Mudanças (Cline)

- **Não alterar URLs existentes** sem criar redirecionamento compatível.
- Mudanças em filtros devem:
  - manter parâmetros atuais funcionando
  - preservar valores selecionados no Thymeleaf
- Mudanças em enums (StatusProcesso/TipoHospital/MaintenanceType) impactam:
  - forms de cadastro/edição
  - dashboard e relatórios
  - contagens por status
- Endpoints `/api/dashboard/*` devem continuar retornando JSON compatível com os gráficos (Highcharts).