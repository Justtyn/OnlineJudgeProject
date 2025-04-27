<template>
  <div class="login-container">
    <!-- 欢迎文字区域 -->
    <div class="welcome-text">
      <TypingEffect />
    </div>
    <!-- 登录框主体 -->
    <div class="login-box">
      <div class="login-text">Login</div>
      <el-divider />
      <el-form
          class="login-form"
          :model="data.form"
          ref="formRef"
          :rules="rules"
      >
        <!-- 账号输入框 -->
        <el-form-item prop="username">
          <el-input
              prefix-icon="User"
              v-model="data.form.username"
              placeholder="请输入账号"
          ></el-input>
        </el-form-item>
        <!-- 密码输入框 -->
        <el-form-item prop="password">
          <el-input
              show-password
              prefix-icon="Lock"
              v-model="data.form.password"
              placeholder="请输入密码"
          ></el-input>
        </el-form-item>
        <!-- 验证码输入 -->
        <el-form-item prop="captcha">
          <div class="captcha-wrapper">
            <el-input
                v-model="data.form.captcha"
                placeholder="请输入验证码"
                class="captcha-input"
                prefix-icon="Key"
            ></el-input>
            <canvas
                ref="captchaCanvas"
                @click="generateCaptcha"
                width="100"
                height="38"
                class="captcha-img"
            ></canvas>
          </div>
        </el-form-item>
        <!-- 角色选择 -->
        <el-form-item prop="role">
          <el-select style="width: 100%" v-model="data.form.role">
            <el-option value="STUDENT" label="学生"></el-option>
            <el-option value="TEACHER" label="教师"></el-option>
            <el-option value="ADMIN" label="管理员"></el-option>
          </el-select>
        </el-form-item>
        <!-- 登录按钮 -->
        <el-form-item>
          <el-button type="danger" style="width: 100%" @click="login"
          >登 陆</el-button
          >
        </el-form-item>
      </el-form>
      <!-- 注册提示 -->
      <div class="register-text">
        还没有账号？请
        <router-link to="/register" class="register-link">注册</router-link>
        <span class="divider">|</span>
        <a href="javascript:;" class="reset-link" @click="showResetDialog">忘记密码？</a>
      </div>
    </div>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetDialogVisible"
      title="重置密码"
      width="30%"
      :close-on-click-modal="false"
    >
      <el-form :model="resetForm" :rules="resetRules" ref="resetFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="resetForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="resetForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="resetDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleResetPassword">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 导入打字效果组件，用于登录页面的动态文字展示
import TypingEffect from "@/views/tool/TypingEffect-Login.vue";
// 导入 Vue 的响应式 API
import { reactive, ref, onMounted } from "vue";
// 导入 Element Plus 的消息提示组件
import { ElMessage } from "element-plus";
// 导入封装好的请求工具
import request from "@/utils/request.js";
// 导入路由实例，用于页面跳转
import router from "@/router/index.js";

// 记住登录信息的开关（界面中未启用）
const saveUserLoginInfo = ref(true);

// 表单引用
const formRef = ref();
// 验证码画布引用
const captchaCanvas = ref(null);

// 用户数据对象
const data = reactive({
  form: {
    username: "",
    password: "",
    role: "STUDENT",
    captcha: ""
  }
});

