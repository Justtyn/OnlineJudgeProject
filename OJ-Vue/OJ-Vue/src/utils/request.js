import axios from 'axios' // 导入axios HTTP请求库
import { ElMessage } from 'element-plus' // 导入Element Plus的消息提示组件
import router from '../router' // 导入Vue路由实例

/**
 * 创建axios实例
 * 配置基本请求参数，如基础URL、超时时间和请求头
 */
const service = axios.create({
    baseURL: 'http://localhost:9090', // 设置API的基础URL
    timeout: 10000, // 设置请求超时时间为10秒
    headers: {
        'Content-Type': 'application/json' // 设置默认请求头为JSON格式
    }
})

/**
 * 请求拦截器
 * 在发送请求之前执行，可以在此处理请求配置
 * 例如：添加token、修改请求参数等
 */
service.interceptors.request.use(config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
        config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
}, error => {
    return Promise.reject(error)
});

/**
 * 响应拦截器
 * 在接收到响应后执行，可以在此统一处理响应数据
 * 例如：错误处理、数据格式化等
 */
service.interceptors.response.use(
    response => {
        return response
    },
    error => {
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    // token失效，清除本地token并跳转到登录页
                    localStorage.removeItem('token')
                    ElMessage.error('登录已过期，请重新登录')
                    router.push('/login')
                    break
                default:
                    ElMessage.error(error.response.data.message || '请求失败')
            }
        } else {
            ElMessage.error('网络错误，请稍后重试')
        }
        return Promise.reject(error)
    }
)

export default service