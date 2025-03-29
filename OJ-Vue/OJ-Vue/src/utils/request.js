import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建axios实例
const service = axios.create({
    baseURL: 'http://localhost:9090',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
service.interceptors.request.use(config => {
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
service.interceptors.response.use(
    response => {
        return response
    },
    error => {
        ElMessage.error(error.message || '请求失败')
        return Promise.reject(error)
    }
)

export default service