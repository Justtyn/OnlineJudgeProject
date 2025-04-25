<template>
  <div class="solution-container">
    <el-card class="table-card">
      <!-- 卡片头部 -->
      <template #header>
        <div class="card-header">
          <span class="title">题解详情</span>
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
        <el-descriptions-item label="题解ID">
          {{ solutionData.id }}
        </el-descriptions-item>
        <el-descriptions-item label="题目ID">
          <a class="problem-link" @click="$router.push(`/problem/${solutionData.problemId}`)">
            {{ solutionData.problemId }}
          </a>
        </el-descriptions-item>
        <el-descriptions-item label="发布者">
          <div class="user-info" v-if="userInfo">
            <el-avatar 
              :src="userInfo.avatar" 
              class="user-avatar"
              :size="32"
            />
            <span class="user-name">{{ userInfo.username }}</span>
          </div>
          <span v-else>加载中...</span>
        </el-descriptions-item>
        <el-descriptions-item label="点赞数">
          <div class="like-container">
            <el-button 
              type="primary" 
              link 
              @click="handleLike"
              class="like-button"
            >
              <el-icon><Star /></el-icon>
            </el-button>
            <span>{{ solutionData.likeNum || 0 }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="发布时间" :span="2">
          {{ formatDateTime(solutionData.createTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 代码展示区域 -->
      <div class="code-toolbar">
        <span class="code-title">题解代码</span>
        <el-button type="primary" size="small" @click="copyCode">
          <el-icon><Document /></el-icon>
          复制代码
        </el-button>
      </div>

      <!-- 代码框：行号 + 自动撑高 -->
      <div class="code-wrapper">
        <pre class="code-block"><code>
          <span v-for="(line, idx) in linesList" :key="idx">{{ line || ' ' }}</span>
        </code></pre>
      </div>

      <!-- AI 代码优化建议 -->
      <div class="ai-chat">
        <!-- 模型选择及重新提问按钮 -->
        <el-form inline class="model-select">
          <el-form-item label="选择模型：">
            <el-select
              v-model="aiModel"
              placeholder="模型"
              size="small"
              style="width: 180px;"
            >
              <el-option
                v-for="m in models"
                :key="m"
                :label="m"
                :value="m"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="small" @click="askAI">
              获取优化建议
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 思考提示 -->
        <div v-if="isThinking" class="thinking">
          <el-icon class="spin"><Loading /></el-icon>
          AI 正在分析代码<em class="dots"><span>.</span><span>.</span><span>.</span></em>
        </div>

        <!-- AI 回复窗口 -->
        <div ref="chatWindowRef" class="chat-window">
          <pre>{{ typewriterContent }}</pre>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, Star, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

// 路由
const route = useRoute()
const router = useRouter()

// 题解详情
const solutionData = ref({ content: '' })
const userInfo = ref(null)

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

// 行列表
const linesList = computed(() => solutionData.value.content.split('\n'))

// AI 模型选项
const models = ['llama3.2:latest', 'gemma3:4b']
const aiModel = ref(models[0])

// AI 对话状态
const isThinking = ref(false)
const fullResponse = ref('')
const typewriterContent = ref('')
const chatWindowRef = ref(null)

// 获取题解详情
const fetchSolutionDetail = async (id) => {
  try {
    const res = await request.get(`/solution/${id}`)
    if (res.data.code === 200) {
      solutionData.value = res.data.data
      // 获取发布者信息
      await fetchUserInfo(res.data.data.userId)
    }
  } catch (e) {
    console.error('获取题解详情失败：', e)
    ElMessage.error('获取题解详情失败')
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
        username: userData.username || userData.name || '未知用户',
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

// 复制代码
const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(solutionData.value.content)
    ElMessage.success('代码已复制到剪贴板')
  } catch (err) {
    console.error('复制失败:', err)
    ElMessage.error('复制失败')
  }
}

// 点赞
const handleLike = async () => {
  if (!userId) {
    ElMessage.warning('请先登录后才能点赞')
    return
  }
  
  try {
    const response = await request.put(`/solution/like/${solutionData.value.id}`)
    
    if (response.data.code === 200) {
      solutionData.value.likeNum = response.data.data.likeNum
      ElMessage.success(response.data.message || '点赞成功')
    } else {
      ElMessage.error(response.data.message || '点赞失败')
    }
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('点赞失败：' + (error.response?.data?.message || error.message || '未知错误'))
  }
}

// AI 提问逻辑
const askAI = async () => {
  isThinking.value = true
  fullResponse.value = ''
  typewriterContent.value = ''
  const prompt = `请帮我分析并优化以下代码，指出可能的改进点和优化建议：\n${solutionData.value.content}`
  
  try {
    const res = await request.post(
      'http://localhost:11434/api/generate',
      { model: aiModel.value, prompt, stream: false },
      { timeout: 120000 }
    )
    fullResponse.value = res.data.response || ''
    for (let i = 0; i < fullResponse.value.length; i++) {
      typewriterContent.value += fullResponse.value[i]
      await new Promise(r => setTimeout(r, 30))
      await nextTick()
      if (chatWindowRef.value) {
        chatWindowRef.value.scrollTop = chatWindowRef.value.scrollHeight
      }
    }
  } catch (e) {
    console.error('AI 请求失败：', e)
    typewriterContent.value = 'AI 请求失败，请重试。'
  } finally {
    isThinking.value = false
  }
}

onMounted(() => fetchSolutionDetail(route.params.id))
</script>

<style scoped>
/* 复用 StatusDetailPage 的样式并添加新的样式 */
.solution-container {
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

.like-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.like-button {
  padding: 4px;
  font-size: 20px;
}

.like-button:hover {
  color: #e6a23c;
  transform: scale(1.15);
}

.code-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16px 0 8px;
}

.code-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.code-wrapper {
  background: #2d2d2d;
  border-radius: 4px;
  overflow: auto;
}

.code-block {
  counter-reset: line;
  margin: 0;
  padding: 16px;
  font-family: Consolas, Menlo, Monaco, "Courier New", monospace;
  font-size: 14px;
  line-height: 1.6em;
  color: #f8f8f2;
  background: transparent;
}

.code-block code {
  display: block;
}

.code-block code > span {
  display: block;
  counter-increment: line;
  padding-left: 3em;
  position: relative;
}

.code-block code > span::before {
  content: counter(line);
  position: absolute;
  left: 0;
  width: 2.5em;
  text-align: right;
  color: #7c7c7c;
}

.code-block code > span:nth-child(even) {
  background: rgba(255,255,255,0.02);
}

/* AI 对话样式 */
.ai-chat {
  margin-top: 20px;
}

.model-select {
  margin-bottom: 8px;
}

.thinking {
  display: flex;
  align-items: center;
  color: #909399;
  margin-bottom: 8px;
}

.spin {
  margin-right: 4px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.dots span {
  animation: blink 1.4s infinite both;
}

.dots span:nth-child(1) { animation-delay: 0s; }
.dots span:nth-child(2) { animation-delay: 0.2s; }
.dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes blink {
  0%, 80%, 100% { opacity: 0; }
  40% { opacity: 1; }
}

.chat-window {
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  min-height: 100px;
  max-height: 300px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.chat-window pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
