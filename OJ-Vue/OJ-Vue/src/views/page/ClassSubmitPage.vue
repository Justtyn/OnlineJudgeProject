<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import * as monaco from 'monaco-editor'
import request from "@/utils/request.js"

// 获取路由对象，用于获取页面的路由参数
const route = useRoute()
const router = useRouter()

// 从本地存储获取班级ID和问题ID
const getClassProblemInfo = () => {
  const infoStr = localStorage.getItem('classProbSubInfo')
  if (!infoStr) {
    console.warn('未找到班级和问题信息')
    return { classId: null, problemId: null }
  }
  try {
    return JSON.parse(infoStr)
  } catch (error) {
    console.error('解析班级和问题信息失败:', error)
    return { classId: null, problemId: null }
  }
}

// 获取班级和问题信息
const classProbInfo = getClassProblemInfo()
const classId = classProbInfo.classId

// 语言ID映射
const languageIdMap = {
  'c': 1,
  'cpp': 2,
  'java': 4,
  'python': 10
}

// 语言ID到字符串的反向映射
const languageStrMap = {
  1: 'C',
  2: 'C++',
  4: 'Java',
  10: 'Python'
}

// 获取当前登录用户信息
const getCurrentUser = () => {
  const userStr = localStorage.getItem('student-user')
  if (!userStr) {
    ElMessage.error('用户未登录')
    router.push('/login')
    return null
  }
  try {
    return JSON.parse(userStr)
  } catch (error) {
    console.error('解析用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
    return null
  }
}

// 从本地存储获取用户信息和token
const getAuthInfo = () => {
  const userStr = localStorage.getItem('student-user')
  if (!userStr) return null
  try {
    const userInfo = JSON.parse(userStr)
    return {
      token: userInfo.token,
      user: userInfo
    }
  } catch (error) {
    console.error('解析用户信息失败:', error)
    return null
  }
}

// 用于存储当前选择的编程语言和主题
const selectedLanguage = ref('cpp')  // 默认使用 C++
const selectedTheme = ref('vs-light') // 默认使用 'vs-light' 主题
const code = ref('') // 存储编辑器中的代码
const useBase64 = ref(false) // 是否使用base64编码提交

// 支持的编程语言和主题选项
const languages = [
  { value: 'java', label: 'java' },
  { value: 'c', label: 'c' },
  { value: 'cpp', label: 'c++' },
  { value: 'python', label: 'python3' }
]

const themes = [
  { value: 'vs-light', label: 'Light' },
  { value: 'vs-dark', label: 'Dark' }
]

// Monaco 编辑器的配置项
const editorOptions = {
  selectOnLineNumbers: true, // 选择行号
  lineNumbers: 'on', // 显示行号
  scrollBeyondLastLine: false, // 不让滚动条超出最后一行
  wordWrap: 'on', // 自动换行
  minimap: { enabled: false }, // 禁用右侧小地图
  fontSize: 14, // 字体大小
  lineHeight: 1.5 // 行高
}

// 用于存储提交结果的数据
const submissionResult = ref(null)
const isSubmitting = ref(false)

// 格式化当前时间为 YYYY-MM-DD HH:mm:ss
const formatDateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 提交状态记录
const submitStatus = async (judgeResult) => {
  const user = getCurrentUser()
  if (!user) return

  // 获取认证信息
  const authInfo = getAuthInfo()
  if (!authInfo) {
    ElMessage.error('获取认证信息失败')
    return
  }

  try {
    const statusData = {
      problemId: Number(problemId),
      userId: user.id,
      username: user.username,
      time: `${Math.round(parseFloat(judgeResult.time) * 1000)}ms`,
      creatTime: formatDateTime(),
      language: languageStrMap[languageIdMap[selectedLanguage.value]],
      memory: judgeResult.memory.toString(), // 直接使用返回的内存值（已经是KB）
      status: judgeResult.status.description,
      classId: classId // 添加班级ID
    }

    // 先记录提交状态
    await request.post('/status', statusData)

    // 增加问题的提交次数
    try {
      await request.put(`/problem/${problemId}/submit`)
    } catch (error) {
      console.error('更新问题提交次数失败:', error)
      ElMessage.error('更新问题提交次数失败')
    }

    // 增加用户的提交次数
    try {
      await request.put(`/api/student/incrementSubmit/${user.id}`, null, {
        headers: { Authorization: 'Bearer ' + authInfo.token }
      })
    } catch (error) {
      console.error('更新用户提交次数失败:', error)
      ElMessage.error('更新用户提交次数失败')
    }
    
    // 增加班级题目的提交次数
    try {
      await request.put(`/homework-problem/increment-submit?homeworkId=${classProbInfo.classId}&problemId=${problemId}`)
    } catch (error) {
      console.error('更新班级题目提交次数失败:', error)
      ElMessage.error('更新班级题目提交次数失败')
    }

    // 如果是通过的提交，增加通过次数
    if (judgeResult.status.description === 'Accepted') {
      try {
        // 增加问题的通过次数
        await request.put(`/problem/${problemId}/ac`)
        
        // 增加用户的通过次数
        await request.put(`/api/student/incrementAc/${user.id}`, null, {
          headers: { Authorization: 'Bearer ' + authInfo.token }
        })
        
        // 增加班级题目的通过次数
        await request.put(`/homework-problem/increment-ac?homeworkId=${classProbInfo.classId}&problemId=${problemId}`)
      } catch (error) {
        console.error('更新通过次数失败:', error)
        ElMessage.error('更新通过次数失败')
      }
    }

    // 提交状态成功后跳转到状态列表页面
    router.push('/statusListPage')
    ElMessage.success('提交成功，正在跳转到状态列表...')
  } catch (error) {
    console.error('提交状态记录失败:', error)
    ElMessage.error('状态记录保存失败')
  }
}

// 工具函数：将字符串转换为base64
const toBase64 = (str) => {
  return btoa(unescape(encodeURIComponent(str)))
}

// 工具函数：将base64转换为字符串
const fromBase64 = (str) => {
  return decodeURIComponent(escape(atob(str)))
}

// 处理base64编码的提交
const submitCodeWithBase64 = async () => {
  if (!code.value.trim()) {
    ElMessage.error('代码不能为空')
    return
  }

  const loading = ElLoading.service({
    lock: true,
    text: '代码提交中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  isSubmitting.value = true

  try {
    const sampleInput = decodeURIComponent(route.params.sampleInput)
    const sampleOutput = decodeURIComponent(route.params.sampleOutput)

    const submitData = {
      source_code: toBase64(code.value),
      language_id: languageIdMap[selectedLanguage.value],
      stdin: toBase64(sampleInput),
      expected_output: toBase64(sampleOutput)
    }

    const response = await request.post('http://localhost:2358/submissions?wait=true&base64_encoded=true', submitData)
    
    // 处理返回结果中的base64编码
    const result = {
      ...response.data,
      stdout: response.data.stdout ? fromBase64(response.data.stdout) : null,
      stderr: response.data.stderr ? fromBase64(response.data.stderr) : null,
      compile_output: response.data.compile_output ? fromBase64(response.data.compile_output) : null,
      message: response.data.message ? fromBase64(response.data.message) : null
    }
    
    submissionResult.value = result
    await submitStatus(result)

    if (result.status && result.status.description === 'Accepted') {
      ElMessage.success('代码提交成功')
    } else {
      ElMessage.error(result.status?.description || '提交失败')
    }
  } catch (error) {
    console.error('提交代码失败:', error)
    ElMessage.error('提交代码失败')
  } finally {
    loading.close()
    isSubmitting.value = false
  }
}

// 修改原有的submitCode函数
const submitCode = async () => {
  if (useBase64.value) {
    await submitCodeWithBase64()
    return
  }
  
  if (!code.value.trim()) {
    ElMessage.error('代码不能为空')
    return
  }

  // 创建全屏加载
  const loading = ElLoading.service({
    lock: true,
    text: '代码提交中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  isSubmitting.value = true

  try {
    const sampleInput = decodeURIComponent(route.params.sampleInput)
    const sampleOutput = decodeURIComponent(route.params.sampleOutput)

    const submitData = {
      source_code: code.value,
      language_id: languageIdMap[selectedLanguage.value],
      stdin: sampleInput,
      expected_output: sampleOutput
    }

    const response = await request.post('http://localhost:2358/submissions?wait=true', submitData)
    submissionResult.value = response.data

    // 如果判题请求成功（不论结果如何），提交状态记录
    if (response.data) {
      await submitStatus(response.data)
    }

    if (response.data.status && response.data.status.description === 'Accepted') {
      ElMessage.success('代码提交成功')
    } else {
      ElMessage.error(response.data.status?.description || '提交失败')
    }
  } catch (error) {
    console.error('提交代码失败:', error)
    ElMessage.error('提交代码失败')
  } finally {
    loading.close()
    isSubmitting.value = false
  }
}

// 获取路由参数中的题目信息
const problemId = route.params.problemId
const problemName = decodeURIComponent(route.params.problemName) // 解码题目名称

// Monaco 编辑器实例
let editor = null
const editorContainer = ref(null) // 引用编辑器容器

// 处理编辑器内容变化
const handleEditorChange = () => {
  code.value = editor.getValue() // 将编辑器内容同步到 `code` 变量
}

// 初始化 Monaco 编辑器
onMounted(() => {
  nextTick(() => {
    // 创建 Monaco 编辑器实例
    editor = monaco.editor.create(editorContainer.value, {
      value: code.value,
      language: selectedLanguage.value,
      theme: selectedTheme.value,
      ...editorOptions
    })

    // 监听编辑器内容的变化
    editor.onDidChangeModelContent(handleEditorChange)
  })
})

// 清理编辑器实例
watch(editorContainer, (newVal, oldVal) => {
  if (editor && oldVal) {
    editor.dispose() // 组件销毁时清理编辑器实例
  }
})

// 监听语言的变化
watch(selectedLanguage, (newLang) => {
  if (editor) {
    const model = editor.getModel();
    monaco.editor.setModelLanguage(model, newLang); // 动态切换语言
  }
})

// 监听主题的变化
watch(selectedTheme, (newTheme) => {
  if (editor) {
    monaco.editor.setTheme(newTheme) // 动态切换主题
  }
});
</script>

<template>
  <div class="submit-code-container">
    <h2>{{ problemName }} - 提交代码</h2>

    <div class="settings">
      <!-- 选择语言下拉框 -->
      <el-select v-model="selectedLanguage" placeholder="选择语言" class="select" :disabled="isSubmitting">
        <el-option v-for="lang in languages" :key="lang.value" :label="lang.label" :value="lang.value">
        </el-option>
      </el-select>

      <!-- 选择主题下拉框 -->
      <el-select v-model="selectedTheme" placeholder="选择主题" class="select" :disabled="isSubmitting">
        <el-option v-for="theme in themes" :key="theme.value" :label="theme.label" :value="theme.value">
        </el-option>
      </el-select>

      <!-- Base64编码切换开关 -->
      <el-switch
        v-model="useBase64"
        active-text="使用Base64编码"
        inactive-text="普通提交"
        :disabled="isSubmitting"
        class="base64-switch"
      />
    </div>

    <!-- Monaco 编辑器容器 -->
    <div class="code-editor-container" ref="editorContainer"></div>

    <!-- 提交按钮 -->
    <el-button type="primary" @click="submitCode" :loading="isSubmitting" :disabled="isSubmitting">
      {{ isSubmitting ? '提交中...' : '提交' }}
    </el-button>
  </div>
</template>

<style scoped>
.submit-code-container {
  padding: 20px;
  max-width: 1200px;
  margin: 10px auto;
  background-color: var(--color-background);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  height: 80vh;
}

/* 添加移动端适配样式 */
@media screen and (max-width: 768px) {
  .submit-code-container {
    padding: 10px;
    height: auto;
    min-height: 80vh;
  }
  
  h2 {
    font-size: 20px;
    margin-bottom: 15px;
  }
  
  .settings {
    flex-direction: column;
    gap: 10px;
  }
  
  .select {
    width: 100%;
  }
  
  .code-editor-container {
    height: 400px;
    margin-bottom: 15px;
  }
  
  :deep(.monaco-editor) {
    font-size: 14px;
  }
  
  :deep(.el-button) {
    font-size: 14px;
    padding: 8px;
  }
  
  :deep(.el-select) {
    width: 100%;
  }
  
  :deep(.el-input__inner) {
    font-size: 14px;
  }
}

h2 {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 10px;
  color: var(--color-heading);
  text-align: center;
}

.settings {
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 10px;
}

.base64-switch {
  margin-top: 10px;
  width: 48%;
}

.el-select {
  width: 200px;
  margin-top: 10px;
}

.select {
  width: 48%;
}

.el-button {
  margin-top: 10px;
  width: 100%;
  background-color: #409EFF;
  border-color: #409EFF;
  color: var(--color-background);
  font-size: 16px;
  padding: 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.el-button:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.code-editor-container {
  margin-bottom: 20px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: var(--bg-color-mute);
  position: relative;
  height: 550px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

h1,
h2 {
  color: var(--color-heading);
}

.el-select .el-input__inner {
  font-size: 14px;
}

.el-option {
  font-size: 14px;
}
</style>
