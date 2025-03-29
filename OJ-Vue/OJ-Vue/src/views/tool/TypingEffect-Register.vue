<script setup>
import { ref, onMounted } from "vue";

// 定义要展示的文本
const texts = ["Welcome to XUJCOJ v0.1", "This is Register Page."];
const colors = [
  // "#ff5733", "#33ff57", "#3357ff", "#ff33a1", "#ffcc33", "#33ffee",
  // "#ff6633", "#99ff33", "#9933ff", "#ff3366", "#33ff99", "#ff33ff",
  // "#ff9900", "#00ff99", "#6600ff", "#ff0033", "#00ffff", "#ff6600"
    "#FFFFFF"
];

// 状态变量
const displayedText = ref([]);
const textIndex = ref(0);
const charIndex = ref(0);
const isDeleting = ref(false);

// 控制打字速度
const typingSpeed = 150; // 打字速度
const delayAfterTyping = 1000; // 打完一遍的停顿时间
const delayAfterDeleting = 500; // 删除前的停顿时间

// 处理打字效果
const type = () => {
  const currentText = texts[textIndex.value].split("");

  if (!isDeleting.value) {
    if (charIndex.value < currentText.length) {
      displayedText.value.push({ char: currentText[charIndex.value], color: colors[charIndex.value % colors.length] });
      charIndex.value++;
      setTimeout(type, typingSpeed);
    } else {
      setTimeout(() => {
        isDeleting.value = true;
        type();
      }, delayAfterTyping);
    }
  } else {
    if (charIndex.value > 0) {
      displayedText.value.pop();
      charIndex.value--;
      setTimeout(type, typingSpeed / 2);
    } else {
      isDeleting.value = false;
      textIndex.value = (textIndex.value + 1) % texts.length;
      setTimeout(type, delayAfterDeleting);
    }
  }
};

// 组件加载后启动打字机
onMounted(() => {
  type();
});
</script>

<template>
  <div class="typewriter-container">
    <span class="text">
      <span v-for="(item, index) in displayedText" :key="index" :style="{ color: item.color }">
        {{ item.char }}
      </span>
    </span>
    <span class="cursor"></span>
  </div>
</template>

<style scoped>
/* 页面居中布局 */
.typewriter-container {
  display: flex;
  align-items: center;
  font-size: 50px;
  font-weight: bold;
  font-family: "Courier New", monospace;
}

/* 光标动画 */
.cursor {
  display: inline-block;
  width: 10px;
  height: 30px;
  background-color: black;
  margin-left: 5px;
  animation: blink 0.8s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
</style>
