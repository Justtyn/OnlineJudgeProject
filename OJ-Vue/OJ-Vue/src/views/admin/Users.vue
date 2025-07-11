<template>
  <page-layout 
    title="用户管理" 
    :show-back="false"
  >
    <div class="users-container">
      <!-- 操作栏 -->
      <div class="operation-bar">
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon>
              <plus-outlined />
            </template>
            添加用户
          </a-button>
          <a-button @click="fetchUsers">
            <template #icon>
              <reload-outlined />
            </template>
            刷新
          </a-button>
          
          <!-- 搜索和筛选 -->
          <a-input-search
            v-model:value="searchQuery"
            placeholder="搜索用户名/姓名"
            style="width: 200px"
            @search="handleSearch"
          />
          <a-select
            v-model:value="roleFilter"
            placeholder="角色"
            style="width: 120px"
            allowClear
            @change="handleSearch"
          >
            <a-select-option value="STUDENT">学生</a-select-option>
            <a-select-option value="ADMIN">管理员</a-select-option>
          </a-select>
        </a-space>
      </div>

      <!-- 用户表格 -->
      <a-table
        :columns="columns"
        :data-source="users"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
        :scroll="{ x: 1200 }"
      >
        <template #bodyCell="{ column, record }">
          <!-- 角色列 -->
          <template v-if="column.key === 'role'">
            <a-tag :color="record.role === 'ADMIN' ? 'red' : 'green'">
              {{ record.role === 'ADMIN' ? '管理员' : '学生' }}
            </a-tag>
          </template>
          
          <!-- 操作列 -->
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-button type="link" @click="handleViewProfile(record)">
                查看资料
              </a-button>
              <a-popconfirm
                title="确定要删除这个用户吗？"
                @confirm="handleDelete(record)"
                :disabled="record.role === 'ADMIN'"
              >
                <a-button 
                  type="link" 
                  danger 
                  :disabled="record.role === 'ADMIN'"
                >
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- 添加/编辑用户对话框 -->
      <a-modal
        v-model:visible="dialogVisible"
        :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
        @ok="handleSubmit"
        width="600px"
      >
        <a-form
          ref="formRef"
          :model="form"
          :rules="rules"
          :label-col="{ span: 4 }"
          :wrapper-col="{ span: 20 }"
        >
          <a-form-item label="用户名" name="username">
            <a-input 
              v-model:value="form.username" 
              placeholder="请输入用户名"
              :disabled="dialogType === 'edit'"
            />
          </a-form-item>
          <a-form-item label="密码" prop="password" v-if="dialogType === 'add'">
            <a-input 
              v-model:value="form.password" 
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </a-form-item>
          <a-form-item label="角色" prop="role">
            <a-select v-model:value="form.role" placeholder="请选择角色">
              <a-select-option value="STUDENT">学生</a-select-option>
              <a-select-option value="ADMIN">管理员</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="姓名" prop="name">
            <a-input v-model:value="form.name" placeholder="请输入姓名" />
          </a-form-item>
          <a-form-item label="性别" prop="sex">
            <a-radio-group v-model:value="form.sex">
              <a-radio value="男">男</a-radio>
              <a-radio value="女">女</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item label="邮箱" prop="email">
            <a-input v-model:value="form.email" placeholder="请输入邮箱" />
          </a-form-item>
          <a-form-item label="电话" prop="phone">
            <a-input v-model:value="form.phone" placeholder="请输入电话号码" />
          </a-form-item>
          <a-form-item label="学校" prop="school">
            <a-input v-model:value="form.school" placeholder="请输入学校" />
          </a-form-item>
          <a-form-item label="生日" prop="birth">
            <a-date-picker
              v-model:value="form.birth"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </a-form-item>
        </a-form>
      </a-modal>
    </div>
  </page-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { message, Modal, type FormRules } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import PageLayout from '@/components/layout/PageLayout.vue'
import request from "@/utils/request.js"

const router = useRouter()

// 获取token
const localUser = localStorage.getItem('student-user') 
  ? JSON.parse(localStorage.getItem('student-user'))
  : localStorage.getItem('admin-user')
    ? JSON.parse(localStorage.getItem('admin-user'))
    : null;

