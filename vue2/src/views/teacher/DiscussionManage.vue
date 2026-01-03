<template>
  <div class="manage-container">
    
    <div class="action-header">
      <div class="title-section">
        <h2>讨论区管理</h2>
        <p class="subtitle">管理学生话题、置顶重要通知及在线答疑</p>
      </div>
      <div class="operation-section">
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchKeyword" 
            placeholder="搜索话题标题..."
            @keyup.enter="handleSearch"
          >
          <button class="btn-search" @click="handleSearch">🔍</button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>状态</th>
            <th>话题标题</th>
            <th width="120">来源</th>
            <th width="120">发布人</th>
            <th width="80">回复数</th>
            <th width="160">发布时间</th>
            <th width="180">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="topic in topics" :key="topic.topicId">
            <td>
              <span v-if="topic.topOrder > 0" class="badge-top">已置顶</span>
              <span v-else class="text-gray">-</span>
            </td>
            <td class="title-col">
              <span class="topic-title" :title="topic.topicTitle">{{ topic.topicTitle }}</span>
            </td>
            <td>
              <span class="tag-course">{{ topic.role || '公共区' }}</span>
            </td>
            <td>{{ topic.authorName }}</td>
            <td><span class="reply-count">{{ topic.replyCount }}</span></td>
            <td class="time-col">{{ formatTime(topic.publishTime) }}</td>
            <td>
              <div class="action-col">
                <button class="btn-text btn-primary" @click="openDetail(topic)">查看/回复</button>
                
                <button v-if="topic.topOrder > 0" class="btn-text btn-warn" @click="handleToggleTop(topic, 0)">取消置顶</button>
                <button v-else class="btn-text btn-primary" @click="handleToggleTop(topic, 1)">置顶</button>
                
                <button class="btn-text btn-danger" @click="handleDelete(topic)">删除</button>
              </div>
            </td>
          </tr>
          <tr v-if="topics.length === 0">
            <td colspan="7" class="empty-state">暂无讨论话题</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="modal-mask" v-if="showDetailModal">
      <div class="modal-box wide-modal-xl">
        <div class="modal-header">
          <h3>话题详情</h3>
          <span class="close-btn" @click="closeDetailModal">×</span>
        </div>
        
        <div class="modal-body scroll-body discussion-container">
          <div class="post-card main-post">
            <div class="post-header">
              <div class="user-info">
                <div class="avatar bg-blue">{{ getAvatarChar(currentTopic.authorName) }}</div>
                <div class="meta">
                  <span class="username">{{ currentTopic.authorName }}</span>
                  <span class="time">发布于：{{ formatTime(currentTopic.publishTime) }}</span>
                </div>
              </div>
              <div class="floor-tag">楼主</div>
            </div>
            <div class="post-content">
              <h2 class="content-title">{{ currentTopic.topicTitle }}</h2>
              <div class="content-body">{{ currentTopic.topicContent }}</div>
            </div>
          </div>

          <div class="reply-list">
            <div class="divider">共 {{ currentReplies.length }} 条回复</div>
            
            <div v-for="(reply, index) in currentReplies" :key="reply.replyId" class="post-card reply-post">
              <div class="post-header">
                <div class="user-info">
                  <div class="avatar bg-gray">{{ getAvatarChar(reply.authorName) }}</div>
                  <div class="meta">
                    <span class="username">{{ reply.authorName }}</span>
                    <span class="time">{{ formatTime(reply.publishTime) }}</span>
                  </div>
                </div>
                <div class="floor-actions">
                  <span class="floor-tag">#{{ index + 1 }}</span>
                  <button class="btn-text btn-danger btn-xs" @click="handleDeleteReply(reply.replyId)">删除</button>
                </div>
              </div>
              <div class="post-content">
                {{ reply.replyContent }}
              </div>
            </div>
            
            <div v-if="currentReplies.length === 0" class="empty-reply">
              暂无回复，快来抢沙发吧~
            </div>
          </div>
        </div>

        <div class="modal-footer reply-footer">
          <div class="reply-input-area">
            <textarea 
              v-model="replyContent" 
              placeholder="作为老师回复... (Ctrl+Enter 发送)" 
              rows="3"
              @keyup.ctrl.enter="submitReply"
            ></textarea>
            <div class="reply-actions">
              <button class="btn btn-primary" @click="submitReply">发送回复</button>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { getTopics, toggleTopTopic, deleteTopic, getTopicDetail, createReply, deleteReply } from '@/api/discussion'

