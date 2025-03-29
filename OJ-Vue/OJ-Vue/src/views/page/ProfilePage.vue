<template>
    <div class="profile-page">
      <!-- 顶部背景图 -->
      <div
        class="profile-header"
        :style="{ backgroundImage: 'url(' + (studentInfo.backgroundImage || defaultBackground) + ')' }"
      >
        <!-- 头像+用户名 -->
        <div class="avatar-section">
          <!-- 头像，改为方形圆角，点击可预览 -->
          <el-avatar
            v-model="avatarSize"
            :src="studentInfo.avatar || defaultAvatar"
            class="profile-avatar"
            @click="avatarPreviewVisible = true"
          />
          <!-- 用户名在头像右侧 -->
          <div class="avatar-info">
            <h2 class="username">{{ studentInfo.name || studentInfo.username }}</h2>
          </div>
        </div>
      </div>
  
      <!-- 下方标签页（信息、背景、设置） -->
      <div class="profile-tabs">
        <el-tabs v-model="activeTab">
          <!-- 信息标签页 -->
          <el-tab-pane label="信息" name="info">
            <div class="info-container">
              <p v-if="studentInfo.username">用户名：{{ studentInfo.username }}</p>
              <p v-if="studentInfo.sex">性别：{{ studentInfo.sex }}</p>
              <p v-if="studentInfo.birth">出生日期：{{ studentInfo.birth }}</p>
              <p v-if="studentInfo.phone">电话：{{ studentInfo.phone }}</p>
              <p v-if="studentInfo.email">邮箱：{{ studentInfo.email }}</p>
            </div>
          </el-tab-pane>
  
          <!-- 背景标签页 -->
          <el-tab-pane label="背景" name="background">
            <div class="background-container">
              <!-- 两行两列展示默认背景，可自行调整行列数 -->
              <div class="bg-grid">
                <div
                  v-for="(img, index) in defaultBackgrounds"
                  :key="index"
                  class="bg-item"
                  @click="selectBackground(img)"
                >
                  <img :src="img" alt="背景图" />
                </div>
              </div>
            </div>
          </el-tab-pane>
  
          <!-- 设置标签页 -->
          <el-tab-pane label="设置" name="settings">
            <div class="settings-container">
              <el-button type="primary" @click="openEditDialog">修改信息</el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
  
      <!-- 修改信息弹窗 -->
      <el-dialog
        v-model="editDialogVisible"
        title="修改个人信息"
        width="500px"
      >
        <div class="edit-dialog-content">
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
          </el-form>
        </div>
        <template #footer>
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitEdit">确 定</el-button>
        </template>
      </el-dialog>
  
      <!-- 头像预览弹窗 -->
      <el-dialog
        v-model="avatarPreviewVisible"
        width="400px"
        center
        class="avatar-preview-dialog"
      >
        <div class="avatar-preview-wrapper">
          <img
            :src="studentInfo.avatar || defaultAvatar"
            class="avatar-preview-img"
            alt="头像预览"
          />
        </div>
        <!-- 修改头像上传按钮 -->
        <div style="text-align: center; margin-top: 20px;">
          <el-upload
            action="/api/student/uploadAvatar"
            :data="{ id: studentInfo.id }"
            :on-success="handleAvatarUploadSuccess"
            :headers="{ Authorization: 'Bearer ' + token }"
            :show-file-list="false"
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
  const defaultBackground = 'http://localhost:9090/uploads/ocean-8032698_1920.jpg';
  const defaultBackgrounds = [
    'http://localhost:9090/uploads/ocean-8032698_1920.jpg',
    'http://localhost:9090/uploads/pattern-8032716_1920.jpg',
    'http://localhost:9090/uploads/winter-landscape-7891462_1920.jpg',
    'http://localhost:9090/uploads/wood-pallets-3614890_1280.jpg',
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
  
  // Tabs
  const activeTab = ref('info'); // 默认选中“信息”标签
  
  // 修改信息弹窗
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
    backgroundImage: editForm.backgroundImage
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
        backgroundImage: img
      };
      const res = await request.put(`/api/student/update`, payload, {
        headers: { Authorization: 'Bearer ' + token }
      });
      if (res.data.code === '200') {
        studentInfo.backgroundImage = img;
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
  const handleAvatarUploadSuccess = (res: any) => {
  console.log("上传头像响应:", res);
  if (res.data || res.data.code === "200") {
    // 根据返回数据，将 data 字段（URL 字符串）赋值给 studentInfo.avatar
    studentInfo.avatar = res.data.data;
    ElMessage.success("头像上传成功");
    // 可选：重新调用 fetchStudentInfo() 以刷新所有信息
    fetchStudentInfo();
  } else {
    ElMessage.error(res.data.msg || "头像上传失败");
  }
};

  
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
  }
  
  /* 顶部背景图 */
  .profile-header {
    position: relative;
    height: 260px;
    background-size: cover;
    background-position: center;
  }
  
  /* 头像+用户名 */
  .avatar-section {
    display: flex;
    align-items: center;
    position: absolute;
    bottom: -60px;
    left: 40px;
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
    margin-top: 80px;
    padding: 20px 40px;
  }
  
  /* 信息栏 */
  .info-container p {
    font-size: 15px;
    margin: 6px 0;
    color: #555;
  }
  
  /* 背景栏 */
  .background-container {
    margin-top: 10px;
  }
  .bg-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    max-width: 500px;
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
  </style>
  