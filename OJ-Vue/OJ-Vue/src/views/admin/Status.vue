<template>
  <page-layout 
    title="状态管理" 
    :show-back="false"
  >
    <div class="status-container">
      <a-card class="status-card">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-statistic
              title="系统运行时间"
              :value="uptime"
              :precision="2"
              suffix="小时"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="CPU使用率"
              :value="cpuUsage"
              suffix="%"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="内存使用率"
              :value="memoryUsage"
              suffix="%"
            />
          </a-col>
          <a-col :span="6">
            <a-statistic
              title="磁盘使用率"
              :value="diskUsage"
              suffix="%"
            />
          </a-col>
        </a-row>
      </a-card>

      <a-card class="status-card" title="系统日志">
        <a-table
          :columns="columns"
          :data-source="logList"
          :loading="loading"
          :pagination="pagination"
          @change="handleTableChange"
        />
      </a-card>
    </div>
  </page-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import PageLayout from '@/components/layout/PageLayout.vue'

interface Log {
  id: number
  time: string
  level: string
  message: string
  source: string
}

const columns = [
  {
    title: '时间',
    dataIndex: 'time',
    key: 'time',
  },
  {
    title: '级别',
    dataIndex: 'level',
    key: 'level',
  },
  {
    title: '消息',
    dataIndex: 'message',
    key: 'message',
  },
  {
    title: '来源',
    dataIndex: 'source',
    key: 'source',
  },
]

const uptime = ref(0)
const cpuUsage = ref(0)
const memoryUsage = ref(0)
const diskUsage = ref(0)
const logList = ref<Log[]>([])
const loading = ref(false)
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
})

const fetchSystemStatus = async () => {
  try {
    // TODO: 调用API获取系统状态
    // const response = await api.getSystemStatus()
    // uptime.value = response.uptime
    // cpuUsage.value = response.cpuUsage
    // memoryUsage.value = response.memoryUsage
    // diskUsage.value = response.diskUsage
  } catch (error) {
    message.error('获取系统状态失败')
  }
}

const fetchLogs = async () => {
  loading.value = true
  try {
    // TODO: 调用API获取日志列表
    // const response = await api.getLogs(pagination.value)
    // logList.value = response.data
    // pagination.value.total = response.total
  } catch (error) {
    message.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  fetchLogs()
}

onMounted(() => {
  fetchSystemStatus()
  fetchLogs()
  // 定时更新系统状态
  setInterval(fetchSystemStatus, 60000)
})
</script>

<style scoped>
.status-container {
  padding: 24px;
}

.status-card {
  margin-bottom: 24px;
}
</style> 