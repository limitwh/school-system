# 项目目录结构

```
school-system/
├── backend/                          # Spring Boot 后端
│   └── src/
│       ├── main/
│       │   ├── java/com/school/
│       │   │   ├── config/           # 配置类（CORS、JPA 等）
│       │   │   ├── common/           # 统一响应 Result、全局异常处理
│       │   │   ├── security/         # JWT 认证与授权
│       │   │   ├── controller/       # REST API
│       │   │   │   ├── admin/        # 管理端接口
│       │   │   │   ├── teacher/      # 教师端接口
│       │   │   │   └── student/      # 学生端接口
│       │   │   ├── service/          # 业务逻辑（+ impl/）
│       │   │   ├── repository/       # 数据访问层
│       │   │   ├── entity/           # JPA 实体
│       │   │   └── dto/              # 数据传输对象
│       │   └── resources/
│       │       ├── application.yml   # 应用配置
│       │       └── db/               # SQL 脚本（建表、种子数据）
│       └── test/                     # 单元测试
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── api/                      # 接口请求封装
│       ├── assets/                   # 静态资源
│       ├── components/common/        # 公共组件
│       ├── router/                   # 路由配置
│       ├── store/                    # Pinia 状态管理
│       ├── utils/                    # 工具函数
│       └── views/
│           ├── admin/                # 管理端页面
│           ├── teacher/              # 教师端页面
│           ├── student/              # 学生端页面
│           └── auth/                 # 登录/注册页面
└── docs/                             # 项目文档
    ├── database/                     # 数据库设计文档
    └── api/                          # API 接口文档
```

## 分层说明

- **controller**：接收请求、参数校验，不包含业务逻辑
- **service**：业务逻辑，通过 repository 访问数据
- **repository**：Spring Data JPA 数据访问层
- **dto**：接口入参/出参对象，避免直接暴露实体
- **entity**：JPA 实体，与数据库表对应
