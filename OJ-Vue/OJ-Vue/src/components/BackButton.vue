<template>
  <el-button 
    :icon="ArrowLeft"
    @click="goBack"
    type="text"
    class="back-button"
  >
    返回
  </el-button>
</template>

<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.back-button {
  margin: 10px;
}
</style> 