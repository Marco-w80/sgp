# Testes

Testes automatizados:

- `ImportacaoTabularServiceTest`: números brasileiros, cabeçalho, sequência e descrição obrigatória;
- `LicitacaoServiceTest`: criar, editar, consultar, arquivar, excluir, impedir salto, avançar/retornar, bloquear pendência, resultado, histórico, dashboard, busca relacionada e Em andamento;
- `LicitacaoControllerTest`: renderização do Dashboard/formulário/detalhes e exclusão ADMIN versus USUARIO;
- `LicitacaoAnexoServiceTest`: upload, download, exclusão, formato inválido e limite de 20 MB;
- `PortfolioServiceTest`: CRUD, pesquisa e importação em lote;
- `LegacySqliteDryRunServiceTest`: contagem read-only de um SQLite real temporário;
- `PncpServiceTest`: mapeamento do exemplo oficial, link com barra/query/hash, bloqueio de formato inválido e site externo sem acesso à rede, 404, timeout, JSON inesperado, validação exata do resultado do índice do portal, contingência pela API detalhada, nova tentativa curta do índice, paginação com conferência da quantidade total e cache das respostas completas;
- `PncpControllerTest`: autenticação, CSRF, contrato JSON de sucesso e erro amigável sem stack trace;
- testes existentes do SGP continuam na mesma suíte.

Os testes usam H2 em memória, Flyway desabilitado, diretório temporário e SMTP local fictício por `src/test/resources/application.properties`. Nunca conectam ao MySQL de produção.

Checklist manual antes de produção: criar edital completo; importar planilha grande; editar/excluir item e cotação; conferir CSV no Excel; decidir todos os itens; testar bloqueio com pendência; avançar e retornar; registrar três resultados; arquivar/restaurar; carregar cada formato de anexo; testar arquivo >20 MB e extensão inválida; CRUD/pesquisa/importação de fornecedor; conferir layout em desktop, notebook e tablet.

Checklist PNCP: consultar `https://pncp.gov.br/app/editais/46374500000194/2026/7477`; conferir a prévia e a quantidade; salvar e confirmar que todos os itens foram cadastrados; na página seguinte, desmarcar itens individuais, testar “Selecionar todos” e avançar para Cotação; confirmar que apenas os escolhidos aparecem nas etapas seguintes e que os demais permanecem na aba Cadastro; rejeitar avanço sem seleção e avanço Cadastro → Cotação pelo Kanban; rejeitar `/app/editais/teste`; rejeitar `https://google.com/teste` sem chamada externa; consultar uma referência estruturalmente válida e inexistente; simular timeout/indisponibilidade; confirmar que campos já preenchidos e não retornados não são apagados; alterar o link após a consulta e confirmar que a prévia é descartada; alterar manualmente um campo importado e salvar; abrir “Abrir edital” e confirmar o link salvo em nova aba com `noopener noreferrer`.
