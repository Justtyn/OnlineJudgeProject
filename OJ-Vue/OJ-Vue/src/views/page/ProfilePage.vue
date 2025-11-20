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

    <!-- 下方标签页（信息、背景、设置） -->
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

        <!-- 背景标签页 -->
        <el-tab-pane label="背景" name="background">
          <div class="background-container">
            <!-- 两行两列展示默认背景，可自行调整行列数 -->
            <div class="bg-grid">
              <div v-for="(img, index) in defaultBackgrounds" :key="index" class="bg-item"
                @click="selectBackground(img)">
                <img :src="img" alt="背景图" />
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 设置标签页 -->
        <el-tab-pane label="设置" name="settings">
          <div class="settings-container">
            <el-button type="primary" @click="openEditDialog">修改信息</el-button>
            <el-button type="primary" @click="showChangePassword">修改密码</el-button>
          </div>
          <change-password ref="changePasswordRef" />
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 修改信息弹窗 -->
    <el-dialog v-model="editDialogVisible" title="修改个人信息" width="500px">
      <div class="edit-dialog-content">
        <el-form :model="editForm" ref="editFormRef" label-width="80px">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="editForm.name" />
          </el-form-item>
          <el-form-item label="性别" prop="sex">
            <el-select v-model="editForm.sex">
              <el-option label="男" value="男"></el-option>
              <el-option label="女" value="女"></el-option>
              <el-option label="沃尔玛购物袋" value="沃尔玛购物袋"></el-option>
              <el-option label="武装直升机" value="武装直升机"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="出生日期" prop="birth">
            <el-date-picker v-model="editForm.birth" type="date" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input v-model="editForm.phone" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="editForm.email" />
          </el-form-item>
          <el-form-item label="学校" prop="school">
            <el-input v-model="editForm.school" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="editDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitEdit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 头像预览弹窗 -->
    <el-dialog v-model="avatarPreviewVisible" width="400px" center class="avatar-preview-dialog">
      <div class="avatar-preview-wrapper">
        <img :src="studentInfo.avatar || defaultAvatar" class="avatar-preview-img" alt="头像预览" />
      </div>
      <!-- 修改头像上传按钮 -->
      <div style="text-align: center; margin-top: 20px;">
        <el-upload
            class="avatar-uploader"
            action="/api/student/uploadAvatar"
            name="file"
            :data="{ id: studentInfo.id }"
            :headers="{ Authorization: 'Bearer ' + token }"
            :on-success="handleAvatarUploadSuccess"
            :show-file-list="false"
            accept="image/*"
        >
          <el-button type="primary" size="mini">修改头像</el-button>
        </el-upload>
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
import ChangePassword from '@/components/ChangePassword.vue'
import 'animate.css';
import particles from 'particles.js';
import { getUploadUrl } from '@/utils/env.js';

// 从 localStorage 中获取登录信息
const localUser = localStorage.getItem('student-user') 
  ? JSON.parse(localStorage.getItem('student-user'))
  : localStorage.getItem('admin-user')
    ? JSON.parse(localStorage.getItem('admin-user'))
    : null;

const token = localUser ? localUser.token : '';
const studentId = localUser ? localUser.id : null;
const role = localUser ? localUser.role : null;

if (!localUser) {
  ElMessage.error('未登录或用户信息不存在');
  router.push('/login');
}

