param(
    [ValidateSet('run', 'test', 'package')]
    [string]$Acao = 'run'
)

$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    # Usa as ferramentas portáteis locais quando disponíveis.
    $jdkLocal = Get-ChildItem -LiteralPath "$PSScriptRoot/.tools" -Directory -Filter 'jdk-*' -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($jdkLocal) { $env:JAVA_HOME = $jdkLocal.FullName }
    $mavenLocal = Get-ChildItem -LiteralPath "$PSScriptRoot/.tools" -Directory -Filter 'apache-maven-*' -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($mavenLocal) {
        $maven = Join-Path $mavenLocal.FullName 'bin/mvn.cmd'
    } else {
        $comando = Get-Command mvn -ErrorAction SilentlyContinue
        if (-not $comando) { throw 'Instale Maven 3.6.3+ e JDK 17+ e configure o PATH/JAVA_HOME. Consulte README.md.' }
        $maven = $comando.Source
    }
    switch ($Acao) {
        'run' { & $maven spring-boot:run }
        'test' { & $maven clean verify }
        'package' { & $maven clean package }
    }
    if ($LASTEXITCODE -ne 0) { throw "Maven terminou com código $LASTEXITCODE" }
} finally {
    Pop-Location
}
