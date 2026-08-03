<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 左侧品牌区 -->
      <div class="brand-panel">
        <div class="brand-logo">
          <el-icon :size="40"><School /></el-icon>
        </div>
        <h1 class="brand-title">校务管理系统</h1>
        <p class="brand-subtitle">School Management System</p>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-panel">
        <h2 class="form-title">欢迎登录</h2>
        <p class="form-subtitle">请使用您的账号登录系统</p>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名 / 学号 / 职工号"
              :prefix-icon="User"
              clearable
              autofocus
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="remember">记住账号</el-checkbox>
            <el-link type="primary" underline="never" @click="handleForgot">
              忘记密码？
            </el-link>
          </div>

          <el-button
            type="primary"
            class="login-btn"
            size="large"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, School } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getHomePath } from '@/utils/role'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const remember = ref(true)

const loginForm = reactive({
  username: '',
  password: ''
})

// 读取记住的账号
onMounted(() => {
  const saved = localStorage.getItem('school_system_remember')
  if (saved) {
    loginForm.username = saved
    remember.value = true
  }
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (loading.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({ ...loginForm })
    if (remember.value) {
      localStorage.setItem('school_system_remember', loginForm.username)
    } else {
      localStorage.removeItem('school_system_remember')
    }
    ElMessage.success('登录成功')
    const status = userStore.userInfo?.status
    // 初始状态(0)用户：首次登录需设置初始密码
    if (status === 0) {
      router.push('/set-password')
      return
    }
    // 密码过期(10)用户：强制修改密码
    if (status === 10) {
      router.push('/change-password')
      return
    }
    // 优先跳转登录前访问的页面，否则按角色进入对应首页
    const redirect = route.query.redirect || getHomePath(userStore.roles)
    router.push(redirect)
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}

function handleForgot() {
  ElMessage.info('请联系系统管理员重置密码')
}
</script>

<style scoped>
.login-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f6feb 0%, #0b57d0 50%, #094a9e 100%);
  padding: 20px;
}

.login-card {
  display: flex;
  width: 860px;
  max-width: 100%;
  min-height: 520px;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.brand-panel {
  flex: 1;
  padding: 48px 40px;
  color: #fff;
  background: linear-gradient(160deg, #2d7ff9, #0b57d0);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.18);
  margin-bottom: 24px;
}

.brand-title {
  font-size: 30px;
  font-weight: 700;
  letter-spacing: 2px;
  margin-bottom: 8px;
}

.brand-subtitle {
  font-size: 14px;
  opacity: 0.8;
  letter-spacing: 1px;
  margin-bottom: 40px;
}

.form-panel {
  flex: 1.1;
  padding: 56px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-title {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 6px;
}

.form-subtitle {
  font-size: 14px;
  color: #909399;
  margin-bottom: 32px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 6px;
}

.form-footer {
  margin-top: 28px;
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
}

@media (max-width: 720px) {
  .brand-panel {
    display: none;
  }
  .login-card {
    width: 100%;
  }
}
</style>
