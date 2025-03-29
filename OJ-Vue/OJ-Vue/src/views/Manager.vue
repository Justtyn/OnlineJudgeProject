<template>
  <div class="manager-container">
    <el-container>
      <el-header class="header">
        <div class="header-box">
          <!-- 左侧 Logo 部分 -->
          <div class="header-body" style="flex-direction: row;flex: 1">
            <el-icon color="#409efc" class="is-loading" style="font-size: 35px; margin-right: 10px">
              <SwitchFilled/>
            </el-icon>
            <!-- 这里建议 Logo 链接指向首页 -->
            <router-link to="/" class="header-link">XUJCOJ</router-link>
          </div>

          <!-- 中间菜单部分 -->
          <div class="header-body" style="flex: 3">
            <div style="display: flex;width: 100%">
              <router-link to="/homePage" class="header-link-1">首页</router-link>
              <router-link to="/problemListPage" class="header-link-1">题库</router-link>
              <router-link to="/register" class="header-link-1">状态</router-link>
              <router-link to="/register" class="header-link-1">排名</router-link>
              <router-link to="/register" class="header-link-1">竞赛</router-link>
              <router-link to="/register" class="header-link-1">作业</router-link>
              <router-link to="/register" class="header-link-1">题解</router-link>
              <router-link to="/register" class="header-link-1">讨论</router-link>
              <router-link to="/register" class="header-link-1">公告</router-link>
              <router-link to="/register" class="header-link-1">关于</router-link>
            </div>
          </div>

          <!-- 右侧登录/注册 或退出登录链接 -->
          <div class="header-body" style="flex-direction: row-reverse;flex: 1">
            <template v-if="!token">
              <router-link to="/login" class="header-link">登录</router-link>
              <router-link to="/register" class="header-link">注册</router-link>
            </template>
            <template v-else>
              <router-link @click="logout" to="/login" class="header-link">退出登录</router-link>
            </template>
          </div>

        </div>
      </el-header>

      <el-container>
        <el-container>
          <el-main class="main">
            <router-view/>
          </el-main>

          <el-footer class="footer">
            <div style="display: flex; width: 100%; height: 100%; justify-content: center; align-items: center">
              <p style="font-size: 15px; color: #424242">
                Copyright&nbsp;&copy;&nbsp;2024&nbsp;-&nbsp;2025&nbsp;XUJC&nbsp;SWEU24025-焦梓豪
              </p>
            </div>
          </el-footer>
        </el-container>
      </el-container>
    </el-container>
  </div>
</template>

<script lang="ts" setup>
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {SwitchFilled} from "@element-plus/icons-vue";

const $route = useRoute()
const router = useRouter()

// 使用 computed 来动态获取 token，当 localStorage 变化时可触发重新计算
const token = computed(() => {
  const userStr = localStorage.getItem('student-user')
  const user = userStr ? JSON.parse(userStr) : null
  return user ? user.token : null
})

// 退出登录操作：清除用户信息后跳转到登录页
const logout = () => {
  localStorage.removeItem('student-user')
  router.push('/login')
}
</script>

<style scoped>
html, body {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden; /* 避免滚动条影响布局 */
}

.manager-container {
  height: 100vh;
  width: 100vw;
}

.header {
  width: 100vw;
  background: #23d5ab;
  height: 7vh;
}

.main {
  height: 87vh;
  background: white;
}

.footer {
  height: 6vh;
  background: #23d5ab;
}

.header-box {
  display: flex;
  align-items: center;
  flex-direction: row;
  height: 100%;
  width: 100%;
}

.header-body {
  display: flex;
  height: 100%;
  align-items: center;
  width: 100%;
}

.header-link, span, .header-link-1 {
  margin: 0 10px;
  font-size: 15px;
  color: #595757;
}
.header-link-1 {
  flex: 1;
}

a:hover, span:hover {
  background: none;
  color: black;
}

.item-style:hover {
  background-color: rgba(255, 255, 255, 0) !important;
}
</style>
