# 项目目录结构

```
school-system/
├── backend/                          # Spring Boot 后端
│   └── src/
│       ├── main/
│       │   ├── java/com/school/
│       │   │   ├── config/           # 配置类（Security、数据初始化、定时任务开关）
│       │   │   ├── common/           # 统一响应 Result、全局异常处理、用户状态常量
│       │   │   ├── security/         # JWT 认证与授权
│       │   │   ├── controller/       # REST API
│       │   │   │   ├── auth/         # 登录 / 改密 / 退出
│       │   │   │   ├── admin/        # 管理端（用户、日志、学籍）
│       │   │   │   └── MessageController.java   # 消息接口
│       │   │   ├── job/              # 定时任务（操作日志归档）
│       │   │   ├── service/          # 业务逻辑（+ impl/）
│       │   │   ├── repository/       # 数据访问层
│       │   │   ├── entity/           # JPA 实体
│       │   │   └── dto/              # 数据传输对象
│       │   └── resources/
│       │       ├── application.yml   # 应用配置（密码策略、日志归档等）
│       │       └── db/               # SQL 脚本（建表、种子数据）
│       └── test/                     # 单元测试
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── api/                      # 接口封装（auth / admin / log / message / academic）
│       ├── assets/                   # 静态资源
│       ├── components/
│       │   ├── common/               # 公共组件（欢迎页、消息角标）
│       │   └── message/              # 消息组件（新建会话）
│       ├── router/                   # 路由配置
│       ├── store/                    # Pinia 状态管理（user / message）
│       ├── utils/                    # 工具函数（auth / role / idle）
│       └── views/
│           ├── admin/                # 管理端（学籍、操作日志、重置密码）
│           ├── auth/                 # 登录 / 设置密码 / 修改密码
│           ├── message/              # 消息中心
│           ├── principal/            # 校长工作台
│           ├── grade-admin/          # 年级管理员工作台
│           ├── teacher/              # 教师工作台
│           └── student/              # 学生中心
└── docs/                             # 项目文档
    ├── database/                     # 数据库设计（权限、学籍、消息、日志、考试、成绩）
    └── api/                          # API 接口文档
```

## 分层说明

- **controller**：接收请求、参数校验，不包含业务逻辑
- **service**：业务逻辑，通过 repository 访问数据
- **repository**：Spring Data JPA 数据访问层
- **dto**：接口入参/出参对象，避免直接暴露实体
- **entity**：JPA 实体，与数据库表对应
- **job**：定时任务，如操作日志按天归档

## 模块划分

| 模块 | 后端 | 前端 |
|------|------|------|
| 认证 | `controller/auth`、`security` | `views/auth`、`store/user` |
| 学籍 | `AcademicService`、`controller/admin/AcademicController` | `views/admin/AcademicManagement.vue` |
| 消息 | `MessageService`、`MessageController` | `views/message`、`components/message`、`store/message` |
| 操作日志 | `OperationLogService`、`LogArchiveService`、`job` | `views/admin/OperationLogs.vue` |
| 用户与密码 | `AdminUserService`、`AuthService` | `views/admin/ResetUserPassword.vue` |
