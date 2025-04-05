<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from "@/utils/request.js";
import router from '@/router/index.js';

// 在 script setup 开头添加获取 token 的逻辑
const localUser = localStorage.getItem('student-user') 
  ? JSON.parse(localStorage.getItem('student-user'))
  : localStorage.getItem('admin-user')
    ? JSON.parse(localStorage.getItem('admin-user'))
    : null;

const token = localUser ? localUser.token : '';

if (!localUser) {
  ElMessage.error('未登录或用户信息不存在');
  router.push('/login');
}

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

// 查询条件
const queryForm = reactive({
  username: '',
  name: '',
  year: ''
})

// 年份选项
const yearOptions = [
  { value: '2023', label: '2023年' },
  { value: '2024', label: '2024年' },
  { value: '2025', label: '2025年' }
]

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表格数据
const tableData = ref([])

// 问题表单数据
const problemForm = reactive({
  name: '',
  setter: '',
  desc: '',
  descInput: '',
  descOutput: '',
  sampleInput: '',
  sampleOutput: '',
  hint: '',
  createTime: '',
  acCount: 0,
  submitCount: 0
})

// 表单规则
const rules = {
  name: [{ required: true, message: '请输入问题名称', trigger: 'blur' }],
  setter: [{ required: true, message: '请输入出题人', trigger: 'blur' }],
  desc: [{ required: true, message: '请输入题目描述', trigger: 'blur' }],
  descInput: [{ required: true, message: '请输入输入描述', trigger: 'blur' }],
  descOutput: [{ required: true, message: '请输入输出描述', trigger: 'blur' }],
  sampleInput: [{ required: true, message: '请输入输入样例', trigger: 'blur' }],
  sampleOutput: [{ required: true, message: '请输入输出样例', trigger: 'blur' }]
}

// 表单引用
const addFormRef = ref(null)
const editFormRef = ref(null)

// 重置表单
const resetForm = () => {
  problemForm.name = ''
  problemForm.setter = ''
  problemForm.desc = ''
  problemForm.descInput = ''
  problemForm.descOutput = ''
  problemForm.sampleInput = ''
  problemForm.sampleOutput = ''
  problemForm.hint = ''
  problemForm.createTime = new Date().toISOString().split('T')[0]  // 设置当前日期
  problemForm.acCount = 0
  problemForm.submitCount = 0
}

// 加载数据
const loadData = async () => {
  try {
    let response;
    const baseParams = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    };
    
    const requestConfig = {
      headers: { Authorization: 'Bearer ' + token },
      params: baseParams
    };

    // 根据查询条件选择对应的API接口
    if (queryForm.username) {
      // 按用户名查询
      response = await request.get('/api/student/searchByUsername', {
        ...requestConfig,
        params: {
          ...baseParams,
          username: queryForm.username
        }
      });
      handleResponse(response);
    } else if (queryForm.name) {
      // 按姓名查询
      response = await request.get('/api/student/searchByName', {
        ...requestConfig,
        params: {
          ...baseParams,
          name: queryForm.name
        }
      });
      handleResponse(response);
    } else if (queryForm.year) {
      // 按注册年份查询
      response = await request.get('/api/student/searchByYear', {
        ...requestConfig,
        params: {
          ...baseParams,
          year: queryForm.year
        }
      });
      handleResponse(response);
    } else {
      // 无查询条件，获取所有数据
      response = await request.get('/api/student/rankByAc', requestConfig);
      handleResponse(response);
    }
  } catch (error) {
    console.error('请求异常:', error);
    ElMessage.error('获取数据失败：' + (error.message || '未知错误'));
  }
}

// 处理响应数据
const handleResponse = (response) => {
  const res = response.data;
  if (res.code === '200') {
    // 判断返回数据格式，适配不同API的返回结构
    if (Array.isArray(res.data)) {
      tableData.value = res.data;
      total.value = res.data.length;
    } else if (res.data && res.data.records) {
      tableData.value = res.data.records;
      total.value = res.data.total;
    } else if (res.data && res.data.list) {
      tableData.value = res.data.list;
      total.value = res.data.total;
    } else {
      tableData.value = [];
      total.value = 0;
    }
    console.log('成功获取数据:', tableData.value);
  } else {
    console.error('请求失败:', res.msg);
    ElMessage.error(res.msg || '获取数据失败');
  }
}

// 查询方法
const handleQuery = () => {
  // 检查是否输入了多个查询条件
  const conditions = [queryForm.username, queryForm.name, queryForm.year].filter(Boolean);
  if (conditions.length > 1) {
    ElMessage.warning('请只选择一个查询条件');
    return;
  }

  currentPage.value = 1;
  loadData();
}

// 刷新方法
const handleRefresh = () => {
  queryForm.username = '';
  queryForm.name = '';
  queryForm.year = '';
  currentPage.value = 1;
  loadData();
}

// 分页变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

// 每页条数变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadData()
}

// 页面加载时获取数据
onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="problem-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <template #header>
        <div class="card-header">
          <span class="title">Rating 排名</span>
        </div>
      </template>

      <el-form :model="queryForm" inline>
        <el-form-item>
          <el-input v-model="queryForm.username" placeholder="按账号查询" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryForm.name" placeholder="按姓名查询" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.year" placeholder="请选择年份" clearable style="width: 160px;">
            <el-option v-for="item in yearOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleRefresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <el-table :data="tableData" style="width: 100%" border stripe highlight-current-row>
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column prop="username" label="账号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="name" label="姓名" width="100" align="center" />
        <el-table-column prop="score" label="Rating" width="100" align="center">
          <template #default="scope">
            {{ scope.row.score || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="ac" label="AC" width="80" align="center">
          <template #default="scope">
            {{ scope.row.ac || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="submit" label="Submit" width="80" align="center">
          <template #default="scope">
            {{ scope.row.submit || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="lastVisitTime" label="Last Visit Time" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.lastVisitTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10, 20, 30, 50]" layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleCurrentChange" @size-change="handleSizeChange" />
      </div>
    </el-card>

  </div>
</template>

<style scoped>
.problem-container {
  padding: 20px;
  background-color: #f5f7fa;
  height: 80vh;
}

.search-card {
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

.table-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-card__header) {
  padding: 15px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}

.problem-link {
  color: #1890ff;
  cursor: pointer;
  text-decoration: underline;
}
.problem-link:hover {
  color: #40a9ff;
}

</style>