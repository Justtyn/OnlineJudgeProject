import axios from 'axios' // 导入axios HTTP请求库
import { ElMessage, ElMessageBox } from 'element-plus' // 导入Element Plus的消息提示和消息框组件
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
    // 从localStorage获取用户信息，然后提取token
    const userStr = localStorage.getItem('student-user');
    const user = userStr ? JSON.parse(userStr) : null;
    const token = user ? user.token : null;

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
                    // token失效，清除本地用户数据
                    localStorage.removeItem('student-user')

                    // 显示过期提醒弹窗
                    ElMessageBox.alert('登录已过期，请重新登录', '登录过期', {
                        confirmButtonText: '重新登录',
                        type: 'warning',
                        showClose: false,
                        closeOnClickModal: false,
                        closeOnPressEscape: false
                    }).then(() => {
                        router.push('/login')
                    }).catch(() => {
                        router.push('/login')
                    })
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