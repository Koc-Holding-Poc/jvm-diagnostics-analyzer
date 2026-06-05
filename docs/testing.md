# Testing Guide

See also: [Setup Guide](setup.md), [Troubleshooting Guide](troubleshooting.md), [Contributing](../CONTRIBUTING.md)

## Test Scope

Tests are in [src/test/java/com/heapanalyzer](../src/test/java/com/heapanalyzer):

- Controller tests in [src/test/java/com/heapanalyzer/controller](../src/test/java/com/heapanalyzer/controller)
- Service tests in [src/test/java/com/heapanalyzer/service](../src/test/java/com/heapanalyzer/service)
- Model tests in [src/test/java/com/heapanalyzer/model](../src/test/java/com/heapanalyzer/model)

## Core Commands

Run full test suite:

```bash
mvn -B test
```

Compile only:

```bash
mvn -B clean compile
```

Package without tests:

```bash
mvn -B clean package -DskipTests
```

## CI Parity

The CI workflow in [.github/workflows/ci.yml](../.github/workflows/ci.yml) runs:

1. mvn -B -DskipTests dependency:go-offline
2. mvn -B clean compile
3. mvn -B test

Running these locally helps reproduce CI outcomes.

## Practical Local Validation

After backend changes, validate:

1. App starts with mvn spring-boot:run.
2. Upload endpoints accept expected file types.
3. /api/analysis/{id}/status transitions through lifecycle values.
4. Settings save and test endpoint work when API key is set.

## Change Coupling Reminders

From project instructions and AGENTS guidance:

- If you change controller contracts, update controller tests.
- If you change service behavior, add or update service tests.
- If you add model fields, update model tests and any serialization dependent assertions.

## Related Docs

- [Architecture Overview](architecture.md)
- [API and Routes Overview](api-routes.md)
