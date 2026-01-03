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

// --- 学生端接口 ---

// 获取我的考试列表
export function getMyExamList() {
  return request({
    url: '/api/exam/student/list',
    method: 'get'
  })
}

// 开始考试 (获取试卷)
export function startExam(examId) {
  return request({
    url: `/api/exam/student/paper/${examId}`,
    method: 'get'
  })
}

// 提交试卷
export function submitExamPaper(data) {
  return request({
    url: '/api/exam/student/submit',
    method: 'post',
    data // { examId: 'xxx', answers: { q1: 'A', q2: ['A','B'] } }
  })
}

// 获取考试结果(解析)
export function getExamResultDetail(examId) {
  return request({
    url: `/api/exam/student/result/${examId}`,
    method: 'get'
  })
}

// --- 教师阅卷补充接口 ---

// 获取试卷详情（包含题目和标准答案，用于阅卷对照）
// 注意：这里假设你有一个 /api/paper/detail/{paperId} 或者复用学生端的获取试卷接口
// 为了方便，我们这里调用学生端的获取试卷接口（因为它返回题目信息），或者你需要确认后端是否有专门的教师端获取试卷题目接口
// 临时方案：使用学生端的逻辑获取题目详情，或者调用后端 PaperController
//import { getPaperDetail } from '@/api/paper' // 如果你有 paper.js，建议引入这个

// 如果没有 paper.js，我们在这里定义一个
export function getPaperQuestionsForGrading(paperId) {
  return request({
    url: `/api/paper/${paperId}/questions`, // 假设后端有这个接口，或者复用学生端接口
    method: 'get'
  })
}