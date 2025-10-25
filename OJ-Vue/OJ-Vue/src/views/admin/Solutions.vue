<template>
  <page-layout 
    title="题解管理" 
    :show-back="false"
  >
    <div class="solutions-container">
      <!-- 统计信息卡片 -->
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-item">
            <div class="stat-value">{{ totalSolutions }}</div>
            <div class="stat-label">总题解数</div>
          </div>
        </div>
      </el-card>

      <!-- 表格区域 -->
      <el-card class="table-card">
        <el-table
          :data="solutionList"
          :loading="loading"
          border
          stripe
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column label="题目" min-width="180" align="center">
            <template #default="{ row }">
              <div v-if="problemCache[row.problemId]">
                <el-link type="primary" @click="handleViewProblem(row.problemId)">
                  {{ problemCache[row.problemId].name }}
                </el-link>
              </div>
              <span v-else>加载中...</span>
            </template>
          </el-table-column>
          <el-table-column label="作者" width="180" align="center">
            <template #default="{ row }">
              <div v-if="userCache[row.userId]" class="user-info">
                <el-avatar 
                  :src="userCache[row.userId].avatar" 
                  :size="32"
                />
                <span class="user-name">{{ userCache[row.userId].username }}</span>
              </div>
              <span v-else>加载中...</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="发布时间" width="180" align="center">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="likeNum" label="点赞数" width="100" align="center" />
          <el-table-column label="操作" width="250" align="center" fixed="right">
            <template #default="{ row }">
              <el-space>
                <el-button 
                  type="primary" 
                  link
                  @click="handleView(row.id)"
                >
                  查看
                </el-button>
                <el-button 
                  type="success" 
                  link
                  @click="handleEdit(row)"
                >
                  编辑
                </el-button>
                <el-popconfirm
                  title="确定要删除这个题解吗？"
                  @confirm="handleDelete(row.id)"
                >
                  <template #reference>
                    <el-button type="danger" link>删除</el-button>
                  </template>
                </el-popconfirm>
              </el-space>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页器 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
      
      <!-- 编辑题解对话框 -->
      <el-dialog
        v-model="editDialogVisible"
        title="编辑题解"
        width="800px"
        :close-on-click-modal="false"
        :close-on-press-escape="false"
      >
        <el-form :model="editForm" label-width="100px">
          <el-form-item label="题解ID">
            <el-input v-model="editForm.id" disabled />
          </el-form-item>
          <el-form-item label="题目">
            <el-input v-model="editForm.problemName" disabled />
          </el-form-item>
          <el-form-item label="作者">
            <el-input v-model="editForm.authorName" disabled />
          </el-form-item>
          <el-form-item label="发布时间">
            <el-input v-model="editForm.createTime" disabled />
          </el-form-item>
          <el-form-item label="点赞数">
            <el-input v-model="editForm.likeNum" disabled />
          </el-form-item>
          <el-form-item label="题解内容" required>
            <el-input
              v-model="editForm.content"
              type="textarea"
              :rows="10"
              placeholder="请输入题解内容"
              maxlength="10000"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="editDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSaveEdit" :loading="editLoading">
              保存
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </page-layout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import PageLayout from '@/components/layout/PageLayout.vue'

const router = useRouter()

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

// 数据状态
const loading = ref(false)
const solutionList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalSolutions = ref(0)

// 缓存
const userCache = ref({})
const problemCache = ref({})

// 编辑相关
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editForm = ref({
  id: null,
  problemName: '',
  authorName: '',
  createTime: '',
  likeNum: 0,
  content: ''
})

// 获取题解列表
const fetchSolutions = async () => {
  loading.value = true
  try {
    const res = await request.get('/solution/page', {
      params: {
        current: currentPage.value,
        size: pageSize.value
      }
    })
    
    if (res.data.code === 200) {
      solutionList.value = res.data.data.records
      total.value = res.data.data.total
      
      // 获取用户和题目信息
      await Promise.all([
        ...solutionList.value.map(item => fetchUserInfo(item.userId)),
        ...solutionList.value.map(item => fetchProblemInfo(item.problemId))
      ])
    }
  } catch (error) {
    console.error('获取题解列表失败:', error)
    ElMessage.error('获取题解列表失败')
  } finally {
    loading.value = false
  }
}

