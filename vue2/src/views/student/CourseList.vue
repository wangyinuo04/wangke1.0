<template>
  <div class="manage-container">
    <div class="action-header">
      <div class="title-section">
        <h2>我的课程列表</h2>
        <p class="subtitle">查看已选修课程，使用邀请码加入新班级</p>
      </div>
      <div class="operation-section">
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="搜索课程或教师..."
            @keyup.enter="handleSearch"
          >
          <button class="btn btn-search" @click="handleSearch">🔍 搜索</button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th width="120">学期</th>
            <th>课程名称</th>
            <th>教学班级</th>
            <th>授课教师</th>
            <th width="80">学分</th>
            <th width="100">状态</th>
            <th width="220">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="course in filteredCourses" :key="course.id">
            <td><span class="term-tag">{{ course.semester }}</span></td>
            <td class="course-name-col">{{ course.courseName }}</td>
            <td>{{ course.className }}</td>
            <td>{{ course.teacherName }}</td>
            <td class="credit-col">{{ course.credits }}</td>
            <td>
              <span class="status-badge status-active">进行中</span>
            </td>
            <td>
              <div class="action-col">
                <button class="btn-text btn-resource" @click="openResourceModal(course)">
                  📂 教学资源
                </button>
                <button class="btn-text btn-danger" @click="quitCourse(course)">
                  退课
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="filteredCourses.length === 0">
            <td colspan="7" class="empty-state">
              暂无课程数据
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="modal-mask" v-if="showJoinModal">
      <div class="modal-box join-modal">
        <div class="modal-header">
          <h3>加入新课程</h3>
          <span class="close-btn" @click="closeJoinModal">×</span>
        </div>
        <div class="modal-body align-center">
          <div class="icon-placeholder">🎫</div>
          <p class="instruction">请输入老师提供的 6 位班级邀请码</p>
          
          <div class="input-wrapper">
            <input 
              type="text" 
              v-model="inviteCode" 
              class="code-input" 
              placeholder="请输入邀请码" 
              maxlength="6"
              @keyup.enter="handleJoin"
            >
          </div>

          <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeJoinModal">取消</button>
          <button class="btn btn-primary" @click="handleJoin" :disabled="!inviteCode">立即加入</button>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showResourceModal">
      <div class="modal-box wide-modal">
        <div class="modal-header">
          <h3>{{ currentCourse.courseName }} - 教学资源</h3>
          <span class="close-btn" @click="closeResourceModal">×</span>
        </div>
        <div class="modal-body">
          <div class="resource-list">
            <div v-if="currentResources.length === 0" class="empty-resource">
              暂无上传的教学资源
            </div>
            
            <div v-else class="resource-table-wrapper">
              <table class="data-table resource-table">
                <thead>
                  <tr>
                    <th>资源名称</th>
                    <th width="80">类型</th>
                    <th width="120">上传时间</th>
                    <th width="80">状态</th>
                    <th width="150">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="res in currentResources" :key="res.id">
                    <td>
                      <div class="res-name-wrapper">
                        <span class="file-icon">{{ getFileIcon(res.type) }}</span>
                        <span class="res-name" :title="res.name">{{ res.name }}</span>
                      </div>
                    </td>
                    <td>{{ res.type }}</td>
                    <td class="text-gray">{{ res.uploadDate }}</td>
                    <td>
                      <span class="read-badge" :class="res.isRead ? 'read' : 'unread'">
                        {{ res.isRead ? '已读' : '未读' }}
                      </span>
                    </td>
                    <td>
                      <div class="action-col">
                        <button class="btn-text btn-view" @click="previewResource(res)">
                          👁️ 预览
                        </button>
                        <button 
                          class="btn-text btn-down" 
                          v-if="res.allowDownload"
                          @click="downloadResource(res)"
                        >
                          📥 下载
                        </button>
                        <span v-else class="text-disabled">不可下载</span>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="closeResourceModal">关闭</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'CourseList',
  data() {
    return {
      searchQuery: '',
      
      // 加入课程相关
      showJoinModal: false,
      inviteCode: '',
      errorMsg: '',

      // 资源弹窗相关
      showResourceModal: false,
      currentCourse: {},
      currentResources: [],
      
      // 模拟课程数据
      myCourses: [
        { 
          id: 1, 
          semester: '2025-2026-1', 
          courseName: '软件工程导论', 
          className: '软件2201班', 
          teacherName: '王建国', 
          credits: 3.0 
        },
        { 
          id: 2, 
          semester: '2025-2026-1', 
          courseName: 'Web前端开发', 
          className: '计科卓越班', 
          teacherName: '李晓梅', 
          credits: 4.0 
        },
        { 
          id: 3, 
          semester: '2025-2026-1', 
          courseName: '数据库原理', 
          className: '软件2201班', 
          teacherName: '张伟', 
          credits: 3.5 
        }
      ],

      // 模拟资源库
      mockResourceDB: {
        1: [
          { id: 101, name: '第一章：软件工程概述.ppt', type: 'PPT', uploadDate: '2025-09-01', isRead: true, allowDownload: true },
          { id: 102, name: '需求分析实验指导书.pdf', type: 'PDF', uploadDate: '2025-09-05', isRead: false, allowDownload: true },
          { id: 103, name: '敏捷开发教学视频.mp4', type: 'Video', uploadDate: '2025-09-10', isRead: false, allowDownload: false }
        ],
        2: [
          { id: 201, name: 'Vue3 基础语法.pdf', type: 'PDF', uploadDate: '2025-09-02', isRead: true, allowDownload: true },
          { id: 202, name: '组件通信源码示例.zip', type: 'Code', uploadDate: '2025-09-12', isRead: false, allowDownload: true }
        ]
      }
    }
  },
  computed: {
    filteredCourses() {
      if (!this.searchQuery) return this.myCourses;
      const q = this.searchQuery.toLowerCase();
      return this.myCourses.filter(c => 
        c.courseName.toLowerCase().includes(q) || 
        c.teacherName.includes(q)
      );
    }
  },
  methods: {
    handleSearch() {
      console.log('Searching:', this.searchQuery);
    },

    openJoinModal() {
      this.inviteCode = '';
      this.errorMsg = '';
      this.showJoinModal = true;
    },
    closeJoinModal() {
      this.showJoinModal = false;
    },
    handleJoin() {
      if (this.inviteCode.length !== 6) {
        this.errorMsg = '请输入完整的6位邀请码';
        return;
      }
      if (this.inviteCode.toUpperCase() === 'A8J9K2') {
        alert('🎉 加入成功！\n课程：人工智能基础 - 2023级合班');
        this.myCourses.push({
          id: 4, 
          semester: '2025-2026-1',
          courseName: '人工智能基础',
          className: '2023级合班',
          teacherName: '赵强',
          credits: 3.0
        });
        this.closeJoinModal();
      } else {
        this.errorMsg = '无效的邀请码，请检查后重试';
      }
    },

    quitCourse(course) {
      if (confirm(`确定要退出 "${course.courseName}" 吗？\n退出后将无法提交作业和查看资料。`)) {
        this.myCourses = this.myCourses.filter(c => c.id !== course.id);
      }
    },

    openResourceModal(course) {
      this.currentCourse = course;
      this.currentResources = this.mockResourceDB[course.id] || [];
      this.showResourceModal = true;
    },
    closeResourceModal() {
      this.showResourceModal = false;
      this.currentResources = [];
    },
    getFileIcon(type) {
      const map = {
        'PPT': '📊', 'PDF': '📄', 'Video': '🎬', 'Word': '📝', 'Code': '💻'
      };
      return map[type] || '📁';
    },
    previewResource(res) {
      alert(`正在打开预览：${res.name}\n\n系统已记录您的阅读状态。`);
      res.isRead = true;
    },
    downloadResource(res) {
      if (res.allowDownload) {
        alert(`开始下载文件：${res.name}`);
      } else {
        alert('该资源不允许下载');
      }
    }
  }
}
</script>

