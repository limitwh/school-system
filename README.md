# 校务系统（School Affairs System）

前后端分离的学校校务管理系统。

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot + Spring Security + JWT + Spring Data JPA |
| 前端 | Vue 3 + Vite + Pinia + Vue Router + Element Plus + Axios |
| 数据库 | PostgreSQL 17 |
| 构建 | Maven 3.9（后端）/ npm（前端） |

## 角色体系（按权限从高到低）

| 角色 | 权限说明 | 数据范围 |
|------|---------|---------|
| **系统管理员（ADMIN）** | 权限最高，可读写所有信息 | 全部数据 |
| **校长（PRINCIPAL）** | 可读写所有教师、年级管理员和学生信息 | 所有教职工/学生（不含系统管理员） |
| **年级管理员（GRADE_ADMIN）** | 只能读写本年级的信息 | 本年级 |
| **教师（TEACHER）** | 只能读写本人任教班级学生的信息 | 本人任教班级 |
| **学生（STUDENT）** | 权限最低，只能查看自己的信息 | 仅本人 |

> **年级管理员也可以是教师**：一个用户可同时拥有多个角色（如 `GRADE_ADMIN` + `TEACHER`），权限取并集。

## 权限模型设计

采用「**功能权限（RBAC）+ 数据权限（数据范围）**」双层模型：功能权限控制能否访问某个接口，数据权限控制能操作哪些数据行（年级 / 班级 / 本人）。

### 角色数据范围

| 角色 code | data_scope | 数据范围含义 |
|-----------|-----------|-------------|
| `ADMIN` | `ALL` | 所有数据 |
| `PRINCIPAL` | `ALL_STAFF_STUDENT` | 所有教师、年级管理员、学生（不含系统管理员） |
| `GRADE_ADMIN` | `GRADE` | 仅负责的年级（由 `sys_user_role.grade_id` 指定） |
| `TEACHER` | `CLASS` | 仅本人任教班级的学生（由 `teacher_class` 决定） |
| `STUDENT` | `SELF` | 仅本人信息 |

### ER 图

> 权限模型实体关系图见：[`docs/database/er-diagram.md`](docs/database/er-diagram.md)

### 表结构（PostgreSQL 17）

> 完整的建表 SQL 脚本见：[`docs/database/schema.sql`](docs/database/schema.sql)

### 权限判断流程

> 以「查询学生列表」为例的权限判断流程见：[`docs/database/authorization-flow.md`](docs/database/authorization-flow.md)

### 接口功能权限（Spring Security）

高角色天然能访问低角色的接口：

| 接口前缀 | 允许角色 |
|---------|---------|
| `/api/admin/**` | ADMIN |
| `/api/principal/**` | ADMIN, PRINCIPAL |
| `/api/grade-admin/**` | ADMIN, PRINCIPAL, GRADE_ADMIN |
| `/api/teacher/**` | ADMIN, PRINCIPAL, GRADE_ADMIN, TEACHER |
| `/api/student/**` | 所有登录用户（只能查自己） |

> 功能权限用 `@PreAuthorize("hasRole('ADMIN')")` 控制；数据权限建议写一个 AOP 注解（如 `@DataScope`）在查询时自动拼接数据范围条件，避免每个 Service 手写过滤逻辑。

## 功能模块

| 模块 | 说明 | 设计文档 |
|------|------|---------|
| 权限管理 | 角色体系、数据范围、登录名规则 | [`docs/database/schema.sql`](docs/database/schema.sql) |
| 学籍管理 | 年级（小学6+初中3）、班级、学生档案、学年；批量入学、升年级、毕业 | [`docs/database/academic-flow.md`](docs/database/academic-flow.md) |
| 在线考试 | 题库、试卷、考试、阅卷 | [`docs/database/exam-module.md`](docs/database/exam-module.md) |
| 成绩单 | 成绩记录、排名、按角色数据权限 | [`docs/database/score-report.md`](docs/database/score-report.md) |
| 系统日志 | 操作审计：谁在什么时间做了什么操作（登录/登出/改密/重置密码等） | [`docs/database/schema.sql`](docs/database/schema.sql) |
| 消息 | 用户间 1:1 单聊、未读提醒、轮询新消息 | [`docs/database/message-module.md`](docs/database/message-module.md) |

## 会话与安全

- 密码连续输错 4 次锁定账号；除系统管理员（ADMIN）外，其他用户密码 **90 天**过期强制修改（`app.password.valid-days` 可配置）。
- 管理员重置的密码为一次性随机密码，24 小时内有效，首次登录强制改密。
- 登录后连续 **5 分钟无任何操作自动退出**（前端空闲检测，时长见 `frontend/src/utils/idle.js` 的 `IDLE_TIMEOUT_MS`）。
- 登录、退出、改密、重置密码等关键操作写入 `sys_operation_log`，管理员可在「操作日志」页查询。
- 操作日志默认保留 **30 天**，每天 **03:00** 自动归档为 JSONL 文件（`backend/logs/archive`）并从库中清理；管理员也可手动触发归档。
- **消息内容不写入操作日志**（`msg_message` 本身即记录），避免隐私扩散与日志膨胀。

## 项目结构

> 详细目录结构见：[`docs/project-structure.md`](docs/project-structure.md)

项目采用前后端分离架构：后端为 Spring Boot（controller / service / repository 分层），前端为 Vue 3（api / store / views 分层）。

## 快速开始（TODO）

> 完整启动步骤见：[`docs/quick-start.md`](docs/quick-start.md)
