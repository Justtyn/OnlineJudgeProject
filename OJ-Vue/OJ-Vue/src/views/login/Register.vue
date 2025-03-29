<script setup>
// 导入打字效果组件，用于注册页面的欢迎文字动画
import TypingEffectRegister from "@/views/tool/TypingEffect-Register.vue";

// 导入必要的 Vue 组合式 API 函数
import {reactive, ref, watch} from 'vue'
// 导入 canvas-confetti 库，用于创建烟花/彩带效果
import confetti from 'canvas-confetti'
// 导入封装好的请求工具
import request from "@/utils/request.js";
// 导入 Element Plus 的消息提示组件
import {ElMessage} from "element-plus";
// 导入路由实例，用于页面跳转
import router from "@/router/index.js";

// 表单验证相关
const formRef = ref() // 表单引用，用于表单验证和获取表单实例
const active = ref(0) // 当前激活的步骤，初始为第一步（索引为0）

// 进度条下一步点击方法
const next = () => {
  // 验证表单数据
  formRef.value.validate((valid) => {
    if (valid) {
      // 如果验证通过，步骤索引加1，超过2则重置为0
      if (active.value++ > 2) active.value = 0
      console.log(active.value)
    }
  })
}

// 用户注册数据，使用 reactive 创建响应式对象
const userRegisterData = reactive({
  form: {
    role: 'STUDENT', // 默认角色为学生
    username: '',    // 用户名
    password: '',    // 密码
    name: '',        // 姓名
    sex: '',         // 性别
    email: '',       // 邮箱
    phone: ''        // 手机号
  }
})

// 监听 active 的变化，当 active 等于 3 时（完成所有步骤）触发烟花效果和注册请求
watch(active, (newVal) => {
  if (newVal === 3) {
    startFireworks(); // 触发烟花效果
    console.log(userRegisterData.form) // 打印用户注册数据
    register(); // 调用注册方法
  }
});

