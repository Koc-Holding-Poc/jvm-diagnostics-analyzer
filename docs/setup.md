# Setup Guide

See also: [Documentation Index](README.md), [Usage Guide](usage.md), [Troubleshooting Guide](troubleshooting.md)

## Prerequisites

- Java 25 for local development
- Maven for local build and test commands
- Docker (optional, recommended for easiest MAT setup)
- OpenRouter API key for AI features

Project versions are defined in [pom.xml](../pom.xml).

## Option 1: Run With Docker Compose

1. Create environment file.

```bash
cp .env.example .env
```

2. Edit .env and set OPENROUTER_API_KEY.

3. Build and run.

```bash
docker compose up --build
```

4. Open http://localhost:8080

Compose configuration is in [compose.yaml](../compose.yaml).

## Option 2: Run Locally With Maven

1. Ensure Java 25 is active.

```bash
java -version
```

2. Set required environment variables.

```bash
export OPENROUTER_API_KEY=sk-or-your-key-here
```

3. Optional local MAT path (for heap dump analysis outside Docker).

```bash
export APP_MAT_HOME=/path/to/mat
```

4. Start app.

```bash
mvn spring-boot:run
```

5. Open http://localhost:8080

## Common Build Commands

```bash
mvn -B -DskipTests dependency:go-offline
mvn -B clean compile
mvn -B test
mvn -B clean package
```

These align with [AGENTS.md](../AGENTS.md) and CI workflow in [.github/workflows/ci.yml](../.github/workflows/ci.yml).

## First Run Configuration

If AI is not configured, use setup UI at /setup or settings at /settings.

Settings are persisted by [src/main/java/com/heapanalyzer/service/ConfigService.java](../src/main/java/com/heapanalyzer/service/ConfigService.java) to user home path:

- ~/.jvm-diagnostics/config.properties

## Key Runtime Configuration

Defaults and env override mapping are defined in [src/main/resources/application.yml](../src/main/resources/application.yml).

Important variables:

- OPENROUTER_API_KEY
- OPENROUTER_BASE_URL
- AI_MODEL
- AI_TEMPERATURE
- APP_STORAGE_LOCATION
- APP_MAT_HOME
- APP_MAT_TIMEOUT_MINUTES
- MAT_HEAP_SIZE
- DYNAMIC_MAX_MAT_MEMORY
- MCP_ENABLED
- MCP_SESSION_TIMEOUT

## Related Docs

- [Architecture Overview](architecture.md)
- [API and Routes Overview](api-routes.md)
- [Docker Build and Publish](../DOCKER.md)
