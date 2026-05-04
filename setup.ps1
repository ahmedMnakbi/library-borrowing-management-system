param(
    [string]$DbHost,
    [int]$DbPort,
    [string]$DbName,
    [string]$DbUser,
    [string]$DbPassword,
    [switch]$SkipDatabase,
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

function Write-Section {
    param([string]$Message)
    Write-Host ""
    Write-Host "=== $Message ===" -ForegroundColor Cyan
}

function Write-Ok {
    param([string]$Message)
    Write-Host "[OK] $Message" -ForegroundColor Green
}

function Write-WarnLine {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Gray
}

function Find-Executable {
    param(
        [string]$CommandName,
        [string[]]$CandidatePaths
    )

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

function Read-Properties {
    param([string]$Path)

    $properties = @{}
    if (-not (Test-Path $Path)) {
        return $properties
    }

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

function Write-DbProperties {
    param(
        [string]$Path,
        [string]$JdbcUrl,
        [string]$User,
        [string]$Password
    )

    $content = @(
        "db.url=$JdbcUrl"
        "db.user=$User"
        "db.password=$Password"
    )
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllLines($Path, $content, $utf8NoBom)
}

function Parse-JdbcUrl {
    param([string]$JdbcUrl)

    $pattern = '^jdbc:mysql://(?<host>[^:/?]+)(:(?<port>\d+))?/(?<db>[^?]+)'
    $match = [regex]::Match($JdbcUrl, $pattern)
    if (-not $match.Success) {
        throw "Unable to parse db.url value: $JdbcUrl"
    }

    $portValue = 3306
    if ($match.Groups["port"].Success) {
        $portValue = [int]$match.Groups["port"].Value
    }

    return @{
        Host = $match.Groups["host"].Value
        Port = $portValue
        Database = $match.Groups["db"].Value
    }
}

function Add-ReportLine {
    param(
        [System.Collections.Generic.List[string]]$Report,
        [string]$Line
    )
    $Report.Add($Line) | Out-Null
}

function Test-MysqlConnection {
    param(
        [string]$MysqlPath,
        [string[]]$MysqlArgs
    )

    & $MysqlPath @MysqlArgs -e "SELECT 1;" | Out-Null
    return ($LASTEXITCODE -eq 0)
}

function Try-StartXamppMySql {
    param([System.Collections.Generic.List[string]]$Report)

    $xamppStartScript = "C:\xampp\mysql_start.bat"
    if (-not (Test-Path $xamppStartScript)) {
        return $false
    }

    Write-Info "XAMPP detected. MySQL is not responding, so the setup script will try to start it."
    Add-ReportLine -Report $Report -Line "XAMPP MySQL start: ATTEMPTED"

    try {
        & $xamppStartScript | Out-Null
        Start-Sleep -Seconds 6
        Write-Ok "XAMPP MySQL start command executed"
        Add-ReportLine -Report $Report -Line "XAMPP MySQL start: COMMAND EXECUTED"
        return $true
    } catch {
        Write-WarnLine ("Could not start MySQL with XAMPP: " + $_.Exception.Message)
        Add-ReportLine -Report $Report -Line ("XAMPP MySQL start: FAILED - " + $_.Exception.Message)
        return $false
    }
}

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$dbPropertiesPath = Join-Path $projectRoot "src\main\resources\db.properties"
$schemaPath = Join-Path $projectRoot "src\main\resources\schema.sql"
$reportPath = Join-Path $projectRoot "setup-report.txt"

$report = [System.Collections.Generic.List[string]]::new()
Add-ReportLine -Report $report -Line ("Setup report generated on " + (Get-Date -Format "yyyy-MM-dd HH:mm:ss"))
Add-ReportLine -Report $report -Line ("Project root: " + $projectRoot)

Write-Section "Project files"
if (-not (Test-Path $dbPropertiesPath)) {
    throw "Missing file: $dbPropertiesPath"
}
if (-not (Test-Path $schemaPath)) {
    throw "Missing file: $schemaPath"
}
Write-Ok "Found db.properties and schema.sql"
Add-ReportLine -Report $report -Line "Project files: OK"

Write-Section "Java check"
$javaPath = Find-Executable -CommandName "java" -CandidatePaths @(
    "$env:USERPROFILE\.jdks\*\bin\java.exe",
    "C:\Program Files\Java\jdk-21\bin\java.exe",
    "C:\Program Files\Java\jdk-17\bin\java.exe",
    "C:\Program Files\Eclipse Adoptium\jdk-21*\bin\java.exe",
    "C:\Program Files\Eclipse Adoptium\jdk-17*\bin\java.exe",
    "C:\Program Files\JetBrains\IntelliJ IDEA*\jbr\bin\java.exe"
)

if ($javaPath) {
    $javaVersionOutput = cmd /c "`"$javaPath`" -version 2>&1" | Select-Object -First 1
    Write-Ok ("Java detected: " + $javaVersionOutput)
    Add-ReportLine -Report $report -Line ("Java: OK - " + $javaVersionOutput)
} else {
    Write-WarnLine "Java was not found. Install Java 17 or newer before running the app."
    Add-ReportLine -Report $report -Line "Java: MISSING"
}

Write-Section "Maven check"
$mavenPath = Find-Executable -CommandName "mvn" -CandidatePaths @(
    "C:\Program Files\Apache\maven\bin\mvn.cmd",
    "C:\Program Files\Maven\bin\mvn.cmd"
)

if ($mavenPath) {
    $mavenVersionOutput = cmd /c "`"$mavenPath`" -version 2>&1" | Select-Object -First 1
    Write-Ok ("Maven detected: " + $mavenVersionOutput)
    Add-ReportLine -Report $report -Line ("Maven: OK - " + $mavenVersionOutput)
} else {
    Write-WarnLine "Maven was not found. IntelliJ can still import the project using pom.xml."
    Add-ReportLine -Report $report -Line "Maven: NOT FOUND"
}

Write-Section "Database configuration"
$properties = Read-Properties -Path $dbPropertiesPath
$existingJdbcUrl = $properties["db.url"]
$existingDbUser = $properties["db.user"]
$existingDbPassword = $properties["db.password"]

if (-not $existingJdbcUrl) {
    $existingJdbcUrl = "jdbc:mysql://localhost:3306/library_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
}
if ($null -eq $existingDbUser -or $existingDbUser -eq "") {
    $existingDbUser = "root"
}
if ($null -eq $existingDbPassword) {
    $existingDbPassword = ""
}

$parsedJdbc = Parse-JdbcUrl -JdbcUrl $existingJdbcUrl

if ($DbHost) { $parsedJdbc["Host"] = $DbHost }
if ($DbPort) { $parsedJdbc["Port"] = $DbPort }
if ($DbName) { $parsedJdbc["Database"] = $DbName }
if ($DbUser) { $existingDbUser = $DbUser }
if ($PSBoundParameters.ContainsKey("DbPassword")) { $existingDbPassword = $DbPassword }

$jdbcUrl = "jdbc:mysql://{0}:{1}/{2}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" -f `
    $parsedJdbc["Host"], $parsedJdbc["Port"], $parsedJdbc["Database"]

Write-DbProperties -Path $dbPropertiesPath -JdbcUrl $jdbcUrl -User $existingDbUser -Password $existingDbPassword
Write-Ok ("db.properties updated: " + $jdbcUrl)
Add-ReportLine -Report $report -Line ("db.properties: " + $jdbcUrl)

Write-Section "MySQL / MariaDB check"
$mysqlPath = Find-Executable -CommandName "mysql" -CandidatePaths @(
    "C:\xampp\mysql\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
    "C:\Program Files\MariaDB 11.4\bin\mysql.exe",
    "C:\Program Files\MariaDB 11.3\bin\mysql.exe"
)

$databaseReady = $false

if ($SkipDatabase) {
    Write-Info "Database setup skipped because -SkipDatabase was used."
    Add-ReportLine -Report $report -Line "Database setup: SKIPPED"
} elseif (-not $mysqlPath) {
    Write-WarnLine "mysql client not found. Install MySQL/MariaDB or run schema.sql manually."
    Add-ReportLine -Report $report -Line "Database setup: mysql client not found"
} else {
    Write-Ok ("mysql client detected: " + $mysqlPath)
    $mysqlArgs = @(
        "--protocol=TCP",
        "-h", $parsedJdbc["Host"],
        "-P", [string]$parsedJdbc["Port"],
        "-u", $existingDbUser
    )
    if (-not [string]::IsNullOrEmpty($existingDbPassword)) {
        $mysqlArgs += "--password=$existingDbPassword"
    }

    try {
        $connectionOk = Test-MysqlConnection -MysqlPath $mysqlPath -MysqlArgs $mysqlArgs
        if (-not $connectionOk -and $mysqlPath.StartsWith("C:\xampp\", [System.StringComparison]::OrdinalIgnoreCase)) {
            $started = Try-StartXamppMySql -Report $report
            if ($started) {
                $connectionOk = Test-MysqlConnection -MysqlPath $mysqlPath -MysqlArgs $mysqlArgs
            }
        }

        if (-not $connectionOk) {
            throw "The MySQL/MariaDB server is not responding."
        }

        Write-Ok "Database connection succeeded"
        Add-ReportLine -Report $report -Line "Database connection: OK"

        $schemaContent = Get-Content -Raw $schemaPath
        $schemaContent | & $mysqlPath @mysqlArgs | Out-Null
        Write-Ok "schema.sql applied successfully"
        Add-ReportLine -Report $report -Line "schema.sql: APPLIED"

        $verifyOutput = & $mysqlPath @mysqlArgs -e ("SHOW TABLES FROM " + $parsedJdbc["Database"] + ";") 2>&1
        if ($LASTEXITCODE -eq 0) {
            $databaseReady = $true
            Write-Ok "Database tables are ready"
            Add-ReportLine -Report $report -Line "Database tables: READY"
        } else {
            Write-WarnLine "Could not verify tables after applying schema."
            Add-ReportLine -Report $report -Line "Database tables: NOT VERIFIED"
        }
    } catch {
        Write-WarnLine ("Database setup failed: " + $_.Exception.Message)
        Write-WarnLine "Check the database server status, credentials, and port in db.properties."
        Add-ReportLine -Report $report -Line ("Database setup: FAILED - " + $_.Exception.Message)
    }
}

Write-Section "Project build"
$buildReady = $false
if ($SkipBuild) {
    Write-Info "Build skipped because -SkipBuild was used."
    Add-ReportLine -Report $report -Line "Build: SKIPPED"
} elseif ($mavenPath) {
    try {
        & $mavenPath -q -DskipTests compile
        if ($LASTEXITCODE -eq 0) {
            $buildReady = $true
            Write-Ok "Project compiled successfully"
            Add-ReportLine -Report $report -Line "Build: OK"
        } else {
            Write-WarnLine "Maven compile finished with a non-zero exit code."
            Add-ReportLine -Report $report -Line "Build: FAILED"
        }
    } catch {
        Write-WarnLine ("Build failed: " + $_.Exception.Message)
        Add-ReportLine -Report $report -Line ("Build: FAILED - " + $_.Exception.Message)
    }
} else {
    Write-WarnLine "Build not executed because Maven is not available. Open the project in IntelliJ and import pom.xml."
    Add-ReportLine -Report $report -Line "Build: NOT RUN"
}

Write-Section "Summary"
if ($javaPath) {
    Write-Ok "Java is available"
} else {
    Write-WarnLine "Java still needs to be installed"
}

if ($databaseReady) {
    Write-Ok "Database is ready to use"
} else {
    Write-WarnLine "Database still needs manual verification or setup"
}

if ($buildReady) {
    Write-Ok "Project build is ready"
} else {
    Write-WarnLine "Project build was not completed automatically"
}

Write-Host ""
Write-Host "Next step to launch the app:" -ForegroundColor Cyan
Write-Host "1. Open the project in IntelliJ IDEA"
Write-Host "2. Run com.library.app.Main"
Write-Host "3. If there are no users yet, create the first admin account"

Add-ReportLine -Report $report -Line ""
Add-ReportLine -Report $report -Line "Next step: open the project in IntelliJ IDEA and run com.library.app.Main"
Set-Content -Path $reportPath -Value $report -Encoding UTF8
Write-Info ("A detailed report was written to " + $reportPath)
