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
        <button class="btn btn-primary" @click="openJoinModal">+ 加入新课程</button>
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
          <tr v-for="course in filteredCourses" :key="course.classId">
            <td><span class="term-tag">{{ course.semester }}</span></td>
            <td class="course-name-col">{{ course.courseName }}</td>
            <td>{{ course.className }}</td>
            <td>{{ course.teacherName }}</td>
            <td class="credit-col">{{ course.credit }}</td>
            <td>
              <span class="status-badge status-active">{{ course.status || '进行中' }}</span>
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
              {{ isLoading ? '加载中...' : '暂无课程数据，请点击右上方按钮加入课程' }}
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
              maxlength="10"
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
              暂无上传的教学资源 (功能开发中...)
            </div>
            
            <div v-else class="resource-table-wrapper">
              <table class="data-table resource-table">
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
// 引入 API
import { getMyCourses, joinClass } from '@/api/student'

export default {
  name: 'CourseList',
  data() {
    return {
      searchQuery: '',
      isLoading: false,
      myCourses: [], // 真实数据存储数组
      
      // 加入课程相关
      showJoinModal: false,
      inviteCode: '',
      errorMsg: '',

      // 资源弹窗相关
      showResourceModal: false,
      currentCourse: {},
      currentResources: [], // 暂时置空
    }
  },
  computed: {
    // 前端搜索过滤
    filteredCourses() {
      if (!this.searchQuery) return this.myCourses;
      const q = this.searchQuery.toLowerCase();
      return this.myCourses.filter(c => 
        (c.courseName && c.courseName.toLowerCase().includes(q)) || 
        (c.teacherName && c.teacherName.toLowerCase().includes(q))
      );
    }
  },
  created() {
    this.fetchData();
  },
  methods: {
    // 从后端获取数据
    fetchData() {
      this.isLoading = true;
      getMyCourses().then(res => {
        if (res.success) {
          this.myCourses = res.data;
        } else {
          // 如果没有登录或者报错，可以提示用户
          console.error(res.message);
          if(res.message.includes("登录")) {
             // 可以在这里跳转登录页
          }
        }
      }).catch(err => {
        console.error("获取课程列表失败", err);
      }).finally(() => {
        this.isLoading = false;
      });
    },

    handleSearch() {
      // 当前使用前端 computed 过滤，这里仅作为触发器（可选）
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
    
    // 连接后端加入班级
    handleJoin() {
      if (!this.inviteCode) return;
      
      const params = { invitationCode: this.inviteCode };
      
      joinClass(params).then(res => {
        if (res.success) {
          alert('🎉 ' + res.message);
          this.closeJoinModal();
          this.fetchData(); // 重新加载列表
        } else {
          this.errorMsg = res.message; // 显示后端返回的具体错误（如验证码无效、已加入）
        }
      }).catch(err => {
        console.error("加入班级失败", err); // <--- 加上这一行，使用了 err 变量
        this.errorMsg = '网络错误，请稍后重试';
      });
    },

    quitCourse(course) {
      if (confirm(`确定要退出 "${course.courseName}" 吗？\n退出后将无法提交作业和查看资料。`)) {
        // TODO: 调用后端退课接口
        alert("退课功能暂未开放 (需要后端添加 deleteEnrollment 接口)");
      }
    },

    openResourceModal(course) {
      this.currentCourse = course;
      // TODO: 这里将来要调用 getCourseResources(course.classId)
      this.currentResources = []; 
      this.showResourceModal = true;
    },
    closeResourceModal() {
      this.showResourceModal = false;
      this.currentResources = [];
    },
    
    // 工具方法
    getFileIcon(type) {
      const map = {
        'PPT': '📊', 'PDF': '📄', 'Video': '🎬', 'Word': '📝', 'Code': '💻', 'Audio': '🎵'
      };
      return map[type] || '📁';
    },
    previewResource(res) {
      alert(`正在打开预览：${res.name}`);
    },
    downloadResource(res) {
      if (res.allowDownload) {
        alert(`开始下载文件：${res.name}`);
        // window.open(res.filePath)
      } else {
        alert('该资源不允许下载');
      }
    }
  }
}
</script>

<style scoped>
/* 保持原有样式，仅增加一个加载状态样式 */
.manage-container { padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; }

.action-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.title-section h2 { margin: 0; font-size: 20px; color: #333; }
.subtitle { margin: 5px 0 0; font-size: 13px; color: #999; }
.operation-section { display: flex; gap: 15px; }

.search-box { display: flex; }
.search-box input { padding: 8px 12px; border: 1px solid #dcdfe6; border-right: none; border-radius: 4px 0 0 4px; outline: none; font-size: 14px; width: 220px; transition: border 0.3s; }
.search-box input:focus { border-color: #1890ff; }
.btn-search { border-radius: 0 4px 4px 0; background: #f5f7fa; color: #606266; border: 1px solid #dcdfe6; border-left: none; cursor: pointer; padding: 8px 12px; }
.btn-search:hover { background: #e6f7ff; color: #1890ff; }

.btn { padding: 8px 16px; border: none; cursor: pointer; font-size: 14px; border-radius: 4px; transition: all 0.3s; }
.btn-primary { background: #1890ff; color: white; box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3); }
.btn-primary:hover:not(:disabled) { background: #40a9ff; }
.btn-primary:disabled { background: #a0cfff; cursor: not-allowed; }
.btn-secondary { background: #fff; border: 1px solid #dcdfe6; color: #606266; }
.btn-secondary:hover { color: #1890ff; border-color: #c6e2ff; background: #ecf5ff; }

.table-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #fafafa; padding: 16px; color: #333; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 16px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.data-table tr:hover { background-color: #f5f7fa; }

.term-tag { background: #f0f5ff; color: #2f54eb; border: 1px solid #adc6ff; padding: 2px 6px; border-radius: 4px; font-size: 12px; font-family: monospace; }
.course-name-col { font-weight: 600; color: #333; font-size: 15px; }
.credit-col { font-weight: bold; color: #1890ff; }
.status-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.status-active { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }

.action-col { display: flex; gap: 12px; align-items: center; }
.btn-text { background: none; border: none; cursor: pointer; font-size: 13px; padding: 4px 8px; border-radius: 4px; transition: all 0.2s; }
.btn-text.btn-resource { color: #13c2c2; font-weight: 500; display: flex; align-items: center; gap: 4px; }
.btn-text.btn-resource:hover { background-color: #e6fffb; color: #08979c; }
.btn-text.btn-danger { color: #ff4d4f; } 
.btn-text.btn-danger:hover { background-color: #fff1f0; }

.empty-state { text-align: center; padding: 40px; color: #999; }

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

.resource-list { min-height: 200px; }
.empty-resource { text-align: center; color: #999; margin-top: 50px; }

.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
</style>