const token = localUser ? localUser.token : '';

if (!localUser) {
  message.error('未登录或用户信息不存在');
  router.push('/login');
}

// 状态定义
const loading = ref(false)
const users = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchQuery = ref('')
const roleFilter = ref('')
const dialogVisible = ref(false)
const dialogType = ref<'add' | 'edit'>('add')
const formRef = ref<FormInstance>()

// 修改排序状态的默认值
const sortOrder = ref('ascend')
const sortProp = ref('id')

// 表单数据
const form = ref({
  id: '',
  username: '',
  password: '',
  role: 'STUDENT',
  name: '',
  sex: '男',
  email: '',
  phone: '',
  school: '',
  birth: '',
  avatar: '',
  background: ''
})

// 表单验证规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ]
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const baseParams = {
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      orderBy: sortProp.value,
      order: sortOrder.value
    }

    const requestConfig = {
      headers: { Authorization: 'Bearer ' + token },
      params: baseParams
    }

    let response;

    if (searchQuery.value) {
      if (/^[a-zA-Z]/.test(searchQuery.value)) {
        response = await request.get('/api/student/searchByUsername', {
          ...requestConfig,
          params: { ...baseParams, username: searchQuery.value }
        })
      } else {
        response = await request.get('/api/student/searchByName', {
          ...requestConfig,
          params: { ...baseParams, name: searchQuery.value }
        })
      }
    } else {
      // 修改为 all 接口，这是按 id 查询的接口
      response = await request.get('/api/student/all', requestConfig)
    }

    const res = response.data
    if (res.code === '200') {
      // 判断返回数据格式，适配不同API的返回结构
      if (Array.isArray(res.data)) {
        users.value = res.data
        pagination.value.total = res.data.length
      } else if (res.data && res.data.records) {
        users.value = res.data.records
        pagination.value.total = res.data.total
      } else if (res.data && res.data.list) {
        users.value = res.data.list
        pagination.value.total = res.data.total
      } else {
        users.value = []
        pagination.value.total = 0
      }

      // 如果有角色筛选
      if (roleFilter.value) {
        users.value = users.value.filter(user => user.role === roleFilter.value)
        pagination.value.total = users.value.length
      }

      // 修改排序逻辑
      if (sortProp.value) {
        users.value.sort((a, b) => {
          const aValue = a[sortProp.value]
          const bValue = b[sortProp.value]
          
          // 处理数字类型的排序
          if (sortProp.value === 'id') {
            return sortOrder.value === 'ascend' 
              ? Number(aValue) - Number(bValue) 
              : Number(bValue) - Number(aValue)
          }
          
          // 其他类型的排序保持不变
          if (typeof aValue === 'number' && typeof bValue === 'number') {
            return sortOrder.value === 'ascend' ? aValue - bValue : bValue - aValue
          }
          
          const aStr = String(aValue || '').toLowerCase()
          const bStr = String(bValue || '').toLowerCase()
          return sortOrder.value === 'ascend' 
            ? aStr.localeCompare(bStr)
            : bStr.localeCompare(aStr)
        })
      }
    } else {
      message.error(res.msg || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    message.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索用户
const handleSearch = () => {
  fetchUsers()
}

// 重置表单
const resetForm = () => {
  form.value = {
    id: '',
    username: '',
    password: '',
    role: 'STUDENT',
    name: '',
    sex: '男',
    email: '',
    phone: '',
    school: '',
    birth: '',
    avatar: '',
    background: ''
  }
}

// 添加用户按钮点击
const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 编辑用户按钮点击
const handleEdit = (row) => {
  dialogType.value = 'edit'
  // 深拷贝用户数据到表单
  form.value = JSON.parse(JSON.stringify({
    ...row,
    password: '' // 编辑时不显示密码
  }))
  dialogVisible.value = true
}

// 查看用户资料
const handleViewProfile = (row) => {
  // 修改为正确的路由路径
  router.push(`/userProfile/${row.id}`)
}

// 删除用户
const handleDelete = (row) => {
  if (row.role === 'ADMIN') {
    message.warning('不能删除管理员账号')
    return
  }

  Modal.confirm({
    title: '警告',
    content: '确定要删除该用户吗？此操作不可恢复！',
    okText: '确定',
    cancelText: '取消',
    type: 'warning',
    async onOk() {
      try {
        const response = await request.delete(`/api/student/delete/${row.id}`, {
          headers: { Authorization: 'Bearer ' + token }
        })
        
        if (response.data.code === '200') {
          message.success('删除成功')
          await fetchUsers()
        } else {
          message.error(response.data.msg || '删除失败')
        }
      } catch (error) {
        console.error('删除用户失败:', error)
        message.error('删除失败')
      }
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  try {
    if (!formRef.value) return
    
    // 等待表单验证完成
    const valid = await formRef.value.validate()
    
    if (valid) {
      const requestData = { ...form.value }
      
      // 如果是编辑模式，移除密码字段
      if (dialogType.value === 'edit') {
        delete requestData.password
      }
      
      const response = await request({
        method: dialogType.value === 'add' ? 'post' : 'put',
        url: dialogType.value === 'add' ? '/api/student/add' : '/api/student/update',
        data: requestData,
        headers: { Authorization: 'Bearer ' + token }
      })
      
      if (response.data.code === '200') {
        message.success(dialogType.value === 'add' ? '添加成功' : '编辑成功')
        dialogVisible.value = false
        await fetchUsers()
      } else {
        throw new Error(response.data.msg || (dialogType.value === 'add' ? '添加失败' : '编辑失败'))
      }
    }
  } catch (error) {
    console.error('表单提交失败:', error)
    message.error(error.message || (dialogType.value === 'add' ? '添加失败' : '编辑失败'))
  }
}

// 表格变化处理
const handleTableChange = (pag, filters, sorter) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  
  if (sorter) {
    sortProp.value = sorter.field
    sortOrder.value = sorter.order || 'ascend'
  }
  
  fetchUsers()
}

// 初始化时立即获取数据
onMounted(() => {
  fetchUsers()
})

// 定义表格列
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
    sorter: true,
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
    width: 120,
    sorter: true,
  },
  {
    title: '姓名',
    dataIndex: 'name',
    key: 'name',
    width: 120,
    sorter: true,
  },
  {
    title: '角色',
    dataIndex: 'role',
    key: 'role',
    width: 100,
  },
  {
    title: '性别',
    dataIndex: 'sex',
    key: 'sex',
    width: 80,
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email',
    width: 200,
    ellipsis: true,
  },
  {
    title: '电话',
    dataIndex: 'phone',
    key: 'phone',
    width: 120,
  },
  {
    title: 'AC数',
    dataIndex: 'ac',
    key: 'ac',
    width: 100,
    sorter: true,
  },
  {
    title: '提交数',
    dataIndex: 'submit',
    key: 'submit',
    width: 100,
    sorter: true,
  },
  {
    title: '操作',
    key: 'action',
    width: 250,
    fixed: 'right'
  },
]

// 分页配置
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条`,
})
</script>

<style scoped>
.users-container {
  padding: 24px;
  background: #f0f2f5;
}

.operation-bar {
  margin-bottom: 16px;
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

:deep(.ant-table-wrapper) {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
}

:deep(.ant-table-row) {
  cursor: pointer;
  transition: all 0.3s ease;
}

:deep(.ant-table-row:hover) {
  background: #f5f5f5;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .users-container {
    padding: 10px;
  }

  .operation-bar {
    padding: 15px;
  }
}

/* 添加过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 对话框动画 */
:deep(.ant-modal-content) {
  transition: all 0.3s ease;
  transform: scale(0.9);
}

:deep(.ant-modal-content:hover) {
  transform: scale(1);
}

/* 表格行动画 */
.el-table__row {
  transition: all 0.3s ease;
}

.el-table__row:hover {
  background-color: #f5f7fa;
  transform: scale(1.01);
}

/* 对话框动画 */
.el-dialog__wrapper {
  transition: all 0.3s ease;
}

.el-dialog {
  transform: scale(0.9);
  transition: all 0.3s ease;
}

.el-dialog__wrapper.dialog-fade-enter-active .el-dialog,
.el-dialog__wrapper.dialog-fade-leave-active .el-dialog {
  transform: scale(1);
}
</style> 