<style scoped>
/* 保持统一的页面布局风格 */
.manage-container { padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; }

/* 顶部操作栏 */
.action-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.title-section h2 { margin: 0; font-size: 20px; color: #333; }
.subtitle { margin: 5px 0 0; font-size: 13px; color: #999; }
.operation-section { display: flex; gap: 15px; }

/* 搜索框 */
.search-box { display: flex; }
.search-box input { padding: 8px 12px; border: 1px solid #dcdfe6; border-right: none; border-radius: 4px 0 0 4px; outline: none; font-size: 14px; width: 220px; transition: border 0.3s; }
.search-box input:focus { border-color: #1890ff; }
.btn-search { border-radius: 0 4px 4px 0; background: #f5f7fa; color: #606266; border: 1px solid #dcdfe6; border-left: none; cursor: pointer; padding: 8px 12px; }
.btn-search:hover { background: #e6f7ff; color: #1890ff; }

/* 按钮通用 */
.btn { padding: 8px 16px; border: none; cursor: pointer; font-size: 14px; border-radius: 4px; transition: all 0.3s; }
.btn-primary { background: #1890ff; color: white; box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3); }
.btn-primary:hover:not(:disabled) { background: #40a9ff; }
.btn-primary:disabled { background: #a0cfff; cursor: not-allowed; }
.btn-secondary { background: #fff; border: 1px solid #dcdfe6; color: #606266; }
.btn-secondary:hover { color: #1890ff; border-color: #c6e2ff; background: #ecf5ff; }

/* 表格样式 */
.table-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #fafafa; padding: 16px; color: #333; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 16px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.data-table tr:hover { background-color: #f5f7fa; }

/* 表格内元素 */
.term-tag { background: #f0f5ff; color: #2f54eb; border: 1px solid #adc6ff; padding: 2px 6px; border-radius: 4px; font-size: 12px; font-family: monospace; }
.course-name-col { font-weight: 600; color: #333; font-size: 15px; }
.credit-col { font-weight: bold; color: #1890ff; }
.status-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.status-active { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }

/* 操作按钮 */
.action-col { display: flex; gap: 12px; align-items: center; }
.btn-text { background: none; border: none; cursor: pointer; font-size: 13px; padding: 4px 8px; border-radius: 4px; transition: all 0.2s; }

/* 重点修复：教学资源按钮样式 */
.btn-text.btn-resource { 
  color: #13c2c2; /* 使用清爽的青色/Teal色，避免全蓝 */
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}
.btn-text.btn-resource:hover { 
  background-color: #e6fffb; /* 悬停时淡青色背景，不刺眼 */
  color: #08979c;
}

/* 退课按钮 */
.btn-text.btn-danger { color: #ff4d4f; } 
.btn-text.btn-danger:hover { background-color: #fff1f0; }

.btn-text.btn-view { color: #1890ff; }
.btn-text.btn-view:hover { text-decoration: underline; }
.btn-text.btn-down { color: #52c41a; }
.btn-text.btn-down:hover { text-decoration: underline; }
.text-disabled { font-size: 12px; color: #ccc; cursor: not-allowed; }

.empty-state { text-align: center; padding: 40px; color: #999; }

/* --- 弹窗样式 --- */
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: modalFadeIn 0.3s ease; display: flex; flex-direction: column; }
.join-modal { width: 400px; }
.wide-modal { width: 800px; max-height: 80vh; }

@keyframes modalFadeIn { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }

.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 16px; color: #333; }
.close-btn { font-size: 20px; cursor: pointer; color: #999; }
.close-btn:hover { color: #333; }

.modal-body { padding: 30px 25px; }
.modal-body.align-center { text-align: center; }

/* 加入课程弹窗内容 */
.icon-placeholder { font-size: 40px; margin-bottom: 10px; }
.instruction { color: #666; font-size: 14px; margin-bottom: 20px; }
.input-wrapper { margin-bottom: 10px; }
.code-input { 
  width: 100%; padding: 12px; font-size: 20px; text-align: center; letter-spacing: 4px; 
  border: 2px solid #dcdfe6; border-radius: 6px; box-sizing: border-box; 
  font-family: monospace; text-transform: uppercase; font-weight: bold; color: #333;
}
.code-input:focus { border-color: #1890ff; outline: none; }
.error-msg { color: #f5222d; font-size: 12px; margin-top: 8px; }

/* 资源弹窗内容 */
.resource-list { min-height: 200px; }
.empty-resource { text-align: center; color: #999; margin-top: 50px; }
.resource-table-wrapper { border: 1px solid #ebeef5; border-radius: 4px; }
.resource-table th { background: #f9f9f9; padding: 12px; }
.resource-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; }
.res-name-wrapper { display: flex; align-items: center; }
.file-icon { font-size: 18px; margin-right: 8px; }
.res-name { font-weight: 500; color: #333; cursor: default; }
.read-badge { font-size: 12px; padding: 2px 6px; border-radius: 4px; }
.read-badge.read { background: #f0f9eb; color: #67c23a; }
.read-badge.unread { background: #fdf6ec; color: #e6a23c; }
.text-gray { color: #999; font-size: 13px; }

.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
</style>