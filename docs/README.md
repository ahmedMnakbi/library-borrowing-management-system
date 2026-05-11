# Project Documentation

This folder groups the V2 documentation and diagram sources.

## Main Project Pages

- [Project README](../README.md)
- [Course Concepts](../CONCEPTS_USED.md)
- [Professor Demo Guide](../PROF_DEMO_GUIDE.md)
- [Manual Test Script](../TEST_SCRIPT.md)
- [Demo Database Scenarios](../DEMO_DATABASE_SCENARIOS.md)

## Setup Script

- [setup-and-open.ps1](../setup-and-open.ps1)
- [setup-and-open.bat](../setup-and-open.bat)

The setup script is optional. It checks Java 17+, Maven, MySQL/MariaDB, project files, compiles the project, and can optionally import the demo database, run the console app, and open an IDE.

## Diagrams

PlantUML source files:

- [Class Diagram](diagrams/class-diagram.puml)
- [Architecture Diagram](diagrams/architecture-diagram.puml)
- [Borrow Sequence](diagrams/borrow-sequence.puml)
- [Return Sequence](diagrams/return-sequence.puml)
- [Reservation Sequence](diagrams/reservation-sequence.puml)
- [Database Overview](diagrams/database-overview.puml)

Render later with PlantUML:

```bash
plantuml -tsvg docs/diagrams/*.puml
```

Or with a local jar:

```bash
java -jar tools/plantuml.jar -tsvg docs/diagrams/*.puml
```
