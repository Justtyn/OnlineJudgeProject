import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
    history: createWebHashHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'Manager',
            component: () => import('../views/Manager.vue'),
            redirect: '/homePage',
            children: [
                { path: 'homePage', name: 'HomePage', component: () => import('@/views/page/HomePage.vue') },
                { path: 'problemListPage', name: 'ProblemListPage', component: () => import('@/views/page/ProblemListPage.vue') },
                { path: 'problem/:id', name: 'ProblemDetail', component: () => import('@/views/page/ProblemDetailPage.vue') },
                { path: 'profilePage', name: 'ProfilePage', component: () => import('@/views/page/ProfilePage.vue') },
            ]
        },
        {
            path: '/login',
            name: 'Login',
            component: () => import('@/views/login/Login.vue'),
        },
        {
            path: '/register',
            name: 'Register',
            component: () => import('@/views/login/Register.vue'),
        },
    ],
})

export default router

router.beforeEach((to, from, next) => {
    const userStr = localStorage.getItem('student-user');
    const user = userStr ? JSON.parse(userStr) : null;
    const token = user ? user.token : null;

    // 如果目标路由不是登录页和注册页，且 token 不存在，则重定向到登录页
    if (to.name !== 'Login' && to.name !== 'Register' && !token) {
        console.log('用户未登录，重定向到登录页');
        localStorage.removeItem('student-user');
        return next({ name: 'Login' });
    }

    // 如果用户已登录且尝试访问登录页，则重定向到主页
    if ((to.name === 'Login' || to.name === 'Register') && token) {
        return next({ path: '/' });
    }

    next();
})

function isTokenExpired(token) {
    if (!token) return true;
    try {
        // JWT 格式：header.payload.signature，取第二部分并解码
        const payload = JSON.parse(atob(token.split('.')[1]));
        // 获取当前时间戳（单位：秒）
        const currentTime = Math.floor(Date.now() / 1000);
        // 如果当前时间大于或等于 exp，则表示 token 已经过期
        return currentTime >= payload.exp;
    } catch (e) {
        // 如果解析失败，认为 token 无效或已过期
        return true;
    }
}
