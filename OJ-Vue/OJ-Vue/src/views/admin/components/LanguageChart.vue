<template>
  <el-card class="chart-card">
    <template #header>语言分布</template>
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
  const res = await request.get('/analysis/submission-analysis')
  if (res.data.code === '200') {
    const { languageDistribution } = res.data.data
    const langData = languageDistribution.map(i => ({ name: i.language, value: i.count }))
    
    chart.setOption({
      title: {
        text: '语言分布',
        left: 'center',
        textStyle: {
          color: '#333',
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
          color: '#666',
          fontSize: 12
        }
      },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        data: langData,
        label: {
          formatter: '{b}\n{d}%',
          color: '#333',
          fontSize: 12,
          fontWeight: 'bold'
        },
        labelLine: {
          show: true,
          length: 10,
          length2: 10,
          lineStyle: {
            color: '#999'
          }
        },
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
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
        color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
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