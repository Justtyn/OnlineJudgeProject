<template>
  <div class="back-button" @click="handleBack">
    <el-button 
      :icon="ArrowLeft"
      type="text"
      size="large"
      class="back-btn"
    >
      返回
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const props = defineProps({
  fallbackPath: {
    type: String,
    default: '/'
  }
})

const handleBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push(props.fallbackPath)
  }
}
</script>

<style scoped>
.back-button {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
}

.back-btn {
  font-size: 16px;
  padding: 0;
  height: auto;
}

.back-btn:hover {
  color: var(--el-color-primary);
}
</style> 