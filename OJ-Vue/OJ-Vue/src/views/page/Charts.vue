<template>
  <div class="charts-container">
    <div class="chart-row">
      <div class="chart-item">
        <div ref="lineChart" class="chart"></div>
      </div>
      <div class="chart-item">
        <div ref="barChart" class="chart"></div>
      </div>
    </div>
    <div class="chart-row">
      <div class="chart-item">
        <div ref="pieChart" class="chart"></div>
      </div>
      <div class="chart-item">
        <div ref="radarChart" class="chart"></div>
      </div>
    </div>
    <div class="chart-row">
      <div class="chart-item">
        <div ref="scatterChart" class="chart"></div>
      </div>
      <div class="chart-item">
        <div ref="heatmapChart" class="chart"></div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'Charts',
  data() {
    return {
      charts: {}
    }
  },
  mounted() {
    this.initCharts()
  },
  methods: {
    initCharts() {
      this.initLineChart()
      this.initBarChart()
      this.initPieChart()
      this.initRadarChart()
      this.initScatterChart()
      this.initHeatmapChart()
    },
    initLineChart() {
      const chart = echarts.init(this.$refs.lineChart)
      const data = this.generateTimeSeriesData()
      const option = {
        title: {
          text: '时间序列数据趋势'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'time',
          boundaryGap: false
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '数据1',
          type: 'line',
          smooth: true,
          data: data.series1
        }, {
          name: '数据2',
          type: 'line',
          smooth: true,
          data: data.series2
        }]
      }
      chart.setOption(option)
      this.charts.lineChart = chart
    },
    initBarChart() {
      const chart = echarts.init(this.$refs.barChart)
      const data = this.generateBarData()
      const option = {
        title: {
          text: '多维度柱状图'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['类别1', '类别2', '类别3']
        },
        xAxis: {
          type: 'category',
          data: data.categories
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '类别1',
          type: 'bar',
          data: data.values1
        }, {
          name: '类别2',
          type: 'bar',
          data: data.values2
        }, {
          name: '类别3',
          type: 'bar',
          data: data.values3
        }]
      }
      chart.setOption(option)
      this.charts.barChart = chart
    },
    initPieChart() {
      const chart = echarts.init(this.$refs.pieChart)
      const data = this.generatePieData()
      const option = {
        title: {
          text: '环形图展示'
        },
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: '数据分布',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '20',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: data
        }]
      }
      chart.setOption(option)
      this.charts.pieChart = chart
    },
    initRadarChart() {
      const chart = echarts.init(this.$refs.radarChart)
      const data = this.generateRadarData()
      const option = {
        title: {
          text: '雷达图分析'
        },
        tooltip: {},
        legend: {
          data: ['指标1', '指标2']
        },
        radar: {
          indicator: data.indicators
        },
        series: [{
          name: '数据对比',
          type: 'radar',
          data: [{
            value: data.values1,
            name: '指标1'
          }, {
            value: data.values2,
            name: '指标2'
          }]
        }]
      }
      chart.setOption(option)
      this.charts.radarChart = chart
    },
    initScatterChart() {
      const chart = echarts.init(this.$refs.scatterChart)
      const data = this.generateScatterData()
      const option = {
        title: {
          text: '散点图分布'
        },
        xAxis: {},
        yAxis: {},
        series: [{
          symbolSize: 10,
          data: data,
          type: 'scatter'
        }]
      }
      chart.setOption(option)
      this.charts.scatterChart = chart
    },
    initHeatmapChart() {
      const chart = echarts.init(this.$refs.heatmapChart)
      const data = this.generateHeatmapData()
      const option = {
        title: {
          text: '热力图展示'
        },
        tooltip: {
          position: 'top'
        },
        grid: {
          height: '50%',
          top: '10%'
        },
        xAxis: {
          type: 'category',
          data: data.xAxis,
          splitArea: {
            show: true
          }
        },
        yAxis: {
          type: 'category',
          data: data.yAxis,
          splitArea: {
            show: true
          }
        },
        visualMap: {
          min: 0,
          max: 100,
          calculable: true,
          orient: 'horizontal',
          left: 'center',
          bottom: '15%'
        },
        series: [{
          name: '热力图',
          type: 'heatmap',
          data: data.data,
          label: {
            show: true
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      chart.setOption(option)
      this.charts.heatmapChart = chart
    },
    generateTimeSeriesData() {
      const now = new Date()
      const data = {
        series1: [],
        series2: []
      }
      for (let i = 0; i < 100; i++) {
        const time = new Date(now - (100 - i) * 24 * 3600 * 1000)
        data.series1.push([
          time,
          Math.random() * 100
        ])
        data.series2.push([
          time,
          Math.random() * 100 + 50
        ])
      }
      return data
    },
    generateBarData() {
      const categories = ['类别A', '类别B', '类别C', '类别D', '类别E', '类别F']
      return {
        categories,
        values1: categories.map(() => Math.floor(Math.random() * 100)),
        values2: categories.map(() => Math.floor(Math.random() * 100)),
        values3: categories.map(() => Math.floor(Math.random() * 100))
      }
    },
    generatePieData() {
      return [
        { value: 1048, name: '类别A' },
        { value: 735, name: '类别B' },
        { value: 580, name: '类别C' },
        { value: 484, name: '类别D' },
        { value: 300, name: '类别E' }
      ]
    },
    generateRadarData() {
      return {
        indicators: [
          { name: '指标1', max: 100 },
          { name: '指标2', max: 100 },
          { name: '指标3', max: 100 },
          { name: '指标4', max: 100 },
          { name: '指标5', max: 100 }
        ],
        values1: [80, 70, 60, 90, 85],
        values2: [60, 80, 75, 65, 70]
      }
    },
    generateScatterData() {
      const data = []
      for (let i = 0; i < 100; i++) {
        data.push([
          Math.random() * 100,
          Math.random() * 100
        ])
      }
      return data
    },
    generateHeatmapData() {
      const xAxis = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      const yAxis = ['上午', '下午', '晚上']
      const data = []
      for (let i = 0; i < yAxis.length; i++) {
        for (let j = 0; j < xAxis.length; j++) {
          data.push([j, i, Math.floor(Math.random() * 100)])
        }
      }
      return { xAxis, yAxis, data }
    }
  },
  beforeDestroy() {
    Object.values(this.charts).forEach(chart => {
      chart.dispose()
    })
  }
}
</script>

<style scoped>
.charts-container {
  padding: 20px;
  background-color: #f5f5f5;
}

/* 添加移动端适配样式 */
@media screen and (max-width: 768px) {
  .charts-container {
    padding: 10px;
  }
  
  .chart-row {
    flex-direction: column;
    gap: 15px;
  }
  
  .chart-item {
    width: 100%;
  }
  
  .chart {
    height: 300px;
  }
  
  :deep(.echarts-tooltip) {
    font-size: 12px;
  }
  
  :deep(.echarts-title) {
    font-size: 14px;
  }
}

.chart-row {
  display: flex;
  margin-bottom: 20px;
  gap: 20px;
}

.chart-item {
  flex: 1;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.chart {
  width: 100%;
  height: 400px;
}
</style> 