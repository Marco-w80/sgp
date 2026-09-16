# Histórico de decisões

## 15/09/2026

- Mantida a stack Spring Boot 3.5, Java 17, JPA, MySQL, Thymeleaf, Bootstrap/SB Admin e Spring Security.
- Prefixo `lic_` escolhido para evitar colisões.
- Usuário atual reutilizado; nenhuma entidade de usuário paralela.
- “Em andamento” calculado em leitura e não persistido.
- Transições concentradas no service; drag-and-drop é apenas cliente dessa regra.
- Fornecedor da cotação guarda relação opcional e cópia do nome para estabilidade histórica.
- Snapshots de transição usam JSON compacto da licitação e decisões dos itens.
- Importações usam JDBC batch por causa da limitação de batching de entidades com IDENTITY.
- Arquivos usam UUID e raiz configurável persistente.
- Flyway adicionado, mas desabilitado por padrão para preservar o banco atual até execução controlada.
- SQLite de referência ausente: criado dry-run genérico, mas a escrita definitiva não foi inventada.
- Não foram adicionadas bibliotecas de drag-and-drop; HTML5 nativo atende o layout atual.

## 16/09/2026

- A importação PNCP foi implementada como pré-preenchimento opcional com prévia dos itens. A persistência só ocorre ao salvar e inclui cabeçalho e itens em uma única transação, sem alteração do modelo de dados.
- A prévia do PNCP mostra todos os itens sem seleção. Ao salvar, todos são persistidos; a seleção dos que seguirão para Cotação acontece na aba Cadastro da página seguinte. Os itens não escolhidos continuam registrados para consulta e possível revisão futura.
- O backend funciona como proxy controlado: valida o link público, extrai apenas CNPJ/ano/sequencial e monta internamente URLs fixas do domínio oficial, evitando SSRF.
- A consulta usa `java.net.http.HttpClient`, sem nova dependência, com HTTP/1.1, timeout de 12 segundos por tentativa, uma nova tentativa no endpoint atual e redirecionamentos desabilitados.
- `dataEncerramentoProposta` foi adotada provisoriamente como `dataDisputa`, preservando o horário local de Brasília e exigindo aviso de conferência na interface.
- Em 16/09/2026, o endpoint especificado `/api/pncp/v1/orgaos/{cnpj}/compras/{ano}/{sequencial}` passou a responder 301 sem `Location`, indicando `/api/consulta/v1/...`. A chamada depreciada foi removida e a integração passou a usar diretamente o prefixo oficial atual, evitando uma requisição e a reutilização da conexão com o gateway antigo.
- `numeroControlePNCP` permanece apenas no retorno da consulta; nenhuma migration ou coluna foi criada.
- A espera da consulta passou a exibir overlay com spinner para deixar explícito que a tela aguarda o PNCP, inclusive durante uma nova tentativa por instabilidade temporária.
- A API de consulta atual fornece o cabeçalho, enquanto a rota oficial legada `/api/pncp/v1/.../itens` fornece os itens; ambas são chamadas pelo backend a partir do mesmo link validado.
- O timeout por tentativa foi ampliado para 30 segundos porque a API de cabeçalho pode responder corretamente depois de mais de 20 segundos, mesmo quando a página pública do edital já abriu.
- A medição do edital `46374500000194/2026/7477` mostrou que a página HTML abria imediatamente, enquanto `/api/consulta/v1/...` podia ficar mais de 40 segundos sem resposta e os itens levavam de 9 a 16 segundos. O cabeçalho passou a usar primeiro o índice `/api/search/` do próprio portal, validando a identidade exata do registro, com a API detalhada como contingência. Os itens passaram a ter uma única espera de até 25 segundos e resultados completos são mantidos em cache por 5 minutos.
- Em nova medição, a rota de itens sem parâmetros respondeu em 26,05 segundos, logo depois do limite anterior, enquanto a variante paginada usada pelo portal respondeu em 0,13 segundo. A integração passou a iniciar com `pagina=1&tamanhoPagina=50`, conferir a quantidade total quando a página vier cheia e usar até 40 segundos para absorver oscilações sem aceitar lista parcial.
