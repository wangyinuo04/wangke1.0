<template>
  <div class="manage-container">

    <div v-if="currentView === 'list'" class="view-wrapper fade-in">
      <div class="action-header">
        <div class="title-section">
          <h2>互动讨论管理</h2>
          <p class="subtitle">发布课程公告、答疑解惑及管理学生言论</p>
        </div>
        <div class="operation-section">
          <div class="class-filter">
            <select v-model="selectedClassId" @change="loadTopics">
              <option value="">所有班级</option>
              <option v-for="cls in teachingClasses" :key="cls.id" :value="cls.id">
                {{ cls.courseName }} - {{ cls.className }}
              </option>
            </select>
          </div>
          <div class="search-box">
            <input type="text" v-model="searchQuery" placeholder="搜索话题标题..." @keyup.enter="handleSearch">
            <button class="btn btn-search" @click="handleSearch">🔍 搜索</button>
          </div>
          <button class="btn btn-primary" @click="openCreateModal">+ 发布新话题</button>
        </div>
      </div>

      <div class="topic-list-card">
        <div class="list-header">
          <span class="col-main">话题</span>
          <span class="col-author">发布人</span>
          <span class="col-stat">回复/浏览</span>
          <span class="col-time">最后回复</span>
          <span class="col-action">操作</span>
        </div>

        <div class="topic-list-body">
          <div v-for="topic in topics" :key="topic.topicId" class="topic-item"
            :class="{ 'is-pinned': topic.topOrder > 0 }">
            <div class="col-main" @click="enterTopic(topic)">
              <div class="class-info">
                <span class="class-tag">{{ getClassName(topic.classId) }}</span>
              </div>
              <span v-if="topic.topOrder > 0" class="tag-top">置顶</span>
              <span class="topic-title">{{ topic.topicTitle }}</span>
              <p class="topic-preview">{{ topic.topicContent }}</p>
            </div>
            <div class="col-author">
              <div class="avatar-circle" :class="topic.role === 'teacher' ? 'blue-bg' : 'gray-bg'">
                {{ topic.authorName ? topic.authorName[0] : '?' }}
              </div>
              <span>{{ topic.authorName }}</span>
            </div>
            <div class="col-stat">
              <span class="count-reply">{{ topic.replyCount || 0 }}</span>
              <span class="count-view">/ {{ topic.viewCount || 0 }}</span>
            </div>
            <div class="col-time">{{ formatTime(topic.lastReplyTime || topic.publishTime) }}</div>
            <div class="col-action">
              <button v-if="topic.topOrder === 0" class="btn-text btn-primary"
                @click.stop="toggleTop(topic.topicId, 1)">置顶</button>
              <button v-else class="btn-text btn-secondary" @click.stop="toggleTop(topic.topicId, 0)">取消置顶</button>
              <button class="btn-text btn-danger" @click.stop="deleteTopic(topic.topicId)">删除</button>
            </div>
          </div>

          <div v-if="loading" class="loading-state">加载中...</div>
          <div v-else-if="topics.length === 0" class="empty-state">暂无讨论话题</div>
        </div>
      </div>
    </div>

    <div v-else class="view-wrapper fade-in">
      <div class="detail-header-bar">
        <button class="btn-back" @click="currentView = 'list'">← 返回列表</button>
        <h3>详情管理</h3>
        <div class="class-tag">{{ getClassName(currentTopic.classId) }}</div>
      </div>

      <div class="discussion-detail-layout">
        <!-- 主话题卡片 -->
        <div class="post-card main-post" v-if="currentTopic">
          <div class="post-header">
            <div class="user-info">
              <div class="avatar-circle big" :class="currentTopic.role === 'teacher' ? 'blue-bg' : 'gray-bg'">
                {{ currentTopic.authorName ? currentTopic.authorName[0] : '?' }}
              </div>
              <div>
                <div class="user-name">
                  {{ currentTopic.authorName }}
                  <span v-if="currentTopic.role === 'teacher'" class="tag-teacher">教师</span>
                  <span v-else class="tag-student">学生</span>
                </div>
                <div class="post-time">发布于 {{ formatTime(currentTopic.publishTime) }}</div>
              </div>
            </div>
            <div class="post-title-large">
              <span v-if="currentTopic.topOrder > 0" class="tag-top">置顶</span>
              {{ currentTopic.topicTitle }}
            </div>
          </div>
          <div class="post-content">
            {{ currentTopic.topicContent }}
          </div>
        </div>

        <!-- 回复区域 -->
        <div class="replies-section">
          <div class="section-title">共 {{ replies.length }} 条回复</div>

          <!-- 修复这里：确保 replies 存在且有内容 -->
          <template v-if="replies && replies.length > 0">
            <div v-for="(replyItem, index) in replies" :key="replyItem.replyId" class="post-card reply-item">
              <div class="post-header">
                <div class="user-info">
                  <div class="avatar-circle" :class="replyItem.role === 'teacher' ? 'blue-bg' : 'gray-bg'">
                    {{ replyItem.authorName ? replyItem.authorName[0] : '?' }}
                  </div>
                  <div>
                    <div class="user-name">
                      {{ replyItem.authorName }}
                      <span v-if="replyItem.role === 'teacher'" class="tag-teacher">教师</span>
                      <span v-else class="tag-student">学生</span>
                    </div>
                    <div class="post-time">{{ index + 1 }}楼 · {{ formatTime(replyItem.publishTime) }}</div>
                  </div>
                </div>
                <div class="reply-actions" style="opacity: 1 !important;">
                  <button class="btn-text btn-primary" @click="setReplyTarget(replyItem)">回复</button>
                  <button class="btn-text btn-danger" @click="deleteReply(replyItem.replyId)">删除违规</button>
                </div>
              </div>
              <div class="post-content">
                <div v-if="replyItem.quoteContent" class="quote-box">
                  <span class="quote-author">@{{ replyItem.quoteAuthorName || '用户' }}</span>：
                  <span class="quote-text">{{ replyItem.quoteContent }}</span>
                </div>
                {{ replyItem.replyContent || replyItem.content }}
              </div>
            </div>
          </template>

          <div v-if="!loadingReplies && (!replies || replies.length === 0)" class="empty-replies">暂无回复，快来抢沙发吧~</div>
          <div v-if="loadingReplies" class="loading-state">加载回复中...</div>
        </div>

        <!-- 回复编辑器 -->
        <div class="reply-editor-fixed">
          <div class="editor-container">
            <div v-if="replyTarget" class="reply-target-tip">
              正在回复 {{ replyTarget.authorName || replyTarget.author }} <span class="close-target"
                @click="replyTarget = null">×</span>
            </div>
            <div class="input-row">
              <textarea v-model="newReplyContent" :placeholder="replyTarget ? '回复TA的内容...' : '发布您的评论...'"
                rows="2"></textarea>
              <button class="btn btn-primary send-btn" @click="submitReply"
                :disabled="!newReplyContent.trim()">发送</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showCreateModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>发布讨论主题</h3>
          <span class="close-btn" @click="closeCreateModal">×</span>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveTopic">
            <div class="form-group">
              <label>选择教学班级 <span class="required">*</span></label>
              <select v-model="form.classId" required>
                <option value="" disabled>请选择教学班级</option>
                <option v-for="cls in teachingClasses" :key="cls.id" :value="cls.id">
                  {{ cls.courseName }} - {{ cls.className }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>主题标题 <span class="required">*</span></label>
              <input type="text" v-model="form.topicTitle" placeholder="请输入标题 (如：关于期中考试的疑问)" required>
            </div>
            <div class="form-group">
              <label>内容详情 <span class="required">*</span></label>
              <textarea v-model="form.topicContent" rows="6" placeholder="请输入讨论内容..." required></textarea>
            </div>
            <div class="form-group checkbox-group">
              <label>
                <input type="checkbox" v-model="form.isTop"> 设为置顶话题
              </label>
              <span class="hint">置顶后将显示在列表最顶部</span>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeCreateModal">取消</button>
              <button type="submit" class="btn btn-primary">立即发布</button>
            </div>
          </form>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { getTeacherCourses } from '@/api/teachingClass'
import {
  getTopics, createTopic, deleteTopic, toggleTopTopic,
  getTopicDetail, getReplies, createReply, deleteReply
} from '@/api/discussion'

export default {
  name: 'DiscussionManage',
  data() {
    return {
      currentView: 'list', // 'list' | 'detail'
      searchQuery: '',
      selectedClassId: '',
      showCreateModal: false,
      loading: false,
      loadingReplies: false,

      // 用户信息
      teacherId: '',
      teacherName: '',

      // 教学班级列表
      teachingClasses: [],

      // 话题数据
      topics: [],

      // 详情页数据
      currentTopic: null,
      replies: [],

      // 表单数据
      form: {
        classId: '',
        topicTitle: '',
        topicContent: '',
        isTop: false
      },
      newReplyContent: '',
      replyTarget: null
    }
  },
  created() {
    this.initUserInfo()
    this.loadTeachingClasses()
  },
  methods: {
    // 初始化用户信息
    initUserInfo() {
      const userInfo = this.$store.state.userInfo || JSON.parse(localStorage.getItem('userInfo') || '{}')
      if (userInfo.role === 'teacher' && userInfo.id) {
        this.teacherId = userInfo.id
        this.teacherName = userInfo.name || userInfo.id
      } else {
        console.error('未获取到教师信息')
        this.$message.error('无法获取教师信息，请重新登录')
      }
    },

    // 加载教学班级
    async loadTeachingClasses() {
      try {
        const response = await getTeacherCourses(this.teacherId)
        if (response.success && response.data) {
          this.teachingClasses = response.data.map(cls => ({
            id: cls.id,
            classId: cls.id,
            courseName: cls.courseName,
            className: cls.className,
            semester: cls.semester
          }))
          this.loadTopics()
        } else {
          this.$message.error(response.message || '加载教学班级失败')
        }
      } catch (error) {
        console.error('加载教学班级失败:', error)
        this.$message.error('网络错误，请检查后端服务')
      }
    },

    // 加载话题
    async loadTopics() {
      if (!this.teacherId) return

      this.loading = true
      try {
        const response = await getTopics(this.teacherId, this.selectedClassId, this.searchQuery)
        if (response.success && response.data) {
          this.topics = response.data.map(topic => ({
            ...topic,
            isTop: topic.topOrder > 0,
            author: topic.authorName || '未知',
            title: topic.topicTitle,
            content: topic.topicContent,
            publishTime: topic.publishTime,
            lastReplyTime: topic.lastReplyTime,
            replyCount: topic.replyCount || 0,
            viewCount: topic.viewCount || 0
          }))
        } else {
          this.$message.error(response.message || '加载话题失败')
        }
      } catch (error) {
        console.error('加载话题失败:', error)
        this.$message.error('网络错误，请稍后重试')
      } finally {
        this.loading = false
      }
    },

    handleSearch() {
      this.loadTopics()
    },

    // 获取班级名称
    getClassName(classId) {
      const cls = this.teachingClasses.find(c => c.id === classId)
      return cls ? `${cls.courseName} - ${cls.className}` : '未知班级'
    },

    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return '刚刚'
      try {
        // 如果是日期对象
        if (timeStr instanceof Date) {
          return timeStr.toLocaleString('zh-CN')
        }

        // 如果是字符串
        const time = new Date(timeStr)
        if (isNaN(time.getTime())) {
          return timeStr
        }

        const now = new Date()
        const diff = now - time

        // 如果是一天内，显示相对时间
        if (diff < 24 * 60 * 60 * 1000) {
          if (diff < 60 * 1000) return '刚刚'
          if (diff < 60 * 60 * 1000) return `${Math.floor(diff / (60 * 1000))}分钟前`
          return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
        }

        // 否则显示完整时间
        return time.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
      } catch (e) {
        console.error('格式化时间错误:', e, timeStr)
        return timeStr
      }
    },

    // --- 话题管理 ---
    openCreateModal() {
      if (this.teachingClasses.length === 0) {
        this.$message.warning('您还没有教学班级，无法发布话题')
        return
      }

      this.form = {
        classId: this.selectedClassId || this.teachingClasses[0].id,
        topicTitle: '',
        topicContent: '',
        isTop: false
      }
      this.showCreateModal = true
    },

    closeCreateModal() {
      this.showCreateModal = false
    },

    async saveTopic() {
      if (!this.form.classId || !this.form.topicTitle.trim() || !this.form.topicContent.trim()) {
        this.$message.warning('请填写完整信息')
        return
      }

      try {
        const topicData = {
          topicTitle: this.form.topicTitle,
          topicContent: this.form.topicContent,
          classId: this.form.classId,
          authorId: this.teacherId,
          topOrder: this.form.isTop ? 1 : 0
        }

        const response = await createTopic(topicData)
        if (response.success && response.data) {
          this.$message.success('发布话题成功')
          this.closeCreateModal()
          this.loadTopics()
        } else {
          this.$message.error(response.message || '发布失败')
        }
      } catch (error) {
        console.error('发布话题失败:', error)
        this.$message.error('网络错误，请稍后重试')
      }
    },

    async deleteTopic(topicId) {
      try {
        this.$confirm('确定删除该话题及其所有回复吗？', '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          const response = await deleteTopic(topicId)
          if (response.success) {
            this.$message.success('删除成功')
            this.loadTopics()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        }).catch(() => { })
      } catch (error) {
        console.error('删除话题失败:', error)
        this.$message.error('网络错误，请稍后重试')
      }
    },

    async toggleTop(topicId, topOrder) {
      try {
        const response = await toggleTopTopic(topicId, topOrder)
        if (response.success) {
          this.$message.success(topOrder > 0 ? '置顶成功' : '已取消置顶')
          this.loadTopics()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('置顶操作失败:', error)
        this.$message.error('网络错误，请稍后重试')
      }
    },

    // --- 详情与回复管理 ---
    async enterTopic(topic) {
      this.loading = true
      try {
        // 获取话题详情
        const detailResponse = await getTopicDetail(topic.topicId)
        if (detailResponse.success && detailResponse.data) {
          this.currentTopic = detailResponse.data

          // 获取回复
          await this.loadReplies(topic.topicId)

          this.currentView = 'detail'
          this.replyTarget = null
          this.newReplyContent = ''
        } else {
          this.$message.error(detailResponse.message || '加载话题详情失败')
        }
      } catch (error) {
        console.error('进入话题失败:', error)
        this.$message.error('网络错误，请稍后重试')
      } finally {
        this.loading = false
      }
    },

    async loadReplies(topicId) {
      this.loadingReplies = true
      try {
        const response = await getReplies(topicId)
        console.log('回复数据:', response) // 调试用

        if (response.success && response.data) {
          // 确保数据结构正确
          this.replies = response.data.map(reply => ({
            replyId: reply.replyId,
            replyContent: reply.replyContent,
            authorName: reply.authorName,
            authorId: reply.authorId,
            role: reply.role,
            publishTime: reply.publishTime,
            quoteContent: reply.quoteContent,
            quoteAuthorId: reply.quoteAuthorId,
            quoteAuthorName: reply.quoteAuthorName,
            // 为了兼容旧代码
            author: reply.authorName,
            content: reply.replyContent,
            time: reply.publishTime
          }))
        } else {
          this.$message.error(response.message || '加载回复失败')
        }
      } catch (error) {
        console.error('加载回复失败:', error)
        this.$message.error('网络错误，请稍后重试')
      } finally {
        this.loadingReplies = false
      }
    },

    async deleteReply(replyId) {
      try {
        this.$confirm('确定删除这条回复吗？(违规处理)', '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          const response = await deleteReply(replyId)
          if (response.success) {
            this.$message.success('删除成功')
            // 重新加载回复
            await this.loadReplies(this.currentTopic.topicId)
            // 更新话题回复数
            this.currentTopic.replyCount = Math.max(0, (this.currentTopic.replyCount || 1) - 1)
          } else {
            this.$message.error(response.message || '删除失败')
          }
        }).catch(() => { })
      } catch (error) {
        console.error('删除回复失败:', error)
        this.$message.error('网络错误，请稍后重试')
      }
    },

    setReplyTarget(replyItem) {
      this.replyTarget = {
        authorId: replyItem.authorId,
        authorName: replyItem.authorName || replyItem.author,
        replyContent: replyItem.replyContent || replyItem.content,
        replyId: replyItem.replyId
      }
      // 聚焦输入框
      const textarea = this.$el.querySelector('textarea')
      if (textarea) textarea.focus()
    },

    async submitReply() {
      if (!this.newReplyContent.trim()) {
        this.$message.warning('请输入回复内容')
        return
      }

      try {
        const replyData = {
          replyContent: this.newReplyContent,
          topicId: this.currentTopic.topicId,
          authorId: this.teacherId,
        }

        // 如果有引用目标，添加引用字段
        if (this.replyTarget) {
          replyData.quoteAuthorId = this.replyTarget.authorId
          replyData.quoteContent = this.replyTarget.replyContent || this.replyTarget.content
          console.log('包含引用的回复数据:', replyData)
        }

        const response = await createReply(replyData)
        if (response.success && response.data) {
          this.$message.success('回复成功')

          // 重新加载回复
          await this.loadReplies(this.currentTopic.topicId)

          this.newReplyContent = ''
          this.replyTarget = null

          // 滚动到底部
          this.$nextTick(() => {
            window.scrollTo(0, document.body.scrollHeight)
          })
        } else {
          this.$message.error(response.message || '回复失败')
        }
      } catch (error) {
        console.error('回复失败:', error)
        this.$message.error('网络错误，请稍后重试')
      }
    }
  }
}
</script>

