<template>
  <div class="profile-page">
    <!-- 顶部背景图 -->
    <div class="profile-header"
      :style="{ backgroundImage: 'url(' + (studentInfo.background || defaultBackground) + ')' }">

      <!-- 头像+用户名 -->
      <div class="avatar-section">
        <!-- 头像，改为方形圆角，点击可预览 -->
        <el-avatar v-model="avatarSize" :src="studentInfo.avatar || defaultAvatar" class="profile-avatar"
          @click="avatarPreviewVisible = true" />
        <!-- 用户名在头像右侧 -->
        <div class="avatar-info">
          <h2 class="username">{{ studentInfo.name || studentInfo.username }}</h2>
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
const defaultAvatar = 'http://124.222.43.168:9090/uploads/1743236403200_IMG_0748.JPG';
const defaultBackground = 'http://124.222.43.168:9090/uploads/ocean-8032698_1920.jpg';

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
  lastVisitTime: ''
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
  background-color: #fff;
  height: 80vh;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  box-sizing: border-box;
}

/* 顶部背景图 */
.profile-header {
  position: relative;
  height: 260px;
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
  bottom: -60px;
  left: 40px;
  flex-wrap: wrap;
  gap: 20px;
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
  word-break: break-word;
}

/* 主体下方 Tab */
.profile-tabs {
  margin-top: 80px;
  padding: 20px 40px;
  width: 100%;
  box-sizing: border-box;
}

/* 信息栏 */
.info-container p {
  font-size: 15px;
  margin: 6px 0;
  color: #555;
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

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.info-label {
  width: 120px;
  font-size: 15px;
  color: #888;
}

.info-value {
  font-size: 15px;
  color: #555;
  word-break: break-word;
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
</style>
