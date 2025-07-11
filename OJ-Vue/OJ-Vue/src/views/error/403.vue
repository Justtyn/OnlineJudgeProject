<template>
  <div class="forbidden-container">
    <el-result
      icon="error"
      title="403"
      sub-title="对不起，您没有权限访问此页面"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.forbidden-container {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}
</style> 