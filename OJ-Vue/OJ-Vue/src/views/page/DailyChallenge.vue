<script setup>
import { ref, onMounted, computed, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import request from "@/utils/request.js"
import 'animate.css'

const router = useRouter()
const loading = ref(false)
const dailyProblems = ref([])
const completedProblems = ref([])
const currentDate = ref(new Date().toISOString().split('T')[0])
let dateCheckInterval = null
const error = ref(null)

// 获取每日挑战题目
const loadDailyProblems = async (retryCount = 3) => {
  loading.value = true
  try {
    const response = await request.get('/problem/daily-challenge')
    if (response.data?.code === '200') {
      dailyProblems.value = response.data.data
      
      // 获取已完成的题目
      await loadCompletedProblems()
    } else {
      ElMessage.error(response.data?.msg || '获取每日挑战题目失败')
    }
  } catch (error) {
    if (retryCount > 0) {
      await new Promise(resolve => setTimeout(resolve, 1000))
      return loadDailyProblems(retryCount - 1)
    }
    error.value = '加载失败，请刷新页面重试'
  } finally {
    loading.value = false
  }
}

// 获取已完成的题目
const loadCompletedProblems = async () => {
  try {
    // 从localStorage获取用户信息
    const localUser = localStorage.getItem('student-user') 
      ? JSON.parse(localStorage.getItem('student-user'))
      : localStorage.getItem('admin-user')
        ? JSON.parse(localStorage.getItem('admin-user'))
        : null;

    if (!localUser || !localUser.id) {
      console.error('未找到用户信息')
      return
    }

    const response = await request.get(`/status/user/${localUser.id}/daily`, {
      params: {
        date: currentDate.value
      }
    })
    
    if (response.data?.code === '200') {
      // 获取当日挑战题目的ID列表
      const dailyProblemIds = dailyProblems.value.map(problem => problem.id)
      
      // 只保留状态为Accepted且属于当日挑战题目的题目ID，并去重
      completedProblems.value = [...new Set(
        response.data.data
          .filter(status => 
            status.status === 'Accepted' && 
            dailyProblemIds.includes(status.problemId)
          )
          .map(status => status.problemId)
      )]
      console.log('已完成的挑战题目ID:', completedProblems.value)
    } else {
      ElMessage.error(response.data?.msg || '获取已完成题目失败')
    }
  } catch (error) {
    console.error('获取已完成题目失败:', error)
    ElMessage.error('获取已完成题目失败：' + (error.response?.data?.msg || error.message))
  }
}

// 计算完成进度
const progress = computed(() => {
  return (completedProblems.value.length / dailyProblems.value.length) * 100
})

// 检查是否完成所有题目
const isAllCompleted = computed(() => {
  return completedProblems.value.length === dailyProblems.value.length && dailyProblems.value.length > 0
})

// 监听完成状态变化
watch(isAllCompleted, async (newValue) => {
  if (newValue) {
    // 从localStorage获取用户信息
    const localUser = localStorage.getItem('student-user') 
      ? JSON.parse(localStorage.getItem('student-user'))
      : localStorage.getItem('admin-user')
        ? JSON.parse(localStorage.getItem('admin-user'))
        : null;

    if (!localUser || !localUser.id) {
      console.error('未找到用户信息')
      return
    }

    try {
      const response = await request.put('/api/student/updateDailyChallenge', {
        id: localUser.id,
        dailyChallenge: "TRUE"
      }, {
        headers: { Authorization: 'Bearer ' + localUser.token }
      })

      if (response.data?.code === '200') {
        ElMessage({
          message: '恭喜你完成今日所有挑战！',
          type: 'success',
          duration: 3000
        })
      } else {
        ElMessage.error(response.data?.msg || '更新每日挑战状态失败')
      }
    } catch (error) {
      console.error('更新每日挑战状态失败:', error)
      ElMessage.error('更新每日挑战状态失败：' + (error.response?.data?.msg || error.message))
    }
  }
})

// 跳转到题目详情
const goToProblem = (problemId) => {
  router.push(`/problem/${problemId}`)
}

// 检查题目是否已完成
const isProblemCompleted = (problemId) => {
  console.log('检查题目完成状态:', {
    problemId,
    completedProblems: completedProblems.value,
    isCompleted: completedProblems.value.includes(problemId)
  })
  return completedProblems.value.includes(problemId)
}

// 获取当前日期（使用本地时区）
const getCurrentDate = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化日期显示
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// 检查并更新日期
const checkAndUpdateDate = () => {
  const newDate = getCurrentDate()
  if (newDate !== currentDate.value) {
    currentDate.value = newDate
    loadDailyProblems() // 重新加载每日题目
    loadCompletedProblems() // 重新加载完成状态
  }
}

onMounted(() => {
  const user = localStorage.getItem('student-user') || localStorage.getItem('admin-user')
  if (!user) {
    error.value = '请先登录'
    return
  }
  currentDate.value = getCurrentDate() // 初始化时使用正确的日期
  loadDailyProblems()
  // 每30秒检查一次日期
  dateCheckInterval = setInterval(checkAndUpdateDate, 30000)
})

onUnmounted(() => {
  if (dateCheckInterval) {
    clearInterval(dateCheckInterval)
  }
})
</script>

<template>
  <div v-if="error" class="error-message">
    {{ error }}
  </div>
  <div v-else-if="loading" class="loading-message">
    正在加载...
  </div>
  <div v-else>
    <div class="daily-challenge-container">
      <!-- 顶部信息卡片 -->
      <el-card class="info-card animate__animated animate__fadeIn" 
        :class="{ 
          'all-completed': isAllCompleted,
          'animate__pulse animate__infinite': isAllCompleted
        }">
        <template #header>
          <div class="card-header">
            <h2 class="title">每日挑战</h2>
            <span class="date">{{ formatDate(currentDate) }}</span>
          </div>
        </template>
        
        <!-- 进度条 -->
        <div class="progress-section">
          <el-progress 
            :percentage="progress" 
            :format="(percentage) => `${completedProblems.length}/${dailyProblems.length}`"
            :stroke-width="20"
            :show-text="true"
            :status="isAllCompleted ? 'success' : ''"
            class="progress-bar"
          />
          <div class="progress-text">
            已完成 {{ completedProblems.length }} 题，还剩 {{ dailyProblems.length - completedProblems.length }} 题
            <span v-if="isAllCompleted" class="completion-message">🎉 太棒了！你已完成今日所有挑战！</span>
          </div>
        </div>
      </el-card>

      <!-- 题目列表 -->
      <div class="problems-grid">
        <el-card 
          v-for="(problem, index) in dailyProblems" 
          :key="problem.id"
          class="problem-card animate__animated animate__fadeIn"
          :style="{ animationDelay: `${index * 0.1}s` }"
          :class="{ 
            'completed': isProblemCompleted(problem.id),
            'animate__heartBeat animate__infinite': isAllCompleted && isProblemCompleted(problem.id)
          }"
          @click="goToProblem(problem.id)"
        >
          <div class="problem-content">
            <div class="problem-number">{{ index + 1 }}</div>
            <div class="problem-info">
              <h3 class="problem-title">{{ problem.name }}</h3>
              <div class="problem-meta">
                <el-tag size="small" :type="isProblemCompleted(problem.id) ? 'success' : 'info'">
                  {{ isProblemCompleted(problem.id) ? '已完成' : '未完成' }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.daily-challenge-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  min-height: 100vh;
  background-color: #f5f7fa;
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(216, 241, 230, 0.46) 0%, rgba(233, 226, 226, 0.28) 90.2%),
    radial-gradient(circle at 90% 80%, rgba(216, 241, 230, 0.46) 0%, rgba(233, 226, 226, 0.28) 90.2%);
}

