# Code Review: MAT Download and Execution Flow

**Date**: 2026-06-05  
**Scope**: `MatDownloadService`, `MatAnalysisService`, `MatQueryService`  
**Ready for Production**: No (hardening recommended before broad exposure)  
**Critical Issues**: 0

## Priority 1 (Must Fix)

### High: No integrity verification for downloaded MAT binaries before extraction/execution
- Evidence:
  - `DOWNLOAD_BASE` points to remote artifacts without pinned digest validation.
  - Download accepts any HTTP 200 response and writes directly to disk.
  - Extracted launcher scripts are then used for execution.
- Risk:
  - A compromised upstream/CDN path or malicious redirect target can deliver a trojanized MAT package.
  - This can result in arbitrary code execution when launcher scripts are invoked.
- References:
  - `src/main/java/com/heapanalyzer/service/MatDownloadService.java:36`
  - `src/main/java/com/heapanalyzer/service/MatDownloadService.java:181`
  - `src/main/java/com/heapanalyzer/service/MatDownloadService.java:194`
  - `src/main/java/com/heapanalyzer/service/MatDownloadService.java:225`
  - `src/main/java/com/heapanalyzer/service/MatAnalysisService.java:241`

### High: Windows OQL command argument is not robustly escaped for `cmd.exe /c`
- Evidence:
  - Windows launcher uses `cmd.exe /c` for MAT scripts.
  - OQL query is concatenated into a command argument with quote-only escaping.
  - Validation only enforces `SELECT` prefix.
- Risk:
  - Command metacharacters and Windows expansion semantics can still alter parsing in shell contexts.
  - If attacker-controlled query input reaches this path, this is a command injection surface.
- References:
  - `src/main/java/com/heapanalyzer/service/MatAnalysisService.java:241`
  - `src/main/java/com/heapanalyzer/service/MatAnalysisService.java:244`
  - `src/main/java/com/heapanalyzer/service/MatAnalysisService.java:245`
  - `src/main/java/com/heapanalyzer/service/MatQueryService.java:301`
  - `src/main/java/com/heapanalyzer/service/MatQueryService.java:413`

## Priority 2 (Should Fix)

### Medium: Launcher discovery trusts file existence only (symlink/path trust hardening missing)
- Evidence:
  - Script resolution checks `Files.exists(...)` and selects first matching launcher.
  - No canonical path/owner/symlink policy before execution.
- Risk:
  - A local attacker with write access to configured MAT locations can swap launcher scripts and get code execution under service identity.
- References:
  - `src/main/java/com/heapanalyzer/service/MatDownloadService.java:271`
  - `src/main/java/com/heapanalyzer/service/MatDownloadService.java:287`
  - `src/main/java/com/heapanalyzer/service/MatAnalysisService.java:197`
  - `src/main/java/com/heapanalyzer/service/MatAnalysisService.java:247`

### Medium: Process output is fully buffered in memory, enabling query-driven memory pressure
- Evidence:
  - Command output is collected with `Collectors.joining("\n")` before truncation.
- Risk:
  - Expensive OQL queries can produce very large output and pressure heap memory (DoS).
- References:
  - `src/main/java/com/heapanalyzer/service/MatQueryService.java:438`
  - `src/main/java/com/heapanalyzer/service/MatQueryService.java:440`

## Recommended Remediations (Minimal)

1. Enforce artifact integrity:
   - Maintain OS/arch-specific SHA-256 constants for MAT archives.
   - Verify digest before extraction; fail closed on mismatch.
   - Validate final response URI host against an allowlist after redirects.
2. Harden Windows command construction:
   - Treat OQL as untrusted input.
   - Add strict allowlist validation for query characters/length, rejecting shell metacharacters (`&`, `|`, `<`, `>`, `%`, `!`, `^`, backtick).
   - Prefer non-shell-safe argument passing where possible; otherwise add dedicated `cmd.exe` escaping routine and tests.
3. Harden launcher trust:
   - Resolve to canonical path and ensure launcher is inside expected MAT home.
   - Reject symlink launchers and enforce owner/write permission checks on launcher and parent directory.
4. Bound output memory:
   - Stream process output into a capped buffer (max bytes/lines) instead of joining unbounded output.

## Quick Regression Checklist

- [ ] Tamper test: modified MAT zip fails digest validation and does not execute.
- [ ] Redirect test: download redirected to non-allowlisted host is rejected.
- [ ] Windows injection tests: OQL containing shell metacharacters is rejected/safely handled.
- [ ] Launcher trust test: symlinked `ParseHeapDump.cmd/.bat` is rejected.
- [ ] DoS test: very large MAT output is capped and service remains responsive.
