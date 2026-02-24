@echo off
setlocal EnableExtensions
chcp 65001 >nul

echo [1/3] Close all Codex CLI / VSCode windows first.
echo Press any key to start BOM repair. Press Ctrl+C to cancel.
pause >nul

set "SCRIPT=%~dp0repair_codex_jsonl_bom.ps1"
if not exist "%SCRIPT%" (
    echo Script not found: "%SCRIPT%"
    pause
    exit /b 1
)

echo [2/3] Running BOM repair for Codex jsonl files...
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%"
set "EC=%ERRORLEVEL%"
echo.

if "%EC%"=="0" (
    echo [3/3] Repair complete. Validation passed.
) else (
    echo [3/3] Repair incomplete. ExitCode=%EC%.
    echo Check FAILED_FILES and BOM_LEFT in output.
)

pause
exit /b %EC%
