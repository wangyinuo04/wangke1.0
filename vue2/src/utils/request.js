import axios from 'axios'
import { Message } from 'element-ui'

// 创建axios实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || 'http://localhost:8088', // 必须改为8088
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    console.log('发送请求到:', config.baseURL + config.url)
    
    // 从localStorage或Vuex获取用户信息并添加到请求头
    const userInfoStr = localStorage.getItem('userInfo');
    if (userInfoStr) {
      try {
        const userInfo = JSON.parse(userInfoStr);
        if (userInfo.id) {
          config.headers['X-User-Id'] = userInfo.id;
        }
        if (userInfo.role) {
          config.headers['X-User-Role'] = userInfo.role;
        }
      } catch (e) {
        console.warn('解析用户信息失败:', e);
      }
    }
    
    return config
  },
  error => {
    console.log('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.log('响应错误详情:')
    console.log('错误对象:', error)
    console.log('错误状态:', error.response?.status)
    console.log('错误数据:', error.response?.data)
    
    if (error.code === 'ECONNABORTED') {
      Message.error('请求超时，请稍后重试')
    } else if (error.message === 'Network Error') {
      Message.error('网络连接失败，请检查：\n1. 后端服务是否启动 (端口8088)\n2. 是否在访问正确地址: http://localhost:8088')
    } else if (error.response) {
      Message.error(`服务器错误: ${error.response.status}`)
    }
    
    return Promise.reject(error)
  }
)

export default service