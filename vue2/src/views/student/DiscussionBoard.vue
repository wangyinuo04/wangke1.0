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
          <tr v-for="topic in filteredTopics" :key="topic.id">
            <td class="title-col">
              <span v-if="topic.isTop" class="badge-top">置顶</span>
              
              <span class="topic-title" :title="topic.title">{{ topic.title }}</span>
              <span v-if="topic.courseName" class="tag-course">{{ topic.courseName }}</span>
            </td>
            <td>{{ topic.author }}</td>
            <td>
              <span class="reply-count">{{ topic.replyCount }}</span>
            </td>
            <td class="time-col">{{ topic.lastUpdateTime }}</td>
            <td>
              <div class="action-col">
                <button class="btn-op op-primary" @click="openDetailModal(topic)">
                  参与讨论
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="filteredTopics.length === 0">
            <td colspan="5" class="empty-state">暂无相关话题，快来发布第一个吧！</td>
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
              <label>关联课程 (可选)</label>
              <select v-model="createForm.courseName">
                <option value="">-- 公共讨论区 --</option>
                <option value="软件工程导论">软件工程导论</option>
                <option value="Web前端开发">Web前端开发</option>
                <option value="数据库原理">数据库原理</option>
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
                <div class="avatar bg-blue">{{ currentTopic.author[0] }}</div>
                <div class="meta">
                  <span class="username">{{ currentTopic.author }}</span>
                  <span class="time">{{ currentTopic.publishTime }}</span>
                </div>
              </div>
              <div class="floor-tag">楼主</div>
            </div>
            <div class="post-content">
              <h2 class="content-title">{{ currentTopic.title }}</h2>
              <div class="content-body">{{ currentTopic.content }}</div>
            </div>
          </div>

          <div class="reply-list">
            <div class="divider">共 {{ currentTopic.replies.length }} 条回复</div>
            
            <div v-for="(reply, index) in currentTopic.replies" :key="reply.id" class="post-card reply-post">
              <div class="post-header">
                <div class="user-info">
                  <div class="avatar" :class="reply.isTeacher ? 'bg-orange' : 'bg-gray'">
                    {{ reply.author[0] }}
                  </div>
                  <div class="meta">
                    <span class="username">
                      {{ reply.author }}
                      <span v-if="reply.isTeacher" class="tag-teacher">教师</span>
                    </span>
                    <span class="time">{{ reply.time }}</span>
                  </div>
                </div>
                <div class="floor-tag">#{{ index + 1 }}</div>
              </div>
              <div class="post-content">
                {{ reply.content }}
              </div>
            </div>
            
            <div v-if="currentTopic.replies.length === 0" class="empty-reply">
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
export default {
  name: 'DiscussionBoard',
  data() {
    return {
      searchQuery: '',
      studentName: '我', 
      
      showCreateModal: false,
      showDetailModal: false,

      createForm: { title: '', content: '', courseName: '' },
      replyContent: '',
      
      currentTopic: {},

      topics: [
        {
          id: 1,
          title: '关于需求分析中ER图多对多关系的转换问题',
          courseName: '软件工程导论',
          author: '张三',
          publishTime: '2025-09-12 10:00',
          lastUpdateTime: '2025-09-12 14:30',
          content: '老师，在将ER图转换为关系模式时，多对多关系是否必须单独创建一个关系表？如果属性很少，能否合并到其中一端？求解答！',
          replyCount: 2,
          isTop: true, // 置顶
          replies: [
            { id: 101, author: '李四', time: '2025-09-12 10:30', content: '我觉得必须单独建表，否则会有冗余。', isTeacher: false },
            { id: 102, author: '王建国', time: '2025-09-12 14:30', content: '同学你好，多对多关系在规范化设计中，通常建议转换为独立的关系模式，主键为两端实体主键的组合。', isTeacher: true }
          ]
        },
        {
          id: 2,
          title: 'Vue3 的 setup 语法糖中如何定义 props？',
          courseName: 'Web前端开发',
          author: '赵六',
          publishTime: '2025-10-05 09:20',
          lastUpdateTime: '2025-10-05 09:20',
          content: '如题，刚开始学 Vue3，不太清楚 defineProps 怎么用，有没有同学给个例子？',
          replyCount: 0,
          isTop: false,
          replies: []
        },
        {
          id: 3,
          title: '期末考试范围划定了吗？',
          courseName: '', 
          author: '孙七',
          publishTime: '2025-11-01 16:00',
          lastUpdateTime: '2025-11-02 08:00',
          content: '听说这次考试很难，老师有没有说重点在哪几章？',
          replyCount: 1,
          isTop: false,
          replies: [
            { id: 301, author: '周八', time: '2025-11-02 08:00', content: '老师课上说了，重点是第3、4、5章。', isTeacher: false }
          ]
        }
      ]
    }
  },
  computed: {
    filteredTopics() {
      let list = this.topics;
      if (this.searchQuery) {
        const q = this.searchQuery.toLowerCase();
        list = list.filter(t => t.title.toLowerCase().includes(q) || t.author.includes(q));
      }
      // 排序：置顶优先，然后按最后更新时间倒序
      return list.sort((a, b) => {
        if (a.isTop !== b.isTop) return a.isTop ? -1 : 1;
        return new Date(b.lastUpdateTime) - new Date(a.lastUpdateTime);
      });
    }
  },
  methods: {
    handleSearch() { console.log('Searching...'); },

    openCreateModal() {
      this.createForm = { title: '', content: '', courseName: '' };
      this.showCreateModal = true;
    },
    closeCreateModal() {
      this.showCreateModal = false;
    },
    saveTopic() {
      if (!this.createForm.title || !this.createForm.content) return;
      
      const now = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-');
      const newTopic = {
        id: Date.now(),
        title: this.createForm.title,
        courseName: this.createForm.courseName,
        author: this.studentName,
        publishTime: now,
        lastUpdateTime: now,
        content: this.createForm.content,
        replyCount: 0,
        isTop: false,
        replies: []
      };
      
      this.topics.unshift(newTopic);
      this.closeCreateModal();
      alert('发布成功！');
    },

    openDetailModal(topic) {
      this.currentTopic = topic;
      this.replyContent = '';
      this.showDetailModal = true;
    },
    closeDetailModal() {
      this.showDetailModal = false;
    },

    submitReply() {
      if (!this.replyContent.trim()) {
        alert('请输入回复内容');
        return;
      }
      
      const now = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-');
      const newReply = {
        id: Date.now(),
        author: this.studentName,
        time: now,
        content: this.replyContent,
        isTeacher: false
      };

      this.currentTopic.replies.push(newReply);
      this.currentTopic.replyCount++;
      this.currentTopic.lastUpdateTime = now;
      
      this.replyContent = '';
      
      this.$nextTick(() => {
        const container = document.querySelector('.discussion-container');
        if (container) container.scrollTop = container.scrollHeight;
      });
    }
  }
}
</script>

