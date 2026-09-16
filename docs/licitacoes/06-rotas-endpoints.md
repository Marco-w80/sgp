# Rotas e endpoints

Todas exigem autenticação. Rotas mutáveis usam POST e CSRF.

| Método | Rota | Ação |
|---|---|---|
| GET | `/licitacoes` | Dashboard; `busca`, `arquivadas` |
| GET/POST | `/licitacoes/nova`, `/licitacoes` | Formulário/criação |
| POST | `/api/licitacoes/pncp/consultar` | Valida um link de edital do PNCP e retorna dados para pré-preenchimento |
| POST | `/licitacoes/{id}/etapa` | Altera a etapa; em Cadastro → Cotação recebe os `itemIds` selecionados |
| GET/POST | `/licitacoes/{id}/editar`, `/licitacoes/{id}` | Edição/detalhes |
| POST | `/licitacoes/{id}/etapa` | Transição adjacente (`destino`) |
| POST | `/licitacoes/{id}/etapa-kanban` | Transição adjacente em JSON para drag-and-drop |
| POST | `/licitacoes/{id}/arquivar` | Arquivar/restaurar |
| POST | `/licitacoes/{id}/excluir` | Exclusão permanente, somente ADMIN |
| POST | `/licitacoes/{id}/itens` | Criar/editar item |
| POST | `/licitacoes/{id}/itens/{itemId}/excluir` | Excluir item |
| POST | `/licitacoes/{id}/itens/importar` | Importação tabular em lote |
| POST | `/licitacoes/{id}/itens/{itemId}/decisao` | Estratégia e decisão |
| POST | `/licitacoes/{id}/itens/{itemId}/resultado` | Resultado da participação |
| POST | `/licitacoes/{id}/itens/{itemId}/cotacoes` | Criar/editar cotação |
| POST | `/licitacoes/{id}/itens/{itemId}/cotacoes/{cotacaoId}/excluir` | Excluir cotação |
| GET | `/licitacoes/{id}/cotacoes.csv` | Exportar CSV |
| POST/GET | `/licitacoes/{id}/anexos`, `/licitacoes/{id}/anexos/{anexoId}` | Upload/download |
| POST | `/licitacoes/{id}/anexos/{anexoId}/excluir` | Excluir anexo |
| GET | `/licitacoes/portfolio` | Estoque e fornecedores |
| POST | `/licitacoes/portfolio/estoque[...]` | CRUD de estoque |
| POST | `/licitacoes/portfolio/fornecedores[...]` | CRUD de fornecedores/itens/importação |

## Consulta do PNCP

Body:

```json
{"link":"https://pncp.gov.br/app/editais/46374500000194/2026/7477"}
```

A resposta contém `dados` (`orgao`, `numeroEdital`, `uasg`, `modalidade`, `objeto`, `dataDisputa`, `valorEstimadoTotal` e `linkEdital`), `pncp` (`cnpj`, `ano`, `sequencial` e `numeroControlePNCP`) e `itens` (`numeroItem`, `descricao`, `quantidade`, `unidade` e `valorReferencia`). Erros usam `{"error":"Mensagem"}` com status 400 para link inválido, 404 para contratação inexistente, 502 para JSON inesperado e 503 para timeout/indisponibilidade. O endpoint é autenticado e protegido por CSRF.

Os dados gerais são lidos da API de consulta do PNCP e os itens da rota oficial `/api/pncp/v1/orgaos/{cnpj}/compras/{ano}/{sequencial}/itens`. O navegador chama somente o backend do SGP; as URLs externas são montadas a partir da referência validada no link.

O servidor extrai exclusivamente CNPJ, ano e sequencial do link validado e constrói URLs fixas no domínio `pncp.gov.br`; não é feito `fetch` da URL recebida. Para os dados gerais, consulta primeiro o índice `/api/search/` usando o número de controle PNCP calculado e só aceita um resultado cujo CNPJ, ano, sequencial e `item_url` coincidam exatamente. Se o registro ainda não estiver indexado ou o índice falhar, usa `/api/consulta/v1/...` como contingência. Os itens continuam vindo da rota oficial `/api/pncp/v1/.../itens`.

O índice tem duas tentativas curtas de até 6 segundos. As APIs detalhada e de itens têm uma tentativa de até 40 segundos, evitando duas esperas longas consecutivas. A primeira chamada de itens usa a forma paginada do próprio portal (`pagina=1&tamanhoPagina=50`), que possui resposta mais estável. Se a primeira página vier cheia, o backend consulta a quantidade total e só aceita o resultado após garantir que todos os itens foram recebidos. Respostas completas ficam em cache de memória por 5 minutos. As chamadas usam HTTP/1.1 explícito, `User-Agent` da aplicação e redirecionamentos desabilitados.
