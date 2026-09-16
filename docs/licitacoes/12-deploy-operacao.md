# Deploy e operação

## Banco

Faça backup do MySQL. Em homologação, aplique a migration com `FLYWAY_ENABLED=true` e `HIBERNATE_DDL_AUTO=validate`. Em banco existente não gerenciado pelo Flyway, `baseline-on-migrate` cria baseline `2026091400` e aplica `V2026091501`. Confirme antes que a tabela atual de usuários se chama `usuario` e possui `id BIGINT`.

O padrão continua seguro para a instalação existente: `FLYWAY_ENABLED=false` e `HIBERNATE_DDL_AUTO=update`. Para produção controlada, prefira Flyway + `validate` depois de validar a migration em cópia do banco.

## Arquivos

Defina `LICITACOES_UPLOAD_DIR` para volume persistente e gravável. Inclua esse diretório no backup e monitore espaço/permissões. Limites HTTP estão em 20 MB por arquivo e 21 MB por request.

## Build

```powershell
mvn clean test
mvn -DskipTests package
```

Não é necessário Node, React ou processo adicional. Após deploy, valide login, menu, migration, criação de licitação, transições, CSV e upload/download. O datasource e SMTP atuais contêm configuração direta no arquivo existente; recomenda-se externalizá-los em trabalho separado, sem misturar essa mudança com o módulo.
