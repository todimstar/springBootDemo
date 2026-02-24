$ErrorActionPreference = 'Stop'

$codexHome = Join-Path $env:USERPROFILE '.codex'
$roots = @(
    (Join-Path $codexHome 'sessions'),
    (Join-Path $codexHome 'archived_sessions')
)

foreach ($root in $roots) {
    if (-not (Test-Path $root)) {
        throw "Path not found: $root"
    }
}

$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$backupRoot = Join-Path $codexHome "backups\bom-repair-$stamp"
New-Item -ItemType Directory -Path $backupRoot -Force | Out-Null

$files = foreach ($root in $roots) {
    Get-ChildItem -Path $root -Recurse -Filter '*.jsonl' -File
}

$fixed = 0
$skipped = 0
$failed = New-Object System.Collections.Generic.List[string]

foreach ($file in $files) {
    try {
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
        if ($bytes.Length -lt 3) {
            $skipped++
            continue
        }

        $hasBom = ($bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF)
        if (-not $hasBom) {
            $skipped++
            continue
        }

        $relative = $file.FullName.Substring($codexHome.Length).TrimStart('\')
        $backupFile = Join-Path $backupRoot $relative
        New-Item -ItemType Directory -Path (Split-Path -Path $backupFile -Parent) -Force | Out-Null
        Copy-Item -Path $file.FullName -Destination $backupFile -Force

        $newBytes = New-Object byte[] ($bytes.Length - 3)
        [Array]::Copy($bytes, 3, $newBytes, 0, $newBytes.Length)
        [System.IO.File]::WriteAllBytes($file.FullName, $newBytes)

        $fixed++
    }
    catch {
        $failed.Add($file.FullName) | Out-Null
    }
}

$bomLeft = 0
foreach ($file in $files) {
    try {
        $fs = [System.IO.File]::OpenRead($file.FullName)
        $b0 = $fs.ReadByte()
        $b1 = $fs.ReadByte()
        $b2 = $fs.ReadByte()
        $fs.Dispose()
        if ($b0 -eq 0xEF -and $b1 -eq 0xBB -and $b2 -eq 0xBF) {
            $bomLeft++
        }
    }
    catch {
    }
}

Write-Host "FIXED=$fixed SKIPPED=$skipped FAILED=$($failed.Count)"
Write-Host "BOM_LEFT=$bomLeft"
Write-Host "BACKUP=$backupRoot"

if ($failed.Count -gt 0) {
    Write-Host 'FAILED_FILES:'
    $failed | ForEach-Object { Write-Host $_ }
}

if ($failed.Count -gt 0 -or $bomLeft -gt 0) {
    exit 1
}

exit 0
