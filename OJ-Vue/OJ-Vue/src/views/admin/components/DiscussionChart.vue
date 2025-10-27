<template>
  <el-card class="chart-card">
    <template #header>讨论区活跃度</template>
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
  const res = await request.get('/analysis/discussion-activity')
  if (res.data.code === '200') {
    const d = res.data.data
    chart.setOption({
      title: [{
        text: '讨论区活跃度',
        left: 'center',
        top: 20,
        textStyle: {
          color: 'var(--color-heading)',
          fontSize: 14,
          fontWeight: 'normal'
        }
      }, {
        text: `参与人数: ${d.totalParticipants}`,
        left: 'center',
        top: 50,
        textStyle: {
          color: 'var(--color-text)',
          fontSize: 12
        }
      }],
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
        radius: ['40%', '65%'],
        center: ['40%', '55%'],
        data: [
          { value: d.totalComments, name: '评论' },
          { value: d.totalDiscussions, name: '讨论' }
        ],
        label: {
          show: true,
          position: 'outside',
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
          borderWidth: 0
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
        color: ['#5470C6', '#91CC75']
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
.chart-card {
  border: none;
  box-shadow: none;
}

.chart-card > .el-card__body { 
  padding: 0; 
}

.chart-card > .el-card__header {
  border: none;
}
</style>