<style scoped>
.manage-container {
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 头部样式复用 */
.action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.title-section h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.subtitle {
  margin: 5px 0 0;
  font-size: 13px;
  color: #999;
}

.operation-section {
  display: flex;
  gap: 15px;
  align-items: center;
}

.class-filter {
  margin-right: 15px;
}

.class-filter select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  min-width: 200px;
}

.search-box {
  display: flex;
}

.search-box input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-right: none;
  border-radius: 4px 0 0 4px;
  width: 220px;
}

.btn-search {
  border-radius: 0 4px 4px 0;
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  border-left: none;
  cursor: pointer;
  padding: 8px 12px;
}

/* 话题列表样式 */
.topic-list-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.list-header {
  display: flex;
  background: #f9fafc;
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
  color: #333; /* 统一颜色为深灰色，不是浅色 */
  font-size: 14px; /* 统一字体大小 */
}

.col-main {
  flex: 1;
  cursor: pointer;
}

.col-author {
  width: 120px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.col-stat { 
  width: 100px; 
  text-align: center; 
  color: #333; /* 改成和发布人一样的深色 */
  font-size: 14px; /* 改成和发布人一样的大小 */
  font-weight: normal; 
}

.col-time { 
  width: 140px; 
  color: #333; /* 改成和发布人一样的深色 */
  font-size: 14px; /* 改成和发布人一样的大小 */
  text-align: right; 
  padding-right: 20px;
  font-weight: normal;
}

.list-header .col-stat,
.list-header .col-time {
  color: #333; /* 列表头也改成深色 */
  font-size: 14px;
  font-weight: 600; /* 保持粗体 */
}

.col-action {
  width: 180px;
  text-align: center;
  display: flex;
  gap: 8px;
  justify-content: center;
}

.topic-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  transition: background 0.2s;
}

