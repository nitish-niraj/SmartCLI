================================================
 Smart Command Line System — CAP477 | LPU
================================================

 Team: Section D2526
   - Nitish Kumar   (RD2536B60) — Team Leader
   - Nikita Chauhan (RD2526B37)
   - Ayush Kumar    (RD2526B41)

 Supervisor: Dr. Prince Arora

------------------------------------------------
 HOW TO RUN
------------------------------------------------
 Requirement: Java 17 or higher installed.

 Check Java version:
     java -version

 Run the application:
     java -jar SmartCLI.jar

 Run in console mode explicitly:
     java -jar SmartCLI.jar --mode console

------------------------------------------------
 COMMANDS
------------------------------------------------
 create <filename>           Create a new file
 write  <filename> <text>    Write text to a file
 read   <filename>           Read a file
 delete <filename>           Delete a file
 list                        List all files in memory
 help                        Show all commands
 exit                        Exit the application

------------------------------------------------
 EXAMPLE SESSION
------------------------------------------------
 smartcli> create notes.txt
 smartcli> write notes.txt Hello from CAP477
 smartcli> read notes.txt
 smartcli> list
 smartcli> delete notes.txt
 smartcli> exit

================================================
