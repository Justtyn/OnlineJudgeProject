<template>
  <div class="solution-container fade-in">
    <el-card class="table-card slide-in">
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
          <div class="user-info" v-if="userInfo" @click="$router.push(`/userProfile/${solutionData.userId}`)">
            <el-avatar 
              :src="userInfo.avatar" 
              class="user-avatar hover-effect"
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
              :class="{ 'liked': isLiked }"
            >
              <el-icon><Star /></el-icon>
            </el-button>
            <span class="like-count">{{ solutionData.likeNum || 0 }}</span>
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
          <span v-for="(line, idx) in linesList" :key="idx" class="code-line">{{ line || ' ' }}</span>
        </code></pre>
      </div>

      <!-- AI 代码优化建议 -->
      <div class="ai-chat">
        <!-- AI工具栏 -->
        <div class="ai-toolbar">
          <div class="ai-title">
            <span>AI 代码优化</span>
            <div class="ai-badge">Beta</div>
          </div>
          
          <div class="ai-controls">
            <el-select
              v-model="aiModel"
              placeholder="选择模型"
              size="small"
              class="model-select"
            >
              <el-option
                v-for="m in models"
                :key="m"
                :label="m"
                :value="m"
              />
            </el-select>
            
            <el-button 
              type="primary" 
              size="small" 
              @click="askAI"
              :disabled="isThinking"
              :loading="isThinking"
              class="ai-button"
            >
              <el-icon v-if="!isThinking"><Lightning /></el-icon>
              {{ hasResponse ? '重新优化' : '获取优化建议' }}
            </el-button>
          </div>
        </div>

        <!-- 思考提示 -->
        <div v-if="isThinking" class="thinking">
          <el-icon class="spin"><Loading /></el-icon>
          AI 正在分析代码<em class="dots"><span>.</span><span>.</span><span>.</span></em>
        </div>
        
        <!-- 提示语 -->
        <div v-if="!hasResponse && !isThinking" class="ai-prompt">
          <el-icon><InfoFilled /></el-icon>
          <span>点击"获取优化建议"按钮，AI将分析你的代码并提供性能优化和代码改进建议</span>
        </div>

        <!-- AI 回复窗口 -->
        <div v-if="hasResponse || isThinking" ref="chatWindowRef" class="chat-window">
          <pre :class="{ 'blur-text': isThinking }">{{ typewriterContent }}</pre>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, Star, Loading, Lightning, InfoFilled } from '@element-plus/icons-vue'
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
const models = ['deepseek-v3']
const aiModel = ref(models[0])

// AI 对话状态
const isThinking = ref(false)
const fullResponse = ref('')
const typewriterContent = ref('')
const chatWindowRef = ref(null)
const hasResponse = computed(() => typewriterContent.value.length > 0)

