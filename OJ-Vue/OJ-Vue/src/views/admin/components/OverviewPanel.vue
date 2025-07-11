<template>
  <div class="overview-panel">
    <el-row :gutter="20">
      <el-col v-for="stat in stats" :key="stat.key" :xs="24" :sm="12" :md="8" :lg="3">
        <el-card class="stat-card">
          <template #header>
            <i :class="stat.icon" /> {{ stat.title }}
          </template>
          <div class="stat-body">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-unit">{{ stat.unit }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const stats = ref([])

async function loadOverview() {
  const res = await request.get('/analysis/overview')
  console.log('OverviewData:', res.data)
  if (res.data.code === '200') {
    const d = res.data.data
    stats.value = [
      { key: 'totalStudents',      title: '学生总数',       icon: 'el-icon-user',             value: d.totalStudents,      unit: '人' },
      { key: 'totalProblems',      title: '题目总数',       icon: 'el-icon-notebook-1',       value: d.totalProblems,      unit: '道' },
      { key: 'totalSubmissions',   title: '提交总数',       icon: 'el-icon-document',         value: d.totalSubmissions,   unit: '次' },
      { key: 'totalAnnouncements', title: '公告总数',       icon: 'el-icon-bell',             value: d.totalAnnouncements, unit: '条' },
      { key: 'totalDiscussions',   title: '讨论总数',       icon: 'el-icon-chat-dot-square',  value: d.totalDiscussions,   unit: '条' },
      { key: 'totalHomeworks',     title: '作业总数',       icon: 'el-icon-tickets',          value: d.totalHomeworks,     unit: '份' },
      { key: 'totalClasses',       title: '班级数量',       icon: 'el-icon-school',           value: d.totalClasses,       unit: '个' },
      { key: 'totalSolutions',     title: '题解总数',       icon: 'el-icon-folder-add',       value: d.totalSolutions,     unit: '篇' }
    ]
  }
}

onMounted(loadOverview)
</script>

<style scoped>
.overview-panel {
  margin-bottom: 20px;
}
.stat-card {
  background: #ffffff;
  border-left: 5px solid #409EFF;
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.stat-card :deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  background: #f5f7fa;
}
.stat-card :deep(.el-card__body) {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.stat-body {
  display: flex;
  align-items: baseline;
  justify-content: center;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.stat-unit {
  margin-left: 4px;
  font-size: 14px;
  color: #909399;
}
.stat-card :deep(.el-icon) {
  margin-right: 8px;
  font-size: 16px;
  color: #409EFF;
}
</style>