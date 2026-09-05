$ErrorActionPreference = 'Stop'

$env:JAVA_HOME = 'C:\Users\edwin\OneDrive\Escritorio\Aplicaciones\Kotlin\kotlin-dev\jdk'
$env:ANDROID_HOME = 'C:\Users\edwin\AppData\Local\Android\Sdk'
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"

Push-Location $PSScriptRoot
try {
    & "$PSScriptRoot\gradlew.bat" :app:assembleDebug
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    $apk = Join-Path $PSScriptRoot 'app\build\outputs\apk\debug\app-debug.apk'
    Write-Host "APK creado: $apk" -ForegroundColor Green
}
finally {
    Pop-Location
}