.topic-item:hover {
  background: #f5f7fa;
}

.topic-item.is-pinned {
  background: #f0f9ff;
}

/* 置顶高亮 */

.class-info {
  margin-bottom: 5px;
}

.class-tag {
  background: #f0f5ff;
  color: #2f54eb;
  border: 1px solid #adc6ff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-family: monospace;
}

.tag-top {
  background: #ff4d4f;
  color: white;
  padding: 2px 5px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 8px;
  vertical-align: middle;
}

.topic-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.topic-title:hover {
  color: #1890ff;
  text-decoration: underline;
}

.topic-preview {
  margin: 5px 0 0;
  font-size: 13px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 500px;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
  font-weight: bold;
}

.avatar-circle.blue-bg {
  background: #e6f7ff;
  color: #1890ff;
}

.avatar-circle.gray-bg {
  background: #f5f5f5;
  color: #999;
}

.count-reply {
  color: #1890ff;
  font-weight: bold;
}

/* 详情页样式 */
.detail-header-bar {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.btn-back {
  border: none;
  background: none;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  font-weight: bold;
}

.btn-back:hover {
  color: #1890ff;
}

.discussion-detail-layout {
  padding-bottom: 80px;
  /* 为底部输入框留空 */
}

.post-card {
  background: #fff;
  border-radius: 8px;
  padding: 25px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

.main-post {
  border-left: 4px solid #1890ff;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  gap: 15px;
  align-items: center;
}

.avatar-circle.big {
  width: 48px;
  height: 48px;
  font-size: 20px;
}

.avatar-circle.big.blue-bg {
  background: #1890ff;
  color: white;
}

.avatar-circle.big.gray-bg {
  background: #f5f5f5;
  color: #999;
}

.user-name {
  font-weight: bold;
  font-size: 15px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 5px;
}

.post-time {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.tag-teacher {
  background: #1890ff;
  color: white;
  padding: 1px 4px;
  border-radius: 3px;
  font-size: 11px;
}

.tag-student {
  background: #f0f0f0;
  color: #999;
  padding: 1px 4px;
  border-radius: 3px;
  font-size: 11px;
}

.post-title-large {
  font-size: 22px;
  font-weight: bold;
  margin-top: 15px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.post-content {
  font-size: 15px;
  line-height: 1.6;
  color: #444;
  white-space: pre-wrap;
  margin-top: 10px;
}

.replies-section {
  margin-top: 30px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 3px solid #1890ff;
}

.reply-item {
  border: 1px solid #ebeef5;
  box-shadow: none;
}

/* 修复：确保回复按钮始终显示且明显 */
.reply-actions {
  opacity: 1 !important;
  visibility: visible !important;
  display: flex !important;
  gap: 10px;
  align-items: center;
}

.btn-text {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 13px;
  padding: 4px 8px !important;
  border-radius: 4px;
  transition: all 0.2s;
  min-width: 50px;
  text-align: center;
}

.btn-text.btn-primary {
  color: #1890ff;
  background-color: rgba(24, 144, 255, 0.1) !important;
  border: 1px solid rgba(24, 144, 255, 0.3) !important;
}

.btn-text.btn-primary:hover {
  background-color: rgba(24, 144, 255, 0.2) !important;
  color: #40a9ff;
  transform: translateY(-1px);
}

.btn-text.btn-danger {
  color: #f5222d;
  background-color: rgba(245, 34, 45, 0.1) !important;
  border: 1px solid rgba(245, 34, 45, 0.3) !important;
}

.btn-text.btn-danger:hover {
  background-color: rgba(245, 34, 45, 0.2) !important;
  color: #ff4d4f;
  transform: translateY(-1px);
}

/* 美化引用框样式 */
.quote-box {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 12px 15px;
  border-radius: 8px;
  color: #495057;
  font-size: 14px;
  margin-bottom: 15px;
  border-left: 4px solid #1890ff;
  position: relative;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.quote-box:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.quote-box::before {
  content: "📎 引用";
  display: block;
  font-size: 12px;
  color: #6c757d;
  margin-bottom: 5px;
  font-weight: 600;
  letter-spacing: 1px;
}

.quote-author {
  color: #1890ff;
  font-weight: 600;
  margin-right: 5px;
}

.quote-text {
  color: #495057;
  line-height: 1.5;
}

.quote-box::after {
  content: "";
  position: absolute;
  bottom: -1px;
  left: 10%;
  right: 10%;
  height: 1px;
  background: linear-gradient(to right, transparent, #dee2e6, transparent);
}

/* 底部固定回复框 */
.reply-editor-fixed {
  position: fixed;
  bottom: 0;
  left: 250px;
  /* 适配侧边栏宽度，如果全屏则设为0 */
  right: 0;
  background: white;
  padding: 15px 30px;
  border-top: 1px solid #ebeef5;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.editor-container {
  max-width: 1000px;
  margin: 0 auto;
}

.input-row {
  display: flex;
  gap: 15px;
  align-items: flex-end;
}

textarea {
  flex: 1;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  resize: none;
  font-family: inherit;
}

textarea:focus {
  border-color: #1890ff;
  outline: none;
}

.send-btn {
  height: 40px;
  padding: 0 25px;
}

.reply-target-tip {
  font-size: 12px;
  color: #666;
  margin-bottom: 5px;
  background: #f0f9ff;
  display: inline-block;
  padding: 4px 10px;
  border-radius: 10px;
  border: 1px solid #1890ff;
}

.close-target {
  cursor: pointer;
  margin-left: 5px;
  color: #999;
  font-size: 16px;
}

.close-target:hover {
  color: #ff4d4f;
}

/* 按钮 & 弹窗通用 */
.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-primary:hover {
  background: #40a9ff;
}

.btn-secondary {
  background: white;
  border: 1px solid #dcdfe6;
  color: #606266;
}

.empty-state,
.empty-replies {
  text-align: center;
  color: #ccc;
  padding: 40px;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-box {
  background: white;
  width: 600px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: fade 0.3s;
}

.modal-header {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
}

.close-btn {
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  font-size: 13px;
}

.form-group input[type="text"],
textarea,
select {
  width: 100%;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

.checkbox-group label {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}

.hint {
  font-size: 12px;
  color: #999;
  margin-left: 20px;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@keyframes fade {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in {
  animation: fade 0.3s;
}

.loading-state {
  text-align: center;
  padding: 20px;
  color: #1890ff;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .reply-editor-fixed {
    left: 0;
    padding: 10px;
  }

  .topic-list-card {
    overflow-x: auto;
  }

  .list-header {
    min-width: 600px;
  }

  .operation-section {
    flex-wrap: wrap;
  }

  .class-filter select {
    min-width: 150px;
  }
}
</style>