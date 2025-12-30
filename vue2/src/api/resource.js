import request from '@/utils/request'

/**
 * 教学资源管理API
 */

// 获取章节树
export function getChapterTree(classId) {
  return request({
    url: `/api/chapters/tree/${classId}`,
    method: 'get'
  })
}

// 新增章节
export function addChapter(chapterData) {
  return request({
    url: '/api/chapters/add',
    method: 'post',
    data: chapterData
  })
}

// 更新章节
export function updateChapter(chapterData) {
  return request({
    url: '/api/chapters/update',
    method: 'put',
    data: chapterData
  })
}

// 删除章节
export function deleteChapter(chapterId) {
  return request({
    url: `/api/chapters/delete/${chapterId}`,
    method: 'delete'
  })
}

// 获取章节详情
export function getChapterDetail(chapterId) {
  return request({
    url: `/api/chapters/detail/${chapterId}`,
    method: 'get'
  })
}

// 上传资源（支持文件上传）
export function uploadResource(formData) {
  return request({
    url: '/api/resources/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取章节资源列表
export function getResourcesByChapter(chapterId) {
  return request({
    url: `/api/resources/chapter/${chapterId}`,
    method: 'get'
  })
}

// 更新资源信息
export function updateResource(resourceData) {
  return request({
    url: '/api/resources/update',
    method: 'put',
    data: resourceData
  })
}

// 删除资源
export function deleteResource(resourceId) {
  return request({
    url: `/api/resources/delete/${resourceId}`,
    method: 'delete'
  })
}

// 下载资源
export function downloadResource(resourceId) {
  return request({
    url: `/api/resources/download/${resourceId}`,
    method: 'get',
    responseType: 'blob'
  })
}

// 获取资源详情
export function getResourceDetail(resourceId) {
  return request({
    url: `/api/resources/detail/${resourceId}`,
    method: 'get'
  })
}

// 获取教师的教学班列表（用于选择班级）
export function getTeacherClasses(teacherId) {
  return request({
    url: `/api/teachingClass/teacher/${teacherId}`,
    method: 'get'
  })
}