<template>
  <el-card class="chart-card">
    <template #header>学生 AC 与提交分布</template>
    <div ref="chartRef" style="height:300px;" />
  </el-card>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const buckets = ['0','1-10','11-50','51-100','100+']
const chartRef = ref(null)
let chart = null

async function loadData() {
  const res = await request.get('/analysis/student-performance')
  if (res.data.code==='200') {
    const d = res.data.data
    const acValues = buckets.map(k=>d.acCountDistribution[k]||0)
    const submitValues = buckets.map(k=>d.submitCountDistribution[k]||0)
    chart.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: function(params) {
          let result = params[0].axisValue + '<br/>'
          params.forEach(param => {
            result += param.seriesName + ': ' + param.value + '人<br/>'
          })
          return result
        }
      },
      legend: {
        data: ['AC题数', '提交次数'],
        bottom: 0,
        textStyle: {
          color: 'var(--color-text)',
          fontSize: 12
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: buckets,
        name: '区间',
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
        }
      },
      yAxis: {
        type: 'value',
        name: '人数',
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
      series: [
        {
          name: 'AC题数',
          type: 'bar',
          data: acValues,
          barWidth: '30%',
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
        },
        {
          name: '提交次数',
          type: 'bar',
          data: submitValues,
          barWidth: '30%',
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#67C23A' },
              { offset: 1, color: '#85ce61' }
            ]),
            borderRadius: [4, 4, 0, 0]
          },
          label: {
            show: true,
            position: 'top',
            color: 'var(--color-text)',
            fontSize: 12
          }
        }
      ]
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