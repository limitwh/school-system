# 权限模型 ER 图

权限模型 8 张核心表的实体关系图（Mermaid）。

```mermaid
erDiagram
    SYS_USER ||--o{ SYS_USER_ROLE : "拥有"
    SYS_ROLE ||--o{ SYS_USER_ROLE : "被分配"
    SYS_USER_ROLE }o--|| GRADE : "年级管理员负责年级"
    GRADE ||--o{ CLASS : "包含"
    TEACHER ||--o{ TEACHER_CLASS : "任教"
    CLASS ||--o{ TEACHER_CLASS : "被任教"
    SYS_USER ||--o| TEACHER : "扩展"
    SYS_USER ||--o| STUDENT : "扩展"
    CLASS ||--o{ STUDENT : "包含"

    SYS_USER {
        bigint id PK
        varchar username "登录名"
        varchar login_no "学号/职工号"
        varchar password
        varchar real_name
        varchar phone "手机或座机"
        varchar email
        smallint status "0初始~8毕业"
        timestamp last_login_time
        int pwd_fail_count
        timestamp pwd_update_time
    }
    SYS_USER ||--o{ SYS_USER_SECURITY_ANSWER : "设置"
    SYS_SECURITY_QUESTION ||--o{ SYS_USER_SECURITY_ANSWER : "被回答"
    SYS_ROLE {
        bigint id PK
        varchar code "ADMIN/PRINCIPAL/GRADE_ADMIN/TEACHER/STUDENT"
        varchar data_scope "ALL/ALL_STAFF_STUDENT/GRADE/CLASS/SELF"
    }
    SYS_USER_ROLE {
        bigint id PK
        bigint user_id FK
        bigint role_id FK
        bigint grade_id FK "年级管理员的数据范围"
    }
    GRADE {
        bigint id PK
        varchar name "年级名称"
        varchar stage "PRIMARY/JUNIOR"
        int sort_order "1~9"
    }
    CLASS {
        bigint id PK
        varchar name "班级名称"
        bigint grade_id FK
        boolean is_graduated "是否毕业"
        timestamp graduate_time
    }
    STUDENT {
        bigint id PK
        bigint user_id FK
        varchar student_no "学号"
        bigint class_id FK
        int enroll_year "入学年份"
        timestamp graduate_time
    }
    SYS_SECURITY_QUESTION {
        bigint id PK
        varchar question "问题文本"
    }
    SYS_USER_SECURITY_ANSWER {
        bigint id PK
        bigint user_id FK
        bigint question_id FK
        varchar answer_hash "BCrypt 哈希"
    }
```

## 关系说明

- `sys_user` 与 `sys_user_role`：一对多，一个用户可拥有多个角色
- `sys_role` 与 `sys_user_role`：一对多，一个角色可被多个用户拥有
- `sys_user_role` 与 `grade`：年级管理员通过 `grade_id` 绑定负责年级
- `sys_user` 与 `teacher` / `student`：一对一业务扩展（工号 / 学号 / 班级）
- `teacher` 与 `class`：通过 `teacher_class` 多对多，决定教师的数据范围
- `sys_user` 与 `sys_user_security_answer`：一对多，一个用户可设置多个安全问题
- `sys_security_question` 与 `sys_user_security_answer`：一对多，同一问题可被多个用户作答

## 登录名规则

| 角色 | 登录名 | 说明 |
|------|--------|------|
| 学生 | `login_no`（学号） | 创建账号时系统自动生成，默认 = 学号 |
| 教师 / 年级管理员 | `login_no`（职工号） | 创建账号时系统自动生成，默认 = 职工号 |
| 系统管理员 / 校长 | `username` | 自定义登录名 |

- `login_no` 全局唯一（学号与职工号不会重复）
- 后端登录统一按「`username` 或 `login_no`」匹配，即输入学号/职工号或自定义用户名均可登录
- `teacher.teacher_no` / `student.student_no` 与 `sys_user.login_no` 保持一致（业务编号）

## 年级列表（一年级 ~ 初三）

| 学段 | 年级 | sort_order |
|------|------|-----------|
| 小学（PRIMARY） | 一年级 | 1 |
| 小学（PRIMARY） | 二年级 | 2 |
| 小学（PRIMARY） | 三年级 | 3 |
| 小学（PRIMARY） | 四年级 | 4 |
| 小学（PRIMARY） | 五年级 | 5 |
| 小学（PRIMARY） | 六年级 | 6 |
| 初中（JUNIOR） | 初一 | 7 |
| 初中（JUNIOR） | 初二 | 8 |
| 初中（JUNIOR） | 初三 | 9 |

> `stage` 字段便于按学段统计与管理；新增年级时只需插入新行，无需改表结构。

## 用户状态枚举

| 值 | 状态 | 适用对象 |
|----|------|---------|
| 0 | 初始（未设置密码） | 所有用户 |
| 1 | 正常 | 所有用户 |
| 2 | 请假 | 教师 / 学生 |
| 3 | 休学 | 学生 |
| 4 | 转出 | 学生 |
| 5 | 离职 | 教师 |
| 6 | 病假 | 教师 |
| 7 | 退休 | 教师 |
| 8 | 毕业 | 学生 |
| 9 | 锁定（密码连续错误） | 所有用户 |
| 10 | 密码过期（超过有效期） | 非 ADMIN 用户 |

> 后续新增状态时，只扩展枚举值即可，表结构不变。

## 相关数据模型

- 学籍（年级 / 班级 / 学生 / 学年 / 变更历史）：[`academic-flow.md`](academic-flow.md)
- 消息（会话 / 会话成员 / 消息）：[`message-module.md`](message-module.md)
- 操作日志（`sys_operation_log`）：见 [`schema.sql`](schema.sql)