const isLiked = ref(false)

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
    ElMessage.success({
      message: '代码已复制到剪贴板',
      duration: 2000,
      customClass: 'copy-success-message'
    })
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
      isLiked.value = true
      setTimeout(() => {
        isLiked.value = false
      }, 1000)
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
  
  try {
    // 获取题目信息
    const problemRes = await request.get(`/problem/${solutionData.value.problemId}`)
    if (problemRes.data.code !== '200') {
      throw new Error('获取题目信息失败')
    }
    const problemData = problemRes.data.data

    // 构建请求内容
    const messages = [
      {
        role: 'system',
        content: '你是一个专业的代码改进助手，精通 C++、Python、Java 和 C 语言。无论用户提交何种已有代码，你都要：\n1. 首先简要分析现有代码的关键问题与改进方向（如性能优化、可读性提升、内存管理、错误处理、安全性等）\n2. 针对每个改进点给出清晰说明，并指出改动前后的差异\n3. 输出重构后的完整、可运行代码，保持原有输入输出规范和功能不变\n4. 在代码前用注释或简短文字说明主要改动与优化思路\n5. 如果用户没有指定语言，默认帮其用原语言改进；如指定其它语言，可提供等效实现。'
      },
      {
        role: 'user',
        content: `题目：${problemData.name}\n题目描述：${problemData.desc}\n输入格式：${problemData.descInput}\n输出格式：${problemData.descOutput}\n示例输入：\n${problemData.sampleInput}\n示例输出：\n${problemData.sampleOutput}\n\n这是我目前的代码：\n\`\`\`${solutionData.value.language?.toLowerCase() || 'cpp'}\n${solutionData.value.content}\n\`\`\`\n请帮我改进并优化性能。`
      }
    ]
    
    const res = await request.post(
      'https://api.deepseek.com/chat/completions',
      {
        model: 'deepseek-chat',
        messages: messages,
        stream: false
      },
      {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer sk-663f54627f1a4c539b9fc02a5fa1f2eb'
        },
        timeout: 120000
      }
    )
    
    // 更新响应处理逻辑 - 加快打字机效果
    if (res.data && res.data.choices && res.data.choices[0] && res.data.choices[0].message) {
      fullResponse.value = res.data.choices[0].message.content || ''
      
      // 更快的打字机效果，每批处理多个字符
      const BATCH_SIZE = 10; // 每次添加10个字符
      const DELAY = 5; // 每批延迟5ms
      
      for (let i = 0; i < fullResponse.value.length; i += BATCH_SIZE) {
        const end = Math.min(i + BATCH_SIZE, fullResponse.value.length);
        typewriterContent.value += fullResponse.value.substring(i, end);
        await new Promise(r => setTimeout(r, DELAY));
        
        // 每3批更新一次滚动位置，减少重绘
        if (i % (BATCH_SIZE * 3) === 0) {
          await nextTick();
          if (chatWindowRef.value) {
            chatWindowRef.value.scrollTop = chatWindowRef.value.scrollHeight;
          }
        }
      }
      
      // 最后确保滚动到底部
      await nextTick();
      if (chatWindowRef.value) {
        chatWindowRef.value.scrollTop = chatWindowRef.value.scrollHeight;
      }
    } else {
      throw new Error('Invalid API response format')
    }
  } catch (e) {
    console.error('AI 请求失败：', e)
    typewriterContent.value = 'AI 请求失败，请重试。'
  } finally {
    isThinking.value = false
  }
}

// 监听聊天窗口引用，确保可以正确滚动
watch(chatWindowRef, (newVal) => {
  if (newVal && hasResponse.value) {
    newVal.scrollTop = newVal.scrollHeight;
  }
});

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
  margin-top: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  overflow: hidden;
}

