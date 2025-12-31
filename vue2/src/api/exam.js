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

/**
 * 阅卷统计API
 */

// 获取考试的所有参与记录
export function getExamSubmissions(examId, teacherId = '') {
  return request({
    url: `/api/exam/${examId}/submissions`,
    method: 'get',
    params: { teacherId }
  })
}

// 获取待批阅的主观题
export function getPendingGrading(examId) {
  return request({
    url: `/api/exam/${examId}/pending-grading`,
    method: 'get'
  })
}

// 获取考试统计数据
export function getExamStats(examId) {
  return request({
    url: `/api/exam/${examId}/stats`,
    method: 'get'
  })
}

// 提交主观题评分（复合主键版本）
export function submitSubjectiveScore(studentId, examId, subjectiveScore) {
  return request({
    url: '/api/exam/submission/grade',
    method: 'post',
    data: { studentId, examId, subjectiveScore }
  })
}