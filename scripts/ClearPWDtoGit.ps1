<#
.SYNOPSIS
    ClearPWDtoGit：一键「清除敏感密码 + 提交 + 推送」GitHub。

.DESCRIPTION
    流程：
      1. 从本地 docs/aiconfig.md（已被 .gitignore 忽略）提取真实敏感值（数据库密码等）
      2. 扫描所有待提交文件，将文档类文件中的真实密码替换为占位符 <数据库密码>
      3. 校验待提交文件不再含任何敏感值（含则中止推送）
      4. git add -A && git commit && git push origin master

.PARAMETER CommitMessage
    可选的提交信息。留空时使用默认信息。

.PARAMETER DryRun
    仅执行扫描/替换/校验，不执行 git add/commit/push。用于安全检查。

.EXAMPLE
    .\scripts\ClearPWDtoGit.ps1
    .\scripts\ClearPWDtoGit.ps1 -CommitMessage "feat: 更新XX功能"
    .\scripts\ClearPWDtoGit.ps1 -DryRun
#>

param(
    [string]$CommitMessage = '',
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'

# ---- 定位项目根目录（脚本位于 <root>/scripts/ 下） ----
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if ((Split-Path -Leaf $scriptDir) -eq 'scripts') {
    $root = Split-Path -Parent $scriptDir
} else {
    $root = $scriptDir
}
Set-Location $root

Write-Host "=== ClearPWDtoGit ===" -ForegroundColor Cyan
Write-Host "项目根目录: $root"
if ($DryRun) { Write-Host "[DRY-RUN] 仅检查，不执行 git 提交/推送" -ForegroundColor Yellow }

# ---- 1. 从本地 aiconfig.md 提取敏感值 ----
$aiConfig = Join-Path $root 'docs\aiconfig.md'
$secrets = @()
if (Test-Path $aiConfig) {
    $cfg = Get-Content -Raw -Encoding UTF8 $aiConfig
    # 表格密码行：| 密码 | `xxx` |
    [regex]::Matches($cfg, '\|\s*密码\s*\|\s*`([^`]+)`') | ForEach-Object { $secrets += $_.Groups[1].Value }
    # 环境变量形式：DB_PASSWORD=xxx
    [regex]::Matches($cfg, 'DB_PASSWORD\s*=\s*(\S+)') | ForEach-Object { $secrets += $_.Groups[1].Value }
    $secrets = $secrets | Where-Object { $_ } | Select-Object -Unique
    Write-Host "从 docs/aiconfig.md 提取到敏感值: $($secrets -join ', ')" -ForegroundColor Green
} else {
    Write-Host "未找到 docs/aiconfig.md（本地敏感文件），跳过提取" -ForegroundColor Yellow
}

if ($secrets.Count -eq 0) {
    Write-Host "未提取到敏感值，跳过替换与泄漏校验。" -ForegroundColor Yellow
}

# ---- 2. 收集将提交的文件（已跟踪 + 未忽略的未跟踪文件） ----
$files = @()
$files += git ls-files
$files += git ls-files --others --exclude-standard
$files = $files | Where-Object { $_ -and (Test-Path $_) } | Select-Object -Unique
Write-Host "待检查文件数: $($files.Count)"

# ---- 3. 将文档中的敏感值替换为占位符 ----
$changed = @()
foreach ($f in $files) {
    if ($secrets.Count -eq 0) { continue }
    $content = Get-Content -Raw -Encoding UTF8 $f -ErrorAction SilentlyContinue
    if ($null -eq $content) { continue }
    $orig = $content
    foreach ($s in $secrets) {
        if ($content.Contains($s)) {
            if ($f -match '\.(md|yml|yaml|sql|txt|properties)$') {
                $content = $content -replace [regex]::Escape($s), '<数据库密码>'
            } else {
                Write-Host "!! 非文档文件疑似含敏感值: $f" -ForegroundColor Red
            }
        }
    }
    if ($content -ne $orig) {
        Set-Content -Path $f -Value $content -Encoding UTF8 -NoNewline
        $changed += $f
    }
}
if ($changed.Count -gt 0) {
    Write-Host "已替换敏感信息: $($changed -join ', ')" -ForegroundColor Green
} else {
    Write-Host "未发现需要替换的敏感信息（已是占位符或干净）" -ForegroundColor Green
}

# ---- 4. 最终校验：待提交文件不得再含任何敏感值 ----
$leaked = @()
foreach ($f in $files) {
    if ($secrets.Count -eq 0) { continue }
    $content = Get-Content -Raw -Encoding UTF8 $f -ErrorAction SilentlyContinue
    foreach ($s in $secrets) {
        if ($content -and $content.Contains($s)) {
            $leaked += "$f (含: $s)"
            break
        }
    }
}
if ($leaked.Count -gt 0) {
    Write-Host "!! 以下待提交文件仍含敏感信息，已中止：" -ForegroundColor Red
    $leaked | ForEach-Object { Write-Host "   - $_" -ForegroundColor Red }
    exit 1
}
Write-Host "敏感信息校验通过 ✓" -ForegroundColor Green

# ---- 5. git add / commit / push ----
if ($DryRun) {
    Write-Host "[DRY-RUN] 已跳过 git add/commit/push" -ForegroundColor Yellow
    Write-Host "=== 完成（DRY-RUN） ===" -ForegroundColor Cyan
    exit 0
}

git add -A
if (-not $CommitMessage) {
    $CommitMessage = "chore: 自动提交（ClearPWDtoGit）"
}
git commit -m $CommitMessage
if ($LASTEXITCODE -ne 0) {
    Write-Host "commit 未执行（可能没有需要提交的改动）" -ForegroundColor Yellow
}
git push origin master
if ($LASTEXITCODE -ne 0) {
    Write-Host "!! git push 失败" -ForegroundColor Red
    exit 1
}
Write-Host "=== 已推送到 origin/master ===" -ForegroundColor Green