.info-card {
  margin-bottom: 30px;
  position: relative;
  z-index: 2;
  transition: all 0.3s ease;
  background: var(--color-background);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.info-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.5s ease;
}

.info-card:hover::before {
  transform: scaleX(1);
}

.info-card.all-completed {
  border: 2px solid #67C23A;
  box-shadow: 0 0 20px rgba(103, 194, 58, 0.3);
  animation: completedPulse 2s ease-in-out infinite;
}

@keyframes completedPulse {
  0% { box-shadow: 0 0 20px rgba(103, 194, 58, 0.3); }
  50% { box-shadow: 0 0 30px rgba(103, 194, 58, 0.5); }
  100% { box-shadow: 0 0 20px rgba(103, 194, 58, 0.3); }
}

.problem-card {
  cursor: pointer;
  transition: all 0.3s ease;
  background: var(--color-background);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
  border: 2px solid transparent;
}

.problem-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, #409EFF, #36D1DC);
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.problem-card:hover::before {
  opacity: 0.9;
}

.problem-card.completed {
  border: 2px solid #67C23A;
  background: rgba(103, 194, 58, 0.05);
  animation: completedFloat 3s ease-in-out infinite;
}

.problem-card.completed::before {
  background: linear-gradient(45deg, #67C23A, #95D475);
}

.problem-card:hover::after {
  transform: translateX(100%);
}

.problem-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: var(--bg-color-soft);
  border-radius: 12px 12px 0 0;
  position: relative;
  overflow: hidden;
}

