  <template>
    <div class="ai-assistant-fullscreen">
      <!-- 顶部工具栏 -->
      <div class="ai-toolbar">
        <div class="toolbar-left">
          <div class="ai-avatar">
            <div class="avatar-circle">
              <el-icon size="28" color="#ffffff">
                <MagicStick />
              </el-icon>
            </div>
          </div>
          <div class="ai-info">
            <div class="ai-title-row">
              <h2 class="ai-name">DeepSeek 编程导师</h2>
              <el-tag type="success" size="small" class="status-tag">
                <el-icon><Check /></el-icon>
                在线
              </el-tag>
            </div>
          </div>
        </div>
        
        <div class="toolbar-right">
          <el-input
            v-model="searchQuery"
            placeholder="搜索对话内容..."
            class="search-input"
            size="small"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button @click="exportChat" class="export-btn">
            <el-icon><Download /></el-icon>
            导出对话
          </el-button>
          <el-button @click="clearChat" class="clear-btn">
            <el-icon><Delete /></el-icon>
            清空对话
          </el-button>
        </div>
      </div>

      <!-- 主聊天区域 -->
      <div class="chat-main">
        <!-- 聊天消息区域 -->
        <div class="chat-messages">
          <div class="messages-container" ref="chatMessagesRef">
            <div 
              v-for="(message, index) in filteredMessages" 
              :key="index"
              :class="['message', message.role, { 'streaming': message.isStreaming }]"
            >
              <div class="message-avatar">
                <el-avatar 
                  v-if="message.role === 'user'"
                  :src="userAvatar"
                  size="40"
                  class="user-avatar"
                />
                <div v-else class="ai-avatar-small">
                  <el-icon size="20" color="#ffffff">
                    <MagicStick />
                  </el-icon>
                </div>
              </div>
              <div class="message-content">
                <div class="message-bubble">
                  <div v-if="message.role === 'assistant'" class="message-header">
                    <span class="ai-name-small">DeepSeek 导师</span>
                    <span class="message-time">{{ formatTime(message.timestamp) }}</span>
                  </div>
                  
                  <!-- 思考状态显示在回答框内 -->
                  <div v-if="message.role === 'assistant' && message.isStreaming" class="thinking-indicator-inline">
                    <div class="thinking-dots">
                      <span></span>
                      <span></span>
                      <span></span>
                    </div>
                    <span>AI 正在思考中...</span>
                  </div>
                  
                  <div class="message-text" v-html="formatMessage(message.content)"></div>
                  
                  <!-- 流式传输光标 -->
                  <span v-if="message.isStreaming" class="streaming-cursor">|</span>
                  
                  <!-- 回答完成后的操作按钮 -->
                  <div v-if="message.role === 'assistant' && !message.isStreaming && message.content" class="message-actions">
                    <div class="action-buttons">
                      <el-button size="small" @click="copyMessage(message.content)" class="copy-btn">
                        <el-icon><Document /></el-icon>
                        复制全文
                      </el-button>
                      <el-button size="small" @click="regenerateResponse(message)" class="regenerate-btn">
                        <el-icon><Refresh /></el-icon>
                        重新生成
                      </el-button>
                    </div>
                    <div v-if="message.duration" class="response-duration">
                      用时: {{ formatDuration(message.duration) }}
                    </div>
                  </div>
                  
                  <div v-if="message.role === 'user'" class="message-time user-time">
                    {{ formatTime(message.timestamp) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 底部输入区域 - 嵌入到聊天区域 -->
          <div class="chat-input-area">
            <!-- 快捷选项和模式选择 -->
            <div class="input-toolbar">
              <!-- 快捷问题 -->
              <div class="quick-actions">
                <div
                  v-for="question in quickQuestions" 
                  :key="question.id"
                  class="quick-action-item"
                >
                  <el-button 
                    class="quick-btn"
                    size="small"
                    @click="handleQuickQuestion(question)"
                  >
                    <el-icon><component :is="question.icon" /></el-icon>
                    {{ question.text }}
                  </el-button>
                  <el-button 
                    class="info-btn"
                    size="small"
                    @click="showQuestionDetail(question)"
                  >
                    <el-icon><InfoFilled /></el-icon>
                  </el-button>
                </div>
              </div>
              
              <!-- 对话模式 -->
              <div class="mode-selector">
                <div
                  v-for="mode in chatModes" 
                  :key="mode.id"
                  class="mode-action-item"
                >
                  <el-button 
                    :type="currentMode === mode.id ? 'primary' : ''"
                    class="mode-btn"
                    size="small"
                    @click="switchMode(mode.id)"
                  >
                    <el-icon><component :is="mode.icon" /></el-icon>
                    {{ mode.name }}
                  </el-button>
                  <el-button 
                    class="info-btn"
                    size="small"
                    @click="showModeDetail(mode)"
                  >
                    <el-icon><InfoFilled /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 题目选择器 -->
            <div v-if="showProblemSelector" class="problem-selector">
              <el-select
                v-model="selectedProblemId"
                placeholder="请选择一道题目"
                filterable
                remote
                :remote-method="searchProblems"
                :loading="problemLoading"
                class="problem-select"
                @change="handleProblemSelect"
              >
                <el-option
                  v-for="problem in problemList"
                  :key="problem.id"
                  :label="`${problem.id}. ${problem.name}`"
                  :value="problem.id"
                />
              </el-select>
              <el-button 
                type="primary" 
                :disabled="!selectedProblemId"
                @click="sendProblemToAI"
              >
                发送给AI
              </el-button>
            </div>

            <!-- 输入框 -->
            <div class="input-container">
              <div class="input-wrapper">
                <el-input
                  v-model="inputMessage"
                  type="textarea"
                  :rows="1"
                  placeholder="输入你的问题，按 Ctrl+Enter 发送..."
                  class="message-input"
                  @keydown.ctrl.enter="sendMessage"
                  :disabled="isThinking"
                  :autosize="{ minRows: 1, maxRows: 4 }"
                />
                <div class="input-actions">
                  <el-button 
                    type="primary" 
                    @click="sendMessage"
                    :disabled="!inputMessage.trim() || isThinking"
                    :loading="isThinking"
                    class="send-btn"
                  >
                    <el-icon><Position /></el-icon>
                    发送
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 详情弹窗 -->
      <el-dialog
        v-model="showDetailDialog"
        :title="detailDialog.title"
        width="800px"
        class="detail-dialog"
        :close-on-click-modal="false"
        :close-on-press-escape="true"
      >
        <div class="detail-content">
          <div class="detail-header">
            <div class="detail-icon">
              <el-icon size="32" color="#ffffff">
                <component :is="iconMap[detailDialog.icon]" />
              </el-icon>
            </div>
            <div class="detail-title-section">
              <h3 class="detail-title">{{ detailDialog.title }}</h3>
              <p class="detail-subtitle">{{ detailDialog.subtitle }}</p>
            </div>
          </div>
          
          <div class="detail-body">
            <div class="detail-description">
              <h4>功能说明</h4>
              <p>{{ detailDialog.description }}</p>
            </div>
            
            <div class="detail-features" v-if="detailDialog.features">
              <h4>主要特性</h4>
              <ul>
                <li v-for="feature in detailDialog.features" :key="feature">
                  <el-icon class="feature-icon"><Check /></el-icon>
                  {{ feature }}
                </li>
              </ul>
            </div>
            
            <div class="detail-examples" v-if="detailDialog.examples">
              <h4>使用示例</h4>
              <div class="example-item" v-for="(example, index) in detailDialog.examples" :key="index">
                <div class="example-title">{{ example.title }}</div>
                <div class="example-content">{{ example.content }}</div>
              </div>
            </div>
            
            <div class="detail-tips" v-if="detailDialog.tips">
              <h4>💡 小贴士</h4>
              <p>{{ detailDialog.tips }}</p>
            </div>
          </div>
        </div>
        
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showDetailDialog = false">关闭</el-button>
            <el-button 
              type="primary" 
              @click="handleDetailAction"
              v-if="detailDialog.action"
            >
              {{ detailDialog.actionText || '立即使用' }}
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, onMounted, nextTick, computed, watch } from 'vue'
  import { ElMessage } from 'element-plus'
  import { 
    ChatDotRound, 
    Check, 
    Loading, 
    Position, 
    Delete,
    QuestionFilled,
    Trophy,
    Setting,
    Star,
    Document,
    MagicStick,
    Tools,
    Refresh,
    Search,
    Download,
    Cpu,
    InfoFilled
  } from '@element-plus/icons-vue'
  import PageLayout from '@/components/layout/PageLayout.vue'
  import request from '@/utils/request.js'
  import { marked } from 'marked'
  import hljs from 'highlight.js'
  import 'highlight.js/styles/github.css'
  
  // 图标映射
  const iconMap = {
    'QuestionFilled': QuestionFilled,
    'MagicStick': MagicStick,
    'Document': Document,
    'Trophy': Trophy,
    'Star': Star,
    'ChatDotRound': ChatDotRound,
    'Tools': Tools
  }
  
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
  
  // 响应式数据
  const messages = ref([])
  const inputMessage = ref('')
  const isThinking = ref(false)
  const currentMode = ref('general')
  const showProblemSelector = ref(false)
  const selectedProblemId = ref(null)
  const problemList = ref([])
  const problemLoading = ref(false)
  const chatMessagesRef = ref(null)
  const showAPIWarning = ref(false)
  
  // 详情弹窗相关
  const showDetailDialog = ref(false)
  const detailDialog = ref({
    title: '',
    subtitle: '',
    icon: '',
    description: '',
    features: [],
    examples: [],
    tips: '',
    action: null,
    actionText: ''
  })
  
  // 用户头像
  const userAvatar = computed(() => {
    const localUser = localStorage.getItem('student-user') 
      ? JSON.parse(localStorage.getItem('student-user'))
      : localStorage.getItem('admin-user')
        ? JSON.parse(localStorage.getItem('admin-user'))
        : null;
    
    return localUser?.avatar || 'http://localhost:9090/uploads/1743236403200_IMG_0748.JPG'
  })
  
  // 快捷问题选项
  const quickQuestions = ref([
    {
      id: 'problem-solve',
      text: '这道题怎么解？',
      icon: 'QuestionFilled',
      action: 'problem-solve',
      title: '题目求解助手',
      subtitle: '专业的算法题目分析工具',
      description: '当你遇到编程题目时，我可以帮你分析题目要求、设计解题思路、提供代码实现，就像一位经验丰富的编程导师在身边指导。',
      features: [
        '详细分析题目要求和约束条件',
        '提供多种解题思路和算法选择',
        '生成完整的代码实现',
        '解释算法复杂度和优化方案',
        '提供测试用例和调试建议'
      ],
      examples: [
        {
          title: '动态规划题目',
          content: '分析状态转移方程，提供自底向上和自顶向下的解法'
        },
        {
          title: '图论算法',
          content: '解释最短路径、最小生成树等经典算法应用'
        }
      ],
      tips: '建议先自己思考5-10分钟，再寻求帮助，这样学习效果更好！'
    },
    {
      id: 'algorithm-help',
      text: '算法思路指导',
      icon: 'MagicStick',
      action: 'algorithm-help',
      title: '算法学习导师',
      subtitle: '从基础到高级的算法指导',
      description: '无论你是算法初学者还是想要提升的进阶者，我都能为你提供系统性的算法学习指导，让复杂的算法概念变得简单易懂。',
      features: [
        '基础数据结构详解（数组、链表、栈、队列）',
        '经典算法原理和实现（排序、搜索、递归）',
        '高级算法技巧（动态规划、贪心、分治）',
        '算法复杂度分析和优化',
        '实际应用场景和解题策略'
      ],
      examples: [
        {
          title: '排序算法',
          content: '从冒泡排序到快速排序，理解不同排序算法的适用场景'
        },
        {
          title: '递归思维',
          content: '掌握递归的基本原理和常见模式，避免栈溢出问题'
        }
      ],
      tips: '建议配合刷题练习，理论与实践相结合效果最佳！'
    },
    {
      id: 'code-review',
      text: '代码审查优化',
      icon: 'Document',
      action: 'code-review',
      title: '代码质量专家',
      subtitle: '专业的代码审查和优化服务',
      description: '我会像经验丰富的代码审查专家一样，仔细检查你的代码，发现潜在问题，提供优化建议，让你的代码更加优雅和高效。',
      features: [
        '代码逻辑错误检测和修复建议',
        '性能优化和算法改进',
        '代码风格和最佳实践指导',
        '安全漏洞识别和防护',
        '可读性和可维护性提升'
      ],
      examples: [
        {
          title: '性能优化',
          content: '识别时间复杂度瓶颈，提供更高效的算法实现'
        },
        {
          title: '代码规范',
          content: '检查命名规范、注释完整性、函数设计合理性'
        }
      ],
      tips: '好的代码不仅要能运行，更要易读、易维护、易扩展！'
    },
    {
      id: 'contest-prep',
      text: '竞赛准备建议',
      icon: 'Trophy',
      action: 'contest-prep',
      title: '竞赛备战教练',
      subtitle: '专业的算法竞赛指导',
      description: '无论你是准备ACM、ICPC、蓝桥杯还是其他编程竞赛，我都能为你制定个性化的备赛计划，提供针对性的训练建议。',
      features: [
        '个性化备赛计划制定',
        '知识点梳理和重点突破',
        '刷题策略和时间安排',
        '竞赛技巧和心理调节',
        '历年真题分析和预测'
      ],
      examples: [
        {
          title: '备赛时间规划',
          content: '根据剩余时间制定每日、每周的学习计划'
        },
        {
          title: '薄弱环节突破',
          content: '针对特定算法类型进行专项训练'
        }
      ],
      tips: '竞赛不仅是技术的比拼，更是心态和策略的较量！'
    },
    {
      id: 'learning-path',
      text: '学习路径规划',
      icon: 'Star',
      action: 'learning-path',
      title: '学习规划师',
      subtitle: '个性化的编程学习路线',
      description: '根据你的基础水平、学习目标和时间安排，为你量身定制最适合的学习路径，让学习更加高效和有针对性。',
      features: [
        '评估当前技能水平和学习目标',
        '制定阶段性学习计划',
        '推荐优质学习资源',
        '设计实践项目和练习',
        '跟踪学习进度和调整计划'
      ],
      examples: [
        {
          title: '初学者路径',
          content: '从编程基础语法到简单算法，循序渐进的学习安排'
        },
        {
          title: '进阶者路径',
          content: '针对特定技术栈的深入学习计划'
        }
      ],
      tips: '学习编程是一个持续的过程，保持耐心和坚持最重要！'
    }
  ])
  
  // 对话模式
  const chatModes = ref([
    {
      id: 'general',
      name: '通用对话',
      icon: 'ChatDotRound',
      systemPrompt: '你是一个专业的编程导师，擅长算法和数据结构。请用友好、专业的语气回答学生的问题，提供清晰的解释和实用的建议。',
      title: '通用编程助手',
      subtitle: '全方位的编程学习伙伴',
      description: '我是你的编程学习伙伴，可以回答各种编程相关的问题。无论是语法问题、算法思路、还是项目实践，我都会用最亲切的方式为你提供帮助。',
      features: [
        '编程语言语法和特性解答',
        '项目开发指导和最佳实践',
        '学习资源推荐和学习方法',
        '职业发展建议和规划',
        '技术趋势和前沿知识分享'
      ],
      examples: [
        {
          title: '语法问题',
          content: '解释JavaScript闭包、Python装饰器等高级特性'
        },
        {
          title: '项目指导',
          content: '从需求分析到架构设计，全程指导项目开发'
        }
      ],
      tips: '保持好奇心，多问多实践，编程技能会不断提升！'
    },
    {
      id: 'algorithm',
      name: '算法专精',
      icon: 'MagicStick',
      systemPrompt: '你是一个算法专家，专门帮助学生理解复杂的算法概念。请从基础开始，逐步深入，提供详细的算法分析和实现指导。',
      title: '算法学习专家',
      subtitle: '深入浅出的算法教学',
      description: '专注于算法和数据结构的学习指导，从基础概念到高级技巧，用最清晰的方式帮你掌握各种算法思想和实现方法。',
      features: [
        '基础数据结构详解（数组、链表、树、图）',
        '经典算法原理和实现（排序、搜索、动态规划）',
        '算法复杂度分析和性能优化',
        '实际应用场景和解题技巧',
        '算法竞赛和面试准备'
      ],
      examples: [
        {
          title: '动态规划',
          content: '从斐波那契数列到背包问题，掌握DP的核心思想'
        },
        {
          title: '图论算法',
          content: '深度优先搜索、广度优先搜索的实际应用'
        }
      ],
      tips: '算法学习需要大量练习，建议每天至少刷1-2道题！'
    },
    {
      id: 'debug',
      name: '调试助手',
      icon: 'Tools',
      systemPrompt: '你是一个代码调试专家，能够帮助学生快速定位和解决代码问题。请提供系统性的调试方法和最佳实践。',
      title: '代码调试专家',
      subtitle: '专业的代码问题诊断',
      description: '当你的代码出现问题时，我会像经验丰富的调试专家一样，帮你快速定位问题根源，提供解决方案和预防措施。',
      features: [
        '代码错误诊断和修复',
        '性能瓶颈识别和优化',
        '调试技巧和工具使用',
        '常见错误类型和预防',
        '代码质量评估和改进'
      ],
      examples: [
        {
          title: '运行时错误',
          content: '空指针异常、数组越界等常见问题的诊断和修复'
        },
        {
          title: '性能问题',
          content: '内存泄漏、算法效率低下的识别和优化'
        }
      ],
      tips: '调试是编程的重要技能，保持耐心和细心是关键！'
    }
  ])
  
  // 初始化欢迎消息
  const initWelcomeMessage = () => {
    const welcomeMessage = {
      role: 'assistant',
      content: `你好！我是 DeepSeek 编程导师，一个专业的算法题目高手。我可以帮助你：
  
  🎯 **算法学习指导** - 从基础到高级的算法知识
  💡 **题目解答** - 详细的解题思路和代码实现
  🔍 **代码优化** - 性能优化和最佳实践
  📚 **学习规划** - 个性化的学习路径建议
  🏆 **竞赛准备** - 算法竞赛的专项指导
  
  选择上方的快捷问题，或者直接向我提问吧！我会用最专业的方式帮助你提升编程技术。`,
      timestamp: new Date()
    }
    messages.value.push(welcomeMessage)
  }
  
  // 格式化时间
  const formatTime = (timestamp) => {
    return new Date(timestamp).toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }
  
  // 格式化消息内容 - 支持完整Markdown解析
  const formatMessage = (content) => {
    if (!content) return ''
    
    try {
      // 使用marked解析Markdown，支持代码高亮
      return marked(content)
    } catch (error) {
      console.error('Markdown解析错误:', error)
      // 如果解析失败，回退到简单的换行处理
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
  
  // 复制消息内容
  const copyMessage = async (content) => {
    try {
      // 移除HTML标签，获取纯文本
      const tempDiv = document.createElement('div')
      tempDiv.innerHTML = content
      const textContent = tempDiv.textContent || tempDiv.innerText || ''
      
      await navigator.clipboard.writeText(textContent)
      ElMessage.success('已复制到剪贴板')
    } catch (error) {
      console.error('复制失败:', error)
      ElMessage.error('复制失败，请手动复制')
    }
  }
  
  // 重新生成回答
  const regenerateResponse = async (message) => {
    // 找到当前消息的索引
    const messageIndex = messages.value.findIndex(m => m === message)
    if (messageIndex === -1) return
    
    // 移除当前AI回答
    messages.value.splice(messageIndex, 1)
    
    // 重新发送最后一个用户消息
    const lastUserMessage = messages.value[messages.value.length - 1]
    if (lastUserMessage && lastUserMessage.role === 'user') {
      await sendToAI(lastUserMessage.content)
    }
  }
  
  // 导出对话
  const exportChat = () => {
    if (messages.value.length === 0) {
      ElMessage.warning('没有对话内容可导出')
      return
    }
    
    let exportContent = '# AI 编程助手对话记录\n\n'
    exportContent += `导出时间: ${new Date().toLocaleString()}\n\n`
    
    messages.value.forEach((message, index) => {
      const role = message.role === 'user' ? '用户' : 'AI助手'
      const time = formatTime(message.timestamp)
      const duration = message.duration ? ` (用时: ${formatDuration(message.duration)})` : ''
      
      exportContent += `## ${role} - ${time}${duration}\n\n`
      exportContent += message.content + '\n\n'
      exportContent += '---\n\n'
    })
    
    // 创建下载链接
    const blob = new Blob([exportContent], { type: 'text/markdown' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `AI助手对话记录_${new Date().toISOString().slice(0, 10)}.md`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    
    ElMessage.success('对话记录已导出')
  }
  
  // 切换对话模式
  const switchMode = (modeId) => {
    currentMode.value = modeId
    ElMessage.success(`已切换到${chatModes.value.find(m => m.id === modeId)?.name}模式`)
  }
  
  // 显示快捷问题详情
  const showQuestionDetail = (question) => {
    detailDialog.value = {
      title: question.title,
      subtitle: question.subtitle,
      icon: question.icon,
      description: question.description,
      features: question.features,
      examples: question.examples,
      tips: question.tips,
      action: question.action,
      actionText: '立即使用'
    }
    showDetailDialog.value = true
  }
  
  // 显示对话模式详情
  const showModeDetail = (mode) => {
    detailDialog.value = {
      title: mode.title,
      subtitle: mode.subtitle,
      icon: mode.icon,
      description: mode.description,
      features: mode.features,
      examples: mode.examples,
      tips: mode.tips,
      action: mode.id,
      actionText: '切换模式'
    }
    showDetailDialog.value = true
  }
  
  // 处理详情弹窗的操作
  const handleDetailAction = () => {
    if (detailDialog.value.action) {
      if (detailDialog.value.actionText === '切换模式') {
        switchMode(detailDialog.value.action)
      } else {
        handleQuickQuestion({ action: detailDialog.value.action })
      }
      showDetailDialog.value = false
    }
  }
  
  // 处理快捷问题
  const handleQuickQuestion = (question) => {
    switch (question.action) {
      case 'problem-solve':
        showProblemSelector.value = true
        loadProblems()
        break
      case 'algorithm-help':
        sendSystemMessage('请为我提供算法学习指导，包括基础算法、数据结构、复杂度分析等内容。')
        break
      case 'code-review':
        sendSystemMessage('请帮我审查和优化代码，提供性能改进建议和最佳实践。')
        break
      case 'contest-prep':
        sendSystemMessage('请为我制定算法竞赛的备赛计划，包括知识点梳理、刷题策略、时间安排等。')
        break
      case 'learning-path':
        sendSystemMessage('请根据我的情况制定个性化的编程学习路径，包括学习顺序、资源推荐、实践项目等。')
        break
    }
  }
  
  // 加载题目列表
  const loadProblems = async () => {
    try {
      problemLoading.value = true
      const response = await request.get('/problem/simple')
      if (response.data.code === '200') {
        problemList.value = response.data.data
      }
    } catch (error) {
      ElMessage.error('加载题目列表失败')
    } finally {
      problemLoading.value = false
    }
  }
  
  // 搜索题目
  const searchProblems = async (query) => {
    if (!query) {
      loadProblems()
      return
    }
    
    try {
      const response = await request.get('/problem/search', {
        params: { keyword: query }
      })
      if (response.data.code === '200') {
        problemList.value = response.data.data
      }
    } catch (error) {
      console.error('搜索题目失败:', error)
    }
  }
  
  // 处理题目选择
  const handleProblemSelect = (problemId) => {
    selectedProblemId.value = problemId
  }
  
  // 发送题目给AI
  const sendProblemToAI = async () => {
    if (!selectedProblemId.value) return
    
    try {
      const response = await request.get(`/problem/${selectedProblemId.value}`)
      if (response.data.code === '200') {
        const problem = response.data.data
        const problemInfo = `题目：${problem.name}\n题目描述：${problem.desc}\n输入格式：${problem.descInput}\n输出格式：${problem.descOutput}\n示例输入：\n${problem.sampleInput}\n示例输出：\n${problem.sampleOutput}`
        
        sendSystemMessage(`请帮我分析这道题目：\n\n${problemInfo}\n\n请提供详细的解题思路、算法分析和代码实现。`)
        showProblemSelector.value = false
        selectedProblemId.value = null
      }
    } catch (error) {
      ElMessage.error('获取题目信息失败')
    }
  }
  
  // 发送系统消息
  const sendSystemMessage = (content) => {
    inputMessage.value = content
    sendMessage()
  }
  
  // 发送消息
  const sendMessage = async () => {
    if (!inputMessage.value.trim() || isThinking.value) return
    
    const userMessage = {
      role: 'user',
      content: inputMessage.value,
      timestamp: new Date()
    }
    
    messages.value.push(userMessage)
    const currentInput = inputMessage.value
    inputMessage.value = ''
    
    await nextTick()
    scrollToBottom()
    
    // 发送给AI
    await sendToAI(currentInput)
  }
  
  // 流式传输相关状态
  const isStreaming = ref(false)
  const currentStreamingMessage = ref('')
  const streamingMessageIndex = ref(-1)
  
  // 计时相关状态
  const responseStartTime = ref(null)
  const responseEndTime = ref(null)
  const responseDuration = ref(0)
  
  // 搜索和导出功能
  const searchQuery = ref('')
  const filteredMessages = computed(() => {
    if (!searchQuery.value.trim()) {
      return messages.value
    }
    return messages.value.filter(message => 
      message.content.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
  })

  // 发送给AI - 流式传输版本
  const sendToAI = async (userInput) => {
    isThinking.value = true
    isStreaming.value = true
    responseStartTime.value = Date.now()
    
    try {
      const currentModeConfig = chatModes.value.find(m => m.id === currentMode.value)
      const systemPrompt = currentModeConfig?.systemPrompt || chatModes.value[0].systemPrompt
      
      const aiMessages = [
        {
          role: 'system',
          content: systemPrompt
        },
        ...messages.value.map(msg => ({
          role: msg.role,
          content: msg.content
        }))
      ]
      
      // 创建空的AI消息占位符
      const aiResponse = {
        role: 'assistant',
        content: '',
        timestamp: new Date(),
        isStreaming: true
      }
      messages.value.push(aiResponse)
      streamingMessageIndex.value = messages.value.length - 1
      currentStreamingMessage.value = ''
      
      await nextTick()
      scrollToBottom()
      
      // 使用fetch进行流式传输
      const response = await fetch('https://api.deepseek.com/chat/completions', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer sk-ff342bebb7114fbbbf402971065c977e'
        },
        body: JSON.stringify({
          model: 'deepseek-chat',
          messages: aiMessages,
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
              messages.value[streamingMessageIndex.value].isStreaming = false
              messages.value[streamingMessageIndex.value].duration = responseDuration.value
              isStreaming.value = false
              isThinking.value = false
              await nextTick()
              if (chatMessagesRef.value) {
                chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
              }
              return
            }
            
            try {
              const parsed = JSON.parse(data)
              if (parsed.choices && parsed.choices[0] && parsed.choices[0].delta && parsed.choices[0].delta.content) {
                const content = parsed.choices[0].delta.content
                currentStreamingMessage.value += content
                messages.value[streamingMessageIndex.value].content = currentStreamingMessage.value
                
                // 平滑滚动到底部 - 每次内容更新都滚动
                await nextTick()
                if (chatMessagesRef.value) {
                  chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
                }
              }
            } catch (e) {
              console.warn('解析流式数据失败:', e)
            }
          }
        }
      }
    } catch (error) {
      console.error('AI请求失败:', error)
      
      let errorContent = '抱歉，我遇到了一些技术问题。请稍后再试，或者尝试重新表述你的问题。'
      
      if (error.message.includes('401')) {
        errorContent = 'API密钥验证失败，请联系管理员检查API配置。'
      } else if (error.message.includes('429')) {
        errorContent = '请求过于频繁，请稍后再试。'
      } else if (error.message) {
        errorContent = `网络错误: ${error.message}`
      }
      
      // 如果有流式消息占位符，替换它
      if (streamingMessageIndex.value >= 0) {
        messages.value[streamingMessageIndex.value] = {
          role: 'assistant',
          content: errorContent,
          timestamp: new Date(),
          isStreaming: false
        }
      } else {
        const errorMessage = {
          role: 'assistant',
          content: errorContent,
          timestamp: new Date()
        }
        messages.value.push(errorMessage)
      }
    } finally {
      isThinking.value = false
      isStreaming.value = false
      streamingMessageIndex.value = -1
      currentStreamingMessage.value = ''
      await nextTick()
      scrollToBottom()
    }
  }
  
  // 清空聊天
  const clearChat = () => {
    messages.value = []
    initWelcomeMessage()
    ElMessage.success('聊天记录已清空')
  }
  
  // 滚动到底部
  const scrollToBottom = () => {
    nextTick(() => {
      if (chatMessagesRef.value) {
        chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
      }
    })
  }
  
  // 测试API密钥
  const testAPIKey = async () => {
    try {
      // 直接使用axios，避免经过request拦截器
      const axios = (await import('axios')).default
      const response = await axios.post(
        'https://api.deepseek.com/chat/completions',
        {
          model: 'deepseek-chat',
          messages: [{ role: 'user', content: 'Hello' }],
          stream: false,
          max_tokens: 10
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer sk-ff342bebb7114fbbbf402971065c977e'
          },
          timeout: 10000
        }
      )
      
      if (response.data && response.data.choices) {
        console.log('API密钥测试成功')
        showAPIWarning.value = false
        return true
      }
    } catch (error) {
      console.error('API密钥测试失败:', error)
      if (error.response?.data?.error?.type === 'authentication_error') {
        ElMessage.error('API密钥无效，请检查密钥配置')
        showAPIWarning.value = true
      }
      return false
    }
  }

  // 监听聊天消息引用，确保可以正确滚动
  watch(chatMessagesRef, (newVal) => {
    if (newVal && messages.value.length > 0) {
      nextTick(() => {
        newVal.scrollTop = newVal.scrollHeight
      })
    }
  })

  // 组件挂载
  onMounted(async () => {
    initWelcomeMessage()
    // 测试API密钥
    await testAPIKey()
  })
  </script>
  
  <style scoped>
  .ai-assistant-fullscreen {
    height: 90vh;
    max-height: 90vh;
    display: flex;
    flex-direction: column;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #1e293b;
    overflow: hidden;
  }
  
  /* 顶部工具栏 */
  .ai-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  }
  
  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  
  .ai-title-row {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .ai-name {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1e293b;
  }
  
  .toolbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .search-input {
    width: 200px;
  }
  
  .export-btn {
    background: rgba(59, 130, 246, 0.1);
    border: 1px solid rgba(59, 130, 246, 0.2);
    color: #3b82f6;
    border-radius: 12px;
    transition: all 0.3s ease;
  }
  
  .export-btn:hover {
    background: #3b82f6;
    color: white;
    transform: translateY(-1px);
  }
  
  /* 主聊天区域 */
  .chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  
  /* 聊天消息区域 */
  .chat-messages {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(5px);
    position: relative;
  }
  
  .messages-container {
    flex: 1;
    overflow-y: auto;
    padding: 16px 24px;
    max-height: calc(100vh - 300px);
  }
  
  /* 消息样式 */
  .message {
    display: flex;
    margin-bottom: 24px;
    animation: messageSlideIn 0.4s ease-out;
    gap: 12px;
  }
  
  .message.user {
    flex-direction: row-reverse;
  }
  
  .message.streaming {
    animation: pulse 2s infinite;
  }
  
  .message-avatar {
    flex-shrink: 0;
  }
  
  .user-avatar {
    border: 2px solid #6366f1;
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  }
  
  .ai-avatar-small {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea, #764ba2);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    border: 2px solid rgba(255, 255, 255, 0.2);
  }
  
  .ai-avatar-small .el-icon {
    color: #ffffff !important;
  }
  
  .message-content {
    flex: 1;
    max-width: 70%;
  }
  
  .message.user .message-content {
    display: flex;
    justify-content: flex-end;
  }
  
  .message-bubble {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    padding: 16px 20px;
    position: relative;
    backdrop-filter: blur(10px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.2);
  }
  
  .message.user .message-bubble {
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    color: white;
    border-color: transparent;
  }
  
  .message-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    font-size: 12px;
    opacity: 0.8;
  }
  
  .ai-name-small {
    font-weight: 600;
    color: #6366f1;
  }
  
  .message-time {
    font-size: 11px;
    opacity: 0.7;
  }
  
  .user-time {
    text-align: right;
    margin-top: 8px;
  }
  
  .message-text {
    line-height: 1.6;
    word-wrap: break-word;
  }
  
  /* 思考状态内联显示 */
  .thinking-indicator-inline {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #6366f1;
    font-style: italic;
    margin: 8px 0;
    padding: 8px 12px;
    background: rgba(99, 102, 241, 0.05);
    border-radius: 8px;
    border-left: 3px solid #6366f1;
  }
  
  /* 消息操作按钮 */
  .message-actions {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid rgba(0, 0, 0, 0.1);
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
    color: #64748b;
    font-style: italic;
  }
  
  /* 流式传输光标 */
  .streaming-cursor {
    animation: blink 1s infinite;
    color: #6366f1;
    font-weight: bold;
  }
  
  /* 思考状态 */
  .thinking-message {
    opacity: 0.8;
  }
  
  .thinking-indicator {
    display: flex;
    align-items: center;
    gap: 12px;
    color: #6366f1;
    font-style: italic;
  }
  
  .thinking-dots {
    display: flex;
    gap: 4px;
  }
  
  .thinking-dots span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #6366f1;
    animation: thinkingBounce 1.4s infinite ease-in-out;
  }
  
  .thinking-dots span:nth-child(1) { animation-delay: -0.32s; }
  .thinking-dots span:nth-child(2) { animation-delay: -0.16s; }
  .thinking-dots span:nth-child(3) { animation-delay: 0s; }
  
  /* 底部输入区域 - 悬浮效果 */
  .chat-input-area {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(20px);
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 20px 20px 0 0;
    padding: 20px 24px;
    box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.1);
    position: sticky;
    bottom: 0;
    z-index: 10;
  }
  
  .input-toolbar {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-bottom: 16px;
  }
  
  .quick-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
  
  .quick-action-item {
    display: flex;
    align-items: center;
    gap: 4px;
  }
  
  .mode-action-item {
    display: flex;
    align-items: center;
    gap: 4px;
  }
  
  .quick-btn {
    background: rgba(255, 255, 255, 0.9);
    border: 1px solid rgba(99, 102, 241, 0.3);
    color: #6366f1;
    border-radius: 20px;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .quick-btn:hover {
    background: #6366f1;
    color: white;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  }
  
  .mode-selector {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
  
  .mode-btn {
    background: rgba(255, 255, 255, 0.9);
    border: 1px solid rgba(0, 0, 0, 0.1);
    color: #64748b;
    border-radius: 20px;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .mode-btn:hover {
    background: #6366f1;
    border-color: #6366f1;
    color: white;
    transform: translateY(-1px);
  }
  
  .mode-btn.el-button--primary {
    background: #6366f1;
    border-color: #6366f1;
    color: white;
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  }
  
  .info-btn {
    background: rgba(99, 102, 241, 0.1);
    border: 1px solid rgba(99, 102, 241, 0.2);
    color: #6366f1;
    border-radius: 50%;
    width: 28px;
    height: 28px;
    padding: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  .info-btn:hover {
    background: #6366f1;
    color: white;
    transform: scale(1.1);
    box-shadow: 0 4px 8px rgba(99, 102, 241, 0.3);
  }
  
  .info-btn .el-icon {
    font-size: 12px;
  }
  
  .problem-selector {
    display: flex;
    gap: 12px;
    align-items: center;
    padding: 16px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 12px;
    border: 1px solid rgba(99, 102, 241, 0.2);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .problem-select {
    flex: 1;
  }
  
  .input-container {
    margin-top: 16px;
  }
  
  .input-wrapper {
    display: flex;
    gap: 12px;
    align-items: flex-end;
  }
  
  .message-input {
    flex: 1;
    background: rgba(255, 255, 255, 0.95);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 16px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  .message-input:focus-within {
    border-color: rgba(99, 102, 241, 0);
    box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
  }
  
  .message-input :deep(.el-textarea__inner) {
    background: transparent;
    border: none;
    color: #1e293b;
    resize: none;
    border-radius: 16px;
    padding: 12px 16px;
    min-height: 40px;
    line-height: 1.5;
  }
  
  .message-input :deep(.el-textarea__inner)::placeholder {
    color: #94a3b8;
  }
  
  .input-actions {
    display: flex;
    align-items: flex-end;
  }
  
  .send-btn {
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    border: none;
    border-radius: 12px;
    padding: 12px 20px;
    height: auto;
    transition: all 0.3s ease;
  }
  
  .send-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(99, 102, 241, 0.4);
  }
  
  .clear-btn {
    background: rgba(239, 68, 68, 0.1);
    border: 1px solid rgba(239, 68, 68, 0.2);
    color: #ef4444;
    border-radius: 12px;
    transition: all 0.3s ease;
  }
  
  .clear-btn:hover {
    background: #ef4444;
    color: white;
    transform: translateY(-1px);
  }
  
  .status-tag {
    background: rgba(34, 197, 94, 0.1);
    border: 1px solid rgba(34, 197, 94, 0.2);
    color: #22c55e;
  }
  
  /* Markdown样式优化 */
  .message-text :deep(pre) {
    background: #1e293b;
    border-radius: 8px;
    padding: 16px;
    margin: 12px 0;
    font-family: 'JetBrains Mono', 'Courier New', monospace;
    font-size: 14px;
    overflow-x: auto;
    color: #e2e8f0;
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  .message-text :deep(code) {
    background: rgba(99, 102, 241, 0.1);
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'JetBrains Mono', 'Courier New', monospace;
    font-size: 13px;
    color: #6366f1;
    border: 1px solid rgba(99, 102, 241, 0.2);
  }
  
  .message-text :deep(pre code) {
    background: transparent;
    padding: 0;
    border: none;
    color: inherit;
  }
  
  .message-text :deep(h1), 
  .message-text :deep(h2), 
  .message-text :deep(h3), 
  .message-text :deep(h4), 
  .message-text :deep(h5), 
  .message-text :deep(h6) {
    color: #6366f1;
    margin: 16px 0 8px 0;
    font-weight: 600;
  }
  
  .message-text :deep(h1) { font-size: 1.5em; }
  .message-text :deep(h2) { font-size: 1.3em; }
  .message-text :deep(h3) { font-size: 1.2em; }
  
  .message-text :deep(ul), 
  .message-text :deep(ol) {
    margin: 8px 0;
    padding-left: 20px;
  }
  
  .message-text :deep(li) {
    margin: 4px 0;
    line-height: 1.6;
  }
  
  .message-text :deep(blockquote) {
    border-left: 4px solid #6366f1;
    padding-left: 16px;
    margin: 12px 0;
    color: #64748b;
    font-style: italic;
  }
  
  .message-text :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 12px 0;
  }
  
  .message-text :deep(th), 
  .message-text :deep(td) {
    border: 1px solid rgba(0, 0, 0, 0.1);
    padding: 8px 12px;
    text-align: left;
  }
  
  .message-text :deep(th) {
    background: rgba(99, 102, 241, 0.1);
    font-weight: 600;
  }
  
  /* 动画效果 */
  @keyframes messageSlideIn {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  @keyframes blink {
    0%, 50% { opacity: 1; }
    51%, 100% { opacity: 0; }
  }
  
  @keyframes thinkingBounce {
    0%, 80%, 100% {
      transform: scale(0);
    }
    40% {
      transform: scale(1);
    }
  }
  
  @keyframes pulse {
    0%, 100% {
      opacity: 1;
    }
    50% {
      opacity: 0.8;
    }
  }
  
  /* 移动端适配 */
  @media screen and (max-width: 768px) {
    .ai-toolbar {
      padding: 2px 16px;
    }
    
    .toolbar-left {
      gap: 12px;
    }
    
    .ai-name {
      font-size: 18px;
    }
    
    .chat-messages {
      padding: 16px;
    }
    
    .message-content {
      max-width: 85%;
    }
    
    .chat-input-area {
      padding: 16px;
    }
    
    .input-toolbar {
      gap: 12px;
    }
    
    .quick-actions, .mode-selector {
      flex-direction: column;
      align-items: stretch;
    }
    
    .quick-btn, .mode-btn {
      width: 100%;
      justify-content: center;
    }
    
    .problem-selector {
      flex-direction: column;
      align-items: stretch;
    }
    
    .input-wrapper {
      flex-direction: column;
      gap: 12px;
    }
    
    .send-btn {
      width: 100%;
    }
  }
  
  .ai-info {
    flex: 1;
  }
  
  .ai-name {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 700;
    color: #1e293b;
  }
  
  .ai-desc {
    margin: 0 0 12px 0;
    font-size: 14px;
    color: #64748b;
  }
  
  .ai-status {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .api-warning {
    margin-bottom: 24px;
  }
  
  .quick-questions, .chat-modes {
    margin-bottom: 24px;
    padding: 24px;
    background: white;
    border-radius: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    border: 1px solid #e2e8f0;
  }
  
  .quick-questions h3, .chat-modes h3 {
    margin: 0 0 16px 0;
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
  }
  
  .question-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 12px;
  }
  
  .question-btn {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    color: #475569;
    padding: 12px 16px;
    border-radius: 12px;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 500;
  }
  
  .question-btn:hover {
    background: #3b82f6;
    border-color: #3b82f6;
    color: white;
    transform: translateY(-1px);
    box-shadow: 0 4px 6px rgba(59, 130, 246, 0.15);
  }
  
  .mode-tabs {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }
  
  .mode-btn {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    color: #475569;
    padding: 10px 16px;
    border-radius: 12px;
    transition: all 0.2s ease;
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 500;
  }
  
  .mode-btn:hover {
    background: #3b82f6;
    border-color: #3b82f6;
    color: white;
    transform: translateY(-1px);
    box-shadow: 0 4px 6px rgba(59, 130, 246, 0.15);
  }
  
  .mode-btn.el-button--primary {
    background: #3b82f6;
    border-color: #3b82f6;
    color: white;
    box-shadow: 0 4px 6px rgba(59, 130, 246, 0.15);
  }
  
  .problem-selector {
    margin-bottom: 24px;
    padding: 24px;
    background: white;
    border-radius: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    border: 1px solid #e2e8f0;
  }
  
  .problem-selector h3 {
    margin: 0 0 16px 0;
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
  }
  
  .selector-content {
    display: flex;
    gap: 12px;
    align-items: center;
  }
  
  .problem-select {
    flex: 1;
    max-width: 400px;
  }
  
  .chat-container {
    background: white;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    border: 1px solid #e2e8f0;
  }
  
  .chat-messages {
    height: 500px;
    overflow-y: auto;
    padding: 24px;
    background: #f8fafc;
  }
  
  .message {
    display: flex;
    margin-bottom: 20px;
    animation: messageSlideIn 0.3s ease-out;
  }
  
  .message.user {
    flex-direction: row-reverse;
  }
  
  .message-avatar {
    flex-shrink: 0;
    margin: 0 12px;
  }
  
  .ai-avatar-small {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: linear-gradient(135deg, #3b82f6, #1d4ed8);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .message-content {
    flex: 1;
    max-width: 70%;
  }
  
  .message.user .message-content {
    display: flex;
    justify-content: flex-end;
  }
  
  .message-bubble {
    background: white;
    border-radius: 16px;
    padding: 16px 20px;
    position: relative;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    border: 1px solid #e2e8f0;
  }
  
  .message.user .message-bubble {
    background: #3b82f6;
    color: white;
    border-color: #3b82f6;
  }
  
  .message-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    font-size: 12px;
    opacity: 0.8;
  }
  
  .ai-name-small {
    font-weight: 600;
    color: #3b82f6;
  }
  
  .message-time {
    font-size: 11px;
    opacity: 0.7;
  }
  
  .user-time {
    text-align: right;
    margin-top: 8px;
  }
  
  .message-text {
    line-height: 1.5;
    word-wrap: break-word;
  }
  
  .thinking-indicator {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #3b82f6;
    font-style: italic;
  }
  
  .spin {
    animation: spin 1s linear infinite;
  }
  
  .dots span {
    animation: blink 1.4s infinite both;
  }
  
  .dots span:nth-child(1) { animation-delay: 0s; }
  .dots span:nth-child(2) { animation-delay: 0.2s; }
  .dots span:nth-child(3) { animation-delay: 0.4s; }
  
  .chat-input {
    padding: 24px;
    background: white;
    border-top: 1px solid #e2e8f0;
  }
  
  .input-container {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .message-input {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
  }
  
  .message-input :deep(.el-textarea__inner) {
    background: transparent;
    border: none;
    color: #1e293b;
    resize: none;
  }
  
  .message-input :deep(.el-textarea__inner)::placeholder {
    color: #94a3b8;
  }
  
  .input-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
  
  .code-block {
    background: #1e293b;
    border-radius: 8px;
    padding: 16px;
    margin: 12px 0;
    font-family: 'JetBrains Mono', 'Courier New', monospace;
    font-size: 14px;
    overflow-x: auto;
    color: #e2e8f0;
  }
  
  .inline-code {
    background: #f1f5f9;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'JetBrains Mono', 'Courier New', monospace;
    font-size: 13px;
    color: #1e293b;
  }
  
  @keyframes messageSlideIn {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  @keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }
  
  @keyframes blink {
    0%, 80%, 100% { opacity: 0; }
    40% { opacity: 1; }
  }
  
  /* 移动端适配 */
  @media screen and (max-width: 768px) {
    .ai-assistant-container {
      padding: 16px;
    }
    
    .ai-header {
      flex-direction: column;
      text-align: center;
      gap: 16px;
      padding: 20px;
    }
    
    .question-grid {
      grid-template-columns: 1fr;
    }
    
    .mode-tabs {
      flex-direction: column;
    }
    
    .selector-content {
      flex-direction: column;
      align-items: stretch;
    }
    
    .problem-select {
      max-width: none;
    }
    
    .chat-messages {
      height: 400px;
      padding: 20px;
    }
    
    .message-content {
      max-width: 85%;
    }
    
    .input-actions {
      flex-direction: column;
    }
    
    .quick-questions, .chat-modes, .problem-selector {
      padding: 20px;
    }
    
    .chat-input {
      padding: 20px;
    }
  }
  
  /* 详情弹窗样式 */
  .detail-dialog {
    border-radius: 16px;
    overflow: hidden;
  }
  
  .detail-dialog :deep(.el-dialog) {
    border-radius: 16px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
  }
  
  .detail-dialog :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    color: white;
    padding: 20px 24px;
    border-radius: 16px 16px 0 0;
  }
  
  .detail-dialog :deep(.el-dialog__title) {
    color: white;
    font-size: 18px;
    font-weight: 600;
  }
  
  .detail-dialog :deep(.el-dialog__headerbtn) {
    color: white;
  }
  
  .detail-dialog :deep(.el-dialog__body) {
    padding: 0;
  }
  
  .detail-content {
    background: white;
  }
  
  .detail-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    background: linear-gradient(135deg, #f8fafc, #e2e8f0);
    border-bottom: 1px solid #e2e8f0;
  }
  
  .detail-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 3px 8px rgba(99, 102, 241, 0.3);
  }
  
  .detail-title-section {
    flex: 1;
  }
  
  .detail-title {
    margin: 0 0 2px 0;
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
  }
  
  .detail-subtitle {
    margin: 0;
    font-size: 13px;
    color: #64748b;
  }
  
  .detail-body {
    padding: 16px 20px;
  }
  
  .detail-description,
  .detail-features,
  .detail-examples,
  .detail-tips {
    margin-bottom: 16px;
  }
  
  .detail-description h4,
  .detail-features h4,
  .detail-examples h4,
  .detail-tips h4 {
    margin: 0 0 8px 0;
    font-size: 15px;
    font-weight: 600;
    color: #1e293b;
    display: flex;
    align-items: center;
    gap: 6px;
  }
  
  .detail-description p {
    margin: 0;
    line-height: 1.6;
    color: #475569;
  }
  
  .detail-features ul {
    margin: 0;
    padding: 0;
    list-style: none;
  }
  
  .detail-features li {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 0;
    color: #475569;
    line-height: 1.4;
    font-size: 14px;
  }
  
  .feature-icon {
    color: #22c55e;
    font-size: 14px;
  }
  
  .example-item {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    padding: 8px 10px;
    margin-bottom: 6px;
  }
  
  .example-title {
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 2px;
    font-size: 13px;
  }
  
  .example-content {
    color: #64748b;
    font-size: 12px;
    line-height: 1.4;
  }
  
  .detail-tips {
    background: linear-gradient(135deg, #fef3c7, #fde68a);
    border: 1px solid #f59e0b;
    border-radius: 6px;
    padding: 10px 12px;
  }
  
  .detail-tips h4 {
    color: #92400e;
    margin-bottom: 4px;
    font-size: 13px;
  }
  
  .detail-tips p {
    margin: 0;
    color: #92400e;
    line-height: 1.4;
    font-size: 12px;
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding: 12px 20px;
    background: #f8fafc;
    border-top: 1px solid #e2e8f0;
  }
  
  /* 移动端适配 */
  @media screen and (max-width: 768px) {
    .detail-dialog :deep(.el-dialog) {
      width: 95% !important;
      margin: 0 auto;
    }
    
    .detail-header {
      flex-direction: column;
      text-align: center;
      gap: 8px;
      padding: 12px 16px;
    }
    
    .detail-body {
      padding: 12px 16px;
    }
    
    .detail-description,
    .detail-features,
    .detail-examples,
    .detail-tips {
      margin-bottom: 12px;
    }
    
    .quick-action-item,
    .mode-action-item {
      flex-direction: column;
      align-items: stretch;
      gap: 6px;
    }
    
    .info-btn {
      align-self: center;
    }
  }
</style>