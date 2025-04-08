<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import request from "@/utils/request.js";
import { Plus } from '@element-plus/icons-vue'
import * as monaco from 'monaco-editor'

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

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表格数据
const tableData = ref([])

// 用户信息缓存
const userCache = reactive({})

// 题目信息缓存
const problemCache = reactive({})

// 题解总数
const solutionCount = ref(0)

// 发布题解相关
const publishDialogVisible = ref(false)
const problemOptions = ref([])
const problemsLoading = ref(false)
const publishing = ref(false)
const publishForm = reactive({
  problemId: null,
  content: ''
})

// 编辑器相关
let editor = null
const editorContainer = ref(null)
const editorOptions = {
  selectOnLineNumbers: true,
  lineNumbers: 'on',
  scrollBeyondLastLine: false,
  wordWrap: 'on',
  minimap: { enabled: false },
  fontSize: 14,
  lineHeight: 1.5
}

// 题目缓存
const problemsCache = ref([]);
const totalProblems = ref(0);
const currentProblemPage = ref(1);
const problemPageSize = ref(100);

// 分批加载题目
const loadMoreProblems = async () => {
  if (problemsCache.value.length >= totalProblems.value && totalProblems.value > 0) {
    return; // 已加载全部题目
  }
  
  problemsLoading.value = true;
  try {
    const response = await request.get('/problem/page', {
      params: {
        pageNum: currentProblemPage.value,
        pageSize: problemPageSize.value
      }
    });
    
    if (response.data?.code === '200' && response.data?.data?.list) {
      // 添加到缓存
      problemsCache.value = [...problemsCache.value, ...response.data.data.list];
      totalProblems.value = response.data.data.total;
      currentProblemPage.value++;
      
      // 更新下拉选项
      problemOptions.value = problemsCache.value.map(item => ({
        id: item.id,
        name: `${item.id} - ${item.name}`
      }));
    }
  } catch (error) {
    console.error('加载更多题目失败:', error);
  } finally {
    problemsLoading.value = false;
  }
};

