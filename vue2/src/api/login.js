import request from '@/utils/request'

// 登录接口
export function login(data) {
  return request({
    url: '/api/login',
    method: 'post',
    data: data
  })
}

// 注册接口
export function register(data) {
  return request({
    url: '/api/register',
    method: 'post',
    data: data
  })
}

// 管理员修改密码接口
export function changeAdminPassword(data) {
  return request({
    url: '/api/admin/change-password',
    method: 'post',
    data: data
  })
}