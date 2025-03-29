<script setup>
// 导入打字效果组件，用于登录页面的动态文字展示
import TypingEffect from "@/views/tool/TypingEffect-Login.vue"
// 导入 Vue 的响应式 API
import {reactive, ref} from "vue";
// 导入 Element Plus 的消息提示组件
import {ElMessage} from "element-plus";
// 导入封装好的请求工具
import request from "@/utils/request.js";
// 导入路由实例，用于页面跳转
import router from "@/router/index.js";

// 记住登录信息的开关（虽然在界面中被注释掉了，但变量保留）
const saveUserLoginInfo = ref(true)

// 表单引用，用于表单验证和获取表单实例
const formRef = ref()

// 用户数据对象，使用 reactive 使其具有响应性
const data = reactive({
  form: {
    username: '', // 用户名输入
    password: '', // 密码输入
    role: 'STUDENT' // 默认角色为学生
  }
})

// 表单验证规则定义
const rules = {
  username: [
    {required: true, message: '请输入账号', trigger: 'blur'}, // 用户名为必填项
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'}, // 密码为必填项
  ],
}

// 登录方法，使用 async/await 处理异步操作
const login = async () => {
  try {
    // 表单验证，确保所有必填项都已填写
    const valid = await formRef.value.validate()
    if (!valid) return // 如果验证失败，直接返回不继续执行
    
    // 发送登录请求到后端
    const res = await request.post('/login', data.form)
    console.log(res) // 打印响应结果，用于调试
    if (res.data.code === '200') {  // 检查响应状态码是否为成功
      // 将用户信息和 token 存储到本地存储中，用于保持登录状态
      localStorage.setItem('student-user', JSON.stringify(res.data.data))
      // 显示登录成功消息
      ElMessage.success(res.data.msg || '登录成功')
      try {
        // 登录成功后跳转到首页
        await router.push('/')  // 先跳转到根路由，让它自动重定向到 homePage
        console.log('路由跳转完成')
      } catch (err) {
        // 处理路由跳转失败的情况
        console.error('路由跳转失败:', err)
        ElMessage.error('页面跳转失败，请刷新重试')
      }
    } else {
      // 显示登录失败消息
      ElMessage.error(res.data.msg || '登录失败')
    }
  } catch (error) {
    // 处理登录过程中的其他错误
    console.error('登录出错:', error)
    ElMessage.error('登录过程中发生错误')
  }
}
</script>

<template>
  <!-- 登录页面容器，水平垂直居中、浮动布局 -->
  <div class="login-container">
    <!-- 欢迎文字区域，使用打字效果组件 -->
    <div class="welcome-text">
      <TypingEffect/>
    </div>
    <!-- 登录框主体 -->
    <div class="login-box">
      <!-- 登录标题 -->
      <div class="login-text">Login</div>
      <!-- 分隔线 -->
      <el-divider/>
      <!-- 登录表单，绑定数据模型、表单引用和验证规则 -->
      <el-form class="login-form" :model="data.form" ref="formRef" :rules="rules">
        <!-- 账号输入框，带验证 -->
        <el-form-item prop="username">
          <el-input prefix-icon="User" v-model="data.form.username" placeholder="请输入账号"></el-input>
        </el-form-item>
        <!-- 密码输入框，带验证和密码隐藏功能 -->
        <el-form-item prop="password">
          <el-input show-password prefix-icon="Lock" v-model="data.form.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <!-- 角色选择下拉框 -->
        <el-form-item prop="role">
          <el-select style="width: 100%" v-model="data.form.role">
            <el-option value="STUDENT" label="学生"></el-option>
            <el-option value="TEACHER" label="教师"></el-option>
            <el-option value="ADMIN" label="管理员"></el-option>
          </el-select>
        </el-form-item>
        <!-- 记住登录状态复选框（已被注释掉，暂未启用） -->
<!--        <el-checkbox v-model="saveUserLoginInfo" label="始终保持登录状态" size="large"/>-->
        <!-- 登录按钮 -->
        <el-form-item>
          <el-button type="danger" style="width: 100%" @click="login">登 陆</el-button>
        </el-form-item>
      </el-form>
      <!-- 注册提示文字 -->
      <div class="register-text">
        还没有账号？请<router-link to="/register" class="register-link">注册</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 重置 HTML 和 body 样式，确保登录页面占满整个视口 */
html, body {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden; /* 避免滚动条影响布局 */
}

/* 登录容器样式，占满整个视口并居中内容 */
.login-container {
  height: 100vh; /* 视口高度 */
  width: 100vw; /* 视口宽度 */
  display: flex; /* 弹性布局 */
  flex-flow: column; /* 纵向排列 */
  align-items: center; /* 水平居中 */
  justify-content: center; /* 垂直居中 */
  /* 渐变背景色 */
  background: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);
  background-size: 400% 400%;
  /* 背景动画 */
  animation: gradientBG 15s ease infinite;
}

/* 背景渐变动画关键帧 */
@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 登录框样式 */
.login-box {
  border-radius: 25px; /* 圆角边框 */
  background-color: rgba(255, 255, 255, 0.85); /* 半透明白色背景 */
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1); /* 阴影效果 */
  padding: 40px;
  width: min(600px, 90%); /* 响应式宽度，最大600px或90%视口宽度 */
  margin-bottom: 200px; /* 底部外边距 */
}

/* 登录标题文字样式 */
.login-text {
  color: grey;
  font-weight: bold;
  font-size: 30px;
  text-align: center;
  margin-bottom: 30px;
}

/* 登录表单内边距 */
.login-form {
  padding: 0 50px;
}

/* 欢迎文字区域样式 */
.welcome-text {
  height: 100px;
  margin-bottom: 30px;
}

/* 注册提示文字样式 */
.register-text {
  margin-top: 30px;
  text-align: right;
  font-size: 15px;
  color: black;
}

.register-link {
  color: #1890ff;
  text-decoration: none;
}
.register-link:hover {
  text-decoration: underline;
}
</style>