# Dados e Indicadores — BI do SGP (Intranet / Dashboard)

Este documento descreve as métricas, séries e regras de negócio exibidas no **BI interno do SGP**
na rota **/intranet** (Thymeleaf + Highcharts), com base no `DashboardService`.

Objetivo do BI:
- Mostrar **visão executiva** do status dos processos
- Evidenciar **gargalos operacionais** (pendências documentais e lead time)
- Permitir cortes por **período** (ano(s), trimestre ou mês)
- Trazer análises de **perfil de pacientes**, **produtividade jurídica** e **consumo de produtos/medicamentos**

---

## 1) Fonte de Dados (entidades principais)

- `Processo` (tabela `processos`)
  - status, datas, tipo de hospital, doença, paciente, itens enviados, flags de documentação etc.
- `Pessoa` (`pessoas`) com herança SINGLE_TABLE
  - Paciente/Advogado/Médico, sexo e nascimento (idade)
- `ProcessoProduto` (tabela `processo_produtos`)
  - produto, data_envio, quantidade
- `Produto` (tabela `produtos`)
  - nome_item, grupo (MEDICAMENTOS/OUTROS)
- Apoio:
  - `ProcessoLog` (tabela `processo_logs`) — auditoria de alterações
  - `ProcessoExcluido` (tabela `processos_excluidos`) — arquivo morto
  - `Local`, `Hospital`, `Doenca`

---

## 2) Filtros disponíveis no BI (/intranet)

Tela possui filtros GET (form method="get"):

### 2.1) Período
- `anos` (multiselect)
  - Pode selecionar **1+ anos**
  - **Se selecionar mais de 1 ano**, os filtros **trimestre** e **mês** ficam desativados
- `trimestre` (1 a 4) — **somente quando 1 ano selecionado**
- `mes` (1 a 12) — **somente quando 1 ano selecionado**
- Regra de conflito:
  - Selecionou trimestre -> zera mês
  - Selecionou mês -> zera trimestre

### 2.2) Pendências (idade mínima)
- `dias` (default 0)
  - Usado para listar processos com pendência documental há pelo menos **X dias**

---

## 3) Indicadores e gráficos exibidos

A intranet está organizada como “páginas/slides”:
- Visão Geral
- Análise de Processos
- Perfil dos Pacientes
- Produtos e Pendências

A seguir, cada métrica/serie e sua definição.

---

## 4) KPIs (Visão Geral)

### 4.1) Processos pendentes (card)
**Nome na UI:** Processos pendentes  
**Fonte:** `processoService.listarProcessosComDocumentacaoPendenteComMinDias(dias, de, ate)` (controller Home)

Regra:
- Contabiliza processos que têm **alguma documentação faltante**
- E cujo tempo de pendência é >= `dias`

> Observação: o cálculo exato de “dias aguardando” e como detecta pendência fica no `ProcessoService` / query do repositório.

---

### 4.2) Contagem por Status (cards)
**Nome na UI:** cards gerados por `statusCounts`  
**Fonte:** `DashboardService.contarPorStatusFormatado(de, ate)`

Regras:
- Quando **sem período**, usa `processoRepository.contarPorStatus()`
- Com período, usa `processoRepository.contarPorStatusPeriodo(de, ate)`
- Output é um mapa String -> Long, com:
  - "ABERTO"
  - "EM ANDAMENTO"
  - "CONCLUIDO"
  - "SUSPENSO"
- Caso algum registro venha com status nulo: entra como `"SEM STATUS"`

Normalização:
- `StatusProcesso.name().replace('_',' ')`
  - EM_ANDAMENTO vira "EM ANDAMENTO"

---

### 4.3) Lead Time (dias) (card)
**Nome na UI:** Lead Time (dias)  
**Fonte:** `DashboardService.leadTimeResumo(de, ate)`

Definição:
- Usa `processoRepository.leadTimePorProcesso()` (ou `...Periodo(de,ate)`)
- A projection retorna `leadTimeDias` por processo
- Métricas:
  - `mediaDias`: média dos lead times
  - `p50`: percentil 50 (mediana aproximada por índice)
  - `p90`: percentil 90

