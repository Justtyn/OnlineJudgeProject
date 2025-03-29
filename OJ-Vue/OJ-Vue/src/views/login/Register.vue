<script setup>

import TypingEffectRegister from "@/views/tool/TypingEffect-Register.vue";

import {reactive, ref, watch} from 'vue'
import confetti from 'canvas-confetti'
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

// 表单验证
const formRef = ref()
const active = ref(0)

// 进度条下一步点击方法
const next = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      if (active.value++ > 2) active.value = 0
      console.log(active.value)
    }
  })
}
// 用户注册数据
const userRegisterData = reactive({
  form: {
    role: 'STUDENT',
    username: '',
    password: '',
    name: '',
    sex: '',
    email: '',
    phone: ''
  }
})
// 监听 active 的变化，当 active 等于 3 时触发烟花效果
watch(active, (newVal) => {
  if (newVal === 3) {
    startFireworks();
    console.log(userRegisterData.form)
    register();
  }

});

const register = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      request.post('/register', userRegisterData.form).then(res => {
        if (res.code === '200') {
          localStorage.setItem('student-user', JSON.stringify(res.data))
          ElMessage.success('注册成功')
          console.log(res.data)
          // 跳转到登陆页
          router.push('/login')
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

// 定义烟花效果函数
function startFireworks() {
  const end = Date.now() + 15 * 1000; // 烟花效果持续15秒
  const colors = ['#bb0000', '#ffffff'];

  (function frame() {
    confetti({
      particleCount: 2,
      angle: 60,
      spread: 55,
      origin: {x: 0},
      colors: colors
    });
    confetti({
      particleCount: 2,
      angle: 120,
      spread: 55,
      origin: {x: 1},
      colors: colors
    });

    if (Date.now() < end) {
      requestAnimationFrame(frame);
    }
  }());
}

// 验证规则
const rules = reactive({
  username: [
    {required: true, message: '请输入账号', trigger: 'blur'},
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
  ],
})

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
  <div class="register-container">
    <div class="welcome-text">
      <TypingEffectRegister/>
    </div>

    <div class="register-box">

      <div style="width: 100%;height: 100%;display: flex;flex-direction: column;justify-content: center;align-items: center">
        <div class="register-text">Register</div>
        <el-steps style="max-width: 600px;width: 100%" :active="active" align-center>
          <el-step title="Step 1" description="角色选择"/>
          <el-step title="Step 2" description="账户信息"/>
          <el-step title="Step 3" description="个人信息"/>
        </el-steps>

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

          <!-- Step 4: 大功告成 -->
          <template v-else-if="active === 3">
            <div style="display: flex;justify-content: center;align-items: center;text-align: center;">
              <el-text style="margin: auto;font-size: 25px">大功告成 🎉</el-text>
            </div>
          </template>

        </el-form>
        <el-button @click="next">Next</el-button>

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

.register-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100vw;
  flex-direction: column;
  background: deeppink;
}

.register-box {
  background: white;
  box-shadow: 0 4px 8px rgba(255, 255, 255, 0.67);
  width: 40vw;
  height: 50vh;
  border-radius: 30px;
}

.welcome-text {
  height: 100px;
  margin-bottom: 30px;
}

.register-form {
  display: flex;
  flex-direction: column;
  margin: 10px;
  justify-content: center;
}

.register-text {
  color: grey;
  font-weight: bold;
  font-size: 30px;
  text-align: center;
  margin: 15px;
}

</style>