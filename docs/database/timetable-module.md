# 课程表模块

按班级维护每周课表：年级课程计划 → 节次作息 → 班级课表。支持冲突校验，后续可扩展教师排课与打印。

## 一、设计前提

| 项 | 约定 |
|----|------|
| 小学作息 | 每天 6 节（上午 4 + 下午 2），每周 **30 节**，每节 40 分钟 |
| 初中作息 | 每天 7 节（上午 4 + 下午 3），每周 **35 节**，每节 45 分钟 |
| 上课日 | 周一 ~ 周五 |
| 课表粒度 | 按**班级**独立排课，每班一张；不做年级统一模板，可从其他班复制 |
| 教师排课 | **第一版不做**；`timetable_entry.teacher_id` 预留字段，后续版本再启用 |
| 学期 | 按学年 + 学期（1 上学期 / 2 下学期），引用 `school_year` |

> 音乐、班会、自习不在原始科目清单中，但为填满课时而纳入；可在「课程设置」里调整或删除。

## 二、数据模型

```mermaid
erDiagram
    SUBJECT ||--o{ GRADE_SUBJECT : "年级计划"
    GRADE ||--o{ GRADE_SUBJECT : "包含"
    CLASS ||--o{ TIMETABLE : "拥有"
    SCHOOL_YEAR ||--o{ TIMETABLE : "学年"
    TIMETABLE ||--o{ TIMETABLE_ENTRY : "包含"
    SUBJECT ||--o{ TIMETABLE_ENTRY : "科目"
    TEACHER ||--o{ TIMETABLE_ENTRY : "任课"
    PERIOD }o--|| TIMETABLE_ENTRY : "节次作息"
```

### 1. `subject`（已有表，补种子）

| 字段 | 说明 |
|------|------|
| `name` / `code` | 科目名称与编码 |
| `enabled` / `sort_order` | 启用、排序 |
| **建议新增** `category` | `ACADEMIC` 学科 / `ACTIVITY` 非学科（音乐、班会、自习），统计时排除 |

科目种子：

| 名称 | code | category |
|------|------|----------|
| 语文 | CHINESE | ACADEMIC |
| 数学 | MATH | ACADEMIC |
| 英语 | ENGLISH | ACADEMIC |
| 体育 | PE | ACADEMIC |
| 美术 | ART | ACADEMIC |
| 科学 | SCIENCE | ACADEMIC |
| 道德与法律 | MORAL | ACADEMIC |
| 政治 | POLITICS | ACADEMIC |
| 生物 | BIOLOGY | ACADEMIC |
| 物理 | PHYSICS | ACADEMIC |
| 化学 | CHEMISTRY | ACADEMIC |
| 音乐 | MUSIC | ACTIVITY |
| 班会 | CLASS_MEETING | ACTIVITY |
| 自习 | SELF_STUDY | ACTIVITY |

### 2. `grade_subject` 年级课程计划

```sql
CREATE TABLE grade_subject (
    id             BIGSERIAL PRIMARY KEY,
    grade_id       BIGINT NOT NULL REFERENCES grade(id),
    subject_id     BIGINT NOT NULL REFERENCES subject(id),
    weekly_periods INT NOT NULL,              -- 每周节数
    UNIQUE (grade_id, subject_id)
);
```

### 3. `period` 节次作息

```sql
CREATE TABLE period (
    id         BIGSERIAL PRIMARY KEY,
    stage      VARCHAR(20) NOT NULL,          -- PRIMARY / JUNIOR
    period_no  INT NOT NULL,                  -- 第几节
    start_time TIME NOT NULL,
    end_time   TIME NOT NULL,
    UNIQUE (stage, period_no)
);
```

默认作息（可调整）：

| 节次 | 小学 | 初中 |
|------|------|------|
| 1 | 08:00–08:40 | 08:00–08:45 |
| 2 | 08:50–09:30 | 08:55–09:40 |
| 3 | 09:40–10:20 | 10:00–10:45 |
| 4 | 10:50–11:30 | 10:55–11:40 |
| 5 | 13:30–14:10 | 13:30–14:15 |
| 6 | 14:20–15:00 | 14:25–15:10 |
| 7 | — | 15:20–16:05 |

