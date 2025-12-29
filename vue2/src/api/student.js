import request from '@/utils/request'

/**
 * 学生管理API
 */

// 获取学生列表（支持搜索）
export function getStudentList(keyword = '') {
  return request({
    url: '/api/student/list',
    method: 'get',
    params: { keyword }
  })
}

// 更新学生信息
export function updateStudent(studentData) {
  return request({
    url: '/api/student/update',
    method: 'put',
    data: studentData
  })
}

// 切换账号状态（启用/禁用）
export function toggleStudentStatus(studentId) {
  return request({
    url: `/api/student/toggle-status/${studentId}`,
    method: 'put'
  })
}

// 重置密码
export function resetStudentPassword(studentId) {
  return request({
    url: `/api/student/reset-password/${studentId}`,
    method: 'put'
  })
}

// 删除学生
export function deleteStudent(studentId) {
  return request({
    url: `/api/student/delete/${studentId}`,
    method: 'delete'
  })
}

// 获取学生总数
export function getStudentCount() {
  return request({
    url: '/api/student/count',
    method: 'get'
  })
}