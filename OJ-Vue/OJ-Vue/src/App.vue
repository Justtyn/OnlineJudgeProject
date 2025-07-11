<template>
  <router-view v-slot="{ Component }">
    <keep-alive :include="cachedViews">
      <component :is="Component" />
    </keep-alive>
  </router-view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 需要缓存的页面组件名称
const cachedViews = ref([
  'ProblemListPage',
  'StatusListPage',
  'SolutionPage',
  'DiscussPage'
])
</script>
