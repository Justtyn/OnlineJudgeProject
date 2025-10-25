<template>
  <page-layout title="作业管理" :show-back="false">
    <div class="homework-container">
      <!-- 操作按钮 -->
      <div class="header">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>创建作业
        </el-button>
      </div>

      <!-- 搜索和筛选区 -->
      <div class="search-bar">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchQuery"
              placeholder="搜索作业标题"
              clearable
              @clear="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-select v-model="classFilter" placeholder="选择班级" clearable @change="handleSearch">
              <el-option
                v-for="item in classOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="statusFilter" placeholder="作业状态" clearable @change="handleSearch">
              <el-option label="未开始" value="pending" />
              <el-option label="进行中" value="ongoing" />
              <el-option label="已结束" value="finished" />
            </el-select>
          </el-col>
        </el-row>
      </div>

      <!-- 作业列表 -->
      <el-table
        :data="homeworks"
        style="width: 100%"
        v-loading="loading"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="作业标题" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="handleViewDetail(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="classId" label="所属班级" width="120">
          <template #default="{ row }">
            {{ getClassName(row.classId) }}
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" sortable />
        <el-table-column prop="endTime" label="结束时间" width="180" sortable />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- <el-table-column label="完成情况" width="150">
          <template #default="{ row }">
            <el-progress 
              :percentage="calculateProgress(row)"
              :status="getProgressStatus(row)"
            />
          </template>
        </el-table-column> -->
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button type="primary" link @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button type="success" link @click="handleManageProblems(row)">
                管理题目
              </el-button>
              <el-button type="danger" link @click="handleDelete(row)">
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页器 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- 添加/编辑作业对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogType === 'add' ? '创建作业' : '编辑作业'"
        width="50%"
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
        >
          <el-form-item label="作业标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入作业标题" />
          </el-form-item>
          <el-form-item label="所属班级" prop="classId">
            <el-select v-model="form.classId" placeholder="请选择班级">
              <el-option
                v-for="item in classOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围" prop="timeRange">
            <el-date-picker
              v-model="form.timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmit">
              确定
            </el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 管理题目对话框 -->
      <el-dialog
        v-model="problemDialogVisible"
        title="管理作业题目"
        width="70%"
        @open="handleDialogOpen"
      >
        <div class="problem-management">
          <!-- 已选题目列表 -->
          <div class="selected-problems">
            <h4>已选题目</h4>
            <el-table
              :data="selectedProblems"
              border
              style="width: 100%"
            >
              <el-table-column prop="id" label="题目ID" width="80" />
              <el-table-column prop="name" label="题目名称" show-overflow-tooltip />
              <el-table-column label="完成情况" width="150">
                <template #default="{ row }">
                  <div class="completion-info">
                    <span>AC: {{ row.acCount }}</span>
                    <span>提交: {{ row.submitCount }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button type="danger" link @click="handleRemoveProblem(row)">
                    移除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 添加题目区域 -->
          <div class="problem-selector">
            <h4>添加题目</h4>
            <el-select
              v-model="selectedProblemIds"
              multiple
              filterable
              placeholder="请选择要添加的题目"
              style="width: 100%"
              :loading="problemsLoading"
              @focus="handleProblemSelectFocus"
              remote
              :remote-method="debouncedLoadProblemOptions"
            >
              <el-option
                v-for="item in problemOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
            <div class="problem-selector-footer">
              <el-button type="primary" @click="handleAddSelectedProblems">
                添加选中题目
              </el-button>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
  </page-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import PageLayout from '@/components/layout/PageLayout.vue'
import request from "@/utils/request.js"
import { debounce } from 'lodash-es'

// 状态定义
const loading = ref(false)
const homeworks = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchQuery = ref('')
const classFilter = ref('')
const statusFilter = ref('')
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref<FormInstance>()
const problemDialogVisible = ref(false)
const selectedProblems = ref([])
const availableProblems = ref([])
const problemSearchQuery = ref('')
const classOptions = ref([])
const currentHomeworkId = ref(null)
const problemOptions = ref([])
const problemsLoading = ref(false)
const selectedProblemIds = ref([])

// 表单数据
const form = ref({
  id: '',
  title: '',
  classId: '',
  timeRange: [],
  description: ''
})

// 表单验证规则
const rules: FormRules = {
  title: [
    { required: true, message: '请输入作业标题', trigger: 'blur' }
  ],
  classId: [
    { required: true, message: '请选择班级', trigger: 'change' }
  ],
  timeRange: [
    { required: true, message: '请选择时间范围', trigger: 'change' }
  ]
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  try {
    if (dateTimeStr.includes('T')) {
      return dateTimeStr.replace('T', ' ').split('.')[0]
    }
    return dateTimeStr
  } catch (e) {
    console.error('时间格式化错误:', e)
    return '-'
  }
}

// 获取作业列表
const fetchHomeworks = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value
    }
    
    // 添加搜索参数
    if (searchQuery.value && searchQuery.value.trim()) {
      params.title = searchQuery.value.trim()
    }
    if (classFilter.value) {
      params.classId = classFilter.value
    }
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    
    const response = await request.get('/homework/page', {
      params
    })
    
    if (response.data && response.data.code === '200') {
      const pageData = response.data.data
      homeworks.value = pageData.records || []
      total.value = pageData.total || 0
    }
  } catch (error) {
    console.error('获取作业列表失败:', error)
    ElMessage.error('获取作业列表失败：' + (error.response?.data?.msg || error.message))
  } finally {
    loading.value = false
  }
}

