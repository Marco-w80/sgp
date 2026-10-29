# Padrão visual de Licitações

Este documento registra o layout e os comportamentos definidos para as telas de Licitações. O estado atual está preservado no commit `4c4ea9b` (`ajuste de dashboard`).

## Arquivos de referência

| Finalidade | Arquivo |
| --- | --- |
| Dashboard e estrutura do Kanban | `src/main/resources/templates/licitacoes/dashboard.html` |
| Cadastro e edição | `src/main/resources/templates/licitacoes/formulario.html` |
| Detalhes e etapas da licitação | `src/main/resources/templates/licitacoes/detalhes.html` |
| Estilos compartilhados do módulo | `src/main/resources/static/css/licitacoes.css` |
| Busca externa, navegação, barra fixa e arrastar | `src/main/resources/static/js/licitacoes.js` |
| Padrão geral das telas de cadastro e edição | `docs/PADRAO_VISUAL_CADASTRO_EDICAO.md` |

Esses arquivos são a fonte de verdade. Para criar uma tela nova, reutilizar as classes existentes antes de duplicar estilos no próprio template.

## Identidade visual

### Cores principais

```css
--lic-blue: #166298;
--lic-dark: #192f51;
--lic-border: #e3e6f0;
```

- Azul `#166298`: títulos, botões principais, ícones informativos, foco e elementos ativos.
- Azul escuro `#192f51`: texto de maior destaque, cabeçalhos do Kanban e hover de ações principais.
- Fundo claro `#f8f9fc`: cabeçalhos de cards, áreas secundárias e blocos de apoio.
- Borda `#e3e6f0`: separação suave entre blocos.
- Vermelho: atraso, urgência, exclusão e coluna “Em andamento”.
- Amarelo: atenção e prazos próximos.
- Verde: situação regular, amostra e conclusão.
- Ciano: disputas do dia e informações intermediárias.

### Forma e profundidade

- Cards principais: raio de `.75rem`, sem borda forte e sombra discreta.
- Cards internos: raio entre `.5rem` e `.65rem`, borda clara e sombra leve.
- Hover: pequena elevação, sombra mais visível e mudança sutil da borda.
- Ícones: Font Awesome, sempre relacionados ao conteúdo e marcados com `aria-hidden="true"` quando decorativos.

## Estrutura do dashboard

### Cabeçalho principal

- Título “Licitações” à esquerda, acompanhado por ícone.
- Ações à direita: “Importar legado” como secundária e “Nova licitação” como primária.
- Em telas estreitas, as ações podem quebrar linha e ocupar a largura disponível.
- Cabeçalho claro com borda inferior azul de `2px`.

### Indicadores resumidos

Os indicadores são compactos e apresentam somente rótulo, valor e ícone. O dashboard atual usa:

1. Ativas ou Arquivadas.
2. Em andamento.
3. Próximos 7 dias.
4. Disputas hoje.
5. Exigem amostra.
6. Sem itens.

Regras:

- Usar `.lic-dashboard-overview` como grade horizontal.
- Cada indicador usa `.lic-dashboard-stat`, uma classe de estado e `border-left-*`.
- Manter o valor com maior peso visual e o ícone dentro de um quadrado tonal claro.
- Permitir rolagem horizontal quando todos os indicadores não couberem.
- Evitar cards altos quando há apenas um número e um rótulo.

### Busca

- A busca pertence ao cabeçalho de “Acompanhamento por etapa”.
- Em telas largas, fica ao lado do título; abaixo de `1200px`, ocupa uma nova linha.
- Ordem dos controles: texto de busca, opção “Arquivadas” e botão “Buscar”.
- O campo pesquisa órgão, edital, objeto ou item.
- Em telas muito estreitas, o campo ocupa uma linha completa.

## Kanban de acompanhamento

### Colunas

- Largura padrão: `360px`.
- Largura em telas menores: `320px`.
- Espaço entre colunas: `1.25rem`.
- Fundo cinza claro e cabeçalho escuro.
- Cabeçalho em maiúsculas, com ícone da etapa e contador em badge.
- A coluna “Em andamento” usa vermelho escuro para diferenciá-la do fluxo editável.
- Cada coluna mantém altura mínima suficiente para receber um card mesmo quando vazia.

### Navegação horizontal

O quadro possui uma navegação própria acima das colunas:

- Barra de rolagem sempre acessível enquanto o usuário percorre verticalmente o Kanban.
- Setas nas duas extremidades.
- Cada clique avança ou retorna exatamente até a próxima posição de coluna.
- A barra superior e o `scrollLeft` do Kanban permanecem sincronizados.
- Os botões são desativados quando o quadro chega ao início ou ao fim.
- A barra é ocultada quando não existe conteúdo horizontal excedente.
- Quando fixa, mantém a largura e o alinhamento horizontal do quadro.
- Um espaço reservado impede salto no layout quando a barra passa para `position: fixed`.

### Referência fixa das etapas

Quando os cabeçalhos originais saem da área visível, uma faixa visual repete os nomes das etapas logo abaixo da barra fixa.