// 搜索题目 - 使用缓存方案
const remoteSearch = async (query) => {
  if (query) {
    problemsLoading.value = true;
    
    // 如果缓存中的题目不多，先尝试加载更多
    if (problemsCache.value.length < 500 && problemsCache.value.length < totalProblems.value) {
      await loadMoreProblems();
    }
    
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

// 初始化加载
const initProblemOptions = async () => {
  // 重置缓存
  problemsCache.value = [];
  totalProblems.value = 0;
  currentProblemPage.value = 1;
  
  // 加载第一批
  await loadMoreProblems();
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

// 加载数据
const loadData = async () => {
  try {
    const response = await request.get('/solution/page', {
      params: {
        current: currentPage.value,
        size: pageSize.value
      }
    });
    
    const res = response.data;
    if (res.code === 200) {
      tableData.value = res.data.records;
      total.value = res.data.total;
      console.log('成功获取题解数据:', tableData.value);
      
      // 获取所有用户信息
      await Promise.all(tableData.value.map(item => fetchUserInfo(item.userId)));
      
      // 获取所有题目信息
      await Promise.all(tableData.value.map(item => fetchProblemInfo(item.problemId)));
    } else {
      console.error('请求失败:', res.message);
      ElMessage.error(res.message || '获取数据失败');
    }
  } catch (error) {
    console.error('请求异常:', error);
    ElMessage.error('获取数据失败：' + (error.message || '未知错误'));
  }
}

// 获取用户信息
const fetchUserInfo = async (userId) => {
  // 如果缓存中已有该用户信息，则不重复请求
  if (userCache[userId]) return;
  
  try {
    const response = await request.get(`/api/student/${userId}`, {
      headers: { Authorization: 'Bearer ' + token }
    });
    
    if (response.data.code === '200') {
      const userData = response.data.data;
      userCache[userId] = {
        username: userData.username || userData.name || '未知用户',
        avatar: userData.avatar || 'http://localhost:9090/uploads/1743236403200_IMG_0748.JPG' // 默认头像
      };
    } else {
      console.error('获取用户信息失败:', response.data.msg);
      userCache[userId] = {
        username: '未知用户',
        avatar: 'http://localhost:9090/uploads/1743236403200_IMG_0748.JPG' // 默认头像
      };
    }
  } catch (error) {
    console.error('获取用户信息异常:', error);
    userCache[userId] = {
      username: '未知用户',
      avatar: 'http://localhost:9090/uploads/1743236403200_IMG_0748.JPG' // 默认头像
    };
  }
}

// 获取题目信息
const fetchProblemInfo = async (problemId) => {
  // 如果缓存中已有该题目信息，则不重复请求
  if (problemCache[problemId]) return;
  
  try {
    const response = await request.get(`/problem/${problemId}`);
    
    if (response.data.code === '200') {
      const problemData = response.data.data;
      problemCache[problemId] = {
        name: problemData.name || `题目 #${problemId}`
      };
    } else {
      console.error('获取题目信息失败:', response.data.msg);
      problemCache[problemId] = {
        name: `题目 #${problemId}`
      };
    }
  } catch (error) {
    console.error('获取题目信息异常:', error);
    problemCache[problemId] = {
      name: `题目 #${problemId}`
    };
  }
}

// 获取题解总数
const getSolutionCount = async () => {
  try {
    const response = await request.get('/solution/count');
    const res = response.data;
    if (res.code === 200) {
      solutionCount.value = res.data;
      console.log('成功获取题解总数:', solutionCount.value);
    } else {
      console.error('获取题解总数失败:', res.message);
    }
  } catch (error) {
    console.error('获取题解总数异常:', error);
  }
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
  getSolutionCount()
})

// 初始化编辑器
const initEditor = () => {
  // 给一点延迟确保DOM已完全渲染
  setTimeout(() => {
    if (editorContainer.value) {
      // 如果编辑器已存在，先销毁
      if (editor) {
        editor.dispose()
      }
      
      // 创建新的编辑器实例
      editor = monaco.editor.create(editorContainer.value, {
        value: publishForm.content,
        language: 'cpp',
        theme: 'vs-light',
        ...editorOptions
      })
      
      // 监听编辑器内容变化
      editor.onDidChangeModelContent(() => {
        publishForm.content = editor.getValue()
      })
      
      // 手动触发一次布局更新
      window.addEventListener('resize', () => {
        if (editor) {
          editor.layout()
        }
      })
      
      // 立即触发一次布局更新
      if (editor) {
        editor.layout()
      }
    }
  }, 300) // 300ms延迟
}

// 打开发布题解对话框
const openPublishDialog = () => {
  if (!userId) {
    ElMessage.warning('请先登录')
    return
  }
  publishForm.problemId = null
  publishForm.content = ''
  publishDialogVisible.value = true
  
  // 在对话框打开后初始化编辑器
  nextTick(() => {
    initEditor()
  })
}

// 监听对话框打开事件
const handleDialogOpen = () => {
  initProblemOptions();
  // 确保DOM已更新后再初始化编辑器
  nextTick(() => {
    initEditor();
  });
}

// 发布题解
const handlePublish = async () => {
  if (!publishForm.problemId) {
    ElMessage.warning('请选择题目')
    return
  }
  
  if (!publishForm.content.trim()) {
    ElMessage.warning('请输入题解内容')
    return
  }
  
  publishing.value = true
  try {
    const response = await request.post('/solution/add', {
      userId: userId,
      problemId: publishForm.problemId,
      content: publishForm.content
    })
    
    if (response.data.code === 200) {
      ElMessage.success('题解发布成功')
      publishDialogVisible.value = false
      loadData() // 刷新数据
    } else {
      ElMessage.error(response.data.message || '发布失败')
    }
  } catch (error) {
    console.error('发布题解失败:', error)
    ElMessage.error('发布题解失败：' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    publishing.value = false
  }
}

// 监听选择变化
const handleSelectChange = (value) => {
  if (value === -1) {
    // 如果选择了"加载更多"选项
    loadMoreProblems().then(() => {
      // 加载完成后清除选择
      publishForm.problemId = null;
    });
  }
};
</script>

<template>
  <div class="solution-container">
    <!-- 标题区域 -->
    <el-card class="title-card">
      <template #header>
        <div class="card-header">
          <span class="title">题解广场</span>
          <div class="header-actions">
            <el-button type="primary" @click="openPublishDialog">
              <el-icon><Plus /></el-icon>
              发布题解
            </el-button>
            <span class="solution-count">题解数量：{{ solutionCount }}</span>
          </div>
        </div>
      </template>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <el-table :data="tableData" style="width: 100%" border stripe highlight-current-row>
        <el-table-column prop="id" label="#" width="80" align="center" />
        <el-table-column label="发布者" width="180" align="center">
          <template #default="scope">
            <div class="user-info" @click="$router.push('/profilePage')" v-if="userCache[scope.row.userId]">
              <el-avatar 
                :src="userCache[scope.row.userId].avatar" 
                class="user-avatar"
                :size="32"
              />
              <span class="user-name">{{ userCache[scope.row.userId].username }}</span>
            </div>
            <span v-else>加载中...</span>
          </template>
        </el-table-column>
        <el-table-column label="标题" min-width="150" align="center">
          <template #default="scope">
            <a @click="$router.push(`/problem/${scope.row.problemId}`)" class="problem-link" v-if="problemCache[scope.row.problemId]">
              {{ problemCache[scope.row.problemId].name }}
            </a>
            <span v-else>加载中...</span>
          </template>
        </el-table-column>
        <el-table-column prop="likeNum" label="点赞" width="100" align="center">
          <template #default="scope">
            {{ scope.row.likeNum || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="$router.push(`/solution/${scope.row.id}`)">
              查看
            </el-button>
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
    
    <!-- 发布题解对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布题解"
      width="800px"
      @open="handleDialogOpen"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form :model="publishForm" label-width="100px" style="width: 100%">
        <el-form-item label="选择题目" required>
          <div class="problem-select-wrapper">
            <el-select
              v-model="publishForm.problemId"
              filterable
              remote
              reserve-keyword
              placeholder="输入题目关键词搜索"
              :remote-method="remoteSearch"
              :loading="problemsLoading"
              style="width: 100%"
              @change="handleSelectChange"
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
          <div v-if="problemsCache.length < totalProblems" style="margin-top: 8px; text-align: center;">
            <el-button type="primary" link @click="loadMoreProblems" :loading="problemsLoading">
              加载更多题目 (已加载 {{ problemsCache.length }}/{{ totalProblems }})
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="题解内容" required style="width: 100%">
          <div class="code-editor-wrapper">
            <div class="code-editor-container" ref="editorContainer"></div>
          </div>
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
.solution-container {
  padding: 20px;
  background-color: #f5f7fa;
  height: 80vh;
}

.title-card {
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

.solution-count {
  font-size: 14px;
  color: #606266;
}

/* 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.user-info:hover {
  background-color: #f0f9ff;
}

.user-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  transition: all 0.3s;
}

.user-info:hover .user-avatar {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}

.user-name {
  margin-left: 16px;
  font-size: 14px;
  font-weight: bold;
  color: #333;
  transition: all 0.3s;
}

.user-info:hover .user-name {
  color: #1890ff;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.code-editor-wrapper {
  width: 100%;
  position: relative;
}

.code-editor-container {
  height: 400px;
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-bottom: 20px;
  overflow: hidden; /* 防止内容溢出 */
}

/* 确保表单项占满宽度 */
:deep(.el-form-item__content) {
  width: calc(100% - 100px); /* 减去label宽度 */
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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
</style>
