<template>
  <div class="manager-container">
    <el-container>
      <el-header class="header">
        <div class="header-box">
          <!-- 左侧 Logo 部分 -->
          <div class="header-body logo-section">
            <el-icon color="#1890ff" class="logo-icon">
              <SwitchFilled/>
            </el-icon>
            <router-link to="/" class="logo-text">XUJCOJ</router-link>
          </div>

          <!-- 中间菜单部分 -->
          <div class="header-body nav-section">
            <nav class="nav-links">
              <router-link to="/homePage" class="nav-link">首页</router-link>
              <router-link to="/problemListPage" class="nav-link">题库</router-link>
              <router-link to="/statusListPage" class="nav-link">状态</router-link>
              <router-link to="/rankPage" class="nav-link">排名</router-link>
              <router-link to="/homePage" class="nav-link">竞赛</router-link>
              <router-link to="/homeworkPage" class="nav-link">作业</router-link>
              <router-link to="/homePage" class="nav-link">题解</router-link>
              <router-link to="/homePage" class="nav-link">讨论</router-link>
              <router-link to="/announcementPage" class="nav-link">公告</router-link>
              <router-link to="/homePage" class="nav-link">关于</router-link>
            </nav>
          </div>

          <!-- 右侧登录/注册或用户下拉菜单部分 -->
          <div class="header-body auth-section">
            <template v-if="!token">
              <router-link to="/login" class="auth-link">登录</router-link>
              <router-link to="/register" class="auth-link">注册</router-link>
            </template>
            <template v-else>
              <el-dropdown @command="handleCommand">
                <span class="el-dropdown-link auth-link">
                  {{ username }}<i class="el-icon-arrow-down el-icon--right"></i>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="userCenter">用户中心</el-dropdown-item>
                    <el-dropdown-item command="userClass">我的班级</el-dropdown-item>
                    <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                    <el-dropdown-item command="solutions">我的题解</el-dropdown-item>
                    <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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
            <div class="footer-content">
              <p class="copyright-text">
                Copyright © 2024 - 2025 XUJC SWEU24025-焦梓豪
              </p>
            </div>
          </el-footer>
        </el-container>
      </el-container>
    </el-container>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { SwitchFilled } from '@element-plus/icons-vue'

const router = useRouter()

// 获取 token（从 localStorage 中解析 student-user 对象）
const token = computed<string | null>(() => {
  try {
    const userStr = localStorage.getItem('student-user')
    if (userStr) {
      const user = JSON.parse(userStr)
      return user?.token || null
    }
    return null
  } catch (error) {
    console.error('解析 token 出错：', error)
    return null
  }
})

// 获取当前用户名（从 localStorage 中解析 student-user 对象）
const username = computed<string>(() => {
  try {
    const userStr = localStorage.getItem('student-user')
    if (userStr) {
      const user = JSON.parse(userStr)
      return user?.username || '用户'
    }
    return '用户'
  } catch (error) {
    console.error('解析用户名出错：', error)
    return '用户'
  }
})

// 退出登录：清除 localStorage 中的用户信息并跳转到登录页
const logout = () => {
  localStorage.removeItem('student-user')
  router.push('/login')
}

// 下拉菜单命令处理，根据选项执行相应跳转或退出登录
const handleCommand = (command: string) => {
  switch (command) {
    case 'userCenter':
      router.push('/profilePage')
      break
    case 'favorites':
      router.push('/homePage')
      break
    case 'userClass':
      router.push('/userClass')
      break
    case 'solutions':
      router.push('/homePage')
      break
    case 'logout':
      logout()
      break
    default:
      break
  }
}
</script>

<style scoped>
:root {
  --primary-color: #1890ff;
  --secondary-color: #f5f5f5;
  --text-color: #333;
  --hover-color: #40a9ff;
}

html, body {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.manager-container {
  height: 100vh;
  width: 100vw;
  background-color: var(--secondary-color);
}

.header {
  width: 100%;
  background: white;
  height: 64px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  z-index: 1000;
}

.main {
  margin-top: 64px;
  min-height: calc(100vh - 130px);
  padding: 24px;
  background: white;
}

.footer {
  height: 60px;
  background: white;
  border-top: 1px solid #e8e8e8;
}

.header-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
}

.logo-section {
  display: flex;
  align-items: center;
  min-width: 150px;
}

.logo-icon {
  font-size: 28px;
  margin-right: 12px;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: #1a1a1a;
  text-decoration: none;
}

.nav-section {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-links {
  display: flex;
  gap: 24px;
  justify-content: center;
}

.nav-link {
  color: #2c3e50;
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 4px;
  transition: color 0.3s ease, background-color 0.3s ease;
}

.nav-link:hover {
  color: #0056b3;
  background: rgba(0, 86, 179, 0.1);
  font-weight: 600;
}

.auth-section {
  display: flex;
  gap: 16px;
  min-width: 150px;
  justify-content: flex-end;
}

.auth-link {
  color: #2c3e50;
  text-decoration: none;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 4px;
  transition: color 0.3s ease, background-color 0.3s ease;
  cursor: pointer;
  background: none;
  border: none;
  font-size: 15px;
}

.auth-link:not(.logout-btn):hover {
  color: #0056b3;
  background: rgba(0, 86, 179, 0.1);
  font-weight: 600;
}

.logout-btn {
  background-color: #f56c6c;
  color: white !important;
  font-weight: 500;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background-color: #e64242;
  color: white !important;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.footer-content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.copyright-text {
  font-size: 14px;
  color: #666;
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }
  
  .header-box {
    padding: 0 16px;
  }
  
  .auth-section {
    gap: 8px;
  }
}
</style>
