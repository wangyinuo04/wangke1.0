import request from '@/utils/request'

// 发送验证码
export function sendCode(data) {
  return request({
    url: '/api/retrieve/sendCode',
    method: 'post',
    data: data
  })
}

// 验证验证码
export function verifyCode(data) {
  return request({
    url: '/api/retrieve/verifyCode',
    method: 'post',
    data: data
  })
}

// 重置密码
export function resetPassword(data) {
  return request({
    url: '/api/retrieve/resetPassword',
    method: 'post',
    data: data
  })
}

// 检查用户
export function checkUser(data) {
  return request({
    url: '/api/retrieve/checkUser',
    method: 'post',
    data: data
  })
}