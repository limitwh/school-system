-- ============================================================
-- 校务系统 - 测试用户清理脚本
-- 说明：删除 testdataAdd.sql 创建的 10 个测试用户及其角色关联
--       仅删除指定的测试账号，不影响 admin 等真实数据
-- 执行：psql -U ai -h localhost -d school_system -f docs/database/testdataClear.sql
-- ============================================================

-- 1) 删除用户-角色关联（先删子表，避免外键约束）
DELETE FROM sys_user_role
WHERE user_id IN (
    SELECT id FROM sys_user
    WHERE username IN (
        'admin-test1',       'admin-test2',
        'principal-test1',   'principal-test2',
        'grade-admin-test1', 'grade-admin-test2',
        'teacher-test1',     'teacher-test2',
        'student-test1',     'student-test2'
    )
);

-- 2) 删除测试用户
DELETE FROM sys_user
WHERE username IN (
    'admin-test1',       'admin-test2',
    'principal-test1',   'principal-test2',
    'grade-admin-test1', 'grade-admin-test2',
    'teacher-test1',     'teacher-test2',
    'student-test1',     'student-test2'
);