### 4. `timetable` 班级课表头

```sql
CREATE TABLE timetable (
    id             BIGSERIAL PRIMARY KEY,
    class_id       BIGINT NOT NULL REFERENCES class(id),
    school_year_id BIGINT NOT NULL REFERENCES school_year(id),
    term           SMALLINT NOT NULL,         -- 1 上 / 2 下
    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (class_id, school_year_id, term)
);
```

### 5. `timetable_entry` 课表条目

```sql
CREATE TABLE timetable_entry (
    id           BIGSERIAL PRIMARY KEY,
    timetable_id BIGINT NOT NULL REFERENCES timetable(id),
    weekday      SMALLINT NOT NULL,           -- 1~5
    period_no    INT NOT NULL,
    subject_id   BIGINT NOT NULL REFERENCES subject(id),
    teacher_id   BIGINT REFERENCES teacher(id),   -- 第一版留空，预留：后续支持教师排课
    UNIQUE (timetable_id, weekday, period_no)      -- 同班同一节只能一门课
);
CREATE INDEX idx_entry_teacher ON timetable_entry (teacher_id);
CREATE INDEX idx_entry_subject ON timetable_entry (subject_id);
```

> 第一版不写 `teacher_id`；后续启用教师排课时再填充，并增加教师冲突校验。

## 三、周课时计划

### 小学（30 节/周）

| 科目 | 一 | 二 | 三 | 四 | 五 | 六 |
|------|----|----|----|----|----|----|
| 语文 | 8 | 8 | 7 | 7 | 7 | 7 |
| 数学 | 5 | 5 | 5 | 5 | 5 | 5 |
| 英语 | – | – | 2 | 3 | 3 | 3 |
| 体育 | 4 | 4 | 4 | 4 | 4 | 4 |
| 美术 | 2 | 2 | 2 | 2 | 2 | 2 |
| 科学 | 1 | 2 | 2 | 2 | 2 | 2 |
| 道德与法律 | – | 2 | 2 | 2 | 2 | 2 |
| 音乐 | 2 | 2 | 2 | 2 | 2 | 2 |
| 班会 | 1 | 1 | 1 | 1 | 1 | 1 |
| 自习 | 7 | 4 | 3 | 2 | 2 | 2 |
| **合计** | 30 | 30 | 30 | 30 | 30 | 30 |

### 初中（35 节/周）

| 科目 | 初一 | 初二 | 初三 |
|------|------|------|------|
| 语文 | 6 | 6 | 6 |
| 数学 | 6 | 6 | 6 |
| 英语 | 6 | 6 | 6 |
| 体育 | 3 | 3 | 3 |
| 政治 | 2 | 2 | 2 |
| 生物 | 2 | 2 | 2 |
| 物理 | 2 | 3 | 3 |
| 化学 | 2 | 2 | 3 |
| 美术 | 1 | 1 | 1 |
| 班会 | 1 | 1 | 1 |
| 自习 | 4 | 3 | 2 |
| **合计** | 35 | 35 | 35 |

## 四、示例课表

### 一年级（30 节）

| 节次 | 周一 | 周二 | 周三 | 周四 | 周五 |
|------|------|------|------|------|------|
| 1 | 语文 | 数学 | 语文 | 数学 | 语文 |
| 2 | 数学 | 语文 | 数学 | 自习 | 数学 |
| 3 | 语文 | 语文 | 体育 | 体育 | 语文 |
| 4 | 体育 | 科学 | 语文 | 美术 | 体育 |
| 5 | 美术 | 音乐 | 自习 | 音乐 | 班会 |
| 6 | 自习 | 自习 | 自习 | 自习 | 自习 |

课时核对：语文 8、数学 5、体育 4、美术 2、科学 1、音乐 2、班会 1、自习 7 = 30 ✅

### 初一（35 节）

