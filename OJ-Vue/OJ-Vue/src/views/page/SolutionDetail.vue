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
            <span>AI 代码助手</span>
            <div class="ai-badge">Beta</div>
          </div>
          
          <div class="ai-controls">
            <!-- AI角色选择 -->
            <el-tooltip
              v-for="role in aiRoles"
              :key="role.id"
              :content="getRoleTooltip(role)"
              placement="top"
              effect="dark"
            >
              <el-button
                :type="selectedRole.id === role.id ? 'primary' : ''"
                size="small"
                class="role-btn"
                @click="selectedRole = role"
              >
                <el-icon><component :is="role.icon" /></el-icon>
                {{ role.name }}
              </el-button>
            </el-tooltip>
            
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
              {{ hasResponse ? '重新分析' : '开始分析' }}
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
          <div class="ai-response-content">
            <div 
              v-html="formatMessage(typewriterContent)" 
              class="markdown-content"
            ></div>
            <span v-if="isStreaming" class="streaming-cursor">|</span>
          </div>
          
          <!-- 操作按钮 -->
          <div v-if="hasResponse && !isThinking" class="ai-response-actions">
            <div class="action-buttons">
              <el-button size="small" @click="copyAIResponse" class="copy-btn">
                <el-icon><DocumentCopy /></el-icon>
                复制全文
              </el-button>
              <el-button size="small" @click="regenerateAIResponse" class="regenerate-btn">
                <el-icon><Refresh /></el-icon>
                重新生成
              </el-button>
            </div>
            <div v-if="responseDuration > 0" class="response-duration">
              用时: {{ formatDuration(responseDuration) }}
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Document, Star, Loading, Lightning, InfoFilled, DocumentCopy, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

// 配置Markdown解析器
marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value
      } catch (err) {
        console.error('代码高亮错误:', err)
      }
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true,
  gfm: true
})

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

// AI 角色选项
const aiRoles = ref([
  {
    id: 'optimizer',
    name: '代码优化师',
    icon: 'MagicStick',
    description: '专注于代码性能优化和最佳实践',
    systemPrompt: '你是一个专业的代码优化专家，精通各种编程语言的性能优化技巧。请分析用户提供的代码，从时间复杂度、空间复杂度、代码可读性、最佳实践等角度提供详细的优化建议，并给出优化后的代码实现。'
  },
  {
    id: 'reviewer',
    name: '代码审查员',
    icon: 'Document',
    description: '从代码质量和规范角度进行审查',
    systemPrompt: '你是一个资深的代码审查专家，擅长发现代码中的潜在问题、安全漏洞、性能瓶颈和规范性问题。请对用户代码进行全面的审查，提供详细的改进建议和最佳实践指导。'
  },
  {
    id: 'architect',
    name: '架构师',
    icon: 'Setting',
    description: '从系统架构和设计模式角度分析',
    systemPrompt: '你是一个经验丰富的软件架构师，擅长从系统设计、架构模式、可扩展性等角度分析代码。请评估代码的架构合理性，提供重构建议和设计模式应用指导。'
  }
])

const selectedRole = ref(aiRoles.value[0])

// AI 对话状态
const isThinking = ref(false)
const isStreaming = ref(false)
const fullResponse = ref('')
const typewriterContent = ref('')
const chatWindowRef = ref(null)
const hasResponse = computed(() => typewriterContent.value.length > 0)
const streamingMessageIndex = ref(-1)
const currentStreamingMessage = ref('')

// 计时相关状态
const responseStartTime = ref(null)
const responseEndTime = ref(null)
const responseDuration = ref(0)

const isLiked = ref(false)

// 格式化消息内容 - 支持Markdown解析
const formatMessage = (content) => {
  if (!content) return ''
  
  try {
    return marked(content)
  } catch (error) {
    console.error('Markdown解析错误:', error)
    return content.replace(/\n/g, '<br>')
  }
}

// 格式化持续时间
const formatDuration = (duration) => {
  if (duration < 1000) {
    return `${duration}ms`
  } else if (duration < 60000) {
    return `${(duration / 1000).toFixed(1)}s`
  } else {
    const minutes = Math.floor(duration / 60000)
    const seconds = Math.floor((duration % 60000) / 1000)
    return `${minutes}m ${seconds}s`
  }
}

