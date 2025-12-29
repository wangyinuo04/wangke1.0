<template>
  <div class="manage-container">
    
    <div v-if="currentView === 'list'" class="view-wrapper fade-in">
      <div class="action-header">
        <div class="title-section">
          <h2>互动讨论管理</h2>
          <p class="subtitle">发布课程公告、答疑解惑及管理学生言论</p>
        </div>
        <div class="operation-section">
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
          <div 
            v-for="topic in filteredTopics" 
            :key="topic.id" 
            class="topic-item"
            :class="{ 'is-pinned': topic.isTop }"
          >
            <div class="col-main" @click="enterTopic(topic)">
              <span v-if="topic.isTop" class="tag-top">置顶</span>
              <span class="topic-title">{{ topic.title }}</span>
              <p class="topic-preview">{{ topic.content }}</p>
            </div>
            <div class="col-author">
              <div class="avatar-circle">{{ topic.author[0] }}</div>
              <span>{{ topic.author }}</span>
            </div>
            <div class="col-stat">
              <span class="count-reply">{{ topic.replyCount }}</span>
              <span class="count-view">/ {{ topic.viewCount }}</span>
            </div>
            <div class="col-time">{{ topic.lastReplyTime }}</div>
            <div class="col-action">
              <button class="btn-text btn-danger" @click.stop="deleteTopic(topic.id)">删除</button>
            </div>
          </div>
          
          <div v-if="filteredTopics.length === 0" class="empty-state">暂无讨论话题</div>
        </div>
      </div>
    </div>

    <div v-else class="view-wrapper fade-in">
      <div class="detail-header-bar">
        <button class="btn-back" @click="currentView = 'list'">← 返回列表</button>
        <h3>详情管理</h3>
      </div>

      <div class="discussion-detail-layout">
        <div class="post-card main-post">
          <div class="post-header">
            <div class="user-info">
              <div class="avatar-circle big">{{ currentTopic.author[0] }}</div>
              <div>
                <div class="user-name">{{ currentTopic.author }} <span class="tag-teacher">教师</span></div>
                <div class="post-time">发布于 {{ currentTopic.publishTime }}</div>
              </div>
            </div>
            <div class="post-title-large">
              <span v-if="currentTopic.isTop" class="tag-top">置顶</span>
              {{ currentTopic.title }}
            </div>
          </div>
          <div class="post-content">
            {{ currentTopic.content }}
          </div>
        </div>

        <div class="replies-section">
          <div class="section-title">共 {{ currentReplies.length }} 条回复</div>
          
          <div v-for="(reply, index) in currentReplies" :key="reply.id" class="post-card reply-item">
            <div class="post-header">
              <div class="user-info">
                <div class="avatar-circle" :class="reply.role === 'teacher' ? 'blue-bg' : 'gray-bg'">
                  {{ reply.author[0] }}
                </div>
                <div>
                  <div class="user-name">
                    {{ reply.author }} 
                    <span v-if="reply.role === 'teacher'" class="tag-teacher">教师</span>
                    <span v-else class="tag-student">学生</span>
                  </div>
                  <div class="post-time">{{ index + 1 }}楼 · {{ reply.time }}</div>
                </div>
              </div>
              <div class="reply-actions">
                <button class="btn-text btn-primary" @click="setReplyTarget(reply)">回复</button>
                <button class="btn-text btn-danger" @click="deleteReply(reply.id)">删除违规</button>
              </div>
            </div>
            <div class="post-content">
              <div v-if="reply.quote" class="quote-box">
                引用 @{{ reply.quote.author }}：{{ reply.quote.content }}
              </div>
              {{ reply.content }}
            </div>
          </div>
          
          <div v-if="currentReplies.length === 0" class="empty-replies">暂无回复，快来抢沙发吧~</div>
        </div>

        <div class="reply-editor-fixed">
          <div class="editor-container">
            <div v-if="replyTarget" class="reply-target-tip">
              正在回复 {{ replyTarget.author }} <span class="close-target" @click="replyTarget = null">×</span>
            </div>
            <div class="input-row">
              <textarea 
                v-model="newReplyContent" 
                :placeholder="replyTarget ? '回复TA的内容...' : '发布您的评论...'"
                rows="2"
              ></textarea>
              <button class="btn btn-primary send-btn" @click="submitReply">发送</button>
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
              <label>主题标题 <span class="required">*</span></label>
              <input type="text" v-model="form.title" placeholder="请输入标题 (如：关于期中考试的疑问)" required>
            </div>
            <div class="form-group">
              <label>内容详情 <span class="required">*</span></label>
              <textarea v-model="form.content" rows="6" placeholder="请输入讨论内容..." required></textarea>
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
export default {
  name: 'DiscussionManage',
  data() {
    return {
      currentView: 'list', // 'list' | 'detail'
      searchQuery: '',
      showCreateModal: false,
      
      // --- 模拟话题数据 ---
      topics: [
        { 
          id: 1, 
          title: '【重要通知】本学期课程作业提交规范说明', 
          content: '请各位同学务必按照附件中的格式提交作业，否则将扣分...', 
          author: '王建国', 
          role: 'teacher',
          publishTime: '2025-09-01', 
          lastReplyTime: '2025-09-02 14:20', 
          replyCount: 12, 
          viewCount: 345, 
          isTop: true 
        },
        { 
          id: 2, 
          title: '关于第三章“需求分析”的难点讨论', 
          content: '大家觉得数据流图绘制中最难的部分是什么？欢迎交流。', 
          author: '李晓梅', 
          role: 'teacher',
          publishTime: '2025-09-10', 
          lastReplyTime: '2025-09-15 09:30', 
          replyCount: 5, 
          viewCount: 120, 
          isTop: false 
        }
      ],

      // --- 详情页数据 ---
      currentTopic: null,
      currentReplies: [],
      // 模拟回复库
      mockRepliesRepo: [
        { id: 101, topicId: 1, author: '张三', role: 'student', content: '收到，老师！', time: '2025-09-01 10:00' },
        { id: 102, topicId: 1, author: '李四', role: 'student', content: '请问PDF格式可以吗？', time: '2025-09-01 10:05' },
        { id: 103, topicId: 1, author: '王建国', role: 'teacher', content: '可以，PDF和Word均可。', time: '2025-09-01 11:00', quote: { author: '李四', content: '请问PDF格式可以吗？' } }
      ],

      // 表单数据
      form: { id: null, title: '', content: '', isTop: false },
      newReplyContent: '',
      replyTarget: null
    }
  },
  computed: {
    filteredTopics() {
      let list = this.topics;
      if (this.searchQuery) {
        list = list.filter(t => t.title.includes(this.searchQuery));
      }
      // 排序：置顶优先，然后按时间倒序
      return list.sort((a, b) => {
        if (a.isTop === b.isTop) return new Date(b.publishTime) - new Date(a.publishTime);
        return a.isTop ? -1 : 1;
      });
    }
  },
  methods: {
    handleSearch() { console.log('Searching...'); },

    // --- 话题管理 (2.6.1) ---
    openCreateModal() {
      this.form = { id: Date.now(), title: '', content: '', isTop: false };
      this.showCreateModal = true;
    },
    closeCreateModal() { this.showCreateModal = false; },
    saveTopic() {
      const newTopic = {
        ...this.form,
        author: '我(教师)',
        role: 'teacher',
        publishTime: new Date().toISOString().split('T')[0],
        lastReplyTime: '刚刚',
        replyCount: 0,
        viewCount: 0
      };
      this.topics.unshift(newTopic);
      this.closeCreateModal();
    },
    deleteTopic(id) {
      if (confirm('确定删除该话题及其所有回复吗？')) {
        this.topics = this.topics.filter(t => t.id !== id);
      }
    },

    // --- 详情与回复管理 (2.6.2) ---
    enterTopic(topic) {
      this.currentTopic = topic;
      // 模拟加载对应话题的回复
      this.currentReplies = this.mockRepliesRepo.filter(r => r.topicId === topic.id || topic.id === 1); // 简单模拟
      this.currentView = 'detail';
      this.replyTarget = null;
      this.newReplyContent = '';
    },
    deleteReply(id) {
      if (confirm('确定删除这条回复吗？(违规处理)')) {
        this.currentReplies = this.currentReplies.filter(r => r.id !== id);
        this.currentTopic.replyCount--;
      }
    },
    setReplyTarget(reply) {
      this.replyTarget = reply;
      // 聚焦输入框 (简单处理)
      const textarea = this.$el.querySelector('textarea');
      if (textarea) textarea.focus();
    },
    submitReply() {
      if (!this.newReplyContent.trim()) return alert('请输入回复内容');
      
      const newReply = {
        id: Date.now(),
        topicId: this.currentTopic.id,
        author: '我(教师)',
        role: 'teacher',
        content: this.newReplyContent,
        time: '刚刚',
        quote: this.replyTarget ? { author: this.replyTarget.author, content: this.replyTarget.content } : null
      };
      
      this.currentReplies.push(newReply);
      this.currentTopic.replyCount++;
      this.currentTopic.lastReplyTime = '刚刚';
      
      this.newReplyContent = '';
      this.replyTarget = null;
      
      // 滚动到底部
      this.$nextTick(() => {
        const container = this.$el.querySelector('.discussion-detail-layout'); // 如果有滚动条在body
        if(container) window.scrollTo(0, document.body.scrollHeight);
      });
    }
  }
}
</script>