| 节次 | 周一 | 周二 | 周三 | 周四 | 周五 |
|------|------|------|------|------|------|
| 1 | 语文 | 数学 | 语文 | 数学 | 语文 |
| 2 | 数学 | 语文 | 数学 | 语文 | 数学 |
| 3 | 英语 | 英语 | 英语 | 英语 | 英语 |
| 4 | 政治 | 生物 | 物理 | 生物 | 英语 |
| 5 | 物理 | 化学 | 政治 | 化学 | 语文 |
| 6 | 体育 | 美术 | 体育 | 体育 | 数学 |
| 7 | 自习 | 自习 | 班会 | 自习 | 自习 |

课时核对：语文 6、数学 6、英语 6、体育 3、政治 2、生物 2、物理 2、化学 2、美术 1、班会 1、自习 4 = 35 ✅

## 五、排课规则与冲突校验

保存课表时服务端校验：

1. **同班同节唯一**：`(timetable_id, weekday, period_no)` 唯一约束兜底；
2. **课时一致**：每科周节数应与 `grade_subject.weekly_periods` 一致，不一致返回差异清单（可仅警告）；
3. **同科频次**：同一科目一天不超过 2 节（可配置）；
4. **连堂**：默认允许，可在配置中禁止；
5. 保存失败返回冲突列表：星期 / 节次 / 科目 / 原因，前端高亮。

> 教师冲突校验在启用教师排课后加入（第二阶段）。

## 六、接口设计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/timetable/plans?gradeId=` | 年级周课时计划 |
| PUT | `/api/admin/timetable/plans?gradeId=` | 批量保存年级周课时 |
| GET | `/api/admin/timetable/periods?stage=` | 节次作息 |
| GET | `/api/admin/timetable?classId=&schoolYearId=&term=` | 读取班级课表 |
| PUT | `/api/admin/timetable?classId=&schoolYearId=&term=` | 保存整张课表（含校验） |
| POST | `/api/admin/timetable/copy` | 从一个班复制到另一个班 |
| GET | `/api/admin/timetable/export?classId=` | 导出（后续，Excel/PDF） |

## 七、页面设计

### 课程设置页

- 选择年级 → 显示「科目 × 周节数」表格，可编辑；
- 底部实时合计，超出或不足每周节数时提示；
- 保存后作为该年级课表的默认课时。

### 课表管理页

- 顶部选择：学年 / 学期 / 班级；
- 主体：5 列（周一~周五）× N 行（节次）网格；
- 点击格子弹出选择器：**仅选择科目**（第一版不排教师）；
- 冲突格子标红，保存前统一校验；
- 支持「复制其他班课表」「清空」「打印/导出」。

## 八、与现有模块的关系

| 模块 | 关系 |
|------|------|
| 学籍 | 课表挂在 `class` 上，班级来自学籍模块 |
| 学年 | `timetable.school_year_id` 引用 `school_year`，学期用 1/2 |
| 教师 | `teacher_id` 引用 `teacher`；任课关系可参考 `teacher_class` |
| 成绩/考试 | 后续可用 `subject_id` 关联，统一学期口径 |
| 操作日志 | 保存课表写 `ACADEMIC / SAVE_TIMETABLE` 日志 |

> 现有 `teacher_class.subject` 是字符串，建议后续改为 `subject_id` 与科目表对齐，便于课表与成绩统一取数。

## 九、实施计划

1. **基础数据**：`subject` 补种子（含 `category`）、`period` 作息种子、`grade_subject` 计划接口与页面；
2. **课表条目**：`timetable` + `timetable_entry`（不含教师）接口与网格页面；
3. **增强**：从其他班复制、导出打印；
4. **后续**：教师排课与冲突检测、单双周、走班、教师视角课表。

## 十、已确认的决定

| 问题 | 决定 |
|------|------|
| 作息时间 | 按默认（08:00 开始，午休 11:30–13:30） |
| 教师排课 | 第一版不做，后续设计实施；`teacher_id` 预留 |
| 课表粒度 | 每班独立排课，不做年级统一模板 |
