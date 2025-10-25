<template>
  <div class="admin-layout">
    <el-container>
      <!-- 移动端菜单按钮 -->
      <div class="mobile-menu-btn" @click="toggleMobileMenu" v-if="isMobile">
        <el-icon><Menu /></el-icon>
      </div>

      <!-- 侧边栏遮罩 -->
      <div class="sidebar-mask" v-if="isMobile && showMobileMenu" @click="toggleMobileMenu"></div>

      <!-- 侧边栏 -->
      <el-aside 
        :width="isMobile ? '100%' : (isCollapse ? '64px' : '220px')" 
        class="admin-aside"
        :class="{ 'mobile-sidebar': isMobile, 'show-mobile-menu': showMobileMenu }"
      >
        <div class="logo-container">
          <div class="logo-header">
            <el-icon color="#1890ff" class="logo-icon"><Monitor /></el-icon>
            <span class="logo-text" v-show="!isCollapse || isMobile">XUJCOJ 管理系统</span>
          </div>
          <div class="collapse-btn-container" v-if="!isMobile">
            <div class="collapse-btn" @click="toggleCollapse">
              <el-icon><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
            </div>
          </div>
        </div>
        <el-scrollbar>
          <el-menu
            :default-active="$route.path"
            class="admin-menu"
            @select="handleSelect"
            :router="true"
            background-color="#545c64"
            text-color="#fff"
            :collapse="isCollapse && !isMobile"
          >
            <el-menu-item index="/admin/dashboard">
              <el-icon><Odometer /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            
            <!-- 管理员专用菜单 -->
            <template v-if="userRole === 'ADMIN'">
              <el-menu-item index="/admin/users">
                <el-icon><User /></el-icon>
                <span>用户管理</span>
              </el-menu-item>
              
              <el-menu-item index="/admin/teachers">
                <el-icon><Avatar /></el-icon>
                <span>教师管理</span>
              </el-menu-item>
              
              <el-menu-item index="/admin/problems">
                <el-icon><Document /></el-icon>
                <span>题目管理</span>
              </el-menu-item>
            </template>
            
            <!-- 管理员和教师都可以访问的菜单 -->
            <el-menu-item index="/admin/classes">
              <el-icon><Reading /></el-icon>
              <span>班级管理</span>
            </el-menu-item>

            <el-sub-menu index="1">
              <template #title>
                <el-icon><Reading /></el-icon>
                <span>作业管理</span>
              </template>
              <el-menu-item index="/admin/homework">作业列表</el-menu-item>
            </el-sub-menu>

            <!-- 管理员专用菜单 -->
            <template v-if="userRole === 'ADMIN'">
              <el-sub-menu index="2">
                <template #title>
                  <el-icon><ChatDotRound /></el-icon>
                  <span>讨论管理</span>
                </template>
                <el-menu-item index="/admin/discussions">讨论列表</el-menu-item>
              </el-sub-menu>

              <el-sub-menu index="3">
                <template #title>
                  <el-icon><Bell /></el-icon>
                  <span>公告管理</span>
                </template>
                <el-menu-item index="/admin/announcements">公告列表</el-menu-item>
              </el-sub-menu>

              <el-sub-menu index="4">
                <template #title>
                  <el-icon><Collection /></el-icon>
                  <span>题解管理</span>
                </template>
                <el-menu-item index="/admin/solutions">题解列表</el-menu-item>
              </el-sub-menu>
            </template>
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <!-- 主要内容区 -->
      <el-container class="main-container">
        <el-header height="60px" class="admin-header">
          <div class="header-left">
            <back-button v-if="showBackButton" />
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ currentPath }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-button 
              v-if="userRole === 'ADMIN'"
              type="primary" 
              link 
              @click="goToHome"
              class="home-link"
            >
              <el-icon><House /></el-icon>
              返回主页
            </el-button>
            <el-divider direction="vertical" />
            <el-dropdown @command="handleCommand" class="user-dropdown">
              <span class="el-dropdown-link">
                <el-avatar :size="32" class="user-avatar">{{ adminUser.username.charAt(0) }}</el-avatar>
                <span class="username">{{ adminUser.username }}</span>
                <el-icon class="el-icon--right"><CaretBottom /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="admin-main">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  DataLine,
  User,
  Document,
  Reading,
  ChatDotRound,
  Bell,
  Collection,
  ArrowDown,
  Monitor,
  Odometer,
  CaretBottom,
  House,
  Fold,
  Expand,
  Menu,
  Avatar,
  School,
  Edit
} from '@element-plus/icons-vue'
import BackButton from '@/components/BackButton.vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

