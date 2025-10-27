<template>
  <el-card class="chart-card">
    <template #header>
      <div class="chart-header">
        <h3>用户注册趋势</h3>
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
  const res = await request.get('/stats/user/registration-trend?days=3000')
  const arr = res.data.data || []
  const labels = arr.map(item => item.date)
  const counts = arr.map(item => item.count)
  
  chart = new Chart(chartRef.value.getContext('2d'), {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: '注册人数',
        data: counts,
        tension: 0.4,
        fill: true,
        backgroundColor: 'rgba(54, 162, 235, 0.1)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 2,
        pointBackgroundColor: 'rgba(54, 162, 235, 1)',
        pointRadius: 4,
        pointHoverRadius: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: {
        duration: 2000,
        easing: 'easeInOutQuart'
      },
      scales: {
        x: {
          title: { display: true, text: '日期' },
          grid: { display: false }
        },
        y: {
          title: { display: true, text: '人数' },
          beginAtZero: true,
          grid: { color: 'rgba(0, 0, 0, 0.05)' }
        }
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          mode: 'index',
          intersect: false,
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          titleColor: 'var(--color-heading)',
          bodyColor: 'var(--color-text)',
          borderColor: 'var(--color-text)',
          borderWidth: 1
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
  border: none;
  box-shadow: none;
}

.chart-card > .el-card__header {
  border: none;
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