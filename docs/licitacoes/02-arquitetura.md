# Arquitetura

O módulo segue a arquitetura monolítica atual:

- controllers Spring MVC em `controller/licitacao`;
- regras transacionais em `service/licitacao`;
- entidades JPA em `model/licitacao`;
- repositories Spring Data em `repository/licitacao`;
- páginas server-side em `templates/licitacoes`;
- CSS e JavaScript próprios em `static/css/licitacoes.css` e `static/js/licitacoes.js`;
- MySQL como banco operacional;
- autenticação Spring Security e entidade `Usuario` já existentes.

`LicitacaoService` é a fronteira das regras de fluxo. `PortfolioService` cuida de estoque e fornecedores. `LicitacaoAnexoService` isola validação e armazenamento. `ImportacaoTabularService` normaliza texto e números brasileiros. As importações persistem por `JdbcTemplate.batchUpdate`, dentro de transação.

As tabelas novas têm prefixo `lic_` para não colidir com os módulos existentes. A migration Flyway está versionada, porém Flyway fica desabilitado por padrão para não alterar automaticamente o banco em uso; veja o procedimento de deploy.
