# AGENTS.md

## Project Overview
JVM Diagnostics Analyzer is a Spring Boot web app that analyzes heap dumps, thread dumps, and GC logs, then augments findings with Spring AI responses. It also exposes MCP tools for agent-driven heap analysis.

Primary runtime and dependency versions are defined in [pom.xml](pom.xml) and runtime settings are in [src/main/resources/application.yml](src/main/resources/application.yml).

## Repository Structure
- [src/main/java/com/heapanalyzer](src/main/java/com/heapanalyzer): Application code.
- [src/main/resources/templates](src/main/resources/templates): Thymeleaf pages for upload, results, setup, and MCP chat.
- [src/main/resources/static/css](src/main/resources/static/css): Frontend styling.
- [src/test/java/com/heapanalyzer](src/test/java/com/heapanalyzer): Unit and controller tests.
- [.github/workflows](.github/workflows): GitHub Actions workflows.
- [README.md](README.md), [CONTRIBUTING.md](CONTRIBUTING.md), [CHANGELOG.md](CHANGELOG.md): Contributor and release docs.

## Tech Stack
- Java + Maven
- Spring Boot (Web, Thymeleaf)
- Spring AI (OpenAI-compatible provider)
- Eclipse MAT integration for heap analysis
- SSE for streaming AI chat and MCP logs

## Build And Run
- Install dependencies and compile:
  - `mvn -B -DskipTests dependency:go-offline`
  - `mvn -B clean compile`
- Run tests:
  - `mvn -B test`
- Run application locally:
  - `mvn spring-boot:run`
- Build package:
  - `mvn -B clean package`

## Testing
- Test root: [src/test/java/com/heapanalyzer](src/test/java/com/heapanalyzer)
- Default command: `mvn -B test`
- Keep service changes paired with service tests and controller endpoint changes paired with [src/test/java/com/heapanalyzer/controller/AnalysisControllerTest.java](src/test/java/com/heapanalyzer/controller/AnalysisControllerTest.java).

## Key Patterns And Conventions
- Controller routes and API contracts are centralized in [src/main/java/com/heapanalyzer/controller/AnalysisController.java](src/main/java/com/heapanalyzer/controller/AnalysisController.java).
- Analysis orchestration is in [src/main/java/com/heapanalyzer/service/AnalysisService.java](src/main/java/com/heapanalyzer/service/AnalysisService.java), delegating by analysis type.
- MCP tools are defined in [src/main/java/com/heapanalyzer/mcp/HeapDumpMcpTools.java](src/main/java/com/heapanalyzer/mcp/HeapDumpMcpTools.java) and registered in [src/main/java/com/heapanalyzer/config/McpToolConfig.java](src/main/java/com/heapanalyzer/config/McpToolConfig.java).
- Runtime configuration values are loaded from [src/main/resources/application.yml](src/main/resources/application.yml) and setup logic in [src/main/java/com/heapanalyzer/service/ConfigService.java](src/main/java/com/heapanalyzer/service/ConfigService.java).

## CI/CD
- Existing workflows:
  - [GitHub workflow for Copilot setup](.github/workflows/copilot-setup-steps.yml)
  - [GitHub release workflow](.github/workflows/release.yml)
- PR validation workflow:
  - [GitHub CI workflow](.github/workflows/ci.yml)

## Adding A New Analysis Type
When adding a new analysis type (for example, JFR analysis), update all of the following:
1. Add enum entry in [src/main/java/com/heapanalyzer/model/AnalysisType.java](src/main/java/com/heapanalyzer/model/AnalysisType.java).
2. Add pipeline handling in [src/main/java/com/heapanalyzer/service/AnalysisService.java](src/main/java/com/heapanalyzer/service/AnalysisService.java).
3. Add upload/status endpoints and page route in [src/main/java/com/heapanalyzer/controller/AnalysisController.java](src/main/java/com/heapanalyzer/controller/AnalysisController.java).
4. Add parser/service class in [src/main/java/com/heapanalyzer/service](src/main/java/com/heapanalyzer/service).
5. Add/update UI template in [src/main/resources/templates](src/main/resources/templates).
6. Add tests in [src/test/java/com/heapanalyzer](src/test/java/com/heapanalyzer).
7. Update docs in [README.md](README.md) and [CHANGELOG.md](CHANGELOG.md).

## Documentation Status
- Primary docs exist in [README.md](README.md), [CONTRIBUTING.md](CONTRIBUTING.md), [DOCKER.md](DOCKER.md), and [HEAP_ANALYSIS.md](HEAP_ANALYSIS.md).
- Changelog is tracked in [CHANGELOG.md](CHANGELOG.md).

## Common Pitfalls
- Do not assume MAT is available locally; respect MAT setup endpoints and checks in [src/main/java/com/heapanalyzer/service/MatDownloadService.java](src/main/java/com/heapanalyzer/service/MatDownloadService.java).
- Preserve async flow and status transitions when editing [src/main/java/com/heapanalyzer/service/AnalysisService.java](src/main/java/com/heapanalyzer/service/AnalysisService.java).
- For MCP/chat changes, update both tool definitions and session lifecycle paths to avoid stale session behavior.
