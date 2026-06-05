# Copilot Instructions For JVM Diagnostics Analyzer

## Purpose
Contribute safely to a Java/Spring diagnostics tool that analyzes heap dumps, thread dumps, and GC logs, with AI-assisted explanations and MCP tool access.

## Language And Framework Conventions
- Use Java for backend logic under [src/main/java/com/heapanalyzer](src/main/java/com/heapanalyzer).
- Follow Spring Boot conventions: constructor injection, `@Service` for domain services, `@Controller` for routes, and explicit endpoint mappings.
- Keep routing/API concerns in [src/main/java/com/heapanalyzer/controller/AnalysisController.java](src/main/java/com/heapanalyzer/controller/AnalysisController.java).
- Keep parsing/analysis logic in dedicated service classes under [src/main/java/com/heapanalyzer/service](src/main/java/com/heapanalyzer/service).

## MCP And AI Conventions
- MCP tools are implemented in [src/main/java/com/heapanalyzer/mcp/HeapDumpMcpTools.java](src/main/java/com/heapanalyzer/mcp/HeapDumpMcpTools.java) and registered via [src/main/java/com/heapanalyzer/config/McpToolConfig.java](src/main/java/com/heapanalyzer/config/McpToolConfig.java).
- Do not invent analysis values in prompts or AI output handling; preserve tool-result-first behavior.
- Keep chat stream behavior and event names consistent in [src/main/java/com/heapanalyzer/service/HeapDumpChatService.java](src/main/java/com/heapanalyzer/service/HeapDumpChatService.java).

## Testing Conventions
- Run `mvn -B test` before proposing merges.
- Add unit tests for service changes in [src/test/java/com/heapanalyzer/service](src/test/java/com/heapanalyzer/service).
- Add/update controller tests in [src/test/java/com/heapanalyzer/controller/AnalysisControllerTest.java](src/test/java/com/heapanalyzer/controller/AnalysisControllerTest.java) when endpoints or HTTP contracts change.
- For model changes, update matching tests in [src/test/java/com/heapanalyzer/model](src/test/java/com/heapanalyzer/model).

## CI And Local Commands
- Install dependencies: `mvn -B -DskipTests dependency:go-offline`
- Build: `mvn -B clean package`
- Test: `mvn -B test`
- Run app: `mvn spring-boot:run`

## Maintenance Matrix
Use this matrix whenever you edit code. If one file group changes, update linked areas in the same PR.

| If you modify | Also review/update |
|---|---|
| [src/main/java/com/heapanalyzer/model/AnalysisType.java](src/main/java/com/heapanalyzer/model/AnalysisType.java) | [src/main/java/com/heapanalyzer/service/AnalysisService.java](src/main/java/com/heapanalyzer/service/AnalysisService.java), [src/main/java/com/heapanalyzer/controller/AnalysisController.java](src/main/java/com/heapanalyzer/controller/AnalysisController.java), related templates in [src/main/resources/templates](src/main/resources/templates), and tests in [src/test/java/com/heapanalyzer](src/test/java/com/heapanalyzer) |
| [src/main/java/com/heapanalyzer/controller/AnalysisController.java](src/main/java/com/heapanalyzer/controller/AnalysisController.java) | Controller tests in [src/test/java/com/heapanalyzer/controller/AnalysisControllerTest.java](src/test/java/com/heapanalyzer/controller/AnalysisControllerTest.java), affected services under [src/main/java/com/heapanalyzer/service](src/main/java/com/heapanalyzer/service), and UI templates under [src/main/resources/templates](src/main/resources/templates) |
| [src/main/java/com/heapanalyzer/service/AnalysisService.java](src/main/java/com/heapanalyzer/service/AnalysisService.java) | Downstream analysis services ([src/main/java/com/heapanalyzer/service/MatAnalysisService.java](src/main/java/com/heapanalyzer/service/MatAnalysisService.java), [src/main/java/com/heapanalyzer/service/ThreadDumpAnalysisService.java](src/main/java/com/heapanalyzer/service/ThreadDumpAnalysisService.java), [src/main/java/com/heapanalyzer/service/GcLogAnalysisService.java](src/main/java/com/heapanalyzer/service/GcLogAnalysisService.java)), plus tests in [src/test/java/com/heapanalyzer/service/AnalysisServiceTest.java](src/test/java/com/heapanalyzer/service/AnalysisServiceTest.java) |
| [src/main/java/com/heapanalyzer/mcp/HeapDumpMcpTools.java](src/main/java/com/heapanalyzer/mcp/HeapDumpMcpTools.java) | Tool registration in [src/main/java/com/heapanalyzer/config/McpToolConfig.java](src/main/java/com/heapanalyzer/config/McpToolConfig.java), MAT query logic in [src/main/java/com/heapanalyzer/service/MatQueryService.java](src/main/java/com/heapanalyzer/service/MatQueryService.java), and MCP/session flows in [src/main/java/com/heapanalyzer/service/McpSessionManager.java](src/main/java/com/heapanalyzer/service/McpSessionManager.java) |
| [src/main/java/com/heapanalyzer/service/HeapDumpChatService.java](src/main/java/com/heapanalyzer/service/HeapDumpChatService.java) | Chat endpoint behavior in [src/main/java/com/heapanalyzer/controller/AnalysisController.java](src/main/java/com/heapanalyzer/controller/AnalysisController.java), model/provider setup in [src/main/resources/application.yml](src/main/resources/application.yml), and MCP tools in [src/main/java/com/heapanalyzer/mcp/HeapDumpMcpTools.java](src/main/java/com/heapanalyzer/mcp/HeapDumpMcpTools.java) |
| [src/main/resources/application.yml](src/main/resources/application.yml) | Config persistence and masking in [src/main/java/com/heapanalyzer/service/ConfigService.java](src/main/java/com/heapanalyzer/service/ConfigService.java), docs in [README.md](README.md), and setup UI in [src/main/resources/templates/setup.html](src/main/resources/templates/setup.html) |
| Any template in [src/main/resources/templates](src/main/resources/templates) | Matching controller model attributes in [src/main/java/com/heapanalyzer/controller/AnalysisController.java](src/main/java/com/heapanalyzer/controller/AnalysisController.java), CSS in [src/main/resources/static/css/app.css](src/main/resources/static/css/app.css), and endpoint wiring |
| GitHub workflows in [.github/workflows](.github/workflows) | Keep commands aligned with [pom.xml](pom.xml), Java version, and docs in [README.md](README.md) |
| Public behavior/docs | Update [README.md](README.md) and [CHANGELOG.md](CHANGELOG.md) in the same PR |

## PR Review Patterns To Follow
- Keep each PR focused to one concern (analysis logic, controller contract, MCP/chat, or docs/workflow).
- Preserve backward-compatible endpoint payloads unless the PR explicitly migrates clients.
- Include tests for any bug fix or behavioral change.
- Avoid broad refactors mixed with feature work.
