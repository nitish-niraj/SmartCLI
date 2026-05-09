# Smart Command Line System

SmartCLI is a Java 17 command-line interpreter built for CAP477 at LPU. It supports custom in-memory file commands, natural language AI interpretations, and can pass unknown commands through to the real operating system shell with built-in safety filters.

## Features

- **Built-in commands:** `create`, `write`, `read`, `delete`, `list`, `help`, `exit`
- **In-memory file system:** Backed by `HashMap<String, String>`
- **AI Command Interpretation:** Uses NVIDIA's Minimax model to translate natural language to CLI commands (requires `nvidia.api.key` in `config.properties`)
- **OS Safety Filter:** Prevents dangerous commands (e.g. `rm -rf`), prompts on sensitive commands (e.g. `del`, `git clean`), and prevents infinite long-running tasks.
- **Smart CLI Tools:** Features an Autocompleter, Autocorrect, Syntax Highlighter, and Fuzzy Searcher for an enhanced UX.
- **Command parser & Registry:** OOP-based modular architecture.
- **OS command passthrough:** Real OS shell pass-through using `ProcessBuilder`.
- **Session & History Manager:** Maintains session states and command history across uses.
- **Cross-platform shell execution:** `cmd.exe /c` on Windows, `sh -c` on Linux/macOS
- **Robust testing:** Comprehensive JUnit test suite coverage.

## Build

```powershell
.\.tools\apache-maven-3.9.9\bin\mvn.cmd clean package -DskipTests
```

## Run

```powershell
java -jar target\SmartCLI.jar
```

For correct Unicode box display on Windows CMD:

```cmd
chcp 65001
java -Dfile.encoding=UTF-8 -jar target\SmartCLI.jar
```

## Commands

```text
create <filename>           Create a new file
write  <filename> <text>    Write text to a file
read   <filename>           Read a file
delete <filename>           Delete a file
list                        List all files in memory
help                        Show all commands
exit                        Exit the application
```

### AI Natural Language Commands

If you have an NVIDIA API key configured, you can type natural language statements such as:
```text
smartcli> make a file named test.txt
smartcli> write hello world into test.txt
smartcli> show me the contents of test.txt
```
The AI client will translate these into native `create`, `write`, and `read` commands.

### OS Passthrough

Any command that is not built in is passed to the real OS shell:

```text
smartcli> dir
smartcli> ipconfig
smartcli> git status
smartcli> java -version
```

## Team

- Nitish Kumar (RD2536B60) — Team Leader
- Nikita Chauhan (RD2526B37)
- Ayush Kumar (RD2526B41)

Supervisor: Dr. Prince Arora

Course: CAP477, LPU
