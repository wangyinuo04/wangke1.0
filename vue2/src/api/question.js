import request from '@/utils/request'

/**
 * 题库/试题管理 API
 */

// 1. 根据课程ID获取该课程下的所有试题
export function getQuestionsByCourse(courseId) {
  return request({
    url: `/api/question/course/${courseId}`,
    method: 'get'
  })
}

// 2. 新增试题
export function createQuestion(data) {
  return request({
    url: '/api/question/add',
    method: 'post',
    data
  })
}

// 3. 更新试题
export function updateQuestion(data) {
  return request({
    url: '/api/question/update',
    method: 'put',
    data
  })
}

// 4. 删除试题
export function deleteQuestion(questionId) {
  return request({
    url: `/api/question/delete/${questionId}`,
    method: 'delete'
  })
}

// 5. 获取试题详情
export function getQuestionDetail(questionId) {
  return request({
    url: `/api/question/detail/${questionId}`,
    method: 'get'
  })
}