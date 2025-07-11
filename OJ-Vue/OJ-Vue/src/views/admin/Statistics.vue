<template>
  <div class="statistics">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>用户注册趋势</span>
            </div>
          </template>
          <div class="chart" ref="registrationChart"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>提交状态分布</span>
            </div>
          </template>
          <div class="chart" ref="statusChart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const registrationChart = ref(null)
const statusChart = ref(null)

onMounted(async () => {
  // 初始化图表
  const regChart = echarts.init(registrationChart.value)
  const statChart = echarts.init(statusChart.value)
  
  // 获取数据
  const [regData, statusData] = await Promise.all([
    getRegistrationTrend(),
    getStatusDistribution()
  ])
  
  // 渲染注册趋势图
  regChart.setOption({
    title: { text: '用户注册趋势' },
    xAxis: {
      type: 'category',
      data: regData.map(item => item.date)
    },
    yAxis: { type: 'value' },
    series: [{
      data: regData.map(item => item.count),
      type: 'line'
    }]
  })
  
  // 渲染状态分布图
  statChart.setOption({
    title: { text: '提交状态分布' },
    series: [{
      type: 'pie',
      data: statusData.map(item => ({
        name: item.status,
        value: item.count
      }))
    }]
  })
})
</script>

<style scoped>
.statistics {
  padding: 20px;
}

.chart {
  height: 400px;
}

.card-header {
  font-weight: bold;
}
</style> 