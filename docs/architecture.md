# Architecture Overview

See also: [Documentation Index](README.md), [API and Routes Overview](api-routes.md), [Heap Analysis Pipeline](../HEAP_ANALYSIS.md)

## System Shape

JVM Diagnostics Analyzer is a Spring Boot web application that combines:

- Server-rendered UI pages (Thymeleaf templates)
- HTTP APIs for upload, status, settings, history, and MCP session control
- Asynchronous analysis pipelines for heap dumps, thread dumps, and GC logs
- Optional AI interpretation through Spring AI with an OpenAI compatible endpoint
- MCP tools for interactive heap analysis

## Main Runtime Components

- Entry point: [src/main/java/com/heapanalyzer/HeapDumpAnalyzerApplication.java](../src/main/java/com/heapanalyzer/HeapDumpAnalyzerApplication.java)
- Web controller: [src/main/java/com/heapanalyzer/controller/AnalysisController.java](../src/main/java/com/heapanalyzer/controller/AnalysisController.java)
- Pipeline orchestrator: [src/main/java/com/heapanalyzer/service/AnalysisService.java](../src/main/java/com/heapanalyzer/service/AnalysisService.java)
- Heap pipeline internals: [src/main/java/com/heapanalyzer/service/MatAnalysisService.java](../src/main/java/com/heapanalyzer/service/MatAnalysisService.java)
- Thread pipeline internals: [src/main/java/com/heapanalyzer/service/ThreadDumpAnalysisService.java](../src/main/java/com/heapanalyzer/service/ThreadDumpAnalysisService.java)
- GC pipeline internals: [src/main/java/com/heapanalyzer/service/GcLogAnalysisService.java](../src/main/java/com/heapanalyzer/service/GcLogAnalysisService.java)
- AI gateway: [src/main/java/com/heapanalyzer/service/SpringAiService.java](../src/main/java/com/heapanalyzer/service/SpringAiService.java)
- MCP tool definitions: [src/main/java/com/heapanalyzer/mcp/HeapDumpMcpTools.java](../src/main/java/com/heapanalyzer/mcp/HeapDumpMcpTools.java)
- MCP tool registration: [src/main/java/com/heapanalyzer/config/McpToolConfig.java](../src/main/java/com/heapanalyzer/config/McpToolConfig.java)

## High Level Flow

1. A user uploads a diagnostic file on a tool page.
2. The file is persisted by the storage service.
3. A new analysis state is created and processed asynchronously.
4. Static analysis is performed by the selected analyzer service.
5. The static report is optionally sent to AI for interpretation.
6. UI polls status until completion and renders report content.

## Processing Pipelines

### Heap Dump

1. Upload .hprof file via /api/heap/upload.
2. MAT based static analysis runs in [src/main/java/com/heapanalyzer/service/MatAnalysisService.java](../src/main/java/com/heapanalyzer/service/MatAnalysisService.java).
3. AI interpretation is generated via Spring AI.
4. Temporary files are cleaned up in [src/main/java/com/heapanalyzer/service/AnalysisService.java](../src/main/java/com/heapanalyzer/service/AnalysisService.java).

### Thread Dump

1. Upload file via /api/thread-dump/upload.
2. Parse and summarize thread states and deadlock indicators.
3. Send summary to AI using thread dump prompt profile.

### GC Log

1. Upload file via /api/gc-log/upload.
2. Parse collector behavior and pause metrics.
3. Send summary to AI using GC log prompt profile.

## MCP And Chat

- MCP heap session upload starts at /api/mcp/upload.
- Session status is exposed at /api/mcp/status.
- JSON-RPC traffic is observed by [src/main/java/com/heapanalyzer/filter/McpLoggingFilter.java](../src/main/java/com/heapanalyzer/filter/McpLoggingFilter.java) and streamed to UI log consumers.
- Chat endpoint /api/mcp/chat streams SSE events from [src/main/java/com/heapanalyzer/service/HeapDumpChatService.java](../src/main/java/com/heapanalyzer/service/HeapDumpChatService.java).

## State Model

- Analysis status values are defined in [src/main/java/com/heapanalyzer/model/AnalysisStatus.java](../src/main/java/com/heapanalyzer/model/AnalysisStatus.java): UPLOADING, ANALYZING, AI_PROCESSING, COMPLETED, FAILED.
- Analysis type values are defined in [src/main/java/com/heapanalyzer/model/AnalysisType.java](../src/main/java/com/heapanalyzer/model/AnalysisType.java): HEAP_DUMP, THREAD_DUMP, GC_LOG.

## Frontend And Templates

Templates are in [src/main/resources/templates](../src/main/resources/templates):

- index.html
- heap.html
- thread-dump.html
- gc-log.html
- mcp.html
- mcp-logs.html
- setup.html

App styles live in [src/main/resources/static/css/app.css](../src/main/resources/static/css/app.css).

## Related Docs

- [Setup Guide](setup.md)
- [Usage Guide](usage.md)
- [Testing Guide](testing.md)
- [Troubleshooting Guide](troubleshooting.md)