// 设置响应拦截器处理401错误
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      // 认证失败，自动退出登录
      localStorage.removeItem('student-user')
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

const activeMenu = ref(route.path)
const adminUser = computed(() => {
  const userStr = localStorage.getItem('student-user')
  return userStr ? JSON.parse(userStr) : {}
})

const userRole = computed(() => {
  return adminUser.value.role || 'STUDENT'
})

const currentPath = computed(() => {
  return route.meta.title || '仪表盘'
})

const showBackButton = computed(() => {
  return route.path !== '/admin/dashboard'
})

const isCollapse = ref(false)
const isMobile = ref(false)
const showMobileMenu = ref(false)

const handleSelect = (index: string) => {
  activeMenu.value = index
  if (isMobile.value) {
    showMobileMenu.value = false
  }
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    localStorage.removeItem('student-user')
    ElMessage.success('退出成功')
    router.push('/login')
  }
}

const goToHome = () => {
  router.push('/')  // 使用Vue Router进行页面跳转
}

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const toggleMobileMenu = () => {
  showMobileMenu.value = !showMobileMenu.value
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.admin-layout .el-container {
  height: 100%;
}

.admin-aside {
  background-color: #545c64;
  color: white;
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  transition: width 0.3s;
  position: relative;
}

.logo-container {
  height: auto;
  display: flex;
  flex-direction: column;
  background-color: #002140;
  white-space: nowrap;
  position: relative;
  padding: 10px 0;
}

.logo-header {
  display: flex;
  align-items: center;
  padding: 0 16px;
  white-space: nowrap;
  margin-bottom: 10px;
}

.collapse-btn-container {
  display: flex;
  justify-content: flex-end;
  width: 100%;
  padding-right: 16px;
}

.collapse-btn {
  width: 32px;
  height: 32px;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #fff;
  transition: all 0.3s;
  border-radius: 4px;
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #1890ff;
}

.collapse-btn .el-icon {
  font-size: 20px;
}

.logo-icon {
  font-size: 24px;
  margin-right: 12px;
  flex-shrink: 0;
}

.logo-text {
  color: white;
  font-size: 16px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: opacity 0.3s;
}

.admin-menu {
  border-right: none;
  background-color: #545c64;
}

.main-container {
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.admin-header {
  background-color: white;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.home-link {
  display: flex;
  align-items: center;
  gap: 4px;
}

.user-dropdown {
  height: 100%;
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 8px;
  height: 100%;
}

.el-dropdown-link:hover {
  background-color: rgba(0, 0, 0, 0.025);
}

.user-avatar {
  background-color: #1890ff;
  margin-right: 8px;
}

.username {
  margin: 0 8px;
  color: #333;
}

.admin-main {
  background-color: #f0f2f5;
  padding: 24px;
  height: calc(100vh - 60px);
  overflow: auto;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item) {
  margin: 4px 0;
  border-radius: 4px;
  margin-inline: 4px;
}

:deep(.el-sub-menu .el-menu-item) {
  min-width: 190px;
}

:deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
  color: white !important;
}

:deep(.el-menu-item:hover) {
  background-color: #001f3d !important;
  color: white !important;
}

:deep(.el-sub-menu__title) {
  color: #a6adb4 !important;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #001f3d !important;
  color: white !important;
}

:deep(.el-menu--collapse) {
  width: 64px;
}

:deep(.el-menu--collapse .el-menu-item),
:deep(.el-menu--collapse .el-sub-menu__title) {
  padding: 0 20px !important;
}

:deep(.el-menu--collapse .el-sub-menu__icon-arrow) {
  display: none;
}

.mobile-menu-btn {
  position: fixed;
  top: 14px;
  right: 16px;
  z-index: 1000;
  width: 36px;
  height: 36px;
  background: #1890ff;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #fff;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.mobile-menu-btn:hover {
  background: #40a9ff;
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.mobile-menu-btn:active {
  transform: scale(0.95);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.mobile-menu-btn .el-icon {
  font-size: 20px;
  transition: transform 0.3s;
}

.mobile-menu-btn:active .el-icon {
  transform: scale(0.9);
}

.sidebar-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 998;
}

.mobile-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  transform: translateX(-100%);
  transition: transform 0.3s;
  z-index: 999;
}

.show-mobile-menu {
  transform: translateX(0);
}

@media screen and (max-width: 768px) {
  .main-container {
    margin-left: 0 !important;
  }

  .admin-header {
    padding-right: 60px;
  }

  .logo-container {
    padding: 10px 0;
  }

  .logo-text {
    font-size: 18px;
  }

  .admin-header {
    height: 64px;
    padding: 0 20px;
  }

  .header-right {
    margin-right: 40px;
  }
}
</style> 