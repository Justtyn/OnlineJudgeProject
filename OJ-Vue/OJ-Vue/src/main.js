import './assets/main.css'
import 'animate.css'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

// 配置 NProgress
NProgress.configure({
    showSpinner: false,
    easing: 'ease',
    speed: 500
})

// 路由守卫
router.beforeEach((to, from, next) => {
    NProgress.start()
    next()
})

router.afterEach(() => {
    NProgress.done()
})

const app = createApp(App)

app.use(router)
app.use(ElementPlus)
app.use(Antd) // 使用 Ant Design Vue

app.mount('#app')

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}