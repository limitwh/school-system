# 校务管理系统 - 启动手册（startWeb）

> 手动启动前端 + 后端的完整步骤。适用于本机开发环境（Windows + PowerShell）。
> 快速上手（仅 3 步）见 `docs/quick-start.md`。
> 相关文档：测试账号见 `docs/database/testdata.md`，建表脚本见 `docs/database/schema.sql`。
> ⚠️ 数据库真实密码保存在本地 `docs/aiconfig.md`（已被 .gitignore，不会上传），下文统一用 `<数据库密码>` 占位。

## 一、环境概览

| 项 | 说明 |
|----|------|
| 后端 | Spring Boot（Java），端口 `8080` |
| 前端 | Vue 3 + Vite，端口 `5173`（代理 `/api` → `8080`） |
| 数据库 | PostgreSQL 17，库名 `school_system`（`localhost:5432`） |
| 前端访问地址 | http://localhost:5173/ |
| 后端 API 地址 | http://localhost:8080/api/ |

## 二、前置条件

- JDK 17+（后端编译目标 17）
- Maven 3.9+
- Node.js（含 npm）
- PostgreSQL 17 已启动，`school_system` 库已建好（20 张表）
- 首次运行后端会自动创建角色/初始管理员（`DataInitializer`）

### 数据库初始化（仅首次 / 重建时）

1. 创建数据库（PostgreSQL 管理员执行）：
   ```powershell
   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -c "CREATE DATABASE school_system;"
   ```
2. 执行建表脚本（20 张表 + 年级种子数据；表已存在会报错，仅首次执行）：
   ```powershell
   $env:PGPASSWORD = '<数据库密码>'   # 见本地 docs/aiconfig.md
   $env:PGCLIENTENCODING = 'UTF8'
   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U ai -h localhost -d school_system -f C:\dev\school-system\docs\database\schema.sql
   ```
3. 添加测试用户（可选，幂等可重复执行）：
   ```powershell
   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U ai -h localhost -d school_system -f C:\dev\school-system\docs\database\testdataAdd.sql
   ```
4. 首次运行后端会自动创建 5 个角色与初始管理员 `admin / 123456`（`DataInitializer`）。

## 三、启动后端

```powershell
cd C:\dev\school-system\backend

# 设置数据库连接环境变量（ai 账户；密码见本地 docs/aiconfig.md）
$env:DB_USERNAME = 'ai'
$env:DB_PASSWORD = '<数据库密码>'

# 启动（首次会下载依赖，较慢）
mvn spring-boot:run
```

- 看到 `Started SchoolApplication` 即启动成功，监听 `8080`。
- 若后端已占用 8080 端口，说明已在运行，无需重复启动。
- 可选：JWT 密钥可通过 `$env:JWT_SECRET = '...'` 覆盖默认值。

### 打包运行（可选，生产方式）

```powershell
cd C:\dev\school-system\backend
$env:DB_USERNAME = 'ai'
$env:DB_PASSWORD = '<数据库密码>'   # 见本地 docs/aiconfig.md
mvn clean package -DskipTests
java -jar target\school-system-backend-0.0.1-SNAPSHOT.jar
```

## 四、启动前端

```powershell
cd C:\dev\school-system\frontend
npm install          # 仅首次或依赖变更时需要
npm run dev
```

- 看到 `Local: http://localhost:5173/` 即启动成功。
- 前端通过 Vite 代理 `/api` 请求转发到后端 `8080`，需保证后端已启动。

## 五、验证是否联通

1. 后端：浏览器访问 http://localhost:8080/api/auth/me（未带 Token 会返回 401，属正常）
2. 前端：浏览器打开 http://localhost:5173/ ，应显示登录页
3. 登录一个测试账号，应跳转到对应角色欢迎页

PowerShell 一键联通测试（登录学生账号）：

```powershell
$body = @{ username='student-test1'; password='123456' } | ConvertTo-Json
Invoke-RestMethod -Uri http://localhost:5173/api/auth/login -Method Post -ContentType 'application/json' -Body $body
```

## 六、测试账号

所有测试用户密码统一 `123456`（详见 `docs/database/testdata.md`）：

| 角色 | 登录名 |
|------|--------|
| 系统管理员 | `admin`、`admin-test1`、`admin-test2` |
| 校长 | `principal-test1`、`principal-test2` |
| 年级管理员 | `grade-admin-test1`、`grade-admin-test2` |
| 教师 | `teacher-test1`、`teacher-test2` |
| 学生 | `student-test1`、`student-test2` |

## 七、停止

### 方式一：在启动终端按 Ctrl + C（推荐）

- 前端：在运行 `npm run dev` 的终端按 `Ctrl + C`
- 后端：在运行 `mvn spring-boot:run` 的终端按 `Ctrl + C`（Maven 会结束 Java 进程）

### 方式二：按端口强制终止进程

适用于忘记在哪个终端启动、或终端已被关闭的情况（PowerShell）：

```powershell
# 终止前端（Vite，5173 端口）
$pid5173 = (Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue).OwningProcess
if ($pid5173) { Stop-Process -Id $pid5173 -Force; Write-Host "前端已终止 (PID $pid5173)" } else { Write-Host "前端未在运行" }

# 终止后端（Spring Boot，8080 端口）
$pid8080 = (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue).OwningProcess
if ($pid8080) { Stop-Process -Id $pid8080 -Force; Write-Host "后端已终止 (PID $pid8080)" } else { Write-Host "后端未在运行" }
```

### 验证端口已释放

```powershell
$b = (Test-NetConnection -ComputerName localhost -Port 8080 -WarningAction SilentlyContinue).TcpTestSucceeded
$f = (Test-NetConnection -ComputerName localhost -Port 5173 -WarningAction SilentlyContinue).TcpTestSucceeded
Write-Host "后端8080在运行: $b  前端5173在运行: $f"   # 均为 False 即已全部停止
```

## 八、注意事项

- ⚠️ 数据库连接密码：真实密码保存在本地 `docs/aiconfig.md`（已被 .gitignore，不会上传），本文档使用占位符 `<数据库密码>`。
- 登录连续输错 4 次密码会锁定账号（`status=9`），解锁命令：
  ```powershell
  & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U ai -h localhost -d school_system `
    -c "UPDATE sys_user SET status=1, pwd_fail_count=0 WHERE username='admin';"
  ```
- 添加/清理测试数据：执行 `docs/database/testdataAdd.sql` / `docs/database/testdataClear.sql`。
