<template>
  <div class="change-password">
    <el-dialog
      title="修改密码"
      v-model="dialogVisible"
      width="30%"
      :before-close="handleClose"
    >
      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="100px"
        class="change-password-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            v-model="form.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

export default {
  name: 'ChangePassword',
  setup() {
    const dialogVisible = ref(false)
    const formRef = ref(null)
    const form = reactive({
      username: '',
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else {
        if (form.confirmPassword !== '') {
          formRef.value.validateField('confirmPassword')
        }
        callback()
      }
    }

    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== form.newPassword) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      oldPassword: [
        { required: true, message: '请输入旧密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, validator: validatePass, trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, validator: validatePass2, trigger: 'blur' }
      ]
    }

    const handleClose = () => {
      dialogVisible.value = false
      formRef.value?.resetFields()
    }

    const handleSubmit = async () => {
      if (!formRef.value) return
      
      await formRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const localUser = localStorage.getItem('student-user') 
              ? JSON.parse(localStorage.getItem('student-user'))
              : localStorage.getItem('admin-user')
                ? JSON.parse(localStorage.getItem('admin-user'))
                : null;
                
            const token = localUser ? localUser.token : '';
            
            const response = await request.post('/api/student/changePassword', {
              username: form.username,
              oldPassword: form.oldPassword,
              newPassword: form.newPassword
            }, {
              headers: {
                Authorization: `Bearer ${token}`
              }
            })

            if (response.data.code === '200') {
              ElMessage.success('密码修改成功，请查收邮件')
              handleClose()
            } else {
              ElMessage.error(response.data.msg || '修改失败')
            }
          } catch (error) {
            ElMessage.error('修改失败，请稍后重试')
          }
        }
      })
    }

    return {
      dialogVisible,
      form,
      formRef,
      rules,
      handleClose,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.change-password-form {
  padding: 20px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 