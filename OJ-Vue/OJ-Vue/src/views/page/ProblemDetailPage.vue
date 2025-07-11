<script setup lang="ts">
// 导入 Vue 的核心功能
import { ref, onMounted } from 'vue'
// 导入路由相关功能，用于获取路由参数和页面跳转
import { useRoute, useRouter } from 'vue-router'
// 导入 Element Plus 的消息提示组件
import { ElMessage } from 'element-plus'
// 导入自定义的请求工具
import request from "@/utils/request.js"
import BackButton from '@/components/BackButton.vue'
import PageLayout from '@/components/layout/PageLayout.vue'

// 获取当前路由实例，用于访问路由参数
const route = useRoute()
// 获取路由器实例，用于页面跳转
const router = useRouter()

// 使用 ref 创建响应式的题目数据对象
const problem = ref({
  id: '',            // 题目ID
  name: '',          // 题目名称
  setter: '',        // 出题人
  createTime: '',    // 创建时间
  acCount: 0,        // 通过次数
  submitCount: 0,    // 提交次数
  desc: '',          // 题目描述
  descInput: '',     // 输入描述
  descOutput: '',    // 输出描述
  sampleInput: '',   // 输入样例
  sampleOutput: '',  // 输出样例
  hint: ''           // 提示说明
})

// 格式化日期时间
// 将后端返回的ISO格式日期时间转换为更易读的格式
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'  // 如果日期为空，返回占位符
  try {
    if (dateTimeStr.includes('T')) {
      // 处理ISO格式的日期时间，例如：2023-01-01T12:00:00.000Z
      // 将T替换为空格，并去除毫秒部分
      return dateTimeStr.replace('T', ' ').split('.')[0]
    }
    return dateTimeStr  // 如果已经是格式化的日期，直接返回
  } catch (e) {
    console.error('时间格式化错误:', e)
    return '-'  // 出错时返回占位符
  }
}

// 加载题目详情
// 通过API请求获取当前题目的详细信息
const loadProblemDetail = async () => {
  try {
    // 发送GET请求获取题目详情，使用路由参数中的ID
    const response = await request.get(`/problem/${route.params.id}`)
    if (response.data.code === '200') {
      // 请求成功，更新题目数据
      problem.value = response.data.data
    } else {
      // 请求返回错误码，显示错误消息
      ElMessage.error(response.data.msg || '获取题目详情失败')
    }
  } catch (error) {
    // 请求异常处理
    console.error('获取题目详情失败:', error)
    ElMessage.error('获取题目详情失败')
  }
}

// 跳转到提交代码页面
// 用户点击"提交代码"按钮时触发
const goToSubmitPage = () => {
  console.log(problem.value);  // 调试用，输出当前题目信息
  
  // 使用路由导航到提交页面，并传递题目ID和名称作为参数
  router.push({
    name: 'SubmitPage',
    params: {
      problemId: problem.value.id,
      problemName: encodeURIComponent(problem.value.name),
      sampleInput: encodeURIComponent(problem.value.sampleInput),
      sampleOutput: encodeURIComponent(problem.value.sampleOutput)
    }
  })
}

// 组件挂载完成后自动加载题目详情
onMounted(() => {
  loadProblemDetail()
})
</script>

<template>
  <page-layout 
    title="题目详情" 
    :show-back="true"
    fallback-path="/problemListPage"
  >
    <!-- 页面内容 -->
    <div class="problem-detail">
      <div class="problem-detail-container">
        <!-- 使用Element Plus的卡片组件展示题目信息 -->
        <el-card class="problem-card animate__animated animate__fadeIn">
          <!-- 卡片头部，显示题目标题和元信息 -->
          <template #header>
            <div class="card-header">
              <div class="header-top">
                <span class="problem-title animate__animated animate__fadeInDown">{{ problem.id }}. {{ problem.name }}</span>
              </div>
              <!-- 题目元信息区域 -->
              <div class="problem-meta">
                <!-- 出题人信息标签 -->
                <el-tag size="small" class="animate__animated animate__fadeInLeft" style="animation-delay: 0.2s">出题人: {{ problem.setter }}</el-tag>
                <!-- 通过次数标签 -->
                <el-tag size="small" type="success" class="animate__animated animate__fadeInLeft" style="animation-delay: 0.3s">通过: {{ problem.acCount }}</el-tag>
                <!-- 提交次数标签 -->
                <el-tag size="small" type="info" class="animate__animated animate__fadeInLeft" style="animation-delay: 0.4s">提交: {{ problem.submitCount }}</el-tag>
                <!-- 创建时间标签 -->
                <el-tag size="small" type="warning" class="animate__animated animate__fadeInLeft" style="animation-delay: 0.5s">创建时间: {{ formatDateTime(problem.createTime) }}</el-tag>
                <!-- 提交按钮 -->
                <el-button type="primary" class="submit-btn animate__animated animate__fadeInLeft" style="animation-delay: 0.6s" @click="goToSubmitPage">提交代码</el-button>
              </div>
            </div>
          </template>

          <!-- 题目内容主体部分 -->
          <div class="problem-content">
            <!-- 题目描述部分 -->
            <div class="section animate__animated animate__fadeInUp" style="animation-delay: 0.7s">
              <h3>题目描述</h3>
              <div class="content">{{ problem.desc }}</div>
            </div>

            <!-- 输入描述部分 -->
            <div class="section animate__animated animate__fadeInUp" style="animation-delay: 0.8s">
              <h3>输入描述</h3>
              <div class="content">{{ problem.descInput }}</div>
            </div>

            <!-- 输出描述部分 -->
            <div class="section animate__animated animate__fadeInUp" style="animation-delay: 0.9s">
              <h3>输出描述</h3>
              <div class="content">{{ problem.descOutput }}</div>
            </div>

            <!-- 输入样例部分 -->
            <div class="section animate__animated animate__fadeInUp" style="animation-delay: 1s">
              <h3>输入样例</h3>
              <el-card class="sample-card">
                <pre>{{ problem.sampleInput }}</pre>
              </el-card>
            </div>

            <!-- 输出样例部分 -->
            <div class="section animate__animated animate__fadeInUp" style="animation-delay: 1.1s">
              <h3>输出样例</h3>
              <el-card class="sample-card">
                <pre>{{ problem.sampleOutput }}</pre>
              </el-card>
            </div>

            <!-- 提示说明部分，仅当hint存在时显示 -->
            <div v-if="problem.hint" class="section animate__animated animate__fadeInUp" style="animation-delay: 1.2s">
              <h3>提示说明</h3>
              <div class="content">{{ problem.hint }}</div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </page-layout>
