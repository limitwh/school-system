<template>
  <div class="academic-page">
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
        <el-card shadow="never" class="academic-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon color="#1f6feb"><Management /></el-icon>
                <span>学籍管理</span>
              </div>
              <span class="hint">年级为系统种子数据（小学 6 + 初中 3）；班级、学生、学年可维护</span>
            </div>
          </template>

          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <!-- 学生 -->
            <el-tab-pane label="学生" name="students">
              <div class="toolbar">
                <el-input
                  v-model="studentQuery.keyword"
                  placeholder="姓名 / 学号"
                  clearable
                  style="width: 180px"
                  @keyup.enter="searchStudents"
                />
                <el-select
                  v-model="studentQuery.classId"
                  clearable
                  placeholder="全部班级"
                  style="width: 200px"
                >
                  <el-option
                    v-for="c in classes"
                    :key="c.id"
                    :label="`${c.gradeName} ${c.name}`"
                    :value="c.id"
                  />
                </el-select>
                <el-button type="primary" @click="searchStudents">查询</el-button>
                <el-button @click="resetStudentQuery">重置</el-button>
                <div class="spacer" />
                <el-button type="primary" @click="openStudentDialog()">新增学生</el-button>
              </div>

              <el-table :data="students" v-loading="loadingStudents" stripe empty-text="暂无学生">
                <el-table-column prop="studentNo" label="学号" width="130" />
                <el-table-column prop="realName" label="姓名" width="120" />
                <el-table-column prop="className" label="班级" min-width="140" />
                <el-table-column prop="gradeName" label="年级" width="100" />
                <el-table-column prop="enrollYear" label="入学年份" width="100" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="90" align="center">
                  <template #default="{ row }">
                    <el-button size="small" text type="primary" @click="openStudentDialog(row)">编辑</el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div class="pagination">
                <el-pagination
                  v-model:current-page="studentQuery.page"
                  v-model:page-size="studentQuery.size"
                  :total="studentTotal"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next"
                  background
                  @current-change="loadStudents"
                  @size-change="searchStudents"
                />
              </div>
            </el-tab-pane>

            <!-- 班级 -->
            <el-tab-pane label="班级" name="classes">
              <div class="toolbar">
                <span class="hint">共 {{ classes.length }} 个班级</span>
                <div class="spacer" />
                <el-button type="primary" @click="openClassDialog()">新增班级</el-button>
              </div>
              <el-table :data="classes" stripe empty-text="暂无班级">
                <el-table-column prop="name" label="班级" min-width="160" />
                <el-table-column prop="gradeName" label="年级" width="120" />
                <el-table-column prop="studentCount" label="学生数" width="100" />
                <el-table-column label="状态" width="110">
                  <template #default="{ row }">
                    <el-tag :type="row.graduated ? 'info' : 'success'" size="small">
                      {{ row.graduated ? '已毕业' : '在读' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="90" align="center">
                  <template #default="{ row }">
                    <el-button size="small" text type="primary" @click="openClassDialog(row)">编辑</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <!-- 年级 -->
            <el-tab-pane label="年级" name="grades">
              <el-table :data="grades" stripe>
                <el-table-column prop="name" label="年级" width="140" />
                <el-table-column label="学段" width="120">
                  <template #default="{ row }">
                    <el-tag :type="row.stage === 'PRIMARY' ? 'success' : 'warning'" size="small">
                      {{ row.stage === 'PRIMARY' ? '小学' : '初中' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="sortOrder" label="排序" width="100" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row }">{{ row.enabled === 1 ? '启用' : '停用' }}</template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <!-- 学年 -->
            <el-tab-pane label="学年" name="years">
              <div class="toolbar">
                <span class="hint">当前学年用于后续成绩/考试的学期口径</span>
                <div class="spacer" />
                <el-button type="primary" @click="openYearDialog">新增学年</el-button>
              </div>
              <el-table :data="schoolYears" stripe empty-text="暂无学年">
                <el-table-column prop="name" label="学年" width="140" />
                <el-table-column prop="startDate" label="开始日期" width="140" />
                <el-table-column prop="endDate" label="结束日期" width="140" />
                <el-table-column label="当前" width="100">
                  <template #default="{ row }">
                    <el-tag v-if="row.current" type="success" size="small">当前</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120" align="center">
                  <template #default="{ row }">
                    <el-button
                      v-if="!row.current"
                      size="small"
                      text
                      type="primary"
                      @click="handleSetCurrent(row)"
                    >
                      设为当前
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>

            <!-- 升级 / 毕业 -->
            <el-tab-pane label="升级/毕业" name="promotion">
              <el-alert
                v-if="!promotionPreview || !promotionPreview.schoolYear"
                type="warning"
                :closable="false"
                show-icon
                title="请先在「学年」中新增并设为当前学年，才能执行升级/毕业。"
              />

              <template v-else>
                <div class="toolbar">
                  <span class="hint">当前学年：{{ promotionPreview.schoolYear }}</span>
                  <div class="spacer" />
                  <el-button :loading="loadingPreview" @click="loadPromotionPreview">刷新预览</el-button>
                  <el-button
                    type="danger"
                    :loading="executing"
                    :disabled="promotionPreview.promotionExecuted && promotionPreview.graduationExecuted"
                    @click="handleExecutePromotion"
                  >
                    执行升级/毕业
                  </el-button>
                </div>

                <el-row :gutter="16">
                  <el-col :span="12">
                    <el-card shadow="never" class="sub-card">
                      <template #header>
                        <div class="sub-header">
                          <span>升年级</span>
                          <el-tag
                            :type="promotionPreview.promotionExecuted ? 'success' : 'warning'"
                            size="small"
                          >
                            {{
                              promotionPreview.promotionExecuted
                                ? '本学年已执行'
                                : `待升级 ${promotionPreview.promotionStudentCount} 人`
                            }}
                          </el-tag>
                        </div>
                      </template>
                      <el-table :data="promotionPreview.promotions" size="small" empty-text="没有需要升级的班级">
                        <el-table-column prop="className" label="班级" />
                        <el-table-column prop="fromGrade" label="当前年级" width="90" />
                        <el-table-column prop="toGrade" label="目标年级" width="90" />
                        <el-table-column prop="studentCount" label="人数" width="70" />
                      </el-table>
                    </el-card>
                  </el-col>

                  <el-col :span="12">
                    <el-card shadow="never" class="sub-card">
                      <template #header>
                        <div class="sub-header">
                          <span>毕业离校</span>
                          <el-tag
                            :type="promotionPreview.graduationExecuted ? 'success' : 'danger'"
                            size="small"
                          >
                            {{
                              promotionPreview.graduationExecuted
                                ? '本学年已执行'
                                : `待毕业 ${promotionPreview.graduationStudentCount} 人`
                            }}
                          </el-tag>
                        </div>
                      </template>
                      <el-table :data="promotionPreview.graduations" size="small" empty-text="没有需要毕业的班级">
                        <el-table-column prop="className" label="班级" />
                        <el-table-column prop="gradeName" label="年级" width="90" />
                        <el-table-column prop="studentCount" label="人数" width="70" />
                      </el-table>
                    </el-card>
                  </el-col>
                </el-row>

                <el-alert
                  type="info"
                  :closable="false"
                  class="promotion-tip"
                  title="执行后：班级整体升入下一年级并同步改名；初三班级归档、学生状态置为「毕业」。同一学年只能执行一次。"
                />
              </template>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-main>
    </el-container>

    <!-- 班级弹窗 -->
    <el-dialog
      v-model="classDialog.visible"
      :title="classDialog.editing ? '编辑班级' : '新增班级'"
      width="420px"
    >
      <el-form :model="classForm" label-width="80px">
        <el-form-item label="班级名称" required>
          <el-input v-model="classForm.name" placeholder="如：一年级1班" />
        </el-form-item>
        <el-form-item label="年级" required>
          <el-select v-model="classForm.gradeId" placeholder="请选择年级" style="width: 100%">
            <el-option v-for="g in grades" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="classDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveClass">保存</el-button>
      </template>
    </el-dialog>

    <!-- 学生弹窗 -->
    <el-dialog
      v-model="studentDialog.visible"
      :title="studentDialog.editing ? '编辑学生' : '新增学生'"
      width="480px"
    >
      <el-form :model="studentForm" label-width="90px">
        <el-form-item label="姓名" required>
          <el-input v-model="studentForm.realName" placeholder="学生姓名" />
        </el-form-item>
        <el-form-item label="班级" required>
          <el-select v-model="studentForm.classId" placeholder="请选择班级" style="width: 100%">
            <el-option
              v-for="c in classes"
              :key="c.id"
              :label="`${c.gradeName} ${c.name}`"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入学年份" required>
          <el-input-number v-model="studentForm.enrollYear" :min="2000" :max="2100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="学号">
          <el-input
            v-model="studentForm.studentNo"
            :disabled="!!studentDialog.editing"
            placeholder="留空自动生成，如 20260001"
          />
        </el-form-item>
      </el-form>

      <el-alert
        v-if="!studentDialog.editing"
        type="info"
        :closable="false"
        show-icon
        title="新增学生会自动创建登录账号：账号=学号，初始密码 123456，首次登录强制改密。"
      />

      <template #footer>
        <el-button @click="studentDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveStudent">保存</el-button>
      </template>
    </el-dialog>

    <!-- 学年弹窗 -->
    <el-dialog v-model="yearDialog.visible" title="新增学年" width="420px">
      <el-form :model="yearForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="yearForm.name" placeholder="如：2026-2027" />
        </el-form-item>
        <el-form-item label="开始" required>
          <el-date-picker
            v-model="yearForm.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束" required>
          <el-date-picker
            v-model="yearForm.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="设为当前">
          <el-switch v-model="yearForm.current" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="yearDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveYear">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Management, School } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import {
  createClass,
  createSchoolYear,
  createStudent,
  executePromotion,
  getClasses,
  getGrades,
  getPromotionPreview,
  getSchoolYears,
  getStudents,
  setCurrentSchoolYear,
  updateClass,
  updateStudent
} from '@/api/academic'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('students')
const grades = ref([])
const classes = ref([])
const schoolYears = ref([])

const students = ref([])
const studentTotal = ref(0)
const loadingStudents = ref(false)
const studentQuery = reactive({ page: 1, size: 10, keyword: '', classId: null })

const saving = ref(false)

const classDialog = reactive({ visible: false, editing: null })
const classForm = reactive({ name: '', gradeId: null })

const studentDialog = reactive({ visible: false, editing: null })
const studentForm = reactive({
  realName: '',
  classId: null,
  enrollYear: new Date().getFullYear(),
  studentNo: ''
})

const yearDialog = reactive({ visible: false })
const yearForm = reactive({ name: '', startDate: '', endDate: '', current: false })

const promotionPreview = ref(null)
const loadingPreview = ref(false)
const executing = ref(false)

const STATUS_MAP = {
  0: '初始',
  1: '正常',
  2: '请假',
  3: '休学',
  4: '转出',
  5: '离职',
  6: '病假',
  7: '退休',
  8: '毕业',
  9: '锁定',
  10: '密码过期'
}

const avatarText = computed(() => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username || 'U'
  return name.charAt(0).toUpperCase()
})

function statusText(status) {
  return STATUS_MAP[status] ?? status
}

function statusType(status) {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  return 'info'
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

async function loadStudents() {
  loadingStudents.value = true
  try {
    const params = { page: studentQuery.page, size: studentQuery.size }
    if (studentQuery.keyword.trim()) params.keyword = studentQuery.keyword.trim()
    if (studentQuery.classId) params.classId = studentQuery.classId
    const data = await getStudents(params)
    students.value = data.records || []
    studentTotal.value = data.total || 0
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    loadingStudents.value = false
  }
}

function searchStudents() {
  studentQuery.page = 1
  loadStudents()
}

function resetStudentQuery() {
  studentQuery.keyword = ''
  studentQuery.classId = null
  studentQuery.page = 1
  loadStudents()
}

/* ---------- 班级 ---------- */

function openClassDialog(row) {
  classDialog.editing = row || null
  classForm.name = row?.name || ''
  classForm.gradeId = row?.gradeId || classes.value[0]?.gradeId || grades.value[0]?.id || null
  classDialog.visible = true
}

async function saveClass() {
  const name = classForm.name.trim()
  if (!name || !classForm.gradeId) {
    ElMessage.warning('请填写班级名称并选择年级')
    return
  }

  saving.value = true
  try {
    if (classDialog.editing) {
      await updateClass(classDialog.editing.id, { name, gradeId: classForm.gradeId })
    } else {
      await createClass({ name, gradeId: classForm.gradeId })
    }
    ElMessage.success('保存成功')
    classDialog.visible = false
    await loadClasses()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    saving.value = false
  }
}

/* ---------- 学生 ---------- */

function openStudentDialog(row) {
  studentDialog.editing = row || null
  studentForm.realName = row?.realName || ''
  studentForm.classId = row?.classId || classes.value[0]?.id || null
  studentForm.enrollYear = row?.enrollYear || new Date().getFullYear()
  studentForm.studentNo = row?.studentNo || ''
  studentDialog.visible = true
}

async function saveStudent() {
  const realName = studentForm.realName.trim()
  if (!realName || !studentForm.classId) {
    ElMessage.warning('请填写姓名并选择班级')
    return
  }

  saving.value = true
  try {
    if (studentDialog.editing) {
      await updateStudent(studentDialog.editing.id, {
        realName,
        classId: studentForm.classId,
        enrollYear: studentForm.enrollYear
      })
    } else {
      await createStudent({
        realName,
        classId: studentForm.classId,
        enrollYear: studentForm.enrollYear,
        studentNo: studentForm.studentNo.trim() || null
      })
    }
    ElMessage.success('保存成功')
    studentDialog.visible = false
    await Promise.all([loadStudents(), loadClasses()])
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    saving.value = false
  }
}

/* ---------- 学年 ---------- */

function openYearDialog() {
  yearForm.name = ''
  yearForm.startDate = ''
  yearForm.endDate = ''
  yearForm.current = false
  yearDialog.visible = true
}

async function saveYear() {
  if (!yearForm.name.trim() || !yearForm.startDate || !yearForm.endDate) {
    ElMessage.warning('请填写完整学年信息')
    return
  }

  saving.value = true
  try {
    await createSchoolYear({
      name: yearForm.name.trim(),
      startDate: yearForm.startDate,
      endDate: yearForm.endDate,
      current: yearForm.current
    })
    ElMessage.success('保存成功')
    yearDialog.visible = false
    await loadYears()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    saving.value = false
  }
}

async function handleSetCurrent(row) {
  try {
    await ElMessageBox.confirm(`确定把「${row.name}」设为当前学年吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await setCurrentSchoolYear(row.id)
    ElMessage.success('已设为当前学年')
    await loadYears()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  }
}

/* ---------- 升级 / 毕业 ---------- */

async function loadPromotionPreview() {
  loadingPreview.value = true
  try {
    promotionPreview.value = await getPromotionPreview()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    loadingPreview.value = false
  }
}

async function handleExecutePromotion() {
  const preview = promotionPreview.value
  if (!preview?.schoolYear) {
    ElMessage.warning('请先设置当前学年')
    return
  }

  try {
    await ElMessageBox.confirm(
      `将升级 ${preview.promotionStudentCount} 人、毕业 ${preview.graduationStudentCount} 人，执行后不可撤销，确定继续吗？`,
      '执行升级/毕业确认',
      {
        confirmButtonText: '确定执行',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  executing.value = true
  try {
    const result = await executePromotion()
    ElMessage.success(
      `升级 ${result.promotedClasses} 个班 / ${result.promotedStudents} 人；` +
        `毕业 ${result.graduatedClasses} 个班 / ${result.graduatedStudents} 人`
    )
    await Promise.all([loadPromotionPreview(), loadClasses(), loadStudents(), loadYears()])
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    executing.value = false
  }
}

function handleTabChange(name) {
  if (name === 'promotion') {
    loadPromotionPreview()
  }
}

/* ---------- 通用 ---------- */

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
  await Promise.all([loadGrades(), loadClasses(), loadYears(), loadStudents()])
})
</script>

<style scoped>
.academic-page {
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

.academic-card {
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
  margin-bottom: 12px;
}

.spacer {
  flex: 1;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.sub-card {
  height: 100%;
}

.sub-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.promotion-tip {
  margin-top: 16px;
}
</style>
