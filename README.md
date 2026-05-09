# Smart Command Line System

SmartCLI is a Java 17 command-line interpreter built for CAP477 at LPU. It supports custom in-memory file commands and can pass unknown commands through to the real operating system shell.

## Features

- Built-in commands: `create`, `write`, `read`, `delete`, `list`, `help`, `exit`
- In-memory file system backed by `HashMap<String, String>`
- Command parser and command registry using OOP
- Autocomplete, autocorrect, and fuzzy search components
- OS command passthrough using `ProcessBuilder`
- Cross-platform shell execution: `cmd.exe /c` on Windows, `sh -c` on Linux/macOS
- JUnit test suite with 109 passing tests

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
