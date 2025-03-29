<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as monaco from 'monaco-editor'

// 获取路由对象，用于获取页面的路由参数
const route = useRoute()
const router = useRouter()

// 用于存储当前选择的编程语言和主题
const selectedLanguage = ref('python')
const selectedTheme = ref('vs-light') // 默认使用 'vs-light' 主题
const code = ref('') // 存储编辑器中的代码

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

// 提交代码的函数，验证代码是否为空
const submitCode = () => {
  if (!code.value.trim()) {
    ElMessage.error('代码不能为空') // 如果没有输入代码，显示错误消息
    return
  }
  // 这里可以加入提交代码的逻辑（如API请求等）
  ElMessage.success('代码提交成功') // 提交成功后显示成功消息
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
      <el-select v-model="selectedLanguage" placeholder="选择语言" class="select">
        <el-option 
          v-for="lang in languages" 
          :key="lang.value" 
          :label="lang.label" 
          :value="lang.value">
        </el-option>
      </el-select>

      <!-- 选择主题下拉框 -->
      <el-select v-model="selectedTheme" placeholder="选择主题" class="select">
        <el-option 
          v-for="theme in themes" 
          :key="theme.value" 
          :label="theme.label" 
          :value="theme.value">
        </el-option>
      </el-select>
    </div>
    
    <!-- Monaco 编辑器容器 -->
    <div class="code-editor-container" ref="editorContainer"></div>
    
    <!-- 提交按钮 -->
    <el-button type="primary" @click="submitCode">提交</el-button>
  </div>
</template>

<style scoped>
.submit-code-container {
  padding: 20px;
  max-width: 1200px;
  margin: 10px auto;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  height: 80vh;
}

h2 {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #333333;
  text-align: center;
}

.settings {
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
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
  color: #fff;
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
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background-color: #f5f5f5;
  position: relative;
  height: 550px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

h1, h2 {
  color: #333333;
}

.el-select .el-input__inner {
  font-size: 14px;
}

.el-option {
  font-size: 14px;
}
</style>
