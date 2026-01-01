import request from '@/utils/request'

/**
 * 作业管理API
 */

// 获取教师发布的作业列表
export function getHomeworkList(teacherId) {
  return request({
    url: `/api/homework/teacher/${teacherId}`,
    method: 'get'
  })
}

// 发布作业（带附件）
export function publishHomework(formData) {
  return request({
    url: '/api/homework/publish',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取作业详情
export function getHomeworkDetail(homeworkId) {
  return request({
    url: `/api/homework/detail/${homeworkId}`,
    method: 'get'
  })
}

// 获取作业提交列表
export function getHomeworkSubmissions(homeworkId) {
  return request({
    url: `/api/homework/submissions/${homeworkId}`,
    method: 'get'
  })
}

// 批改作业 - 使用 FormData 方式（根据您的后端代码，应该用这种方式）
export function gradeHomework(submissionId, score, feedback) {
  const formData = new FormData()
  formData.append('submissionId', submissionId)
  formData.append('score', score)
  if (feedback) {
    formData.append('feedback', feedback)
  }
  
  return request({
    url: '/api/homework/grade',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 下载作业附件
export function downloadHomeworkFile(filePath) {
  return request({
    url: `/api/homework/download/${encodeURIComponent(filePath)}`,
    method: 'get',
    responseType: 'blob'
  })
}

// 批量下载作业附件
export function batchDownloadHomework(homeworkId) {
  return request({
    url: `/api/homework/batch-download/${homeworkId}`,
    method: 'get',
    responseType: 'blob'
  })
}

// 删除作业
export function deleteHomework(homeworkId) {
  return request({
    url: `/api/homework/delete/${homeworkId}`,
    method: 'delete'
  })
}

// 更新作业信息
export function updateHomework(homeworkData) {
  return request({
    url: '/api/homework/update',
    method: 'put',
    data: homeworkData
  })
}

// 学生获取我的作业列表
export function getMyHomeworkList() {
  return request({
    url: '/api/homework/student/list',
    method: 'get'
  })
}

// 学生提交作业
export function submitHomework(formData) {
  return request({
    url: '/api/homework/student/submit',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}