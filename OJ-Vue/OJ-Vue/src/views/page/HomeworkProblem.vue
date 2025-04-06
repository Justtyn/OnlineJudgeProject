<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from "@/utils/request.js"

const route = useRoute()
const homeworkId = route.params.id
const homeworkInfo = ref({
  id: homeworkId,
  title: route.query.title || '作业详情',
  startTime: route.query.startTime || '-',
  endTime: route.query.endTime || '-'
})
const problemList = ref([])
const loading = ref(false)

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr || dateTimeStr === '-') return '-'
  
  try {
    let date
    
    // 处理时间戳（数字或字符串）
    if (!isNaN(dateTimeStr)) {
      date = new Date(Number(dateTimeStr))
    }
    // 处理ISO格式
    else if (typeof dateTimeStr === 'string' && dateTimeStr.includes('T')) {
      date = new Date(dateTimeStr)
    }
    // 处理标准日期字符串
    else if (typeof dateTimeStr === 'string') {
      date = new Date(dateTimeStr)
    }

    // 验证日期是否有效
    if (date && !isNaN(date.getTime())) {
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      }).replace(/\//g, '-')
    }

    return '-'
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
    // 获取作业信息
    console.log('homeworkId:', homeworkId)  
    const homeworkResponse = await request.get(`/homework/class/${homeworkId}`)
    console.log('原始作业数据:', homeworkResponse)

    if (homeworkResponse.data) {
      // 处理不同的响应格式
      let homeworkData = null
      
      if (Array.isArray(homeworkResponse.data) && homeworkResponse.data.length > 0) {
        // 数组格式的响应
        homeworkData = homeworkResponse.data[0]
      } else if (homeworkResponse.data.data && Array.isArray(homeworkResponse.data.data) && homeworkResponse.data.data.length > 0) {
        // 标准响应格式 {code: 200, data: [{...}], msg: 'success'}
        homeworkData = homeworkResponse.data.data[0]
      } else if (homeworkResponse.data.data) {
        // 标准响应格式 {code: 200, data: {...}, msg: 'success'}
        homeworkData = homeworkResponse.data.data
      } else if (homeworkResponse.data.id) {
        // 直接返回数据对象格式
        homeworkData = homeworkResponse.data
      }
      
      // 如果成功获取到数据，更新 homeworkInfo
      if (homeworkData) {
        const formattedStartTime = formatDateTime(homeworkData.startTime)
        const formattedEndTime = formatDateTime(homeworkData.endTime)
        
        homeworkInfo.value = {
          id: homeworkData.id,
          title: homeworkData.title || route.query.title || '作业详情',
          startTime: formattedStartTime,
          endTime: formattedEndTime
        }
      } else {
        // 如果没有获取到有效数据，使用路由参数
        homeworkInfo.value = {
          id: homeworkId,
          title: route.query.title || '作业详情',
          startTime: formatDateTime(route.query.startTime) || '-',
          endTime: formatDateTime(route.query.endTime) || '-'
        }
      }
      
      console.log('处理后的作业数据:', homeworkInfo.value)
    }
    
    // 获取作业问题列表 - 使用正确的API端点
    const problemResponse = await request.get(`/homework-problem/list/${homeworkId}`)
    if (problemResponse.data) {
      if (Array.isArray(problemResponse.data)) {
        // 获取问题详情
        const problemIds = problemResponse.data.map(item => item.problemId)
        const problemDetails = []
        
        // 如果有问题ID，则获取每个问题的详细信息
        if (problemIds.length > 0) {
          for (const problemId of problemIds) {
            try {
              const detailResponse = await request.get(`/problem/${problemId}`)
              if (detailResponse.data && detailResponse.data.data) {
                // 找到对应的提交统计信息
                const stats = problemResponse.data.find(p => p.problemId === problemId)
                problemDetails.push({
                  ...detailResponse.data.data,
                  acCount: stats ? stats.acCount : 0,
                  submitCount: stats ? stats.submitCount : 0
                })
              } else if (detailResponse.data) {
                // 直接使用返回的数据
                const stats = problemResponse.data.find(p => p.problemId === problemId)
                problemDetails.push({
                  ...detailResponse.data,
                  acCount: stats ? stats.acCount : 0,
                  submitCount: stats ? stats.submitCount : 0
                })
              }
            } catch (err) {
              console.error(`获取问题 ${problemId} 详情失败:`, err)
            }
          }
        }
        
        problemList.value = problemDetails
      } else if (problemResponse.data.success && Array.isArray(problemResponse.data.data)) {
        problemList.value = problemResponse.data.data
      } else {
        problemList.value = []
        ElMessage.warning('未获取到问题列表')
      }
      console.log('成功获取问题列表:', problemList.value)
    }
  } catch (error) {
    console.error('请求异常:', error)
    ElMessage.error('获取数据失败：' + (error.response?.data?.msg || error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 刷新方法
const handleRefresh = () => {
  loadData()
}

// 跳转到问题详情页
const goToProblemDetail = (id) => {
  router.push(`/problem/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="homework-problem-container">
    <!-- 标题区域 -->
    <el-card class="header-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="title">{{ homeworkInfo.title || '作业详情' }}</span>
          <el-button type="primary" @click="handleRefresh" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      
      <!-- 作业信息 -->
      <div class="homework-info">
        <div class="info-item">
          <span class="info-label">开始时间：</span>
          <span class="info-value">{{ homeworkInfo.startTime }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">结束时间：</span>
          <span class="info-value">{{ homeworkInfo.endTime }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">状态：</span>
          <el-tag 
            :type="getHomeworkStatus(homeworkInfo.startTime, homeworkInfo.endTime).status === '进行中' ? 'success' : 
                  getHomeworkStatus(homeworkInfo.startTime, homeworkInfo.endTime).status === '未开始' ? 'info' : 'danger'"
            effect="plain"
          >
            {{ getHomeworkStatus(homeworkInfo.startTime, homeworkInfo.endTime).status }}
          </el-tag>
        </div>
      </div>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" v-loading="loading">
      <el-table :data="problemList" style="width: 100%" border stripe highlight-current-row>
        <el-table-column type="index" label="序号" width="80" align="center" />
        <el-table-column label="问题" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <a @click="$router.push(`/classProblem/${scope.row.id}`)" class="problem-link">
              {{ scope.row.name }}
            </a>
          </template>
        </el-table-column>
        <el-table-column prop="setter" label="出题人" width="100" align="center" />
        <el-table-column prop="acCount" label="AC" width="100" align="center">
          <template #default="scope">
            {{ scope.row.acCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="submitCount" label="Submit" width="100" align="center">
          <template #default="scope">
            {{ scope.row.submitCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="$router.push(`/classProblem/${scope.row.id}`)">
              查看详情
            </el-button>
            <el-button link type="primary" @click="$router.push(`/classSubmitPage/${scope.row.id}/${encodeURIComponent(scope.row.name)}/${encodeURIComponent(scope.row.sampleInput)}/${encodeURIComponent(scope.row.sampleOutput)}`)">
              提交代码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 无数据时显示空状态 -->
      <el-empty v-if="problemList.length === 0" description="暂无问题信息" />
    </el-card>
  </div>
</template>

<style scoped>
.homework-problem-container {
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

.homework-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  font-weight: bold;
  margin-right: 8px;
  color: #606266;
}

.info-value {
  color: #303133;
}

.problem-link {
  color: #1890ff;
  cursor: pointer;
  text-decoration: none;
}

.problem-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}

:deep(.el-card__header) {
  padding: 15px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
