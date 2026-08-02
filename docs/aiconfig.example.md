# AI 数据库账户配置（模板）

> 本文件是模板，真实密码已脱敏。
> 使用方法：复制本文件为 `docs/aiconfig.md`（已被 .gitignore 忽略，不会上传），填入你的真实密码。

建立日期：2026-08-02

## 账户信息

| 项目 | 值 |
|------|-----|
| 数据库类型 | PostgreSQL 17（本机） |
| 账户名 | `ai` |
| 密码 | `YOUR_AI_DB_PASSWORD` |
| 数据库 | `school_system` |
| 权限 | 库所有者（可建表 / 改表 / 删表） |

## 连接方式

```bash
psql -U ai -h localhost -d school_system
```

> 提示：`psql` 不在系统 PATH 中，请使用完整路径：
> `C:\Program Files\PostgreSQL\17\bin\psql.exe`

## 建表命令

```bash
# 用 ai 账户执行完整建表脚本
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U ai -h localhost -d school_system -f docs/database/schema.sql
```

## 后端应用对接

`application.yml` 默认使用 `postgres/postgres`，可通过环境变量切换到 `ai` 账户：

```
DB_USERNAME=ai
DB_PASSWORD=YOUR_AI_DB_PASSWORD
```

## 超级用户（管理用，仅必要操作时使用）

| 项目 | 值 |
|------|-----|
| 账户名 | `postgres` |
| 密码 | `YOUR_POSTGRES_PASSWORD` |

> 超级用户密码仅用于数据库管理操作；日常建表/开发请使用 `ai` 账户。

## 备注

- `ai` 是 `school_system` 库的所有者，具备该库内完整建表权限（非全局超级用户）。
- 如需升级为全局超级用户（可管理所有库/账户），需额外执行授权操作。
- 真实密码请填入本地 `docs/aiconfig.md`，切勿提交到 Git 仓库。
