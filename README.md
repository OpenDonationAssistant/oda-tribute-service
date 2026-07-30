# ODA Tribute Service
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/OpenDonationAssistant/oda-tribute-service)
![Sonar Tech Debt](https://img.shields.io/sonar/tech_debt/OpenDonationAssistant_oda-tribute-service?server=https%3A%2F%2Fsonarcloud.io)
![Sonar Violations](https://img.shields.io/sonar/violations/OpenDonationAssistant_oda-tribute-service?server=https%3A%2F%2Fsonarcloud.io)
![Sonar Tests](https://img.shields.io/sonar/tests/OpenDonationAssistant_oda-tribute-service?server=https%3A%2F%2Fsonarcloud.io)
![Sonar Coverage](https://img.shields.io/sonar/coverage/OpenDonationAssistant_oda-tribute-service?server=https%3A%2F%2Fsonarcloud.io)

## Running Locally with Docker

Pre-built Docker images are published to the GitHub Container Registry on every push to `main`.

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/)
- A running **PostgreSQL** instance
- A running **RabbitMQ** instance
- A **JWKS URI** from a Keycloak (or compatible OIDC) instance

### Quick Start

```bash
docker run --rm -p 8080:8080 \
  -e JWKS_URI="https://your-keycloak.example.com/realms/your-realm/protocol/openid-connect/certs" \
  -e JDBC_URL="jdbc:postgresql://host.docker.internal:5432/postgres" \
  -e JDBC_USER="postgres" \
  -e JDBC_PASSWORD="postgres" \
  -e RABBITMQ_HOST="host.docker.internal" \
  ghcr.io/opendonationassistant/oda-tribute-service:latest
```

The service will be available at `http://localhost:8080`.

### Available Image Tags

| Tag | Description |
|-----|-------------|
| `latest` | Most recent build from `main` branch |
| `<number>` | Specific build version (e.g., `42`) |

All images are available at `ghcr.io/opendonationassistant/oda-tribute-service`.

### Configuration

The service is configured via environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `JWKS_URI` | _(required)_ | JWKS endpoint URL for JWT signature verification |
| `JDBC_URL` | `jdbc:postgresql://localhost/postgres` | PostgreSQL JDBC connection URL |
| `JDBC_USER` | `postgres` | PostgreSQL username |
| `JDBC_PASSWORD` | `postgres` | PostgreSQL password |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ hostname |

### Docker Compose Example

For a full local environment with PostgreSQL and RabbitMQ:

```yaml
version: "3.8"
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: postgres
    ports:
      - "5432:5432"

  rabbitmq:
    image: rabbitmq:4-management
    ports:
      - "5672:5672"
      - "15672:15672"

  tribute-service:
    image: ghcr.io/opendonationassistant/oda-tribute-service:latest
    ports:
      - "8080:8080"
    environment:
      JWKS_URI: "https://your-keycloak.example.com/realms/your-realm/protocol/openid-connect/certs"
      JDBC_URL: "jdbc:postgresql://postgres:5432/postgres"
      JDBC_USER: "postgres"
      JDBC_PASSWORD: "postgres"
      RABBITMQ_HOST: "rabbitmq"
    depends_on:
      - postgres
      - rabbitmq
```

Save as `docker-compose.yml` and run:

```bash
docker compose up
```
