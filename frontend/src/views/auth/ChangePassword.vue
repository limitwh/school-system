<template>
  <div class="change-password-container">
    <div class="change-password-card">
      <div class="card-header">
        <div class="logo">
          <el-icon :size="38" color="#1f6feb"><School /></el-icon>
        </div>
        <h2 class="title">修改密码</h2>
        <p v-if="isExpired" class="subtitle warning">
          <el-icon><Warning /></el-icon>
          您的密码已过期，请设置新密码（新密码生效后 90 天内有效）
        </p>
        <p v-else class="subtitle">
          欢迎 <strong>{{ realName }}</strong>，请设置新密码
        </p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @keyup.enter="handleSubmit"
      >
        <el-form-item prop="oldPassword">
          <el-input
            v-model="form.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码（6-32 位）"
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-button
          type="primary"
          class="submit-btn"
          size="large"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ loading ? '提交中...' : '确认修改' }}
        </el-button>
      </el-form>

      <div class="footer">
        <el-link type="info" underline="never" @click="handleLogout">退出登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Lock, School, Warning } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { changePassword } from '@/api/auth'
import { getHomePath } from '@/utils/role'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const realName = computed(
  () => userStore.userInfo?.realName || userStore.userInfo?.username || ''
)

// 密码过期（状态 10）时展示强制改密提示
const isExpired = computed(() => userStore.userInfo?.status === 10)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirm, trigger: 'blur' }]
}

async function handleSubmit() {
  if (loading.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })
    // 状态恢复为正常并跳转角色首页
    userStore.markNormal()
    ElMessage.success('密码修改成功')
    router.push(getHomePath(userStore.roles))
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}

function handleLogout() {
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
</script>

<style scoped>
.change-password-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f6feb 0%, #0b57d0 50%, #094a9e 100%);
  padding: 20px;
}

.change-password-card {
  width: 440px;
  max-width: 100%;
  padding: 40px 40px 28px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.card-header {
  text-align: center;
  margin-bottom: 28px;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 68px;
  height: 68px;
  border-radius: 18px;
  background: #e8f1fe;
  margin-bottom: 16px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
}

.subtitle.warning {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #e6a23c;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
}

.footer {
  margin-top: 20px;
  text-align: center;
}
</style>
