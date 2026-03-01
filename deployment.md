# AgendaSync Deployment Strategy: Option 1 - Direct JAR Execution

**Last Updated:** March 1, 2026  
**Deployment Type:** Direct JAR Execution with Process Manager  
**Target Audience:** Personal use with continuous operation on local/home server

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Pre-Deployment Preparation](#pre-deployment-preparation)
4. [Building the Application](#building-the-application)
5. [Configuration Setup](#configuration-setup)
6. [Deployment by Operating System](#deployment-by-operating-system)
7. [Process Management & Auto-Restart](#process-management--auto-restart)
8. [Monitoring & Health Checks](#monitoring--health-checks)
9. [Troubleshooting](#troubleshooting)
10. [Backup & Recovery](#backup--recovery)
11. [Maintenance & Updates](#maintenance--updates)

---

## Overview

**What is Option 1?**

Direct JAR execution means running the compiled Spring Boot application directly using the Java Virtual Machine (JVM), without containerization or cloud platforms. This approach:

- Provides maximum control and visibility
- Requires minimal setup and dependencies (only Java 15+)
- Allows direct filesystem access and customization
- Suitable for personal/home server deployments
- Requires manual process management for restarts

**Key Characteristics:**

| Aspect | Details |
|--------|---------|
| **Base Memory** | ~300 MB (idle) + event cache |
| **Recommended Heap** | `-Xmx512m` for personal use, `-Xmx1g` for multiple users |
| **Disk Space** | ~50 MB for JAR + logs (100-500 MB/month) |
| **Network Ports** | 8080 (main), 8888 (OAuth callback) |
| **Database** | None required (file-based state) |
| **Continuous Operation** | Requires systemd/launchd/Task Scheduler management |

---

## Prerequisites

### 1. Java Installation

**Requirement:** Java Development Kit (JDK) 15 or higher

**Verify Installation:**
```bash
java -version
javac -version

### Credentials
https://www.googleapis.com
https://caldav.icloud.com

port 8888 deployen

### Deployment
Maak een deployment map aan:
C:\agendasync

map structuur:
agendasync/
├── AgendaSync-1.0-SNAPSHOT.jar      # Compiled application
├── .env                              # Environment variables (secrets)
├── application.yml                   # Optional: custom config
├── logs/                             # Auto-created: log files
├── tokens/                           # Auto-created: OAuth tokens
└── sync-state.properties             # Auto-created: sync state on first run

## .env

#### Stel Google API Creds in

Stel webhook in:
- channel id
- channel token
- port 8888

#### Stel Apple Creds in

### maak een application.yml aan voor config

# Server Configuration
server:
  port: 8080
  servlet:
    context-path: /

# Spring Configuration
spring:
  application:
    name: AgendaSync
  profiles:
    active: local

# Logging Configuration
logging:
  level:
    root: INFO
    calendar: DEBUG
    syncengine: DEBUG
    sync: DEBUG
  file:
    name: logs/agendasyc.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

# AgendaSync Custom Configuration
agendasyc:
  sync:
    apple-poll-interval-minutes: 15
    sync-state-file: ./sync-state.properties
  webhook:
    google-channel-enabled: true
    validate-headers: true

# App uitvoeren:
### .env inladen:
@echo off
setlocal enabledelayedexpansion

REM Change to script directory
cd /d "%~dp0"

REM Load environment variables from .env
for /F "usebackq tokens=1,2 delims==" %%A in (".env") do (
    set "%%A=%%B"
)

REM Create required directories
if not exist logs mkdir logs
if not exist tokens mkdir tokens

REM Start application
java ^
  -Xmx512m ^
  -Xms256m ^
  -Dspring.config.location=file:./application.yml ^
  -Dspring.profiles.active=production ^
  -jar AgendaSync-1.0-SNAPSHOT.jar

REM Capture exit code
set EXIT_CODE=%ERRORLEVEL%
echo AgendaSync exited with code: %EXIT_CODE%
exit /b %EXIT_CODE%

## Task Scheduler
#### maak batch file
C:/agendasync/start_agendasync.bat

`@echo off
setlocal enabledelayedexpansion

REM Navigate to application directory
cd /d "C:\agendasync"

REM Load .env file
for /F "usebackq tokens=1,2 delims==" %%A in (".env") do (
    set "%%A=%%B"
)

REM Log startup
echo [%DATE% %TIME%] Starting AgendaSync >> logs\startup.log

REM Create directories if they don't exist
if not exist logs mkdir logs
if not exist tokens mkdir tokens

REM Start application
java -Xmx512m ^
  -Dspring.config.location=file:./application.yml ^
  -Dspring.profiles.active=production ^
  -jar AgendaSync-1.0-SNAPSHOT.jar

REM Log exit
echo [%DATE% %TIME%] AgendaSync exited with code %ERRORLEVEL% >> logs\startup.log`


### Ga naar Task Scheduler -> Win + R -> taskschd.msc

Right click -> Create Task
Steps:

Right-click "Task Scheduler Library" → Create Task

General tab:

Name: AgendaSync
Description: Synchronize events between Apple Calendar and Google Calendar
Check: "Run with highest privileges"
Check: "Run whether user is logged in or not"
OS: Windows 10 or later
Triggers tab:

New trigger:
Begin the task: "At log on"
OR "At startup"
Check: "Repeat task every: 5 minutes"
Duration: "Indefinitely"
Actions tab:

New action:
Action: "Start a program"
Program/script: C:\agendasync\start_agendasync.bat
Start in: C:\agendasync\
Conditions tab:

Check: "Wake the computer to run this task"
Settings tab:

Check: "If the task fails, restart every: 1 minute"
Retry: "3 times"
Check: "If the task is still running, then the following rule applies: Stop the existing instance"


### Create Health Check script
agendasync/health-check.ps1

param(
    [string]$AppUrl = "http://localhost:8080/api/health/ping",
    [string]$LogFile = "C:\agendasync\logs\health-check.log",
    [int]$MaxRetries = 3,
    [int]$RetryDelayMS = 5000
)

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

Add-Content $LogFile "[$timestamp] Starting health check..."

for ($i = 1; $i -le $MaxRetries; $i++) {
    try {
        $response = Invoke-WebRequest -Uri $AppUrl -UseBasicParsing -TimeoutSec 5
        
        if ($response.StatusCode -eq 200) {
            Add-Content $LogFile "[$timestamp] Health check passed (HTTP 200)"
            Exit 0
        }
    } catch {
        Add-Content $LogFile "[$timestamp] Health check attempt $i failed: $($_.Exception.Message)"
        
        if ($i -lt $MaxRetries) {
            Start-Sleep -Milliseconds $RetryDelayMS
        }
    }
}

Add-Content $LogFile "[$timestamp] Health check failed. Restarting service..."
Restart-Service -Name "AgendaSync" -Force

Schedule in Task Scheduler:

Create new task
Trigger: Repeat every 5 minutes
Action: powershell.exe -ExecutionPolicy Bypass -File C:\agendasync\health-check.ps1


## Checklist

I'll create a comprehensive deployment guide for Option 1. Since I don't have direct file creation tools available, I'll present the complete markdown content that you can copy into a file named DEPLOYMENT_OPTION1.md in your repository root.

If not installed:

Linux (Ubuntu/Debian):

macOS:

Windows:

Download from Eclipse Adoptium or Oracle
Run installer and add to PATH
Verify in PowerShell: java -version
2. Network Connectivity
Verify outbound access to:

https://www.googleapis.com (Google Calendar API)
https://caldav.icloud.com (Apple CalDAV server)
Inbound access on ports 8080 and 8888 (for webhooks/OAuth callback)
Test connectivity:

3. Available Ports
Ensure ports 8080 and 8888 are not in use:

Linux/Mac:

Windows (PowerShell):

If ports are in use, either:

Stop the blocking process
Modify application.yml to use different ports (8080 → 8090, 8888 → 8889)
4. File System Permissions
Ensure your deployment directory allows read/write access:

Pre-Deployment Preparation
1. Create Deployment Directory
Choose a persistent location for your deployment:

Linux/Mac: ~/agendasync or /opt/agendasync (for system-level)
Windows: C:\agendasync or %APPDATA%\agendasync
Create structure:

2. Prepare Credentials
Google Calendar API Credentials
Steps:

Go to Google Cloud Console
Create new project or use existing one
Enable APIs:
Search "Google Calendar API" → Enable
Create OAuth2 credentials:
Menu → APIs & Services → Credentials
Click "Create Credentials" → OAuth 2.0 Client ID
Choose "Desktop application"
Download JSON → Save as credentials.json
Add authorized redirect URIs:
URI 1: http://localhost:8888/
URI 2: urn:ietf:wg:oauth:2.0:oob (for fallback)
Place credentials.json:

The JAR contains credentials.json by default. For custom location:

Extract JAR: jar xf AgendaSync-1.0-SNAPSHOT.jar
Replace BOOT-INF/classes/credentials.json
Re-package: jar cf AgendaSync-1.0-SNAPSHOT.jar .
Or copy to deployment directory and modify application.yml (see Configuration Setup below).

Apple Credentials
Required Information:

Apple ID Email (e.g., john.doe@icloud.com)

CalDAV username
App-Specific Password

NOT your regular Apple password
Generate at appleid.apple.com
Security → App-specific passwords → Generate
Use 16-character password provided
CalDAV Server URL

Standard: https://caldav.icloud.com
Or your Apple calendar server endpoint
3. Create .env File
Create .env file in deployment directory:

File: agendasync/.env

Protect .env file (contains secrets):

4. Create Optional application.yml Override
If you need to customize configurations, create application.yml in deployment directory:

File: agendasync/application.yml

Building the Application
Build from Source
Prerequisite: Gradle wrapper is included in repository

Steps:

Output:

JAR file: ~/agendasync/AgendaSync-1.0-SNAPSHOT.jar (~54 MB)
Built with embedded Tomcat and all dependencies
Verify Build:

Using Pre-Built JAR
If JAR already exists in libs:

Configuration Setup
1. Load Environment Variables
Your shell must load .env before starting the application.

Option A: Automatic Loading (Recommended)
Create start.sh (Linux/Mac) or start.bat (Windows):

File: agendasync/start.sh (Linux/Mac)

File: agendasync/start.bat (Windows)

Make executable:

Option B: Manual Environment Export
Linux/Mac:

Windows (PowerShell):

2. Memory Configuration
JVM memory is set in startup scripts. Adjust based on usage:

Scenario	Heap Size	Command
Personal use (< 100 events)	512 MB	-Xmx512m
Multiple calendars (100-1000 events)	1 GB	-Xmx1g
Heavy usage (1000+ events)	2 GB	-Xmx2g
Example with custom memory:

-Xmx1g = Maximum heap size (1 GB)
-Xms512m = Initial heap size (512 MB)
Deployment by Operating System
Linux (Ubuntu/Debian/RHEL)
Option A: Systemd Service (Recommended for continuous operation)
1. Create service user (optional, for security):

2. Create systemd service file:

File: /etc/systemd/system/agendasync.service

3. Enable and start service:

4. Service management commands:

Option B: Cron + Supervisor (Alternative)
If you prefer lighter process management:

Install supervisor:

File: /etc/supervisor/conf.d/agendasync.conf

Start:

macOS
Using Launchd (Native macOS process manager)
1. Create launch agent:

File: ~/Library/LaunchAgents/com.agendasync.plist

Replace <YOUR_USERNAME> with your actual username.

2. Load the launch agent:

3. Management commands:

Windows
Option A: Task Scheduler (System-wide, Recommended)
1. Create batch file to capture environment:

File: C:\agendasync\start_agendasync.bat

2. Create Task Scheduler task:

Open Task Scheduler:

Press Win + R → Type taskschd.msc → Enter
Steps:

Right-click "Task Scheduler Library" → Create Task

General tab:

Name: AgendaSync
Description: Synchronize events between Apple Calendar and Google Calendar
Check: "Run with highest privileges"
Check: "Run whether user is logged in or not"
OS: Windows 10 or later
Triggers tab:

New trigger:
Begin the task: "At log on"
OR "At startup"
Check: "Repeat task every: 5 minutes"
Duration: "Indefinitely"
Actions tab:

New action:
Action: "Start a program"
Program/script: C:\agendasync\start_agendasync.bat
Start in: C:\agendasync\
Conditions tab:

Check: "Wake the computer to run this task"
Settings tab:

Check: "If the task fails, restart every: 1 minute"
Retry: "3 times"
Check: "If the task is still running, then the following rule applies: Stop the existing instance"
3. Manage task:

Option B: NSSM (Non-Sucking Service Manager)
For services that require more control:

1. Download NSSM: https://nssm.cc/download

2. Install service:

Process Management & Auto-Restart
Health Check Script
Create a monitoring script to ensure application stays running:

File: agendasync/health-check.sh (Linux/Mac)

Make executable and add to crontab:

File: agendasync/health-check.ps1 (Windows PowerShell)

Schedule in Task Scheduler:

Create new task
Trigger: Repeat every 5 minutes
Action: powershell.exe -ExecutionPolicy Bypass -File C:\agendasync\health-check.ps1
Monitoring & Health Checks
Manual Health Verification
Check if application is running:

Expected response:

Detailed Status Check
Get comprehensive status:

Response example:

Log Monitoring
Monitor real-time logs:

Linux (systemd):

Mac (launchd):

Windows (file-based):

All platforms (file-based):

What to Look For in Logs
Successful startup:

Successful sync:

Errors to watch for:

IllegalArgumentException: Missing required environment variable: APPLE_USR
FileNotFoundException: Resource not found: /credentials.json
ConnectException: Connection refused to caldav.icloud.com
401 Unauthorized on Google API
Troubleshooting
Application Won't Start
Symptom: Service starts but immediately stops

Diagnostic steps:

Check logs for errors:

Verify Java installation:

Verify environment variables are set:

Test manual startup:

Ports Already in Use
Symptom: Address already in use: :8080

Solution:

Find what's using the port:

Stop the conflicting service or choose different ports

Modify application.yml:

OAuth2 Authorization Hangs
Symptom: App starts but waits at OAuth authorization screen, no browser opens

Causes:

Port 8888 unreachable
Firewall blocking 8888
Display server issues (headless environment)
Solutions:

Configure Google OAuth manually:

Run on machine with GUI first time to authorize
Copy generated ./tokens/StoredCredential to production server
Use different port:

Modify GoogleCalendarService to use port 9999 instead of 8888
For headless servers:

Generate token locally, copy to server, skip interactive auth
Connection to Apple CalDAV Fails
Symptom: ConnectException or 401 Unauthorized from caldav.icloud.com

Verification checklist:

 Apple credentials are correct:

 Apple ID has CalDAV enabled:

Go to appleid.apple.com
Security → Confirm 2FA is on
iCloud → Calendar sharing is enabled
 Test connectivity:

 Network firewall allows HTTPS outbound to caldav.icloud.com

 Check logs for actual error message:

High Memory Usage
Symptom: Java process consuming > 1 GB memory

Possible causes:

Event cache growing indefinitely
Memory leak in calendar service
Too many events being processed
Mitigation:

Reduce heap size initially, then monitor:

Monitor memory in real-time:

Restart service if memory exceeds threshold:

Sync Not Running (No Log Activity)
Symptom: App is running but no sync logs appear every 15 minutes

Check:

Verify scheduler is enabled (should see in startup logs):

Check health status:

If timestamp is stuck, check for stuck processes:

Increase logging verbosity in application.yml:

Backup & Recovery
What to Backup
Critical files:

.env - Contains credentials (encrypted/secured)
./tokens/StoredCredential - OAuth tokens (sensitive)
./sync-state.properties - Sync state (recoverable but useful)
application.yml - Custom configuration (if modified)
Non-critical (recoverable):

logs - Can be regenerated
AgendaSync-1.0-SNAPSHOT.jar - Can be rebuilt
Backup Procedure
Linux/Mac:

Schedule daily backup:

Windows PowerShell:

Recovery Procedure
If tokens are lost:

Delete ./tokens/StoredCredential
Restart application
Authorize new OAuth2 flow when prompted (requires browser access)
If .env is lost:

Recreate from backup or secure password manager
Restart application
If entire deployment directory is lost:

Extract backup: tar -xzf agendasync_backup_TIMESTAMP.tar.gz
Verify .env is present and valid
Restart systemd service or application
Maintenance & Updates
Regular Maintenance Tasks
Weekly:

Check logs for errors: grep -i error logs/agendasyc.log
Verify sync is running: curl http://localhost:8080/api/health/status
Monitor disk space: df -h ~/agendasync
Monthly:

Backup configuration and tokens
Check Google API quota usage:
https://console.cloud.google.com/apis/api/calendar/quotas
Rotate old logs if they exceed 500 MB
Quarterly:

Check for Java updates
Review logs for patterns/issues
Test backup/recovery procedure
Updating to New Version
1. Build new version:

2. Backup current deployment:

3. Replace JAR:

4. Restart service:

5. Verify:

Checklist: Deployment Verification
Pre-Deployment (Once)
 Java 15+ installed and in PATH
 Network connectivity to Google APIs and Apple CalDAV verified
 Google OAuth2 credentials obtained
 Apple ID and app-specific password generated
 Deployment directory created (agendasync/)
 .env file created with all required variables
 JAR built successfully (~54 MB)
Post-Deployment (After First Startup)
 Application starts without errors
 Health check returns HTTP 200
 Google OAuth2 authorization completed (first run only)
 Logs show successful Apple CalDAV connection
 First sync cycle completes (check logs for 15-minute mark)
 ./tokens/StoredCredential created (Google auth token)
 ./sync-state.properties created with timestamps
 Google webhook registration configured (optional)
Ongoing Verification
 Service restarts automatically after machine reboot
 Service restarts automatically if process dies
 Health check passes consistently
 Sync cycles run every 15 minutes (check logs)
 No errors in recent logs (tail -100 logs/agendasyc.log)
 Disk space not filling up (df -h)
 Memory usage stable (no gradual growth)
Before Production
 Tested backup/restore procedure
 Health check monitoring configured
 Log rotation working correctly
 Performance acceptable (CPU < 50%, Memory < 512 MB at idle)
 Applied IP firewall rules if applicable