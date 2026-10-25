-- ============================================================
-- 校务系统 - 权限模型建表脚本（PostgreSQL 17）
-- 说明：功能权限（RBAC）+ 数据权限（数据范围）双层模型
-- 角色：ADMIN / PRINCIPAL / GRADE_ADMIN / TEACHER / STUDENT
-- ============================================================

-- 1. 用户表：统一登录账号 + 公共信息
CREATE TABLE sys_user (
    id               BIGSERIAL PRIMARY KEY,
    username         VARCHAR(50)  NOT NULL UNIQUE,   -- 登录名（管理员/校长自定义；师生账号由系统生成，默认=学号/职工号）
    login_no         VARCHAR(20) UNIQUE,             -- 登录号：学生=学号，教职工=职工号；师生用它作为登录名
    password         VARCHAR(100) NOT NULL,          -- BCrypt 加密
    real_name        VARCHAR(50)  NOT NULL,
    phone            VARCHAR(20),                    -- 联系电话：手机号或固定电话（座机带区号，如 010-12345678）
    email            VARCHAR(100),                   -- 邮箱地址
    status           SMALLINT DEFAULT 1,             -- 状态枚举：0初始 1正常 2请假 3休学 4转出 5离职 6病假 7退休 8毕业 9锁定 10密码过期
    last_login_time  TIMESTAMP,                      -- 最近一次登录时间
    pwd_fail_count   INT DEFAULT 0,                  -- 密码连续失败次数（达到阈值锁定）
    pwd_update_time  TIMESTAMP,                      -- 密码到期时间（修改密码时间+90天，当前时间晚于它则状态置为密码过期）
    otp_expire_time  TIMESTAMP,                      -- 一次性密码到期时间（管理员重置后写入，超时需重新重置；设置正式密码后清空）
    created_at       TIMESTAMP DEFAULT now()
);

-- 2. 角色表：功能角色 + 数据范围标记
CREATE TABLE sys_role (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(30) NOT NULL UNIQUE,    -- ADMIN / PRINCIPAL / GRADE_ADMIN / TEACHER / STUDENT
    name        VARCHAR(50) NOT NULL,           -- 系统管理员 / 校长 / 年级管理员 / 教师 / 学生
    data_scope  VARCHAR(20) NOT NULL,           -- 数据范围：ALL / ALL_STAFF_STUDENT / GRADE / CLASS / SELF
    description VARCHAR(200)
);

-- 3. 年级表：一年级 ~ 初三（九年一贯制：小学 6 年 + 初中 3 年）
CREATE TABLE grade (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,     -- 年级名称：一年级~六年级、初一~初三
    stage      VARCHAR(20) NOT NULL,            -- 学段：PRIMARY(小学) / JUNIOR(初中)
    sort_order INT NOT NULL,                    -- 排序：一年级=1 ... 六年级=6、初一=7 ... 初三=9
    enabled    SMALLINT DEFAULT 1               -- 1启用 0停用
);

-- 年级种子数据
INSERT INTO grade (name, stage, sort_order) VALUES
    ('一年级', 'PRIMARY', 1),
    ('二年级', 'PRIMARY', 2),
    ('三年级', 'PRIMARY', 3),
    ('四年级', 'PRIMARY', 4),
    ('五年级', 'PRIMARY', 5),
    ('六年级', 'PRIMARY', 6),
    ('初一',   'JUNIOR',  7),
    ('初二',   'JUNIOR',  8),
    ('初三',   'JUNIOR',  9);

-- 4. 用户-角色关联表：多对多，一个用户可拥有多个角色
CREATE TABLE sys_user_role (
    id       BIGSERIAL PRIMARY KEY,
    user_id  BIGINT NOT NULL REFERENCES sys_user(id),
    role_id  BIGINT NOT NULL REFERENCES sys_role(id),
    grade_id BIGINT REFERENCES grade(id),       -- 仅 GRADE_ADMIN 角色填写：负责哪个年级
    UNIQUE (user_id, role_id, grade_id)
);

