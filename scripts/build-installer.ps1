Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Push-Location (Join-Path $PSScriptRoot "..")
try {
    .\.tools\apache-maven-3.9.9\bin\mvn.cmd clean package -DskipTests
    $inputDir = "target\jpackage-input"
    if (Test-Path -LiteralPath $inputDir) {
        Remove-Item -LiteralPath $inputDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $inputDir | Out-Null
    Copy-Item -LiteralPath "target\SmartCLI.jar" -Destination $inputDir
    if (Test-Path -LiteralPath "target\installer") {
        Remove-Item -LiteralPath "target\installer" -Recurse -Force
    }
    jpackage `
        --type app-image `
        --name SmartCLI `
        --app-version 1.0.0 `
        --vendor "LPU CAP477" `
        --input $inputDir `
        --main-jar SmartCLI.jar `
        --main-class com.lpu.smartcli.ui.Terminal `
        --dest target\installer
} finally {
    Pop-Location
}
