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

const loading = ref(false)

// 加载数据
const loadData = async () => {
  loading.value = true
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
      response = await request.get('/api/student/searchByUsername', {
        ...requestConfig,
        params: {
          ...baseParams,
          username: queryForm.username
        }
      });
      handleResponse(response);
    } else if (queryForm.name) {
      response = await request.get('/api/student/searchByName', {
        ...requestConfig,
        params: {
          ...baseParams,
          name: queryForm.name
        }
      });
      handleResponse(response);
    } else if (queryForm.year) {
      response = await request.get('/api/student/searchByYear', {
        ...requestConfig,
        params: {
          ...baseParams,
          year: queryForm.year
        }
      });
      handleResponse(response);
    } else {
      response = await request.get('/api/student/rankByAc', requestConfig);
      handleResponse(response);
    }
  } catch (error) {
    console.error('请求异常:', error);
    ElMessage.error('获取数据失败：' + (error.message || '未知错误'));
  } finally {
    loading.value = false
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

// 在 script setup 部分添加处理函数
const handleRowClick = (row) => {
  router.push(`/userProfile/${row.id}`);
};
</script>

<template>
  <div class="problem-container">
    <!-- 搜索区域 -->
    <el-card class="search-card animate__animated animate__fadeIn">
      <template #header>
        <div class="card-header">
          <span class="title">Rating 排名</span>
        </div>
      </template>

      <el-form :model="queryForm" inline class="search-form">
        <el-form-item>
          <el-input v-model="queryForm.username" placeholder="按账号查询" clearable class="animate__animated animate__fadeInLeft" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryForm.name" placeholder="按姓名查询" clearable class="animate__animated animate__fadeInLeft animate__delay-1s" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryForm.year" placeholder="请选择年份" clearable style="width: 160px;" class="animate__animated animate__fadeInLeft animate__delay-2s">
            <el-option v-for="item in yearOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" class="animate__animated animate__fadeInLeft animate__delay-3s">查询</el-button>
          <el-button @click="handleRefresh" class="animate__animated animate__fadeInLeft animate__delay-3s">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card animate__animated animate__fadeIn animate__delay-1s">
      <el-table v-loading="loading" :data="tableData" style="width: 100%" border stripe highlight-current-row @row-click="handleRowClick">
        <el-table-column type="index" label="#" width="60" align="center">
          <template #default="scope">
            <span :class="['rank-number', `rank-${scope.$index + 1}`, 'animate__animated animate__bounceIn']">{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="账号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="name" label="姓名" width="150" align="center" />
        <el-table-column prop="score" label="Rating" width="100" align="center">
          <template #default="scope">
            <span class="score-value">{{ scope.row.score || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ac" label="AC" width="80" align="center">
          <template #default="scope">
            <span class="ac-value">{{ scope.row.ac || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="submit" label="Submit" width="80" align="center">
          <template #default="scope">
            <span class="submit-value">{{ scope.row.submit || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastVisitTime" label="Last Visit Time" width="180" align="center">
          <template #default="scope">
            <span class="time-value">{{ formatDateTime(scope.row.lastVisitTime) }}</span>
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
@import 'animate.css';

.problem-container {
  padding: 20px;
  background-color: #f5f7fa;
  height: 80vh;
  perspective: 1000px;
}

.search-card, .table-card {
  transition: all 0.3s ease;
  transform-style: preserve-3d;
}

.search-card:hover, .table-card:hover {
  transform: translateY(-5px) rotateX(2deg);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.rank-number {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 50%;
  font-weight: bold;
  transition: all 0.3s ease;
}

.rank-number:hover {
  transform: scale(1.2);
}

.rank-1 {
  background: linear-gradient(45deg, #FFD700, #FFA500);
  color: var(--color-background);
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
}

.rank-2 {
  background: linear-gradient(45deg, #C0C0C0, #A9A9A9);
  color: var(--color-background);
  box-shadow: 0 0 10px rgba(192, 192, 192, 0.5);
}

.rank-3 {
  background: linear-gradient(45deg, #CD7F32, #8B4513);
  color: var(--color-background);
  box-shadow: 0 0 10px rgba(205, 127, 50, 0.5);
}

.score-value, .ac-value, .submit-value, .time-value {
  transition: all 0.3s ease;
  display: inline-block;
}

.score-value:hover, .ac-value:hover, .submit-value:hover, .time-value:hover {
  transform: scale(1.1);
  color: #409EFF;
}

:deep(.el-table__row) {
  transition: all 0.3s ease;
}

:deep(.el-table__row:hover) {
  transform: translateX(5px);
  background-color: rgba(64, 158, 255, 0.1) !important;
}

:deep(.el-table__row:nth-child(1)) {
  background: linear-gradient(to right, rgba(255, 215, 0, 0.1), transparent) !important;
}

:deep(.el-table__row:nth-child(2)) {
  background: linear-gradient(to right, rgba(192, 192, 192, 0.1), transparent) !important;
}

:deep(.el-table__row:nth-child(3)) {
  background: linear-gradient(to right, rgba(205, 127, 50, 0.1), transparent) !important;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 移动端适配样式 */
@media screen and (max-width: 768px) {
  .problem-container {
    padding: 10px;
  }
  
  .search-card {
    margin-bottom: 15px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .title {
    font-size: 16px;
  }
  
  :deep(.el-form) {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  :deep(.el-form-item) {
    margin-right: 0;
    width: 100%;
  }
  
  :deep(.el-input) {
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
  
  :deep(.el-table .cell) {
    padding-left: 5px;
    padding-right: 5px;
  }
  
  :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
    gap: 10px;
  }
  
  .rank-number {
    width: 20px;
    height: 20px;
    line-height: 20px;
    font-size: 12px;
  }
}
</style>