<template>
  <!-- 根容器，设置100%高度以填充父元素 -->
  <div style="height: 100%">
    <div v-if="loading" class="loading-container">
      <el-loading />
    </div>
    <div v-else class="card">
      <!-- 引入打字效果组件 -->
      <TypingEffect/>
      <!-- 添加 Learn More 按钮 -->
      <div class="button-container">
        <el-button 
          class="learn-more-btn"
          size="large" 
          @click="handleNavigation('/problemListPage')"
        >
          Learn More
        </el-button>
        <el-button 
          class="learn-more-btn"
          size="large" 
          @click="handleNavigation('/dailyChallenge')"
        >
          每日挑战
        </el-button>
        <el-button 
          class="learn-more-btn ai-assistant-btn"
          size="large" 
          @click="handleNavigation('/aiAssistant')"
        >
          <el-icon><ChatDotRound /></el-icon>
          AI 编程助手
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onErrorCaptured, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)

// 检查用户角色并自动跳转
const checkUserRole = () => {
  const userStr = localStorage.getItem('student-user')
  if (userStr) {
    const user = JSON.parse(userStr)
    // 只有教师需要自动跳转到管理后台，管理员可以正常访问主页
    if (user.role === 'TEACHER') {
      router.push('/admin/dashboard')
      return
    }
  }
}

const handleNavigation = (path) => {
  loading.value = true
  router.push(path).finally(() => {
    loading.value = false
  })
}

// 导入打字效果组件
import TypingEffect from "@/views/tool/TypingEffect-Main.vue";
// 导入图标
import { ChatDotRound } from '@element-plus/icons-vue';

onErrorCaptured((err) => {
  console.error('路由跳转错误:', err)
  // 可以在这里添加错误提示
  return false
})

// 组件挂载时检查用户角色
onMounted(() => {
  checkUserRole()
})
</script>

<style scoped>
/* 卡片样式定义 */
.card {
  /* 设置行高，确保文本垂直间距合适 */
  line-height: 30px;
  /* 使用 flex 布局使内容居中 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  /* 设置卡片高度为100% */
  height: 100%;
  
  /* 卡片外观美化 */
  /* 添加细边框，使用半透明黑色 */
  border: 1px solid rgba(0, 0, 0, 0.1);
  /* 添加圆角效果 */
  border-radius: 8px;
  /* 添加阴影效果 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  /* 动画效果 */
  /* 页面加载时的淡入动画 */
  animation: fadeIn 1s ease;
  /* 阴影过渡效果 */
  transition: box-shadow 0.3s ease;
}

/* 按钮容器样式 */
.button-container {
  margin-top: 50px;  /* 增加上边距 */
  display: flex;
  flex-direction: row;  /* 改为水平排列 */
  justify-content: center;  /* 水平居中 */
  align-items: center;  /* 垂直居中 */
  gap: 20px;  /* 按钮之间的间距 */
  width: 100%;  /* 占满父容器宽度 */
  max-width: 800px;  /* 增加最大宽度以适应三个按钮 */
  flex-wrap: wrap;  /* 允许换行 */
}

/* Learn More 按钮样式 */
.learn-more-btn {
  width: 200px;  /* 固定宽度 */
  font-size: 18px;
  padding: 15px 40px;
  border-radius: 12px;  /* 调整圆角 */
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #4a90e2 0%, #357abd 100%);  /* 渐变背景 */
  border: none;
  color: white;
  font-weight: 500;
  letter-spacing: 0.5px;
  position: relative;
  overflow: hidden;
  text-align: center;  /* 文字居中 */
}

.learn-more-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.2),
    transparent
  );
  transition: 0.5s;
}

.learn-more-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(74, 144, 226, 0.3);
  background: linear-gradient(135deg, #357abd 0%, #2c6aa0 100%);
}

.learn-more-btn:hover::before {
  left: 100%;
}

.learn-more-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(74, 144, 226, 0.3);
}

/* AI 助手按钮特殊样式 */
.ai-assistant-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  border: none;
  color: white;
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
}

.ai-assistant-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  transition: 0.5s;
}

.ai-assistant-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(255, 107, 107, 0.4);
  background: linear-gradient(135deg, #ee5a24 0%, #d63031 100%);
}

.ai-assistant-btn:hover::before {
  left: 100%;
}

.ai-assistant-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.4);
}

/* 鼠标悬停时增强阴影效果 */
.card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 定义淡入动画关键帧 */
@keyframes fadeIn {
  /* 动画起始状态 */
  from {
    opacity: 0;          /* 完全透明 */
    transform: scale(0.95); /* 略微缩小 */
  }
  /* 动画结束状态 */
  to {
    opacity: 1;          /* 完全不透明 */
    transform: scale(1);   /* 原始大小 */
  }
}

/* 添加移动端适配样式 */
@media screen and (max-width: 768px) {
  .card {
    padding: 15px;
    line-height: 24px;
  }
  
  :deep(.typewriter-container) {
    font-size: 24px;
  }
  
  :deep(.cursor) {
    width: 5px;
    height: 20px;
    margin-left: 3px;
  }

  .button-container {
    width: 100%;
    max-width: 300px;
    flex-direction: column;
    gap: 15px;
  }
  
  .learn-more-btn {
    font-size: 16px;
    padding: 12px 30px;
    width: 100%;
  }
  
  .ai-assistant-btn {
    width: 100%;
  }
}
</style>
