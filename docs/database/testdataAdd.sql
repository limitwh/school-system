-- ============================================================
-- 校务系统 - 测试用户种子数据（可独立执行，幂等）
-- 说明：为 5 个角色各建立 2 个测试用户（共 10 个）
--       密码统一 123456（BCrypt 加密，Spring Security 可校验）
--       依赖 pgcrypto 扩展；重复执行不会报错（ON CONFLICT DO NOTHING）
-- 执行：psql -U ai -h localhost -d school_system -f docs/database/testdataAdd.sql
-- 清理：执行 docs/database/testdataClear.sql 可删除本脚本创建的数据
-- ============================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1) 插入用户
INSERT INTO sys_user (username, login_no, password, real_name, status) VALUES
    ('admin-test1',        'admin-test1',        crypt('123456', gen_salt('bf')), '张一管理员test',     1),
    ('admin-test2',        'admin-test2',        crypt('123456', gen_salt('bf')), '张二管理员test',     1),
    ('principal-test1',    'principal-test1',    crypt('123456', gen_salt('bf')), '李一校长test',       1),
    ('principal-test2',    'principal-test2',    crypt('123456', gen_salt('bf')), '李二校长test',       1),
    ('grade-admin-test1',  'grade-admin-test1',  crypt('123456', gen_salt('bf')), '王一年级管理员test', 1),
    ('grade-admin-test2',  'grade-admin-test2',  crypt('123456', gen_salt('bf')), '王二年级管理员test', 1),
    ('teacher-test1',      'teacher-test1',      crypt('123456', gen_salt('bf')), '赵一老师test',       1),
    ('teacher-test2',      'teacher-test2',      crypt('123456', gen_salt('bf')), '赵二老师test',       1),
    ('student-test1',      'student-test1',      crypt('123456', gen_salt('bf')), '孙一同学test',       1),
    ('student-test2',      'student-test2',      crypt('123456', gen_salt('bf')), '孙二同学test',       1)
ON CONFLICT (username) DO NOTHING;

-- 2) 用户-角色关联（按角色编码取 role_id，幂等）
-- 注意：sys_user_role 的唯一约束 UNIQUE(user_id, role_id, grade_id) 中 grade_id 为 NULL 时，
--       PostgreSQL 默认视 NULL 互不相等（不触发 ON CONFLICT），故用 NOT EXISTS 保证不重复插入
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'admin-test1'       AND r.code = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'admin-test2'       AND r.code = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'principal-test1'   AND r.code = 'PRINCIPAL'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'principal-test2'   AND r.code = 'PRINCIPAL'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'grade-admin-test1' AND r.code = 'GRADE_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'grade-admin-test2' AND r.code = 'GRADE_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'teacher-test1'     AND r.code = 'TEACHER'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'teacher-test2'     AND r.code = 'TEACHER'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'student-test1'     AND r.code = 'STUDENT'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'student-test2'     AND r.code = 'STUDENT'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
