<template>
  <div class="chart-container" ref="chartContainer">
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="!priceHistory || priceHistory.length === 0" class="empty-state">
      <el-empty description="暂无价格历史数据" />
    </div>
    <v-chart
        v-else
        class="price-chart"
        :option="chartOption"
        autoresize
    />
  </div>
</template>

<script setup>/* eslint-disable no-undef */
import { ref, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DataZoomComponent
} from 'echarts/components'
import VChart from 'vue-echarts'

// 注册需要的组件
use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DataZoomComponent
])

const props = defineProps({
  priceHistory: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const chartContainer = ref(null)

const chartOption = computed(() => {
  const dates = props.priceHistory.map(item =>
      new Date(item.checkedAt).toLocaleDateString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
  )

  const prices = props.priceHistory.map(item => parseFloat(item.price))

  const minPrice = Math.min(...prices)
  const maxPrice = Math.max(...prices)
  const padding = (maxPrice - minPrice) * 0.1 || 10


  return {
    title: {
      text: '价格趋势图',
      left: 'center',
      textStyle: {
        fontSize: 18,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const data = params[0]
        const price = data.value
        const date = data.name
        return `
          <div style="padding: 8px;">
            <div style="font-weight: bold; margin-bottom: 6px;">${date}</div>
            <div style="color: #f56c6c; font-size: 18px; font-weight: bold;">¥${price.toFixed(2)}</div>
          </div>
        `
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        rotate: 45,
        interval: 'auto'
      }
    },
    yAxis: {
      type: 'value',
      name: '价格 (元)',
      min: Math.max(0, minPrice - padding),
      max: maxPrice + padding,
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    dataZoom: [
      {
        type: 'slider',
        start: 0,
        end: 100,
        height: 20,
        bottom: 10
      },
      {
        type: 'inside',
        start: 0,
        end: 100
      }
    ],
    series: [
      {
        name: '商品价格',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: '#409EFF'
        },
        lineStyle: {
          color: '#409EFF',
          width: 3
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              {
                offset: 0,
                color: 'rgba(64, 158, 255, 0.3)'
              },
              {
                offset: 1,
                color: 'rgba(64, 158, 255, 0.05)'
              }
            ]
          }
        },
        data: prices
      }
    ]
  }
})
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 400px;
  background: white;
  border-radius: 12px;
  padding: 20px;
}

.loading-state,
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.price-chart {
  width: 100%;
  height: 100%;
}
</style>
