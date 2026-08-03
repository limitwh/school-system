<template>
  <div class="reset-password">
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

      <!-- 主内容：锁定用户列表 -->
      <el-main class="main">
        <el-card shadow="never" class="list-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon color="#f56c6c"><Lock /></el-icon>
                <span>锁定用户管理</span>
              </div>
              <div class="card-actions">
                <el-tag type="danger" effect="light" size="small">
                  已锁定 {{ users.length }} 人
                </el-tag>
                <el-button size="small" :loading="loading" @click="load">刷新</el-button>
              </div>
            </div>
          </template>

          <el-table
            :data="users"
            v-loading="loading"
            empty-text="暂无锁定用户"
            stripe
          >
            <el-table-column prop="realName" label="姓名" min-width="120" />
            <el-table-column prop="username" label="登录名" min-width="140" />
            <el-table-column prop="loginNo" label="登录号" min-width="140" />
            <el-table-column label="最近登录时间" min-width="180">
              <template #default="{ row }">
                {{ row.lastLoginTime ? formatTime(row.lastLoginTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="openReset(row)">
                  重置密码
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-main>
    </el-container>

    <!-- 重置密码弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="重置密码"
      width="420px"
      :close-on-click-modal="false"
    >
      <p class="dialog-tip">
        将为 <strong>{{ currentUser?.realName }}</strong>（{{ currentUser?.username }}）
        分配初始密码，其状态将置为「初始」，首次登录需重新设置密码。
      </p>
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="90px">
        <el-form-item label="初始密码" prop="newPassword">
          <el-input
            v-model="resetForm.newPassword"
            type="password"
            show-password
            placeholder="6-32 位"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleReset">
          确认重置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Lock, School } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getLockedUsers, resetUserPassword } from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()

const users = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const submitting = ref(false)
const currentUser = ref(null)
const resetFormRef = ref(null)
const resetForm = ref({
  newPassword: '',
  confirmPassword: ''
})

const avatarText = computed(() => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username || 'U'
  return name.charAt(0).toUpperCase()
})

function formatTime(time) {
  // 后端返回 ISO 时间，转为本地可读格式
  const d = new Date(time)
  if (Number.isNaN(d.getTime())) return time
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function load() {
  loading.value = true
  try {
    users.value = await getLockedUsers()
  } catch (e) {
    // 错误提示已由拦截器统一处理
  } finally {
    loading.value = false
  }
}

const validateConfirm = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== resetForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const resetRules = {
  newPassword: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirm, trigger: 'blur' }]
}

function openReset(row) {
  currentUser.value = row
  resetForm.value = { newPassword: '', confirmPassword: '' }
  dialogVisible.value = true
}

async function handleReset() {
  if (submitting.value) return
  const valid = await resetFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await resetUserPassword(currentUser.value.id, {
      newPassword: resetForm.value.newPassword
    })
    ElMessage.success(`已重置 ${currentUser.value.realName} 的密码`)
    dialogVisible.value = false
    await load()
  } catch (e) {
    // 错误提示已由拦截器统一处理
  } finally {
    submitting.value = false
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
        router.push('/login')
      })
      .catch(() => {})
  }
}

onMounted(load)
</script>

<style scoped>
.reset-password {
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

.list-card {
  width: 100%;
  max-width: 900px;
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

.dialog-tip {
  margin-bottom: 16px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
</style>
