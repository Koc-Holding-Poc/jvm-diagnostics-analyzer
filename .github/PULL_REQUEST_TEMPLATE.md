## Summary
Describe the problem and the approach.

## Changes
- 

## How To Test
- [ ] `mvn -B test`
- [ ] Manual verification for changed endpoints/pages

## Maintenance Matrix Checklist
- [ ] If [src/main/java/com/heapanalyzer/model/AnalysisType.java](src/main/java/com/heapanalyzer/model/AnalysisType.java) changed, I updated controller/service/template/tests.
- [ ] If [src/main/java/com/heapanalyzer/controller/AnalysisController.java](src/main/java/com/heapanalyzer/controller/AnalysisController.java) changed, I updated [src/test/java/com/heapanalyzer/controller/AnalysisControllerTest.java](src/test/java/com/heapanalyzer/controller/AnalysisControllerTest.java).
- [ ] If MCP tools/chat/config changed, I updated related registration/session/config docs and tests.
- [ ] If behavior changed, I updated [README.md](README.md) and [CHANGELOG.md](CHANGELOG.md) as needed.

## Checklist
- [ ] Focused change set (no unrelated refactors)
- [ ] Backward compatibility considered for public endpoints
- [ ] New/updated tests included
