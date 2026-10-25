<template>
  <div class="timetable-page">
    <el-container class="container">
      <el-header class="header">
        <div class="header-left">
          <el-icon :size="22" color="#1f6feb"><School /></el-icon>
          <span class="logo-text">校务管理系统</span>
        </div>
        <div class="header-right">
          <el-button text @click="$router.push('/admin')">返回首页</el-button>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" class="avatar">{{ avatarText }}</el-avatar>
              <span class="username">
                {{ userStore.userInfo?.realName || userStore.userInfo?.username }}
              </span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="change-password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <el-card shadow="never" class="timetable-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon color="#1f6feb"><Calendar /></el-icon>
                <span>课程表</span>
              </div>
              <span class="hint">小学 30 节/周、初中 35 节/周；每班独立排课</span>
            </div>
          </template>

          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <!-- 课程设置 -->
            <el-tab-pane label="课程设置" name="plan">
              <div class="toolbar">
                <span class="label">年级</span>
                <el-select v-model="planGradeId" style="width: 150px" @change="loadPlan">
                  <el-option v-for="g in grades" :key="g.id" :label="g.name" :value="g.id" />
                </el-select>
                <div class="spacer" />
                <el-tag :type="planTotal === planStandard ? 'success' : 'danger'" size="large">
                  合计 {{ planTotal }} / {{ planStandard }} 节
                </el-tag>
                <el-button type="primary" :loading="planSaving" @click="handleSavePlan">
                  保存计划
                </el-button>
              </div>

              <el-table :data="planRows" stripe empty-text="加载中">
                <el-table-column prop="name" label="科目" width="160" />
                <el-table-column label="类别" width="110">
                  <template #default="{ row }">
                    <el-tag size="small" :type="row.category === 'ACADEMIC' ? 'primary' : 'info'">
                      {{ row.category === 'ACADEMIC' ? '学科' : '活动' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="每周节数" width="150">
                  <template #default="{ row }">
                    <el-input-number v-model="row.weeklyPeriods" :min="0" :max="20" size="small" />
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <!-- 班级课表 -->
            <el-tab-pane label="班级课表" name="timetable">
              <div class="toolbar">
                <el-select
                  v-model="tt.schoolYearId"
                  placeholder="学年"
                  style="width: 150px"
                  @change="loadTimetable"
                >
                  <el-option v-for="y in schoolYears" :key="y.id" :label="y.name" :value="y.id" />
                </el-select>
                <el-select v-model="tt.term" style="width: 110px" @change="loadTimetable">
                  <el-option label="上学期" :value="1" />
                  <el-option label="下学期" :value="2" />
                </el-select>
                <el-select
                  v-model="tt.classId"
                  placeholder="班级"
                  style="width: 220px"
                  @change="loadTimetable"
                >
                  <el-option
                    v-for="c in classes"
                    :key="c.id"
                    :label="`${c.gradeName} ${c.name}`"
                    :value="c.id"
                  />
                </el-select>
                <div class="spacer" />
                <el-button
                  type="primary"
                  :loading="tt.saving"
                  :disabled="!tt.data"
                  @click="handleSaveTimetable"
                >
                  保存课表
                </el-button>
              </div>

              <el-empty v-if="!tt.data" description="请选择学年、学期和班级" />

              <div v-else class="grid-wrap" v-loading="tt.loading">
                <div class="grid-title">
                  {{ tt.data.gradeName }} {{ tt.data.className }}
                  <span class="grid-sub">{{ tt.data.stage === 'PRIMARY' ? '小学' : '初中' }}</span>
                </div>

                <table class="tt-grid">
                  <thead>
                    <tr>
                      <th class="tt-corner">节次</th>
                      <th v-for="w in WEEKDAYS" :key="w.value">{{ w.label }}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="p in tt.data.periods" :key="p.periodNo">
                      <td class="tt-period">
                        <div>第 {{ p.periodNo }} 节</div>
                        <div class="tt-time">{{ p.startTime.slice(0, 5) }}</div>
                      </td>
                      <td
                        v-for="w in WEEKDAYS"
                        :key="w.value"
                        class="tt-cell"
                        :class="{ filled: !!cellOf(w.value, p.periodNo) }"
                        @click="openCell(w.value, p.periodNo)"
                      >
                        <span v-if="cellOf(w.value, p.periodNo)" class="tt-subject">
                          {{ cellOf(w.value, p.periodNo).subjectName }}
                        </span>
                        <span v-else class="tt-empty">+</span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-main>
    </el-container>

    <!-- 选科目弹窗 -->
    <el-dialog v-model="cellDialog.visible" :title="cellTitle" width="520px">
      <div class="subject-grid">
        <el-button
          v-for="s in subjects"
          :key="s.id"
          size="small"
          :type="cellValue && cellValue.subjectId === s.id ? 'primary' : 'default'"
          @click="chooseSubject(s)"
        >
          {{ s.name }}
        </el-button>
      </div>
      <template #footer>
        <el-button v-if="cellValue" type="danger" plain @click="clearCell">清空该节</el-button>
        <el-button @click="cellDialog.visible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Calendar, School } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getClasses, getGrades, getSchoolYears } from '@/api/academic'
import {
  getGradePlan,
  getSubjects,
  getTimetable,
  saveGradePlan,
  saveTimetable
} from '@/api/timetable'

const WEEKDAYS = [
  { value: 1, label: '周一' },
  { value: 2, label: '周二' },
  { value: 3, label: '周三' },
  { value: 4, label: '周四' },
  { value: 5, label: '周五' }
]

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('plan')
const grades = ref([])
const classes = ref([])
const schoolYears = ref([])
const subjects = ref([])

const planGradeId = ref(null)
const planRows = ref([])
const planSaving = ref(false)

const tt = reactive({
  schoolYearId: null,
  term: 1,
  classId: null,
  data: null,
  loading: false,
  saving: false
})

const entryMap = ref({})
const cellDialog = reactive({ visible: false, weekday: null, periodNo: null })

const avatarText = computed(() => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username || 'U'
  return name.charAt(0).toUpperCase()
})

const planStage = computed(
  () => grades.value.find((g) => g.id === planGradeId.value)?.stage || 'PRIMARY'
)
const planStandard = computed(() => (planStage.value === 'PRIMARY' ? 30 : 35))
const planTotal = computed(() =>
  planRows.value.reduce((sum, row) => sum + (row.weeklyPeriods || 0), 0)
)

const cellValue = computed(
  () => entryMap.value[cellKey(cellDialog.weekday, cellDialog.periodNo)] || null
)

const cellTitle = computed(() => {
  const weekday = WEEKDAYS.find((w) => w.value === cellDialog.weekday)
  return `选择科目：${weekday?.label || ''} 第 ${cellDialog.periodNo} 节`
})

function cellKey(weekday, periodNo) {
  return `${weekday}-${periodNo}`
}

function cellOf(weekday, periodNo) {
  return entryMap.value[cellKey(weekday, periodNo)] || null
}

/* ---------- 加载 ---------- */

async function loadGrades() {
  grades.value = await getGrades()
}

async function loadClasses() {
  classes.value = await getClasses()
}

async function loadYears() {
  schoolYears.value = await getSchoolYears()
}

async function loadSubjects() {
  subjects.value = await getSubjects()
}

async function loadPlan() {
  if (!planGradeId.value) return
  try {
    planRows.value = await getGradePlan(planGradeId.value)
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  }
}

async function loadTimetable() {
  if (!tt.schoolYearId || !tt.classId) return
  tt.loading = true
  try {
    tt.data = await getTimetable({
      classId: tt.classId,
      schoolYearId: tt.schoolYearId,
      term: tt.term
    })
    const map = {}
    for (const entry of tt.data.entries) {
      map[cellKey(entry.weekday, entry.periodNo)] = entry
    }
    entryMap.value = map
  } catch (e) {
    tt.data = null
  } finally {
    tt.loading = false
  }
}

/* ---------- 课程设置 ---------- */

async function handleSavePlan() {
  if (!planGradeId.value) return

  const items = planRows.value
    .filter((row) => (row.weeklyPeriods || 0) > 0)
    .map((row) => ({ subjectId: row.subjectId, weeklyPeriods: row.weeklyPeriods }))

  planSaving.value = true
  try {
    await saveGradePlan(planGradeId.value, items)
    ElMessage.success('课程计划已保存')
    await loadPlan()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    planSaving.value = false
  }
}

/* ---------- 课表格子 ---------- */

function openCell(weekday, periodNo) {
  cellDialog.weekday = weekday
  cellDialog.periodNo = periodNo
  cellDialog.visible = true
}

function chooseSubject(subject) {
  const key = cellKey(cellDialog.weekday, cellDialog.periodNo)
  entryMap.value = {
    ...entryMap.value,
    [key]: {
      weekday: cellDialog.weekday,
      periodNo: cellDialog.periodNo,
      subjectId: subject.id,
      subjectName: subject.name
    }
  }
  cellDialog.visible = false
}

function clearCell() {
  const key = cellKey(cellDialog.weekday, cellDialog.periodNo)
  const next = { ...entryMap.value }
  delete next[key]
  entryMap.value = next
  cellDialog.visible = false
}

async function handleSaveTimetable() {
  if (!tt.data) return

  const entries = Object.values(entryMap.value).map((item) => ({
    weekday: item.weekday,
    periodNo: item.periodNo,
    subjectId: item.subjectId
  }))

  tt.saving = true
  try {
    const result = await saveTimetable({
      classId: tt.data.classId,
      schoolYearId: tt.data.schoolYearId,
      term: tt.data.term,
      entries
    })

    if (result.warnings && result.warnings.length) {
      ElMessageBox.alert(result.warnings.join('<br/>'), '课时与年级计划不一致', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '知道了',
        type: 'warning'
      })
    } else {
      ElMessage.success('课表已保存')
    }
    await loadTimetable()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    tt.saving = false
  }
}

