# SmartCLI

SmartCLI is a cross-platform Smart Command Line System built as a Java 17 Maven project.

## Project Overview

This repository currently contains the Phase 0 foundation only: package layout, contracts, placeholders, and Maven setup for a production-ready starting architecture.

## Planned Features

- Smart command parsing and execution
- Command history and session management
- Autocorrect, fuzzy search, and autocomplete
- Cross-platform shell support
- Plugin architecture
- Storage for aliases and configuration

## Tech Stack

- Java 17
- Maven
- JavaFX
- JLine3
- SQLite JDBC
- Gson
- Apache Commons Lang3
- SLF4J + Logback
- JUnit 5

## Clone

```bash
git clone https://github.com/nitish-niraj/SmartCLI.git
cd SmartCLI
```

## Build

```bash
mvn compile
```

## Run

```bash
java -jar target/SmartCLI.jar
```

## Package Structure

```text
src/main/java/com/lpu/smartcli/
  core/
  data/
  ui/
  smart/
  integration/
  plugins/
  storage/
  utils/

src/test/java/com/lpu/smartcli/
```

## Team

- Nitish Kumar (Leader RD2536B60)
- Nikita Chauhan (RD2526B37)
- Ayush Kumar (RD2526B41)

Supervisor: Dr. Prince Arora

Course: CAP477, LPU

## Phase Note

This phase contains only foundational architecture.
