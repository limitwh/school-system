<template>
  <div class="dashboard">
    <el-container class="container">
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon :size="22" color="#1f6feb"><School /></el-icon>
          <span class="logo-text">校务管理系统</span>
        </div>
        <div class="header-right">
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

      <!-- 主内容：欢迎卡片 -->
      <el-main class="main">
        <el-card shadow="never" class="welcome-card">
          <div class="welcome-hero">
            <div class="hero-icon">
              <el-icon :size="44"><School /></el-icon>
            </div>
            <h2 class="welcome-title">
              欢迎{{ realName }}<span class="role-suffix">{{ roleSuffix }}</span>
            </h2>
            <p class="welcome-sub">欢迎登录校务管理系统，祝您使用愉快！</p>
          </div>

          <el-divider />

          <div class="info-grid">
            <div class="info-item">
              <span class="label">当前用户</span>
              <span class="value">{{ realName }}</span>
            </div>
            <div class="info-item">
              <span class="label">登录账号</span>
              <span class="value">{{ userStore.userInfo?.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">角色</span>
              <span class="value">
                <el-tag v-for="r in userStore.roles" :key="r" class="role-tag" size="small" effect="plain">
                  {{ roleTagText(r) }}
                </el-tag>
              </span>
            </div>
          </div>

          <el-divider />

          <!-- 业务功能入口（由各角色页面通过具名插槽提供） -->
          <slot name="actions" />

          <el-empty description="业务功能开发中，敬请期待" />
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { ROLE_MAP } from '@/utils/role'

// 欢迎语称呼后缀：同学 / 老师 / 校长 / 管理员 / 年级管理员
const props = defineProps({
  roleSuffix: {
    type: String,
    default: ''
  }
})

const router = useRouter()
const userStore = useUserStore()

const realName = computed(
  () => userStore.userInfo?.realName || userStore.userInfo?.username || ''
)

const avatarText = computed(() => {
  const name = realName.value || 'U'
  return name.charAt(0).toUpperCase()
})

onMounted(() => {
  // 本地无用户信息时向后端拉取
  if (!userStore.userInfo) {
    userStore.fetchMe().catch(() => {})
  }
})

// 角色编码 → 中文角色名
function roleTagText(code) {
  return ROLE_MAP[code]?.name || code
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
</script>

<style scoped>
.dashboard {
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

.welcome-card {
  width: 100%;
  max-width: 820px;
  margin: 32px 0;
  border-radius: 12px;
}

.welcome-hero {
  text-align: center;
  padding: 12px 0 8px;
}

.hero-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 84px;
  height: 84px;
  border-radius: 24px;
  color: #1f6feb;
  background: linear-gradient(135deg, #e8f1fe, #d6e6fd);
  margin-bottom: 20px;
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.role-suffix {
  color: #1f6feb;
}

.welcome-sub {
  font-size: 14px;
  color: #909399;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 8px 0;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.label {
  font-size: 13px;
  color: #909399;
}

.value {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
}

.role-tag {
  margin-right: 6px;
}

@media (max-width: 640px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
