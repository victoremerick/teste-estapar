# Estapar - teste técnico

Projeto Spring Boot (Java 21) para receber configuração da garagem, processar eventos de estacionamento e consultar faturamento por setor/data.

## Stack
- Java 21
- Spring Boot (Web + Data JPA)
- MySQL
- Flyway
- Docker Compose
- JUnit + Mockito

## Estrutura de pacotes
- `com.estapar.config`: configuração de beans (`RestClient`).
- `com.estapar.controller`: endpoints HTTP (`/webhook` e `/revenue`).
- `com.estapar.dto`: contratos de entrada/saída da API e integração externa.
- `com.estapar.entity`: entidades JPA (`Sector`, `ParkingSpot`, `ParkingSession`).
- `com.estapar.enums`: enums de domínio (`EventType`, `SessionStatus`).
- `com.estapar.repository`: acesso a dados com Spring Data JPA.
- `com.estapar.service`: regras de negócio (bootstrap da garagem, processamento de eventos, faturamento).

## Fluxo de negócio
1. Na inicialização, `GarageBootstrapService` chama `GET http://localhost:3000/garage`.
2. Setores e vagas são persistidos no banco (tabelas Flyway).
3. `POST /webhook` recebe eventos:
   - `ENTRY`: abre sessão do veículo (ex.: `license_plate`, `entry_time`, `event_type`).
   - `PARKED`: pode receber coordenadas (`lat`, `lng`) e marca sessão como estacionada.
   - `EXIT`: libera vaga, fecha sessão e calcula receita (ex.: `license_plate`, `exit_time`, `event_type`).
4. `GET /revenue?date=YYYY-MM-DD&sector=A` soma a receita das saídas do setor na data.

## Banco de dados
Migração Flyway em `src/main/resources/db/migration/V1__create_parking_tables.sql` cria:
- `sectors`
- `parking_spots`
- `parking_sessions`

## Como executar
```bash
docker compose up -d
mvn spring-boot:run
```

## Testes
```bash
mvn test
```
