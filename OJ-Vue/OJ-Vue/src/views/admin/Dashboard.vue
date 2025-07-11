<template>
  <div class="dashboard-container">
    <overview-panel />

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12"><submission-chart /></el-col>
      <el-col :span="12"><language-chart /></el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12"><discussion-chart /></el-col>
      <el-col :span="12"><quality-radar /></el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12"><performance-chart /></el-col>
      <el-col :span="12"><activity-chart /></el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12"><registration-trend-chart /></el-col>
      <el-col :span="12"><class-distribution-chart /></el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12"><top-submit-chart /></el-col>
      <el-col :span="12"><status-distribution-chart /></el-col>
    </el-row>
  </div>
</template>

<script setup>
import OverviewPanel from './components/OverviewPanel.vue'
import SubmissionChart from './components/SubmissionChart.vue'
import LanguageChart from './components/LanguageChart.vue'
import PerformanceChart from './components/PerformanceChart.vue'
import ActivityChart from './components/ActivityChart.vue'
import DiscussionChart from './components/DiscussionChart.vue'
import QualityRadar from './components/QualityRadar.vue'
import RegistrationTrendChart from './components/RegistrationTrendChart.vue'
import ClassDistributionChart from './components/ClassDistributionChart.vue'
import TopSubmitChart from './components/TopSubmitChart.vue'
import StatusDistributionChart from './components/StatusDistributionChart.vue'
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa;
}
.chart-row {
  margin-top: 20px;
}
</style>