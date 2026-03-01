# Arquitetura e Stack — SGP (Sistema Grupo Prod)

## Visão Geral da Arquitetura

O SGP é um sistema web interno construído em Java com Spring Boot, utilizando Thymeleaf para renderização server-side das páginas e MySQL (AWS RDS) como banco de dados.

A aplicação segue um modelo típico de arquitetura em camadas:
- Controllers (camada web)
- Services (regras de negócio e orquestração)
- Repositories (acesso a dados via Spring Data JPA)
- Templates (Thymeleaf)
- Static assets (JS/CSS, incluindo Highcharts)

> Modelo atual: **Monolito (single application)** — um único projeto/aplicação responsável por backend e páginas.
> (Se futuramente houver serviços separados, isso será documentado aqui.)

---

## Stack Técnica

### Backend
- Linguagem: **Java 17**
- Framework: **Spring Boot 3.5.0**
- Módulos principais:
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-security
  - spring-boot-starter-validation
  - spring-boot-starter-thymeleaf
  - thymeleaf-extras-springsecurity6
  - spring-security-crypto

### Frontend
- Renderização: **Thymeleaf (server-side)**
- Gráficos: **Highcharts**
- Estáticos: `/static` (CSS/JS/imagens, conforme estrutura padrão Spring)

### Banco de Dados
- SGBD: **MySQL**
- Hospedagem: **AWS RDS (MySQL)**

### Build / Dependências
- Gerenciador: **Maven**
- Plugin: **spring-boot-maven-plugin**
- Driver MySQL: `mysql-connector-j`

---

## Estrutura de Pastas (Padrão Spring Boot)

Estrutura típica esperada:

- `src/main/java/com/sgp/...`
  - `controller/` (rotas, endpoints, navegação de telas)
  - `service/` (regras de negócio, cálculos, orquestração)
  - `repository/` (Spring Data JPA, queries)
  - `model/` ou `entity/` (entidades JPA)
  - `dto/` (se existir)
  - `config/` (Spring Security, configs gerais)

- `src/main/resources/`
  - `templates/` (páginas Thymeleaf)
  - `static/` (JS/CSS/libs como Highcharts)
  - `application.properties` ou `application.yml`

---

## Padrões e Convenções

### Acesso a Dados
- ORM: Spring Data JPA
- Entidades representam tabelas do MySQL (RDS)
- Repositories encapsulam consultas

### Segurança (Spring Security)
- Autenticação/autorização via Spring Security
- Integração com Thymeleaf Security extras (controle de UI por roles)

### Validação
- Bean Validation (starter-validation) para validar DTOs/requests

### Observações para tarefas no Cline
- Evitar mudanças invasivas em camadas fora do escopo da tarefa
- Preferir alterações incrementais
- Manter compatibilidade de endpoints e telas existentes