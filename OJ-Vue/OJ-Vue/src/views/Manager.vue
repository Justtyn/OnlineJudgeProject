<template>
  <div class="manager-container">
    <!-- 添加代码背景装饰 -->
    <div class="code-background">
      <div class="code-line" v-for="(line, index) in codeLines" :key="index" :style="{ left: `${line.left}%`, animationDelay: `${line.delay}s` }">
        <span class="code-keyword">{{ line.keyword }}</span>
        <span class="code-text">{{ line.text }}</span>
      </div>
    </div>
    
    <!-- 添加终端风格装饰 -->
    <div class="terminal-decoration">
      <div class="terminal-line top"></div>
      <div class="terminal-line bottom"></div>
      <div class="terminal-corner top-left"></div>
      <div class="terminal-corner top-right"></div>
      <div class="terminal-corner bottom-left"></div>
      <div class="terminal-corner bottom-right"></div>
    </div>
    
    <!-- 添加 ASCII 艺术 -->
    <div class="ascii-art">
      <pre>
   _____ _    _ _____  ______ 
  / ____| |  | |  __ \|  ____|
 | (___ | |  | | |__) | |__   
  \___ \| |  | |  _  /|  __|  
  ____) | |__| | | \ \| |____ 
 |_____/ \____/|_|  \_\______|
      </pre>
    </div>
    
    <el-container>
      <el-header class="header">
        <div class="header-box">
          <!-- 左侧 Logo 部分 -->
          <div class="header-body logo-section">
            <div class="logo-container">
              <el-icon class="logo-icon code-icon">
                <Document />
              </el-icon>
              <el-icon class="logo-icon check-icon">
                <Check />
              </el-icon>
            </div>
            <router-link to="/" class="logo-text">XUJCOJ</router-link>
          </div>

          <!-- 中间菜单部分 -->
          <div class="header-body nav-section" :class="{ 'nav-active': isMenuOpen }">
            <nav class="nav-links">
              <router-link to="/homePage" class="nav-link" @click="closeMenu">首页</router-link>
              <router-link to="/stats" class="nav-link" @click="closeMenu">统计</router-link>
              <router-link to="/problemListPage" class="nav-link" @click="closeMenu">题库</router-link>
              <router-link to="/statusListPage" class="nav-link" @click="closeMenu">状态</router-link>
              <router-link to="/rankPage" class="nav-link" @click="closeMenu">排名</router-link>
              <router-link to="/homeworkPage" class="nav-link" @click="closeMenu">作业</router-link>
              <router-link to="/solutionPage" class="nav-link" @click="closeMenu">题解</router-link>
              <router-link to="/discussPage" class="nav-link" @click="closeMenu">讨论</router-link>
              <router-link to="/announcementPage" class="nav-link" @click="closeMenu">公告</router-link>
              <router-link to="/about" class="nav-link" @click="closeMenu">关于</router-link>
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
                    <el-dropdown-item v-if="userRole === 'ADMIN'" command="adminCenter">
                      管理系统
                    </el-dropdown-item>
                    <el-dropdown-item v-if="userRole === 'STUDENT'"  command="userCenter">个人主页</el-dropdown-item>
                    <el-dropdown-item v-if="userRole === 'STUDENT'"  command="userClass">我的班级</el-dropdown-item>
                    <!-- <el-dropdown-item command="favorites">我的收藏</el-dropdown-item> -->
                    <el-dropdown-item command="solutions">我的题解</el-dropdown-item>
                    <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <!-- 汉堡菜单按钮 -->
            <div class="menu-toggle" @click="toggleMenu">
              <el-icon :size="24">
                <Menu />
              </el-icon>
            </div>
          </div>
        </div>
      </el-header>

      <el-container>
        <el-container>
          <el-main class="main">
            <router-view v-slot="{ Component }">
              <transition name="fade" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
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
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { SwitchFilled, Menu, ArrowDown, Document, Check } from '@element-plus/icons-vue'

const router = useRouter()
const isMenuOpen = ref(false)

