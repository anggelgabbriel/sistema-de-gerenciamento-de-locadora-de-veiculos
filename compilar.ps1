$ErrorActionPreference = 'Stop'

function Find-JavaCompiler {
    if ($env:JAVA_HOME) {
        $fromHome = Join-Path $env:JAVA_HOME 'bin\javac.exe'
        if (Test-Path -LiteralPath $fromHome) { return $fromHome }
    }
    $fromPath = Get-Command javac -ErrorAction SilentlyContinue
    if ($fromPath) { return $fromPath.Source }

    $extensions = Join-Path $env:USERPROFILE '.vscode\extensions'
    if (Test-Path -LiteralPath $extensions) {
        $bundled = Get-ChildItem -LiteralPath $extensions -Directory -Filter 'redhat.java-*' |
            Sort-Object LastWriteTime -Descending | ForEach-Object {
                Get-ChildItem -Path (Join-Path $_.FullName 'jre\*\bin\javac.exe') -ErrorAction SilentlyContinue
            } | Select-Object -First 1
        if ($bundled) { return $bundled.FullName }
    }
    throw 'JDK nao encontrado. Configure JAVA_HOME ou coloque javac no PATH.'
}

$javac = Find-JavaCompiler
$jar = Join-Path (Split-Path $javac) 'jar.exe'
$saida = Join-Path $PSScriptRoot 'build\classes'
$destino = Join-Path $PSScriptRoot 'Vertice.jar'
$fontes = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'src') -Recurse -Filter '*.java' |
    ForEach-Object { $_.FullName })
New-Item -ItemType Directory -Force -Path $saida | Out-Null

$versao = (& $javac -version 2>&1 | Out-String)
if ($versao -match 'javac 1\.8') {
    & $javac -source 8 -target 8 -encoding UTF-8 -d $saida @fontes
} else {
    & $javac --release 8 -encoding UTF-8 -d $saida @fontes
}
if ($LASTEXITCODE -ne 0) { throw 'A compilacao falhou.' }
& $jar cfe $destino locadora.Main -C $saida locadora
if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel gerar o JAR.' }
Write-Host "JAR gerado: $destino"
