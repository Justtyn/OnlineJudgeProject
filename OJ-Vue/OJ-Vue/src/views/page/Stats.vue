<template>
  <div class="stats-dashboard">
    <div class="dashboard-header">
      <h2>数据统计中心</h2>
      <div class="time-filter">
        <el-select v-model="days" @change="refreshCharts">
          <el-option label="最近7天" :value="7" />
          <el-option label="最近30天" :value="30" />
          <el-option label="最近90天" :value="90" />
        </el-select>
      </div>
    </div>

    <div class="charts-container">
      <!-- 1. 用户注册趋势（折线图） -->
      <div class="chart-card" :class="{ 'active': activeChart === 'registration' }" @click="setActiveChart('registration')">
        <div class="chart-header">
          <h3>用户注册趋势</h3>
          <div class="chart-actions">
            <el-tooltip content="查看详情" placement="top">
              <el-button type="text" icon="el-icon-more" />
            </el-tooltip>
          </div>
        </div>
        <div class="chart-content">
          <canvas ref="registrationChart"></canvas>
        </div>
      </div>

      <!-- 2. 班级分布（饼图） -->
      <div class="chart-card" :class="{ 'active': activeChart === 'class' }" @click="setActiveChart('class')">
        <div class="chart-header">
          <h3>班级学生分布</h3>
          <div class="chart-actions">
            <el-tooltip content="查看详情" placement="top">
              <el-button type="text" icon="el-icon-more" />
            </el-tooltip>
          </div>
        </div>
        <div class="chart-content">
          <canvas ref="classDistChart"></canvas>
        </div>
      </div>

      <!-- 3. 题目提交排行（柱状图） -->
      <div class="chart-card" :class="{ 'active': activeChart === 'submit' }" @click="setActiveChart('submit')">
        <div class="chart-header">
          <h3>题目提交排行</h3>
          <div class="chart-actions">
            <el-select v-model="limit" size="small" @change="refreshCharts">
              <el-option label="Top 5" :value="5" />
              <el-option label="Top 10" :value="10" />
              <el-option label="Top 20" :value="20" />
            </el-select>
          </div>
        </div>
        <div class="chart-content">
          <canvas ref="topSubmitChart"></canvas>
        </div>
      </div>

      <!-- 4. 提交状态分布（饼图） -->
      <div class="chart-card" :class="{ 'active': activeChart === 'status' }" @click="setActiveChart('status')">
        <div class="chart-header">
          <h3>提交状态分布</h3>
          <div class="chart-actions">
            <el-tooltip content="查看详情" placement="top">
              <el-button type="text" icon="el-icon-more" />
            </el-tooltip>
          </div>
        </div>
        <div class="chart-content">
          <canvas ref="statusDistChart"></canvas>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import Chart from 'chart.js/auto'
import request from '@/utils/request.js'

// 参数
const days = ref(30)
const limit = ref(10)
const activeChart = ref(null)

// 图表实例
const chartInstances = ref({
  registration: null,
  class: null,
  submit: null,
  status: null
})

// canvas refs
const registrationChart = ref(null)
const classDistChart = ref(null)
const topSubmitChart = ref(null)
const statusDistChart = ref(null)

// 销毁图表实例
const destroyChart = (chartType) => {
  if (chartInstances.value[chartType]) {
    chartInstances.value[chartType].destroy()
    chartInstances.value[chartType] = null
  }
}

// 设置活动图表
const setActiveChart = (chart) => {
  activeChart.value = chart
}

// 刷新所有图表
const refreshCharts = () => {
  // 销毁所有图表实例
  Object.keys(chartInstances.value).forEach(key => {
    destroyChart(key)
  })
  
  // 重新初始化所有图表
  initRegistrationTrend()
  initClassDistribution()
  initTopSubmit()
  initStatusDistribution()
}

// 1. 用户注册趋势
function initRegistrationTrend() {
  request({ url: `/stats/user/registration-trend?days=${days.value}`, method: 'get' })
    .then(({ data }) => {
      const arr = data.data || []
      const labels = arr.map(item => item.date)
      const counts = arr.map(item => item.count)
      
      // 创建新图表实例
      chartInstances.value.registration = new Chart(registrationChart.value.getContext('2d'), {
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
              titleColor: '#333',
              bodyColor: '#666',
              borderColor: '#ddd',
              borderWidth: 1
            }
          }
        }
      })
    })
}

// 2. 班级分布
function initClassDistribution() {
  request({ url: '/stats/user/class-distribution', method: 'get' })
    .then(({ data }) => {
      const arr = data.data || []
      const labels = arr.map(item => item.className || '未分班')
      const counts = arr.map(item => item.count)
      
      // 创建新图表实例
      chartInstances.value.class = new Chart(classDistChart.value.getContext('2d'), {
        type: 'doughnut',
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
            ],
            borderWidth: 0
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          cutout: '70%',
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
    })
}

// 3. 题目提交排行
function initTopSubmit() {
  request({ url: `/stats/problem/top-submit?limit=${limit.value}`, method: 'get' })
    .then(({ data }) => {
      const arr = data.data || []
      const labels = arr.map(item => item.problemName)
      const counts = arr.map(item => item.submitCount)
      
      // 创建新图表实例
      chartInstances.value.submit = new Chart(topSubmitChart.value.getContext('2d'), {
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
    })
}

// 4. 提交状态分布
function initStatusDistribution() {
  request({ url: '/stats/status/distribution', method: 'get' })
    .then(({ data }) => {
      const arr = data.data || []
      const labels = arr.map(item => item.status)
      const counts = arr.map(item => item.count)
      
      // 创建新图表实例
      chartInstances.value.status = new Chart(statusDistChart.value.getContext('2d'), {
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
    })
}

// 组件卸载时清理图表实例
onUnmounted(() => {
  Object.keys(chartInstances.value).forEach(key => {
    destroyChart(key)
  })
})

onMounted(() => {
  initRegistrationTrend()
  initClassDistribution()
  initTopSubmit()
  initStatusDistribution()
})
</script>

<style scoped>
.stats-dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.dashboard-header h2 {
  font-size: 24px;
  color: #2c3e50;
  margin: 0;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
}

.chart-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
}

.chart-card.active {
  border: 2px solid #409EFF;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.chart-header h3 {
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
}

.chart-content {
  height: 300px;
  position: relative;
}

.chart-actions {
  display: flex;
  gap: 10px;
}

@media (max-width: 768px) {
  .charts-container {
    grid-template-columns: 1fr;
  }
  
  .chart-card {
    margin-bottom: 20px;
  }
}
</style>
  