// 复制AI回复内容
const copyAIResponse = async () => {
  try {
    const tempDiv = document.createElement('div')
    tempDiv.innerHTML = typewriterContent.value
    const textContent = tempDiv.textContent || tempDiv.innerText || ''
    
    await navigator.clipboard.writeText(textContent)
    ElMessage.success('AI回复已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  }
}

// 重新生成AI回复
const regenerateAIResponse = async () => {
  typewriterContent.value = ''
  fullResponse.value = ''
  await askAI()
}

// 获取角色提示信息
const getRoleTooltip = (role) => {
  const tooltips = {
    optimizer: '🔧 代码优化师：我是性能优化的专家！让我来帮你把代码调教得又快又优雅，就像给跑车换了个涡轮增压器一样！',
    reviewer: '📋 代码审查员：我是代码质量的守护者！让我用火眼金睛帮你找出那些隐藏的bug和坏习惯，保证你的代码干净整洁！',
    architect: '🏗️ 架构师：我是系统设计的魔法师！让我从全局角度帮你重构代码，就像给房子重新设计蓝图一样，让结构更合理！'
  }
  return tooltips[role.id] || role.description
}

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

// AI 提问逻辑 - 流式传输版本
const askAI = async () => {
  isThinking.value = true
  isStreaming.value = true
  fullResponse.value = ''
  typewriterContent.value = ''
  responseStartTime.value = Date.now()
  
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
        content: selectedRole.value.systemPrompt
      },
      {
        role: 'user',
        content: `题目：${problemData.name}\n题目描述：${problemData.desc}\n输入格式：${problemData.descInput}\n输出格式：${problemData.descOutput}\n示例输入：\n${problemData.sampleInput}\n示例输出：\n${problemData.sampleOutput}\n\n这是我目前的代码：\n\`\`\`${solutionData.value.language?.toLowerCase() || 'cpp'}\n${solutionData.value.content}\n\`\`\`\n请以${selectedRole.value.name}的身份帮我分析并改进这段代码。`
      }
    ]
    
    // 使用fetch直接请求，避免被request拦截器处理
    const response = await fetch('https://api.deepseek.com/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer sk-ff342bebb7114fbbbf402971065c977e'
      },
      body: JSON.stringify({
        model: 'deepseek-chat',
        messages: messages,
        stream: true,
        temperature: 0.7,
        max_tokens: 2000
      })
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      
      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6)
          if (data === '[DONE]') {
            // 流式传输完成
            responseEndTime.value = Date.now()
            responseDuration.value = responseEndTime.value - responseStartTime.value
            isStreaming.value = false
            isThinking.value = false
            await nextTick()
            if (chatWindowRef.value) {
              chatWindowRef.value.scrollTop = chatWindowRef.value.scrollHeight
            }
            return
          }
          
          try {
            const parsed = JSON.parse(data)
            if (parsed.choices && parsed.choices[0] && parsed.choices[0].delta && parsed.choices[0].delta.content) {
              const content = parsed.choices[0].delta.content
              typewriterContent.value += content
              
              // 平滑滚动到底部
              await nextTick()
              if (chatWindowRef.value) {
                chatWindowRef.value.scrollTop = chatWindowRef.value.scrollHeight
              }
            }
          } catch (e) {
            console.warn('解析流式数据失败:', e)
          }
        }
      }
    }
  } catch (e) {
    console.error('AI 请求失败：', e)
    typewriterContent.value = 'AI 请求失败，请重试。'
  } finally {
    isThinking.value = false
    isStreaming.value = false
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
  border: 2px solid var(--color-background);
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
  color: var(--color-background);
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

.role-btn {
  margin-right: 8px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.role-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.model-select {
  width: 160px;
  border-radius: 4px;
}

/* AI回复内容样式 */
.ai-response-content {
  position: relative;
}

.markdown-content {
  line-height: 1.6;
  word-wrap: break-word;
}

.markdown-content :deep(pre) {
  background: #1e293b;
  border-radius: 8px;
  padding: 16px;
  margin: 12px 0;
  font-family: 'JetBrains Mono', 'Courier New', monospace;
  font-size: 14px;
  overflow-x: auto;
  color: var(--border-color);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.markdown-content :deep(code) {
  background: rgba(99, 102, 241, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'JetBrains Mono', 'Courier New', monospace;
  font-size: 13px;
  color: #6366f1;
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.markdown-content :deep(pre code) {
  background: transparent;
  padding: 0;
  border: none;
  color: inherit;
}

.markdown-content :deep(h1), 
.markdown-content :deep(h2), 
.markdown-content :deep(h3), 
.markdown-content :deep(h4), 
.markdown-content :deep(h5), 
.markdown-content :deep(h6) {
  color: #6366f1;
  margin: 16px 0 8px 0;
  font-weight: 600;
}

.markdown-content :deep(h1) { font-size: 1.5em; }
.markdown-content :deep(h2) { font-size: 1.3em; }
.markdown-content :deep(h3) { font-size: 1.2em; }

.markdown-content :deep(ul), 
.markdown-content :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.markdown-content :deep(li) {
  margin: 4px 0;
  line-height: 1.6;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #6366f1;
  padding-left: 16px;
  margin: 12px 0;
  color: #64748b;
  font-style: italic;
}

/* AI回复操作按钮 */
.ai-response-actions {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.action-buttons {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.copy-btn {
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid rgba(34, 197, 94, 0.2);
  color: #22c55e;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.copy-btn:hover {
  background: #22c55e;
  color: white;
  transform: translateY(-1px);
}

.regenerate-btn {
  background: rgba(99, 102, 241, 0.1);
  border: 1px solid rgba(99, 102, 241, 0.2);
  color: #6366f1;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.regenerate-btn:hover {
  background: #6366f1;
  color: white;
  transform: translateY(-1px);
}

.response-duration {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  font-style: italic;
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

.streaming-cursor {
  animation: blink 1s infinite;
  color: #4361ee;
  font-weight: bold;
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
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .role-btn {
    flex: 1;
    min-width: 80px;
    margin-bottom: 8px;
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
</style>
