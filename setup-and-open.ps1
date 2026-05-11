param(
    [string]$MavenPath,
    [switch]$SkipInstallOffers,
    [switch]$SkipDatabaseImport,
    [switch]$SkipRunApp,
    [switch]$SkipOpenIde
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([int]$Number, [string]$Message)
    Write-Host ""
    Write-Host "[$Number/8] $Message" -ForegroundColor Cyan
}

function Write-Ok {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Write-WarnLine {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-ErrorLine {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Confirm-Yes {
    param([string]$Question)
    $answer = Read-Host "$Question (y/N)"
    return $answer -match '^(y|yes|o|oui)$'
}

function Find-Executable {
    param([string]$CommandName, [string[]]$CandidatePaths)

    $command = Get-Command $CommandName -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    foreach ($candidate in $CandidatePaths) {
        if ($candidate.Contains("*")) {
            $resolved = Get-ChildItem -Path $candidate -ErrorAction SilentlyContinue | Select-Object -First 1
            if ($resolved) {
                return $resolved.FullName
            }
        } elseif (Test-Path $candidate) {
            return $candidate
        }
    }

    return $null
}

function Find-Winget {
    return (Get-Command winget -ErrorAction SilentlyContinue).Source
}

function Offer-WingetInstall {
    param([string]$PackageId, [string]$DisplayName)

    if ($SkipInstallOffers) {
        Write-WarnLine "$DisplayName is missing. Install skipped because -SkipInstallOffers was used."
        return
    }

    $winget = Find-Winget
    if (-not $winget) {
        Write-WarnLine "winget is not available. Please install $DisplayName manually."
        return
    }

    Write-WarnLine "$DisplayName is missing or not usable."
    Write-Host "winget package: $PackageId"
    Write-Host "You may need to rerun PowerShell as Administrator if installation fails."
    if (Confirm-Yes "Do you want to install $DisplayName using winget?") {
        & $winget install --id $PackageId --exact
        if ($LASTEXITCODE -ne 0) {
            Write-WarnLine "winget could not install $DisplayName. Please install it manually."
        }
    }
}

function Get-JavaMajorVersion {
    param([string]$JavaPath)

    $versionOutput = cmd /c "`"$JavaPath`" -version 2>&1" | Select-Object -First 1
    $versionText = [string]$versionOutput
    $match = [regex]::Match($versionText, '"(?<version>[^"]+)"')
    if (-not $match.Success) {
        return @{ Major = 0; Text = $versionText }
    }

    $version = $match.Groups["version"].Value
    if ($version.StartsWith("1.")) {
        $major = [int]($version.Split(".")[1])
    } else {
        $major = [int]($version.Split(".")[0])
    }
    return @{ Major = $major; Text = $versionText }
}

function Read-Properties {
    param([string]$Path)

    $properties = @{}
    foreach ($line in Get-Content $Path) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith("#")) {
            continue
        }
        $pair = $trimmed -split "=", 2
        if ($pair.Count -eq 2) {
            $properties[$pair[0].Trim()] = $pair[1].Trim()
        }
    }
    return $properties
}

function Parse-JdbcUrl {
    param([string]$JdbcUrl)

    $match = [regex]::Match($JdbcUrl, '^jdbc:mysql://(?<host>[^:/?]+)(:(?<port>\d+))?/(?<db>[^?]+)')
    if (-not $match.Success) {
        throw "Cannot parse db.url from db.properties: $JdbcUrl"
    }

    $port = 3306
    if ($match.Groups["port"].Success) {
        $port = [int]$match.Groups["port"].Value
    }

    return @{
        Host = $match.Groups["host"].Value
        Port = $port
        Database = $match.Groups["db"].Value
    }
}

function Build-MysqlArgs {
    param([hashtable]$DbSettings, [hashtable]$Properties)

    $args = @("--protocol=TCP", "-h", $DbSettings.Host, "-P", [string]$DbSettings.Port, "-u", $Properties["db.user"])
    if (-not [string]::IsNullOrEmpty($Properties["db.password"])) {
        $args += "--password=$($Properties["db.password"])"
    }
    return $args
}

function Test-MysqlConnection {
    param([string]$MysqlPath, [string[]]$MysqlArgs)

    & $MysqlPath @MysqlArgs -e "SELECT 1;" | Out-Null
    return $LASTEXITCODE -eq 0
}

function Open-ProjectIde {
    param([string]$ProjectRoot)

    $ideaCommand = Get-Command idea -ErrorAction SilentlyContinue
    if ($ideaCommand) {
        Start-Process -FilePath $ideaCommand.Source -ArgumentList "`"$ProjectRoot`""
        Write-Ok "Opened project in IntelliJ IDEA."
        return $true
    }

    $ideaPath = Find-Executable -CommandName "idea64.exe" -CandidatePaths @(
        "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition*\bin\idea64.exe",
        "C:\Program Files\JetBrains\IntelliJ IDEA*\bin\idea64.exe",
        "$env:LOCALAPPDATA\JetBrains\Toolbox\apps\IDEA-C\*\bin\idea64.exe",
        "$env:LOCALAPPDATA\JetBrains\Toolbox\apps\IDEA-U\*\bin\idea64.exe"
    )
    if ($ideaPath) {
        Start-Process -FilePath $ideaPath -ArgumentList "`"$ProjectRoot`""
        Write-Ok "Opened project in IntelliJ IDEA."
        return $true
    }

    if (-not $SkipInstallOffers) {
        $winget = Find-Winget
        if ($winget -and (Confirm-Yes "IntelliJ IDEA was not found. Install IntelliJ IDEA Community with winget?")) {
            & $winget install --id JetBrains.IntelliJIDEA.Community --exact
            $ideaCommand = Get-Command idea -ErrorAction SilentlyContinue
            if ($ideaCommand) {
                Start-Process -FilePath $ideaCommand.Source -ArgumentList "`"$ProjectRoot`""
                return $true
            }
        }
    }

    $codeCommand = Get-Command code -ErrorAction SilentlyContinue
    if ($codeCommand) {
        Start-Process -FilePath $codeCommand.Source -ArgumentList "`"$ProjectRoot`""
        Write-Ok "Opened project in VS Code."
        return $true
    }

    if (-not $SkipInstallOffers) {
        $winget = Find-Winget
        if ($winget -and (Confirm-Yes "VS Code was not found. Install VS Code with winget?")) {
            & $winget install --id Microsoft.VisualStudioCode --exact
            $codeCommand = Get-Command code -ErrorAction SilentlyContinue
            if ($codeCommand) {
                Start-Process -FilePath $codeCommand.Source -ArgumentList "`"$ProjectRoot`""
                return $true
            }
        }
    }

    Start-Process explorer.exe "`"$ProjectRoot`""
    Write-WarnLine "IDE was not found. Opened the project folder in Windows Explorer."
    return $false
}

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$schemaPath = Join-Path $projectRoot "src\main\resources\schema.sql"
$seedPath = Join-Path $projectRoot "src\main\resources\seed.sql"
$dbPropertiesPath = Join-Path $projectRoot "src\main\resources\db.properties"

Write-Step 1 "Checking Java"
$javaPath = Find-Executable -CommandName "java" -CandidatePaths @(
    "$env:USERPROFILE\.jdks\*\bin\java.exe",
    "C:\Program Files\Eclipse Adoptium\jdk-21*\bin\java.exe",
    "C:\Program Files\Eclipse Adoptium\jdk-17*\bin\java.exe",
    "C:\Program Files\Java\jdk-21*\bin\java.exe",
    "C:\Program Files\Java\jdk-17*\bin\java.exe",
    "C:\Program Files\JetBrains\IntelliJ IDEA*\jbr\bin\java.exe"
)
$javacPath = Find-Executable -CommandName "javac" -CandidatePaths @(
    "$env:USERPROFILE\.jdks\*\bin\javac.exe",
    "C:\Program Files\Eclipse Adoptium\jdk-21*\bin\javac.exe",
    "C:\Program Files\Eclipse Adoptium\jdk-17*\bin\javac.exe",
    "C:\Program Files\Java\jdk-21*\bin\javac.exe",
    "C:\Program Files\Java\jdk-17*\bin\javac.exe",
    "C:\Program Files\JetBrains\IntelliJ IDEA*\jbr\bin\javac.exe"
)

if (-not $javaPath -or -not $javacPath) {
    Offer-WingetInstall -PackageId "EclipseAdoptium.Temurin.17.JDK" -DisplayName "Eclipse Temurin JDK 17"
    $javaPath = Find-Executable -CommandName "java" -CandidatePaths @()
    $javacPath = Find-Executable -CommandName "javac" -CandidatePaths @()
}

if (-not $javaPath -or -not $javacPath) {
    throw "Java/JDK is required. Install JDK 17 or newer, then rerun this script."
}

$javaVersion = Get-JavaMajorVersion -JavaPath $javaPath
Write-Host $javaVersion.Text
if ($javaVersion.Major -lt 17) {
    Offer-WingetInstall -PackageId "EclipseAdoptium.Temurin.17.JDK" -DisplayName "Eclipse Temurin JDK 17"
    throw "Java 17 or higher is required. Current detected major version: $($javaVersion.Major)"
}
Write-Ok "Java and javac are available, version $($javaVersion.Major)+."

Write-Step 2 "Checking Maven"
$mvnPath = $MavenPath
if (-not $mvnPath) {
    $mvnPath = Find-Executable -CommandName "mvn" -CandidatePaths @(
        "C:\Program Files\Apache\maven\bin\mvn.cmd",
        "C:\Program Files\Maven\bin\mvn.cmd",
        "$env:USERPROFILE\scoop\apps\maven\current\bin\mvn.cmd"
    )
}
if (-not $mvnPath) {
    Offer-WingetInstall -PackageId "Apache.Maven" -DisplayName "Apache Maven"
    $mvnPath = Find-Executable -CommandName "mvn" -CandidatePaths @()
}
if (-not $mvnPath) {
    throw "Maven is required for compile/run. Install Maven manually or rerun with -MavenPath C:\path\to\mvn.cmd."
}
& $mvnPath -version | Select-Object -First 1
Write-Ok "Maven is available."

Write-Step 3 "Checking MySQL"
$mysqlPath = Find-Executable -CommandName "mysql" -CandidatePaths @(
    "C:\xampp\mysql\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MariaDB 11.4\bin\mysql.exe",
    "C:\Program Files\MariaDB 11.3\bin\mysql.exe"
)
if ($mysqlPath) {
    Write-Ok "mysql client found: $mysqlPath"
} else {
    Write-WarnLine "mysql client was not found."
    Write-Host "Supported options: XAMPP MySQL/MariaDB, WAMP, standalone MySQL, or standalone MariaDB."
    if (Test-Path "C:\xampp\xampp-control.exe") {
        Write-WarnLine "XAMPP is detected. Start MySQL from the XAMPP Control Panel."
    }
    if (-not $SkipInstallOffers) {
        $winget = Find-Winget
        if ($winget -and (Confirm-Yes "Install MySQL Server with winget? This may need administrator/configuration steps.")) {
            & $winget install --id Oracle.MySQL --exact
        }
    }
}

if ($mysqlPath -and (Test-Path $dbPropertiesPath)) {
    try {
        $properties = Read-Properties -Path $dbPropertiesPath
        $dbSettings = Parse-JdbcUrl -JdbcUrl $properties["db.url"]
        $mysqlArgs = Build-MysqlArgs -DbSettings $dbSettings -Properties $properties
        if (Test-MysqlConnection -MysqlPath $mysqlPath -MysqlArgs $mysqlArgs) {
            Write-Ok "Local MySQL/MariaDB server is reachable."
        } else {
            Write-WarnLine "mysql client exists, but the local database server did not respond."
        }
    } catch {
        Write-WarnLine "Could not test database connection: $($_.Exception.Message)"
    }
}

Write-Step 4 "Checking project files"
$requiredFiles = @(
    "pom.xml",
    "src\main\resources\schema.sql",
    "src\main\resources\seed.sql",
    "src\main\resources\db.properties",
    "src\main\java\com\library\Main.java",
    "src\main\java\com\library\ui\ConsoleMenu.java",
    "src\main\java\com\library\service\LibraryService.java",
    "src\main\java\com\library\database\LibraryRepository.java",
    "src\main\java\com\library\database\DatabaseConnection.java"
)
foreach ($file in $requiredFiles) {
    if (-not (Test-Path (Join-Path $projectRoot $file))) {
        throw "Missing required file: $file"
    }
}
Write-Ok "All required project and database files are present."

Write-Step 5 "Compiling project"
& $mvnPath clean compile
if ($LASTEXITCODE -ne 0) {
    throw "mvn clean compile failed. Fix the compile error above before continuing."
}
Write-Ok "Project compiled successfully."

Write-Step 6 "Optional database import"
if ($SkipDatabaseImport) {
    Write-WarnLine "Database import skipped."
} elseif (-not $mysqlPath) {
    Write-WarnLine "Cannot import automatically because mysql client is missing."
    Write-Host "Manual import:"
    Write-Host "mysql -u root < src/main/resources/schema.sql"
    Write-Host "mysql -u root < src/main/resources/seed.sql"
} elseif (Confirm-Yes "Do you want to import/reset the demo database using schema.sql and seed.sql? This may reset demo tables.") {
    $properties = Read-Properties -Path $dbPropertiesPath
    $dbSettings = Parse-JdbcUrl -JdbcUrl $properties["db.url"]
    $mysqlArgs = Build-MysqlArgs -DbSettings $dbSettings -Properties $properties
    try {
        Get-Content -Raw $schemaPath | & $mysqlPath @mysqlArgs
        if ($LASTEXITCODE -ne 0) { throw "schema.sql import failed." }
        Get-Content -Raw $seedPath | & $mysqlPath @mysqlArgs
        if ($LASTEXITCODE -ne 0) { throw "seed.sql import failed." }
        Write-Ok "schema.sql and seed.sql imported successfully."
    } catch {
        Write-WarnLine "Automatic import failed: $($_.Exception.Message)"
        Write-Host "Manual import:"
        Write-Host "$mysqlPath --protocol=TCP -h $($dbSettings.Host) -P $($dbSettings.Port) -u $($properties["db.user"]) < src/main/resources/schema.sql"
        Write-Host "$mysqlPath --protocol=TCP -h $($dbSettings.Host) -P $($dbSettings.Port) -u $($properties["db.user"]) < src/main/resources/seed.sql"
    }
} else {
    Write-WarnLine "Database import skipped by user."
}

Write-Step 7 "Optional app run"
if ($SkipRunApp) {
    Write-WarnLine "App run skipped."
} elseif (Confirm-Yes "Do you want to run the console application now?") {
    & $mvnPath exec:java "-Dexec.mainClass=com.library.Main"
}

Write-Step 8 "Opening IDE"
if ($SkipOpenIde) {
    Write-WarnLine "IDE opening skipped."
} else {
    Open-ProjectIde -ProjectRoot $projectRoot | Out-Null
}

Write-Host ""
Write-Ok "Setup script finished."
