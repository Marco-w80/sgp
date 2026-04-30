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
- **Sem Acompanhamento**
  - processos sem visita há X dias (parâmetro editável)
  - distribuição por status dos processos sem visita
  - tabela resumida com ação rápida para edição

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

Evolução de acompanhamento operacional:
- A listagem passou a exibir colunas de monitoramento:
  - último acesso (data/hora)
  - usuário do último acesso
  - dias sem acesso
- Filtros na própria tela:
  - `diasSemAcesso`
  - `diasSemEdicao`
- Critério aplicado:
  - ao informar X dias, entram processos com data de acesso/edição anterior ao limite e também processos sem registro (`null`).

---

### 5.3) Editar processo
- `GET /processos/editar/{id}` → form edição
- `POST /processos/editar/{id}` → salva edição

Regras e comportamento:
- **Acompanhamento automático** (novo):
  - ao abrir a tela de edição (`GET /processos/editar/{id}`), o sistema registra evento `ACESSO` no histórico;
  - ao salvar edição (`POST /processos/editar/{id}`), o sistema registra evento `EDICAO`.
- Campos auxiliares de acompanhamento no processo (performance para relatórios/BI):
  - `ultimoAcessoEm`, `ultimoAcessoPor`
  - `ultimaEdicaoEm`, `ultimaEdicaoPor`
- A tela de edição exibe no cabeçalho:
  - último acesso (data/hora + usuário)
  - última edição (data/hora + usuário)
- Itens são “resetados”:
  - `proc.clearItems()` + recriação da lista conforme arrays recebidos
- Campo de óbito disponível na edição:
  - `obito` (checkbox informativo)
  - `observacaoObito` (texto opcional)
  - A marcação de óbito **não interrompe** o andamento do processo e não altera automaticamente status.
- Auditoria:
  - existe `ProcessoLogService.logIfChanged(...)` para alterações de campos
  - existe registro de eventos de interação (`ACESSO` e `EDICAO`)
  - objetivo: suportar trilha de acompanhamento operacional em `processo_logs`

#### Acompanhamento operacional (base para indicadores)
- Endpoint de apoio para consultas futuras e BI:
  - `GET /api/processos/acompanhamento?diasSemAcesso=&diasSemEdicao=`
- Retorno: lista de `ProcessoAcompanhamentoDTO` com:
  - processoId
  - nomePessoa
  - ultimoAcesso / ultimoUsuarioAcesso
  - ultimaEdicao / ultimoUsuarioEdicao
  - diasSemAcesso / diasSemEdicao
- Permite identificar cenários como:
  - processos sem acesso há X dias
  - processos sem edição há X dias
  - processos sem interação recente

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
---

## Atualizacao 2026-04-28 - Exportacao Excel de Processos

### Modulo Processos
- A tela GET /processos/listar passou a ter acao de exportacao em Excel pela rota GET /processos/exportar-excel.
- A exportacao respeita os mesmos filtros da listagem (diasSemAcesso, diasSemEdicao, status).

### Modulo Relatorios
- A tela GET /relatorios/processos passou a ter acao de exportacao em Excel pela rota GET /relatorios/processos/exportar-excel.
- A exportacao respeita os mesmos filtros do relatorio (periodo, status, paciente, local e flags de anexos).

### Observacao de Arquitetura
- A geracao do arquivo foi centralizada no servico ProcessoExcelService, evitando duplicacao entre os dois fluxos.



## Atualizacao 2026-04-29 - Resumo Diario de Processos Pendentes por E-mail

### Modulo Processos / Acompanhamento
- Foi adicionada rotina automatica diaria para monitoramento de processos sem acesso recente.
- A rotina executa todos os dias as 09:00 no timezone `America/Sao_Paulo`.
- Escopo da selecao:
  - status `ABERTO` e `EM_ANDAMENTO`;
  - somente processos com 10 dias ou mais sem acesso.

### Regra de dias sem acesso
- Base principal: `processo.ultimoAcessoEm`.
- Quando `ultimoAcessoEm` estiver nulo, o calculo usa `processo.dataInicio` como referencia segura.

### Conteudo do e-mail diario
- Assunto: `SGP - Resumo diario de processos pendentes`.
- Corpo em HTML com data de geracao e tabela contendo:
  - ID, Interno, Processo, Paciente, Advogado, Medico, Inicio, Status,
  - Ultimo acesso, Dias sem acesso, Deferimentos, Pendencias documentais.
- Pendencias documentais verificadas:
  - CPF
  - Comprovante de Residencia
  - Comprovante de Renda
  - Procuracao
  - Declaracao de Insuficiencia
- Quando todos os documentos estiverem anexados: `Sem pendencias documentais`.
- Quando nao houver processos elegiveis: e-mail enviado com mensagem de ausencia de pendencias.

### Operacao e resiliencia
- SMTP reaproveitado da configuracao existente.
- Destinatario configuravel via `app.alertas.email.resumo.to`.
- Erros de envio sao registrados em log e nao interrompem o sistema principal.

## Atualizacao 2026-04-29 - Configuracao de Alertas via Tela Administrativa

### Modulo Alertas
- Novo menu lateral `Alertas` (perfil ADMIN) para acesso a configuracao.
- Nova pagina: `GET /intranet/alertas/config`.
- Recursos da tela:
  - configuracao de e-mails destinatarios
  - configuracao de dias minimos sem acesso
  - ativacao/desativacao da rotina diaria
  - opcao de enviar e-mail mesmo sem pendencias
  - botao de envio manual de teste

### Fluxo operacional
- O scheduler diario consulta a configuracao persistida antes de enviar.
- Se a rotina estiver desativada, o agendamento nao dispara envio.
- O envio manual usa as mesmas regras do envio diario.

### Atualizacao visual 2026-04-29 - E-mail de alertas
- O e-mail de alertas diarios recebeu template HTML customizado com identidade visual.
- A logo oficial da GrupoProd passou a ser exibida no cabecalho da mensagem.
- O relatorio de processos do dia passou a ser enviado como anexo em Excel no mesmo e-mail.

### Atualizacao 2026-04-29 - Horario configuravel de envio
- A configuracao de alertas passou a permitir ajuste de horario (campo `HH:mm`) direto na tela admin.
- A rotina automatica respeita o horario salvo e executa no maximo uma vez por dia.

## Atualizacao 2026-04-29 - Separacao de Pendencias no Dashboard

### Intranet / BI
- O card unico de "Processos pendentes" foi substituido por dois cards:
  - "Sem numero de processo"
  - "Pendencia documental"
- Cada card mostra contagem propria e direciona para listagem filtrada.

### Modulo Processos
- Nova rota de consulta operacional:
  - `GET /processos/pendencias?tipo=sem-numero-processo`
  - `GET /processos/pendencias?tipo=documentacao`
- A listagem exibe identificacao do processo e, no tipo documental, os documentos faltantes.

## Atualizacao 2026-04-29 - Status OBITO e deferimento livre

### Modulo Processos
- O fluxo de cadastro/edicao/listagem passou a trabalhar com `OBITO` como status ativo.
- `CONCLUIDO` foi removido das opcoes de formulario e filtros operacionais.
- Para compatibilidade, processos antigos com `CONCLUIDO` continuam legiveis e exibidos como `OBITO`.

### Modulo Deferimentos
- Na tela de edicao, o deferimento passou a aceitar entrada livre de mensagem.
- A interface nao exige mais escolha manual de tipo para registrar novo deferimento.
