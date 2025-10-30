<template>
  <div class="access-logs">
    <el-card class="mb16" shadow="never">
      <el-form :inline="true" :model="query" label-width="100px">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="daterange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="方法">
          <el-select v-model="query.method" clearable placeholder="全部">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="路径包含">
          <el-input v-model="query.pathLike" placeholder="/api/..." clearable />
        </el-form-item>
        <el-form-item label="状态码">
          <el-input v-model.number="query.status" placeholder="200" clearable />
        </el-form-item>
        <el-form-item label="耗时(ms)">
          <el-input-number v-model.number="query.minDuration" :min="0" placeholder="min" />
          <span class="mx8">-</span>
          <el-input-number v-model.number="query.maxDuration" :min="0" placeholder="max" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="username" clearable />
        </el-form-item>
        <el-form-item label="客户端IP">
          <el-input v-model="query.clientIp" placeholder="1.2.3.4" clearable />
        </el-form-item>
        <el-form-item label="告警">
          <el-select v-model="query.isAlert" clearable placeholder="全部">
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
          </el-select>
        </el-form-item>
        <!-- 按钮独立一行 -->
      </el-form>
      <div class="action-row">
        <el-button type="primary" @click="load(1)">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <el-button @click="exportCsv">导出CSV</el-button>
        <el-popover placement="bottom" width="240" trigger="click">
          <template #reference>
            <el-button>列设置</el-button>
          </template>
          <div class="colset">
            <div class="colset-actions">
              <el-button size="small" type="primary" text @click="selectAllColumns">全选</el-button>
              <el-button size="small" text @click="resetColumns">重置</el-button>
            </div>
            <el-checkbox-group v-model="visibleKeys">
              <el-checkbox v-for="c in allColumns" :key="c.key" :label="c.key">{{ c.title }}</el-checkbox>
            </el-checkbox-group>
          </div>
        </el-popover>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table :data="list" border style="width: 100%">
        <template v-for="col in renderColumns" :key="col.key">
          <el-table-column 
            v-if="col.key !== 'isAlert'"
            :prop="col.prop"
            :label="col.title"
            :width="col.width"
            :min-width="col.minWidth"
            :show-overflow-tooltip="col.overflow === true"
          />
          <el-table-column v-else :label="col.title" :width="col.width">
            <template #default="{ row }">
              <el-tag v-if="row.isAlert" type="danger" size="small">是</el-tag>
              <span v-else>否</span>
            </template>
          </el-table-column>
        </template>
      </el-table>

      <div class="mt16 flex-right">
        <el-pagination
          background
          layout="prev, pager, next, jumper, ->, total"
          :total="total"
          :page-size="pageSize"
          :current-page="pageNo"
          @current-change="load"
        />
      </div>
    </el-card>
  </div>
  
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import axios from '@/utils/request'

const list = ref([])
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(20)

const daterange = ref([])
const query = reactive({
  method: '', pathLike: '', status: undefined,
  minDuration: undefined, maxDuration: undefined,
  username: '', clientIp: '', isAlert: undefined,
})

const buildParams = (pNo) => {
  const params = { ...query, pageNo: pNo || pageNo.value, pageSize: pageSize.value }
  if (daterange.value && daterange.value.length === 2) {
    params.from = daterange.value[0]
    params.to = daterange.value[1]
  }
  return params
}

const load = async (p) => {
  if (typeof p === 'number') pageNo.value = p
  const { data } = await axios.get('/api/access-log/page', { params: buildParams(pageNo.value) })
  if (data && data.code === '200') {
    list.value = data.data.records || []
    total.value = data.data.total || 0
  }
}

const reset = () => {
  daterange.value = []
  Object.assign(query, { method: '', pathLike: '', status: undefined, minDuration: undefined, maxDuration: undefined, username: '', clientIp: '', isAlert: undefined })
  load(1)
}

const exportCsv = async () => {
  const params = buildParams(1)
  const res = await axios.get('/api/access-log/export', {
    params,
    responseType: 'blob',
    headers: { Accept: 'text/csv' }
  })
  const blob = res.data
  const disp = (res.headers && (res.headers['content-disposition'] || res.headers['Content-Disposition'])) || ''
  let filename = 'access_log.csv'
  const m = /filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i.exec(disp)
  if (m) filename = decodeURIComponent(m[1] || m[2])
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

watch(pageSize, () => load(1))

load(1)

// ===== 列设置：显示/隐藏 =====
const STORAGE_KEY = 'accesslog-columns'
const allColumns = ref([
  { key: 'requestTime', title: '请求时间', prop: 'requestTime', width: 180 },
  { key: 'durationMs', title: '耗时(ms)', prop: 'durationMs', width: 100 },
  { key: 'method', title: '方法', prop: 'method', width: 90 },
  { key: 'path', title: '路径', prop: 'path', minWidth: 220, overflow: true },
  { key: 'httpStatus', title: '状态码', prop: 'httpStatus', width: 90 },
  { key: 'clientIp', title: '客户端IP', prop: 'clientIp', width: 150 },
  { key: 'username', title: '用户名', prop: 'username', width: 140 },
  { key: 'isAlert', title: '告警', prop: 'isAlert', width: 80 },
  { key: 'alertReason', title: '原因', prop: 'alertReason', width: 120 },
  { key: 'ipCountry', title: '国家', prop: 'ipCountry', width: 100 },
  { key: 'ipRegion', title: '省份', prop: 'ipRegion', width: 120 },
  { key: 'ipCity', title: '城市', prop: 'ipCity', width: 120 },
  { key: 'ipIsp', title: 'ISP', prop: 'ipIsp', width: 120 }
])

const defaultKeys = allColumns.value.map(c => c.key)
const visibleKeys = ref(defaultKeys.slice())

onMounted(() => {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
    if (Array.isArray(saved) && saved.length) {
      visibleKeys.value = saved
    }
  } catch (e) {}
})

watch(visibleKeys, (v) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(v))
}, { deep: true })

const renderColumns = computed(() => allColumns.value.filter(c => visibleKeys.value.includes(c.key)))

const selectAllColumns = () => {
  visibleKeys.value = allColumns.value.map(c => c.key)
}

const resetColumns = () => {
  visibleKeys.value = defaultKeys.slice()
  localStorage.removeItem(STORAGE_KEY)
}
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.mt16 { margin-top: 16px; }
.mx8 { margin: 0 8px; }
.flex-right { display: flex; justify-content: flex-end; }
.colset-actions { display: flex; justify-content: space-between; margin-bottom: 8px; }
.action-row { display: flex; gap: 8px; padding: 8px 0 0 0; flex-wrap: wrap; }
</style>


