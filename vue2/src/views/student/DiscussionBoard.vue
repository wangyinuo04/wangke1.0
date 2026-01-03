<template>
  <div class="manage-container">
    
    <div class="action-header">
      <div class="title-section">
        <h2>互动讨论区</h2>
        <p class="subtitle">师生交流、答疑解惑与学术探讨</p>
      </div>
      <div class="operation-section">
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="搜索话题标题..."
            @keyup.enter="handleSearch"
          >
          <button class="btn-search" @click="handleSearch">🔍</button>
        </div>
        <button class="btn btn-primary" @click="openCreateModal">💬 发布新话题</button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>话题标题</th>
            <th width="120">发布人</th>
            <th width="100">回复数</th>
            <th width="160">最后更新</th>
            <th width="140">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="topic in filteredTopics" :key="topic.topicId">
            <td class="title-col">
              <span v-if="topic.topOrder > 0" class="badge-top">置顶</span>
              
              <span class="topic-title" :title="topic.topicTitle">{{ topic.topicTitle }}</span>
              <span v-if="topic.role" class="tag-course">{{ topic.role }}</span>
            </td>
            <td>{{ topic.authorName }}</td>
            <td>
              <span class="reply-count">{{ topic.replyCount }}</span>
            </td>
            <td class="time-col">{{ formatTime(topic.lastReplyTime) }}</td>
            <td>
              <div class="action-col">
                <button class="btn-op op-primary" @click="openDetailModal(topic)">
                  参与讨论
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="filteredTopics.length === 0">
            <td colspan="5" class="empty-state">
              {{ isLoading ? '加载中...' : '暂无相关话题，快来发布第一个吧！' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="modal-mask" v-if="showCreateModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>发布新话题</h3>
          <span class="close-btn" @click="closeCreateModal">×</span>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveTopic">
            <div class="form-group">
              <label>关联课程 (必选)</label>
              <select v-model="createForm.classId">
                <option value="">-- 公共讨论区 (全校可见) --</option>
                <option v-for="c in myCourses" :key="c.classId" :value="c.classId">
                  {{ c.courseName }} - {{ c.className }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>话题标题 <span class="text-red">*</span></label>
              <input type="text" v-model="createForm.title" placeholder="请输入清晰的标题" required>
            </div>
            <div class="form-group">
              <label>详细内容 <span class="text-red">*</span></label>
              <textarea 
                v-model="createForm.content" 
                rows="6" 
                placeholder="请详细描述您的问题或观点..." 
                required
              ></textarea>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeCreateModal">取消</button>
              <button type="submit" class="btn btn-primary">立即发布</button>
            </div>
          </form>
        </div>
      </div>
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
                  <div class="avatar bg-gray">
                    {{ getAvatarChar(reply.authorName) }}
                  </div>
                  <div class="meta">
                    <span class="username">
                      {{ reply.authorName }}
                    </span>
                    <span class="time">{{ formatTime(reply.publishTime) }}</span>
                  </div>
                </div>
                <div class="floor-tag">#{{ index + 1 }}</div>
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
              placeholder="发表您的看法... (Ctrl+Enter 发送)" 
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
// 引入 API
import { getStudentTopics, createTopic, getTopicDetail, createReply } from '@/api/discussion'
import { getMyCourses } from '@/api/student' // 复用获取课程接口

export default {
  name: 'DiscussionBoard',
  data() {
    return {
      searchQuery: '',
      isLoading: false,
      
      showCreateModal: false,
      showDetailModal: false,

      // 表单数据
      createForm: { title: '', content: '', classId: '' },
      replyContent: '',
      
      // 数据源
      topics: [],
      myCourses: [], // 课程下拉选项
      
      // 详情数据
      currentTopic: {},
      currentReplies: []
    }
  },
  computed: {
    filteredTopics() {
      let list = this.topics;
      if (this.searchQuery) {
        const q = this.searchQuery.toLowerCase();
        list = list.filter(t => t.topicTitle.toLowerCase().includes(q) || t.authorName.includes(q));
      }
      return list;
    }
  },
  created() {
    this.fetchData();
    this.fetchCourses();
  },
  methods: {
    // 1. 获取话题列表
    fetchData() {
      this.isLoading = true;
      getStudentTopics().then(res => {
        if (res.success) {
          this.topics = res.data;
        }
      }).finally(() => {
        this.isLoading = false;
      });
    },

    // 2. 获取课程列表（用于发布时选择班级）
    fetchCourses() {
      getMyCourses().then(res => {
        if (res.success) {
          this.myCourses = res.data;
        }
      });
    },

    handleSearch() { /* computed */ },

    openCreateModal() {
      this.createForm = { title: '', content: '', classId: '' };
      this.showCreateModal = true;
    },
    closeCreateModal() {
      this.showCreateModal = false;
    },

    // 3. 发布话题
    saveTopic() {
      if (!this.createForm.title || !this.createForm.content) return;
      
      const payload = {
        topicTitle: this.createForm.title,
        topicContent: this.createForm.content,
        classId: this.createForm.classId // 如果为空则是公共话题
      };

      createTopic(payload).then(res => {
        if (res.success) {
          alert('发布成功！');
          this.closeCreateModal();
          this.fetchData(); // 刷新列表
        } else {
          alert('发布失败：' + res.message);
        }
      });
    },

    // 4. 打开详情
    openDetailModal(topic) {
      // 先清空
      this.currentTopic = {};
      this.currentReplies = [];
      this.replyContent = '';
      this.showDetailModal = true;

      getTopicDetail(topic.topicId).then(res => {
        if (res.success) {
          this.currentTopic = res.data.topic;
          this.currentReplies = res.data.replies;
        }
      });
    },
    closeDetailModal() {
      this.showDetailModal = false;
    },

    // 5. 发送回复
    submitReply() {
      if (!this.replyContent.trim()) return;

      const payload = {
        topicId: this.currentTopic.topicId,
        replyContent: this.replyContent
      };

      createReply(payload).then(res => {
        if (res.success) {
          this.replyContent = '';
          // 重新加载详情以显示新回复
          this.openDetailModal(this.currentTopic);
        } else {
          alert(res.message);
        }
      });
    },

    // 工具方法
    formatTime(timeStr) {
      if(!timeStr) return '';
      return timeStr.replace('T', ' ').substring(0, 16);
    },
    getAvatarChar(name) {
      return name ? name.charAt(0) : '?';
    }
  }
}
</script>

<style scoped>
/* 样式保持原样，直接复用你提供的 CSS */
.manage-container { padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background: #f5f7fa; min-height: 100vh; }
.action-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.title-section h2 { margin: 0; font-size: 20px; color: #333; }
.subtitle { margin: 5px 0 0; font-size: 13px; color: #999; }
.operation-section { display: flex; gap: 15px; }
.search-box { display: flex; }
.search-box input { padding: 8px 12px; border: 1px solid #dcdfe6; border-right: none; border-radius: 4px 0 0 4px; width: 250px; outline: none; }
.search-box input:focus { border-color: #1890ff; }
.btn-search { border: 1px solid #dcdfe6; border-left: none; background: #f5f7fa; border-radius: 0 4px 4px 0; cursor: pointer; width: 40px; }
.btn-search:hover { background: #e6f7ff; color: #1890ff; }
.table-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #fafafa; padding: 15px; color: #606266; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 15px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.data-table tr:hover { background-color: #f9f9f9; }
.title-col { max-width: 450px; display: flex; align-items: center; gap: 8px; }
.topic-title { font-weight: 500; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 320px; cursor: default; }
.tag-course { background: #e6f7ff; color: #1890ff; font-size: 12px; padding: 1px 6px; border-radius: 4px; border: 1px solid #91d5ff; white-space: nowrap; }
.badge-top { background: #f5222d; color: white; font-size: 12px; padding: 1px 5px; border-radius: 3px; font-weight: bold; white-space: nowrap; }
.reply-count { font-weight: bold; color: #1890ff; background: #f0f5ff; padding: 2px 8px; border-radius: 10px; font-size: 12px; }
.time-col { font-family: monospace; font-size: 13px; color: #909399; }
.action-col { display: flex; gap: 8px; align-items: center; }
.btn-op { border: none; cursor: pointer; font-size: 13px; padding: 5px 12px; border-radius: 4px; transition: all 0.2s; background: transparent; }
.op-primary { background-color: #1890ff; color: white; }
.op-primary:hover { background-color: #40a9ff; }
.empty-state { text-align: center; padding: 40px; color: #999; }
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; width: 550px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: fade 0.3s; display: flex; flex-direction: column; max-height: 85vh; }
.wide-modal-xl { width: 800px; height: 85vh; }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 16px; }
.close-btn { font-size: 20px; cursor: pointer; color: #999; }
.close-btn:hover { color: #333; }
.modal-body { padding: 20px; overflow-y: auto; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; font-weight: 500; font-size: 14px; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 8px; border: 1px solid #dcdfe6; border-radius: 4px; box-sizing: border-box; font-size: 14px; }
.text-red { color: #f5222d; }
.discussion-container { background: #f5f7fa; padding: 20px; }
.post-card { background: white; border-radius: 8px; padding: 20px; margin-bottom: 20px; border: 1px solid #ebeef5; box-shadow: 0 2px 6px rgba(0,0,0,0.02); }
.main-post { border-left: 4px solid #1890ff; }
.post-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 15px; }
.user-info { display: flex; align-items: center; gap: 10px; }
.avatar { width: 40px; height: 40px; border-radius: 50%; color: white; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 16px; }
.bg-blue { background: #1890ff; }
.bg-orange { background: #fa8c16; }
.bg-gray { background: #ccc; }
.meta { display: flex; flex-direction: column; }
.username { font-weight: bold; color: #333; font-size: 14px; }
.time { font-size: 12px; color: #999; margin-top: 2px; }
.floor-tag { color: #ccc; font-size: 12px; }
.post-content { color: #333; line-height: 1.6; font-size: 14px; white-space: pre-wrap; }
.content-title { margin: 0 0 10px 0; font-size: 18px; color: #333; }
.reply-list .divider { text-align: center; color: #999; font-size: 12px; margin: 20px 0; position: relative; }
.reply-list .divider::before, .reply-list .divider::after { content: ''; position: absolute; top: 50%; width: 40%; height: 1px; background: #e8e8e8; }
.reply-list .divider::before { left: 0; }
.reply-list .divider::after { right: 0; }
.reply-post { margin-left: 20px; border-left: 3px solid #eee; }
.empty-reply { text-align: center; color: #999; padding: 20px; }
.reply-footer { background: white; padding: 20px 30px; border-top: 1px solid #eee; box-sizing: border-box; }
.reply-input-area { display: flex; flex-direction: column; gap: 12px; width: 100%; }
.reply-input-area textarea { width: 100%; border: 1px solid #dcdfe6; border-radius: 4px; padding: 12px; resize: none; font-family: inherit; box-sizing: border-box; font-size: 14px; }
.reply-input-area textarea:focus { border-color: #1890ff; outline: none; }
.reply-actions { display: flex; justify-content: flex-end; }
.btn { padding: 8px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; transition: all 0.3s; }
.btn-primary { background: #1890ff; color: white; }
.btn-primary:hover { background: #40a9ff; }
.btn-secondary { background: white; border: 1px solid #dcdfe6; color: #606266; }
@keyframes fade { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
</style>