Como percentil é calculado:
- Ordena lista de lead times (inteiros)
- Índice: `ceil(p * n) - 1`, limitado ao intervalo [0..n-1]
- p50 usa `p=0.50`
- p90 usa `p=0.90`

Arredondamento:
- média com 2 casas: `round2(...)`

---

### 4.4) Lead Time P90 (dias) (card)
**Nome na UI:** Lead Time P90 (dias)  
**Fonte:** mesmo `leadTimeResumo(de, ate)`  
**Valor exibido:** `leadResumo.p90`

---

## 5) Séries e Gráficos (Highcharts)

### 5.1) Novos processos por mês (linha)
**Fonte:** `DashboardService.novosProcessosPorMes(de, ate)`

Retorno:
- `DTOs.SerieDTO("Novos processos", categorias, valores)`

Categorias:
- `anoMes` (string no formato "YYYY-MM") vindo da projection `SerieMensalProjection`

Valores:
- contagem de processos no mês

No front:
- Converte "YYYY-MM" em Date.UTC para plotar em eixo datetime

---

### 5.2) Documentação Completa (donut)
**Fonte:** `DashboardService.documentacaoResumo(de, ate)`

Cálculo:
- `total`:
  - sem período: `processoRepository.totalProcessos()`
  - com período: `processoRepository.totalProcessosPeriodo(de, ate)`
- `completos`:
  - sem período: `processoRepository.totalProcessosCompletos()`
  - com período: `processoRepository.totalProcessosCompletosPeriodo(de, ate)`
- `pct = 100 * completos / total` (se total=0 => 0)

No front:
- Donut com:
  - "Completos" = completos
  - "Pendentes" = total - completos

Definição de "completo":
- Depende do repositório (provável: todos os flags de documentação = true).

---

### 5.3) Produtividade por Advogado (barra)
**Fonte:** `DashboardService.produtividadePorAdvogado(de, ate)`

Output por advogado:
- `advogado`: nome
- `mediaDias`: média de lead time (ou métrica relacionada) por advogado
- `qtde`: quantidade de processos atribuídos

No front (atual):
- gráfico usa **qtde** como valor (barra por advogado)

> Observação: o nome "Produtividade" no gráfico está ligado à quantidade de processos, mas o service também entrega `mediaDias` caso seja usado futuramente.

---

### 5.4) Lead Time médio por Doença (coluna)
**Fonte:** `DashboardService.leadTimeMedioPorDoenca(de, ate)`

Output por doença:
- `doenca`: nome
- `mediaDias`: média (arredondada para 2 casas)
- `qtde`: quantidade de processos na amostra

No front:
- gráfico usa `mediaDias` (colunas)
- label: doença

---

### 5.5) Perfil do Paciente (Sexo) (pie/donut)
**Fonte:** `DashboardService.perfilGenero(de, ate)`

Normalização de chaves:
- null/blank => "DESCONHECIDO"
- "M" ou "MASCULINO" => "Masculino"
- "F" ou "FEMININO" => "Feminino"
- senão: mantém valor

No front:
- donut com total por categoria

---

### 5.6) Média de Idade por Sexo (coluna)
**Fonte:** `DashboardService.mediaIdadePorSexo(de, ate)`

Retorna:
- `categorias`: ["Masculino","Feminino","Não informado"] (ordem fixa)
- `valores`: média de idade por categoria (1 casa)
- `qtde`: quantidade de pacientes na amostra por categoria
- `label`: "Média de idade"

No front:
- tooltip mostra média e qtde.

---

### 5.7) Top Doenças (bar)
**Fonte:** `DashboardService.topDoencas(de, ate)`

Output:
- lista de mapas:
  - `doenca` (nome)
  - `total` (qtde de processos)
No front:
- gráfico de barras com `total`.

---

