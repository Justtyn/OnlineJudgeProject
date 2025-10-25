<template>
  <el-card class="chart-card">
    <template #header>提交状态分布</template>
    <div ref="statusChart" style="height:300px;" />
  </el-card>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const statusChart = ref(null)
let chart = null

async function loadData() {
  const res = await request.get('/analysis/submission-analysis')
  if (res.data.code === '200') {
    const { statusDistribution } = res.data.data
    const statusData = statusDistribution.map(i => ({ name: i.status, value: i.count }))
    
    chart.setOption({
      title: {
        text: '提交状态分布',
        left: 'center',
        textStyle: {
          color: 'var(--color-heading)',
          fontSize: 14,
          fontWeight: 'normal'
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'middle',
        textStyle: {
          color: 'var(--color-text)',
          fontSize: 12
        }
      },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        data: statusData,
        label: {
          formatter: '{b}\n{d}%',
          color: 'var(--color-heading)',
          fontSize: 12,
          fontWeight: 'bold'
        },
        labelLine: {
          show: true,
          length: 10,
          length2: 10,
          lineStyle: {
            color: 'var(--color-text)'
          }
        },
        itemStyle: {
          borderRadius: 4,
          borderColor: 'var(--color-background)',
          borderWidth: 2
        },
        emphasis: {
          scale: true,
          scaleSize: 5,
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        color: ['#67C23A', '#F56C6C', '#E6A23C', '#409EFF', '#909399']
      }]
    })
  }
}

onMounted(() => {
  chart = echarts.init(statusChart.value)
  loadData()
  window.addEventListener('resize', () => chart.resize())
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', () => {})
  chart.dispose()
})
</script>

<style scoped>
.chart-card > .el-card__body { padding:0; }
</style>