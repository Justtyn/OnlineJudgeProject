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
    // 这里可以添加token验证等逻辑
    // 例如: config.headers['Authorization'] = 'Bearer ' + token
    return config // 返回处理后的配置
}, error => {
    // 请求错误处理
    return Promise.reject(error) // 返回错误，进入catch
});

/**
 * 响应拦截器
 * 在接收到响应后执行，可以在此统一处理响应数据
 * 例如：错误处理、数据格式化等
 */
service.interceptors.response.use(
    response => {
        // 响应数据处理
        // 可以在这里统一处理响应状态码、提取响应数据等
        return response // 返回响应数据
    },
    error => {
        // 响应错误处理
        ElMessage.error(error.message || '请求失败') // 显示错误消息提示
        // 可以在这里根据错误状态码做不同处理
        // 例如：401未授权可以跳转到登录页
        return Promise.reject(error) // 返回错误，进入catch
    }
)

export default service // 导出axios实例，供其他模块使用