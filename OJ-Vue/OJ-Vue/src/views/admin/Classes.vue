<template>
  <page-layout 
    title="课程管理" 
    :show-back="false"
  >
    <div class="classes-container">
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stat-row">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon :size="32"><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">课程总数</div>
                <div class="stat-value">{{ totalCourses }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <!-- 新增：作业总数统计 -->
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: rgba(103, 194, 58, 0.1); color: #67C23A;">
                <el-icon :size="32"><HomeFilled /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">作业总数</div>
                <div class="stat-value">{{ totalHomeworks }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="operation-bar">
        <div class="left-operations">
          <a-button type="primary" @click="handleAdd">
            新增课程
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </div>
        <div class="right-operations">
          <a-button @click="toggleDisplayMode">
            <template #icon><AppstoreOutlined v-if="displayMode === 'list'" /><BarsOutlined v-else /></template>
            {{ displayMode === 'list' ? '卡片模式' : '列表模式' }}
          </a-button>
        </div>
      </div>
      
      <!-- 列表模式 -->
      <a-table
        v-if="displayMode === 'list'"
        :columns="columns"
        :data-source="classList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" @click="handleViewDetail(record)">查看</a-button>
              <a-popconfirm
                title="确定要删除这个课程吗？"
                @confirm="handleDelete(record.id)"
              >
                <a-button type="link" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- 新增：卡片模式 -->
      <div v-else class="card-view">
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :sm="12" :md="8" :lg="6" v-for="(item, index) in classList" :key="item.id">
            <a-card hoverable class="course-card" :style="{ backgroundColor: getRandomColor(index) }">
              <template #cover>
                <div class="card-header">
                  <h3>{{ item.name }}</h3>
                  <div class="card-meta">
                    <span>ID: {{ item.id }}</span>
                    <a-tag color="blue">{{ item.homeworkQuantity || 0 }} 个作业</a-tag>
                  </div>
                </div>
              </template>
              <a-card-meta>
                <template #description>
                  <div class="card-actions">
                    <a-button type="link" @click="handleEdit(item)">编辑</a-button>
                    <a-button type="link" @click="handleViewDetail(item)">查看</a-button>
                    <a-popconfirm
                      title="确定要删除这个课程吗？"
                      @confirm="handleDelete(item.id)"
                    >
                      <a-button type="link" danger>删除</a-button>
                    </a-popconfirm>
                  </div>
                </template>
              </a-card-meta>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="modalTitle"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
      :confirmLoading="modalLoading"
    >
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="课程名称" name="name">
          <a-input v-model:value="formState.name" />
        </a-form-item>
      </a-form>
    </a-modal>
  </page-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { Document, HomeFilled } from '@element-plus/icons-vue'
import PageLayout from '@/components/layout/PageLayout.vue'
import type { FormInstance } from 'ant-design-vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { ReloadOutlined, AppstoreOutlined, BarsOutlined } from '@ant-design/icons-vue'

interface Course {
  id: number
  name: string
  creatorId: number
  homeworkQuantity: number
}

// 表格列定义
const columns = [
  {
    title: '课程ID',
    dataIndex: 'id',
    key: 'id',
    width: 100,
  },
  {
    title: '课程名称',
    dataIndex: 'name',
    key: 'name',
  },
  {
    title: '操作',
    key: 'action',
    width: 250,
  },
]

const classList = ref<Course[]>([])
const loading = ref(false)
const totalCourses = ref(0)
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
})

// 表单相关
const modalVisible = ref(false)
const modalTitle = ref('新增课程')
const modalLoading = ref(false)
const formRef = ref<FormInstance>()
const formState = ref<Partial<Course>>({
  name: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 50, message: '课程名称长度应在 2-50 个字符之间', trigger: 'blur' }
  ]
}

const router = useRouter()

// 新增：显示模式
const displayMode = ref('list')
const totalHomeworks = ref(0)

// 新增：切换显示模式
const toggleDisplayMode = () => {
  displayMode.value = displayMode.value === 'list' ? 'card' : 'list'
}

// 新增：刷新方法
const handleRefresh = () => {
  fetchCourses()
}

// 新增：随机背景色
const getRandomColor = (index: number) => {
  const colors = [
    '#e6f7ff', // 浅蓝
    '#f6ffed', // 浅绿
    '#fff7e6', // 浅橙
    '#fff0f6', // 浅粉
    '#f9f0ff', // 浅紫
    '#e6fffb', // 浅青
  ]
  return colors[index % colors.length]
}

