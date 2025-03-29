<template>
    <div class="profile-page">
      <!-- 顶部背景图 -->
      <div
        class="profile-header"
        :style="{ backgroundImage: 'url(' + (studentInfo.backgroundImage || defaultBackground) + ')' }"
      >
        <div class="avatar-wrapper">
          <el-avatar
            size="100"
            :src="studentInfo.avatar || defaultAvatar"
            class="profile-avatar"
          />
          <!-- 如果没有头像，则显示上传按钮 -->
          <div v-if="!studentInfo.avatar" class="upload-avatar">
            <el-upload
              action="/api/student/uploadAvatar"
              :on-success="handleAvatarUploadSuccess"
              :headers="{ Authorization: 'Bearer ' + token }"
              :show-file-list="false"
            >
              <el-button size="mini">上传头像</el-button>
            </el-upload>
          </div>
        </div>
      </div>
    
      <!-- 个人信息展示 -->
      <div class="profile-info">
        <h2 v-if="studentInfo.name">{{ studentInfo.name }}</h2>
        <p v-if="studentInfo.username">用户名：{{ studentInfo.username }}</p>
        <p v-if="studentInfo.sex">性别：{{ studentInfo.sex }}</p>
        <p v-if="studentInfo.birth">出生日期：{{ studentInfo.birth }}</p>
        <p v-if="studentInfo.phone">电话：{{ studentInfo.phone }}</p>
        <p v-if="studentInfo.email">邮箱：{{ studentInfo.email }}</p>
      </div>
    
      <!-- 修改信息按钮 -->
      <div class="profile-actions">
        <el-button type="primary" @click="openEditDialog">修改信息</el-button>
      </div>
    
      <!-- 修改信息弹窗 -->
      <el-dialog title="修改个人信息" :visible.sync="editDialogVisible" width="500px">
        <div class="edit-dialog-content">
          <!-- 背景图预览及选择 -->
          <div class="edit-background">
            <div
              class="bg-preview"
              :style="{ backgroundImage: 'url(' + editForm.backgroundImage + ')' }"
            ></div>
            <div class="bg-options">
              <span
                v-for="(img, index) in defaultBackgrounds"
                :key="index"
                class="bg-option"
                :class="{ selected: editForm.backgroundImage === img }"
                @click="selectBackground(img)"
              >
                <img :src="img" alt="背景图" />
              </span>
            </div>
          </div>
          <!-- 个人信息表单 -->
          <el-form :model="editForm" ref="editFormRef" label-width="80px">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="editForm.name" />
            </el-form-item>
            <el-form-item label="性别" prop="sex">
              <el-select v-model="editForm.sex">
                <el-option label="男" value="男"></el-option>
                <el-option label="女" value="女"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="出生日期" prop="birth">
              <el-date-picker
                v-model="editForm.birth"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="电话" prop="phone">
              <el-input v-model="editForm.phone" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="editForm.email" />
            </el-form-item>
            <!-- 如果头像为空，则允许在弹窗中上传头像 -->
            <el-form-item label="头像" v-if="!studentInfo.avatar">
              <el-upload
                action="/api/student/uploadAvatar"
                :on-success="handleAvatarUploadSuccess"
                :headers="{ Authorization: 'Bearer ' + token }"
                :show-file-list="false"
              >
                <el-button size="mini">上传头像</el-button>
              </el-upload>
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitEdit">确 定</el-button>
        </template>
      </el-dialog>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import { ElMessage } from 'element-plus';
  import request from '@/utils/request.js';
  import router from '@/router/index.js';
  
  // 从 localStorage 中获取登录信息（包含 token 与学生 id）
  const localUser = localStorage.getItem('student-user')
    ? JSON.parse(localStorage.getItem('student-user'))
    : null;
  const token = localUser ? localUser.token : '';
  const studentId = localUser ? localUser.id : null;
  
  if (!studentId) {
    ElMessage.error('未登录或用户信息不存在');
    router.push('/login');
  }
  
  // 默认头像和背景图（请替换为实际地址）
  const defaultAvatar = 'https://example.com/default-avatar.png';
  const defaultBackground = 'https://example.com/default-background.jpg';
  const defaultBackgrounds = [
    'https://example.com/default-background.jpg',
    'https://example.com/background2.jpg',
    'https://example.com/background3.jpg'
  ];
  
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
    backgroundImage: ''
  });
  
  // 获取学生信息
  const fetchStudentInfo = async () => {
    try {
      const res = await request.get(`/api/student/${studentId}`, {
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
        // 若后台未返回背景图，则使用默认背景
        studentInfo.backgroundImage = data.backgroundImage || defaultBackground;
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
  
  // 编辑信息弹窗相关
  const editDialogVisible = ref(false);
  const editForm = reactive({
    name: '',
    sex: '',
    birth: '',
    phone: '',
    email: '',
    backgroundImage: defaultBackground
  });
  const editFormRef = ref();
  
  const openEditDialog = () => {
    // 初始化表单数据为当前学生信息
    editForm.name = studentInfo.name;
    editForm.sex = studentInfo.sex;
    editForm.birth = studentInfo.birth;
    editForm.phone = studentInfo.phone;
    editForm.email = studentInfo.email;
    editForm.backgroundImage = studentInfo.backgroundImage || defaultBackground;
    editDialogVisible.value = true;
  };
  
  const selectBackground = (img: string) => {
    editForm.backgroundImage = img;
  };
  
  const handleAvatarUploadSuccess = (res: any) => {
    if (res.data.code === '200') {
      // 假设接口返回数据中的 avatarUrl 为上传后的头像地址
      studentInfo.avatar = res.data.data.avatarUrl;
      ElMessage.success('头像上传成功');
    } else {
      ElMessage.error(res.data.msg || '头像上传失败');
    }
  };
  
  const submitEdit = async () => {
    const payload = {
      id: studentInfo.id,  // 更新时需要传入 id
      name: editForm.name,
      sex: editForm.sex,
      birth: editForm.birth,
      phone: editForm.phone,
      email: editForm.email,
      backgroundImage: editForm.backgroundImage
    };
    try {
      const res = await request.put(`/api/student/update`, payload, {
        headers: { Authorization: 'Bearer ' + token }
      });
      if (res.data.code === '200') {
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
  </script>
  
  <style scoped>
  .profile-page {
    max-width: 800px;
    margin: 20px auto;
    background-color: #fff;
    min-height: calc(100vh - 40px);
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
    overflow: hidden;
  }
  
  .profile-header {
    position: relative;
    height: 200px;
    background-size: cover;
    background-position: center;
  }
  
  .avatar-wrapper {
    position: absolute;
    bottom: -50px;
    left: 30px;
  }
  
  .profile-avatar {
    border: 4px solid #fff;
  }
  
  .upload-avatar {
    margin-top: 10px;
    text-align: center;
  }
  
  .profile-info {
    padding: 70px 30px 20px;
  }
  
  .profile-info h2 {
    margin: 0;
    font-size: 24px;
    color: #333;
  }
  
  .profile-info p {
    margin: 5px 0;
    color: #666;
  }
  
  .profile-actions {
    text-align: right;
    padding: 10px 30px 20px;
  }
  
  /* 修改信息弹窗内样式 */
  .edit-dialog-content {
    margin-top: 10px;
  }
  
  .edit-background {
    margin-bottom: 20px;
  }
  
  .bg-preview {
    width: 100%;
    height: 120px;
    background-size: cover;
    background-position: center;
    border-radius: 4px;
    margin-bottom: 10px;
  }
  
  .bg-options {
    display: flex;
    gap: 10px;
  }
  
  .bg-option {
    width: 60px;
    height: 40px;
    border: 2px solid transparent;
    border-radius: 4px;
    overflow: hidden;
    cursor: pointer;
  }
  
  .bg-option.selected {
    border-color: var(--primary-color, #1890ff);
  }
  
  .bg-option img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  </style>
  