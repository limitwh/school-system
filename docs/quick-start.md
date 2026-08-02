# 快速开始

> 前提：已安装 JDK 17+、Maven 3.9、Node.js 18+、PostgreSQL 17

## 后端

```bash
cd backend
mvn spring-boot:run
```

## 前端

```bash
cd frontend
npm install
npm run dev
```

## 数据库初始化

```bash
# 依次执行数据库脚本（需先创建数据库 school_system）
psql -U postgres -d school_system -f docs/database/schema.sql
```