// 表单验证规则定义
const rules = {
  username: [{ required: true, message: "请输入账号", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  captcha: [{ required: true, message: "请输入验证码", trigger: "blur" }]
};

// 验证码原始值
const captchaCode = ref("");

// 生成 4 位随机验证码
const generateCaptcha = () => {
  const chars =
      "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
  let code = "";
  for (let i = 0; i < 4; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  captchaCode.value = code;
  drawCaptcha(code);
};

// 将码绘制到 Canvas
const drawCaptcha = (code) => {
  const canvas = captchaCanvas.value;
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  const w = canvas.width;
  const h = canvas.height;
  // 清空画布
  ctx.clearRect(0, 0, w, h);
  // 填充背景
  ctx.fillStyle = randomLightColor();
  ctx.fillRect(0, 0, w, h);
  // 绘制字符
  for (let i = 0; i < code.length; i++) {
    ctx.font = "24px sans-serif";
    ctx.fillStyle = randomDarkColor();
    const x = 10 + i * 22;
    const y = 28 + Math.random() * 4;
    ctx.fillText(code[i], x, y);
  }
  // 干扰线
  for (let i = 0; i < 3; i++) {
    ctx.strokeStyle = randomDarkColor();
    ctx.beginPath();
    ctx.moveTo(Math.random() * w, Math.random() * h);
    ctx.lineTo(Math.random() * w, Math.random() * h);
    ctx.stroke();
  }
};

// 生成浅色背景
const randomLightColor = () => {
  const r = 180 + Math.floor(Math.random() * 75);
  const g = 180 + Math.floor(Math.random() * 75);
  const b = 180 + Math.floor(Math.random() * 75);
  return `rgb(${r},${g},${b})`;
};

// 生成深色文字/线条
const randomDarkColor = () => {
  const r = Math.floor(Math.random() * 100);
  const g = Math.floor(Math.random() * 100);
  const b = Math.floor(Math.random() * 100);
  return `rgb(${r},${g},${b})`;
};

// 组件挂载后初始化验证码
onMounted(() => {
  generateCaptcha();
});

// 登录方法，验证验证码后再请求
const login = async () => {
  try {
    const valid = await formRef.value.validate();
    if (!valid) return;
    // 本地验证码校验
    if (
        data.form.captcha.trim().toLowerCase() !==
        captchaCode.value.toLowerCase()
    ) {
      ElMessage.error("验证码错误，请重新输入");
      data.form.captcha = "";
      generateCaptcha();
      return;
    }
    // 发送登录请求
    const res = await request.post("/login", data.form);
    if (res.data.code === "200") {
      localStorage.setItem(
          "student-user",
          JSON.stringify(res.data.data)
      );
      ElMessage.success(res.data.msg || "登录成功");
      try {
        await router.push("/");
      } catch (err) {
        ElMessage.error("页面跳转失败，请刷新重试");
      }
    } else {
      ElMessage.error(res.data.msg || "登录失败");
      generateCaptcha();
    }
  } catch (error) {
    ElMessage.error("登录过程中发生错误");
    generateCaptcha();
  }
};

// 重置密码相关
const resetDialogVisible = ref(false);
const resetFormRef = ref();
const resetForm = reactive({
  username: '',
  email: ''
});

const resetRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
};

const showResetDialog = () => {
  resetDialogVisible.value = true;
  resetForm.username = '';
  resetForm.email = '';
};

const handleResetPassword = async () => {
  try {
    await resetFormRef.value.validate();
    const res = await request.post('/api/student/resetPassword', resetForm);
    if (res.data.code === '200') {
      ElMessage.success('重置密码邮件已发送，请查收邮箱');
      resetDialogVisible.value = false;
    } else {
      ElMessage.error(res.data.msg || '重置密码失败');
    }
  } catch (error) {
    ElMessage.error('重置密码过程中发生错误');
  }
};
</script>

<style scoped>
html,
body {
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
  background: linear-gradient(
      -45deg,
      #ee7752,
      #e73c7e,
      #23a6d5,
      #23d5ab
  );
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
}
@keyframes gradientBG {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
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
.register-link {
  color: #1890ff;
  text-decoration: none;
}
.register-link:hover {
  text-decoration: underline;
}
/* 验证码区域样式 */
.captcha-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
}
.captcha-input {
  flex: 1;
  margin-right: 10px;
}
.captcha-img {
  width: 100px;
  height: 38px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
.captcha-img:hover {
  opacity: 0.8;
}

/* 添加移动端适配样式 */
@media screen and (max-width: 768px) {
  .login-box {
    padding: 20px;
    margin-bottom: 100px;
  }
  
  .login-form {
    padding: 0 20px;
  }
  
  .login-text {
    font-size: 24px;
    margin-bottom: 20px;
  }
  
  .welcome-text {
    height: 60px;
    margin-bottom: 20px;
  }
  
  .register-text {
    margin-top: 20px;
    font-size: 14px;
  }
  
  .captcha-wrapper {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  .captcha-input {
    margin-right: 0;
  }
  
  .captcha-img {
    width: 80px;
    height: 32px;
  }
}

.divider {
  margin: 0 10px;
  color: #999;
}

.reset-link {
  color: #1890ff;
  text-decoration: none;
}

.reset-link:hover {
  text-decoration: underline;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .el-dialog {
    width: 90% !important;
  }
}
</style>
