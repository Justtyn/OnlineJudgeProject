<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from "@/utils/request.js";

// 从 localStorage 中获取登录信息
const localUser = localStorage.getItem('student-user') 
  ? JSON.parse(localStorage.getItem('student-user'))
  : localStorage.getItem('admin-user')
    ? JSON.parse(localStorage.getItem('admin-user'))
    : null;

const token = localUser ? localUser.token : '';
const userId = localUser ? localUser.id : null;
const classId = localUser && localUser.classId ? localUser.classId : null;

// 班级列表数据
const classList = ref([])

// 添加显示模式状态
const displayMode = ref('list') // 'list' 或 'card'

// 切换显示模式的函数
const toggleDisplayMode = () => {
  displayMode.value = displayMode.value === 'list' ? 'card' : 'list'
}

// 加载数据
const loadData = async () => {
  try {
    if (!userId) {
      ElMessage.error('未获取到用户ID，请重新登录');
      return;
    }

    const response = await request.get(`/courses/${classId}`);
    if (response.data.success) {
      // 将响应数据转换为数组格式
      const courseData = response.data.data;
      classList.value = Array.isArray(courseData) 
        ? courseData 
        : [courseData].filter(item => item); // 过滤掉可能的null值
      
      // 获取每个班级的创建者信息
      for (const course of classList.value) {
        try {
          const adminResponse = await request.get(`/admin/${course.creatorId}`);
          if (adminResponse.data.code === '200') {
            // 添加创建者名称属性
            course.creatorName = adminResponse.data.data.name || adminResponse.data.data.username || '未知';
          } else {
            course.creatorName = '未知';
          }
        } catch (error) {
          console.error(`获取管理员信息失败，ID: ${course.creatorId}`, error);
          course.creatorName = '未知';
        }
      }
      
      console.log('成功获取班级数据:', classList.value);
    } else {
      console.error('请求失败:', response.data);
      ElMessage.error('获取班级数据失败');
    }
  } catch (error) {
    console.error('请求异常:', error);
    ElMessage.error('获取数据失败：' + (error.message || '未知错误'));
  }
}

// 刷新方法
const handleRefresh = () => {
  loadData();
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
  <div class="class-container">
    <!-- 标题区域 -->
    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">我的班级</span>
            <el-button type="primary" @click="handleRefresh">
              <el-icon>
                <Refresh />
              </el-icon>
              刷新
            </el-button>
          </div>
          <div class="header-right">
            <el-button type="default" @click="toggleDisplayMode" size="small">
              切换到 {{ displayMode === 'list' ? '卡片' : '列表' }} 模式
            </el-button>
          </div>
        </div>
      </template>
    </el-card>

    <!-- 表格展示区域 -->
    <el-card class="table-card" v-if="displayMode === 'list'">
      <el-table :data="classList" style="width: 100%" border stripe highlight-current-row>
        <el-table-column prop="id" label="班级ID" width="100" align="center" />
        <el-table-column label="班级名称" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <a @click="$router.push(`/class/${scope.row.id}?name=${scope.row.name}`)">
              {{ scope.row.name }}
            </a>
          </template>
        </el-table-column>
        <el-table-column label="创建者" width="120" align="center">
          <template #default="scope">
            {{ scope.row.creatorName }}
          </template>
        </el-table-column>
        <el-table-column prop="homeworkQuantity" label="作业数量" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.homeworkQuantity > 0 ? 'success' : 'info'">
              {{ scope.row.homeworkQuantity || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="$router.push({
              path: `/class/${scope.row.id}`,
              query: { name: scope.row.name }
            })">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 无数据时显示空状态 -->
      <el-empty v-if="classList.length === 0" description="暂无班级信息" />
    </el-card>

    <!-- 班级卡片展示区域 -->
    <div class="class-list" v-if="displayMode === 'card'">
      <template v-if="classList.length > 0">
        <el-card 
          v-for="(item, index) in classList" 
          :key="item.id" 
          class="class-card"
        >
          <div class="class-header" :style="{ backgroundColor: getRandomColor(index) }">
            <div class="class-title">{{ item.name }}</div>
            <div class="class-info">作业数量: {{ item.homeworkQuantity || 0 }}</div>
          </div>
          <div class="class-content">
            <div class="class-item">
              <span class="info-label">班级ID：</span>
              <span class="info-value">{{ item.id }}</span>
            </div>
            <div class="class-item">
              <span class="info-label">创建者：</span>
              <span class="info-value">{{ item.creatorName }}</span>
            </div>
          </div>
          <div class="class-actions">
            <el-button type="primary" size="small" @click="$router.push({
              path: `/class/${item.id}`,
              query: { name: item.name }
            })">查看详情</el-button>
          </div>
        </el-card>
      </template>
      <el-empty v-else description="暂无班级信息" />
    </div>
  </div>
</template>

<style scoped>
.class-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 80vh;
  width: 100%;
  overflow-x: hidden; /* 防止横向滚动条 */
}

.header-card, .table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
  margin-right: 8px;
}

/* 表格样式 */
:deep(.el-table) {
  margin-top: 10px;
}

.class-link {
  color: #1890ff;
  cursor: pointer;
  text-decoration: none;
}

.class-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}

/* 卡片列表样式（备选方案） */
.class-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
}

.class-card {
  margin-bottom: 0;
  transition: all 0.3s;
  width: 100%;
}

.class-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.class-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 4px 4px 0 0;
}

.class-title {
  font-size: 16px;
  font-weight: bold;
}

.class-info {
  font-size: 14px;
  color: #606266;
}

.class-content {
  padding: 16px;
  min-height: 80px;
}

.class-item {
  display: flex;
  margin-bottom: 8px;
}

.info-label {
  width: 100px;
  color: #909399;
}

.info-value {
  color: #303133;
}

.class-actions {
  display: flex;
  justify-content: flex-end;
  padding: 0 16px 16px;
}

:deep(.el-card__header) {
  padding: 15px 20px;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>