.ai-toolbar {
  padding: 12px 18px;
  background: linear-gradient(90deg, #0f3460 0%, #1a1a2e 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #fff;
  font-weight: bold;
  font-size: 16px;
}

.ai-badge {
  background: rgba(113, 88, 226, 0.7);
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.ai-controls {
  display: flex;
  gap: 10px;
}

.model-select {
  width: 160px;
  border-radius: 4px;
}

.ai-button {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #4361ee;
  border-color: #4361ee;
  transition: all 0.3s ease;
}

.ai-button:hover {
  background: #3a56d4;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(67, 97, 238, 0.3);
}

.ai-prompt {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px;
  color: rgba(255, 255, 255, 0.7);
  background-color: rgba(255, 255, 255, 0.05);
  font-size: 14px;
  border-radius: 4px;
  margin: 20px;
}

.thinking {
  display: flex;
  align-items: center;
  color: rgba(255, 255, 255, 0.7);
  margin: 20px;
  font-size: 14px;
}

.spin {
  margin-right: 10px;
  animation: spin 1s linear infinite;
  color: #4361ee;
}

.dots span {
  animation: blink 1.4s infinite both;
  color: #4361ee;
}

.dots span:nth-child(1) { animation-delay: 0s; }
.dots span:nth-child(2) { animation-delay: 0.2s; }
.dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes blink {
  0%, 80%, 100% { opacity: 0; }
  40% { opacity: 1; }
}

.chat-window {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  padding: 20px;
  margin: 0 20px 20px 20px;
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
  scroll-behavior: smooth;
  width: calc(100% - 40px);
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  line-height: 1.6;
  backdrop-filter: blur(10px);
}

.chat-window pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'JetBrains Mono', Consolas, monospace;
}

.blur-text {
  filter: blur(3px);
  opacity: 0.5;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 新增霓虹灯脉冲效果 */
@keyframes pulse {
  0% {
    box-shadow: 0 0 5px #4361ee, 0 0 10px #4361ee;
  }
  50% {
    box-shadow: 0 0 10px #4361ee, 0 0 20px #4361ee;
  }
  100% {
    box-shadow: 0 0 5px #4361ee, 0 0 10px #4361ee;
  }
}

/* 添加移动端适配样式 */
@media screen and (max-width: 768px) {
  .solution-container {
    padding: 10px;
  }
  
  .table-card {
    margin-bottom: 15px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .title {
    font-size: 16px;
  }
  
  :deep(.el-descriptions__label) {
    width: 100px;
    font-size: 14px;
  }
  
  :deep(.el-descriptions__content) {
    font-size: 14px;
  }
  
  .code-toolbar {
    flex-direction: column;
    gap: 10px;
  }
  
  .code-title {
    font-size: 16px;
  }
  
  .code-block {
    font-size: 12px;
    padding: 10px;
  }
  
  .ai-toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    padding: 12px;
  }
  
  .ai-controls {
    width: 100%;
    justify-content: space-between;
  }
  
  .model-select {
    width: 120px;
  }
  
  .chat-window {
    min-height: 200px;
    max-height: 300px;
    padding: 15px;
    margin: 0 15px 15px 15px;
    font-size: 13px;
  }
  
  .ai-prompt {
    padding: 15px;
    margin: 15px;
    font-size: 13px;
  }
}

/* 添加新的动画效果 */
.fade-in {
  animation: fadeIn 0.5s ease-in;
}

.slide-in {
  animation: slideIn 0.5s ease-out;
}

.code-line {
  opacity: 0;
  animation: fadeIn 0.3s ease-in forwards;
  animation-delay: calc(var(--line-index) * 0.05s);
}

.like-button {
  transition: all 0.3s ease;
}

.like-button.liked {
  transform: scale(1.2);
  color: #e6a23c;
}

.like-count {
  transition: all 0.3s ease;
}

.hover-effect {
  transition: all 0.3s ease;
}

.hover-effect:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideIn {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 复制成功消息样式 */
:deep(.copy-success-message) {
  background: #67c23a !important;
  color: white !important;
  border-radius: 4px;
  padding: 10px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 代码行动画 */
.code-block code > span {
  --line-index: 0;
  animation: fadeIn 0.3s ease-in forwards;
  animation-delay: calc(var(--line-index) * 0.05s);
}

/* 添加响应式动画调整 */
@media screen and (max-width: 768px) {
  .fade-in {
    animation-duration: 0.3s;
  }
  
  .slide-in {
    animation-duration: 0.3s;
  }
  
  .code-line {
    animation-delay: calc(var(--line-index) * 0.03s);
  }
}

/* 添加移动端适配样式 */
@media screen and (max-width: 768px) {
  .solution-container {
    padding: 10px;
  }
  
  .table-card {
    margin-bottom: 15px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .title {
    font-size: 16px;
  }
  
  :deep(.el-descriptions__label) {
    width: 100px;
    font-size: 14px;
  }
  
  :deep(.el-descriptions__content) {
    font-size: 14px;
  }
  
  .code-toolbar {
    flex-direction: column;
    gap: 10px;
  }
  
  .code-title {
    font-size: 16px;
  }
  
  .code-block {
    font-size: 12px;
    padding: 10px;
  }
  
  .ai-chat {
    margin-top: 15px;
  }
  
  .model-select {
    flex-direction: column;
    gap: 10px;
  }
  
  .chat-window {
    min-height: 150px;
    max-height: 250px;
  }
  
  .chat-window pre {
    font-size: 14px;
  }
  
  :deep(.el-dialog) {
    width: 90% !important;
    margin: 0 auto;
  }
  
  :deep(.el-dialog__body) {
    padding: 15px;
  }
}
</style>
