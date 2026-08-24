@echo off
setlocal enabledelayedexpansion

rem Minimal Windows dev script for VIP frontend
rem Usage: scripts\dev-frontend.bat [BACKEND_URL]

set "BACKEND_URL=%~1"
if "%BACKEND_URL%"=="" set "BACKEND_URL=http://localhost:8080"

rem Go to project root
set "SCRIPT_DIR=%~dp0"
pushd "%SCRIPT_DIR%.." || (echo Failed to change directory & exit /b 1)

where node >nul 2>nul || (echo Node.js not found & exit /b 1)
where npm >nul 2>nul || (echo npm not found & exit /b 1)

pushd frontend || (echo frontend folder not found & exit /b 1)

if not exist node_modules (
  call npm install || (echo Failed to install dependencies & exit /b 1)
)

set "VITE_BACKEND_URL=%BACKEND_URL%"
echo Online on http://localhost:5173 (proxy -> %VITE_BACKEND_URL%)

call npm run dev

popd
popd

endlocal