<style scoped>
.manage-container { padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background: #f5f7fa; min-height: 100vh; }

/* 顶部栏 */
.action-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.title-section h2 { margin: 0; font-size: 20px; color: #333; }
.subtitle { margin: 5px 0 0; font-size: 13px; color: #999; }
.operation-section { display: flex; gap: 15px; }

/* 搜索框 */
.search-box { display: flex; }
.search-box input { padding: 8px 12px; border: 1px solid #dcdfe6; border-right: none; border-radius: 4px 0 0 4px; width: 250px; outline: none; }
.search-box input:focus { border-color: #1890ff; }
.btn-search { border: 1px solid #dcdfe6; border-left: none; background: #f5f7fa; border-radius: 0 4px 4px 0; cursor: pointer; width: 40px; }
.btn-search:hover { background: #e6f7ff; color: #1890ff; }

/* 表格样式 */
.table-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #fafafa; padding: 15px; color: #606266; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 15px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.data-table tr:hover { background-color: #f9f9f9; }

/* 列样式 */
.title-col { max-width: 450px; display: flex; align-items: center; gap: 8px; } /* 使用 Flex 布局排列图标和标题 */
.topic-title { font-weight: 500; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 320px; cursor: default; }
.tag-course { background: #e6f7ff; color: #1890ff; font-size: 12px; padding: 1px 6px; border-radius: 4px; border: 1px solid #91d5ff; white-space: nowrap; }
/* 置顶徽标 */
.badge-top { background: #f5222d; color: white; font-size: 12px; padding: 1px 5px; border-radius: 3px; font-weight: bold; white-space: nowrap; }

.reply-count { font-weight: bold; color: #1890ff; background: #f0f5ff; padding: 2px 8px; border-radius: 10px; font-size: 12px; }
.time-col { font-family: monospace; font-size: 13px; color: #909399; }

/* 操作按钮 */
.action-col { display: flex; gap: 8px; align-items: center; }
.btn-op { border: none; cursor: pointer; font-size: 13px; padding: 5px 12px; border-radius: 4px; transition: all 0.2s; background: transparent; }
.op-primary { background-color: #1890ff; color: white; }
.op-primary:hover { background-color: #40a9ff; }

.empty-state { text-align: center; padding: 40px; color: #999; }

/* 弹窗通用 */
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; width: 550px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: fade 0.3s; display: flex; flex-direction: column; max-height: 85vh; }
.wide-modal-xl { width: 800px; height: 85vh; }

.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 16px; }
.close-btn { font-size: 20px; cursor: pointer; color: #999; }
.close-btn:hover { color: #333; }
.modal-body { padding: 20px; overflow-y: auto; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }

/* 表单样式 */
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; font-weight: 500; font-size: 14px; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 8px; border: 1px solid #dcdfe6; border-radius: 4px; box-sizing: border-box; font-size: 14px; }
.text-red { color: #f5222d; }

/* 讨论详情样式 */
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
.tag-teacher { background: #fff7e6; color: #fa8c16; font-size: 10px; padding: 0 4px; border: 1px solid #ffd591; border-radius: 2px; margin-left: 5px; }
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

/* 底部回复框 - 修复布局 */
.reply-footer { 
  background: white; 
  padding: 20px 30px; /* 增加内边距 */
  border-top: 1px solid #eee;
  box-sizing: border-box; /* 确保 padding 不撑大宽度 */
}
.reply-input-area { 
  display: flex; 
  flex-direction: column; 
  gap: 12px; /* 增加元素间距 */
  width: 100%;
}
.reply-input-area textarea { 
  width: 100%; 
  border: 1px solid #dcdfe6; 
  border-radius: 4px; 
  padding: 12px; 
  resize: none; 
  font-family: inherit; 
  box-sizing: border-box; /* 关键：防止宽度溢出 */
  font-size: 14px;
}
.reply-input-area textarea:focus { border-color: #1890ff; outline: none; }
.reply-actions { display: flex; justify-content: flex-end; } /* 按钮右对齐容器 */

/* 按钮通用 */
.btn { padding: 8px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; transition: all 0.3s; }
.btn-primary { background: #1890ff; color: white; }
.btn-primary:hover { background: #40a9ff; }
.btn-secondary { background: white; border: 1px solid #dcdfe6; color: #606266; }

@keyframes fade { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
</style>