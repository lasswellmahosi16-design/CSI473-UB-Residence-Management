$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) { throw 'JDK 21 is required. Install it and reopen PowerShell.' }
New-Item -ItemType Directory -Force -Path 'build/classes' | Out-Null
$files = @(Get-ChildItem -Path 'src' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
& javac --release 21 -encoding UTF-8 -d build/classes @files
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
Write-Host 'Open http://localhost:8765 in your browser.'
& java -Ddormhub.webRoot=web -cp build/classes ub.dormhub.webapp.PrototypeServer
