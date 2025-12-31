import request from '@/utils/request'

/**
 * 试卷管理API
 */

// 获取试卷列表
export function getPaperList(teacherId = '') {
  return request({
    url: '/api/paper/list',
    method: 'get',
    params: { teacherId }
  })
}

// 创建试卷
export function addPaper(paperData) {
  // 移除从localStorage获取teacherId的代码
  // 直接发送数据
  return request({
    url: '/api/paper/create',
    method: 'post',
    data: paperData
  })
}

// 删除试卷
export function deletePaper(paperId) {
  return request({
    url: `/api/paper/delete/${paperId}`,
    method: 'delete'
  })
}

// 获取已发布的试卷
export function getPublishedPapers(courseId) {
  return request({
    url: `/api/paper/published/${courseId}`,
    method: 'get'
  })
}

// 获取按课程分组的试卷
export function getPapersGroupedByCourse(teacherId = '') {
  return request({
    url: '/api/paper/grouped-by-course',
    method: 'get',
    params: { teacherId }
  })
}