-- 5. 班级表
CREATE TABLE class (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(50) NOT NULL,
    grade_id       BIGINT NOT NULL REFERENCES grade(id),
    is_graduated   BOOLEAN DEFAULT FALSE,      -- 是否已毕业离校
    graduate_time  TIMESTAMP,                  -- 毕业归档时间
    created_at     TIMESTAMP DEFAULT now(),
    UNIQUE (grade_id, name)                    -- 同一年级下班级名唯一
);

-- 6. 教师表（业务扩展，与 sys_user 一对一）
CREATE TABLE teacher (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL UNIQUE REFERENCES sys_user(id),
    teacher_no VARCHAR(20) UNIQUE
);

-- 7. 教师任教班级表：决定教师的"数据范围"
CREATE TABLE teacher_class (
    id         BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teacher(id),
    class_id   BIGINT NOT NULL REFERENCES class(id),
    subject    VARCHAR(30),
    UNIQUE (teacher_id, class_id, subject)
);

-- 8. 学生表（业务扩展，与 sys_user 一对一）
CREATE TABLE student (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL UNIQUE REFERENCES sys_user(id),
    student_no    VARCHAR(20) UNIQUE,
    class_id      BIGINT NOT NULL REFERENCES class(id),
    enroll_year   INT NOT NULL,                -- 入学年份（按届管理、学号校验）
    graduate_time TIMESTAMP,                   -- 毕业时间（毕业离校时写入）
    created_at    TIMESTAMP DEFAULT now()
);

-- 9. 安全问题表：预设问题库（用于重置密码）
CREATE TABLE sys_security_question (
    id         BIGSERIAL PRIMARY KEY,
    question   VARCHAR(200) NOT NULL,       -- 问题文本（如：您母亲的姓名？）
    enabled    SMALLINT DEFAULT 1,          -- 1启用 0停用
    sort_order INT
);

-- 10. 用户安全问题答案表：用户在注册/设置时选择的题目与答案
CREATE TABLE sys_user_security_answer (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES sys_user(id),
    question_id BIGINT NOT NULL REFERENCES sys_security_question(id),
    answer_hash VARCHAR(100) NOT NULL,      -- 答案哈希（BCrypt），不存明文
    updated_at  TIMESTAMP DEFAULT now(),
    UNIQUE (user_id, question_id)
);

-- ============================================================
-- 在线考试模块
-- 题型：客观题（单选/多选/判断/填空）+ 主观题（简答/解答/作文）
--       材料题（阅读/听力/看图 = 材料 + 一组小题）
-- ============================================================

-- 11. 科目表：数学 / 语文 / 英语 等
CREATE TABLE subject (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,      -- 数学 / 语文 / 英语
    code       VARCHAR(20) UNIQUE,               -- MATH / CHINESE / ENGLISH
    category   VARCHAR(20) DEFAULT 'ACADEMIC',   -- ACADEMIC 学科 / ACTIVITY 非学科（音乐、班会、自习）
    enabled    SMALLINT DEFAULT 1,               -- 1启用 0停用
    sort_order INT
);

-- 12. 题目材料表：阅读文章 / 听力音频 / 看图图片
CREATE TABLE exam_question_material (
    id         BIGSERIAL PRIMARY KEY,
    title      VARCHAR(200),                     -- 材料标题（如：阅读理解（一））
    content    TEXT,                             -- 文本材料（阅读文章）
    audio_url  VARCHAR(255),                     -- 听力音频地址
    image_url  VARCHAR(255),                     -- 图片地址（看图题）
    sort_order INT
);

-- 13. 题目表：小题，作答类型决定判分方式
--     question_type: SINGLE_CHOICE 单选 / MULTIPLE_CHOICE 多选 / JUDGE 判断
--                    FILL_BLANK 填空 / SUBJECTIVE 主观（简答/解答/作文）
--     阅读/听力/看图题 = 关联 material_id 的一组小题
CREATE TABLE exam_question (
    id            BIGSERIAL PRIMARY KEY,
    question_type VARCHAR(30) NOT NULL,          -- 作答类型（见上）
    stem          TEXT NOT NULL,                 -- 题干
    material_id   BIGINT REFERENCES exam_question_material(id),  -- 所属材料（可为空=独立题）
    answer        TEXT,                          -- 客观题标准答案（填空/判断等）
    analysis      TEXT,                          -- 答案解析
    auto_grade    BOOLEAN DEFAULT FALSE,         -- 是否机器判分（客观题TRUE/主观题FALSE）
    difficulty    SMALLINT DEFAULT 3,            -- 难度 1~5
    subject_id    BIGINT NOT NULL REFERENCES subject(id),
    created_by    BIGINT REFERENCES sys_user(id),
    created_at    TIMESTAMP DEFAULT now()
);

