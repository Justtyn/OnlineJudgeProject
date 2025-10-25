<template>
  <div class="discussions-container">
    <!-- 标题和刷新按钮 -->
    <div class="header">
      <h2>讨论管理</h2>
      <el-button type="primary" @click="fetchDiscussions">
        <el-icon><Refresh /></el-icon>刷新
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="queryForm.title"
        placeholder="按标题搜索"
        clearable
        @clear="handleQuery"
        style="width: 300px"
        class="search-input"
      >
        <template #append>
          <el-button @click="handleQuery">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      <el-input
        v-model="queryForm.problemId"
        placeholder="按题目ID搜索"
        clearable
        @clear="handleQuery"
        style="width: 300px"
        class="search-input"
      >
        <template #append>
          <el-button @click="handleQuery">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 讨论列表 -->
    <el-card class="table-card">
      <el-table
        :data="discussionList"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="title" label="标题">
          <template #default="{ row }">
            <el-link
              type="primary"
              @click="$router.push(`/discuss/${row.id}`)"
              :underline="false"
            >
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column prop="username" label="作者" width="120">
          <template #default="{ row }">
            <el-link
              type="primary"
              @click="$router.push(`/userProfile/${row.userId}`)"
              :underline="false"
            >
              {{ row.username || '未知用户' }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column prop="problemId" label="题目" width="120">
          <template #default="{ row }">
            <el-link
              type="primary"
              @click="$router.push(`/problem/${row.problemId}`)"
              :underline="false"
            >
              {{ row.problemId }}
            </el-link>
          </template>
        </el-table-column>
        
        <el-table-column prop="viewNum" label="浏览量" width="100" align="center" />
        
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewComments(row)">查看评论</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm
              title="确定要删除这条讨论吗？"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页器 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="(size) => handleTableChange({ current: pagination.current, pageSize: size })"
        @current-change="(current) => handleTableChange({ current, pageSize: pagination.pageSize })"
      />
    </div>

    <!-- 编辑讨论对话框 -->
    <a-modal
      v-model:visible="editModalVisible"
      title="编辑讨论"
      @ok="handleEditSubmit"
      :confirmLoading="editLoading"
    >
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="关联题目" required>
          <div class="problem-select-wrapper">
            <el-select
              v-model="editForm.problemId"
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
        </a-form-item>
        <a-form-item label="标题" required>
          <a-input v-model:value="editForm.title" />
        </a-form-item>
        <a-form-item label="内容" required>
          <a-textarea v-model:value="editForm.content" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 评论管理对话框 -->
    <el-dialog
      v-model="commentsModalVisible"
      title="评论管理"
      width="800px"
      :z-index="2000"
      @ok="handleCommentsModalOk"
    >
      <div class="comments-tree" v-loading="commentsLoading">
        <template v-if="commentList.length">
          <template v-for="comment in commentList" :key="comment.id">
            <CommentNode 
              :comment="comment" 
              :level="0"
              @delete="handleDeleteComment"
              @edit="handleEditComment"
            />
          </template>
        </template>
        <el-empty
          v-else
          description="暂无评论"
          :image-size="200"
        >
          <template #description>
            <span>暂无评论内容</span>
          </template>
        </el-empty>
      </div>
    </el-dialog>

    <!-- 编辑评论对话框 -->
    <el-dialog
      v-model="editCommentModalVisible"
      title="编辑评论"
      width="500px"
      :z-index="2100"
      :append-to-body="true"
      @close="handleEditCommentClose"
    >
      <el-form :model="editCommentForm" label-width="80px">
        <el-form-item label="内容" required>
          <el-input
            v-model="editCommentForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入评论内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editCommentModalVisible = false">取消</el-button>
          <el-button type="primary" :loading="editCommentLoading" @click="handleEditCommentSubmit">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import request from '@/utils/request'
import CommentNode from './CommentNode.vue'  // 修改导入路径

// 接口定义
interface Discussion {
  id: number
  title: string
  content: string
  userId: number
  problemId: number
  createTime: string
  updateTime: string
  viewNum: number
  username: string
}

interface Comment {
  id: number
  content: string
  userId: number
  username: string
  discussId: number
  createTime: string
  parentCommentId: number | null
  replyToUserId: number | null
  children?: Comment[]
}

// 查询表单
const queryForm = reactive({
  title: '',
  problemId: ''
})

// 表格列定义
const columns = [
  {
    title: '标题',
    dataIndex: 'title',
    key: 'title',
    slots: {
      customRender: 'title'
    }
  },
  {
    title: '作者',
    dataIndex: 'username',
    key: 'username',
    slots: {
      customRender: 'username'
    }
  },
  {
    title: '题目',
    dataIndex: 'problemId',
    key: 'problemId',
    slots: {
      customRender: 'problemId'
    }
  },
  {
    title: '浏览量',
    dataIndex: 'viewNum',
    key: 'viewNum',
    align: 'center',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 150,
    align: 'center'
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    align: 'center'
  }
]

const commentColumns = [
  {
    title: '内容',
    dataIndex: 'content',
    key: 'content',
  },
  {
    title: '作者',
    dataIndex: 'username',
    key: 'userId',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 数据相关
const discussionList = ref<Discussion[]>([])
const loading = ref(false)
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
})

// 讨论编辑相关
const editModalVisible = ref(false)
const editLoading = ref(false)
const editForm = ref<Partial<Discussion>>({})

// 评论管理相关
const commentsModalVisible = ref(false)
const commentsLoading = ref(false)
const commentList = ref<Comment[]>([])
const currentDiscussId = ref<number>()

// 评论编辑相关
const editCommentModalVisible = ref(false)
const editCommentLoading = ref(false)
const editCommentForm = ref<Partial<Comment>>({})

// 添加题目选择相关的响应式数据
const problemOptions = ref([])
const problemsLoading = ref(false)
const problemsCache = ref([])

// 格式化时间
const formatDateTime = (dateTimeStr: string) => {
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

// 获取用户信息
const getUserInfo = async (userId: number) => {
  try {
    // 获取token
    const localUser = localStorage.getItem('student-user') 
      ? JSON.parse(localStorage.getItem('student-user'))
      : localStorage.getItem('admin-user')
        ? JSON.parse(localStorage.getItem('admin-user'))
        : null;
    
    const token = localUser ? localUser.token : '';
    
    const response = await request.get(`/api/student/${userId}`, {
      headers: { Authorization: 'Bearer ' + token }
    })
    
    if (response.data.code === '200') {
      const userInfo = response.data.data
      // 直接返回用户信息对象
      return userInfo
    }
    return null
  } catch (error) {
    console.error('获取用户信息失败:', error)
    return null
  }
}

// 查询讨论列表
const fetchDiscussions = async () => {
  loading.value = true
  try {
    let response
    if (queryForm.problemId) {
      // 按题目ID搜索讨论
      response = await request.get(`/discuss/problem/${queryForm.problemId}`)
      if (response.data.code === '200') {
        const discussList = response.data.data
        const discussWithUserInfo = await Promise.all(
          discussList.map(async (discuss: Discussion) => {
            const userInfo = await getUserInfo(discuss.userId)
            return {
              ...discuss,
              username: userInfo ? userInfo.username : '未知用户'
            }
          })
        )
        discussionList.value = discussWithUserInfo
        pagination.value.total = discussList.length
      }
    } else if (queryForm.title) {
      // 按标题搜索讨论
      response = await request.get('/discuss/search', {
        params: { title: queryForm.title }
      })
      if (response.data.code === '200') {
        const discussList = response.data.data
        const discussWithUserInfo = await Promise.all(
          discussList.map(async (discuss: Discussion) => {
            const userInfo = await getUserInfo(discuss.userId)
            return {
              ...discuss,
              username: userInfo ? userInfo.username : '未知用户'
            }
          })
        )
        discussionList.value = discussWithUserInfo
        pagination.value.total = discussList.length
      }
    } else {
      // 分页查询所有讨论
      response = await request.get('/discuss/page', {
        params: {
          pageNum: pagination.value.current,
          pageSize: pagination.value.pageSize
        }
      })
      if (response.data.code === '200') {
        const discussList = response.data.data.list
        pagination.value.total = response.data.data.total
        const discussWithUserInfo = await Promise.all(
          discussList.map(async (discuss: Discussion) => {
            const userInfo = await getUserInfo(discuss.userId)
            return {
              ...discuss,
              username: userInfo ? userInfo.username : '未知用户'
            }
          })
        )
        discussionList.value = discussWithUserInfo
      }
    }
  } catch (error) {
    console.error('获取讨论列表失败:', error)
    message.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 查询按钮处理
const handleQuery = () => {
  pagination.value.current = 1
  fetchDiscussions()
}

// 重置按钮处理
const handleReset = () => {
  queryForm.title = ''
  queryForm.problemId = ''
  pagination.value.current = 1
  fetchDiscussions()
}

// 表格分页变化
const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  fetchDiscussions()
}

// 初始化题目选项
const initProblemOptions = async () => {
  problemsLoading.value = true
  try {
    const response = await request.get('/problem/simple')
    if (response.data?.data) {
      problemsCache.value = response.data.data
      problemOptions.value = response.data.data.map(item => ({
        id: item.id,
        name: `${item.id} - ${item.name}`
      }))
    }
  } catch (error) {
    console.error('获取题目列表失败:', error)
    message.error('获取题目列表失败：' + (error.response?.data?.msg || error.message))
  } finally {
    problemsLoading.value = false
  }
}

// 重置题目搜索
const resetProblemSearch = () => {
  problemsLoading.value = true
  problemOptions.value = problemsCache.value.slice(0, 20).map(item => ({
    id: item.id,
    name: `${item.id} - ${item.name}`
  }))
  problemsLoading.value = false
}

// 修改现有的 handleEdit 方法
const handleEdit = async (record) => {
  editForm.value = {
    id: record.id,
    title: record.title,
    content: record.content,
    problemId: record.problemId
  }
  editModalVisible.value = true
  // 加载题目选项
  await initProblemOptions()
}

// 修改现有的 handleEditSubmit 方法
const handleEditSubmit = async () => {
  if (!editForm.value.title?.trim() || !editForm.value.content?.trim()) {
    message.warning('标题和内容不能为空')
    return
  }

  if (!editForm.value.problemId) {
    message.warning('请选择关联题目')
    return
  }

  editLoading.value = true
  try {
    const response = await request.put('/discuss', {
      id: editForm.value.id,
      title: editForm.value.title.trim(),
      content: editForm.value.content.trim(),
      problemId: editForm.value.problemId
    })
    
    if (response.data.code === '200') {
      message.success('修改成功')
      editModalVisible.value = false
      fetchDiscussions()
    } else {
      message.error(response.data.msg || '修改失败')
    }
  } catch (error) {
    console.error('修改失败:', error)
    message.error('修改失败')
  } finally {
    editLoading.value = false
  }
}

// 删除讨论
const handleDelete = async (id: number) => {
  try {
    const response = await request.delete(`/discuss/${id}`)
    if (response.data.code === '200') {
      message.success('删除成功')
      fetchDiscussions()
    } else {
      message.error(response.data.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

// 查看评论
const handleViewComments = async (record: Discussion) => {
  currentDiscussId.value = record.id
  commentsModalVisible.value = true
  await fetchComments(record.id)
}

// 获取评论列表
const fetchComments = async (discussId: number) => {
  commentsLoading.value = true
  try {
    const response = await request.get('/discuss/comment/list', {
      params: { discussId }
    })
    if (response.data.code === '200') {
      const comments = response.data.data
      // 递归获取所有评论（包括子评论）的用户信息
      const getCommentsWithUserInfo = async (comments: Comment[]): Promise<Comment[]> => {
        if (!comments) return []
        
        const processedComments = await Promise.all(
          comments.map(async (comment: Comment) => {
            const userInfo = await getUserInfo(comment.userId)
            const processedComment = {
              ...comment,
              username: userInfo ? userInfo.username : '未知用户'
            }
            
            if (comment.children) {
              processedComment.children = await getCommentsWithUserInfo(comment.children)
            }
            
            return processedComment
          })
        )
        
        return processedComments
      }

      commentList.value = await getCommentsWithUserInfo(comments)
    }
  } catch (error) {
    console.error('获取评论列表失败:', error)
    message.error('获取评论失败')
  } finally {
    commentsLoading.value = false
  }
}

// 编辑评论
const handleEditComment = (record: Comment) => {
  editCommentForm.value = {
    id: record.id,
    content: record.content
  }
  editCommentModalVisible.value = true
}

// 提交评论编辑
const handleEditCommentSubmit = async () => {
  if (!editCommentForm.value.content?.trim()) {
    message.warning('评论内容不能为空')
    return
  }

  editCommentLoading.value = true
  try {
    const response = await request.put('/discuss/comment/update', editCommentForm.value)
    if (response.data.code === '200') {
      message.success('修改成功')
      editCommentModalVisible.value = false
      if (currentDiscussId.value) {
        await fetchComments(currentDiscussId.value)
      }
    } else {
      message.error(response.data.msg || '修改失败')
    }
  } catch (error) {
    console.error('修改评论失败:', error)
    message.error('修改失败')
  } finally {
    editCommentLoading.value = false
  }
}

// 删除评论
const handleDeleteComment = async (id: number) => {
  try {
    const response = await request.delete(`/discuss/comment/delete/${id}`)
    if (response.data.code === '200') {
      message.success('删除成功')
      if (currentDiscussId.value) {
        await fetchComments(currentDiscussId.value)
      }
    } else {
      message.error(response.data.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除评论失败:', error)
    message.error('删除失败')
  }
}

// 关闭评论管理对话框
const handleCommentsModalOk = () => {
  commentsModalVisible.value = false
  commentList.value = []
  currentDiscussId.value = undefined
}

// 关闭评论编辑对话框
const handleEditCommentClose = () => {
  editCommentForm.value = {}
}

onMounted(() => {
  fetchDiscussions()
})
</script>

<style scoped>
.discussions-container {
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
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-input {
  max-width: 300px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-dialog__body) {
  padding-top: 20px;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .discussions-container {
    padding: 12px;
  }
  
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input {
    max-width: 100%;
  }
}

/* 添加新的样式 */
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
  transition: all 0.3s ease;
}

.reset-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.2);
}

.nested-comments {
  padding: 8px 16px;
}

.nested-comment-item {
  padding: 12px;
  border-bottom: 1px solid var(--bg-color-mute);
}

.nested-comment-item:last-child {
  border-bottom: none;
}

.comment-content {
  margin-bottom: 8px;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  color: var(--color-text);
  font-size: 12px;
}

.comment-author {
  color: #1890ff;
  cursor: pointer;
}

.comment-time {
  flex: 1;
}

.comments-tree {
  max-height: 600px;
  overflow-y: auto;
  padding: 16px;
  min-height: 300px;
  display: flex;
  flex-direction: column;
}

.comment-node {
  margin-bottom: 16px;
  padding: 12px;
  background: var(--color-background);
  border-radius: 4px;
  border: 1px solid var(--bg-color-mute);
}

.comment-node.level-0 {
  margin-left: 0;
}

.comment-node.level-1 {
  margin-left: 24px;
  border-left: 2px solid #1890ff;
}

.comment-node.level-2 {
  margin-left: 48px;
  border-left: 2px solid #52c41a;
}

.comment-node.level-3,
.comment-node.level-4,
.comment-node.level-5 {
  margin-left: 72px;
  border-left: 2px solid #faad14;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 500;
  color: #1890ff;
  margin-right: 12px;
}

.comment-time {
  color: var(--color-text);
  font-size: 12px;
  flex: 1;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.comment-text {
  color: var(--color-heading);
  line-height: 1.5;
  word-break: break-word;
}

.comment-children {
  margin-top: 12px;
}

/* 添加空状态样式 */
:deep(.el-empty) {
  margin: auto;
  padding: 32px 0;
}
</style> 