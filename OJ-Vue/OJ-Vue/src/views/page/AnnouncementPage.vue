<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from "@/utils/request.js";
import { Clock } from '@element-plus/icons-vue'

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'

  try {
    // 处理ISO格式的时间字符串
    if (dateTimeStr.includes('T')) {
      return dateTimeStr.replace('T', ' ').split('.')[0]
    }

    // 处理时间戳
    if (!isNaN(dateTimeStr)) {
      const date = new Date(Number(dateTimeStr))
      if (isNaN(date.getTime())) return '-'

      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      }).replace(',', '')
    }

    // 其他格式直接返回
    return dateTimeStr
  } catch (e) {
    console.error('时间格式化错误:', e)
    return '-'
  }
}

// 查询条件
const queryForm = reactive({
  title: '',
  content: ''
})

// 公告列表数据
const announcementList = ref([])

// 公告表单数据
const announcementForm = reactive({
  title: '',
  content: '',
//   time: new Date().toISOString()
})

// 表单规则
const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

// 表单引用
const addFormRef = ref(null)

// 重置表单
const resetForm = () => {
  announcementForm.title = ''
  announcementForm.content = ''
}

// 加载数据
const loadData = async () => {
  try {
    let response;

    // 根据查询条件选择对应的API接口
    if (queryForm.title) {
      // 按标题查询
      response = await request.get('/announcement/title', {
        params: {
          title: queryForm.title
        }
      });
      if (response.data.code === '200') {
        // 将单个对象转换为数组格式
        announcementList.value = Array.isArray(response.data.data) 
          ? response.data.data 
          : [response.data.data].filter(item => item); // 过滤掉可能的null值
      }
    } else if (queryForm.content) {
      // 按内容查询
      response = await request.get('/announcement/content', {
        params: {
          content: queryForm.content
        }
      });
      if (response.data.code === '200') {
        // 将单个对象转换为数组格式
        announcementList.value = Array.isArray(response.data.data) 
          ? response.data.data 
          : [response.data.data].filter(item => item); // 过滤掉可能的null值
      }
    } else {
      // 无查询条件，获取所有数据
      response = await request.get('/announcement');
      if (response.data.code === '200') {
        announcementList.value = response.data.data || [];
      }
    }

    console.log('成功获取公告数据:', announcementList.value);
  } catch (error) {
    console.error('请求异常:', error);
    ElMessage.error('获取数据失败：' + (error.message || '未知错误'));
  }
}

// 查询方法
const handleQuery = () => {
  // 检查是否输入了多个查询条件
  const conditions = [queryForm.title, queryForm.content].filter(Boolean);
  if (conditions.length > 1) {
    ElMessage.warning('请只选择一个查询条件');
    return;
  }

  loadData();
}

// 刷新方法
const handleRefresh = () => {
  queryForm.title = '';
  queryForm.content = '';
  loadData();
}

// 添加公告对话框
const addDialogVisible = ref(false)

// 打开添加对话框
const handleAdd = () => {
  resetForm()
  addDialogVisible.value = true
}

// 取消操作
const handleCancel = () => {
  resetForm()
  addDialogVisible.value = false
}

// 保存操作
const handleSave = async () => {
  try {
    await addFormRef.value.validate()

    const submitData = {
      ...announcementForm,
    //   time: announcementForm.time || new Date().toISOString()
    }

    // 添加操作
    const response = await request.post('/announcement', submitData)
    const res = response.data
    if (res.code === '200') {
      ElMessage.success('添加成功')
      handleCancel()
      loadData()
    } else {
      ElMessage.error(res.msg || '添加失败')
    }
  } catch (error) {
    if (error.message) {
      ElMessage.error('表单验证失败，请检查必填项')
    } else {
      ElMessage.error('操作失败')
      console.error(error)
    }
  }
}

// 删除公告
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.delete(`/announcement/${id}`)
    const res = response.data
    if (res.code === '200') {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

// 随机生成背景颜色
const getRandomColor = (index) => {
  const colors = [
    '#e6f7ff', // 浅蓝
    '#f6ffed', // 浅绿
    '#fff7e6', // 浅橙
    '#fff0f6', // 浅粉
    '#f9f0ff', // 浅紫
    '#e6fffb', // 浅青
  ]
  return colors[index % colors.length]
}

// 页面加载时获取数据
onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="announcement-container">
    <!-- 搜索区域 -->
    <el-card class="search-card">
      <template #header>
        <div class="card-header">
          <span class="title">公告列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon>
              <Plus />
            </el-icon>
            添加公告
          </el-button>
        </div>
      </template>

      <el-form :model="queryForm" inline>
        <el-form-item>
          <el-input v-model="queryForm.title" placeholder="按标题查询" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryForm.content" placeholder="按内容查询" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleRefresh">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 公告展示区域 -->
    <div class="announcement-list">
      <template v-if="announcementList.length > 0">
        <el-card 
          v-for="(item, index) in announcementList" 
          :key="item.id" 
          class="announcement-card"
        >
          <div class="announcement-header" :style="{ backgroundColor: getRandomColor(index) }">
            <div class="announcement-title">{{ item.title }}</div>
            <div class="announcement-time">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(item.time) }}</span>
            </div>
          </div>
          <div class="announcement-content">
            {{ item.content }}
          </div>
          <!-- 删除按钮已隐藏 -->
        </el-card>
      </template>
      <el-empty v-else description="暂无公告" />
    </div>

    <!-- 添加公告对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加公告" width="50%">
      <el-form ref="addFormRef" :model="announcementForm" :rules="rules" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="announcementForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input v-model="announcementForm.content" type="textarea" rows="6" placeholder="请输入公告内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCancel">取消</el-button>
          <el-button type="primary" @click="handleSave">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
