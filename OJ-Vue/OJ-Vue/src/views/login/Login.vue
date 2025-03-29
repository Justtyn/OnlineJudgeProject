<script setup>
import TypingEffect from "@/views/tool/TypingEffect-Login.vue"
import {reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import request from "@/utils/request.js";
import router from "@/router/index.js";

// 记住登录信息
const saveUserLoginInfo = ref(true)

// 表单验证
const formRef = ref()

// 用户数据
const data = reactive({
  form: {
    username: '',
    password: '',
    role: 'STUDENT'
  }
})
// 验证规则
const rules = {
  username: [
    {required: true, message: '请输入账号', trigger: 'blur'},
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
  ],
}

// 登陆方法
const login = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    const res = await request.post('/login', data.form)
    if (res.code === '200') {
      localStorage.setItem('student-user', JSON.stringify(res.data))
      ElMessage.success('登录成功')
      await router.push({ path: '/homePage' })
      console.log('路由跳转完成')
    } else {
      ElMessage.error(res.msg || '登录失败')
    }
  } catch (error) {
    console.error('登录出错:', error)
    ElMessage.error('登录过程中发生错误')
  }
}
</script>

<template>
  <!-- 水平垂直居中、浮动布局 -->
  <div class="login-container">
    <div class="welcome-text">
      <TypingEffect/>
    </div>
    <div class="login-box">
      <div class="login-text">Login</div>
      <el-divider/>
      <!-- 登录表单 -->
      <el-form class="login-form" :model="data.form" ref="formRef" :rules="rules">
        <!-- 账号表单 -->
        <el-form-item prop="username">
          <el-input prefix-icon="User" v-model="data.form.username" placeholder="请输入账号"></el-input>
        </el-form-item>
        <!-- 密码表单 -->
        <el-form-item prop="password">
          <el-input show-password prefix-icon="Lock" v-model="data.form.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <!-- 角色选择框-->
        <el-form-item prop="role">
          <el-select style="width: 100%" v-model="data.form.role">
            <el-option value="STUDENT" label="学生"></el-option>
            <el-option value="TEACHER" label="教师"></el-option>
            <el-option value="ADMIN" label="管理员"></el-option>
          </el-select>
        </el-form-item>
<!--        <el-checkbox v-model="saveUserLoginInfo" label="始终保持登录状态" size="large"/>-->
        <!-- 登陆按钮-->
        <el-form-item>
          <el-button type="danger" style="width: 100%" @click="login">登 陆</el-button>
        </el-form-item>
      </el-form>
      <div class="register-text">
        还没有账号？请<a href="/register">注册</a>
      </div>
    </div>
  </div>

</template>

<style scoped>
html, body {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden; /* 避免滚动条影响布局 */
}

.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-flow: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
}

@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.login-box {
  border-radius: 25px;
  background-color: rgba(255, 255, 255, 0.85);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: min(600px, 90%);
  margin-bottom: 200px;
}

.login-text {
  color: grey;
  font-weight: bold;
  font-size: 30px;
  text-align: center;
  margin-bottom: 30px;
}

.login-form {
  padding: 0 50px;
}

.welcome-text {
  height: 100px;
  margin-bottom: 30px;
}

.register-text {
  margin-top: 30px;
  text-align: right;
  font-size: 15px;
  color: black;
}

</style>