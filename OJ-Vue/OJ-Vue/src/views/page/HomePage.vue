<template>
  <!-- 根容器，设置100%高度以填充父元素 -->
  <div style="height: 100%">
    <!-- 卡片容器，用于展示打字效果组件 -->
    <div class="card">
      <!-- 引入打字效果组件 -->
      <TypingEffect/>
    </div>
  </div>
</template>

<script setup>
// 导入打字效果组件
import TypingEffect from "@/views/tool/TypingEffect-Main.vue";
</script>

<style scoped>
/* 卡片样式定义 */
.card {
  /* 设置行高，确保文本垂直间距合适 */
  line-height: 30px;
  /* 使用 flex 布局使内容居中 */
  display: flex;
  justify-content: center;
  align-items: center;
  /* 设置卡片高度为100% */
  height: 100%;
  
  /* 卡片外观美化 */
  /* 添加细边框，使用半透明黑色 */
  border: 1px solid rgba(0, 0, 0, 0.1);
  /* 添加圆角效果 */
  border-radius: 8px;
  /* 添加阴影效果 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  /* 动画效果 */
  /* 页面加载时的淡入动画 */
  animation: fadeIn 1s ease;
  /* 阴影过渡效果 */
  transition: box-shadow 0.3s ease;
}

/* 鼠标悬停时增强阴影效果 */
.card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 定义淡入动画关键帧 */
@keyframes fadeIn {
  /* 动画起始状态 */
  from {
    opacity: 0;          /* 完全透明 */
    transform: scale(0.95); /* 略微缩小 */
  }
  /* 动画结束状态 */
  to {
    opacity: 1;          /* 完全不透明 */
    transform: scale(1);   /* 原始大小 */
  }
}

/* 添加移动端适配样式 */
@media screen and (max-width: 768px) {
  .card {
    padding: 15px;
    line-height: 24px;
  }
  
  :deep(.typewriter-container) {
    font-size: 24px;
  }
  
  :deep(.cursor) {
    width: 5px;
    height: 20px;
    margin-left: 3px;
  }
}
</style>
