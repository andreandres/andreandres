param(
    [ValidateSet('run', 'test', 'package')]
    [string]$Acao = 'run',
    [ValidateRange(1024, 65535)]
    [int]$Porta = 8080
)

$ErrorActionPreference = 'Stop'
$javaAnterior = $env:JAVA_HOME
$caminhosFerramentas = @(
    (Join-Path $PSScriptRoot '.tools'),
    (Join-Path $PSScriptRoot '../Trabalho 1/.tools'),
    (Join-Path $PSScriptRoot '../../Trabalho 1/.tools')
)
$maven = $null
foreach ($caminho in $caminhosFerramentas) {
    if (-not (Test-Path -LiteralPath $caminho)) { continue }
    $jdk = Get-ChildItem -LiteralPath $caminho -Directory -Filter 'jdk-*' | Select-Object -First 1
    $mavenLocal = Get-ChildItem -LiteralPath $caminho -Directory -Filter 'apache-maven-*' | Select-Object -First 1
    if ($jdk -and $mavenLocal) {
        $env:JAVA_HOME = $jdk.FullName
        $maven = Join-Path $mavenLocal.FullName 'bin/mvn.cmd'
        break
    }
}
if (-not $maven) {
    $comando = Get-Command mvn -ErrorAction SilentlyContinue
    if (-not $comando) { throw 'Instale JDK 17 ou 21 e Maven 3.6.3+, ou coloque ferramentas portateis em .tools. Veja README.md.' }
    $maven = $comando.Source
}

Push-Location (Join-Path $PSScriptRoot 'backend')
try {
    $argumentos = @('-Dmaven.repo.local=' + (Join-Path $env:USERPROFILE '.m2/repository'))
    switch ($Acao) {
        'run' { $argumentos += @('spring-boot:run', "-Dspring-boot.run.arguments=--server.port=$Porta") }
        'test' { $argumentos += @('clean', 'verify') }
        'package' { $argumentos += @('clean', 'package') }
    }
    # Maven pode escrever avisos em stderr mesmo quando a compilacao passa.
    # O resultado deve ser determinado pelo codigo de saida do processo.
    $preferenciaAnterior = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        & $maven @argumentos
        $codigoMaven = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $preferenciaAnterior
    }
    if ($codigoMaven -ne 0) { throw "Maven terminou com codigo $codigoMaven" }
} finally {
    Pop-Location
    $env:JAVA_HOME = $javaAnterior
}
