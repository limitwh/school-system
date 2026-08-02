<template>
  <div class="home">
    <el-container class="container">
      <el-header class="header">
        <div class="header-left">
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
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <el-card shadow="never" class="welcome-card">
          <template #header>
            <span>欢迎回来 👋</span>
          </template>
          <p class="line">
            当前用户：<strong>{{ userStore.userInfo?.realName }}</strong>
            （{{ userStore.userInfo?.username }}）
          </p>
          <p class="line">
            角色：
            <el-tag v-for="r in userStore.roles" :key="r" class="role-tag" size="small" effect="plain">
              {{ r }}
            </el-tag>
          </p>
          <el-divider />
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

const router = useRouter()
const userStore = useUserStore()

const avatarText = computed(() => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username || 'U'
  return name.charAt(0).toUpperCase()
})

onMounted(() => {
  // 本地无用户信息时向后端拉取
  if (!userStore.userInfo) {
    userStore.fetchMe().catch(() => {})
  }
})

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
</script>

<style scoped>
.home {
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
}

.welcome-card {
  max-width: 800px;
  margin: 24px auto;
}

.line {
  margin-bottom: 12px;
  font-size: 15px;
}

.role-tag {
  margin-right: 6px;
}
</style>
