<template>
  <div style="height: 100%">
    <div class="card">
      <TypingEffect/>
    </div>
  </div>
</template>

<script setup>
import TypingEffect from "@/views/tool/TypingEffect-Main.vue";
</script>

<style scoped>
.card {
  line-height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  /* 添加细边框、圆角和阴影 */
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  /* 淡入动画效果 */
  animation: fadeIn 1s ease;
  transition: box-shadow 0.3s ease;
}

.card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