export default {
  name: 'DiscussionManage',
  data() {
    return {
      teacherId: '',
      searchKeyword: '',
      topics: [],
      
      showDetailModal: false,
      currentTopic: {},
      currentReplies: [],
      replyContent: ''
    }
  },
  created() {
    // 获取当前登录的教师ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    // 优先取 teacherId，如果没有则取 teacher_id (兼容不同字段名)
    this.teacherId = userInfo.teacherId || userInfo.teacher_id || userInfo.username;
    
    // 如果还没取到，尝试打印一下看看
    if (!this.teacherId) {
      console.error("未获取到教师ID，请重新登录");
    }
    
    this.fetchTopics();
  },
  methods: {
    // 1. 获取话题列表
    fetchTopics() {
      if (!this.teacherId) return;
      getTopics(this.teacherId, '', this.searchKeyword).then(res => {
        if (res.success) {
          this.topics = res.data || [];
        }
      });
    },

    handleSearch() {
      this.fetchTopics();
    },

    // 2. 置顶/取消置顶
    handleToggleTop(topic, order) {
      toggleTopTopic(topic.topicId, order).then(res => {
        if (res.success) {
          this.$message.success('操作成功');
          this.fetchTopics();
        } else {
          this.$message.error('操作失败');
        }
      });
    },

    // 3. 删除话题
    handleDelete(topic) {
      if (!confirm(`确定要删除话题 "${topic.topicTitle}" 吗？此操作将同时删除所有回复。`)) return;
      
      deleteTopic(topic.topicId).then(res => {
        if (res.success) {
          this.$message.success('删除成功');
          this.fetchTopics();
        } else {
          this.$message.error('删除失败');
        }
      });
    },

    // 4. 打开详情 (包含回复列表)
    openDetail(topic) {
      this.currentTopic = topic;
      this.replyContent = '';
      this.showDetailModal = true;
      this.loadReplies(topic.topicId);
    },

    loadReplies(topicId) {
      getTopicDetail(topicId).then(res => {
        if (res.success) {
          this.currentTopic = res.data.topic; // 更新最新信息
          this.currentReplies = res.data.replies || [];
        }
      });
    },

    closeDetailModal() {
      this.showDetailModal = false;
      this.fetchTopics(); // 关闭时刷新列表，更新回复数
    },

    // 5. 发送回复 (已修复：显式传递 authorId)
    submitReply() {
      if (!this.replyContent.trim()) {
        this.$message.warning('请输入回复内容');
        return;
      }

      if (!this.teacherId) {
        this.$message.error('教师ID获取失败，请重新登录');
        return;
      }

      // ✅ 关键修复：显式传递 authorId，防止后端 Session 识别失败
      const payload = {
        topicId: this.currentTopic.topicId,
        replyContent: this.replyContent,
        authorId: this.teacherId // 必传！
      };

      console.log("发送回复数据:", payload);

      createReply(payload).then(res => {
        if (res.success) {
          this.$message.success('回复成功'); 
          this.replyContent = '';
          this.loadReplies(this.currentTopic.topicId); // 刷新回复列表
        } else {
          this.$message.error(res.message || '回复失败');
        }
      });
    },

    // 6. 删除单条回复
    handleDeleteReply(replyId) {
      if (!confirm('确定删除这条回复吗？')) return;
      deleteReply(replyId).then(res => {
        if (res.success) {
          this.$message.success('删除成功');
          this.loadReplies(this.currentTopic.topicId);
        }
      });
    },

    // 工具方法
    formatTime(timeStr) {
      return timeStr ? timeStr.replace('T', ' ').substring(0, 16) : '';
    },
    getAvatarChar(name) {
      return name ? name.charAt(0) : '?';
    }
  }
}
</script>

