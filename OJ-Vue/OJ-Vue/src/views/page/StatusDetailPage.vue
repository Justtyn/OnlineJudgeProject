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
          :column="2"
          class="detail-table"
        >
          <el-descriptions-item label="状态ID">
            {{ statusData.id }}
          </el-descriptions-item>
          <el-descriptions-item label="题目ID">
            <a class="problem-link" @click="$router.push(`/problem/${statusData.problemId}`)">
              {{ statusData.problemId }}
            </a>
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ statusData.username }}
          </el-descriptions-item>
          <el-descriptions-item label="耗时">
            {{ statusData.time }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <span :class="getStatusClass(statusData.status)">
              {{ statusData.status }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="语言">
            {{ statusData.language }}
          </el-descriptions-item>
          <el-descriptions-item label="内存">
            {{ statusData.memory }} KB
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatDateTime(statusData.creatTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="输出信息">
            <span v-if="output" :class="getStatusClass('Wrong Answer')">
              {{ output }}
            </span>
            <span v-else class="status-pending">暂无信息</span>
          </el-descriptions-item>
        </el-descriptions>
  
        <!-- Run Code 工具栏 -->
        <div class="code-toolbar">
          <span class="code-title">Run Code</span>
          <el-button type="success" size="mini" @click="runCode">
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
                重新提问
              </el-button>
            </el-form-item>
          </el-form>
  
          <!-- 思考提示 -->
          <div v-if="isThinking" class="thinking">
            <el-icon class="spin"><Loading /></el-icon>
            AI 正在努力思考<em class="dots"><span>.</span><span>.</span><span>.</span></em>
          </div>
  
          <!-- 聊天窗口 -->
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
  import { Refresh, Loading } from '@element-plus/icons-vue'
  import request from '@/utils/request.js'
  
  // 路由
  const route = useRoute()
  const router = useRouter()
  
  // 状态详情
  const statusData = ref({ code: '' })
  const output = ref('')
  
  // 行列表
  const linesList = computed(() => statusData.value.code.split('\n'))
  
  // AI 模型选项
  const models = ['llama3.2:latest', 'gemma3:4b']
  const aiModel = ref(models[0])
  
  // AI 对话状态
  const isThinking = ref(false)
  const fullResponse = ref('')
  const typewriterContent = ref('')
  const chatWindowRef = ref(null)
  
  // 获取状态详情
  const fetchStatusDetail = async (id) => {
    try {
      const res = await request.get(`/status/${id}`)
      if (res.data.code === '200') {
        statusData.value = res.data.data
        output.value = res.data.data.output || ''
        // 首次加载自动提问
        askAI()
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
  
  // AI 提问逻辑，超时120秒，并自动滚动
  const askAI = async () => {
    isThinking.value = true
    fullResponse.value = ''
    typewriterContent.value = ''
    const prompt = output.value
      ? `我的代码出现以下错误，请帮我分析原因并提出修改建议：\n${output.value} \n代码如下：\n${statusData.value.code}`
      : `请帮我优化或改进以下代码：\n${statusData.value.code}`
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
  
  onMounted(() => fetchStatusDetail(route.params.id))
  </script>
  
  <style scoped>
  .status-container {
    padding: 20px;
    background-color: #f5f7fa;
    height: 100%;
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
    background-color: #f0f2f5;
    color: #909399;
    border: 1px solid #d3d4d6;
    font-weight: bold;
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
    max-height: 200px;
    overflow-y: auto;
    scroll-behavior: smooth;
  }
  .chat-window pre {
    margin: 0;
    white-space: pre-wrap;
    word-wrap: break-word;
  }
  </style>
  