<template>
  <el-card class="chart-card">
    <template #header>
      <div class="chart-header">
        <h3>题目提交排行</h3>
        <div class="chart-actions">
          <el-select v-model="limit" size="small" @change="refreshChart">
            <el-option label="Top 5" :value="5" />
            <el-option label="Top 10" :value="10" />
            <el-option label="Top 20" :value="20" />
          </el-select>
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
const limit = ref(10)
let chart = null

async function initChart() {
  const res = await request.get(`/stats/problem/top-submit?limit=${limit.value}`)
  const arr = res.data.data || []
  const labels = arr.map(item => item.problemName)
  const counts = arr.map(item => item.submitCount)
  
  chart = new Chart(chartRef.value.getContext('2d'), {
    type: 'bar',
    data: {
      labels,
      datasets: [{
        label: '提交次数',
        data: counts,
        backgroundColor: 'rgba(75, 192, 192, 0.8)',
        borderRadius: 6,
        barPercentage: 0.6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: {
        duration: 2000,
        easing: 'easeOutQuart'
      },
      scales: {
        x: {
          title: { display: true, text: '题目' },
          grid: { display: false }
        },
        y: {
          title: { display: true, text: '提交次数' },
          beginAtZero: true,
          grid: { color: 'rgba(0, 0, 0, 0.05)' }
        }
      },
      plugins: {
        legend: { display: false }
      }
    }
  })
}

function refreshChart() {
  chart?.destroy()
  initChart()
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