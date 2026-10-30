$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
New-Item -ItemType Directory -Force -Path 'build/classes' | Out-Null
$files = @(Get-ChildItem -Path 'src', 'tests' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
& javac --release 21 -encoding UTF-8 -d build/classes @files
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed.' }
& java -cp build/classes ub.dormhub.tests.VerificationServiceTest
if ($LASTEXITCODE -ne 0) { throw 'Domain tests failed.' }
if (Get-Command python -ErrorAction SilentlyContinue) {
    & python tests/api_smoke_test.py
    if ($LASTEXITCODE -ne 0) { throw 'HTTP smoke tests failed.' }
} else { Write-Host 'Python not found. HTTP smoke tests were skipped.' }
