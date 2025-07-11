<template>
  <div class="page-layout">
    <div class="page-header" v-if="showHeader">
      <div class="header-left">
        <back-button :fallback-path="fallbackPath" v-if="showBack" />
        <h2 class="page-title">{{ title }}</h2>
      </div>
      <div class="header-right">
        <slot name="header-right"></slot>
      </div>
    </div>
    <div class="page-content">
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import BackButton from '@/components/common/BackButton.vue'

defineProps({
  title: {
    type: String,
    default: ''
  },
  showBack: {
    type: Boolean,
    default: true
  },
  showHeader: {
    type: Boolean,
    default: true
  },
  fallbackPath: {
    type: String,
    default: '/'
  }
})
</script>

<style scoped>
.page-layout {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.page-content {
  flex: 1;
  padding: 20px;
  overflow: auto;
}
</style> 