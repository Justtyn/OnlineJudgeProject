<template>
    <div class="status-container">
      <el-card class="table-card">
        <!-- 卡片头部 -->
        <template #header>
          <div class="card-header">
            <span class="title">状态详情</span>
            <el-button type="primary" size="small" @click="goBack" title="返回列表">
              返回列表
            </el-button>
          </div>
        </template>
  
        <!-- 信息描述表格 -->
        <el-descriptions
          border
          :column="3"
          class="detail-table"
          :label-style="{ width: '120px', fontWeight: 'bold', backgroundColor: '#f5f7fa' }"
          :content-style="{ padding: '12px 20px' }"
        >
          <el-descriptions-item label="状态ID" :span="1">
            <span class="detail-value">{{ statusData.id }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="题目ID" :span="1">
            <a class="problem-link" @click="$router.push(`/problem/${statusData.problemId}`)">
              {{ statusData.problemId }}
            </a>
          </el-descriptions-item>
          <el-descriptions-item label="用户名" :span="1">
            <a class="problem-link" @click="$router.push(`/userProfile/${statusData.userId}`)">
              {{ statusData.username }}
            </a>
          </el-descriptions-item>
          <el-descriptions-item label="耗时" :span="1">
            <span class="detail-value">{{ statusData.time }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态" :span="1">
            <span :class="getStatusClass(statusData.status)">
              {{ statusData.status }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="语言" :span="1">
            <span class="detail-value">{{ statusData.language }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="内存" :span="1">
            <span class="detail-value">{{ statusData.memory }} KB</span>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">
            <span class="detail-value">{{ formatDateTime(statusData.creatTime) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="输出信息" :span="3">
            <div class="output-info">
              <span v-if="output" :class="getStatusClass('Wrong Answer')">
                {{ output }}
              </span>
              <span v-else class="status-pending">暂无信息</span>
            </div>
          </el-descriptions-item>
        </el-descriptions>
  
        <!-- Run Code 工具栏 -->
        <div class="code-toolbar">
          <span class="code-title">Run Code</span>
          <el-button type="success" size="small" @click="runCode" class="refresh-btn">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
  
        <!-- 代码框：行号 + 自动撑高 -->
        <div class="code-wrapper">
          <pre class="code-block"><code>
            <span v-for="(line, idx) in linesList" :key="idx">{{ line || ' ' }}</span>
          </code></pre>
        </div>
  
        <!-- AI 对话功能 -->
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
            <span>点击"分析代码"按钮，AI将会分析你的代码并提供改进建议或解决方案</span>
          </div>
  
          <!-- 聊天窗口 -->
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
import { Refresh, Loading, Lightning, InfoFilled, DocumentCopy } from '@element-plus/icons-vue'
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
  
  // 状态详情
  const statusData = ref({ code: '' })
  const output = ref('')
  
  // 行列表
  const linesList = computed(() => statusData.value.code.split('\n'))
  
// AI 模型选项
const models = ['deepseek-v3']
const aiModel = ref(models[0])

// AI 角色选项
const aiRoles = ref([
  {
    id: 'debugger',
    name: '调试专家',
    icon: 'Tools',
    description: '专门解决代码错误和调试问题',
    systemPrompt: '你是一个专业的代码调试专家，擅长快速定位和解决各种编程错误。请分析用户提供的错误代码，找出问题所在，并提供详细的解决方案和调试建议。'
  },
  {
    id: 'optimizer',
    name: '性能优化师',
    icon: 'MagicStick',
    description: '专注于代码性能优化',
    systemPrompt: '你是一个专业的代码性能优化专家，精通各种编程语言的性能优化技巧。请分析用户代码的性能瓶颈，提供优化建议和最佳实践。'
  },
  {
    id: 'teacher',
    name: '编程导师',
    icon: 'QuestionFilled',
    description: '从教学角度解释代码问题',
    systemPrompt: '你是一个经验丰富的编程导师，擅长用通俗易懂的方式解释复杂的编程概念。请从教学角度分析用户代码，提供清晰的学习指导和知识点解释。'
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
    debugger: '🛠️ 调试专家：我是bug终结者！让我用侦探般的敏锐眼光帮你找出代码中的问题，就像福尔摩斯破案一样精准！',
    optimizer: '⚡ 性能优化师：我是速度的魔法师！让我帮你把代码调教得飞快，就像给火箭装上了超音速引擎！',
    teacher: '👨‍🏫 编程导师：我是你的编程老师！让我用通俗易懂的方式解释复杂概念，就像把高深的武功秘籍翻译成白话文！'
  }
  return tooltips[role.id] || role.description
}
  
  // 获取状态详情
  const fetchStatusDetail = async (id) => {
    try {
      const res = await request.get(`/status/${id}`)
      if (res.data.code === '200') {
        statusData.value = res.data.data
        output.value = res.data.data.output || ''
        // 不再自动请求AI
      }
    } catch (e) {
      console.error('获取状态详情失败：', e)
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
  // Run code（示例）
  const runCode = () => console.log('Run code:', statusData.value.code)
  
  // 获取状态类名
  const statusMap = [
    { description: 'Accepted', type: 'success' },
    { description: 'Wrong Answer', type: 'error' },
    { description: 'Time Limit Exceeded', type: 'warning' },
    { description: 'Compilation Error', type: 'error' },
    { description: 'Runtime Error (SIGSEGV)', type: 'error' },
    { description: 'Runtime Error (SIGXFSZ)', type: 'error' },
    { description: 'Runtime Error (SIGFPE)', type: 'error' },
    { description: 'Runtime Error (SIGABRT)', type: 'error' },
    { description: 'Runtime Error (NZEC)', type: 'error' },
    { description: 'Runtime Error (Other)', type: 'error' },
    { description: 'Internal Error', type: 'error' },
    { description: 'Exec Format Error', type: 'error' },
    { description: 'In Queue', type: 'pending' },
    { description: 'Processing', type: 'pending' }
  ]
  const getStatusClass = (status) => {
    const found = statusMap.find(s => s.description === status)
    const type = found ? found.type : 'pending'
    return {
      success: 'status-success',
      error: 'status-error',
      warning: 'status-warning',
      pending: 'status-pending'
    }[type]
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
      const problemRes = await request.get(`/problem/${statusData.value.problemId}`)
      if (problemRes.data.code !== '200') {
        throw new Error('获取题目信息失败')
      }
      const problemData = problemRes.data.data

      // 构建请求内容
      const messages = []
      
      if (statusData.value.status === 'Accepted') {
        // 代码优化模式
        messages.push({
          role: 'system',
          content: selectedRole.value.systemPrompt
        })
        messages.push({
          role: 'user',
          content: `题目：${problemData.name}\n题目描述：${problemData.desc}\n输入格式：${problemData.descInput}\n输出格式：${problemData.descOutput}\n示例输入：\n${problemData.sampleInput}\n示例输出：\n${problemData.sampleOutput}\n\n这是我目前的代码：\n\`\`\`${statusData.value.language.toLowerCase()}\n${statusData.value.code}\n\`\`\`\n请以${selectedRole.value.name}的身份帮我分析并改进这段代码。`
        })
      } else {
        // 解题模式
        messages.push({
          role: 'system',
          content: selectedRole.value.systemPrompt
        })
        messages.push({
          role: 'user',
          content: `题目：${problemData.name}\n题目描述：${problemData.desc}\n输入格式：${problemData.descInput}\n输出格式：${problemData.descOutput}\n示例输入：\n${problemData.sampleInput}\n示例输出：\n${problemData.sampleOutput}\n\n用户代码：\n\`\`\`${statusData.value.language.toLowerCase()}\n${statusData.value.code}\n\`\`\`\n\n错误信息：${output.value}\n请以${selectedRole.value.name}的身份帮我分析问题并提供解决方案。`
        })
      }

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
  
  onMounted(() => fetchStatusDetail(route.params.id))
  </script>
  
  <style scoped>
  .status-container {
    padding: 20px;
    background-color: #f5f7fa;
    height: 100%;
    width: 100%;
    box-sizing: border-box;
    animation: fadeIn 0.5s ease-in-out;
  }
  .table-card {
    margin-bottom: 20px;
    width: 100%;
    animation: slideIn 0.6s ease-out;
  }
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
    animation: slideDown 0.5s ease-out;
  }
  .title {
    font-size: 18px;
    font-weight: bold;
  }
  .detail-table {
    margin-bottom: 20px;
    width: 100%;
    overflow-x: auto;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    animation: fadeIn 0.7s ease-out;
  }
  .detail-value {
    color: #606266;
    font-size: 14px;
  }
  .output-info {
    padding: 8px;
    background-color: var(--bg-color-soft);
    border-radius: 4px;
    min-height: 40px;
    display: flex;
    align-items: center;
  }
  :deep(.el-descriptions__cell) {
    padding: 12px 20px;
  }
  :deep(.el-descriptions__label) {
    color: #606266;
    font-weight: 600;
  }
  :deep(.el-descriptions__content) {
    color: #303133;
    word-break: break-word;
  }
  :deep(.el-descriptions__body) {
    background-color: var(--color-background);
  }
  :deep(.el-descriptions__table) {
    border-collapse: separate;
    border-spacing: 0;
  }
  :deep(.el-descriptions__cell.is-bordered) {
    border: 1px solid var(--border-color);
  }
  :deep(.el-descriptions__cell.is-bordered:first-child) {
    border-left: 1px solid var(--border-color);
  }
  :deep(.el-descriptions__cell.is-bordered:last-child) {
    border-right: 1px solid var(--border-color);
  }
  .problem-link {
    color: #1890ff;
    cursor: pointer;
    text-decoration: underline;
  }
  .problem-link:hover {
    color: #40a9ff;
  }
  .code-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 16px 0 8px;
    flex-wrap: wrap;
    gap: 10px;
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
    width: 100%;
    animation: slideUp 0.6s ease-out;
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
    white-space: pre-wrap;
    word-wrap: break-word;
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
  .status-success {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 4px;
    background-color: #f0f9eb;
    color: #67c23a;
    border: 1px solid #c2e7b0;
    font-weight: bold;
  }
  .status-error {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 4px;
    background-color: #fef0f0;
    color: #f56c6c;
    border: 1px solid #fbc4c4;
    font-weight: bold;
  }
  .status-warning {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 4px;
    background-color: #fdf6ec;
    color: #e6a23c;
    border: 1px solid #f5dab1;
    font-weight: bold;
  }
  .status-pending {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 4px;
    background-color: var(--bg-color-mute);
    color: #909399;
    border: 1px solid #d3d4d6;
    font-weight: bold;
  }
  
  /* 更新的 AI 对话样式 */
  .ai-chat {
    margin-top: 30px;
    width: 100%;
    animation: fadeIn 0.8s ease-out;
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
  
  @keyframes blink {
    0%, 80%, 100% { opacity: 0; }
    40% { opacity: 1; }
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
  
  /* 移动端适配 */
  @media screen and (max-width: 768px) {
    .status-container {
      padding: 10px;
    }
    .card-header {
      flex-direction: column;
      align-items: flex-start;
    }
    .title {
      font-size: 16px;
    }
    :deep(.el-descriptions__label) {
      width: 80px !important;
      font-size: 13px;
    }
    :deep(.el-descriptions__content) {
      font-size: 13px;
    }
    :deep(.el-descriptions__cell) {
      padding: 8px 12px !important;
    }
    .code-toolbar {
      flex-direction: row;
      align-items: center;
      margin: 12px 0 6px;
    }
    .refresh-btn {
      padding: 6px !important;
      min-height: 28px !important;
    }
    .refresh-btn :deep(.el-icon) {
      font-size: 14px;
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
  
  /* 添加动画关键帧 */
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
      transform: translateX(-20px);
      opacity: 0;
    }
    to {
      transform: translateX(0);
      opacity: 1;
    }
  }
  
  @keyframes slideDown {
    from {
      transform: translateY(-20px);
      opacity: 0;
    }
    to {
      transform: translateY(0);
      opacity: 1;
    }
  }
  
  @keyframes slideUp {
    from {
      transform: translateY(20px);
      opacity: 0;
    }
    to {
      transform: translateY(0);
      opacity: 1;
    }
  }
  
  /* 代码高亮效果 */
  .code-block code > span {
    transition: background-color 0.3s ease;
  }
  
  .code-block code > span:hover {
    background-color: rgba(255, 255, 255, 0.05);
  }
  </style>
  