# 测试数据说明

> 本文档记录校务系统测试账号（5 个角色 × 2 个用户 = 10 个）。
> 对应脚本：`docs/database/testdataAdd.sql`（添加）、`docs/database/testdataClear.sql`（清理）。

## 账号总览

所有测试用户**密码统一为 `123456`**，`status = 1`（正常），均通过 `sys_user_role` 关联了对应角色。
登录名即 `username`（同时也写入了 `login_no`，二者一致，均可用于登录）。

| 角色 | 登录名 | 真实姓名（real_name） |
|------|--------|----------------------|
| 系统管理员（ADMIN） | `admin-test1` | 张一管理员test |
| 系统管理员（ADMIN） | `admin-test2` | 张二管理员test |
| 校长（PRINCIPAL） | `principal-test1` | 李一校长test |
| 校长（PRINCIPAL） | `principal-test2` | 李二校长test |
| 年级管理员（GRADE_ADMIN） | `grade-admin-test1` | 王一年级管理员test |
| 年级管理员（GRADE_ADMIN） | `grade-admin-test2` | 王二年级管理员test |
| 教师（TEACHER） | `teacher-test1` | 赵一老师test |
| 教师（TEACHER） | `teacher-test2` | 赵二老师test |
| 学生（STUDENT） | `student-test1` | 孙一同学test |
| 学生（STUDENT） | `student-test2` | 孙二同学test |

## 快速登录测试

以学生为例：

| 登录名 | 密码 |
|--------|------|
| `student-test1` | `123456` |
| `student-test2` | `123456` |

登录成功后前端会按角色跳转到对应欢迎页，例如：

- `student-test1` → 学生中心，显示「欢迎孙一同学test同学」
- `teacher-test1` → 教师工作台，显示「欢迎赵一老师test老师」

## 脚本用法

```powershell
# 添加测试数据（幂等，可重复执行）
psql -U ai -h localhost -d school_system -f docs/database/testdataAdd.sql

# 清理测试数据（仅删除上述 10 个测试账号，不影响 admin 等真实数据）
psql -U ai -h localhost -d school_system -f docs/database/testdataClear.sql
```

## 密码说明

- 密码哈希由 `pgcrypto` 的 `crypt('123456', gen_salt('bf'))` 生成（`$2a$` 格式 BCrypt），Spring Security 可直接校验。
- 数据库中不存明文；如需修改密码，请用管理员功能或重新执行脚本。
