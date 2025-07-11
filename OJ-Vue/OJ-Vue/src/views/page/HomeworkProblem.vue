<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import request from "@/utils/request.js"

const route = useRoute()
const router = useRouter()
const homeworkId = route.params.id

// 初始化作业信息
const homeworkInfo = ref({
  id: homeworkId,
  title: route.query.title || '作业详情',
  startTime: route.query.startTime || '-',
  endTime: route.query.endTime || '-',
  classId: route.query.classId || null
})
const problemList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const selectedProblems = ref([])
const problemOptions = ref([])
const problemsLoading = ref(false)

// 添加计算属性判断作业是否已结束
const isHomeworkEnded = computed(() => {
  const now = new Date()
  const end = new Date(homeworkInfo.value.endTime)
  return now > end
})

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
    console.log('路由参数:', {
      id: homeworkId,
      title: route.query.title,
      startTime: route.query.startTime,
      endTime: route.query.endTime,
      classId: route.query.classId
    })
    
    const homeworkResponse = await request.get(`/homework/class/${homeworkId}`)
    console.log('API响应数据:', homeworkResponse)

    if (homeworkResponse.data) {
      // 处理不同的响应格式
      let homeworkData = null
      
      if (Array.isArray(homeworkResponse.data) && homeworkResponse.data.length > 0) {
        homeworkData = homeworkResponse.data[0]
      } else if (homeworkResponse.data.data && Array.isArray(homeworkResponse.data.data) && homeworkResponse.data.data.length > 0) {
        homeworkData = homeworkResponse.data.data[0]
      } else if (homeworkResponse.data.data) {
        homeworkData = homeworkResponse.data.data
      } else if (homeworkResponse.data.id) {
        homeworkData = homeworkResponse.data
      }
      
      console.log('解析后的作业数据:', homeworkData)
      
      // 如果成功获取到数据，更新 homeworkInfo
      if (homeworkData) {
        homeworkInfo.value = {
          id: homeworkData.id,
          title: homeworkData.title || route.query.title || '作业详情',
          startTime: homeworkData.startTime || route.query.startTime || '-',
          endTime: homeworkData.endTime || route.query.endTime || '-',
          classId: homeworkData.classId || route.query.classId || null
        }
      } else {
        // 如果没有获取到有效数据，使用路由参数
        homeworkInfo.value = {
          id: homeworkId,
          title: route.query.title || '作业详情',
          startTime: route.query.startTime || '-',
          endTime: route.query.endTime || '-',
          classId: route.query.classId || null
        }
      }
      
      console.log('最终设置的作业信息:', homeworkInfo.value)
    } else {
      console.warn('API响应中没有数据')
      // 使用路由参数作为回退
      homeworkInfo.value = {
        id: homeworkId,
        title: route.query.title || '作业详情',
        startTime: route.query.startTime || '-',
        endTime: route.query.endTime || '-',
        classId: route.query.classId || null
      }
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

// 跳转到问题详情页并保存信息到本地存储
const goToProblemDetail = (problemId) => {
  // 保存 classId 和 problemId 到本地存储
  const classProbSubInfo = {
    classId: homeworkId, // 使用作业ID作为班级ID
    problemId: problemId
  }
  localStorage.setItem('classProbSubInfo', JSON.stringify(classProbSubInfo))
  router.push(`/classProblem/${problemId}`)
}

// 跳转到提交代码页面并保存信息到本地存储
const goToSubmitPage = (problem) => {
  // 保存 classId 和 problemId 到本地存储
  const classProbSubInfo = {
    classId: homeworkId, // 使用作业ID作为班级ID
    problemId: problem.id
  }
  localStorage.setItem('classProbSubInfo', JSON.stringify(classProbSubInfo))
  router.push(`/classSubmitPage/${problem.id}/${encodeURIComponent(problem.name)}/${encodeURIComponent(problem.sampleInput)}/${encodeURIComponent(problem.sampleOutput)}`)
}

// 获取可选题目列表
const loadProblemOptions = async () => {
  try {
    const response = await request.get('/problem/simple')
    if (response.data?.data) {
      problemOptions.value = response.data.data.map(item => ({
        id: item.id,
        name: `${item.id} - ${item.name}`
      }))
    }
  } catch (error) {
    ElMessage.error('获取题目列表失败：' + (error.response?.data?.msg || error.message))
  }
}

// 添加题目到作业
const handleAddProblems = async () => {
  if (!selectedProblems.value.length) {
    ElMessage.warning('请选择要添加的题目')
    return
  }

  const loading = ElLoading.service({
    lock: true,
    text: '正在添加题目...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    for (const problemId of selectedProblems.value) {
      await request.post('/homework-problem/add', {
        homeworkId: homeworkId,
        problemId: problemId
      })
    }
    ElMessage.success('添加题目成功')
    dialogVisible.value = false
    selectedProblems.value = []
    loadData() // 刷新页面数据
  } catch (error) {
    ElMessage.error('添加题目失败：' + (error.response?.data?.msg || error.message))
  } finally {
    loading.close()
  }
}

// 处理问题链接点击
const handleProblemClick = (problemId) => {
  if (isHomeworkEnded.value) {
    ElMessage.warning('该作业已结束，无法查看问题详情')
    return
  }
  goToProblemDetail(problemId)
}

// 处理提交按钮点击
const handleSubmitClick = (problem) => {
  if (isHomeworkEnded.value) {
    ElMessage.warning('该作业已结束，无法提交代码')
    return
  }
  goToSubmitPage(problem)
}

// 处理添加题目按钮点击
const handleAddButtonClick = () => {
  if (isHomeworkEnded.value) {
    ElMessage.warning('该作业已结束，无法添加题目')
    return
  }
  dialogVisible.value = true
}

// 添加计算属性
const formattedStartTime = computed(() => formatDateTime(homeworkInfo.value.startTime))
const formattedEndTime = computed(() => formatDateTime(homeworkInfo.value.endTime))

onMounted(() => {
  console.log('HomeworkProblem 组件挂载，路由参数:', {
    params: route.params,
    query: route.query
  })
  console.log('初始化的作业信息:', homeworkInfo.value)
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
          <div class="header-buttons">
            <el-button 
              type="primary" 
              @click="handleAddButtonClick"
              :disabled="isHomeworkEnded"
            >
              <el-icon><Plus /></el-icon>
              添加题目
            </el-button>
            <el-button type="primary" @click="handleRefresh" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 作业信息 -->
      <div class="homework-info">
        <div class="info-item">
          <span class="info-label">开始时间：</span>
          <span class="info-value">{{ formattedStartTime }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">结束时间：</span>
          <span class="info-value">{{ formattedEndTime }}</span>
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
            <a 
              @click="handleProblemClick(scope.row.id)" 
              class="problem-link"
              :class="{ 'disabled-link': isHomeworkEnded }"
            >
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
            <el-button 
              link 
              type="primary" 
              @click="handleProblemClick(scope.row.id)"
              :disabled="isHomeworkEnded"
            >
              查看详情
            </el-button>
            <el-button 
              link 
              type="primary" 
              @click="handleSubmitClick(scope.row)"
              :disabled="isHomeworkEnded"
            >
              提交代码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 无数据时显示空状态 -->
      <el-empty v-if="problemList.length === 0" description="暂无问题信息" />
    </el-card>

    <!-- 添加题目对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="添加题目"
      width="500px"
      @open="loadProblemOptions"
    >
      <el-select
        v-model="selectedProblems"
        multiple
        filterable
        placeholder="请选择要添加的题目"
        style="width: 100%"
        :loading="problemsLoading"
      >
        <el-option
          v-for="item in problemOptions"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddProblems">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
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

/* 添加移动端适配样式 */
@media screen and (max-width: 768px) {
  .homework-problem-container {
    padding: 10px;
  }
  
  .header-card {
    margin-bottom: 15px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .title {
    font-size: 16px;
  }
  
  .header-buttons {
    width: 100%;
    justify-content: space-between;
  }
  
  .homework-info {
    flex-direction: column;
    gap: 10px;
  }
  
  .info-item {
    width: 100%;
  }
  
  :deep(.el-table) {
    font-size: 14px;
  }
  
  :deep(.el-table__header) {
    font-size: 14px;
  }
  
  :deep(.el-table__body) {
    font-size: 14px;
  }
  
  :deep(.el-tag) {
    font-size: 12px;
  }
  
  :deep(.el-dialog) {
    width: 90% !important;
    margin: 0 auto;
  }
  
  :deep(.el-dialog__body) {
    padding: 15px;
  }
  
  :deep(.el-select) {
    width: 100%;
  }
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

.problem-link.disabled-link {
  color: #c0c4cc;
  cursor: not-allowed;
  text-decoration: none;
}

.problem-link.disabled-link:hover {
  color: #c0c4cc;
  text-decoration: none;
}

:deep(.el-button.is-disabled) {
  color: #c0c4cc;
  cursor: not-allowed;
}

:deep(.el-card__header) {
  padding: 15px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}

.header-buttons {
  display: flex;
  gap: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
