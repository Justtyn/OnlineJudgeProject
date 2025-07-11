<template>
  <div class="announcements-container">
    <div class="header">
      <h2>公告管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>添加公告
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchTitle"
        placeholder="请输入公告标题"
        clearable
        @clear="handleSearch"
        style="width: 300px"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 公告列表 -->
    <el-table
      :data="announcements"
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="time" label="发布时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button-group>
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑公告对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加公告' : '编辑公告'"
      width="50%"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="4"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="发布时间" prop="time">
          <el-date-picker
            v-model="form.time"
            type="datetime"
            placeholder="选择发布时间"
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from "@/utils/request.js";

// 状态定义
const loading = ref(false)
const announcements = ref([])
const searchTitle = ref('')
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()

// 表单数据
const form = ref({
  id: '',
  title: '',
  content: '',
  time: ''
})

// 表单验证规则
const rules: FormRules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ],
  time: [
    { required: true, message: '请选择发布时间', trigger: 'change' }
  ]
}

// 获取公告列表
const fetchAnnouncements = async () => {
  loading.value = true
  try {
    const response = await request.get('/announcement')
    const { code, data, msg } = response.data
    if (code === '200') {
      announcements.value = data || []
    } else {
      ElMessage.error(msg || '获取公告列表失败')
    }
  } catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索公告
const handleSearch = async () => {
  if (!searchTitle.value) {
    await fetchAnnouncements()
    return
  }
  
  loading.value = true
  try {
    const response = await request.get('/announcement/title', {
      params: {
        title: searchTitle.value
      }
    })
    const { code, data, msg } = response.data
    if (code === '200') {
      announcements.value = Array.isArray(data) ? data : [data].filter(item => item)
    } else {
      ElMessage.error(msg || '搜索公告失败')
    }
  } catch (error) {
    console.error('搜索公告失败:', error)
    ElMessage.error('搜索公告失败')
  } finally {
    loading.value = false
  }
}

// 添加公告
const handleAdd = () => {
  dialogType.value = 'add'
  form.value = {
    id: '',
    title: '',
    content: '',
    time: ''
  }
  dialogVisible.value = true
}

// 编辑公告
const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  form.value = { ...row }
  dialogVisible.value = true
}

// 删除公告
const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    '确定要删除这条公告吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await request.delete(`/announcement/${row.id}`)
      const { code, msg } = response.data
      if (code === '200') {
        ElMessage.success('删除成功')
        await fetchAnnouncements()
      } else {
        ElMessage.error(msg || '删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await request.post('/announcement', form.value)
        const { code, msg } = response.data
        if (code === '200') {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '编辑成功')
          dialogVisible.value = false
          await fetchAnnouncements()
        } else {
          ElMessage.error(msg || (dialogType.value === 'add' ? '添加失败' : '编辑失败'))
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error(dialogType.value === 'add' ? '添加失败' : '编辑失败')
      }
    }
  })
}

// 初始化
onMounted(() => {
  fetchAnnouncements()
})
</script>

<style scoped>
.announcements-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

:deep(.el-dialog__body) {
  padding-top: 20px;
}
</style> 