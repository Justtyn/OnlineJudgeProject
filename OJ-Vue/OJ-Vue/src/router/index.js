// src/router/index.js

// 引入 Vue Router 库用于路由管理
import { createRouter, createWebHistory } from 'vue-router'

// 创建路由实例
const router = createRouter({
    // 使用 hash 模式的历史记录，导入环境变量中的 BASE_URL 配置
    history: createWebHistory(
        import.meta.env.BASE_URL),

    // 配置路由规则
    routes: [{
            path: '/', // 根路径
            name: 'Manager', // 路由名称
            component: () =>
                import ('../views/Manager.vue'), // 懒加载组件 Manager.vue
            redirect: '/homePage', // 默认重定向到 homePage 页面
            children: [{
                    path: 'homePage', // 子路由路径
                    name: 'HomePage', // 子路由名称
                    component: () =>
                        import ('@/views/page/HomePage.vue') // 懒加载组件 HomePage.vue
                },
                {
                    path: 'problemListPage', // 子路由路径
                    name: 'ProblemListPage', // 子路由名称
                    component: () =>
                        import ('@/views/page/ProblemListPage.vue') // 懒加载组件 ProblemListPage.vue
                },
                {
                    path: 'problem/:id', // 动态路由参数 id
                    name: 'ProblemDetail', // 子路由名称
                    component: () =>
                        import ('@/views/page/ProblemDetailPage.vue') // 懒加载组件 ProblemDetailPage.vue
                },
                {
                    path: 'classProblem/:id', // 动态路由参数 id
                    name: 'ClassProblemDetail', // 子路由名称
                    component: () =>
                        import ('@/views/page/ClassProblemDetailPage.vue') // 懒加载组件 ClassProblemDetail.vue
                },
                {
                    path: 'profilePage', // 子路由路径
                    name: 'ProfilePage', // 子路由名称
                    component: () =>
                        import ('@/views/page/ProfilePage.vue') // 懒加载组件 ProfilePage.vue
                },
                {
                    path: 'submitPage/:problemId/:problemName/:sampleInput/:sampleOutput', // 添加样例输入和输出参数
                    name: 'SubmitPage', // 子路由名称
                    component: () =>
                        import ('@/views/page/SubmitPage.vue') // 懒加载组件 SubmitPage.vue
                },
                {
                    path: 'classSubmitPage/:problemId/:problemName/:sampleInput/:sampleOutput', // 添加样例输入和输出参数
                    name: 'ClassSubmitPage', // 子路由名称
                    component: () =>
                        import ('@/views/page/ClassSubmitPage.vue') // 懒加载组件 SubmitPage.vue
                },
                {
                    path: 'statusListPage', // 状态页面路径
                    name: 'StatusListPage', // 状态页面路由名称
                    component: () =>
                        import ('@/views/page/StatusListPage.vue') // 懒加载组件 StatusListPage.vue
                },
                {
                    path: 'announcementPage', // 公告页面路径
                    name: 'AnnouncementPage', // 公告页面路由名称
                    component: () =>
                        import ('@/views/page/AnnouncementPage.vue') // 懒加载组件 AnnouncementPage.vue
                },
                {
                    path: 'rankPage', // 排名页面路径
                    name: 'RankPage', // 排名页面路由名称
                    component: () =>
                        import ('@/views/page/RankPage.vue') // 懒加载组件 AnnouncementPage.vue
                },
                {
                    path: 'userProfile/:id',
                    name: 'UserProfile',
                    component: () =>
                        import ('@/views/page/UserProfilePage.vue')
                },
                {
                    path: 'userClass', // 用户班级页面路径
                    name: 'UserClass', // 用户班级页面路由名称
                    component: () =>
                        import ('@/views/page/UserClass.vue') // 懒加载组件 UserClass.vue
                },
                {
                    path: 'class/:id',
                    name: 'ClassDetail',
                    component: () =>
                        import ('@/views/page/ClassHomework.vue')
                },
                {
                    path: 'class/:id/homework',
                    name: 'ClassHomework',
                    component: () =>
                        import ('@/views/page/ClassHomework.vue')
                },
                {
                    path: 'homework/:id',
                    name: 'HomeworkProblem',
                    component: () =>
                        import ('@/views/page/HomeworkProblem.vue')
                },
                {
                    path: 'homeworkPage',
                    name: 'HomeworkPage',
                    component: () =>
                        import ('@/views/page/HomeworkPage.vue')
                },
                {
                    path: 'about',
                    name: 'About',
                    component: () =>
                        import ('@/views/page/About.vue')
                },
                {
                    path: 'solutionPage',
                    name: 'SolutionPage',
                    component: () =>
                        import ('@/views/page/SolutionPage.vue')
                },
                {
                    path: 'solution/:id', // 添加题解详情页路由
                    name: 'SolutionDetail',
                    component: () =>
                        import ('@/views/page/SolutionDetail.vue')
                },
                {
                    path: 'statusDetail/:id', // 状态详情页面路径
                    name: 'StatusDetail', // 状态详情页面路由名称
                    component: () =>
                        import ('@/views/page/StatusDetailPage.vue') // 懒加载组件 StatusDetailPage.vue
                },
                {
                    path: 'discussPage',
                    name: 'DiscussPage',
                    component: () =>
                        import ('@/views/page/DiscussPage.vue')
                },
                {
                    path: 'discuss/:id',
                    name: 'DiscussDetail',
                    component: () =>
                        import ('@/views/page/DiscussDetail.vue')
                },
                {
                    path: 'stats',
                    name: 'Stats',
                    component: () =>
                        import ('@/views/page/Stats.vue')
                },
                {
                    path: 'charts',
                    name: 'Charts',
                    component: () =>
                        import ('@/views/page/Charts.vue')
                },
                {
                    path: 'dailyChallenge',
                    name: 'DailyChallenge',
                    component: () =>
                        import ('@/views/page/DailyChallenge.vue')
                }
            ]
        },
        // 管理系统作为独立路由
        {
            path: '/admin',
            name: 'AdminLayout',
            component: () =>
                import ('@/views/admin/AdminLayout.vue'),
            meta: { requiresAuth: true, requiresAdmin: true },
            redirect: '/admin/dashboard',
            children: [{
                    path: 'dashboard',
                    name: 'AdminDashboard',
                    component: () =>
                        import ('@/views/admin/Dashboard.vue'),
                    meta: { title: '仪表盘' }
                },
                {
                    path: 'users',
                    name: 'UserManagement',
                    component: () =>
                        import ('@/views/admin/Users.vue'),
                    meta: { title: '用户管理' }
                },
                {
                    path: 'problems',
                    name: 'ProblemManagement',
                    component: () =>
                        import ('@/views/admin/Problems.vue'),
                    meta: { title: '题目管理' }
                },
                {
                    path: 'problem/add',
                    name: 'ProblemAdd',
                    component: () =>
                        import ('@/views/admin/ProblemEdit.vue'),
                    meta: { title: '新增题目' }
                },
                {
                    path: 'problem/edit/:id',
                    name: 'ProblemEdit',
                    component: () =>
                        import ('@/views/admin/ProblemEdit.vue'),
                    meta: { title: '编辑题目' }
                },
                {
                    path: 'homework',
                    name: 'HomeworkManagement',
                    component: () =>
                        import ('@/views/admin/Homework.vue'),
                    meta: { title: '作业管理' }
                },
                {
                    path: 'discussions',
                    name: 'DiscussionManagement',
                    component: () =>
                        import ('@/views/admin/Discussions.vue'),
                    meta: { title: '讨论管理' }
                },
                {
                    path: 'announcements',
                    name: 'AnnouncementManagement',
                    component: () =>
                        import ('@/views/admin/Announcements.vue'),
                    meta: { title: '公告管理' }
                },
                {
                    path: 'solutions',
                    name: 'SolutionManagement',
                    component: () =>
                        import ('@/views/admin/Solutions.vue'),
                    meta: { title: '题解管理' }
                },
                {
                    path: 'classes',
                    name: 'ClassManagement',
                    component: () =>
                        import ('@/views/admin/Classes.vue'),
                    meta: { title: '课程管理' }
                },
                {
                    path: 'statistics',
                    name: 'Statistics',
                    component: () =>
                        import ('@/views/admin/Statistics.vue'),
                    meta: { title: '数据统计' }
                },
                {
                    path: 'status',
                    name: 'StatusManagement',
                    component: () =>
                        import ('@/views/admin/Status.vue'),
                    meta: { title: '提交状态管理' }
                }
            ]
        },
        {
            path: '/login', // 登录页路径
            name: 'Login', // 登录页路由名称
            component: () =>
                import ('@/views/login/Login.vue'), // 懒加载组件 Login.vue
        },
        {
            path: '/register', // 注册页路径
            name: 'Register', // 注册页路由名称
            component: () =>
                import ('@/views/login/Register.vue'), // 懒加载组件 Register.vue
        },
        {
            path: '/403',
            name: '403',
            component: () =>
                import ('@/views/error/403.vue')
        }
    ],
    // 添加滚动行为
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition
        } else {
            return { top: 0 }
        }
    }
})

