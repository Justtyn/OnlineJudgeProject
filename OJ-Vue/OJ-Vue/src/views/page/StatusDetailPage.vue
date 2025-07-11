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
                {{ hasResponse ? '重新分析' : '分析代码' }}
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
            <pre :class="{ 'blur-text': isThinking }">{{ typewriterContent }}</pre>
          </div>
        </div>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, computed, nextTick, watch } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { Refresh, Loading, Lightning, InfoFilled } from '@element-plus/icons-vue'
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
  const models = ['deepseek-v3']
  const aiModel = ref(models[0])
  
  // AI 对话状态
  const isThinking = ref(false)
  const fullResponse = ref('')
  const typewriterContent = ref('')
  const chatWindowRef = ref(null)
  const hasResponse = computed(() => typewriterContent.value.length > 0)
  
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
  
  // AI 提问逻辑，超时120秒，并自动滚动，更快的打字机效果
  const askAI = async () => {
    isThinking.value = true
    fullResponse.value = ''
    typewriterContent.value = ''
    
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
          content: '你是一个专业的代码改进助手，精通 C++、Python、Java 和 C 语言。无论用户提交何种已有代码，你都要：\n1. 首先简要分析现有代码的关键问题与改进方向（如性能优化、可读性提升、内存管理、错误处理、安全性等）\n2. 针对每个改进点给出清晰说明，并指出改动前后的差异\n3. 输出重构后的完整、可运行代码，保持原有输入输出规范和功能不变\n4. 在代码前用注释或简短文字说明主要改动与优化思路\n5. 如果用户没有指定语言，默认帮其用原语言改进；如指定其它语言，可提供等效实现。'
        })
        messages.push({
          role: 'user',
          content: `题目：${problemData.name}\n题目描述：${problemData.desc}\n输入格式：${problemData.descInput}\n输出格式：${problemData.descOutput}\n示例输入：\n${problemData.sampleInput}\n示例输出：\n${problemData.sampleOutput}\n\n这是我目前的代码：\n\`\`\`${statusData.value.language.toLowerCase()}\n${statusData.value.code}\n\`\`\`\n请帮我改进并优化性能。`
        })
      } else {
        // 解题模式
        messages.push({
          role: 'system',
          content: '你是一个专业的编程助手，精通 C++、Python、Java、C 语言。无论用户给出何种算法题或编程题描述，你都要：\n1. 根据题目要求，给出可运行的、风格清晰的完整代码\n2. 在代码前简要说明解题思路和算法复杂度\n3. 对输入输出格式严格遵守，不要额外打印多余信息\n4. 如果用户没有指定语言，默认给出 C++ 解法；如指定语言，则按用户要求输出对应语言的代码。'
        })
        messages.push({
          role: 'user',
          content: `题目：${problemData.name}\n题目描述：${problemData.desc}\n输入格式：${problemData.descInput}\n输出格式：${problemData.descOutput}\n示例输入：\n${problemData.sampleInput}\n示例输出：\n${problemData.sampleOutput}\n\n用户代码：\n\`\`\`${statusData.value.language.toLowerCase()}\n${statusData.value.code}\n\`\`\`\n\n错误信息：${output.value}\n请帮我分析问题并提供解决方案。`
        })
      }

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
    background-color: #f8f9fa;
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
    background-color: #ffffff;
  }
  :deep(.el-descriptions__table) {
    border-collapse: separate;
    border-spacing: 0;
  }
  :deep(.el-descriptions__cell.is-bordered) {
    border: 1px solid #ebeef5;
  }
  :deep(.el-descriptions__cell.is-bordered:first-child) {
    border-left: 1px solid #ebeef5;
  }
  :deep(.el-descriptions__cell.is-bordered:last-child) {
    border-right: 1px solid #ebeef5;
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
    background-color: #f0f2f5;
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
  