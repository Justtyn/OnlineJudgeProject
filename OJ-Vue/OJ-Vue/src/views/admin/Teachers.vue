<template>
  <page-layout title="教师管理" :show-back="false">
    <div class="teachers-container">
      <!-- 操作按钮 -->
      <div class="header">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>添加教师
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchQuery"
              placeholder="搜索教师姓名"
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
        </el-row>
      </div>

      <!-- 教师列表 -->
      <el-table
        :data="teachers"
        style="width: 100%"
        v-loading="loading"
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="sex" label="性别" width="80" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip />
        <el-table-column prop="classId" label="所属班级" width="120">
          <template #default="{ row }">
            {{ getClassName(row.classId) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
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

      <!-- 添加/编辑教师对话框 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogType === 'add' ? '添加教师' : '编辑教师'"
        width="50%"
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="性别" prop="sex">
            <el-select v-model="form.sex" placeholder="请选择性别">
              <el-option label="男" value="男" />
              <el-option label="女" value="女" />
            </el-select>
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入电话" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
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
          <el-form-item v-if="dialogType === 'add'" label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" />
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
  </page-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import PageLayout from '@/components/layout/PageLayout.vue'
import request from "@/utils/request.js"

// 状态定义
const loading = ref(false)
const teachers = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchQuery = ref('')
const classFilter = ref('')
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref<FormInstance>()
const classOptions = ref([])

// 表单数据
const form = ref({
  id: '',
  username: '',
  name: '',
  sex: '',
  phone: '',
  email: '',
  classId: '',
  password: ''
})

// 表单验证规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  classId: [
    { required: true, message: '请选择班级', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 获取教师列表
const fetchTeachers = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    
    // 添加搜索参数
    if (searchQuery.value && searchQuery.value.trim()) {
      params.name = searchQuery.value.trim()
    }
    if (classFilter.value) {
      params.classId = classFilter.value
    }
    
    const response = await request.get('/teacher/page', {
      params
    })
    
    if (response.data && response.data.code === '200') {
      teachers.value = response.data.data.list || []
      total.value = response.data.data.total || 0
    }
  } catch (error) {
    console.error('获取教师列表失败:', error)
    ElMessage.error('获取教师列表失败：' + (error.response?.data?.msg || error.message))
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

// 获取班级名称
const getClassName = (classId: number) => {
  const classItem = classOptions.value.find(item => item.id === classId)
  return classItem ? classItem.name : '-'
}

// 搜索教师
const handleSearch = () => {
  currentPage.value = 1
  fetchTeachers()
}

// 添加教师
const handleAdd = () => {
  dialogType.value = 'add'
  form.value = {
    id: '',
    username: '',
    name: '',
    sex: '',
    phone: '',
    email: '',
    classId: '',
    password: ''
  }
  dialogVisible.value = true
}

// 编辑教师
const handleEdit = (row: any) => {
  dialogType.value = 'edit'
  form.value = {
    ...row,
    password: ''
  }
  dialogVisible.value = true
}

// 删除教师
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该教师吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.delete(`/teacher/${row.id}`)
    if (response.data && response.data.code === '200') {
      ElMessage.success('删除成功')
      await fetchTeachers()
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
        const submitData = { ...form.value }
        
        // 如果是编辑模式，添加 id 并使用 PUT 请求
        if (dialogType.value === 'edit') {
          submitData.id = form.value.id
          const response = await request.put('/teacher/update', submitData)
          if (response.data?.code === '200') {
            ElMessage.success('编辑成功')
            dialogVisible.value = false
            await fetchTeachers()
          } else {
            ElMessage.error(response.data?.msg || '编辑失败')
          }
        } else {
          // 新增模式使用 POST 请求
          const response = await request.post('/teacher/add', submitData)
          if (response.data?.code === '200') {
            ElMessage.success('添加成功')
            dialogVisible.value = false
            await fetchTeachers()
          } else {
            ElMessage.error(response.data?.msg || '添加失败')
          }
        }
      } catch (error) {
        ElMessage.error('操作失败：' + (error.response?.data?.msg || error.message))
      }
    }
  })
}

// 分页处理
const handleSizeChange = async (val: number) => {
  pageSize.value = val
  await fetchTeachers()
}

const handleCurrentChange = async (val: number) => {
  currentPage.value = val
  await fetchTeachers()
}

// 初始化
onMounted(() => {
  fetchTeachers()
  fetchClassList()
})
</script>

<style scoped>
.teachers-container {
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

:deep(.el-dialog__body) {
  padding-top: 20px;
}
</style>
