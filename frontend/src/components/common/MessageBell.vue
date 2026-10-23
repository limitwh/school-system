<template>
  <el-badge
    :value="messageStore.unreadTotal"
    :max="99"
    :hidden="!messageStore.unreadTotal"
    class="message-bell"
  >
    <el-button text circle @click="$router.push('/messages')">
      <el-icon :size="20"><ChatDotRound /></el-icon>
    </el-button>
  </el-badge>
</template>

<script setup>
import { onMounted } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import { useMessageStore } from '@/store/message'

const messageStore = useMessageStore()

onMounted(() => {
  messageStore.refreshUnread()
})
</script>

<style scoped>
.message-bell {
  margin-right: 8px;
}
</style>
