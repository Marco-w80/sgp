# Migração de dados do SQLite

O banco `server/data/sistema.db` da referência não estava disponível no workspace durante a implementação. Por isso não foi seguro inventar nomes/semântica de colunas nem executar importação.

Foram preparados `LegacySqliteDryRunService` e `LegacySqliteDryRunCli`. A CLI não inicializa Spring, JPA, Hibernate nem o datasource MySQL; abre somente o SQLite, lista tabelas, conta registros e alerta quando o esquema não corresponde aos nomes esperados. Execute em ambiente isolado:

```powershell
mvn -q "-Dexec.mainClass=com.sgp.service.licitacao.LegacySqliteDryRunCli" "-Dexec.args=C:\caminho\sistema.db" org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

Nenhum dado é gravado pelo dry-run e nenhuma conexão MySQL é aberta. Os campos `legado_id` únicos existem em todas as entidades importáveis para permitir `upsert`/idempotência no importador definitivo.

Procedimento obrigatório quando o SQLite for fornecido:

1. copiar banco e pasta de anexos para área de trabalho somente leitura;
2. executar dry-run e salvar contagens/esquema;
3. definir mapeamento de usuários antigos para IDs de `usuario` atuais;
4. detectar duplicidade por `legado_id`, edital/órgão e nomes de fornecedor;
5. fazer backup consistente do MySQL e do diretório de anexos;
6. implementar/validar adaptador do esquema real em homologação;
7. comparar contagens, amostras e totais monetários;
8. só então aplicar em produção dentro de transação por entidade.

Rollback: restaurar backup ou excluir apenas linhas com `legado_id` do lote, em ordem filhos → pais, além dos arquivos copiados. A etapa de aplicação permanece pendente deliberadamente até existir o banco real.
