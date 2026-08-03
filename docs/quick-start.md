# 快速开始

> 快速启动校务系统。完整手册（环境概览、数据库初始化、测试账号、停止方式、注意事项等）见 `startWeb.md`。

## 环境要求

JDK 17+ · Maven 3.9+ · Node.js 18+ · PostgreSQL 17（已启动并创建 `school_system` 库）

## 1. 启动后端（端口 8080）

```powershell
cd C:\dev\school-system\backend
$env:DB_USERNAME = 'ai'
$env:DB_PASSWORD = '<数据库密码，见 startWeb.md>'
mvn spring-boot:run
```

## 2. 启动前端（端口 5173）

```powershell
cd C:\dev\school-system\frontend
npm install        # 仅首次或依赖变更时
npm run dev
```

## 3. 访问与测试

- 打开 http://localhost:5173/ ，使用 `admin / 123456` 登录
- 更多角色测试账号见 `startWeb.md` 第六节 / `docs/database/testdata.md`

## 4. 停止服务

- 在启动终端按 `Ctrl + C`；或按端口强制终止（命令见 `startWeb.md` 第七节）