// 获取题解总数
const fetchTotalSolutions = async () => {
  try {
    const res = await request.get('/solution/count')
    if (res.data.code === 200) {
      totalSolutions.value = res.data.data
    }
  } catch (error) {
    console.error('获取题解总数失败:', error)
  }
}

// 获取用户信息
const fetchUserInfo = async (userId) => {
  if (userCache.value[userId]) return
  
  try {
    const res = await request.get(`/api/student/${userId}`, {
      headers: { 
        Authorization: 'Bearer ' + token 
      }
    })
    
    if (res.data.code === '200') {
      userCache.value[userId] = {
        username: res.data.data.username || res.data.data.name || '未知用户',
        avatar: res.data.data.avatar || 'http://localhost:9090/uploads/1743236403200_IMG_0748.JPG'
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    userCache.value[userId] = {
      username: '未知用户',
      avatar: 'http://localhost:9090/uploads/1743236403200_IMG_0748.JPG'
    }
  }
}

// 获取题目信息
const fetchProblemInfo = async (problemId) => {
  if (problemCache.value[problemId]) return
  
  try {
    const res = await request.get(`/problem/${problemId}`)
    if (res.data.code === '200') {
      problemCache.value[problemId] = {
        name: res.data.data.name || `题目 #${problemId}`
      }
    }
  } catch (error) {
    console.error('获取题目信息失败:', error)
    problemCache.value[problemId] = {
      name: `题目 #${problemId}`
    }
  }
}

// 格式化时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  try {
    if (dateTimeStr.includes('T')) {
      return dateTimeStr.replace('T', ' ').split('.')[0]
    }
    if (!isNaN(dateTimeStr)) {
      const date = new Date(Number(dateTimeStr))
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
  } catch (error) {
    return dateTimeStr
  }
}

// 查看题解
const handleView = (id) => {
  router.push(`/solution/${id}`)
}

// 查看题目
const handleViewProblem = (id) => {
  router.push(`/problem/${id}`)
}

// 编辑题解
const handleEdit = async (row) => {
  try {
    // 获取题解的详细信息
    const res = await request.get(`/solution/${row.id}`)
    if (res.data.code === 200) {
      const solution = res.data.data
      
      // 填充编辑表单
      editForm.value = {
        id: solution.id,
        problemName: problemCache.value[solution.problemId]?.name || `题目 #${solution.problemId}`,
        authorName: userCache.value[solution.userId]?.username || '未知用户',
        createTime: formatDateTime(solution.createTime),
        likeNum: solution.likeNum,
        content: solution.content || ''
      }
      
      editDialogVisible.value = true
    } else {
      ElMessage.error('获取题解详情失败')
    }
  } catch (error) {
    console.error('获取题解详情失败:', error)
    ElMessage.error('获取题解详情失败')
  }
}

// 保存编辑
const handleSaveEdit = async () => {
  if (!editForm.value.content.trim()) {
    ElMessage.warning('题解内容不能为空')
    return
  }
  
  editLoading.value = true
  try {
    const res = await request.put(`/solution/edit/${editForm.value.id}`, {
      content: editForm.value.content
    })
    
    if (res.data.code === 200) {
      ElMessage.success('编辑成功')
      editDialogVisible.value = false
      await fetchSolutions() // 刷新列表
    } else {
      ElMessage.error(res.data.message || '编辑失败')
    }
  } catch (error) {
    console.error('编辑题解失败:', error)
    ElMessage.error('编辑失败')
  } finally {
    editLoading.value = false
  }
}

// 删除题解
const handleDelete = async (id) => {
  try {
    const res = await request.delete(`/solution/delete/${id}`)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      await fetchSolutions()
      await fetchTotalSolutions()
    } else {
      ElMessage.error(res.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除题解失败:', error)
    ElMessage.error('删除失败')
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchSolutions()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchSolutions()
}

// 初始化
onMounted(() => {
  fetchSolutions()
  fetchTotalSolutions()
})
</script>

<style scoped>
.solutions-container {
  padding: 24px;
}

.stat-card {
  margin-bottom: 24px;
}

.stat-content {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
}

.stat-label {
  margin-top: 8px;
  color: var(--color-text);
}

.table-card {
  margin-bottom: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 14px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .solutions-container {
    padding: 12px;
  }
  
  .stat-content {
    flex-direction: column;
    gap: 16px;
  }
  
  :deep(.el-table) {
    font-size: 14px;
  }
  
  .user-info {
    flex-direction: column;
    gap: 4px;
  }
}
</style> 