-- 14. 题目选项表：选择题选项（单选/多选）
CREATE TABLE exam_question_option (
    id          BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES exam_question(id),
    option_text VARCHAR(500) NOT NULL,           -- 选项内容
    is_correct  BOOLEAN DEFAULT FALSE,           -- 是否正确答案
    sort_order  INT NOT NULL                     -- 选项顺序（A/B/C/D）
);

-- 15. 试卷表
CREATE TABLE exam_paper (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,          -- 试卷名称
    subject_id   BIGINT NOT NULL REFERENCES subject(id),
    total_score  DECIMAL(6,1) NOT NULL DEFAULT 0,
    description  VARCHAR(500),
    status       SMALLINT DEFAULT 0,             -- 0草稿 1已发布
    created_by   BIGINT REFERENCES sys_user(id),
    created_at   TIMESTAMP DEFAULT now()
);

-- 16. 试卷题目表：试卷包含哪些题、每题分值、顺序
CREATE TABLE exam_paper_question (
    id          BIGSERIAL PRIMARY KEY,
    paper_id    BIGINT NOT NULL REFERENCES exam_paper(id),
    question_id BIGINT NOT NULL REFERENCES exam_question(id),
    score       DECIMAL(5,1) NOT NULL,           -- 该题在试卷中的分值（可覆盖题目默认分值）
    sort_order  INT NOT NULL,                    -- 试卷内题目顺序
    UNIQUE (paper_id, question_id)
);

-- 17. 考试表：一次考试活动
CREATE TABLE exam (
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(100) NOT NULL,          -- 如：七年级上学期期中考试-数学
    subject_id   BIGINT NOT NULL REFERENCES subject(id),
    paper_id     BIGINT NOT NULL REFERENCES exam_paper(id),
    description  VARCHAR(500),
    duration_min INT NOT NULL,                   -- 考试时长（分钟）
    total_score  DECIMAL(6,1) NOT NULL,          -- 总分
    pass_score   DECIMAL(6,1),                   -- 及格分
    start_time   TIMESTAMP,                      -- 开考时间
    end_time     TIMESTAMP,                      -- 截止时间
    status       SMALLINT DEFAULT 0,             -- 0草稿 1已发布 2进行中 3已结束 4已归档
    term         VARCHAR(20),                    -- 学期：如 2025-2026-1
    exam_type    VARCHAR(20) DEFAULT 'NORMAL',   -- 考试类型：MIDTERM期中 / FINAL期末 / NORMAL平时
    created_by   BIGINT REFERENCES sys_user(id),
    created_at   TIMESTAMP DEFAULT now()
);

-- 18. 学生考试记录表：谁参加了哪场考试（一人一场一条）
CREATE TABLE exam_record (
    id           BIGSERIAL PRIMARY KEY,
    exam_id      BIGINT NOT NULL REFERENCES exam(id),
    student_id   BIGINT NOT NULL REFERENCES student(id),
    start_time   TIMESTAMP,                      -- 进入考试时间
    submit_time  TIMESTAMP,                      -- 交卷时间
    auto_score   DECIMAL(6,1),                   -- 客观题得分（自动判分）
    manual_score DECIMAL(6,1),                   -- 主观题得分（人工阅卷）
    score        DECIMAL(6,1),                   -- 总分 = auto + manual
    status       SMALLINT DEFAULT 0,             -- 0未开始 1作答中 2已提交 3已阅卷
    UNIQUE (exam_id, student_id)
);