// 默认头像和背景图（请替换为实际地址）
const defaultAvatar = getUploadUrl('1743236403200_IMG_0748.JPG');
const defaultBackground = getUploadUrl('ocean-8032698_1920.jpg');
const defaultBackgrounds = [
  'ocean-8032698_1920.jpg',
  'pattern-8032716_1920.jpg',
  'winter-landscape-7891462_1920.jpg',
  'wood-pallets-3614890_1280.jpg',
  'bokeh-2072271_1920.jpg',
  'clouds-4215608_1920.jpg',
  'curtain-9060879_1920.jpg',
].map((fileName) => getUploadUrl(fileName));

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
  console.log('开始获取用户信息，角色:', role);
  console.log('本地用户信息:', localUser);
  
  // 只有当用户是管理员且用户名为admin时才使用本地存储信息
  if (role === 'ADMIN' && localUser.username === 'admin') {
    console.log('超级管理员用户，使用本地存储信息');
    studentInfo.id = localUser.id;
    studentInfo.username = localUser.username || '';
    studentInfo.name = localUser.name || '';
    studentInfo.sex = localUser.sex || '';
    studentInfo.birth = localUser.birth || '';
    studentInfo.phone = localUser.phone || '';
    studentInfo.email = localUser.email || '';
    studentInfo.avatar = localUser.avatar || '';
    studentInfo.background = localUser.background || defaultBackground;
    studentInfo.school = localUser.school || '';
    studentInfo.createTime = localUser.createTime || '';
    studentInfo.lastVisitTime = localUser.lastVisitTime || '';
    studentInfo.dailyChallenge = localUser.dailyChallenge || '';
    return;
  }

  // 其他用户（包括普通管理员和学生）都从API获取信息
  try {
    console.log('开始请求API获取用户信息');
    const res = await request.get(`/api/student/${studentId}`, {
      headers: { Authorization: 'Bearer ' + token }
    });
    console.log('API响应:', res);
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
      console.log('成功更新用户信息:', studentInfo);
    } else {
      console.error('API返回错误:', res.data.msg);
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

// 修改信息弹窗
const editDialogVisible = ref(false);
const editForm = reactive({
  name: '',
  sex: '',
  birth: '',
  phone: '',
  email: '',
  background: defaultBackground,
  school: ''
});
const editFormRef = ref();

const openEditDialog = () => {
  // 初始化表单数据为当前学生信息
  editForm.name = studentInfo.name;
  editForm.sex = studentInfo.sex;
  editForm.birth = studentInfo.birth;
  editForm.phone = studentInfo.phone;
  editForm.email = studentInfo.email;
  editForm.background = studentInfo.background || defaultBackground;
  editForm.school = studentInfo.school;
  editDialogVisible.value = true;
};

// 提交修改信息
const submitEdit = async () => {
  const payload = {
    id: studentInfo.id,
    username: studentInfo.username,  // 保持原用户名，避免更新为 null
    avatar: studentInfo.avatar,
    name: editForm.name,
    sex: editForm.sex,
    birth: editForm.birth,
    phone: editForm.phone,
    email: editForm.email,
    background: editForm.background,
    school: editForm.school
  };
  try {
    const res = await request.put(`/api/student/update`, payload, {
      headers: { Authorization: 'Bearer ' + token }
    });
    if (res.data.code === "200") {
      ElMessage.success(res.data.msg || '更新成功');
      Object.assign(studentInfo, payload);
      editDialogVisible.value = false;
    } else {
      ElMessage.error(res.data.msg || '更新失败');
    }
  } catch (error) {
    console.error('更新用户信息出错:', error);
    ElMessage.error('更新用户信息出错');
  }
};

// 切换背景并持久化到后端
const selectBackground = async (img: string) => {
  try {
    const payload = {
      id: studentInfo.id,
      background: img
    };
    const res = await request.post(`/api/student/updateBackground`, payload, {
      headers: { Authorization: 'Bearer ' + token }
    });
    if (res.data.code === '200') {
      studentInfo.background = img;
      ElMessage.success('背景已切换并保存');
    } else {
      ElMessage.error('更新背景失败');
    }
  } catch (error) {
    console.error('更新背景出错:', error);
    ElMessage.error('更新背景出错');
  }
};

// 头像预览弹窗
const avatarPreviewVisible = ref(false);

// 上传头像成功回调
const handleAvatarUploadSuccess = (response: any) => {
  console.log('上传头像响应:', response)
  const payload = response?.data ?? response
  const isSuccess = String(payload?.code || '') === '200'

  if (isSuccess) {
    const avatarUrl = payload?.data?.url || payload?.data || ''
    if (avatarUrl) {
      studentInfo.avatar = avatarUrl
    }
    ElMessage.success(payload?.msg || '头像上传成功')
    fetchStudentInfo()
    return
  }

  ElMessage.error(payload?.msg || '头像上传失败')
}

/**
 * 避免出现 [Vue warn]: Invalid prop: custom validator check failed for prop "size".
 * 我们不直接使用 size="100" ，改用 size="large" 并在 CSS 中自定义宽高
 */
const avatarSize = ref('large');

const changePasswordRef = ref(null)

const showChangePassword = () => {
  changePasswordRef.value.dialogVisible = true
}
</script>

<style scoped>
.profile-page {
  max-width: 1100px;
  margin: 0 auto;
  background-color: #fff;
  height: 80vh;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

/* 顶部背景图 */
.profile-header {
  position: relative;
  height: 360px; /* 从 260px 改为 360px */
  background-size: 100% auto; /* 宽度铺满，自动计算高度 */
  background-repeat: no-repeat;
  background-position: center;
}

/* 头像+用户名 */
.avatar-section {
  display: flex;
  align-items: center;
  position: absolute;
  width: 360px;
  top: 50%; /* 使用 top: 50% 实现垂直居中 */
  transform: translateY(-50%); /* 配合 top: 50% 实现精确垂直居中 */
  left: 40px;
  z-index: 10;
}

/* 自定义头像尺寸 */
.profile-avatar {
  width: 120px !important;
  height: 120px !important;
  border-radius: 8px;
  border: 3px solid #fff;
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
  color: #333;
}

/* 主体下方 Tab */
.profile-tabs {
  margin-top: 0; /* 删除顶部间距 */
  padding: 20px 40px;
  position: relative;
  z-index: 1;
}

/* 信息栏 */
.info-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px; /* 从 20px 改为 15px，减少卡片之间的间距 */
  padding: 15px; /* 从 20px 改为 15px，减少内边距 */
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 0;
  padding: 10px; /* 从 12px 改为 10px，减少内边距 */
  background-color: #fff;
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
  color: #666;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #333;
  flex: 1;
  word-break: break-word;
}

/* 背景栏 */
.background-container {
  margin-top: 10px;
}

.bg-grid {
  margin: 0 10px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  max-width: 100%;
}

.bg-item img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  border: 2px solid #eee;
  transition: all 0.3s ease;
}

.bg-item img:hover {
  transform: scale(1.02);
  border-color: #1890ff;
}

/* 设置栏 */
.settings-container {
  margin-top: 10px;
}

/* 修改信息弹窗 */
.edit-dialog-content {
  margin-top: 10px;
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
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
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

.golden-badge {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: #fff;
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

@keyframes headerShine {
  0%, 100% {
    background-position: 0% 0%;
  }
  50% {
    background-position: 100% 100%;
  }
}

@keyframes badgeGlow {
  0%, 100% {
    box-shadow: 
      0 2px 8px rgba(255, 215, 0, 0.3),
      0 0 20px rgba(255, 215, 0, 0.2);
  }
  50% {
    box-shadow: 
      0 2px 12px rgba(255, 215, 0, 0.4),
      0 0 30px rgba(255, 215, 0, 0.3);
  }
}

@keyframes badgeShine {
  0% {
    transform: translateX(-100%) translateY(-100%) rotate(45deg);
  }
  100% {
    transform: translateX(100%) translateY(100%) rotate(45deg);
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
</style>