// 注册方法，向后端发送注册请求
const register = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      // 发送 POST 请求到注册接口
      request.post('/register', userRegisterData.form).then(res => {
        if (res.code === '200') {
          // 注册成功，将用户信息存储到本地存储
          localStorage.setItem('student-user', JSON.stringify(res.data))
          // 显示成功消息
          ElMessage.success('注册成功')
          console.log(res.data)
          // 跳转到登录页
          router.push('/login')
        } else {
          // 注册失败，显示错误消息
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

// 定义烟花效果函数，使用 canvas-confetti 库创建庆祝效果
function startFireworks() {
  const end = Date.now() + 15 * 1000; // 烟花效果持续15秒
  const colors = ['#bb0000', '#ffffff']; // 烟花颜色

  (function frame() {
    // 从左侧发射烟花
    confetti({
      particleCount: 2, // 粒子数量
      angle: 60,        // 发射角度
      spread: 55,       // 扩散范围
      origin: {x: 0},   // 起始位置（左侧）
      colors: colors    // 颜色
    });
    // 从右侧发射烟花
    confetti({
      particleCount: 2, // 粒子数量
      angle: 120,       // 发射角度
      spread: 55,       // 扩散范围
      origin: {x: 1},   // 起始位置（右侧）
      colors: colors    // 颜色
    });

    // 如果当前时间小于结束时间，继续动画
    if (Date.now() < end) {
      requestAnimationFrame(frame);
    }
  }());
}

// 表单验证规则
const rules = reactive({
  username: [
    {required: true, message: '请输入账号', trigger: 'blur'}, // 账号为必填项
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'}, // 密码为必填项
  ],
})

// 性别选择选项
const options = [
  {
    value: '男',
    label: '男',
  },
  {
    value: '女',
    label: '女',
  },
  {
    value: '武装直升机',
    label: '武装直升机',
  },
]
</script>

<template>
  <!-- 注册页面容器 -->
  <div class="register-container">
    <!-- 欢迎文字区域，使用打字效果组件 -->
    <div class="welcome-text">
      <TypingEffectRegister/>
    </div>

    <!-- 注册表单容器 -->
    <div class="register-box">
      <!-- 内容布局容器，使用 flex 布局居中显示 -->
      <div style="width: 100%;height: 100%;display: flex;flex-direction: column;justify-content: center;align-items: center">
        <!-- 注册标题 -->
        <div class="register-text">Register</div>
        <!-- 步骤条，显示当前注册进度 -->
        <el-steps style="max-width: 600px;width: 100%" :active="active" align-center>
          <el-step title="Step 1" description="角色选择"/>
          <el-step title="Step 2" description="账户信息"/>
          <el-step title="Step 3" description="个人信息"/>
        </el-steps>

        <!-- 注册表单，根据当前步骤显示不同内容 -->
        <el-form style="width: 30%;height: 45%;" class="register-form" :model="userRegisterData.form" ref="formRef" :rules="rules">
          <!-- Step 1: 角色选择 -->
          <template v-if="active === 0">
            <el-form-item prop="role">
              <el-select style="width: 100%" v-model="userRegisterData.form.role">
                <el-option value="STUDENT" label="学生"></el-option>
                <el-option value="TEACHER" label="教师"></el-option>
                <el-option value="ADMIN" label="管理员"></el-option>
              </el-select>
            </el-form-item>
          </template>

          <!-- Step 2: 账户信息 -->
          <template v-else-if="active === 1">
            <el-form-item prop="username">
              <el-input prefix-icon="User" v-model="userRegisterData.form.username" placeholder="请输入账号"></el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input show-password prefix-icon="Lock" v-model="userRegisterData.form.password" placeholder="请输入密码"></el-input>
            </el-form-item>
          </template>

          <!-- Step 3: 个人信息 -->
          <template v-else-if="active === 2">
            <el-form-item prop="name">
              <el-input prefix-icon="Message" v-model="userRegisterData.form.name" placeholder="请输入姓名"></el-input>
            </el-form-item>
            <el-form-item prop="sex">
              <el-select v-model="userRegisterData.form.sex" placeholder="请选择性别">
                <el-option
                    v-for="item in options"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item prop="email">
              <el-input prefix-icon="Message" v-model="userRegisterData.form.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>
            <el-form-item prop="phone">
              <el-input prefix-icon="Iphone" v-model="userRegisterData.form.phone" placeholder="请输入手机号"></el-input>
            </el-form-item>
          </template>

          <!-- Step 4: 大功告成，显示注册完成信息 -->
          <template v-else-if="active === 3">
            <div style="display: flex;justify-content: center;align-items: center;text-align: center;">
              <el-text style="margin: auto;font-size: 25px">大功告成 🎉</el-text>
            </div>
          </template>

        </el-form>
        <!-- 下一步按钮 -->
        <el-button @click="next">Next</el-button>

      </div>
    </div>
  </div>
</template>

<style scoped>
/* 全局样式重置，确保页面占满整个视口且无滚动条 */
html, body {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden; /* 避免滚动条影响布局 */
}

/* 注册页面容器样式 */
.register-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100vw;
  flex-direction: column;
  background: deeppink; /* 背景颜色 */
}

/* 注册表单盒子样式 */
.register-box {
  background: white;
  box-shadow: 0 4px 8px rgba(255, 255, 255, 0.67);
  width: 40vw;
  height: 50vh;
  border-radius: 30px;
}

/* 欢迎文字区域样式 */
.welcome-text {
  height: 100px;
  margin-bottom: 30px;
}

/* 注册表单样式 */
.register-form {
  display: flex;
  flex-direction: column;
  margin: 10px;
  justify-content: center;
}

/* 注册标题文字样式 */
.register-text {
  color: grey;
  font-weight: bold;
  font-size: 30px;
  text-align: center;
  margin: 15px;
}
</style>