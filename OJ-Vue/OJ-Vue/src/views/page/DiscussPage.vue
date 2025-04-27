<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Plus } from '@element-plus/icons-vue'
import request from "@/utils/request.js"
import { useRouter } from 'vue-router'

const router = useRouter()

// 查询表单数据
const queryForm = reactive({
  title: '',
  problemId: ''
})

// 分页相关数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const tableData = ref([])

// 获取token和用户ID
const getAuthInfo = () => {
  const localUser = localStorage.getItem('student-user') 
    ? JSON.parse(localStorage.getItem('student-user'))
    : localStorage.getItem('admin-user')
      ? JSON.parse(localStorage.getItem('admin-user'))
      : null;
  
  return localUser ? {
    token: localUser.token,
    userId: localUser.id
  } : { token: '', userId: null };
}

const { token, userId } = getAuthInfo();

// 加载讨论列表数据
const loadData = async () => {
  try {
    let response;
    
    // 如果输入了ID，优先按ID搜索
    if (queryForm.problemId) {
      response = await request.get(`/discuss/${queryForm.problemId}`);
      if (response.data.code === '200') {
        const discuss = response.data.data;
        const userInfo = await getUserInfo(discuss.userId);
        tableData.value = [{
          ...discuss,
          username: userInfo ? userInfo.username : '未知用户'
        }];
        total.value = 1;
      }
    } 
    // 如果输入了标题，按标题搜索
    else if (queryForm.title) {
      response = await request.get('/discuss/search', {
        params: {
          title: queryForm.title
        }
      });
      if (response.data.code === '200') {
        const discussList = response.data.data;
        // 获取每个讨论的用户信息
        const discussWithUserInfo = await Promise.all(
          discussList.map(async (discuss) => {
            const userInfo = await getUserInfo(discuss.userId);
            return {
              ...discuss,
              username: userInfo ? userInfo.username : '未知用户'
            };
          })
        );
        tableData.value = discussWithUserInfo;
        total.value = discussList.length;
      }
    } 
    // 如果都没有输入，使用原有的分页查询
    else {
      response = await request.get('/discuss/page', {
        params: {
          pageNum: currentPage.value,
          pageSize: pageSize.value
        }
      });
      if (response.data.code === '200') {
        const discussList = response.data.data.list;
        total.value = response.data.data.total;

        const discussWithUserInfo = await Promise.all(
          discussList.map(async (discuss) => {
            const userInfo = await getUserInfo(discuss.userId);
            return {
              ...discuss,
              username: userInfo ? userInfo.username : '未知用户'
            };
          })
        );
        tableData.value = discussWithUserInfo;
      }
    }

    if (response.data.code !== '200') {
      ElMessage.error(response.data.msg || '获取讨论列表失败');
    }
  } catch (error) {
    console.error('加载数据失败:', error);
    ElMessage.error('获取数据失败：' + (error.message || '未知错误'));
  }
};

// 获取用户信息
const getUserInfo = async (userId) => {
  try {
    // 检查是否有token
    if (!token) {
      console.warn('Token not found')
      return null
    }

    const response = await request.get(`/api/student/${userId}`, {
      headers: {
        'Authorization': 'Bearer ' + token
      }
    })
    
    if (response.data.code === '200') {
      return response.data.data
    }
    return null
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return null
  }
}

// 查询按钮处理
const handleQuery = () => {
  currentPage.value = 1
  loadData()
}

// 重置按钮处理
const handleReset = () => {
  queryForm.title = ''
  queryForm.problemId = ''
  currentPage.value = 1
  loadData()
}

// 分页大小变化处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadData()
}

// 当前页变化处理
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

// 查看讨论详情
const handleViewDiscuss = (row) => {
  router.push(`/discuss/${row.id}`)
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

// 发布讨论相关
const publishDialogVisible = ref(false)
const problemOptions = ref([])
const problemsLoading = ref(false)
const publishing = ref(false)
const publishForm = reactive({
  problemId: null,
  title: '',
  content: ''
})

// 题目缓存
const problemsCache = ref([]);
const totalProblems = ref(0);
const currentProblemPage = ref(1);
const problemPageSize = ref(100);
const loadingAllProblems = ref(false);

// 初始化加载
const initProblemOptions = async () => {
  // 重置缓存
  problemsCache.value = [];
  totalProblems.value = 0;
  currentProblemPage.value = 1;
  
  try {
    const response = await request.get('/problem/simple')
    if (response.data?.data) {
      // 更新缓存
      problemsCache.value = response.data.data;
      totalProblems.value = response.data.data.length;
      
      // 更新选项列表
      problemOptions.value = response.data.data.map(item => ({
        id: item.id,
        name: `${item.id} - ${item.name}`
      }));
    }
  } catch (error) {
    console.error('获取题目列表失败:', error);
    ElMessage.error('获取题目列表失败：' + (error.response?.data?.msg || error.message));
  } finally {
    problemsLoading.value = false;
  }
};

// 搜索题目 - 使用缓存方案
const remoteSearch = async (query) => {
  if (query) {
    problemsLoading.value = true;
    
    // 从缓存中过滤
    const filteredProblems = problemsCache.value.filter(item => 
      item.id.toString().includes(query) || 
      item.name.toLowerCase().includes(query.toLowerCase())
    );
    
    problemOptions.value = filteredProblems.map(item => ({
      id: item.id,
      name: `${item.id} - ${item.name}`
    }));
    
    problemsLoading.value = false;
  }
};

// 重置题目搜索
const resetProblemSearch = () => {
  problemsLoading.value = true;
  
  // 创建加载提示
  const loading = ElLoading.service({
    lock: true,
    text: '重新加载题目列表...',
    background: 'rgba(0, 0, 0, 0.7)'
  });
  
  // 显示已缓存的题目
  problemOptions.value = problemsCache.value.slice(0, 20).map(item => ({
    id: item.id,
    name: `${item.id} - ${item.name}`
  }));
  
  loading.close();
  problemsLoading.value = false;
};

// 打开发布讨论对话框
const openPublishDialog = () => {
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }
  publishForm.problemId = null
  publishForm.title = ''
  publishForm.content = ''
  publishDialogVisible.value = true
  
  // 初始化题目选项
  initProblemOptions()
}

