<template>
  <WelcomeDashboard role-suffix="管理员">
    <template #actions>
      <div class="admin-actions">
        <el-button type="primary" plain @click="$router.push('/admin/reset-password')">
          <el-icon style="margin-right: 6px"><Lock /></el-icon>
          锁定用户管理
        </el-button>
      </div>
    </template>
  </WelcomeDashboard>
</template>

<script setup>
import WelcomeDashboard from '@/components/common/WelcomeDashboard.vue'
</script>

<style scoped>
.admin-actions {
  display: flex;
  justify-content: center;
  padding: 4px 0 16px;
}
</style>
