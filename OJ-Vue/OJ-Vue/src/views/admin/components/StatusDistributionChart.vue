<template>
  <el-card class="chart-card">
    <template #header>
      <div class="chart-header">
        <h3>提交状态分布</h3>
        <div class="chart-actions">
          <el-tooltip content="查看详情" placement="top">
            <el-button type="text" icon="el-icon-more" />
          </el-tooltip>
        </div>
      </div>
    </template>
    <div class="chart-content">
      <canvas ref="chartRef"></canvas>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import Chart from 'chart.js/auto'
import request from '@/utils/request'

const chartRef = ref(null)
let chart = null

async function initChart() {
  const res = await request.get('/stats/status/distribution')
  const arr = res.data.data || []
  const labels = arr.map(item => item.status)
  const counts = arr.map(item => item.count)
  
  chart = new Chart(chartRef.value.getContext('2d'), {
    type: 'polarArea',
    data: {
      labels,
      datasets: [{
        data: counts,
        backgroundColor: [
          'rgba(255, 99, 132, 0.8)',
          'rgba(54, 162, 235, 0.8)',
          'rgba(255, 206, 86, 0.8)',
          'rgba(75, 192, 192, 0.8)',
          'rgba(153, 102, 255, 0.8)'
        ]
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: {
        animateRotate: true,
        animateScale: true
      },
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 20,
            usePointStyle: true
          }
        }
      }
    }
  })
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', () => chart?.resize())
})

onUnmounted(() => {
  window.removeEventListener('resize', () => {})
  chart?.destroy()
})
</script>

<style scoped>
.chart-card {
  height: 100%;
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.chart-content {
  height: 300px;
  position: relative;
}
</style> 