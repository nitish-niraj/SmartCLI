Smart Command Line System

The full updated project documentation is in README.md.

Quick start:

1. Open PowerShell in this folder.
2. Build:
   .\.tools\apache-maven-3.9.9\bin\mvn.cmd clean package -DskipTests
3. Run:
   cmd.exe /c "chcp 65001 & java -Dfile.encoding=UTF-8 -jar target\SmartCLI.jar"

SmartCLI now uses real disk-based file operations with its own working directory.
Use pwd, cd, create, write, read, delete, list, help, and exit inside the CLI.
