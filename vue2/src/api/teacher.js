import request from '@/utils/request'

/**
 * 教师管理API
 */

// 获取教师列表（支持搜索）
export function getTeacherList(keyword = '') {
  return request({
    url: '/api/teacher/list',
    method: 'get',
    params: { keyword }
  })
}

// 新增教师账户
export function addTeacher(teacherData) {
  // 从localStorage获取当前登录的管理员信息
  const userInfoStr = localStorage.getItem('userInfo');
  let adminAccount = null;
  
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr);
      if (userInfo.role === 'admin' && userInfo.account) {
        adminAccount = userInfo.account;
      }
    } catch (e) {
      console.error('解析用户信息失败:', e);
    }
  }
  
  // 如果获取不到管理员账号，使用默认值（确保数据库中有这个管理员）
  if (!adminAccount) {
    adminAccount = 'admin001';
    console.warn('使用默认管理员账号:', adminAccount);
  }
  
  // 将管理员账号添加到请求数据中
  const requestData = {
    ...teacherData,
    adminAccount: adminAccount
  };
  
  return request({
    url: '/api/teacher/add',
    method: 'post',
    data: requestData,
    headers: {
      'X-Admin-Account': adminAccount // 同时通过请求头传递
    }
  })
}

// 更新教师信息
export function updateTeacher(teacherData) {
  return request({
    url: '/api/teacher/update',
    method: 'put',
    data: teacherData
  })
}

// 切换账号状态（启用/禁用）
export function toggleTeacherStatus(teacherId) {
  return request({
    url: `/api/teacher/toggle-status/${teacherId}`,
    method: 'put'
  })
}

// 重置密码
export function resetTeacherPassword(teacherId) {
  return request({
    url: `/api/teacher/reset-password/${teacherId}`,
    method: 'put'
  })
}

// 删除教师
export function deleteTeacher(teacherId) {
  return request({
    url: `/api/teacher/delete/${teacherId}`,
    method: 'delete'
  })
}

// 获取教师总数
export function getTeacherCount() {
  return request({
    url: '/api/teacher/count',
    method: 'get'
  })
}

// 获取教师教授的课程
export function getTeacherCourses(teacherId) {
  return request({
    url: `/api/teacher/courses`,
    method: 'get',
    params: { teacherId }  // 通过查询参数传递
  })
}

// 获取教师的教学班
export function getTeachingClassesByTeacher(teacherId) {
  return request({
    url: `/api/teacher/teaching-classes`,
    method: 'get',
    params: { teacherId }  // 通过查询参数传递
  })
}