</template>

<style scoped>
/* 导入动画库 */
@import 'animate.css';

/* 题目详情页面的主容器样式 */
.problem-detail-container {
  padding: 20px;
  background-color: #f5f7fa;  /* 浅灰色背景 */
  min-height: 100vh;  /* 最小高度为视口高度，确保内容少时也能填满屏幕 */
  perspective: 1000px;
}

/* 添加移动端适配样式 */
@media screen and (max-width: 768px) {
  .problem-detail-container {
    padding: 10px;
  }
  
  .problem-card {
    margin: 0;
  }
  
  .card-header {
    gap: 8px;
  }
  
  .problem-title {
    font-size: 20px;
  }
  
  .problem-meta {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  :deep(.el-tag) {
    font-size: 12px;
  }
  
  .section h3 {
    font-size: 16px;
  }
  
  .content {
    font-size: 14px;
  }
  
  .sample-card {
    margin: 10px 0;
  }
  
  .sample-card pre {
    font-size: 12px;
    padding: 8px;
  }
  
  :deep(.el-card__header) {
    padding: 10px;
  }
  
  :deep(.el-card__body) {
    padding: 15px;
  }
  
  :deep(.el-button) {
    font-size: 14px;
    padding: 8px;
  }
}

/* 添加移动端适配样式 */
@media screen and (max-width: 768px) {
  .problem-detail-container {
    padding: 10px;
  }
  
  .problem-card {
    margin: 0;
  }
  
  .card-header {
    gap: 8px;
  }
  
  .problem-title {
    font-size: 20px;
  }
  
  .problem-meta {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  :deep(.el-tag) {
    font-size: 12px;
  }
  
  .section h3 {
    font-size: 16px;
  }
  
  .content {
    font-size: 14px;
  }
  
  .sample-card {
    margin: 10px 0;
  }
  
  .sample-card pre {
    font-size: 12px;
    padding: 8px;
  }
  
  :deep(.el-card__header) {
    padding: 10px;
  }
  
  :deep(.el-card__body) {
    padding: 15px;
  }
  
  :deep(.el-button) {
    font-size: 14px;
    padding: 8px;
  }
}

/* 题目卡片样式 */
.problem-card {
  max-width: 1200px;  /* 限制最大宽度，提高可读性 */
  margin: 0 auto;  /* 水平居中 */
  transition: transform 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.problem-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

/* 卡片头部样式 */
.card-header {
  display: flex;
  flex-direction: column;  /* 垂直排列 */
  gap: 10px;  /* 元素间距 */
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 20px;
}

/* 题目标题样式 */
.problem-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;  /* 深色文字 */
  line-height: 1.2;
}

/* 题目元信息区域样式 */
.problem-meta {
  display: flex;
  gap: 10px;  /* 标签间距 */
  flex-wrap: wrap;  /* 允许换行，适应小屏幕 */
  align-items: center;  /* 垂直居中对齐 */
  justify-content: space-between;  /* 标签靠左，按钮靠右 */
}

/* 题目内容区域样式 */
.problem-content {
  padding: 20px 0;
}

/* 各个内容部分的样式 */
.section {
  margin-bottom: 30px;  /* 部分之间的间距 */
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.5s ease forwards;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 部分标题样式 */
.section h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;  /* 底部分隔线 */
}

/* 内容文本样式 */
.content {
  font-size: 16px;
  line-height: 1.6;  /* 行高，提高可读性 */
  color: #606266;  /* 中等深度的文字颜色 */
  white-space: pre-wrap;  /* 保留空白符和换行符 */
  transition: all 0.3s ease;
}

/* 样例卡片样式 */
.sample-card {
  background-color: #fafafa;  /* 浅色背景，区分样例 */
  transition: all 0.3s ease;
}

.sample-card:hover {
  transform: translateX(5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 样例代码样式 */
.sample-card pre {
  margin: 0;
  padding: 10px;
  font-family: monospace;  /* 等宽字体，适合代码展示 */
  white-space: pre-wrap;  /* 保留格式但允许自动换行 */
  word-break: break-all;  /* 允许在任意字符间断行 */
}

/* 深度选择器，修改Element Plus组件内部样式 */
:deep(.el-card__header) {
  padding: 15px 20px;  /* 调整卡片头部内边距 */
}

:deep(.el-card__body) {
  padding: 20px;  /* 调整卡片主体内边距 */
}

/* 提交按钮样式 */
.submit-btn {
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  font-size: 14px;
  padding: 8px 16px;
  height: auto;
  margin-left: auto;  /* 将按钮推到最右边 */
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.submit-btn::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.6s ease, height 0.6s ease;
}

.submit-btn:hover::after {
  width: 200%;
  height: 200%;
}

/* 标签样式 */
:deep(.el-tag) {
  transition: all 0.3s ease;
  cursor: pointer;
}

:deep(.el-tag:hover) {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style> 