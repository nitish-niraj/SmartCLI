# SmartCLI v3.2.2 Release Notes

**Released:** May 10, 2026

## ✨ Major Features

### 🎯 Native Installers (Feature #41 - COMPLETED)
- **Windows**: `.exe` installer with automatic PATH setup
- **Linux**: `.deb` package for Debian/Ubuntu distributions
- **macOS**: `.dmg` bundle for easy application installation
- All installers bundle JVM runtime - no Java installation needed by users

### 🤖 AI Natural Language Layer
- Convert natural language to CLI commands
- Example: "make a new file hello.py" → `create hello.py`
- Uses Nvidia's minimaxai model for interpretation

### 🚀 Enhanced CLI Features
- **JLine3 Console**: Professional readline with arrow-key history
- **JavaFX GUI**: Multi-tab terminal with syntax highlighting
- **Git Integration**: Real-time Git status display
- **Fuzzy Search**: Approximate matching in command history
- **Tab Auto-Completion**: Smart suggestions for commands and files
- **Alias System**: Create custom command shortcuts
- **Theme Persistence**: Save dark/light theme preferences

### 📊 Database & Persistence
- SQLite command history tracking
- Session management with UUID tracking
- JSON configuration file management
- Cross-session state preservation

## 🔧 Build & DevOps

### GitHub Actions CI/CD Pipeline
- **Automatic Testing**: Run tests on every commit
- **Cross-Platform Builds**: Build on Windows, Linux, macOS
- **Automated Releases**: Trigger releases on version tags
- **Artifact Management**: Upload installers to releases

### Release Automation
- Push tag like `git push origin v3.2.2`
- Workflows automatically:
  1. Build on all platforms
  2. Generate native installers
  3. Create GitHub release
  4. Upload all artifacts

## 📦 Download

### Latest Version: v3.2.2

**Choose your platform:**

| Platform | Download | Size | Requires |
|----------|----------|------|----------|
| **Windows** | SmartCLI-3.2.2.exe | ~150MB | None (JVM included) |
| **Linux** | smartcli-3.2.2.deb | ~120MB | None (JVM included) |
| **macOS** | SmartCLI-3.2.2.dmg | ~160MB | None (JVM included) |
| **Cross-Platform** | SmartCLI-3.2.2.jar | ~45MB | Java 17+ |

## 🚀 Quick Start

### Windows
```cmd
SmartCLI-3.2.2.exe
# Or via command line:
SmartCLI.exe --mode console
```

### Linux
```bash
sudo dpkg -i smartcli-3.2.2.deb
smartcli --mode console
```

### macOS
```bash
open SmartCLI-3.2.2.dmg
# Drag to Applications, then:
/Applications/SmartCLI.app/Contents/MacOS/SmartCLI
```

### Universal (Java)
```bash
java -jar SmartCLI-3.2.2.jar --mode console
java -jar SmartCLI-3.2.2.jar --mode gui
```

## 📋 Feature Checklist

- ✅ Real OS Command Execution
- ✅ Cross-Platform Support (Windows/Linux/macOS)
- ✅ Async Non-Blocking Execution
- ✅ Real-Time Output Streaming
- ✅ In-Memory File System (6 operations)
- ✅ Command Interface Pattern
- ✅ 7+ Command Implementations
- ✅ Input Tokenizer & Parser
- ✅ Levenshtein Auto-Correct
- ✅ Fuzzy History Search
- ✅ Tab Auto-Completion
- ✅ Syntax Highlighting
- ✅ Alias System
- ✅ SQLite History Database
- ✅ Session Management
- ✅ JSON Configuration
- ✅ Theme Persistence
- ✅ JLine3 Console Terminal
- ✅ JavaFX GUI
- ✅ Git Integration
- ✅ File System Browser
- ✅ Process Manager
- ✅ Centralized Error Handler
- ✅ JUnit5 Unit Tests
- ✅ Executable JAR
- ✅ Native Installers (NEW)
- ✅ AI Natural Language Layer
- ✅ GitHub Actions CI/CD

## 🔄 What's Changed Since v3.1.0

### New
- Native installers for all platforms
- Automated GitHub Actions workflows
- Cross-platform build matrix
- Release drafter configuration

### Improved
- Enhanced Maven build configuration
- Better jpackage integration
- Streamlined installer generation
- Automated release process

### Fixed
- Build consistency across platforms
- Installer size optimization

## 🛠️ System Requirements

### Windows
- Windows 10 or later
- 200MB disk space
- **No Java needed** (bundled)

### Linux (Debian/Ubuntu)
- Ubuntu 20.04+ or Debian 11+
- 200MB disk space
- **No Java needed** (bundled)

### macOS
- macOS 11 or later
- 250MB disk space
- **No Java needed** (bundled)

### Command Line JAR
- Java 17 or later
- 50MB disk space
- Works on any OS with Java

## 📞 Support & Feedback

- **GitHub Issues**: Report bugs or request features
- **GitHub Discussions**: Ask questions and share ideas
- **Documentation**: See FEATURE_REPORT.md for detailed feature list

## 🙏 Contributors

- **Developed by**: LPU CAP477 Student
- **Project**: SmartCLI - Intelligent Command Line Interface
- **License**: MIT

---

**Thank you for using SmartCLI!** 🚀
