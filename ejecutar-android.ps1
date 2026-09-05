$ErrorActionPreference = 'Stop'

$env:JAVA_HOME = 'C:\Users\edwin\OneDrive\Escritorio\Aplicaciones\Kotlin\kotlin-dev\jdk'
$env:ANDROID_HOME = 'C:\Users\edwin\AppData\Local\Android\Sdk'
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"

$adb = Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'
$devices = & $adb devices
$authorized = @($devices | Where-Object { $_ -match "\sdevice$" })

if ($authorized.Count -eq 0) {
    Write-Host 'No hay un celular Android autorizado.' -ForegroundColor Yellow
    Write-Host 'Conecta el celular, desbloquealo y acepta la depuracion USB.' -ForegroundColor Yellow
    & $adb devices -l
    exit 2
}

Push-Location $PSScriptRoot
try {
    & "$PSScriptRoot\gradlew.bat" :app:installDebug
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    & $adb shell am force-stop com.example.miprograma
    & $adb shell am start -n 'com.example.miprograma/.MainActivity'
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    Write-Host 'Aplicacion instalada y abierta en el celular.' -ForegroundColor Green
}
finally {
    Pop-Location
}
