<template>
  <div class="status-container">
    <!-- 搜索区域：包含状态列表标题、刷新按钮和搜索表单 -->
    <el-card class="search-card">
      <template #header>
        <div class="card-header">
          <span class="title">状态列表</span>
          <!-- 刷新按钮：点击时重新加载数据 -->
          <el-button type="primary" size="small" circle @click="loadData" class="refresh-btn" title="刷新数据">
            <el-icon class="refresh-icon"><Refresh /></el-icon>
          </el-button>
        </div>
      </template>

      <!-- 搜索表单：支持按状态ID、用户ID和题目ID进行查询 -->
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

    <!-- 表格区域：显示状态列表数据 -->
    <el-card class="table-card">
      <el-table :data="tableData" style="width: 100%" border stripe highlight-current-row>
        <!-- 状态ID列 -->
        <el-table-column prop="id" label="状态ID" width="140" align="center" />
        <!-- 题目ID列：点击可跳转到题目详情 -->
        <el-table-column prop="problemId" label="题目ID" width="140" align="center">
          <template #default="scope">
            <a @click="$router.push(`/problem/${scope.row.problemId}`)" class="problem-link">
              {{ scope.row.problemId }}
            </a>
          </template>
        </el-table-column>
        <!-- 其他数据列 -->
        <el-table-column prop="username" label="用户名" width="180" align="center">
          <template #default="scope">
            <a @click="$router.push(`/userProfile/${scope.row.userId}`)" class="problem-link">
              {{ scope.row.username }}
            </a>
          </template>
        </el-table-column>
        <!-- 状态列：根据不同状态显示不同样式 -->
        <el-table-column prop="status" label="状态" width="300" align="center">
          <template #default="scope">
            <span @click="$router.push({ name: 'StatusDetail', params: { id: scope.row.id } })" class="status-link">
              <span :class="getStatusClass(scope.row.status)">{{ scope.row.status }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="language" label="语言" width="140" align="center" />
        <el-table-column prop="time" label="耗时" width="140" align="center" />
        <el-table-column prop="memory" label="内存" width="140" align="center" />
        <!-- 提交时间列：格式化显示时间 -->
        <el-table-column prop="creatTime" label="提交时间" min-width="150" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.creatTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
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
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import request from "@/utils/request.js";

// 状态映射配置：定义所有可能的状态及其类型
const statusMap = [
  { id: 1, description: "In Queue", type: "pending" },      // 队列中
  { id: 2, description: "Processing", type: "pending" },    // 处理中
  { id: 3, description: "Accepted", type: "success" },      // 通过
  { id: 4, description: "Wrong Answer", type: "error" },    // 答案错误
  { id: 5, description: "Time Limit Exceeded", type: "warning" }, // 超时
  { id: 6, description: "Compilation Error", type: "error" },     // 编译错误
  { id: 7, description: "Runtime Error (SIGSEGV)", type: "error" }, // 段错误
  { id: 8, description: "Runtime Error (SIGXFSZ)", type: "error" }, // 文件大小超限
  { id: 9, description: "Runtime Error (SIGFPE)", type: "error" },  // 浮点错误
  { id: 10, description: "Runtime Error (SIGABRT)", type: "error" }, // 程序中止
  { id: 11, description: "Runtime Error (NZEC)", type: "error" },    // 非零退出码
  { id: 12, description: "Runtime Error (Other)", type: "error" },   // 其他运行时错误
  { id: 13, description: "Internal Error", type: "error" },          // 内部错误
  { id: 14, description: "Exec Format Error", type: "error" }        // 执行格式错误
];

// 根据状态获取对应的CSS类名
const getStatusClass = (status) => {
  const foundStatus = statusMap.find(item => item.description === status);
  if (!foundStatus) return 'status-unknown';
  
  // 根据状态类型返回对应的样式类
  switch (foundStatus.type) {
    case 'success': return 'status-success';
    case 'error': return 'status-error';
    case 'warning': return 'status-warning';
    case 'pending': return 'status-pending';
    default: return 'status-unknown';
  }
}

// 查询表单数据
const queryForm = reactive({
  id: '',
  userId: '',
  problemId: ''
})

// 分页相关的响应式数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表格数据
const tableData = ref([])

// 自动刷新定时器ID
let refreshInterval = null;

// 加载数据的主要方法
const loadData = async () => {
  try {
    let response;
    const baseParams = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    };

    // 根据不同的查询条件调用不同的API接口
    if (queryForm.id) {
      // 按状态ID查询
      response = await request.get(`/status/${queryForm.id}`);
      console.log('ID查询响应:', response.data);
      
      if (response.data.code === 200 || response.data.code === '200') {
        if (response.data.data) {
          tableData.value = [response.data.data];
          total.value = 1;
          ElMessage.success('查询成功');
        } else {
          ElMessage.warning('未找到对应ID的状态');
          tableData.value = [];
          total.value = 0;
        }
      } else {
        ElMessage.error(response.data.msg || '查询失败');
        tableData.value = [];
        total.value = 0;
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
      // 获取所有状态数据
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

// 处理API响应数据
const handleResponse = (response) => {
  const res = response.data;
  if (Number(res.code) === 200) {
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

// 处理查询按钮点击
const handleQuery = () => {
  // 检查是否同时使用多个查询条件
  const conditions = [queryForm.id, queryForm.userId, queryForm.problemId].filter(Boolean);
  if (conditions.length > 1) {
    ElMessage.warning('请只选择一个查询条件');
    return;
  }

  currentPage.value = 1;
  loadData();
  
  // 重置自动刷新
  clearInterval(refreshInterval);
  startAutoRefresh();
}

// 处理重置按钮点击
const handleRefresh = () => {
  queryForm.id = '';
  queryForm.userId = '';
  queryForm.problemId = '';
  currentPage.value = 1;
  loadData();
}

// 处理分页变化
const handleCurrentChange = (val) => {
  if (total.value > 0) {
    currentPage.value = val;
    loadData();
  }
}

// 处理每页显示数量变化
const handleSizeChange = (val) => {
  if (total.value > 0) {
    pageSize.value = val;
    currentPage.value = 1;
    loadData();
  }
}

// 格式化日期时间显示
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'

  try {
    // 处理ISO格式时间
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

    return dateTimeStr
  } catch (e) {
    console.error('时间格式化错误:', e)
    return '-'
  }
}

// 启动自动刷新（每5秒刷新一次）
const startAutoRefresh = () => {
  refreshInterval = setInterval(() => {
    loadData();
  }, 5000);
}

// 组件挂载时初始化数据和启动自动刷新
onMounted(() => {
  loadData();
  startAutoRefresh();
});

// 组件卸载前清理定时器
onBeforeUnmount(() => {
  clearInterval(refreshInterval);
});
</script>

<style scoped>
/* 容器样式 */
.status-container {
  padding: 20px;
  background-color: #f5f7fa;
  height: 80vh;
  width: 100%;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

/* 搜索卡片样式 */
.search-card {
  margin-bottom: 20px;
  width: 100%;
  transition: all 0.3s ease;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.search-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
}

/* 卡片头部样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

/* 标题样式 */
.title {
  font-size: 18px;
  font-weight: bold;
  background: linear-gradient(45deg, #409eff, #36cfc9);
  -webkit-background-clip: text;
  color: transparent;
  transition: all 0.3s ease;
}

/* 表格卡片样式 */
.table-card {
  margin-bottom: 20px;
  width: 100%;
  overflow-x: auto;
  transition: all 0.3s ease;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.table-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f5f7fa !important;
  font-weight: bold;
  color: #606266;
}

:deep(.el-table td) {
  padding: 12px 0;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafafa;
}

:deep(.el-table__body tr:hover > td) {
  background-color: #f0f9ff !important;
}

/* 题目链接样式 */
.problem-link {
  color: #1890ff;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.3s ease;
  position: relative;
}

.problem-link::after {
  content: '';
  position: absolute;
  width: 0;
  height: 1px;
  bottom: -2px;
  left: 0;
  background-color: #1890ff;
  transition: width 0.3s ease;
}

.problem-link:hover {
  color: #40a9ff;
}

.problem-link:hover::after {
  width: 100%;
}

/* 状态标签样式优化 */
.status-success {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 6px;
  background: linear-gradient(135deg, #f0f9eb, #e1f3d8);
  color: #67c23a;
  border: 1px solid #c2e7b0;
  font-weight: bold;
  width: 100%;
  transition: all 0.3s ease;
  text-align: center;
}

.status-error {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 6px;
  background: linear-gradient(135deg, #fef0f0, #fde2e2);
  color: #f56c6c;
  border: 1px solid #fbc4c4;
  font-weight: bold;
  width: 100%;
  transition: all 0.3s ease;
  text-align: center;
}

.status-warning {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 6px;
  background: linear-gradient(135deg, #fdf6ec, #faecd8);
  color: #e6a23c;
  border: 1px solid #f5dab1;
  font-weight: bold;
  width: 100%;
  transition: all 0.3s ease;
  text-align: center;
}

.status-pending {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 6px;
  background: linear-gradient(135deg, #f0f2f5, #e4e7ed);
  color: #909399;
  border: 1px solid #d3d4d6;
  font-weight: bold;
  width: 100%;
  transition: all 0.3s ease;
  text-align: center;
}

.status-unknown {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 6px;
  background: linear-gradient(135deg, #f5f7fa, #e4e7ed);
  color: #909399;
  border: 1px solid #e4e7ed;
  width: 100%;
  transition: all 0.3s ease;
  text-align: center;
}

/* 刷新按钮样式优化 */
.refresh-btn {
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #409eff, #36cfc9);
  border: none;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

.refresh-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: 0.5s;
}

.refresh-btn:hover::before {
  left: 100%;
}

.refresh-btn:hover {
  transform: rotate(180deg);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.refresh-icon {
  font-size: 16px;
  transition: all 0.3s ease;
}

/* 分页样式优化 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  padding: 10px 0;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #409eff;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: #409eff;
  transform: translateY(-2px);
  transition: all 0.3s ease;
}

/* 移动端适配优化 */
@media screen and (max-width: 768px) {
  .status-container {
    padding: 10px;
    height: auto;
  }

  .card-header {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }

  .title {
    font-size: 16px;
  }

  :deep(.el-table) {
    font-size: 14px;
  }

  :deep(.el-table .el-table__row) {
    height: auto;
  }

  :deep(.el-table__cell) {
    padding: 8px !important;
  }

  :deep(.el-pagination) {
    font-size: 14px;
  }

  :deep(.el-pagination .el-pagination__sizes) {
    display: none;
  }

  :deep(.el-pagination .el-pagination__jump) {
    display: none;
  }

  :deep(.el-form-item) {
    margin-bottom: 10px;
  }

  :deep(.el-input) {
    width: 100%;
  }

  :deep(.el-button) {
    width: 100%;
    margin-top: 10px;
  }

  .refresh-btn {
    width: auto !important;
    margin-top: 0 !important;
    padding: 8px !important;
    min-height: 32px !important;
  }

  .refresh-icon {
    font-size: 14px;
  }

  .status-success,
  .status-error,
  .status-warning,
  .status-pending,
  .status-unknown {
    width: auto !important;
    display: inline-block;
    padding: 4px 8px !important;
    font-size: 12px !important;
    border-radius: 4px !important;
  }
}
</style>
