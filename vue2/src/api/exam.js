import request from '@/utils/request'

/**
 * 试题管理API
 */

// 获取试题列表（按教学班）
export function getQuestionList(classId = '', teacherId = '') {
  return request({
    url: '/api/question/list',
    method: 'get',
    params: { classId, teacherId }
  })
}

// 新增试题
export function addQuestion(questionData) {
  // 从localStorage获取当前登录的教师信息
  const userInfoStr = localStorage.getItem('userInfo');
  let teacherId = null;
  
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr);
      if (userInfo.role === 'teacher' && userInfo.teacherId) {
        teacherId = userInfo.teacherId;
      }
    } catch (e) {
      console.error('解析用户信息失败:', e);
    }
  }
  
  // 将教师ID添加到请求数据中
  const requestData = {
    ...questionData,
    teacherId: teacherId
  };
  
  return request({
    url: '/api/question/add',
    method: 'post',
    data: requestData,
    headers: {
      'X-Teacher-Id': teacherId
    }
  })
}

// 更新试题
export function updateQuestion(questionData) {
  return request({
    url: '/api/question/update',
    method: 'put',
    data: questionData
  })
}

// 删除试题
export function deleteQuestion(questionId) {
  return request({
    url: `/api/question/delete/${questionId}`,
    method: 'delete'
  })
}

// 获取课程相关的试题
export function getQuestionsByCourse(courseId) {
  return request({
    url: `/api/question/course/${courseId}`,
    method: 'get'
  })
}

/**
 * 考试管理API
 */

// 获取考试列表
export function getExamList(teacherId = '') {
  return request({
    url: '/api/exam/list',
    method: 'get',
    params: { teacherId }
  })
}

// 发布考试
export function addExam(examData) {
  // 从localStorage获取当前登录的教师信息
  const userInfoStr = localStorage.getItem('userInfo');
  let teacherId = null;
  
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr);
      if (userInfo.role === 'teacher' && userInfo.teacherId) {
        teacherId = userInfo.teacherId;
      }
    } catch (e) {
      console.error('解析用户信息失败:', e);
    }
  }
  
  // 将教师ID添加到请求数据中
  const requestData = {
    ...examData,
    teacherId: teacherId
  };
  
  return request({
    url: '/api/exam/publish',
    method: 'post',
    data: requestData,
    headers: {
      'X-Teacher-Id': teacherId
    }
  })
}

// 删除考试
export function deleteExam(examId) {
  return request({
    url: `/api/exam/delete/${examId}`,
    method: 'delete'
  })
}