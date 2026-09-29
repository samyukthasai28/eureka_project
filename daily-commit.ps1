# Daily Auto-Commit Script for eureka_project
# Runs at 11:00 PM every day via Windows Task Scheduler

$projectPath = "C:\Users\Pranav\eureka_project"
$logFile = "C:\Users\Pranav\eureka_project\daily-commit.log"
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

function Write-Log {
    param($message)
    $logEntry = "[$timestamp] $message"
    Add-Content -Path $logFile -Value $logEntry
    Write-Host $logEntry
}

Write-Log "=== Daily Auto-Commit Started ==="

# Check if the project directory exists
if (-not (Test-Path $projectPath)) {
    Write-Log "ERROR: Project path not found: $projectPath"
    exit 1
}

# Move into the repo
Set-Location $projectPath

# Check for any changes (tracked modifications, untracked files)
$status = git status --porcelain 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Log "ERROR: git status failed. Is this a git repo? $status"
    exit 1
}

if ([string]::IsNullOrWhiteSpace($status)) {
    Write-Log "Nothing to commit — working tree is clean. Skipping."
    exit 0
}

# Stage all changes
git add . 2>&1 | ForEach-Object { Write-Log "git add: $_" }

# Build a commit message with today's date
$today = Get-Date -Format "yyyy-MM-dd"
$commitMessage = "chore(daily): automated daily commit [$today]"

# Commit
$commitOutput = git commit -m $commitMessage 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Log "SUCCESS: $commitOutput"
} else {
    Write-Log "ERROR: Commit failed — $commitOutput"
    exit 1
}

Write-Log "=== Daily Auto-Commit Finished ==="
