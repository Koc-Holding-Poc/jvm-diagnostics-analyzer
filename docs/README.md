# Documentation Index

This folder contains focused project documentation for developers and contributors.

The goal is to provide a fast path for day to day work without replacing deep reference documents that already exist at the repository root.

## Start Here

- [Setup Guide](setup.md)
- [Usage Guide](usage.md)
- [Architecture Overview](architecture.md)
- [API and Routes Overview](api-routes.md)
- [Testing Guide](testing.md)
- [Troubleshooting Guide](troubleshooting.md)

## Existing Deep Dive Documents

These docs already exist and are still the best source for their specific topics:

- [Root README](../README.md) for full feature overview and quick start options
- [Heap Analysis Pipeline](../HEAP_ANALYSIS.md) for MAT report extraction details
- [Docker Build and Publish](../DOCKER.md) for image build and release flows
- [Contributing](../CONTRIBUTING.md) for contribution expectations

## Conventions Followed In This Folder

- Commands and settings are based on [pom.xml](../pom.xml), [compose.yaml](../compose.yaml), [Dockerfile](../Dockerfile), and [src/main/resources/application.yml](../src/main/resources/application.yml)
- Routes are derived from [src/main/java/com/heapanalyzer/controller/AnalysisController.java](../src/main/java/com/heapanalyzer/controller/AnalysisController.java)
- Architecture notes are grounded in [src/main/java/com/heapanalyzer/service](../src/main/java/com/heapanalyzer/service) and [src/main/java/com/heapanalyzer/mcp](../src/main/java/com/heapanalyzer/mcp)