// 获取班级列表
const fetchClassList = async () => {
  try {
    const response = await request.get('/courses/all')
    if (response.data?.data) {
      classOptions.value = response.data.data
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error('获取班级列表失败：' + (error.response?.data?.msg || error.message))
  }
}

// 获取作业题目列表
const fetchHomeworkProblems = async (homeworkId) => {
  try {
    const response = await request.get(`/homework-problem/list/${homeworkId}`)
    if (response.data) {
      selectedProblems.value = response.data
      await fetchProblemDetails()
    }
  } catch (error) {
    console.error('获取作业题目失败:', error)
    ElMessage.error('获取作业题目失败：' + (error.response?.data?.msg || error.message))
  }
}

// 获取题目详情
const fetchProblemDetails = async () => {
  try {
    const problemIds = selectedProblems.value.map(p => p.problemId)
    const detailedProblems = []
    
    for (const problemId of problemIds) {
      const response = await request.get(`/problem/${problemId}`)
      if (response.data?.data) {
        const stats = selectedProblems.value.find(p => p.problemId === problemId)
        detailedProblems.push({
          ...response.data.data,
          acCount: stats?.acCount || 0,
          submitCount: stats?.submitCount || 0
        })
      }
    }
    
    selectedProblems.value = detailedProblems
  } catch (error) {
    console.error('获取题目详情失败:', error)
  }
}

// 获取班级名称
const getClassName = (classId: number) => {
  const classItem = classOptions.value.find(item => item.id === classId)
  return classItem ? classItem.name : '-'
}

// 获取状态类型
const getStatusType = (row: any) => {
  const now = new Date().getTime()
  const start = new Date(row.startTime).getTime()
  const end = new Date(row.endTime).getTime()

  if (now < start) return 'info'
  if (now > end) return 'danger'
  return 'success'
}

// 获取状态文本
const getStatusText = (row: any) => {
  const now = new Date().getTime()
  const start = new Date(row.startTime).getTime()
  const end = new Date(row.endTime).getTime()

  if (now < start) return '未开始'
  if (now > end) return '已结束'
  return '进行中'
}

// 计算进度
const calculateProgress = (row: any) => {
  // 这里需要根据实际数据计算进度
  return 50
}

// 获取进度状态
const getProgressStatus = (row: any) => {
  const progress = calculateProgress(row)
  if (progress === 100) return 'success'
  if (progress > 50) return ''
  return 'exception'
}

// 获取难度标签类型
const getDifficultyType = (difficulty: string) => {
  const types = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return types[difficulty as keyof typeof types] || 'info'
}

// 搜索作业
const handleSearch = () => {
  fetchHomeworks()
}

// 添加作业
const handleAdd = () => {
  dialogType.value = 'add'
  form.value = {
    id: '',
    title: '',
    classId: '',
    timeRange: [],
    description: ''
  }
  dialogVisible.value = true
}

// 编辑作业
const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  form.value = {
    ...row,
    timeRange: [row.startTime, row.endTime]
  }
  dialogVisible.value = true
}

// 查看详情
const handleViewDetail = (row: any) => {
  // 实现查看详情逻辑
}

// 管理题目
const handleManageProblems = async (row: any) => {
  currentHomeworkId.value = row.id
  problemDialogVisible.value = true
}

// 处理对话框打开
const handleDialogOpen = async () => {
  await fetchHomeworkProblems(currentHomeworkId.value)
}

// 处理选择框获得焦点
const handleProblemSelectFocus = () => {
  if (problemOptions.value.length === 0) {
    debouncedLoadProblemOptions()
  }
}