<style scoped>
.manage-container { padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; height: 100%; display: flex; flex-direction: column; }

/* 头部样式复用 */
.action-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.title-section h2 { margin: 0; font-size: 20px; color: #333; }
.subtitle { margin: 5px 0 0; font-size: 13px; color: #999; }
.operation-section { display: flex; gap: 15px; }
.search-box { display: flex; }
.search-box input { padding: 8px 12px; border: 1px solid #dcdfe6; border-right: none; border-radius: 4px 0 0 4px; width: 220px; }
.btn-search { border-radius: 0 4px 4px 0; background: #f5f7fa; color: #606266; border: 1px solid #dcdfe6; border-left: none; cursor: pointer; padding: 8px 12px; }

/* 话题列表样式 */
.topic-list-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; }
.list-header { display: flex; background: #f9fafc; padding: 15px 20px; border-bottom: 1px solid #ebeef5; font-weight: 600; color: #606266; }
.col-main { flex: 1; cursor: pointer; }
.col-author { width: 120px; display: flex; align-items: center; gap: 8px; }
.col-stat { width: 100px; text-align: center; color: #909399; font-size: 13px; }
.col-time { width: 140px; color: #909399; font-size: 13px; text-align: right; padding-right: 20px; }
.col-action { width: 80px; text-align: center; }

.topic-item { display: flex; align-items: center; padding: 20px; border-bottom: 1px solid #ebeef5; transition: background 0.2s; }
.topic-item:hover { background: #f5f7fa; }
.topic-item.is-pinned { background: #f0f9ff; } /* 置顶高亮 */

.tag-top { background: #ff4d4f; color: white; padding: 2px 5px; border-radius: 4px; font-size: 12px; margin-right: 8px; vertical-align: middle; }
.topic-title { font-size: 16px; font-weight: 500; color: #333; }
.topic-title:hover { color: #1890ff; text-decoration: underline; }
.topic-preview { margin: 5px 0 0; font-size: 13px; color: #999; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 500px; }

.avatar-circle { width: 32px; height: 32px; background: #e6f7ff; color: #1890ff; border-radius: 50%; display: flex; justify-content: center; align-items: center; font-size: 14px; font-weight: bold; }
.count-reply { color: #1890ff; font-weight: bold; }

/* 详情页样式 */
.detail-header-bar { display: flex; align-items: center; gap: 15px; margin-bottom: 20px; }
.btn-back { border: none; background: none; color: #606266; font-size: 14px; cursor: pointer; font-weight: bold; }
.btn-back:hover { color: #1890ff; }

.discussion-detail-layout { padding-bottom: 80px; /* 为底部输入框留空 */ }

.post-card { background: #fff; border-radius: 8px; padding: 25px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.03); }
.main-post { border-left: 4px solid #1890ff; }

.post-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.user-info { display: flex; gap: 15px; align-items: center; }
.avatar-circle.big { width: 48px; height: 48px; font-size: 20px; background: #1890ff; color: white; }
.avatar-circle.blue-bg { background: #e6f7ff; color: #1890ff; }
.avatar-circle.gray-bg { background: #f5f5f5; color: #999; }

.user-name { font-weight: bold; font-size: 15px; color: #333; }
.post-time { font-size: 12px; color: #999; margin-top: 2px; }
.tag-teacher { background: #1890ff; color: white; padding: 1px 4px; border-radius: 3px; font-size: 11px; margin-left: 5px; }
.tag-student { background: #f0f0f0; color: #999; padding: 1px 4px; border-radius: 3px; font-size: 11px; margin-left: 5px; }

.post-title-large { font-size: 22px; font-weight: bold; margin-top: 15px; color: #303133; }
.post-content { font-size: 15px; line-height: 1.6; color: #444; white-space: pre-wrap; }

.replies-section { margin-top: 30px; }
.section-title { font-size: 16px; font-weight: bold; margin-bottom: 15px; padding-left: 10px; border-left: 3px solid #1890ff; }

.reply-item { border: 1px solid #ebeef5; box-shadow: none; }
.quote-box { background: #f5f7fa; padding: 10px; border-radius: 4px; color: #666; font-size: 13px; margin-bottom: 10px; border-left: 3px solid #dcdfe6; }

/* 底部固定回复框 */
.reply-editor-fixed { position: fixed; bottom: 0; left: 250px; /* 适配侧边栏宽度，如果全屏则设为0 */ right: 0; background: white; padding: 15px 30px; border-top: 1px solid #ebeef5; box-shadow: 0 -2px 10px rgba(0,0,0,0.05); z-index: 100; }
.editor-container { max-width: 1000px; margin: 0 auto; }
.input-row { display: flex; gap: 15px; align-items: flex-end; }
textarea { flex: 1; padding: 10px; border: 1px solid #dcdfe6; border-radius: 4px; resize: none; font-family: inherit; }
textarea:focus { border-color: #1890ff; outline: none; }
.send-btn { height: 40px; padding: 0 25px; }
.reply-target-tip { font-size: 12px; color: #666; margin-bottom: 5px; background: #f0f9ff; display: inline-block; padding: 2px 8px; border-radius: 10px; }
.close-target { cursor: pointer; margin-left: 5px; color: #999; }

/* 按钮 & 弹窗通用 */
.btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; transition: all 0.3s; font-size: 14px; }
.btn-primary { background: #1890ff; color: white; } .btn-primary:hover { background: #40a9ff; }
.btn-secondary { background: white; border: 1px solid #dcdfe6; color: #606266; }
.btn-text { background: none; border: none; cursor: pointer; font-size: 13px; padding: 0 5px; }
.btn-danger { color: #f5222d; }
.empty-state, .empty-replies { text-align: center; color: #ccc; padding: 40px; }

.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; width: 600px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: fade 0.3s; }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 16px; }
.close-btn { font-size: 20px; cursor: pointer; color: #999; }
.modal-body { padding: 20px; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 500; font-size: 13px; }
.form-group input[type="text"], textarea { width: 100%; padding: 8px; border: 1px solid #dcdfe6; border-radius: 4px; box-sizing: border-box; }
.checkbox-group label { display: flex; align-items: center; gap: 5px; cursor: pointer; }
.hint { font-size: 12px; color: #999; margin-left: 20px; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }

@keyframes fade { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
.fade-in { animation: fade 0.3s; }

/* 响应式调整 */
@media (max-width: 768px) {
  .reply-editor-fixed { left: 0; padding: 10px; }
  .topic-list-card { overflow-x: auto; }
  .list-header { min-width: 600px; }
}
</style>