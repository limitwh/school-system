<template>
  <div class="reset-user-password">
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

      <!-- 主内容：按登录名重置密码 -->
      <el-main class="main">
        <el-card shadow="never" class="reset-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">
                <el-icon color="#1f6feb"><Key /></el-icon>
                <span>重置用户密码</span>
              </div>
            </div>
          </template>

          <p class="tip">
            输入用户的<strong>登录名</strong>（用户名 / 学号 / 职工号），系统会生成一个
            <strong>一次性随机密码</strong>并显示在下方；该密码有有效期，过期需重新重置，
            用户下次登录时将被强制设置新密码。
          </p>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="reset-form"
            @submit.prevent
          >
            <el-form-item prop="loginName">
              <el-input
                v-model="form.loginName"
                size="large"
                clearable
                placeholder="请输入用户登录名，如 student-test1"
                :prefix-icon="User"
                @keyup.enter="handleReset"
              />
            </el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleReset"
            >
              {{ loading ? '重置中...' : '重置密码' }}
            </el-button>
          </el-form>

          <!-- 重置结果 -->
          <el-alert
            v-if="result"
            type="success"
            :closable="false"
            show-icon
            class="result"
          >
            <template #title>
              已重置「{{ result.realName }}」的密码（登录名：{{ result.username }}）
            </template>
            <div class="result-body">
              <span>
                一次性密码：
                <strong class="pwd">{{ result.oneTimePassword }}</strong>
              </span>
              <el-button size="small" text type="primary" @click="copyPassword">
                复制
              </el-button>
            </div>
            <div class="result-tip">
              该密码只显示这一次，请立即复制并告知用户；页面刷新后将无法再次查看。
              密码 {{ result.validHours }} 小时内有效，用户下次登录时必须设置新密码。
            </div>
          </el-alert>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Key, School, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { resetPasswordByLoginName } from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const result = ref(null)

const form = reactive({
  loginName: ''
})

const rules = {
  loginName: [{ required: true, message: '请输入用户登录名', trigger: 'blur' }]
}

const avatarText = computed(() => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username || 'U'
  return name.charAt(0).toUpperCase()
})

async function handleReset() {
  if (loading.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const loginName = form.loginName.trim()

  try {
    await ElMessageBox.confirm(
      `确定要重置用户「${loginName}」的密码吗？系统会生成一个一次性随机密码，该用户需重新登录并设置新密码。`,
      '重置密码确认',
      {
        confirmButtonText: '确定重置',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  loading.value = true
  try {
    result.value = await resetPasswordByLoginName({ loginName })
    ElMessage.success('密码已重置')
    form.loginName = ''
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
    result.value = null
  } finally {
    loading.value = false
  }
}

async function copyPassword() {
  if (!result.value) return
  try {
    await navigator.clipboard.writeText(result.value.oneTimePassword)
    ElMessage.success('初始密码已复制')
  } catch {
    ElMessage.warning('复制失败，请手动选中复制')
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
</script>

<style scoped>
.reset-user-password {
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

.reset-card {
  width: 100%;
  max-width: 760px;
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

.tip {
  margin-bottom: 20px;
  font-size: 13px;
  color: #606266;
  line-height: 1.7;
}

.reset-form {
  max-width: 560px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
}

.result {
  margin-top: 24px;
}

.result-body {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.pwd {
  font-family: Consolas, Monaco, monospace;
  font-size: 18px;
  color: #1f6feb;
}

.result-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}
</style>
