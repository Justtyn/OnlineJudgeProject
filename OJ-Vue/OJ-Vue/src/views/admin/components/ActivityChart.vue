<template>
  <el-card class="chart-card">
    <template #header>
      <div class="header-container">
        <span>活动统计</span>
        <el-radio-group v-model="quickSelect" size="small" @change="handleQuickSelect">
          <el-radio-button label="3">近三日</el-radio-button>
          <el-radio-button label="7">近七日</el-radio-button>
          <el-radio-button label="30">近一月</el-radio-button>
          <el-radio-button label="365">近一年</el-radio-button>
          <el-radio-button label="3000">全部</el-radio-button>
        </el-radio-group>
      </div>
    </template>
    <div ref="chartRef" style="height:300px;"></div>
    <div class="time-selector">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        size="small"
        @change="loadData"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

// 图表容器和实例
const chartRef = ref(null)
let chart = null

// 日期范围
const dateRange = ref(['', ''])
// 快速选择
const quickSelect = ref('3000')

// 处理快速选择
function handleQuickSelect(days) {
  const end = new Date()
  const start = new Date(end.getTime() - (days - 1) * 24 * 3600 * 1000)
  dateRange.value = [
    start.toISOString().slice(0, 10),
    end.toISOString().slice(0, 10)
  ]
  loadData()
}

// 每次拉回的数据都会赋给这个对象
const stats = ref({
  newDiscussions: 0,
  submissions: 0,
  newSolutions: 0
})

async function loadData() {
  const [start, end] = dateRange.value
  if (!start || !end) return
  const res = await request.get('/analysis/activity-stats', {
    params: { startDate: start, endDate: end }
  })
  if (res.data.code === '200') {
    const d = res.data.data
    stats.value = d
    // 构造柱状图
    const categories = ['新讨论', '提交次数', '新题解']
    const values = [d.newDiscussions, d.submissions, d.newSolutions]

    chart.setOption({
      tooltip: { 
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        formatter: '{b}: {c}'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: categories,
        axisLine: { lineStyle: { color: 'var(--color-text)' } },
        axisLabel: { 
          color: 'var(--color-heading)',
          fontSize: 12
        }
      },
      yAxis: {
        type: 'value',
        name: '数量',
        nameTextStyle: {
          color: 'var(--color-text)',
          fontSize: 12
        },
        axisLine: { lineStyle: { color: 'var(--color-text)' } },
        splitLine: { lineStyle: { color: 'var(--color-text)' } },
        axisLabel: { 
          color: 'var(--color-heading)',
          fontSize: 12
        }
      },
      series: [{
        name: '数量',
        type: 'bar',
        data: values,
        barWidth: '40%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#66b1ff' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        label: {
          show: true,
          position: 'top',
          color: 'var(--color-text)',
          fontSize: 12
        }
      }]
    })
  }
}

onMounted(() => {
  // 初始化图表实例
  chart = echarts.init(chartRef.value)
  // 默认显示近7天
  handleQuickSelect(3000)
  window.addEventListener('resize', () => chart.resize())
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', () => {})
  chart.dispose()
})
</script>

<style scoped>
.chart-card {
  display: flex;
  flex-direction: column;
}
.chart-card > .el-card__body {
  flex: 1;
  padding: 0;
}
.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.time-selector {
  display: flex;
  justify-content: center;
  padding: 15px;
  background-color: #f5f7fa;
  border-top: 1px solid var(--border-color);
}
</style>
