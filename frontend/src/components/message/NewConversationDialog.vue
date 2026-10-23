<template>
  <el-dialog
    :model-value="modelValue"
    title="新建会话"
    width="420px"
    @update:model-value="emit('update:modelValue', $event)"
    @open="loadContacts"
  >
    <el-input
      v-model="keyword"
      placeholder="搜索姓名 / 登录名"
      clearable
      :prefix-icon="Search"
      @input="onSearch"
    />

    <div class="contact-list" v-loading="loading">
      <div v-for="u in contacts" :key="u.userId" class="contact-item" @click="select(u)">
        <el-avatar :size="32" class="contact-avatar">{{ initial(u) }}</el-avatar>
        <div class="contact-info">
          <div class="name">{{ u.realName }}</div>
          <div class="account">{{ u.username }}</div>
        </div>
      </div>
      <el-empty
        v-if="!loading && !contacts.length"
        description="没有找到用户"
        :image-size="60"
      />
    </div>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getContacts } from '@/api/message'

defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'select'])

const keyword = ref('')
const contacts = ref([])
const loading = ref(false)

let searchTimer = null

function initial(user) {
  const name = user.realName || user.username || '?'
  return name.charAt(0).toUpperCase()
}

async function loadContacts() {
  loading.value = true
  try {
    contacts.value = await getContacts(keyword.value.trim() || undefined)
  } catch (e) {
    // 错误提示已由 axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}

function onSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(loadContacts, 300)
}

function select(user) {
  emit('select', user.userId)
  keyword.value = ''
  emit('update:modelValue', false)
}
</script>

<style scoped>
.contact-list {
  margin-top: 12px;
  max-height: 360px;
  overflow-y: auto;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
}

.contact-item:hover {
  background: #f5f7fa;
}

.contact-avatar {
  background: #1f6feb;
  color: #fff;
}

.contact-info {
  display: flex;
  flex-direction: column;
  line-height: 1.4;
}

.name {
  font-size: 14px;
  color: #303133;
}

.account {
  font-size: 12px;
  color: #909399;
}
</style>
