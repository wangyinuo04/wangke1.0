import request from '@/utils/request'

/**
 * 课程管理API
 */

// 获取课程列表（支持搜索）
export function getCourseList(keyword = '') {
  return request({
    url: '/api/course/list',
    method: 'get',
    params: { keyword }
  })
}

// 新增课程
export function addCourse(courseData) {
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
    ...courseData,
    adminAccount: adminAccount
  };
  
  return request({
    url: '/api/course/add',
    method: 'post',
    data: requestData,
    headers: {
      'X-Admin-Account': adminAccount
    }
  })
}

// 更新课程信息
export function updateCourse(courseData) {
  return request({
    url: '/api/course/update',
    method: 'put',
    data: courseData
  })
}

// 删除课程
export function deleteCourse(courseId) {
  return request({
    url: `/api/course/delete/${courseId}`,
    method: 'delete'
  })
}

// 获取课程总数
export function getCourseCount() {
  return request({
    url: '/api/course/count',
    method: 'get'
  })
}