// 代码背景装饰
const codeLines = ref([
  { keyword: 'const', text: ' solve = () => {', left: 10, delay: 0 },
  { keyword: 'return', text: ' "AC";', left: 20, delay: 0.2 },
  { keyword: '};', text: '', left: 30, delay: 0.4 },
  { keyword: 'function', text: ' judge() {', left: 40, delay: 0.6 },
  { keyword: 'return', text: ' true;', left: 50, delay: 0.8 },
  { keyword: '}', text: '', left: 60, delay: 1.0 },
  { keyword: 'class', text: ' Solution {', left: 70, delay: 1.2 },
  { keyword: 'public', text: ' static void main() {', left: 80, delay: 1.4 },
  { keyword: 'System.out.println', text: '("Hello OJ");', left: 90, delay: 1.6 }
])

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

// 获取用户角色
const userRole = computed<string>(() => {
  try {
    const userStr = localStorage.getItem('student-user')
    if (userStr) {
      const user = JSON.parse(userStr)
      return user?.role || 'STUDENT'
    }
    return 'STUDENT'
  } catch (error) {
    console.error('解析用户角色出错：', error)
    return 'STUDENT'
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
    case 'adminCenter':
      router.push('/admin/dashboard')
      break
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

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}

const closeMenu = () => {
  isMenuOpen.value = false
}

const handleNavCommand = (command: string) => {
  switch (command) {
    case 'charts':
      router.push('/charts')
      break
    case 'stats':
      router.push('/stats')
      break
  }
  closeMenu()
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
  position: relative;
}

.header {
  width: 100%;
  background: white;
  height: 64px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  z-index: 10;
}

.main {
  margin-top: 64px;
  min-height: calc(100vh - 130px);
  padding: 24px;
  background: white;
  position: relative;
  z-index: 2;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
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
  position: relative;
}

.logo-section {
  display: flex;
  align-items: center;
  min-width: 150px;
  z-index: 2;
  transition: all 0.3s ease;
}

.logo-container {
  position: relative;
  margin-right: 12px;
  display: flex;
  align-items: center;
}

.logo-icon {
  font-size: 24px;
  transition: all 0.3s ease;
}

.code-icon {
  color: #1890ff;
  transform: rotate(-15deg);
}

.check-icon {
  color: #52c41a;
  position: absolute;
  right: -8px;
  bottom: -8px;
  font-size: 16px;
  background: white;
  border-radius: 50%;
  padding: 2px;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: #1a1a1a;
  text-decoration: none;
  position: relative;
  padding: 4px 0;
  transition: all 0.3s ease;
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
}

.logo-text::after {
  content: ';';
  position: absolute;
  right: -15px;
  opacity: 0;
  transition: all 0.3s ease;
}

.logo-text:hover {
  color: #1890ff;
}

.logo-text:hover::after {
  opacity: 1;
  right: -10px;
}

.logo-container:hover .code-icon {
  transform: rotate(0deg);
}

.logo-container:hover .check-icon {
  transform: scale(1.2);
}

.menu-toggle {
  display: none;
  cursor: pointer;
  padding: 8px;
  background: #f5f5f5;
  border-radius: 8px;
  transition: all 0.3s ease;
  margin-left: 8px;
  z-index: 2;
}

.menu-toggle:hover {
  background: #e6e6e6;
  transform: scale(1.05);
}

.menu-toggle .el-icon {
  color: #2c3e50;
  font-size: 24px;
}

.nav-section {
  flex: 1;
  display: flex;
  justify-content: center;
  transition: all 0.3s ease;
}

.nav-links {
  display: flex;
  gap: 24px;
  justify-content: center;
  transition: all 0.3s ease;
}

.nav-link {
  color: #2c3e50;
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 4px;
  transition: color 0.3s ease, background-color 0.3s ease;
  position: relative;
  overflow: visible;
  font-family: 'Courier New', monospace;
  letter-spacing: 0.5px;
  padding-left: 20px;
}

.nav-link:hover {
  color: #0056b3;
  background: rgba(0, 86, 179, 0.1);
  font-weight: 600;
}

.nav-link::before {
  content: '>';
  position: absolute;
  left: 0;
  opacity: 0;
  transition: all 0.3s ease;
  color: #1890ff;
  font-weight: bold;
}

.nav-link:hover::before {
  opacity: 1;
  text-shadow: 0 0 5px rgba(24, 144, 255, 0.5);
}

.nav-dropdown {
  display: inline-block;
}

.nav-dropdown .nav-link {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 100%;
  line-height: 1;
  cursor: pointer;
}

.nav-dropdown .el-icon {
  margin-left: 4px;
  font-size: 12px;
  transition: transform 0.3s;
}

.nav-dropdown:hover .el-icon {
  transform: rotate(180deg);
}

.dropdown-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-section {
  display: flex;
  gap: 16px;
  min-width: 150px;
  justify-content: flex-end;
  align-items: center;
  z-index: 2;
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
    display: flex;
  }
  
  .header-box {
    padding: 0 16px;
  }
  
  .auth-section {
    gap: 8px;
  }
}

@media screen and (max-width: 1024px) {
  .nav-links {
    gap: 16px;
  }
  
  .nav-link {
    font-size: 14px;
    padding: 8px 10px;
  }
  
  .auth-link {
    font-size: 14px;
    padding: 8px 12px;
  }
}

@media screen and (max-width: 768px) {
  .header-box {
    padding: 0 16px;
  }

  .menu-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
  }

  .nav-section {
    position: fixed;
    top: 64px;
    left: 0;
    width: 100%;
    height: calc(100vh - 64px);
    background: white;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    opacity: 0;
    visibility: hidden;
    transition: opacity 0.3s ease, visibility 0.3s ease;
    z-index: 1;
  }

  .nav-section.nav-active {
    opacity: 1;
    visibility: visible;
  }

  .nav-links {
    flex-direction: column;
    align-items: center;
    gap: 16px;
    width: 100%;
  }

  .nav-link {
    width: 100%;
    text-align: center;
    padding: 12px;
    font-size: 16px;
    border-radius: 8px;
    background: #f5f5f5;
    transition: all 0.3s ease;
  }

  .nav-link:hover {
    background: #e6e6e6;
    transform: translateY(-2px);
  }

  .logo-section {
    min-width: auto;
  }

  .auth-section {
    min-width: auto;
    gap: 8px;
  }

  .auth-link {
    padding: 8px 12px;
    font-size: 14px;
  }
}

