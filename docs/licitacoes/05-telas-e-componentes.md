# Telas e componentes

## Dashboard

`templates/licitacoes/dashboard.html`: indicadores, busca única, filtro de arquivadas, seis colunas, cards com urgência e drag-and-drop adjacente.

## Cadastro

`formulario.html`: órgão, edital, UASG, modalidade, objeto, disputa, prazo, valor, amostra, link e observações. Após criar, redireciona aos detalhes; quando houve consulta ao PNCP, os itens exibidos na prévia são cadastrados junto com a licitação.

No início do formulário, a área **Importar dados do PNCP** reutiliza o campo `linkEdital` e permite consultar o edital sem cadastrar a licitação. Durante a consulta o botão fica desabilitado com o texto “Buscando...” e um overlay bloqueia a tela com spinner e mensagem de espera. Em sucesso, os valores disponíveis são preenchidos e todos os itens aparecem em uma tabela de prévia com número, descrição, quantidade, unidade e valor unitário estimado. A escolha dos itens que seguirão no fluxo é feita somente na página seguinte, depois de salvar. Prazo de entrega, necessidade de amostra, observações e qualquer campo ausente na resposta permanecem intactos. Todos os campos continuam editáveis e o cadastro manual segue disponível. A interface avisa que `dataDisputa` veio do encerramento das propostas e deve ser conferida. Se o link for alterado depois da busca, os itens em espera são descartados para impedir associação ao edital errado.

O overlay atualiza cada etapa assim que ela termina. Como os itens podem levar mais tempo no PNCP, a tela pode informar “Dados gerais recebidos. Aguardando os itens...” sem aplicar parcialmente o resultado; o formulário só é preenchido quando cabeçalho e itens terminam com sucesso.

## Detalhes

`detalhes.html`: cabeçalho, link do edital, stepper e abas Cadastro, Cotação, Aprovação, Definição, Participação, Histórico e Anexos. Inclui tabelas responsivas, edição inline, confirmações, exportação CSV e modal de importação com prévia/mapeamento. Na aba Cadastro, o usuário escolhe os itens que avançarão para Cotação, com seleção individual, “Selecionar todos” e contador. Itens não escolhidos continuam armazenados no cadastro, mas não aparecem nas etapas seguintes. O formulário de edição é renderizado imediatamente após a linha do próprio item; ao abrir, recebe foco e permanece visível com rolagem suave.

## Portfólio

`portfolio.html`: abas Estoque e Diversos/Fornecedores, pesquisa, CRUD inline, expansão de fornecedor e importação em massa de itens.

`licitacoes.css` define Kanban, badges, stepper, cards de decisão e responsividade. `licitacoes.js` implementa drag-and-drop, prévia tabular e consulta assíncrona do PNCP com CSRF e mensagens inline (sem `alert()`).