// 从 localStorage 中获取登录信息
const localUser = localStorage.getItem('student-user') 
  ? JSON.parse(localStorage.getItem('student-user'))
  : localStorage.getItem('admin-user')
    ? JSON.parse(localStorage.getItem('admin-user'))
    : null;

const token = localUser ? localUser.token : '';
const userId = localUser ? localUser.id : null;
const role = localUser ? localUser.role : null;

if (!localUser) {
  message.error('未登录或用户信息不存在');
  router.push('/login');
}

// 获取当前用户ID
const getCurrentUserId = () => {
  if (!userId) {
    message.error('未登录或登录已过期');
    return null;
  }
  return userId;
}

// 获取课程列表
const fetchCourses = async () => {
  loading.value = true
  try {
    const [coursesResponse, homeworksResponse] = await Promise.all([
      request.get('/courses', {
        params: {
          current: pagination.value.current,
          size: pagination.value.pageSize
        }
      }),
      request.get('/homework/count') // 假设有这个接口获取作业总数
    ])
    
    if (coursesResponse.data.success) {
      const { records, total, current, size } = coursesResponse.data.data
      classList.value = records
      pagination.value = {
        current: current,
        pageSize: size,
        total: total
      }
      totalCourses.value = total
    }

    if (homeworksResponse.data.success) {
      totalHomeworks.value = homeworksResponse.data.data
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    message.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 添加课程
const handleAdd = () => {
  modalTitle.value = '新增课程'
  formState.value = {
    name: ''
  }
  modalVisible.value = true
}

// 编辑课程
const handleEdit = (record: Course) => {
  modalTitle.value = '编辑课程'
  formState.value = { ...record }
  modalVisible.value = true
}

// 查看详情
const handleViewDetail = (record: Course) => {
  router.push({
    name: 'ClassDetail',
    params: {
      id: record.id
    },
    query: {
      name: record.name
    }
  })
}

// 删除课程
const handleDelete = async (id: number) => {
  try {
    const response = await request.delete(`/courses/${id}`)
    if (response.data.success) {
      message.success(response.data.message || '删除成功')
      fetchCourses()
    } else {
      message.error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

// 表格分页变化
const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  fetchCourses()
}

// 提交表单
const handleModalOk = () => {
  formRef.value?.validate().then(async () => {
    const currentUserId = getCurrentUserId()
    if (!currentUserId) {
      return
    }

    modalLoading.value = true
    try {
      const payload = {
        name: formState.value.name,
        creatorId: currentUserId
      }
      const response = await request.post('/courses', payload)
      
      if (response.data.success) {
        message.success(response.data.message || (formState.value.id ? '编辑成功' : '添加成功'))
        modalVisible.value = false
        fetchCourses()
      } else {
        message.error(response.data.message || (formState.value.id ? '编辑失败' : '添加失败'))
      }
    } catch (error) {
      console.error('操作失败:', error)
      message.error(formState.value.id ? '编辑失败' : '添加失败')
    } finally {
      modalLoading.value = false
    }
  })
}

// 取消表单
const handleModalCancel = () => {
  formRef.value?.resetFields()
  modalVisible.value = false
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.classes-container {
  padding: 24px;
}

.stat-row {
  margin-bottom: 24px;
}

.stat-card {
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  padding: 16px;
  border-radius: 8px;
  margin-right: 16px;
  background-color: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.left-operations, .right-operations {
  display: flex;
  gap: 8px;
}

.card-view {
  margin-top: 16px;
}

.course-card {
  height: 100%;
  transition: all 0.3s;
}

.course-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 16px;
  background: rgba(255, 255, 255, 0.8);
}

.card-header h3 {
  margin: 0 0 8px 0;
  color: #1a1a1a;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-actions {
  display: flex;
  justify-content: space-around;
  padding-top: 8px;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .classes-container {
    padding: 12px;
  }
  
  .stat-content {
    padding: 12px;
  }
  
  .stat-icon {
    padding: 12px;
  }
  
  .stat-value {
    font-size: 20px;
  }

  .operation-bar {
    flex-direction: column;
    gap: 12px;
  }

  .left-operations, .right-operations {
    width: 100%;
    justify-content: center;
  }

  .stat-row {
    margin-bottom: 12px;
  }

  .stat-card {
    margin-bottom: 12px;
  }
}
</style> 