// 防抖处理的加载题目选项
const debouncedLoadProblemOptions = debounce(async (query = '') => {
  problemsLoading.value = true
  try {
    const response = await request.get('/problem/simple', {
      params: {
        keyword: query
      }
    })
    if (response.data?.data) {
      problemOptions.value = response.data.data.map(item => ({
        id: item.id,
        name: `${item.id} - ${item.name}`
      }))
    }
  } catch (error) {
    console.error('获取题目列表失败:', error)
    ElMessage.error('获取题目列表失败：' + (error.response?.data?.msg || error.message))
  } finally {
    problemsLoading.value = false
  }
}, 300)

// 删除作业
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该作业吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.delete(`/homework/${row.id}`)
    if (response.data === true) {
      ElMessage.success('删除成功')
      await fetchHomeworks()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const [startTime, endTime] = form.value.timeRange
        const submitData = {
          title: form.value.title,
          classId: form.value.classId,
          startTime: formatDateTime(startTime),
          endTime: formatDateTime(endTime)
        }

        // 如果是编辑模式，添加 id 并使用 PUT 请求
        if (dialogType.value === 'edit') {
          submitData.id = form.value.id
          const response = await request.put('/homework', submitData)
          if (response.data?.code === '200') {
            ElMessage.success('编辑成功')
            dialogVisible.value = false
            await fetchHomeworks()
          } else {
            ElMessage.error(response.data?.msg || '编辑失败')
          }
        } else {
          // 新增模式使用 POST 请求
          const response = await request.post('/homework', submitData)
          if (response.data?.code === '200') {
            ElMessage.success('创建成功')
            dialogVisible.value = false
            await fetchHomeworks()
          } else {
            ElMessage.error(response.data?.msg || '创建失败')
          }
        }
      } catch (error) {
        ElMessage.error('操作失败：' + (error.response?.data?.msg || error.message))
      }
    }
  })
}

// 题目管理相关
const handleProblemSearch = () => {
  // 实现题目搜索逻辑
}

const handleRemoveProblem = async (problem: any) => {
  try {
    const response = await request.delete('/homework-problem/remove', {
      params: {
        homeworkId: currentHomeworkId.value,
        problemId: problem.id
      }
    })
    
    if (response.data === true) {
      ElMessage.success('移除成功')
      await fetchHomeworkProblems(currentHomeworkId.value)
    } else {
      ElMessage.error('移除失败')
    }
  } catch (error) {
    ElMessage.error('移除失败：' + (error.response?.data?.msg || error.message))
  }
}

// 添加选中题目
const handleAddSelectedProblems = async () => {
  if (!selectedProblemIds.value.length) {
    ElMessage.warning('请选择要添加的题目')
    return
  }

  const loading = ElLoading.service({
    lock: true,
    text: '正在添加题目...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    // 使用 Promise.all 并行处理多个请求
    await Promise.all(selectedProblemIds.value.map(problemId => 
      request.post('/homework-problem/add', {
        homeworkId: currentHomeworkId.value,
        problemId: problemId
      })
    ))
    
    ElMessage.success('添加题目成功')
    problemDialogVisible.value = false
    selectedProblemIds.value = []
    await fetchHomeworkProblems(currentHomeworkId.value)
  } catch (error) {
    ElMessage.error('添加题目失败：' + (error.response?.data?.msg || error.message))
  } finally {
    loading.close()
  }
}

// 分页处理
const handleSizeChange = async (val: number) => {
  pageSize.value = val
  await fetchHomeworks()
}

const handleCurrentChange = async (val: number) => {
  currentPage.value = val
  await fetchHomeworks()
}

// 初始化
onMounted(() => {
  fetchHomeworks()
  fetchClassList()
})
</script>

<style scoped>
.homework-container {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.el-select {
  width: 100%;
}

.completion-info {
  display: flex;
  gap: 10px;
  font-size: 14px;
  color: var(--color-text);
}

.problem-management {
  .selected-problems {
    margin-bottom: 30px;

    h4 {
      margin: 0 0 15px 0;
    }
  }

  .problem-selector {
    margin-top: 20px;
    
    h4 {
      margin: 0 0 15px 0;
    }

    .problem-selector-footer {
      margin-top: 15px;
      text-align: right;
    }
  }
}

:deep(.el-dialog__body) {
  padding-top: 20px;
}

.el-date-picker {
  width: 100%;
}

/* 添加移动端适配 */
@media screen and (max-width: 768px) {
  .problem-management {
    .selected-problems {
      margin-bottom: 15px;
    }
    
    .problem-selector {
      margin-top: 15px;
      
      .problem-selector-footer {
        margin-top: 10px;
      }
    }
  }
  
  :deep(.el-dialog) {
    width: 90% !important;
    margin: 0 auto;
  }
}
</style> 