# API and Routes Overview

See also: [Usage Guide](usage.md), [Architecture Overview](architecture.md)

All routes below are defined in [src/main/java/com/heapanalyzer/controller/AnalysisController.java](../src/main/java/com/heapanalyzer/controller/AnalysisController.java).

## Page Routes

| Method | Path | Purpose |
|---|---|---|
| GET | / | Landing page and analysis history |
| GET | /heap | Heap dump page |
| GET | /thread-dump | Thread dump page |
| GET | /gc-log | GC log page |
| GET | /mcp | MCP page |
| GET | /mcp/{sessionId}/logs | MCP request and response log page |
| GET | /history/{id} | Open saved analysis in its matching template |
| GET | /setup | First run setup page |
| GET | /settings | Settings page |

## Settings APIs

| Method | Path | Purpose |
|---|---|---|
| GET | /api/settings | Get current settings and masked key |
| POST | /api/settings | Save API config and reconfigure AI client |
| POST | /api/settings/test | Connectivity test for AI provider |

## MAT APIs

| Method | Path | Purpose |
|---|---|---|
| GET | /api/mat/status | MAT availability and download progress |
| GET | /api/mat/config | Resolve effective MAT heap size for a file size |
| POST | /api/mat/download | Start MAT download and install |

## Prompt APIs

Prompt type values are validated by [src/main/java/com/heapanalyzer/service/SpringAiService.java](../src/main/java/com/heapanalyzer/service/SpringAiService.java):

- heap-dump
- thread-dump
- gc-log

| Method | Path | Purpose |
|---|---|---|
| GET | /api/prompts/{type} | Get current and default prompt |
| POST | /api/prompts/{type} | Save custom prompt |
| DELETE | /api/prompts/{type} | Reset prompt to default |

## Upload And Analysis APIs

| Method | Path | Purpose |
|---|---|---|
| POST | /api/heap/upload | Upload .hprof and start heap analysis |
| POST | /api/thread-dump/upload | Upload thread dump and start analysis |
| POST | /api/gc-log/upload | Upload GC log and start analysis |
| GET | /api/analysis/{id}/status | Poll status and report fields |
| POST | /api/analysis/{id}/save | Persist completed analysis |

## History APIs

| Method | Path | Purpose |
|---|---|---|
| GET | /api/history | List saved analyses |
| GET | /api/history/{id} | Get one saved analysis |
| DELETE | /api/history/{id} | Delete one saved analysis |

## MCP APIs

| Method | Path | Purpose |
|---|---|---|
| POST | /api/mcp/upload | Upload .hprof and create MCP session |
| GET | /api/mcp/status | Session status and MCP endpoint fields |
| DELETE | /api/mcp/session | Close active session and cleanup |
| POST | /api/mcp/chat | Start chat SSE response |
| GET | /api/mcp/{sessionId}/logs/stream | Stream MCP logs via SSE |
| GET | /api/mcp/{sessionId}/logs/recent | Get recent MCP logs |

## Analysis Status Values

Defined in [src/main/java/com/heapanalyzer/model/AnalysisStatus.java](../src/main/java/com/heapanalyzer/model/AnalysisStatus.java):

- UPLOADING
- ANALYZING
- AI_PROCESSING
- COMPLETED
- FAILED

## Related Docs

- [Setup Guide](setup.md)
- [Usage Guide](usage.md)
- [Troubleshooting Guide](troubleshooting.md)
