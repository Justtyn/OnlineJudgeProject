<template>
  <el-card class="chart-card">
    <template #header>题解质量指标</template>
    <div ref="chartRef" style="height:300px;" />
  </el-card>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const chartRef = ref(null)
let chart = null

async function loadData() {
  const res = await request.get('/analysis/solution-quality')
  if (res.data.code === '200') {
    const d = res.data.data
    const categories = ['题解总数', '点赞总数', '贡献者数']
    const values = [d.totalSolutions, d.totalLikes, d.totalContributors]
    
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
        axisLabel: {
          color: 'var(--color-heading)',
          fontSize: 12,
          interval: 0
        },
        axisLine: {
          lineStyle: { color: 'var(--color-text)' }
        }
      },
      yAxis: {
        type: 'value',
        name: '数量',
        nameTextStyle: {
          color: 'var(--color-text)',
          fontSize: 12
        },
        axisLabel: {
          color: 'var(--color-heading)',
          fontSize: 12
        },
        axisLine: {
          lineStyle: { color: 'var(--color-text)' }
        },
        splitLine: {
          lineStyle: { color: 'var(--color-text)' }
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
          fontSize: 12,
          fontWeight: 'bold'
        }
      }]
    })
  }
}

onMounted(() => {
  chart = echarts.init(chartRef.value)
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