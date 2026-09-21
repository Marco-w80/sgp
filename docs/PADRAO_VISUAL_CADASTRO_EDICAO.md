# Padrão visual das telas de cadastro e edição

Levantamento do código das telas acessadas pelos menus **Cadastrar** e **Editar** em `fragments/head.html`. O menu Editar leva às listagens, e cada listagem abre o respectivo formulário de edição. Este documento descreve o padrão existente e orienta a criação de novas telas; ele não substitui uma conferência visual no navegador.

## Base compartilhada

- Interface: Thymeleaf, SB Admin 2, Bootstrap 4, Nunito e Font Awesome. O fragmento `fragments/head.html` fornece menu lateral, barra superior e abertura de `.container-fluid`; `fragments/foot.html` fecha a estrutura e carrega os scripts base.
- Menu lateral: gradiente de `#166298` para `#192f51`. Área de conteúdo com barra superior branca, cards claros e sombra.
- Paleta usada nos formulários: azul de cadastro `#166298`, azul escuro de hover `#192f51`, roxo azulado de edição `#4e5aa6`, hover de edição `#3c447f`, fundo de cabeçalho `#f8f9fc`, borda suave `#e3e6f0`, cinza de botão secundário `#858796`.
- A tipografia e os componentes básicos vêm do SB Admin 2. Os ajustes específicos estão hoje em blocos `<style>` dentro de cada template, não em uma folha compartilhada de formulários.

## Referências por tipo de tela

| Tipo | Referências no projeto | Estrutura a reproduzir |
| --- | --- | --- |
| Cadastro simples | `hospitais/cadastrar.html`, `locais/cadastrar-local.html` | Aviso de obrigatoriedade acima do card; `.card.shadow.mb-4.rounded`; `.card-header.py-3.form-header` com ícone e título; `.card-body` com formulário, campos e ações. |
| Cadastro com várias seções | `produtos/cadastrar-produto.html` | Mesma estrutura, com `fieldset`, `legend`, `.form-row` e colunas responsivas para agrupar os campos. |
| Formulário estreito | `usuarios/cadastrar-usuario.html` | Card centralizado em `.row.justify-content-center` e `.col-lg-6.col-md-8`. |
| Formulário com campos condicionais | `pessoas/cadastrar-pessoa.html` | Card largo, grade de campos e subtítulo de seção (`form h6`) para Endereço. |
| Edição | `produtos/editar-produto.html`, `hospitais/editar.html` | Estrutura do cadastro com contêiner `.pagina-edicao`, que muda borda/título/foco/botão principal para roxo azulado. Dados existentes aparecem nos campos. |
| Listagem que antecede edição | `produtos/listar-produtos.html`, `hospitais/listar.html` | Card com título e botão de novo cadastro no cabeçalho; tabela com busca e paginação DataTables; ação Editar por linha. |
| Exceção de fluxo | `doencas/cadastrar.html`, `doencas/listar.html` | Doenças usa uma tela de gerenciamento por localização; cadastro e edição acontecem em modais. A listagem apresenta grupos, não uma tabela DataTables. |

## Regras práticas para novas telas

1. Reutilizar os fragmentos `head :: top` e `foot :: bot`, sem criar outro menu ou outra barra superior.
2. Usar um card principal com cabeçalho claro, borda inferior de 2 px na cor da operação, título em negrito de aproximadamente `1.1rem` e ícone Font Awesome antes do texto.
3. Colocar a indicação de campos obrigatórios acima do card. Usar `label` associado ao campo, asterisco vermelho, `.form-control`, `.form-group` e `.invalid-feedback` quando houver validação.
4. Organizar formulários longos em `fieldset`/`legend` ou subtítulos. Usar `.form-row` e `col-md-*` para distribuir campos; escolher largura centralizada para formulários curtos e largura maior para formulários extensos.
5. No cadastro, aplicar azul `#166298` ao cabeçalho, foco e ação principal. Na edição, envolver o conteúdo em `.pagina-edicao` e aplicar roxo azulado `#4e5aa6` aos mesmos elementos. Usar **Salvar** para cadastro e **Atualizar** para edição. Manter **Cancelar** em cinza quando houver retorno à listagem.
6. Nas listagens comuns, manter o cabeçalho com título à esquerda e ação de cadastro à direita, tabela responsiva, busca/paginação em português e botão de edição identificável em cada linha. Para dados agrupados como Doenças, usar a estrutura de grupos e filtro.
7. Definir estados de foco e validação visíveis, alinhar ações de modo consistente e conferir o resultado em tela estreita antes de considerar a nova página pronta.

## Divergências encontradas; evitar ao copiar um modelo

- **Larguras e espaçamentos variam**: Usuário usa coluna central de 6/8, Hospital de 8/10, enquanto Produto, Local e Pessoa ocupam o contêiner disponível. Escolher a largura conforme a quantidade de campos; não copiar uma largura por acaso.
- **Ações variam**: Pessoa tem apenas botão central de salvar/atualizar; as outras telas geralmente oferecem Cancelar. Local e Hospital em edição misturam `.btn-success` com o tema roxo de `.btn-principal-custom`. Para novas telas, usar uma só classe visual para a ação principal.
- **Variável CSS ausente nas edições**: os estilos de Pessoa e de `legend` em Produto/Local/Hospital usam `var(--cor-secundaria)`, mas essa variável não é declarada nesses templates. Definir a variável ou usar uma variável existente ao extrair o padrão.
- **Rota de Usuário**: os botões Cancelar de cadastro e edição apontam para `/usuarios/listar`; `UsuarioController` expõe a listagem em `GET /usuarios`. Corrigir ao reutilizar esse fluxo.
- **Estrutura HTML dos fragmentos**: `head :: top` já contém `<head>`, `<body>` e a abertura da área principal. Vários templates o inserem dentro de outro `<head>` e ainda abrem outro `<body>` ou `.container-fluid`. Isso produz marcação potencialmente inválida e dificulta padronização. Antes de criar um template base definitivo, organizar uma única estrutura de documento e garantir que o rodapé feche a mesma estrutura.
- **Dependências duplicadas**: `foot :: bot` já carrega jQuery e os arquivos do DataTables, enquanto algumas páginas os carregam novamente. Evitar novos carregamentos repetidos.

O padrão acima foi verificado nos templates e no controlador citado. A aparência renderizada e o comportamento responsivo ainda precisam de conferência no navegador quando novas telas forem implementadas.
