# Troubleshooting Guide

See also: [Setup Guide](setup.md), [Usage Guide](usage.md), [API and Routes Overview](api-routes.md)

## App Does Not Start

Checks:

1. Verify Java 25.
2. Verify Maven is available.
3. Check port conflicts on 8080 or set SERVER_PORT.

Commands:

```bash
java -version
mvn -version
```

## AI Is Not Configured

Symptoms:

- Setup page appears on first run.
- /api/settings/test fails.

Checks:

1. Set OPENROUTER_API_KEY or save it through /settings.
2. Verify effective config from /api/settings.

Config persistence is managed by [src/main/java/com/heapanalyzer/service/ConfigService.java](../src/main/java/com/heapanalyzer/service/ConfigService.java).

## Heap Analysis Fails Because MAT Is Missing

Checks:

1. GET /api/mat/status to confirm availability.
2. Trigger POST /api/mat/download.
3. Confirm APP_MAT_HOME if running outside Docker.

Reference implementation: [src/main/java/com/heapanalyzer/service/MatDownloadService.java](../src/main/java/com/heapanalyzer/service/MatDownloadService.java).

## Upload Rejected Or Fails

Checks:

1. Confirm file extension:
   - Heap: .hprof
   - Thread: .txt, .tdump, .log
   - GC: .log, .txt
2. Confirm multipart size settings in [src/main/resources/application.yml](../src/main/resources/application.yml).
3. Confirm storage path permissions for APP_STORAGE_LOCATION.

## Analysis Stays In Progress

Checks:

1. Poll /api/analysis/{id}/status and inspect errorMessage.
2. Check application logs for MAT timeout or parser errors.
3. Increase APP_MAT_TIMEOUT_MINUTES for very large heap files.
4. Set MAT_HEAP_SIZE when automatic memory sizing is not sufficient.

Pipeline orchestration is in [src/main/java/com/heapanalyzer/service/AnalysisService.java](../src/main/java/com/heapanalyzer/service/AnalysisService.java).

## MCP Session Or Chat Issues

Checks:

1. Upload a heap file through /api/mcp/upload before using MCP tools.
2. Verify session state with /api/mcp/status.
3. Confirm MCP message traffic reaches /mcp/message.
4. Review live logs in /mcp/{sessionId}/logs.

Relevant classes:

- [src/main/java/com/heapanalyzer/service/McpSessionManager.java](../src/main/java/com/heapanalyzer/service/McpSessionManager.java)
- [src/main/java/com/heapanalyzer/filter/McpLoggingFilter.java](../src/main/java/com/heapanalyzer/filter/McpLoggingFilter.java)
- [src/main/java/com/heapanalyzer/service/HeapDumpChatService.java](../src/main/java/com/heapanalyzer/service/HeapDumpChatService.java)

## Docker Specific Issues

Checks:

1. Confirm OPENROUTER_API_KEY is passed into container.
2. Confirm upload volume is mounted.
3. Rebuild image after dependency changes.

Commands:

```bash
docker compose up --build
docker compose logs -f
```

See [Docker Build and Publish](../DOCKER.md) for image level issues.

## Related Docs

- [Testing Guide](testing.md)
- [Architecture Overview](architecture.md)
