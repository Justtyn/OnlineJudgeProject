<template>
  <div class="status-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <template #header>
        <div class="card-header">
          <span class="title">状态列表</span>
        </div>
      </template>

      <el-form :model="queryForm" inline>
        <el-form-item>
          <el-input v-model="queryForm.id" placeholder="按状态ID查询" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryForm.userId" placeholder="按用户ID查询" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryForm.problemId" placeholder="按题目ID查询" clearable />
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
        <el-table-column prop="id" label="状态ID" width="100" align="center" />
        <el-table-column prop="problemId" label="题目ID" width="100" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="username" label="用户名" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="120" align="center" />
        <el-table-column prop="language" label="语言" width="100" align="center" />
        <el-table-column prop="time" label="耗时" width="100" align="center" />
        <el-table-column prop="memory" label="内存" width="100" align="center" />
        <el-table-column prop="creatTime" label="提交时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.creatTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination 
          v-model:current-page="currentPage" 
          v-model:page-size="pageSize" 
          :total="total"
          :page-sizes="[10, 20, 30, 50]" 
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleCurrentChange" 
          @size-change="handleSizeChange" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from "@/utils/request.js";

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
  id: '',
  userId: '',
  problemId: ''
})

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表格数据
const tableData = ref([])

// 加载数据
const loadData = async () => {
  try {
    let response;
    const baseParams = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    };

    // 根据查询条件选择对应的API接口
    if (queryForm.id) {
      // 按状态ID查询
      response = await request.get(`/status/${queryForm.id}`);
      if (response.data.code === 200) {
        // 将单个对象转换为数组格式
        tableData.value = [response.data.data];
        total.value = 1;
      }
    } else if (queryForm.userId) {
      // 按用户ID查询
      response = await request.get(`/status/user/${queryForm.userId}`, {
        params: baseParams
      });
      handleResponse(response);
    } else if (queryForm.problemId) {
      // 按题目ID查询
      response = await request.get(`/status/problem/${queryForm.problemId}`, {
        params: baseParams
      });
      handleResponse(response);
    } else {
      // 无查询条件，获取所有数据
      response = await request.get('/status/page', {
        params: baseParams
      });
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

  // 将返回的 res.code 转换为数字进行比较
  if (Number(res.code) === 200) {
    // 如果返回数据为空，设置一个友好的提示
    if (res.data.records.length === 0) {
      ElMessage.warning('没有找到符合条件的数据');
    }
    tableData.value = res.data.records;
    total.value = res.data.total;
    console.log('成功获取数据:', tableData.value);
  } else {
    console.error('请求失败:', res.message);
    ElMessage.error(res.message || '获取数据失败');
  }
}

// 查询方法
const handleQuery = () => {
  // 检查是否输入了多个查询条件
  const conditions = [queryForm.id, queryForm.userId, queryForm.problemId].filter(Boolean);
  if (conditions.length > 1) {
    ElMessage.warning('请只选择一个查询条件');
    return;
  }

  currentPage.value = 1;
  loadData();
}

// 刷新方法
const handleRefresh = () => {
  queryForm.id = '';
  queryForm.userId = '';
  queryForm.problemId = '';
  currentPage.value = 1;
  loadData();
}

// 分页变化
const handleCurrentChange = (val) => {
  if (total.value > 0) {
    currentPage.value = val;
    loadData();
  }
}

// 每页条数变化
const handleSizeChange = (val) => {
  if (total.value > 0) {
    pageSize.value = val;
    currentPage.value = 1;
    loadData();
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadData();
})
</script>

<style scoped>
.status-container {
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
</style>