@media screen and (max-width: 480px) {
  .header-box {
    padding: 0 12px;
  }

  .logo-icon {
    font-size: 22px;
  }
  
  .check-icon {
    font-size: 14px;
  }
  
  .logo-text {
    font-size: 18px;
  }

  .auth-section {
    gap: 4px;
  }

  .auth-link {
    padding: 6px 10px;
    font-size: 13px;
  }

  .menu-toggle {
    width: 36px;
    height: 36px;
  }
}

/* 代码背景装饰样式 */
.code-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  opacity: 0.1;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.02) 0%, rgba(24, 144, 255, 0.04) 100%);
}

.code-line {
  position: absolute;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  white-space: nowrap;
  animation: floatUp 25s linear infinite;
  opacity: 0;
  text-shadow: 0 0 5px rgba(24, 144, 255, 0.2);
}

.code-keyword {
  color: #f56c6c;
  font-weight: bold;
}

.code-text {
  color: #1890ff;
}

@keyframes floatUp {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 0.6;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    transform: translateY(-100px) rotate(3deg);
    opacity: 0;
  }
}

/* 终端风格装饰 */
.terminal-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.terminal-line {
  position: absolute;
  background: linear-gradient(90deg, transparent, #1890ff, transparent);
  opacity: 0.3;
}

.terminal-line.top {
  top: 0;
  width: 100%;
  height: 2px;
}

.terminal-line.bottom {
  bottom: 0;
  width: 100%;
  height: 2px;
}

.terminal-corner {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid #1890ff;
  opacity: 0.3;
}

.terminal-corner.top-left {
  top: 0;
  left: 0;
  border-right: none;
  border-bottom: none;
}

.terminal-corner.top-right {
  top: 0;
  right: 0;
  border-left: none;
  border-bottom: none;
}

.terminal-corner.bottom-left {
  bottom: 0;
  left: 0;
  border-right: none;
  border-top: none;
}

.terminal-corner.bottom-right {
  bottom: 0;
  right: 0;
  border-left: none;
  border-top: none;
}

/* ASCII 艺术 */
.ascii-art {
  position: fixed;
  bottom: 20px;
  right: 20px;
  font-family: 'Courier New', monospace;
  color: #1890ff;
  opacity: 0.6;
  z-index: 1;
  font-size: 10px;
  line-height: 1.2;
  pointer-events: none;
  text-shadow: 0 0 5px rgba(24, 144, 255, 0.3);
}

/* 增强终端风格边框 */
.manager-container::before,
.manager-container::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, transparent, #1890ff, transparent);
  opacity: 0.5;
  box-shadow: 0 0 10px rgba(24, 144, 255, 0.3);
}
</style>
