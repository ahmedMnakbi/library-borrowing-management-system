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

The root README displays the rendered diagrams directly. This index links both the GitHub-ready SVG files and the editable PlantUML sources.

| Diagram | Rendered SVG | PlantUML source |
|---|---|---|
| Class Diagram | [SVG](diagrams/rendered/class-diagram.svg) | [PUML](diagrams/class-diagram.puml) |
| Architecture Diagram | [SVG](diagrams/rendered/architecture-diagram.svg) | [PUML](diagrams/architecture-diagram.puml) |
| Borrow Sequence | [SVG](diagrams/rendered/borrow-sequence.svg) | [PUML](diagrams/borrow-sequence.puml) |
| Return Sequence | [SVG](diagrams/rendered/return-sequence.svg) | [PUML](diagrams/return-sequence.puml) |
| Reservation Sequence | [SVG](diagrams/rendered/reservation-sequence.svg) | [PUML](diagrams/reservation-sequence.puml) |
| Database Overview | [SVG](diagrams/rendered/database-overview.svg) | [PUML](diagrams/database-overview.puml) |

Render again with PlantUML if a `.puml` file changes:

```bash
plantuml -tsvg docs/diagrams/*.puml
```

Or with a local jar:

```bash
java -jar tools/plantuml.jar -tsvg -o rendered docs/diagrams/*.puml
```