body {
  margin: 0;
  padding: 0;
  width: 100vw;
  height: 100vh;
}

.announcement-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 80vh;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  overflow-x: hidden;
}

<<<<<<< HEAD
/* 添加卡片加载动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.announcement-card {
  margin-bottom: 0;
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  transform: translateY(20px);
}

.announcement-card:nth-child(1) { animation-delay: 0.1s; }
.announcement-card:nth-child(2) { animation-delay: 0.2s; }
.announcement-card:nth-child(3) { animation-delay: 0.3s; }
.announcement-card:nth-child(4) { animation-delay: 0.4s; }
.announcement-card:nth-child(5) { animation-delay: 0.5s; }

.announcement-card:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}

/* 添加标题动画效果 */
.announcement-title {
  font-size: 16px;
  font-weight: bold;
  position: relative;
  display: inline-block;
}

.announcement-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  transition: width 0.3s ease;
}

.announcement-card:hover .announcement-title::after {
  width: 100%;
}

/* 添加内容区域的渐变效果 */
.announcement-content {
  padding: 16px;
  min-height: 80px;
  white-space: pre-line;
  line-height: 1.6;
  font-weight: 500;
  letter-spacing: 0.5px;
  background: linear-gradient(to bottom, transparent, rgba(0, 0, 0, 0.02));
  transition: all 0.3s ease;
}

.announcement-card:hover .announcement-content {
  background: linear-gradient(to bottom, transparent, rgba(0, 0, 0, 0.04));
}

/* 添加时间图标的动画效果 */
.announcement-time .el-icon {
  margin-right: 5px;
  transition: transform 0.3s ease;
}

.announcement-card:hover .announcement-time .el-icon {
  transform: rotate(360deg);
}

/* 添加搜索框的动画效果 */
:deep(.el-input__inner) {
  transition: all 0.3s ease;
}

:deep(.el-input__inner:focus) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 添加按钮的动画效果 */
:deep(.el-button) {
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

:deep(.el-button::after) {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 5px;
  height: 5px;
  background: rgba(255, 255, 255, 0.5);
  opacity: 0;
  border-radius: 100%;
  transform: scale(1, 1) translate(-50%);
  transform-origin: 50% 50%;
}

:deep(.el-button:active::after) {
  animation: ripple 0.6s ease-out;
}

@keyframes ripple {
  0% {
    transform: scale(0, 0);
    opacity: 0.5;
  }
  100% {
    transform: scale(20, 20);
    opacity: 0;
  }
}

=======
>>>>>>> 0ceb392b5528f01c6eb373985bf35281707ba359
/* 添加移动端适配样式 */
@media screen and (max-width: 768px) {
  .announcement-container {
    padding: 10px;
  }
  
  .search-card {
    margin-bottom: 15px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .title {
    font-size: 16px;
  }
  
  :deep(.el-form) {
    padding: 10px 0;
  }
  
  :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 10px;
    width: 100%;
  }
  
  :deep(.el-input) {
    width: 100%;
  }
  
  .announcement-card {
    margin-bottom: 15px;
  }
  
  .announcement-header {
    flex-direction: column;
    gap: 8px;
    padding: 10px;
  }
  
  .announcement-title {
    font-size: 16px;
  }
  
  .announcement-time {
    font-size: 12px;
  }
  
  .announcement-content {
    padding: 12px;
    font-size: 14px;
  }
  
  :deep(.el-dialog) {
    width: 90% !important;
    margin: 0 auto;
  }
  
  :deep(.el-dialog__body) {
    padding: 15px;
  }
}

.search-card {
  margin-bottom: 20px;
  max-width: 1200px; /* 保持一致的最大宽度 */
  margin-left: auto;
  margin-right: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

/* 增加表单间距 */
:deep(.el-form) {
  padding: 20px 0;
}

:deep(.el-form-item) {
  margin-right: 16px;
  margin-bottom: 0;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%; /* 确保不会超出容器宽度 */
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 4px 4px 0 0;
}

.announcement-time {
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
}

.announcement-actions {
  display: flex;
  justify-content: flex-end;
  padding: 0 16px 16px;
}

:deep(.el-card__header) {
  padding: 15px 20px;
}

:deep(.el-card__body) {
  padding: 0;
}

/* 修改搜索区域表单样式 */
:deep(.el-card .el-form) {
  padding: 10px 20px;
}
</style>
