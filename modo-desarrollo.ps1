$ErrorActionPreference = 'Stop'

$runner = Join-Path $PSScriptRoot 'ejecutar-android.ps1'
$sourceRoot = Join-Path $PSScriptRoot 'app\src'

function Install-App {
    Write-Host "`nCompilando e instalando cambios..." -ForegroundColor Cyan
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $runner
    if ($LASTEXITCODE -ne 0) {
        Write-Host 'No se pudieron instalar los cambios.' -ForegroundColor Red
    }
}

Install-App

$watcher = [IO.FileSystemWatcher]::new($sourceRoot, '*.*')
$watcher.IncludeSubdirectories = $true
$watcher.NotifyFilter = [IO.NotifyFilters]'FileName, LastWrite, DirectoryName'
$watcher.EnableRaisingEvents = $true

Write-Host "`nModo desarrollo activo." -ForegroundColor Green
Write-Host 'Guarda un archivo .kt o .xml para reinstalar la app.' -ForegroundColor Green
Write-Host 'Presiona Ctrl+C para detenerlo.' -ForegroundColor Yellow

try {
    while ($true) {
        $change = $watcher.WaitForChanged([IO.WatcherChangeTypes]::All, 1000)

        if (-not $change.TimedOut) {
            $extension = [IO.Path]::GetExtension($change.Name)
            if ($extension -in @('.kt', '.xml')) {
                Start-Sleep -Milliseconds 700
                Install-App
            }
        }
    }
}
finally {
    $watcher.Dispose()
}