### 5.8) Distribuição por Tipo de Hospital (coluna)
**Fonte:** `DashboardService.distribuicaoTipoHospital(de, ate)`

Output:
- Map<String, Long> onde a chave vem da projection `getNome()`
- soma por chave (merge Long::sum)

No front:
- colunas por tipo

> Observação: existe `Processo.tipoHospital` (SUS/CONVENIO/PARTICULAR), mas também existe `Processo.hospital` (cadastro hospital). O gráfico é por **tipo**, não por hospital.

---

### 5.9) Consumo Mensal Total (linha)
**Fonte:** `DashboardService.consumoMensalTotal(de, ate)`

Query base:
- sem período: `processoProdutoRepository.consumoMensalPorProduto()`
- com período: `...Periodo(de, ate)`

O service faz um pivot:
- agrupa por `anoMes`
- soma quantidades de todos os produtos no mês

Output:
- `DTOs.SerieDTO("Consumo total (todos produtos)", meses, valores)`

No front:
- linha com eixo categórico (meses).

---

### 5.10) Top Produtos por Quantidade (bar)
**Fonte:** `DashboardService.consumoTotalPorProduto(de, ate)`

Output por produto:
- `produto` (string)
- `quantidade` (soma total)

No front:
- barra com quantidade por produto.

---

## 6) Utilidades e Tratamento de Dados

### 6.1) Conversão de booleanos vindo de query
`toBool(Object o)`:
- Boolean => valor
- Number => true se != 0
- Outros => false

Aplicado em:
- `listarPendencias(...)` ao converter array de resultado do repositório.

### 6.2) Arredondamento
`round2(double v)`:
- arredonda para 2 casas

Aplicado em:
- porcentagens (documentação)
- médias (lead time, lead time por doença, produtividade por advogado)

---

## 7) Contrato esperado dos Repositórios (importante p/ manutenção)

Para o BI funcionar, o DashboardService depende destas operações:

ProcessoRepository (exemplos):
- contarPorStatus()
- contarPorStatusPeriodo(de, ate)
- pendenciasDocumento(diasMinimos, de, ate)
- totalProcessos()
- totalProcessosPeriodo(de, ate)
- totalProcessosCompletos()
- totalProcessosCompletosPeriodo(de, ate)
- novosPorMes()
- novosPorMesPeriodo(de, ate)
- distribuicaoPorTipoHospital()
- distribuicaoPorTipoHospitalPeriodo(de, ate)
- topDoencas()
- topDoencasPeriodo(de, ate)
- leadTimeMedioPorDoenca()
- leadTimeMedioPorDoencaPeriodo(de, ate)
- produtividadePorAdvogado()
- produtividadePorAdvogadoPeriodo(de, ate)
- leadTimePorProcesso()
- leadTimePorProcessoPeriodo(de, ate)
- distribuicaoPorSexoPaciente()
- distribuicaoPorSexoPacientePeriodo(de, ate)
- mediaIdadePorSexo()
- mediaIdadePorSexoPeriodo(de, ate)
- anosComProcessos()

ProcessoProdutoRepository:
- consumoTotalPorProduto()
- consumoTotalPorProdutoPeriodo(de, ate)
- consumoMensalPorProduto()
- consumoMensalPorProdutoPeriodo(de, ate)

---

## 8) Observações para evolução futura (financeiro / logística)

O modelo atual já suporta:
- consumo/saída de produtos via `ProcessoProduto` (data_envio + quantidade)
Futuro módulo de logística/financeiro pode evoluir para:
- entrada de estoque (compras/notas/fornecedores)
- saldo/estoque
- custo unitário e custo total por envio
- relatórios financeiros por período/unidade/produto

---

## 9) Pendências para completar documentação

Para detalhar o BI com 100% de fidelidade:
1) Enviar as `@Query`/implementações do `ProcessoRepository` e `ProcessoProdutoRepository`
   (para documentar exatamente as regras: período, “completo”, lead time, agrupamentos).
2) Confirmar se existe campo de “data fim/conclusão” do processo (ou se lead time é calculado por status/log).