-- 19. 学生答案表：每道题的作答与得分
CREATE TABLE exam_student_answer (
    id          BIGSERIAL PRIMARY KEY,
    record_id   BIGINT NOT NULL REFERENCES exam_record(id),
    question_id BIGINT NOT NULL REFERENCES exam_question(id),
    answer_text TEXT,                            -- 作答内容（选项/填空/主观答案）
    score       DECIMAL(5,1),                    -- 该题得分（客观题自动判分；主观题阅卷后写入）
    is_correct  BOOLEAN,                         -- 客观题是否正确
    grader_id   BIGINT REFERENCES sys_user(id),  -- 主观题阅卷人
    grade_time  TIMESTAMP,                       -- 阅卷时间
    UNIQUE (record_id, question_id)
);

-- ============================================================
-- 成绩单模块
-- 数据权限：ADMIN/PRINCIPAL 全部，GRADE_ADMIN 本年级，
--           TEACHER 本人任教班级+科目，STUDENT 仅本人
-- ============================================================

-- 20. 成绩记录表：成绩单数据源（考试来源或手工录入）
CREATE TABLE score (
    id           BIGSERIAL PRIMARY KEY,
    student_id   BIGINT NOT NULL REFERENCES student(id),
    subject_id   BIGINT NOT NULL REFERENCES subject(id),
    exam_id      BIGINT REFERENCES exam(id),     -- 成绩来源考试（可空=手工录入，如平时成绩）
    exam_type    VARCHAR(20) NOT NULL DEFAULT 'NORMAL',  -- 考试类型：MIDTERM/FINAL/NORMAL
    term         VARCHAR(20) NOT NULL,           -- 学期：如 2025-2026-1
    score        DECIMAL(6,1) NOT NULL,          -- 成绩（0~满分）
    recorded_by  BIGINT REFERENCES sys_user(id), -- 录入人
    created_at   TIMESTAMP DEFAULT now(),
    UNIQUE (student_id, subject_id, exam_id, exam_type, term)
);

-- 成绩表索引：按学期/科目/学生查询
CREATE INDEX idx_score_term_subject ON score (term, subject_id);
CREATE INDEX idx_score_student ON score (student_id, term);

-- ============================================================
-- 系统日志模块
-- 记录「谁 在什么时间 做了什么操作」，用于安全审计与问题追溯
-- ============================================================

