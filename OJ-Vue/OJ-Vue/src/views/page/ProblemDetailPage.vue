<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from "@/utils/request.js"

const route = useRoute()
const problem = ref({
  id: '',
  name: '',
  setter: '',
  createTime: '',
  acCount: 0,
  submitCount: 0,
  desc: '',
  descInput: '',
  descOutput: '',
  sampleInput: '',
  sampleOutput: '',
  hint: ''
})

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  try {
    if (dateTimeStr.includes('T')) {
      return dateTimeStr.replace('T', ' ').split('.')[0]
    }
    return dateTimeStr
  } catch (e) {
    console.error('时间格式化错误:', e)
    return '-'
  }
}

// 加载题目详情
const loadProblemDetail = async () => {
  try {
    const response = await request.get(`/problem/${route.params.id}`)
    if (response.data.code === '200') {
      problem.value = response.data.data
    } else {
      ElMessage.error(response.data.msg || '获取题目详情失败')
    }
  } catch (error) {
    console.error('获取题目详情失败:', error)
    ElMessage.error('获取题目详情失败')
  }
}

onMounted(() => {
  loadProblemDetail()
})
</script>

<template>
  <div class="problem-detail-container">
    <el-card class="problem-card">
      <template #header>
        <div class="card-header">
          <span class="problem-title">{{ problem.id }}. {{ problem.name }}</span>
          <div class="problem-meta">
            <el-tag size="small">出题人: {{ problem.setter }}</el-tag>
            <el-tag size="small" type="success">通过: {{ problem.acCount }}</el-tag>
            <el-tag size="small" type="info">提交: {{ problem.submitCount }}</el-tag>
            <el-tag size="small" type="warning">创建时间: {{ formatDateTime(problem.createTime) }}</el-tag>
          </div>
        </div>
      </template>

      <div class="problem-content">
        <div class="section">
          <h3>题目描述</h3>
          <div class="content">{{ problem.desc }}</div>
        </div>

        <div class="section">
          <h3>输入描述</h3>
          <div class="content">{{ problem.descInput }}</div>
        </div>

        <div class="section">
          <h3>输出描述</h3>
          <div class="content">{{ problem.descOutput }}</div>
        </div>

        <div class="section">
          <h3>输入样例</h3>
          <el-card class="sample-card">
            <pre>{{ problem.sampleInput }}</pre>
          </el-card>
        </div>

        <div class="section">
          <h3>输出样例</h3>
          <el-card class="sample-card">
            <pre>{{ problem.sampleOutput }}</pre>
          </el-card>
        </div>

        <div v-if="problem.hint" class="section">
          <h3>提示说明</h3>
          <div class="content">{{ problem.hint }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.problem-detail-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.problem-card {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.problem-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.problem-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.problem-content {
  padding: 20px 0;
}

.section {
  margin-bottom: 30px;
}

.section h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.content {
  font-size: 16px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.sample-card {
  background-color: #fafafa;
}

.sample-card pre {
  margin: 0;
  padding: 10px;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
}

:deep(.el-card__header) {
  padding: 15px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style> 