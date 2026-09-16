# Permissões e segurança

O módulo reutiliza `Usuario`, `UsuarioService`, formulário de login, BCrypt e roles `ADMIN`/`USUARIO`. Não existe usuário próprio do módulo.

Usuários autenticados acessam as telas e operações usuais. A exclusão permanente de licitação usa `@PreAuthorize("hasRole('ADMIN')")`; o botão só aparece a ADMIN. `@EnableMethodSecurity` foi habilitado em `SecurityConfig`.

Auditoria resolve o usuário autenticado por e-mail em `UsuarioAtualService` e preenche `criadoPor`, `registradoPor`, `enviadoPor` e usuário do histórico. Registros importados podem ficar com usuário nulo ou ser mapeados a um usuário atual no importador definitivo.

CSRF permanece ativo. Downloads validam que o anexo pertence à licitação da URL; caminhos são normalizados e precisam ficar sob a raiz configurada. Nomes enviados nunca são usados como nome físico.

A consulta `POST /api/licitacoes/pncp/consultar` segue a mesma permissão do cadastro: qualquer usuário autenticado pode usá-la, com CSRF obrigatório. Para evitar SSRF, o backend não acessa o link recebido. Ele valida esquema, domínio, porta, formato e identificadores e então monta somente endpoints fixos do domínio oficial `pncp.gov.br`. Redirecionamentos HTTP automáticos estão desabilitados.