<style scoped>
/* 样式保持原样，未做任何修改 */
.manage-container { padding: 20px; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background: #f5f7fa; min-height: 100vh; }
.action-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.title-section h2 { margin: 0; font-size: 20px; color: #333; }
.subtitle { margin: 5px 0 0; font-size: 13px; color: #999; }
.search-box { display: flex; }
.search-box input { padding: 8px 12px; border: 1px solid #dcdfe6; border-right: none; border-radius: 4px 0 0 4px; width: 250px; outline: none; }
.btn-search { border: 1px solid #dcdfe6; border-left: none; background: #f5f7fa; border-radius: 0 4px 4px 0; cursor: pointer; width: 40px; }

.table-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #fafafa; padding: 15px; color: #606266; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 15px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.data-table tr:hover { background-color: #f9f9f9; }

.badge-top { background: #f5222d; color: white; font-size: 12px; padding: 2px 6px; border-radius: 4px; font-weight: bold; }
.topic-title { font-weight: 500; color: #333; cursor: default; }
.tag-course { background: #e6f7ff; color: #1890ff; font-size: 12px; padding: 2px 8px; border-radius: 4px; }
.reply-count { font-weight: bold; color: #1890ff; background: #f0f5ff; padding: 2px 8px; border-radius: 10px; font-size: 12px; }
.text-gray { color: #ccc; }

.action-col { display: flex; gap: 8px; }
.btn-text { border: none; background: none; cursor: pointer; font-size: 13px; padding: 0 5px; }
.btn-primary { color: #1890ff; }
.btn-danger { color: #f5222d; }
.btn-warn { color: #fa8c16; }
.btn-xs { font-size: 12px; }

/* 详情弹窗 */
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; width: 800px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); display: flex; flex-direction: column; max-height: 85vh; }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 16px; }
.close-btn { font-size: 20px; cursor: pointer; color: #999; }
.modal-body { padding: 20px; overflow-y: auto; background: #f5f7fa; }

/* 帖子样式 */
.post-card { background: white; border-radius: 8px; padding: 20px; margin-bottom: 20px; border: 1px solid #ebeef5; }
.main-post { border-left: 4px solid #1890ff; }
.post-header { display: flex; justify-content: space-between; margin-bottom: 15px; }
.user-info { display: flex; align-items: center; gap: 10px; }
.avatar { width: 40px; height: 40px; border-radius: 50%; color: white; display: flex; align-items: center; justify-content: center; font-weight: bold; }
.bg-blue { background: #1890ff; }
.bg-gray { background: #ccc; }
.meta { display: flex; flex-direction: column; }
.username { font-weight: bold; color: #333; font-size: 14px; }
.time { font-size: 12px; color: #999; }
.floor-tag { color: #ccc; font-size: 12px; }
.content-title { margin: 0 0 10px 0; font-size: 18px; color: #333; }
.post-content { color: #333; line-height: 1.6; font-size: 14px; white-space: pre-wrap; }

.divider { text-align: center; color: #999; font-size: 12px; margin: 20px 0; }
.reply-post { border-left: 3px solid #eee; margin-left: 20px; }
.floor-actions { display: flex; align-items: center; gap: 10px; }

/* 回复输入框 */
.reply-footer { background: white; padding: 20px; border-top: 1px solid #eee; }
.reply-input-area { display: flex; flex-direction: column; gap: 10px; }
.reply-input-area textarea { width: 100%; border: 1px solid #dcdfe6; border-radius: 4px; padding: 10px; resize: none; box-sizing: border-box; }
.reply-input-area textarea:focus { border-color: #1890ff; outline: none; }
.reply-actions { display: flex; justify-content: flex-end; }
.btn { padding: 8px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; }
.btn-primary { background: #1890ff; color: white; }
.empty-reply { text-align: center; color: #999; padding: 20px; }
</style>