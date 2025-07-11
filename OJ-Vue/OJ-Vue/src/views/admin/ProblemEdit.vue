<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageLayout from '@/components/layout/PageLayout.vue'
import request from "@/utils/request.js"

const route = useRoute()
const router = useRouter()
const isEdit = ref(!!route.params.id)

const formRef = ref()
const loading = ref(false)

const formState = ref({
  id: undefined,
  name: '',
  setter: '',
  desc: '',
  descInput: '',
  descOutput: '',
  sampleInput: '',
  sampleOutput: '',
  hint: '',
  dailyChallenge: false,
  createTime: new Date().toISOString(),
  acCount: 0,
  submitCount: 0
})

const rules = {
  name: [
    { required: true, message: '请输入题目名称', trigger: 'blur' }
  ],
  setter: [
    { required: true, message: '请输入出题人', trigger: 'blur' }
  ],
  desc: [
    { required: true, message: '请输入题目描述', trigger: 'blur' }
  ],
  descInput: [
    { required: true, message: '请输入输入描述', trigger: 'blur' }
  ],
  descOutput: [
    { required: true, message: '请输入输出描述', trigger: 'blur' }
  ],
  sampleInput: [
    { required: true, message: '请输入输入样例', trigger: 'blur' }
  ],
  sampleOutput: [
    { required: true, message: '请输入输出样例', trigger: 'blur' }
  ]
}

// 获取题目详情
const fetchProblemDetail = async () => {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    const response = await request.get(`/problem/${route.params.id}`)
    if (response.data.code === '200') {
      const problem = response.data.data
      problem.dailyChallenge = problem.dailyChallenge === 'true' || problem.dailyChallenge === 'TRUE'
      Object.assign(formState.value, problem)
    } else {
      message.error(response.data.msg || '获取题目详情失败')
    }
  } catch (error) {
    message.error('获取题目详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    const submitData = {
      ...formState.value,
      dailyChallenge: formState.value.dailyChallenge ? 'TRUE' : 'FALSE',
      createTime: formState.value.createTime || new Date().toISOString()
    }

    if (isEdit.value) {
      submitData.id = route.params.id
    }

    const response = await (isEdit.value ? 
      request.put('/problem', submitData) :
      request.post('/problem', submitData))

    if (response.data.code === '200') {
      message.success(isEdit.value ? '更新成功' : '添加成功')
      router.push('/admin/problems')
    } else {
      message.error(response.data.msg || (isEdit.value ? '更新失败' : '添加失败'))
    }
  } catch (error) {
    if (error?.errorFields) {
      message.error('请填写必填项')
    } else {
      message.error(isEdit.value ? '更新失败' : '添加失败')
      console.error(error)
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchProblemDetail()
})
</script>

<template>
  <page-layout :title="isEdit ? '编辑题目' : '新增题目'" :show-back="true">
    <div class="problem-edit-container">
      <a-card>
        <a-form
          ref="formRef"
          :model="formState"
          :rules="rules"
          :label-col="{ span: 4 }"
          :wrapper-col="{ span: 16 }"
        >
          <a-form-item label="题目名称" name="name">
            <a-input v-model:value="formState.name" placeholder="请输入题目名称" />
          </a-form-item>

          <a-form-item label="出题人" name="setter">
            <a-input v-model:value="formState.setter" placeholder="请输入出题人" />
          </a-form-item>

          <a-form-item label="题目描述" name="desc">
            <a-textarea
              v-model:value="formState.desc"
              :rows="6"
              placeholder="请输入题目描述"
            />
          </a-form-item>

          <a-form-item label="输入描述" name="descInput">
            <a-textarea
              v-model:value="formState.descInput"
              :rows="4"
              placeholder="请输入输入描述"
            />
          </a-form-item>

          <a-form-item label="输出描述" name="descOutput">
            <a-textarea
              v-model:value="formState.descOutput"
              :rows="4"
              placeholder="请输入输出描述"
            />
          </a-form-item>

          <a-form-item label="输入样例" name="sampleInput">
            <a-textarea
              v-model:value="formState.sampleInput"
              :rows="4"
              placeholder="请输入输入样例"
            />
          </a-form-item>

          <a-form-item label="输出样例" name="sampleOutput">
            <a-textarea
              v-model:value="formState.sampleOutput"
              :rows="4"
              placeholder="请输入输出样例"
            />
          </a-form-item>

          <a-form-item label="提示说明" name="hint">
            <a-textarea
              v-model:value="formState.hint"
              :rows="4"
              placeholder="请输入提示说明（选填）"
            />
          </a-form-item>

          <a-form-item label="每日一题" name="dailyChallenge">
            <a-switch
              v-model:checked="formState.dailyChallenge"
            />
          </a-form-item>

          <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
            <a-space>
              <a-button type="primary" :loading="loading" @click="handleSubmit">
                {{ isEdit ? '更新' : '添加' }}
              </a-button>
              <a-button @click="router.push('/admin/problems')">取消</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>
    </div>
  </page-layout>
</template>

<style scoped>
.problem-edit-container {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

:deep(.ant-card) {
  max-width: 1200px;
  margin: 0 auto;
}

:deep(.ant-form-item) {
  margin-bottom: 24px;
}
</style> 