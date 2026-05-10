# Contributing to SmartCLI

## How to Build Locally

### Prerequisites
- Java 17 or later
- Maven 3.8.1 or later
- Git

### Clone & Build
```bash
git clone https://github.com/yourusername/SmartCLI.git
cd SmartCLI
mvn clean package
```

### Run
```bash
# Console mode
java -jar target/SmartCLI.jar --mode console

# GUI mode
java -jar target/SmartCLI.jar --mode gui
```

### Run Tests
```bash
mvn test
```

### Generate Native Installers
```bash
# Windows (.exe)
mvn clean package
jpackage --input target/jpackage-input --name SmartCLI ...

# Linux (.deb)
mvn clean package -P linux

# macOS (.dmg)
mvn clean package -P macos
```

## Creating a Release

### 1. Update Version in pom.xml
```xml
<version>3.2.0</version>
```

### 2. Create Git Tag
```bash
git tag v3.2.0
git push origin v3.2.0
```

### 3. GitHub Actions Automatic Process
- Push the tag
- Workflows trigger automatically
- Builds on all platforms
- Creates GitHub release
- Uploads installers

### 4. Verify Release
- Check GitHub Releases page
- Download and test installers
- Verify JAR file works

## Code Style Guidelines

- Follow Java naming conventions
- Use 4-space indentation
- Add JavaDoc to public methods
- Write unit tests for new features
- Keep error messages user-friendly

## Commit Message Format

```
[TYPE] Brief description

Detailed explanation of what changed and why.

Fixes: #123 (if applicable)
```

Types: feature, bugfix, docs, refactor, test, build

---

**Questions?** Create a GitHub issue or discussion!
