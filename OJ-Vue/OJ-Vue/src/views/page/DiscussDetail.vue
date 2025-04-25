<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'
import CommentItem from '@/components/CommentItem.vue'

// 路由
const route = useRoute()
const router = useRouter()

// 讨论详情
const discussData = ref({ comments: [] })
const userInfo = ref(null)
const newComment = ref('')
const replyTo = ref(null)
const commentPage = ref(1)
const commentPageSize = ref(10)
const totalComments = ref(0)

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

// 获取讨论详情
const fetchDiscussDetail = async (id) => {
  try {
    const res = await request.get(`/discuss/${id}`)
    if (res.data.code === '200') {
      discussData.value = res.data.data
      // 获取发布者信息
      await fetchUserInfo(res.data.data.userId)
      // 加载评论列表
      await loadMoreComments()
    }
  } catch (e) {
    console.error('获取讨论详情失败：', e)
    ElMessage.error('获取讨论详情失败')
  }
}

// 获取用户信息
const fetchUserInfo = async (userId) => {
  try {
    const response = await request.get(`/api/student/${userId}`, {
      headers: { Authorization: 'Bearer ' + token }
    })
    
    if (response.data.code === '200') {
      const userData = response.data.data
      userInfo.value = {
        username: userData.name || userData.username || '未知用户',
        avatar: userData.avatar || 'http://localhost:9090/uploads/1743236403200_IMG_0748.JPG'
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    userInfo.value = {
      username: '未知用户',
      avatar: 'http://localhost:9090/uploads/1743236403200_IMG_0748.JPG'
    }
  }
}

// 格式化时间
const formatDateTime = (s) => {
  if (!s) return '-'
  if (s.includes('T')) return s.replace('T', ' ').split('.')[0]
  if (!isNaN(s)) {
    const d = new Date(+s)
    return d.toLocaleString('zh-CN', {
      year:'numeric', month:'2-digit', day:'2-digit',
      hour:'2-digit', minute:'2-digit', second:'2-digit', hour12:false
    }).replace(',', '')
  }
  return s
}

// 返回列表
const goBack = () => router.back()

// 处理回复
const handleReply = (comment) => {
  replyTo.value = comment
  newComment.value = `@${comment.username} `
}

// 加载更多评论
const loadMoreComments = async () => {
  try {
    const response = await request.get(`/discuss/comment/list`, {
      params: {
        discussId: discussData.value.id
      }
    })
    if (response.data.code === '200') {
      // 获取所有评论的用户信息
      const comments = response.data.data
      for (let comment of comments) {
        try {
          const userResponse = await request.get(`/api/student/${comment.userId}`, {
            headers: { Authorization: 'Bearer ' + token }
          })
          if (userResponse.data.code === '200') {
            const userData = userResponse.data.data
            comment.username = userData.name || userData.username || '未知用户'
            comment.avatar = userData.avatar || 'http://localhost:9090/uploads/1743236403200_IMG_0748.JPG'
          }
        } catch (error) {
          console.error('获取评论用户信息失败:', error)
          comment.username = '未知用户'
          comment.avatar = 'http://localhost:9090/uploads/1743236403200_IMG_0748.JPG'
        }
      }
      discussData.value.comments = comments
      totalComments.value = comments.length
    }
  } catch (error) {
    console.error('加载评论失败:', error)
    ElMessage.error('加载失败')
  }
}

// 提交评论
const submitComment = async () => {
  if (!userId) {
    ElMessage.warning('请先登录后再评论')
    return
  }

  if (!newComment.value.trim()) {
    return
  }

  try {
    const commentData = {
      discussId: discussData.value.id,
      content: newComment.value,
      userId: userId,
      parentCommentId: replyTo.value ? replyTo.value.id : null,
      replyToUserId: replyTo.value ? replyTo.value.userId : null
    }

    const response = await request.post('/discuss/comment/save', commentData)

    if (response.data.code === '200') {
      ElMessage.success('评论成功')
      
      // 清空评论内容和回复对象
      newComment.value = ''
      replyTo.value = null

      // 重新加载评论列表
      await loadMoreComments()
    } else {
      ElMessage.error(response.data.msg || '评论失败')
    }
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error('评论失败：' + (error.response?.data?.msg || error.message || '未知错误'))
  }
}

// 处理键盘事件
const handleKeyDown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    submitComment()
  }
}

onMounted(async () => {
  await fetchDiscussDetail(route.params.id)
})
</script>