- Os rótulos são clones visuais dos cabeçalhos das colunas.
- A largura de cada rótulo acompanha a respectiva coluna.
- A faixa acompanha a rolagem horizontal do Kanban.
- `pointer-events: none` impede que a faixa intercepte o arrasto ou o clique nos cards.
- A faixa desaparece quando o Kanban deixa a área visível.

### Arrastar cards

- O card editável usa `draggable="true"`.
- A alça `.licitacao-drag-handle`, com `fa-grip-vertical`, comunica visualmente a ação.
- Durante o arrasto, o card recebe `.dragging` e reduz a opacidade.
- A coluna de destino recebe `.drag-over`.
- A movimentação é aceita somente para a etapa imediatamente anterior ou seguinte.
- A coluna “Em andamento” não recebe movimentação e seus cards usam `draggable="false"`.
- Mensagens de erro usam SweetAlert quando disponível.

Não colocar a faixa fixa ou a barra de navegação dentro das áreas que recebem `drop`. A lógica deve continuar ligada a `.kanban-column`, preservando `data-etapa` e `data-id`.

## Anatomia dos cards de licitação

O card foi dividido em blocos para reduzir a confusão visual:

1. **Topo:** alça de arraste, nome do órgão e status de urgência.
2. **Edital:** número e modalidade, acompanhados por ícones.
3. **Objeto:** rótulo discreto e texto limitado a duas linhas.
4. **Metadados:** itens selecionados sobre o total e data da disputa.
5. **Ações:** “Ver detalhes” como ação textual principal e exclusão como ícone discreto.

Regras de densidade:

- Nome do órgão limitado a três linhas.
- Objeto limitado a duas linhas, mantendo o texto completo em `title`.
- Informações agrupadas por separadores horizontais e fundos levemente alternados.
- Status em formato pill, com texto e ícone.
- Exclusão recebe destaque vermelho somente no hover ou foco.
- Card arrastável usa cursor `grab`; durante o arrasto, `grabbing` na alça.

### Estados de urgência

| Estado | Tratamento visual |
| --- | --- |
| Atrasado | Fundo vermelho claro e texto vermelho escuro |
| Urgente | Vermelho suave |
| Atenção | Amarelo suave |
| No prazo | Verde suave |
| Em andamento | Azul suave |

## Cadastro e edição de licitação

Cadastro e edição compartilham `formulario.html`; o texto e a ação mudam conforme a existência do ID.

### Estrutura

- Contêiner `.licitacao-form-page`, centralizado e com largura máxima de `1440px`.
- Link de retorno acima do título.
- Título e descrição curta da tarefa.
- Card opcional de consulta ao PNCP no cadastro.
- Seção “Dados gerais” com maior largura.
- Seção “Condições” como coluna complementar.
- Rodapé de ações com indicação de campos obrigatórios, Cancelar e ação principal.

### Seções de formulário

- Usar `.lic-form-card` para cada assunto.
- Cabeçalho com `.lic-form-section-header`.
- Ícone dentro de `.lic-form-section-icon`.
- Título e texto de apoio em `.lic-form-section-title`.
- Conteúdo em `.lic-form-section-body`.
- Labels permanecem acima dos campos.
- Asterisco obrigatório usa `.lic-form-required`.
- Campos relacionados usam `.form-row` e colunas responsivas.

### Consulta ao PNCP

- Entrada principal para o link do edital.
- Seleção opcional de itens antes de importar.
- Feedback em overlay com as etapas: validar link, buscar dados, buscar itens e preencher formulário.
- Estados visuais: pendente, ativo, concluído e erro.
- Se a consulta falhar, o preenchimento manual continua disponível.

## Responsividade

- Abaixo de `1200px`, a busca passa para uma linha própria.
- Abaixo de `768px`, colunas do Kanban usam `320px`; ações principais podem empilhar.
- Abaixo de `576px`, o campo de busca ocupa toda a linha.
- Os indicadores e o Kanban usam rolagem horizontal quando necessário.
- Textos longos devem quebrar ou ser limitados, sem aumentar descontroladamente os cards.

## Acessibilidade e feedback

- Associar títulos de seções com `aria-labelledby`.
- Usar `role="search"` no formulário de busca.
- Manter labels visuais ou `.sr-only` em todos os campos.
- Usar `aria-label` e `title` nos botões compostos apenas por ícones.
- Preservar foco visível em campos, barra horizontal, setas e botões.
- Usar mensagens claras para ações inválidas e falhas de comunicação.
- Elementos visuais duplicados para a faixa fixa devem usar `aria-hidden="true"`.

## Checklist para novas telas

- Reutilizar `head :: top`, `foot :: bot` e a folha `licitacoes.css`.
- Começar pela hierarquia: título, contexto, ação principal e conteúdo.
- Usar cards compactos e separar informações por blocos claros.
- Manter `#166298` como cor principal do módulo.
- Adicionar ícones somente quando ajudarem a identificar informação ou ação.
- Garantir foco, labels, mensagens de erro e estados vazios.
- Planejar comportamento abaixo de `1200px`, `768px` e `576px`.
- Se houver rolagem horizontal longa, manter controles acessíveis durante a rolagem vertical.
- Se houver arrastar, preservar áreas reais de `drop` e evitar camadas que capturem o ponteiro.
- Conferir o resultado no navegador com colunas cheias, vazias e textos longos.

