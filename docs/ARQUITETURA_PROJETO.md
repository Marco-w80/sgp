# Arquitetura do projeto SGP

Aplicação monolítica Spring Boot 3.5/Java 17, empacotada por Maven. O backend usa Spring MVC, Spring Data JPA/Hibernate, Spring Security e MySQL. O frontend é renderizado no servidor com Thymeleaf e usa SB Admin 2/Bootstrap 4, jQuery, DataTables e Highcharts disponíveis em `src/main/resources/static`.

O código fica sob `com.sgp`: `controller` entrega páginas, `api` contém endpoints JSON, `service` concentra parte das regras, `repository` usa `JpaRepository`, `model` contém entidades/enums e `dto` os modelos de transporte/formulário. Templates ficam em `resources/templates`; `fragments/head.html` e `foot.html` compõem sidebar, topbar, rodapé e scripts.

Autenticação: formulário próprio, `UsuarioService` como `UserDetailsService`, BCrypt e perfis `ADMIN`/`USUARIO`. A maior parte das rotas exige login; restrições específicas aparecem em `SecurityConfig` ou `@PreAuthorize`.

Persistência: datasource MySQL e `hibernate.ddl-auto=update` no legado. O módulo de licitações introduziu Flyway opt-in (`FLYWAY_ENABLED`) e migration versionada, sem remover o comportamento atual por padrão. Produção é configurada em `application.properties`; credenciais hoje estão diretas no arquivo e devem ser externalizadas em mudança separada.

Uploads não tinham infraestrutura compartilhada identificável. Licitações usa diretório configurável e persistente. Deploy é um único JAR Spring Boot; não há frontend ou backend separado.