<template>
  <div class="discuss-container">
    <el-card class="table-card">
      <!-- 卡片头部 -->
      <template #header>
        <div class="card-header">
          <span class="title">讨论详情</span>
          <el-button type="primary" size="small" @click="goBack" title="返回列表">
            返回列表
          </el-button>
        </div>
      </template>

      <!-- 信息描述表格 -->
      <el-descriptions
        border
        :column="2"
        class="detail-table"
      >
        <el-descriptions-item label="讨论ID">
          {{ discussData.id }}
        </el-descriptions-item>
        <el-descriptions-item label="题目ID">
          <a class="problem-link" @click="$router.push(`/problem/${discussData.problemId}`)">
            {{ discussData.problemId }}
          </a>
        </el-descriptions-item>
        <el-descriptions-item label="发布者">
          <div class="user-info" v-if="userInfo" @click="$router.push(`/userProfile/${discussData.userId}`)">
            <el-avatar 
              :src="userInfo.avatar" 
              class="user-avatar"
              :size="32"
            />
            <span class="user-name">{{ userInfo.username }}</span>
          </div>
          <span v-else>加载中...</span>
        </el-descriptions-item>
        <el-descriptions-item label="浏览量">
          {{ discussData.viewNum || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="发布时间" :span="2">
          {{ formatDateTime(discussData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">
          {{ formatDateTime(discussData.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 讨论内容 -->
      <div class="content-section">
        <h2 class="discuss-title">{{ discussData.title }}</h2>
        <div class="discuss-content">{{ discussData.content }}</div>
      </div>

      <!-- 评论区 -->
      <div class="comments-section">
        <h3 class="section-title">评论区</h3>
        
        <!-- 评论列表 -->
        <div class="comments-list">
          <div v-if="discussData.comments.length === 0" class="no-comments">
            <span class="no-comments-text">暂无评论</span>
          </div>
          <div v-else v-for="comment in discussData.comments" :key="comment.id" class="comment-item">
            <CommentItem 
              :comment="comment"
              :user-info="userInfo"
              @reply="handleReply"
              @click-user="(userId) => $router.push(`/userProfile/${userId}`)"
            />
          </div>
        </div>

        <!-- 加载更多 -->
        <div v-if="discussData.comments.length < totalComments" class="load-more">
          <el-button type="text" @click="loadMoreComments">
            加载更多评论
          </el-button>
        </div>

        <!-- 发表评论 -->
        <div class="comment-form">
          <div v-if="replyTo" class="reply-info">
            正在回复 @{{ replyTo.username }}
            <el-button type="text" @click="replyTo = null">取消回复</el-button>
          </div>
          <el-input
            v-model="newComment"
            type="textarea"
            :rows="3"
            :placeholder="replyTo ? '写下你的回复...' : '写下你的评论...'"
            @keydown="handleKeyDown"
          />
          <el-button type="primary" @click="submitComment" :disabled="!newComment.trim()">
            {{ replyTo ? '发表回复' : '发表评论' }}
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.discuss-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.table-card {
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

.detail-table {
  margin-bottom: 20px;
}

:deep(.el-descriptions__label) {
  font-weight: bold;
  width: 120px;
}

:deep(.el-descriptions__content) {
  color: #303133;
}

.problem-link {
  color: #1890ff;
  cursor: pointer;
  text-decoration: underline;
}

.problem-link:hover {
  color: #40a9ff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.user-name {
  font-weight: bold;
}

.content-section {
  margin: 20px 0;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.discuss-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 16px;
}

.discuss-content {
  font-size: 16px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.comments-section {
  margin-top: 20px;
  margin-bottom: 100px; /* 为固定定位的评论框留出空间 */
}

.section-title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 16px;
}

.comments-list {
  margin-bottom: 20px;
}

.comment-item {
  margin-bottom: 16px;
}

.comment-form {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  margin: 0;
  padding: 20px;
  background: #fff;
  box-shadow: 0 -2px 12px 0 rgba(0,0,0,0.1);
  z-index: 1000;
}

.comment-form .el-button {
  margin-top: 12px;
  float: right;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.load-more {
  text-align: center;
  margin: 20px 0;
}

.reply-info {
  margin-bottom: 10px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.no-comments {
  text-align: center;
  padding: 20px 0;
  color: #909399;
  font-size: 14px;
}

.no-comments-text {
  display: inline-block;
  padding: 8px 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>