/* ---------- 其他 ---------- */

function handleTabChange(name) {
  if (name === 'timetable' && !tt.data) {
    loadTimetable()
  }
}

function handleCommand(command) {
  if (command === 'change-password') {
    router.push('/change-password')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/login')
      })
      .catch(() => {})
  }
}

onMounted(async () => {
  await Promise.all([loadGrades(), loadClasses(), loadYears(), loadSubjects()])

  if (grades.value.length) {
    planGradeId.value = grades.value[0].id
    await loadPlan()
  }

  const currentYear = schoolYears.value.find((y) => y.current) || schoolYears.value[0]
  if (currentYear) tt.schoolYearId = currentYear.id
  if (classes.value.length) tt.classId = classes.value[0].id
  if (tt.schoolYearId && tt.classId) {
    await loadTimetable()
  }
})
</script>

<style scoped>
.timetable-page {
  height: 100%;
}

.container {
  height: 100%;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #1f6feb;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #303133;
  outline: none;
}

.avatar {
  background: #1f6feb;
  color: #fff;
  font-weight: 600;
}

.main {
  background-color: #f5f7fa;
  display: flex;
  justify-content: center;
}

.timetable-card {
  width: 100%;
  max-width: 1200px;
  margin: 24px 0;
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.hint {
  font-size: 12px;
  color: #909399;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.label {
  font-size: 14px;
  color: #606266;
}

.spacer {
  flex: 1;
}

.grid-wrap {
  margin-top: 8px;
}

.grid-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.grid-sub {
  margin-left: 8px;
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}

.tt-grid {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.tt-grid th,
.tt-grid td {
  border: 1px solid #ebeef5;
  text-align: center;
}

.tt-grid th {
  height: 40px;
  background: #f5f7fa;
  font-size: 13px;
  color: #606266;
  font-weight: 600;
}

.tt-corner {
  width: 90px;
}

.tt-period {
  height: 54px;
  background: #fafbfc;
  font-size: 13px;
  color: #606266;
}

.tt-time {
  font-size: 11px;
  color: #c0c4cc;
}

.tt-cell {
  height: 54px;
  cursor: pointer;
  transition: background 0.15s;
}

.tt-cell:hover {
  background: #e8f1fe;
}

.tt-cell.filled {
  background: #fff;
}

.tt-subject {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.tt-empty {
  color: #c0c4cc;
  font-size: 18px;
}

.subject-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}
</style>
