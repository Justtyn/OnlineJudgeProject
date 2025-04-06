<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from "@/utils/request.js"

const route = useRoute()
const classId = route.params.id
const className = route.query.name || '未知班级'

// 作业列表数据
const homeworkList = ref([])
const loading = ref(false)

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  try {
    // 处理ISO格式的时间字符串
    if (dateTimeStr.includes('T')) {
      return dateTimeStr.replace('T', ' ').split('.')[0]
    }

    // 处理时间戳
    if (!isNaN(dateTimeStr)) {
      const date = new Date(Number(dateTimeStr))
      if (isNaN(date.getTime())) return '-'

      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      }).replace(',', '')
    }

    // 其他格式直接返回
    return dateTimeStr
  } catch (e) {
    console.error('时间格式化错误:', e)
    return '-'
  }
}

// 获取作业状态
const getHomeworkStatus = (startTime, endTime) => {
  const now = new Date()
  const start = new Date(startTime)
  const end = new Date(endTime)
  
  if (now < start) {
    return { status: '未开始', color: '#909399' } // 灰色
  } else if (now > end) {
    return { status: '已结束', color: '#F56C6C' } // 红色
  } else {
    return { status: '进行中', color: '#67C23A' } // 绿色
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const response = await request.get(`/homework/class/${classId}`)
    // 直接判断返回的数据是否为数组
    if (Array.isArray(response.data)) {
      homeworkList.value = response.data
      console.log('成功获取作业数据:', homeworkList.value)
    } else if (response.data && response.data.success && Array.isArray(response.data.data)) {
      // 兼容 {success: true, data: [...]} 格式
      homeworkList.value = response.data.data
      console.log('成功获取作业数据:', homeworkList.value)
    } else {
      // 如果返回的不是数组也不是标准格式，则设为空数组
      homeworkList.value = []
      ElMessage.warning('未获取到作业数据')
    }
  } catch (error) {
    console.error('请求异常:', error)
    homeworkList.value = []
    ElMessage.error('获取数据失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 刷新方法
const handleRefresh = () => {
  loadData()
}

// 随机生成背景颜色
const getRandomColor = (index) => {
  const colors = [
    '#e6f7ff', '#f6ffed', '#fff7e6', '#fff0f6', '#f9f0ff', '#e6fffb'
  ]
  return colors[index % colors.length]
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="homework-container">
    <!-- 标题区域 -->
    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <span class="title">作业列表 - {{ className }}</span>
          <el-button type="primary" @click="handleRefresh" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
    </el-card>

    <!-- 表格展示区域 -->
    <el-card class="table-card" v-loading="loading">
      <el-table :data="homeworkList" style="width: 100%" border stripe highlight-current-row>
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column label="作业标题" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <a @click="$router.push(`/homework/${scope.row.id}`)">
              {{ scope.row.title }}
            </a>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag 
              :type="getHomeworkStatus(scope.row.startTime, scope.row.endTime).status === '进行中' ? 'success' : 
                    getHomeworkStatus(scope.row.startTime, scope.row.endTime).status === '未开始' ? 'info' : 'danger'"
              effect="plain"
            >
              {{ getHomeworkStatus(scope.row.startTime, scope.row.endTime).status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间安排" min-width="300" align="center">
          <template #default="scope">
            <div>
              <div>开始：{{ formatDateTime(scope.row.startTime) }}</div>
              <div>截止：{{ formatDateTime(scope.row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" 
              @click="$router.push({
                path: `/homework/${scope.row.id}`,
                query: {
                  title: scope.row.title,
                  startTime: scope.row.startTime,
                  endTime: scope.row.endTime
                }
              })">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 无数据时显示空状态 -->
      <el-empty v-if="homeworkList.length === 0" description="暂无作业信息" />
    </el-card>

    <!-- 卡片展示区域 -->
    <div class="homework-list" v-if="true" v-loading="loading">
      <template v-if="homeworkList.length > 0">
        <el-card 
          v-for="(item, index) in homeworkList" 
          :key="item.id" 
          class="homework-card"
          @click="goToHomeworkDetail(item)"
        >
          <div class="homework-header" :style="{ backgroundColor: getRandomColor(index) }">
            <div class="homework-title">{{ item.title }}</div>
            <div class="homework-status">
              <el-tag 
                :type="getHomeworkStatus(item.startTime, item.endTime).status === '进行中' ? 'success' : 
                      getHomeworkStatus(item.startTime, item.endTime).status === '未开始' ? 'info' : 'danger'"
                size="small"
                effect="plain"
              >
                {{ getHomeworkStatus(item.startTime, item.endTime).status }}
              </el-tag>
            </div>
          </div>
          <div class="homework-content">
            <div class="homework-item">
              <span class="info-label">开始时间：</span>
              <span class="info-value">{{ formatDateTime(item.startTime) }}</span>
            </div>
            <div class="homework-item">
              <span class="info-label">截止时间：</span>
              <span class="info-value">{{ formatDateTime(item.endTime) }}</span>
            </div>
          </div>
          <div class="homework-actions">
            <el-button type="primary" size="small" 
              @click="$router.push({
                path: `/homework/${item.id}`,
                query: {
                  title: item.title,
                  startTime: item.startTime,
                  endTime: item.endTime
                }
              })">
              查看详情
            </el-button>
          </div>
        </el-card>
      </template>
      <el-empty v-else description="暂无作业信息" />
    </div>
  </div>
</template>

<style scoped>
.homework-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 80vh;
  width: 100%;
  overflow-x: hidden;
}

.header-card, .table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.homework-link {
  color: #1890ff;
  cursor: pointer;
  text-decoration: none;
}

.homework-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}

/* 卡片列表样式 */
.homework-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
}

.homework-card {
  margin-bottom: 0;
  transition: all 0.3s;
  width: 100%;
  cursor: pointer;
}

.homework-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.homework-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 4px 4px 0 0;
}

.homework-title {
  font-size: 16px;
  font-weight: bold;
}

.homework-info, .homework-status {
  font-size: 14px;
  color: #606266;
}

.homework-content {
  padding: 16px;
  min-height: 80px;
}

.homework-item {
  display: flex;
  margin-bottom: 8px;
}

.info-label {
  width: 100px;
  color: #909399;
}

.info-value {
  color: #303133;
}

.homework-actions {
  display: flex;
  justify-content: flex-end;
  padding: 0 16px 16px;
}

:deep(.el-card__header) {
  padding: 15px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>