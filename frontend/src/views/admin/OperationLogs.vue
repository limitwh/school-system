<template>
  <div class="operation-logs">
    <el-container class="container">
      <!-- 顶部导航栏 -->
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
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <el-card shadow="never" class="log-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon color="#1f6feb"><Document /></el-icon>
                <span>操作日志</span>
              </div>
              <div class="card-actions">
                <el-tag type="info" effect="light" size="small">共 {{ total }} 条</el-tag>
                <el-button size="small" :loading="archiving" @click="handleArchive">
                  <el-icon style="margin-right: 4px"><Download /></el-icon>
                  立即归档
                </el-button>
              </div>
            </div>
          </template>

          <!-- 筛选条件 -->
          <el-form :inline="true" class="filter-form" @submit.prevent>
            <el-form-item label="操作人">
              <el-input
                v-model="query.username"
                placeholder="登录名 / 姓名"
                clearable
                style="width: 170px"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="模块">
              <el-select v-model="query.module" clearable placeholder="全部" style="width: 130px">
                <el-option v-for="m in MODULE_OPTIONS" :key="m.value" :label="m.label" :value="m.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="操作">
              <el-select v-model="query.action" clearable placeholder="全部" style="width: 190px">
                <el-option v-for="a in ACTION_OPTIONS" :key="a.value" :label="a.label" :value="a.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="结果">
              <el-select v-model="query.success" clearable placeholder="全部" style="width: 110px">
                <el-option label="成功" value="true" />
                <el-option label="失败" value="false" />
              </el-select>
            </el-form-item>
            <el-form-item label="时间">
              <el-date-picker
                v-model="query.dateRange"
                type="datetimerange"
                value-format="YYYY-MM-DDTHH:mm:ss"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                style="width: 340px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 日志列表 -->
          <el-table :data="records" v-loading="loading" stripe empty-text="暂无操作日志">
            <el-table-column label="时间" width="170">
              <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作人" min-width="150">
              <template #default="{ row }">
                <div class="operator">
                  <span class="operator-name">{{ row.realName || row.username }}</span>
                  <span class="operator-account">{{ row.username }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="模块" width="110">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">{{ moduleText(row.module) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="170">
              <template #default="{ row }">{{ actionText(row.action) }}</template>
            </el-table-column>
            <el-table-column label="结果" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.success ? 'success' : 'danger'" size="small">
                  {{ row.success ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="detail" label="详情" min-width="220" show-overflow-tooltip />
            <el-table-column prop="ip" label="IP" width="130" />
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="query.page"
              v-model:page-size="query.size"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @current-change="load"
              @size-change="handleSizeChange"
            />
          </div>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Document, Download, School } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { archiveLogs, getOperationLogs } from '@/api/log'

const router = useRouter()
const userStore = useUserStore()

const MODULE_OPTIONS = [
  { value: 'AUTH', label: '认证' },
  { value: 'USER', label: '用户管理' }
]

const ACTION_OPTIONS = [
  { value: 'LOGIN', label: '登录' },
  { value: 'LOGOUT', label: '退出登录' },
  { value: 'SET_PASSWORD', label: '设置初始密码' },
  { value: 'CHANGE_PASSWORD', label: '修改密码' },
  { value: 'RESET_PASSWORD', label: '重置锁定用户密码' },
  { value: 'RESET_PASSWORD_BY_NAME', label: '按登录名重置用户密码' }
]

const MODULE_MAP = Object.fromEntries(MODULE_OPTIONS.map((m) => [m.value, m.label]))
const ACTION_MAP = Object.fromEntries(ACTION_OPTIONS.map((a) => [a.value, a.label]))

const loading = ref(false)
const archiving = ref(false)
const records = ref([])
const total = ref(0)

const query = reactive({
  page: 1,
  size: 20,
  username: '',
  module: '',
  action: '',
  success: '',
  dateRange: []
})

const avatarText = computed(() => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username || 'U'
  return name.charAt(0).toUpperCase()
})

function moduleText(code) {
  return MODULE_MAP[code] || code
}

function actionText(code) {
  return ACTION_MAP[code] || code
}

function formatTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

async function load() {
  loading.value = true
  try {
    const params = {
      page: query.page,
      size: query.size
    }
    if (query.username.trim()) params.username = query.username.trim()
    if (query.module) params.module = query.module
    if (query.action) params.action = query.action
    if (query.success !== '') params.success = query.success === 'true'
    if (query.dateRange?.length === 2) {
      params.startTime = query.dateRange[0]
      params.endTime = query.dateRange[1]
    }

    const data = await getOperationLogs(params)
    records.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  load()
}

function handleSizeChange() {
  query.page = 1
  load()
}

function handleReset() {
  query.username = ''
  query.module = ''
  query.action = ''
  query.success = ''
  query.dateRange = []
  query.page = 1
  load()
}

async function handleArchive() {
  try {
    await ElMessageBox.confirm(
      '将把超过保留期（默认 30 天）的日志导出为文件，并从数据库中删除，确定执行吗？',
      '日志归档确认',
      {
        confirmButtonText: '执行归档',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  archiving.value = true
  try {
    const result = await archiveLogs()
    if (result.archivedCount > 0) {
      ElMessage.success(`已归档 ${result.archivedCount} 条日志，文件：${result.filePath}`)
    } else {
      ElMessage.info('没有超过保留期的日志，无需归档')
    }
    await load()
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    archiving.value = false
  }
}

function handleCommand(command) {
  if (command === 'logout') {
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

onMounted(load)
</script>

<style scoped>
.operation-logs {
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

.log-card {
  width: 100%;
  max-width: 1280px;
  margin: 24px 0;
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-form {
  margin-bottom: 4px;
}

.operator {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.operator-name {
  color: #303133;
  font-weight: 500;
}

.operator-account {
  font-size: 12px;
  color: #909399;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
