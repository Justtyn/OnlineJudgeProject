<template>
  <div class="login-container">
    <!-- 欢迎文字区域 -->
    <div class="welcome-text">
      <TypingEffectRegister />
    </div>
    <!-- 注册框主体（使用登录页样式） -->
    <div class="login-box">
      <div class="login-text">Register</div>
      <el-divider />
      <el-form
          ref="formRef"
          :model="userRegisterData.form"
          :rules="rules"
          class="login-form"
      >
        <!-- 步骤指示条 -->
        <el-steps style="max-width: 600px; width: 100%" :active="active" align-center>
          <el-step title="Step 1" description="角色选择" />
          <el-step title="Step 2" description="账户信息" />
          <el-step title="Step 3" description="个人信息" />
          <el-step title="Step 4" description="完成" />
        </el-steps>

        <!-- 各步骤表单内容 -->
        <div class="step-wrapper">
          <!-- 角色选择 -->
          <template v-if="active === 0">
            <el-form-item prop="role">
              <el-select style="width: 100%" v-model="userRegisterData.form.role">
                <el-option value="STUDENT" label="学生"></el-option>
                <el-option value="TEACHER" label="教师"></el-option>
                <el-option value="ADMIN" label="管理员"></el-option>
              </el-select>
            </el-form-item>
          </template>

          <!-- 账户信息 -->
          <template v-else-if="active === 1">
            <el-form-item prop="username">
              <el-input
                  prefix-icon="User"
                  v-model="userRegisterData.form.username"
                  placeholder="请输入账号"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                  show-password
                  prefix-icon="Lock"
                  v-model="userRegisterData.form.password"
                  placeholder="请输入密码"
              />
            </el-form-item>
          </template>

          <!-- 个人信息 -->
          <template v-else-if="active === 2">
            <el-form-item prop="name">
              <el-input
                  prefix-icon="Message"
                  v-model="userRegisterData.form.name"
                  placeholder="请输入姓名"
              />
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
              <el-input
                  prefix-icon="Message"
                  v-model="userRegisterData.form.email"
                  placeholder="请输入邮箱"
              />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input
                  prefix-icon="Iphone"
                  v-model="userRegisterData.form.phone"
                  placeholder="请输入手机号"
              />
            </el-form-item>
          </template>

          <!-- 注册完成 -->
          <template v-else-if="active === 3">
            <div
                style="display: flex; justify-content: center; align-items: center; text-align: center;"
            >
              <el-text style="margin: auto; font-size: 25px">大功告成 🎉</el-text>
            </div>
          </template>
        </div>

        <!-- 下一步 / 注册 按钮 -->
        <el-button type="primary" style="margin-top: 20px;" @click="next">
          {{
            active < 3 ? (active === 2 ? '注册' : '下一步') : '重新开始'
          }}
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
// 导入打字效果组件，用于注册页面的欢迎文字动画
import TypingEffectRegister from "@/views/tool/TypingEffect-Register.vue";
// 导入必要的 Vue 组合式 API 函数
import { reactive, ref } from "vue";
// 导入 canvas-confetti 库，用于创建烟花/彩带效果
import confetti from "canvas-confetti";
// 导入封装好的请求工具
import request from "@/utils/request.js";
// 导入 Element Plus 的消息提示组件
import { ElMessage } from "element-plus";
// 导入路由实例，用于页面跳转
import router from "@/router/index.js";

// 表单引用，用于验证
const formRef = ref();
// 当前步骤索引
const active = ref(0);
// 性别下拉选项
const options = reactive([
  { label: "男", value: "M" },
  { label: "女", value: "F" }
]);

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
});

// 校验规则
const rules = {
  role: [{ required: true, message: '请选择身份', trigger: 'change' }],
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
};

// 下一步或重新开始
const next = () => {
  formRef.value.validate(valid => {
    if (valid) {
      if (active.value < 3) {
        active.value++;
      } else {
        active.value = 0;
      }
      if (active.value === 3) {
        // 最后一步：提交注册
        handleRegister();
      }
    }
  });
};

// 注册提交逻辑
const handleRegister = async () => {
  try {
    const res = await request.post("/register", userRegisterData.form);
    if (res.data.code === "200") {
      ElMessage.success(res.data.msg || "注册成功");
      confetti();
      await router.push("/login");
    } else {
      ElMessage.error(res.data.msg || "注册失败");
    }
  } catch (error) {
    ElMessage.error("注册过程中发生错误");
  }
};
</script>

<style scoped>
/* —— 登录页背景及容器样式 —— */
html, body {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden;
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
  background-color: rgba(255,255,255,0.85);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
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
/* —— 多步骤注册表单样式 —— */
.step-wrapper {
  margin: 20px 0;
}
</style>