-- 21. 操作日志表
CREATE TABLE sys_operation_log (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT,                          -- 操作人 ID（登录失败且账号不存在时为空，不设外键以免影响用户删除）
    username    VARCHAR(50)  NOT NULL,           -- 操作人登录名快照（失败时=尝试登录的账号）
    real_name   VARCHAR(50),                     -- 操作人姓名快照
    module      VARCHAR(30)  NOT NULL,           -- 所属模块：AUTH / USER / ...
    action      VARCHAR(50)  NOT NULL,           -- 操作类型：LOGIN / LOGOUT / CHANGE_PASSWORD / RESET_PASSWORD / ...
    detail      VARCHAR(500),                    -- 成功说明或失败原因
    success     BOOLEAN      NOT NULL,           -- 是否成功
    ip          VARCHAR(64),                     -- 来源 IP
    user_agent  VARCHAR(255),                    -- 客户端 User-Agent
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

-- 日志索引：按时间倒序、按操作人查询
CREATE INDEX idx_op_log_created ON sys_operation_log (created_at DESC);
CREATE INDEX idx_op_log_user ON sys_operation_log (username, created_at DESC);

-- ============================================================
-- 消息模块（1:1 单聊，GROUP 类型为后续群聊预留）
-- ============================================================

-- 22. 消息会话
CREATE TABLE msg_conversation (
    id          BIGSERIAL PRIMARY KEY,
    type        VARCHAR(10)  NOT NULL DEFAULT 'SINGLE',   -- SINGLE 单聊 / GROUP 群聊（预留）
    title       VARCHAR(100),                              -- 群聊名称（单聊为空）
    pair_key    VARCHAR(50)  UNIQUE,                       -- 单聊去重键：小用户ID_大用户ID
    created_by  BIGINT REFERENCES sys_user(id),
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

-- 23. 会话成员（含已读位置）
CREATE TABLE msg_conversation_member (
    id                    BIGSERIAL PRIMARY KEY,
    conversation_id       BIGINT NOT NULL REFERENCES msg_conversation(id),
    user_id               BIGINT NOT NULL REFERENCES sys_user(id),
    last_read_message_id  BIGINT,                          -- 该成员已读到的最大消息 ID
    joined_at             TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (conversation_id, user_id)
);
CREATE INDEX idx_msg_member_user ON msg_conversation_member (user_id);

-- 24. 消息
CREATE TABLE msg_message (
    id               BIGSERIAL PRIMARY KEY,
    conversation_id  BIGINT NOT NULL REFERENCES msg_conversation(id),
    sender_id        BIGINT NOT NULL REFERENCES sys_user(id),
    content          TEXT NOT NULL,
    content_type     VARCHAR(10) NOT NULL DEFAULT 'TEXT',  -- TEXT（预留 IMAGE / FILE）
    created_at       TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_msg_message_conv ON msg_message (conversation_id, id DESC);

-- ============================================================
-- 学籍基础数据补充
-- ============================================================

-- 25. 学年表：统一下级口径，升级/成绩/考试都引用
CREATE TABLE school_year (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(20) NOT NULL UNIQUE,   -- 如 2025-2026
    start_date DATE NOT NULL,
    end_date   DATE NOT NULL,
    is_current BOOLEAN NOT NULL DEFAULT FALSE -- 当前学年（全表最多一条为 true）
);

-- 26. 学生年级/班级变更历史
CREATE TABLE student_grade_history (
    id             BIGSERIAL PRIMARY KEY,
    student_id     BIGINT NOT NULL REFERENCES student(id),
    from_class_id  BIGINT REFERENCES class(id),
    to_class_id    BIGINT REFERENCES class(id),
    change_type    VARCHAR(20) NOT NULL,      -- PROMOTE/TRANSFER_IN/REPEAT/SKIP/ADJUST/GRADUATE
    school_year_id BIGINT REFERENCES school_year(id),
    operator_id    BIGINT REFERENCES sys_user(id),
    remark         VARCHAR(200),
    created_at     TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_sgh_student ON student_grade_history (student_id);

-- ============================================================
-- 课程表模块
-- ============================================================

-- 27. 年级课程计划：每个年级每门课每周几节
CREATE TABLE grade_subject (
    id             BIGSERIAL PRIMARY KEY,
    grade_id       BIGINT NOT NULL REFERENCES grade(id),
    subject_id     BIGINT NOT NULL REFERENCES subject(id),
    weekly_periods INT NOT NULL,
    UNIQUE (grade_id, subject_id)
);

-- 28. 节次作息（小学/初中两套）
CREATE TABLE period (
    id         BIGSERIAL PRIMARY KEY,
    stage      VARCHAR(20) NOT NULL,   -- PRIMARY / JUNIOR
    period_no  INT NOT NULL,
    start_time TIME NOT NULL,
    end_time   TIME NOT NULL,
    UNIQUE (stage, period_no)
);

-- 29. 班级课表头：一个班在一个学年/学期一张课表
CREATE TABLE timetable (
    id             BIGSERIAL PRIMARY KEY,
    class_id       BIGINT NOT NULL REFERENCES class(id),
    school_year_id BIGINT NOT NULL REFERENCES school_year(id),
    term           SMALLINT NOT NULL,  -- 1 上学期 / 2 下学期
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (class_id, school_year_id, term)
);

-- 30. 课表条目
CREATE TABLE timetable_entry (
    id           BIGSERIAL PRIMARY KEY,
    timetable_id BIGINT NOT NULL REFERENCES timetable(id),
    weekday      SMALLINT NOT NULL,   -- 1~5
    period_no    INT NOT NULL,
    subject_id   BIGINT NOT NULL REFERENCES subject(id),
    teacher_id   BIGINT REFERENCES teacher(id),  -- 第一版留空，预留教师排课
    UNIQUE (timetable_id, weekday, period_no)
);
CREATE INDEX idx_entry_teacher ON timetable_entry (teacher_id);
CREATE INDEX idx_entry_subject ON timetable_entry (subject_id);
