<template>
    <div :class="['comment-node', `level-${level}`]">
      <div class="comment-content">
        <div class="comment-header">
          <span class="comment-author">@{{ comment.username }}</span>
          <span class="comment-time">{{ formatDateTime(comment.createTime) }}</span>
          <div class="comment-actions">
            <el-button type="primary" link @click="$emit('edit', comment)">
              编辑
            </el-button>
            <el-popconfirm
              title="确定要删除这条评论吗？"
              @confirm="$emit('delete', comment.id)"
            >
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>
        <div class="comment-text">{{ comment.content }}</div>
      </div>
      <div v-if="comment.children && comment.children.length > 0" class="comment-children">
        <CommentNode
          v-for="child in comment.children"
          :key="child.id"
          :comment="child"
          :level="level + 1"
          @delete="$emit('delete', $event)"
          @edit="$emit('edit', $event)"
        />
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  interface Comment {
    id: number
    content: string
    userId: number
    username: string
    discussId: number
    createTime: string
    parentCommentId: number | null
    replyToUserId: number | null
    children?: Comment[]
  }
  
  const props = defineProps<{
    comment: Comment
    level: number
  }>()
  
  const emit = defineEmits<{
    (e: 'delete', id: number): void
    (e: 'edit', comment: Comment): void
  }>()
  
  const formatDateTime = (dateTimeStr: string) => {
    if (!dateTimeStr) return '-'
    try {
      if (dateTimeStr.includes('T')) {
        return dateTimeStr.replace('T', ' ').split('.')[0]
      }
      return dateTimeStr
    } catch (e) {
      console.error('时间格式化错误:', e)
      return '-'
    }
  }
  </script>
  
  <style scoped>
  .comment-node {
    margin-bottom: 16px;
    padding: 12px;
    background: #fff;
    border-radius: 4px;
    border: 1px solid #f0f0f0;
  }
  
  .comment-node.level-0 {
    margin-left: 0;
  }
  
  .comment-node.level-1 {
    margin-left: 24px;
    border-left: 2px solid #1890ff;
  }
  
  .comment-node.level-2 {
    margin-left: 48px;
    border-left: 2px solid #52c41a;
  }
  
  .comment-node.level-3,
  .comment-node.level-4,
  .comment-node.level-5 {
    margin-left: 72px;
    border-left: 2px solid #faad14;
  }
  
  .comment-header {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
  }
  
  .comment-author {
    font-weight: 500;
    color: #1890ff;
    margin-right: 12px;
  }
  
  .comment-time {
    color: #999;
    font-size: 12px;
    flex: 1;
  }
  
  .comment-actions {
    display: flex;
    gap: 8px;
  }
  
  .comment-text {
    color: #333;
    line-height: 1.5;
    word-break: break-word;
  }
  
  .comment-children {
    margin-top: 12px;
  }
  </style>