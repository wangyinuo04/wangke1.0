import request from '@/utils/request'

/**
 * 讨论管理API
 */

// 获取话题列表
export function getTopics(teacherId, classId = '', keyword = '') {
  return request({
    url: '/api/discussion/topics',
    method: 'get',
    params: { teacherId, classId, keyword }
  })
}

// 创建话题
export function createTopic(topicData) {
  return request({
    url: '/api/discussion/topics',
    method: 'post',
    data: topicData
  })
}

// 删除话题
export function deleteTopic(topicId) {
  return request({
    url: `/api/discussion/topics/${topicId}`,
    method: 'delete'
  })
}

// 置顶/取消置顶话题
export function toggleTopTopic(topicId, topOrder) {
  return request({
    url: `/api/discussion/topics/${topicId}/top`,
    method: 'put',
    params: { topOrder }
  })
}

// 获取话题详情
export function getTopicDetail(topicId) {
  return request({
    url: `/api/discussion/topics/${topicId}/detail`,
    method: 'get'
  })
}

// 获取话题回复
export function getReplies(topicId) {
  return request({
    url: `/api/discussion/topics/${topicId}/replies`,
    method: 'get'
  })
}

// 创建回复
export function createReply(replyData) {
  return request({
    url: '/api/discussion/replies',
    method: 'post',
    data: replyData
  })
}

// 删除回复
export function deleteReply(replyId) {
  return request({
    url: `/api/discussion/replies/${replyId}`,
    method: 'delete'
  })
}