// 导出创建好的路由实例
export default router

// 路由守卫：在每次路由跳转之前执行
router.beforeEach((to, from, next) => {
    const userStr = localStorage.getItem('student-user');
    const user = userStr ? JSON.parse(userStr) : null;
    const token = user ? user.token : null;

    // 检查是否需要管理员权限
    if (to.matched.some(record => record.meta.requiresAdmin)) {
        if (!user || user.role !== 'ADMIN') {
            console.log('需要管理员权限');
            return next('/403');
        }
    }

    // 原有的登录检查逻辑
    if (to.name !== 'Login' && to.name !== 'Register' && !token) {
        console.log('用户未登录，重定向到登录页');
        localStorage.removeItem('student-user');
        return next({ name: 'Login' });
    }

    if ((to.name === 'Login' || to.name === 'Register') && token) {
        // 如果是管理员，重定向到管理系统
        if (user.role === 'ADMIN') {
            return next('/admin/dashboard');
        }
        return next({ path: '/' });
    }

    next();
})

// 检查 token 是否过期的函数
function isTokenExpired(token) {
    if (!token) return true; // 如果没有 token，认为已过期
    try {
        // JWT 格式：header.payload.signature，获取第二部分并解码
        const payload = JSON.parse(atob(token.split('.')[1]));
        // 获取当前时间戳（单位：秒）
        const currentTime = Math.floor(Date.now() / 1000);
        // 如果当前时间大于或等于 token 中的 exp 时间，表示 token 已过期
        return currentTime >= payload.exp;
    } catch (e) {
        // 如果解析失败，认为 token 无效或已过期
        return true;
    }
}