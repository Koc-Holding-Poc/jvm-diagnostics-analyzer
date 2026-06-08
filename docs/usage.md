# Usage Guide

See also: [Setup Guide](setup.md), [API and Routes Overview](api-routes.md), [Troubleshooting Guide](troubleshooting.md)

## Web Pages

Main UI routes:

- /
- /heap
- /thread-dump
- /gc-log
- /mcp
- /settings

All page routes are handled in [src/main/java/com/heapanalyzer/controller/AnalysisController.java](../src/main/java/com/heapanalyzer/controller/AnalysisController.java).

## Heap Dump Analysis

1. Open /heap.
2. Upload a .hprof file.
3. Polling starts against /api/analysis/{id}/status.
4. Wait for ANALYZING then AI_PROCESSING then COMPLETED.

Notes:

- Heap flow depends on MAT availability.
- MAT can be auto-downloaded by the application when needed.

## Thread Dump Analysis

1. Open /thread-dump.
2. Upload one or more thread dump files (.txt, .tdump, or .log).
3. For trend analysis, upload 3-5 dumps captured at regular intervals.
4. Poll status until COMPLETED.

## GC Log Analysis

1. Open /gc-log.
2. Upload a GC log file (.log or .txt).
3. Poll status until COMPLETED.

## Save And Reopen Results

- Save a completed run: POST /api/analysis/{id}/save
- List saved runs: GET /api/history
- Open from UI: /history/{id}

## MCP Workflow

1. Open /mcp.
2. Upload a .hprof via /api/mcp/upload.
3. Check status using /api/mcp/status.
4. Use SSE endpoint for MCP transport as exposed by status response.
5. Open log viewer page at /mcp/{sessionId}/logs.

## Built In Chat Workflow

1. Ensure an active parsed MCP session exists.
2. Send POST /api/mcp/chat with message and optional stream flag.
3. Consume SSE events.

Chat event names are emitted by [src/main/java/com/heapanalyzer/service/HeapDumpChatService.java](../src/main/java/com/heapanalyzer/service/HeapDumpChatService.java):

- text
- tool_call
- done
- error

## Related Docs

- [Architecture Overview](architecture.md)
- [Testing Guide](testing.md)
- [Heap Analysis Pipeline](../HEAP_ANALYSIS.md)
