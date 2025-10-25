<template>
  <div class="comment-item">
    <!-- 主评论 -->
    <div class="comment-main">
      <div class="comment-header">
        <div class="user-info" @click="$emit('click-user', comment.userId)">
          <el-avatar 
            :src="commentUserInfo?.avatar" 
            :size="32"
            class="user-avatar"
          />
          <span class="username">{{ commentUserInfo?.username || '未知用户' }}</span>
        </div>
        <span class="time">{{ formatDateTime(comment.createTime) }}</span>
      </div>
      <div class="comment-content">{{ comment.content }}</div>
      <div class="comment-actions">
        <el-button type="text" @click="$emit('reply', { id: comment.id, username: commentUserInfo?.username })">
          回复
        </el-button>
      </div>
    </div>

    <!-- 子评论 -->
    <div v-if="comment.children && comment.children.length > 0" class="comment-children">
      <div v-for="child in comment.children" :key="child.id" class="child-comment">
        <div class="comment-header">
          <div class="user-info" @click="$emit('click-user', child.userId)">
            <el-avatar 
              :src="childUserInfo[child.id]?.avatar" 
              :size="24"
              class="user-avatar"
            />
            <span class="username">{{ childUserInfo[child.id]?.username || '未知用户' }}</span>
          </div>
          <span class="time">{{ formatDateTime(child.createTime) }}</span>
        </div>
        <div class="comment-content">{{ child.content }}</div>
        <div class="comment-actions">
          <el-button type="text" @click="$emit('reply', { id: child.id, username: childUserInfo[child.id]?.username })">
            回复
          </el-button>
        </div>

        <!-- 递归显示更深层的回复 -->
        <div v-if="child.children && child.children.length > 0" class="comment-children">
          <CommentItem 
            v-for="grandChild in child.children" 
            :key="grandChild.id"
            :comment="grandChild"
            :user-info="userInfo"
            @reply="$emit('reply', $event)"
            @click-user="$emit('click-user', $event)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, ref, onMounted } from 'vue'
import request from '@/utils/request.js'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  },
  userInfo: {
    type: Object,
    default: null
  }
})

defineEmits(['reply', 'click-user'])

// 获取token
const getAuthInfo = () => {
  const localUser = localStorage.getItem('student-user') 
    ? JSON.parse(localStorage.getItem('student-user'))
    : localStorage.getItem('admin-user')
      ? JSON.parse(localStorage.getItem('admin-user'))
      : null;
  
  return localUser ? {
    token: localUser.token,
    userId: localUser.id
  } : { token: '', userId: null };
}

const { token } = getAuthInfo();

// 用户信息
const commentUserInfo = ref(null)
const childUserInfo = ref({})

// 获取用户信息
const fetchUserInfo = async (userId) => {
  try {
    const response = await request.get(`/api/student/${userId}`, {
      headers: { Authorization: 'Bearer ' + token }
    })
    if (response.data.code === '200') {
      const userData = response.data.data
      return {
        username: userData.name || userData.username || '未知用户',
        avatar: userData.avatar || 'http://localhost:9090/uploads/1743236403200_IMG_0748.JPG'
      }
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
  return null
}

// 格式化时间
const formatDateTime = (s) => {
  if (!s) return '-'
  if (s.includes('T')) return s.replace('T', ' ').split('.')[0]
  if (!isNaN(s)) {
    const d = new Date(+s)
    return d.toLocaleString('zh-CN', {
      year:'numeric', month:'2-digit', day:'2-digit',
      hour:'2-digit', minute:'2-digit', second:'2-digit', hour12:false
    }).replace(',', '')
  }
  return s
}

// 组件挂载时获取用户信息
onMounted(async () => {
  // 获取主评论用户信息
  commentUserInfo.value = await fetchUserInfo(props.comment.userId)
  
  // 获取子评论用户信息
  if (props.comment.children) {
    for (const child of props.comment.children) {
      childUserInfo.value[child.id] = await fetchUserInfo(child.userId)
    }
  }
})
</script>

<style scoped>
.comment-item {
  margin-bottom: 16px;
}

.comment-main {
  background: var(--color-background);
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-info:hover {
  opacity: 0.8;
}

.user-avatar {
  border: 2px solid var(--color-background);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.username {
  font-weight: bold;
  color: #303133;
}

.time {
  color: #909399;
  font-size: 12px;
}

.comment-content {
  color: #606266;
  line-height: 1.6;
  margin: 8px 0;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
}

.comment-children {
  margin-left: 32px;
  margin-top: 16px;
}

.child-comment {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 8px;
}

.child-comment .comment-header {
  margin-bottom: 4px;
}

.child-comment .comment-content {
  margin: 4px 0;
}
</style> 