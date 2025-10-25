<template>
  <div class="profile-page" :class="{ 'golden-mode': studentInfo.dailyChallenge === 'TRUE' }">
    <!-- 顶部背景图 -->
    <div class="profile-header"
      :style="{ backgroundImage: 'url(' + (studentInfo.background || defaultBackground) + ')' }">
      <div id="particles-js" class="particles-container"></div>
      <!-- 头像+用户名 -->
      <div class="avatar-section">
        <!-- 头像，改为方形圆角，点击可预览 -->
        <div class="avatar-wrapper">
          <el-avatar v-model="avatarSize" :src="studentInfo.avatar || defaultAvatar" class="profile-avatar animate__animated animate__fadeIn"
            @click="avatarPreviewVisible = true" />
          <div v-if="studentInfo.dailyChallenge === 'TRUE'" class="crown animate__animated animate__bounceIn">
            👑
          </div>
        </div>
        <!-- 用户名在头像右侧 -->
        <div class="avatar-info">
          <h2 class="username animate__animated animate__fadeIn">{{ studentInfo.name || studentInfo.username }}</h2>
          <div v-if="studentInfo.dailyChallenge === 'TRUE'" class="golden-badge animate__animated animate__fadeInUp">
            完成每日挑战
          </div>
        </div>
      </div>
    </div>

    <!-- 下方标签页（信息） -->
    <div class="profile-tabs">
      <el-tabs v-model="activeTab">
        <!-- 信息标签页 -->
        <el-tab-pane label="信息" name="info">
          <div class="info-container">
            <div class="info-item">
              <span class="info-label">用户名：</span>
              <span class="info-value">{{ studentInfo.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">姓名：</span>
              <span class="info-value">{{ studentInfo.name }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">性别：</span>
              <span class="info-value">{{ studentInfo.sex }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">出生日期：</span>
              <span class="info-value">{{ formatDateTime(studentInfo.birth) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">电话：</span>
              <span class="info-value">{{ studentInfo.phone }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">邮箱：</span>
              <span class="info-value">{{ studentInfo.email }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">学校：</span>
              <span class="info-value">{{ studentInfo.school }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间：</span>
              <span class="info-value">{{ studentInfo.createTime }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">最后访问：</span>
              <span class="info-value">{{ studentInfo.lastVisitTime }}</span>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 头像预览弹窗 -->
    <el-dialog v-model="avatarPreviewVisible" width="400px" center class="avatar-preview-dialog">
      <div class="avatar-preview-wrapper">
        <img :src="studentInfo.avatar || defaultAvatar" class="avatar-preview-img" alt="头像预览" />
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="avatarPreviewVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request.js';
import router from '@/router/index.js';
import { useRoute } from 'vue-router';
import 'animate.css';
import particles from 'particles.js';

// 获取路由参数
const route = useRoute();
const userId = route.params.id;

// 从 localStorage 中获取登录信息
const localUser = localStorage.getItem('student-user') 
  ? JSON.parse(localStorage.getItem('student-user'))
  : localStorage.getItem('admin-user')
    ? JSON.parse(localStorage.getItem('admin-user'))
    : null;

const token = localUser ? localUser.token : '';

if (!localUser) {
  ElMessage.error('未登录或用户信息不存在');
  router.push('/login');
}

// 默认头像和背景图
const defaultAvatar = 'http://localhost:9090/uploads/1743236403200_IMG_0748.JPG';
const defaultBackground = 'http://localhost:9090/uploads/ocean-8032698_1920.jpg';

// 学生信息响应式对象
const studentInfo = reactive({
  id: '',
  username: '',
  name: '',
  sex: '',
  birth: '',
  phone: '',
  email: '',
  avatar: '',
  background: '',
  school: '',
  createTime: '',
  lastVisitTime: '',
  dailyChallenge: ''
});

const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-';
  try {
    if (dateTimeStr.includes('T')) {
      return dateTimeStr.split('T')[0];  // 取 T 前面的部分
    }
    return dateTimeStr;
  } catch (e) {
    console.error('时间格式化错误:', e);
    return '-';
  }
};

// 获取学生信息
const fetchStudentInfo = async () => {
  try {
    const res = await request.get(`/api/student/${userId}`, {
      headers: { Authorization: 'Bearer ' + token }
    });
    if (res.data.code === '200') {
      const data = res.data.data;
      studentInfo.id = data.id;
      studentInfo.username = data.username || '';
      studentInfo.name = data.name || '';
      studentInfo.sex = data.sex || '';
      studentInfo.birth = data.birth || '';
      studentInfo.phone = data.phone || '';
      studentInfo.email = data.email || '';
      studentInfo.avatar = data.avatar || '';
      studentInfo.background = data.background || defaultBackground;
      studentInfo.school = data.school || '';
      studentInfo.createTime = data.createTime || '';
      studentInfo.lastVisitTime = data.lastVisitTime || '';
      studentInfo.dailyChallenge = data.dailyChallenge || '';
    } else {
      ElMessage.error(res.data.msg || '获取用户信息失败');
    }
  } catch (error) {
    console.error('获取用户信息出错:', error);
    ElMessage.error('获取用户信息出错');
  }
};

onMounted(() => {
  fetchStudentInfo();
  // 初始化 particles.js
  if (studentInfo.dailyChallenge === 'TRUE') {
    particlesJS('particles-js', {
      particles: {
        number: {
          value: 80,
          density: {
            enable: true,
            value_area: 800
          }
        },
        color: {
          value: '#FFD700'
        },
        shape: {
          type: 'circle'
        },
        opacity: {
          value: 0.5,
          random: true
        },
        size: {
          value: 3,
          random: true
        },
        line_linked: {
          enable: true,
          distance: 150,
          color: '#FFD700',
          opacity: 0.4,
          width: 1
        },
        move: {
          enable: true,
          speed: 2,
          direction: 'none',
          random: true,
          straight: false,
          out_mode: 'out',
          bounce: false
        }
      },
      interactivity: {
        detect_on: 'canvas',
        events: {
          onhover: {
            enable: true,
            mode: 'grab'
          },
          onclick: {
            enable: true,
            mode: 'push'
          },
          resize: true
        }
      },
      retina_detect: true
    });
  }
});

// Tabs
const activeTab = ref('info'); // 默认选中"信息"标签

// 头像预览弹窗
const avatarPreviewVisible = ref(false);

/**
 * 避免出现 [Vue warn]: Invalid prop: custom validator check failed for prop "size".
 * 我们不直接使用 size="100" ，改用 size="large" 并在 CSS 中自定义宽高
 */
const avatarSize = ref('large');
</script>

<style scoped>
.profile-page {
  max-width: 1100px;
  margin: 0 auto;
  background-color: var(--color-background);
  height: 80vh;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  box-sizing: border-box;
}

/* 顶部背景图 */
.profile-header {
  position: relative;
  height: 360px;
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  width: 100%;
}

/* 头像+用户名 */
.avatar-section {
  display: flex;
  align-items: center;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  left: 40px;
  z-index: 10;
}

/* 自定义头像尺寸 */
.profile-avatar {
  width: 120px !important;
  height: 120px !important;
  border-radius: 8px;
  border: 3px solid var(--color-background);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  cursor: pointer;
}

.avatar-info {
  margin-left: 20px;
}

.username {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  color: var(--color-heading);
  word-break: break-word;
}

/* 主体下方 Tab */
.profile-tabs {
  margin-top: 0;
  padding: 20px 40px;
  position: relative;
  z-index: 1;
}

/* 信息栏 */
.info-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  padding: 15px;
  background-color: var(--bg-color-soft);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 0;
  padding: 10px;
  background-color: var(--color-background);
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.info-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.info-label {
  width: 100px;
  font-size: 14px;
  color: var(--color-text);
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: var(--color-heading);
  flex: 1;
  word-break: break-word;
}

/* 头像预览弹窗 */
.avatar-preview-dialog .avatar-preview-wrapper {
  text-align: center;
}

.avatar-preview-img {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  object-fit: cover;
  border: 2px solid var(--color-background);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .profile-page {
    height: auto;
    min-height: 80vh;
  }

  .profile-header {
    height: 200px;
  }

  .avatar-section {
    left: 20px;
    bottom: -40px;
  }

  .profile-avatar {
    width: 80px !important;
    height: 80px !important;
  }

  .username {
    font-size: 18px;
  }

  .profile-tabs {
    margin-top: 60px;
    padding: 15px;
  }

  .info-label {
    width: 100px;
    font-size: 14px;
  }

  .info-value {
    font-size: 14px;
  }

  .avatar-preview-img {
    width: 150px;
    height: 150px;
  }

  :deep(.el-tabs__nav) {
    width: 100%;
  }

  :deep(.el-tabs__item) {
    font-size: 14px;
  }
}

/* 金色传说模式样式 */
.golden-mode {
  box-shadow: 0 0 30px rgba(255, 215, 0, 0.3);
  position: relative;
  overflow: visible;
}

.particles-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.golden-mode .profile-header {
  position: relative;
  overflow: visible;
}

.golden-mode .profile-header::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M50 0L61 39H100L69 63L80 100L50 77L20 100L31 63L0 39H39L50 0Z' fill='%23FFD700' fill-opacity='0.1'/%3E%3C/svg%3E");
  background-size: 50px 50px;
  pointer-events: none;
  z-index: 2;
}

.avatar-section {
  position: relative;
  z-index: 3;
}

.avatar-wrapper {
  position: relative;
  z-index: 10;
}

.crown {
  position: absolute;
  top: -25px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 32px;
  filter: drop-shadow(0 0 10px rgba(255, 215, 0, 0.5));
  animation: crownFloat 3s ease-in-out infinite;
  z-index: 11;
}

.crown::before {
  content: '';
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.2) 0%, transparent 70%);
  border-radius: 50%;
  animation: crownGlow 2s ease-in-out infinite;
}

.golden-badge {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: var(--color-background);
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  margin-top: 8px;
  box-shadow: 
    0 2px 8px rgba(255, 215, 0, 0.3),
    0 0 20px rgba(255, 215, 0, 0.2);
  animation: badgeGlow 2s ease-in-out infinite;
  position: relative;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.golden-badge::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 0%,
    rgba(255, 255, 255, 0.1) 50%,
    transparent 100%
  );
  animation: badgeShine 3s linear infinite;
}

.golden-badge::after {
  content: '👑';
  font-size: 16px;
  animation: checkmarkPulse 1s ease-in-out infinite;
}

@keyframes checkmarkPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
}

@keyframes goldenShine {
  0%, 100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

@keyframes headerShine {
  0%, 100% {
    background-position: 0% 0%;
  }
  50% {
    background-position: 100% 100%;
  }
}

@keyframes crownFloat {
  0%, 100% {
    transform: translateX(-50%) translateY(0) rotate(0deg);
  }
  50% {
    transform: translateX(-50%) translateY(-8px) rotate(5deg);
  }
}

@keyframes crownGlow {
  0%, 100% {
    opacity: 0.5;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.2);
  }
}

.golden-mode .profile-avatar {
  border: 3px solid #FFD700;
  box-shadow: 
    0 0 15px rgba(255, 215, 0, 0.3),
    0 0 30px rgba(255, 215, 0, 0.2);
  animation: avatarGlow 3s ease-in-out infinite;
}

@keyframes avatarGlow {
  0%, 100% {
    box-shadow: 
      0 0 15px rgba(255, 215, 0, 0.3),
      0 0 30px rgba(255, 215, 0, 0.2);
  }
  50% {
    box-shadow: 
      0 0 20px rgba(255, 215, 0, 0.4),
      0 0 40px rgba(255, 215, 0, 0.3);
  }
}

.golden-mode .username {
  color: #B8860B;
  text-shadow: 
    0 1px 2px rgba(255, 215, 0, 0.2),
    0 2px 4px rgba(255, 215, 0, 0.1);
  animation: textGlow 3s ease-in-out infinite;
}

@keyframes textGlow {
  0%, 100% {
    text-shadow: 
      0 1px 2px rgba(255, 215, 0, 0.2),
      0 2px 4px rgba(255, 215, 0, 0.1);
  }
  50% {
    text-shadow: 
      0 1px 3px rgba(255, 215, 0, 0.3),
      0 3px 6px rgba(255, 215, 0, 0.2);
  }
}

.golden-mode .info-label {
  color: #B8860B;
  text-shadow: 0 1px 1px rgba(255, 215, 0, 0.1);
}

.golden-mode .info-value {
  color: #8B4513;
  text-shadow: 0 1px 1px rgba(255, 215, 0, 0.1);
}

.golden-mode .el-tabs__item.is-active {
  color: #B8860B;
}

.golden-mode .el-tabs__active-bar {
  background-color: #FFD700;
}

.golden-mode .el-tabs__item:hover {
  color: #FFD700;
}
</style>