.card-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #409EFF, transparent);
  animation: headerLine 3s linear infinite;
}

@keyframes headerLine {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.title {
  margin: 0;
  font-size: 28px;
  color: #2c3e50;
  font-weight: 700;
  position: relative;
}

.title::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, #409EFF, transparent);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.title:hover::after {
  transform: scaleX(1);
}

.date {
  font-size: 18px;
  color: #6c757d;
  font-weight: 500;
  padding: 8px 16px;
  background: var(--color-background);
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.date:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.progress-section {
  padding: 25px 0;
}

.progress-bar {
  margin-bottom: 15px;
  transition: all 0.3s ease;
}

.progress-bar:hover {
  transform: scale(1.02);
}

.progress-text {
  text-align: center;
  color: #495057;
  font-size: 16px;
  font-weight: 500;
  position: relative;
}

.completion-message {
  display: block;
  margin-top: 10px;
  color: #67C23A;
  font-weight: bold;
  animation: textGlow 2s ease-in-out infinite;
}

@keyframes textGlow {
  0% { text-shadow: 0 0 10px rgba(103, 194, 58, 0.3); }
  50% { text-shadow: 0 0 20px rgba(103, 194, 58, 0.6); }
  100% { text-shadow: 0 0 10px rgba(103, 194, 58, 0.3); }
}

.problems-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
  padding: 10px;
}

.problem-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.problem-number {
  width: 45px;
  height: 45px;
  background: #409EFF;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  margin-right: 20px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  transition: all 0.3s ease;
}

.problem-card:hover .problem-number {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
}

.problem-info {
  flex: 1;
}

.problem-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #2c3e50;
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
  display: inline-block;
}

.problem-card:hover .problem-title {
  color: #ffffff;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

.problem-card.completed:hover .problem-title {
  color: #ffffff;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.problem-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #ffffff, transparent);
  transition: width 0.3s ease;
}

.problem-card:hover .problem-title::after {
  width: 100%;
}

.problem-card.completed:hover .problem-title::after {
  background: linear-gradient(90deg, #ffffff, transparent);
}

.problem-meta {
  display: flex;
  gap: 12px;
}

/* 动画类 */
.animate__animated {
  animation-duration: 0.6s;
}

.animate__fadeIn {
  animation-name: fadeIn;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 加载动画 */
.loading-message {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
  font-size: 18px;
  color: #409EFF;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 1; }
  100% { opacity: 0.6; }
}

/* 错误消息样式 */
.error-message {
  text-align: center;
  padding: 20px;
  color: #f56c6c;
  font-size: 18px;
  animation: shake 0.5s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-10px); }
  75% { transform: translateX(10px); }
}
</style>