// 发布讨论
const handlePublish = async () => {
  if (!publishForm.problemId) {
    ElMessage.warning('请选择题目')
    return
  }
  
  if (!publishForm.title.trim()) {
    ElMessage.warning('请输入讨论标题')
    return
  }
  
  if (!publishForm.content.trim()) {
    ElMessage.warning('请输入讨论内容')
    return
  }
  
  publishing.value = true
  try {
    const response = await request.post('/discuss', {
      userId: userId,
      problemId: publishForm.problemId,
      title: publishForm.title,
      content: publishForm.content
    })
    
    if (response.data.code === '200') {
      ElMessage.success('讨论发布成功')
      publishDialogVisible.value = false
      loadData() // 刷新数据
    } else {
      ElMessage.error(response.data.message || '发布失败')
    }
  } catch (error) {
    console.error('发布讨论失败:', error)
    ElMessage.error('发布讨论失败：' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    publishing.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="discuss-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <template #header>
        <div class="card-header">
          <span class="title">讨论列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="openPublishDialog">
              <el-icon><Plus /></el-icon>
              发布讨论
            </el-button>
            <el-button type="primary" size="small" circle @click="loadData" title="刷新数据">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索表单 -->
      <el-form :model="queryForm" inline>
        <el-form-item>
          <el-input v-model="queryForm.title" placeholder="按标题搜索" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryForm.problemId" placeholder="按题目ID搜索" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <el-table :data="tableData" style="width: 100%" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" align="left">
          <template #default="scope">
            <a class="discuss-link" @click="handleViewDiscuss(scope.row)">{{ scope.row.title }}</a>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="作者" width="120" align="center">
          <template #default="scope">
            <a class="problem-link" @click="$router.push(`/userProfile/${scope.row.userId}`)">
              {{ scope.row.username }}
            </a>
          </template>
        </el-table-column>
        <el-table-column prop="problemId" label="相关题目" width="120" align="center">
          <template #default="scope">
            <a @click="$router.push(`/problem/${scope.row.problemId}`)" class="problem-link">
              {{ scope.row.problemId }}
            </a>
          </template>
        </el-table-column>
        <el-table-column prop="viewNum" label="浏览数" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.updateTime) }}
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
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 发布讨论对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布讨论"
      width="800px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form :model="publishForm" label-width="100px" style="width: 100%">
        <el-form-item label="选择题目" required>
          <div class="problem-select-wrapper">
            <el-select
              v-model="publishForm.problemId"
              filterable
              placeholder="输入题目关键词搜索"
              :loading="problemsLoading"
              style="width: 100%"
            >
              <el-option
                v-for="item in problemOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
            <el-button 
              class="reset-button" 
              type="primary" 
              plain 
              size="small" 
              @click="resetProblemSearch"
              :disabled="problemsLoading"
            >
              重置
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="讨论标题" required>
          <el-input v-model="publishForm.title" placeholder="请输入讨论标题" />
        </el-form-item>
        <el-form-item label="讨论内容" required>
          <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入讨论内容"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="publishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePublish" :loading="publishing">
            发布
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.discuss-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 80vh;
  max-width: 1200px;
  margin: 0 auto;
}

/* 添加移动端适配样式 */
@media screen and (max-width: 768px) {
  .discuss-container {
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
  
  .header-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  :deep(.el-form) {
    padding: 10px 0;
  }
  
  :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 10px;
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
  
  :deep(.el-pagination) {
    font-size: 14px;
  }
  
  :deep(.el-dialog) {
    width: 90% !important;
    margin: 0 auto;
  }
  
  :deep(.el-dialog__body) {
    padding: 15px;
  }
  
  .problem-select-wrapper {
    flex-direction: column;
  }
  
  .reset-button {
    width: 100%;
    margin-top: 10px;
  }
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

.discuss-link {
  color: #1890ff;
  cursor: pointer;
  text-decoration: none;
}

.discuss-link:hover {
  text-decoration: underline;
}

.problem-link {
  color: #1890ff;
  cursor: pointer;
  text-decoration: underline;
}

.problem-link:hover {
  color: #40a9ff;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.problem-select-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 10px;
}

.problem-select-wrapper .el-select {
  flex: 1;
}

.reset-button {
  flex-shrink: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>