import request from '@/utils/request'

/**
 * 教学班管理API
 */

// 获取教学班列表（支持搜索）
export function getTeachingClassList(keyword = '') {
  return request({
    url: '/api/teachingClass/list',
    method: 'get',
    params: { keyword }
  })
}

// 开设教学班
export function addTeachingClass(classData) {
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
  
  // 如果获取不到管理员账号，使用默认值
  if (!adminAccount) {
    adminAccount = 'admin001';
    console.warn('使用默认管理员账号:', adminAccount);
  }
  
  // 将管理员账号添加到请求数据中
  const requestData = {
    ...classData,
    adminAccount: adminAccount
  };
  
  return request({
    url: '/api/teachingClass/add',
    method: 'post',
    data: requestData,
    headers: {
      'X-Admin-Account': adminAccount
    }
  })
}

// 更新教学班信息
export function updateTeachingClass(classData) {
  return request({
    url: '/api/teachingClass/update',
    method: 'put',
    data: classData
  })
}

// 删除教学班
export function deleteTeachingClass(classId) {
  return request({
    url: `/api/teachingClass/delete/${classId}`,
    method: 'delete'
  })
}

// 将单个学生加入教学班
export function enrollSingleStudent(classId, studentId) {
  return request({
    url: '/api/teachingClass/enroll/single',
    method: 'post',
    data: { classId, studentId }
  })
}

// 批量将学生加入教学班
export function enrollBatchStudents(classId, studentIds) {
  return request({
    url: '/api/teachingClass/enroll/batch',
    method: 'post',
    data: { classId, studentIds }
  })
}

// 从教学班移除学生
export function removeStudentFromClass(classId, studentId) {
  return request({
    url: '/api/teachingClass/remove-student',
    method: 'delete',
    data: { classId, studentId }
  })
}

// 获取教学班的学生列表
export function getClassStudents(classId) {
  return request({
    url: `/api/teachingClass/${classId}/students`,
    method: 'get'
  })
}

// 根据行政班级查询学生
export function searchStudentsByAdminClass(adminClass) {
  return request({
    url: '/api/teachingClass/search-students',
    method: 'get',
    params: { adminClass }
  })
}

// 获取教学班详情
export function getTeachingClassDetail(classId) {
  return request({
    url: `/api/teachingClass/detail/${classId}`,
    method: 'get'
  })
}

// 获取教学班总数
export function getTeachingClassCount() {
  return request({
    url: '/api/teachingClass/count',
    method: 'get'
  })
}