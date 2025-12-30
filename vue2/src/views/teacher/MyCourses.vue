<template>
  <div class="manage-container">
    <div class="action-header">
      <div class="title-section">
        <h2>我的课程班级</h2>
        <p class="subtitle">管理负责的教学班级、生成邀请码及维护学生名单</p>
      </div>
      <div class="operation-section">
        <div class="search-box">
          <input type="text" v-model="searchQuery" placeholder="搜索课程或班级名称..." @keyup.enter="handleSearch">
          <button class="btn btn-search" @click="handleSearch">🔍 搜索</button>
        </div>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th width="120">开课学期</th>
            <th>课程名称</th>
            <th>教学班名称</th>
            <th width="150">学生人数</th>
            <th width="150">班级邀请码</th>
            <th width="180">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="course in filteredCourses" :key="course.id">
            <td><span class="term-tag">{{ course.semester }}</span></td>
            <td class="course-name">{{ course.courseName }}</td>
            <td class="class-name">{{ course.className }}</td>
            <td>
              <div class="capacity-box">
                <span>{{ course.students.length }} 人</span>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: (course.students.length / 60 * 100) + '%' }"></div>
                </div>
              </div>
            </td>
            <td>
              <div v-if="course.inviteCode" class="code-display" @click="openInviteModal(course)">
                <span class="code-text">{{ course.inviteCode }}</span>
                <span class="code-icon">🎫</span>
              </div>
              <button v-else class="btn-text btn-primary" @click="generateInviteCode(course)">
                生成邀请码
              </button>
            </td>
            <td>
              <div class="action-col">
                <button class="btn-text btn-edit" @click="openMemberModal(course)" title="管理班级成员">
                  👥 成员管理
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="filteredCourses.length === 0">
            <td colspan="6" class="empty-state">暂无正在进行的课程</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="modal-mask" v-if="showInviteModal">
      <div class="modal-box invite-modal">
        <div class="modal-header">
          <h3>班级邀请码</h3>
          <span class="close-btn" @click="closeInviteModal">×</span>
        </div>
        <div class="modal-body align-center">
          <p class="invite-course-title">{{ currentCourse.courseName }} - {{ currentCourse.className }}</p>
          <div class="big-code">{{ currentCourse.inviteCode }}</div>
          <p class="invite-expiry">有效期至：{{ currentCourse.inviteExpiry }}</p>
          <p class="hint">请将此码告知学生，学生可在“加入课程”中输入此码入班</p>

          <button class="btn btn-primary btn-refresh" @click="generateInviteCode(currentCourse)">
            🔄 重新生成
          </button>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showMemberModal">
      <div class="modal-box wide-modal">
        <div class="modal-header">
          <h3>成员管理 - {{ currentCourse.className }}</h3>
          <span class="close-btn" @click="closeMemberModal">×</span>
        </div>
        <div class="modal-body member-body">
          <div class="member-header">
            <div class="stat">共 <strong>{{ currentCourse.students.length }}</strong> 名学生</div>
            <input type="text" v-model="studentSearch" placeholder="筛选学生姓名或学号..." class="mini-search">
          </div>

          <div class="member-list-container">
            <table class="member-table">
              <thead>
                <tr>
                  <th>序号</th>
                  <th>学号</th>
                  <th>姓名</th>
                  <th>行政班级</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(stu, index) in filteredStudents" :key="stu.id">
                  <td>{{ index + 1 }}</td>
                  <td class="mono">{{ stu.id }}</td>
                  <td>{{ stu.name }}</td>
                  <td>{{ stu.adminClass }}</td>
                  <td>
                    <button class="btn-text btn-danger" @click="removeStudent(stu)">移除</button>
                  </td>
                </tr>
                <tr v-if="filteredStudents.length === 0">
                  <td colspan="5" class="text-center">无匹配学生</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { getTeacherCourses, generateInviteCode, getClassStudentDetails, removeStudentFromClass } from '@/api/teachingClass'

export default {
  name: 'MyCourses',
  data() {
    return {
      searchQuery: '',
      studentSearch: '',
      showInviteModal: false,
      showMemberModal: false,
      currentCourse: null,
      myCourses: [], // 清空模拟数据，改为空数组
      loading: false,
      teacherId: '' // 存储教师ID
    }
  },
  computed: {
    filteredCourses() {
      if (!this.searchQuery) return this.myCourses;
      const q = this.searchQuery.toLowerCase();
      return this.myCourses.filter(c =>
        (c.courseName && c.courseName.toLowerCase().includes(q)) ||
        (c.className && c.className.toLowerCase().includes(q))
      );
    },
    filteredStudents() {
      if (!this.currentCourse || !this.currentCourse.students) return [];
      if (!this.studentSearch) return this.currentCourse.students;
      const q = this.studentSearch.toLowerCase();
      return this.currentCourse.students.filter(s =>
        (s.name && s.name.includes(q)) ||
        (s.id && s.id.toLowerCase().includes(q))
      );
    }
  },
  created() {
    // 从Vuex或localStorage获取教师ID
    const userInfo = this.$store.state.userInfo || JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (userInfo.role === 'teacher' && userInfo.id) {
      this.teacherId = userInfo.id;
      this.loadTeacherCourses();
    } else {
      console.error('未获取到教师信息');
      this.$message.error('无法获取教师信息，请重新登录');
    }
  },
  methods: {
    // 加载教师负责的课程
    async loadTeacherCourses() {
      this.loading = true;
      try {
        const response = await getTeacherCourses(this.teacherId);
        console.log('API返回数据:', response); // 添加调试日志

        if (response.success && response.data) {
          // 调试：查看第一个课程的数据结构
          if (response.data.length > 0) {
            console.log('第一个课程原始数据:', response.data[0]);
            console.log('可用字段:', Object.keys(response.data[0]));
          }

          this.myCourses = response.data.map(course => ({
            id: course.id || course.classId, // 优先使用id，如果没有则使用classId
            semester: course.semester,
            courseName: course.courseName,
            className: course.className,
            inviteCode: course.invitationCode || '',
            inviteExpiry: course.expiryDate ?
              this.formatDate(course.expiryDate) : '',
            studentCount: course.studentCount || 0,
            // 确保 students 数组不为 null/undefined
            students: course.students || []
          }));

          // 调试：打印处理后的第一个课程
          if (this.myCourses.length > 0) {
            console.log('处理后第一个课程:', this.myCourses[0]);
          }
        } else {
          this.$message.error(response.message || '加载课程失败');
        }
      } catch (error) {
        console.error('加载课程失败:', error);
        this.$message.error('网络错误，请检查后端服务是否启动');
      } finally {
        this.loading = false;
      }
    },

    handleSearch() {
      // 搜索功能已在computed中实现
      console.log('搜索关键词:', this.searchQuery);
    },

    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return '';
      try {
        const date = new Date(dateStr);
        return date.toLocaleDateString('zh-CN');
      } catch (e) {
        return dateStr;
      }
    },

    // --- 邀请码逻辑 ---
    async generateInviteCode(course) {
      try {
        const response = await generateInviteCode(course.id);
        if (response.success && response.data) {
          // 重新加载整个课程列表数据
          await this.loadTeacherCourses();
          this.$message.success('邀请码生成成功');
        } else {
          this.$message.error(response.message || '生成邀请码失败');
        }
      } catch (error) {
        console.error('生成邀请码失败:', error);
        this.$message.error('网络错误，请稍后重试');
      }
    },

    openInviteModal(course) {
      this.currentCourse = { ...course };
      this.showInviteModal = true;
    },

    closeInviteModal() {
      this.showInviteModal = false;
      this.currentCourse = null;
    },

    // --- 成员管理逻辑 ---
    async openMemberModal(course) {
      this.loading = true;
      try {
        // 获取班级学生详细信息
        const response = await getClassStudentDetails(course.id);
        if (response.success && response.data) {
          this.currentCourse = {
            ...course,
            students: response.data
          };
          this.studentSearch = '';
          this.showMemberModal = true;
        } else {
          this.$message.error(response.message || '获取学生列表失败');
        }
      } catch (error) {
        console.error('获取学生详情失败:', error);
        this.$message.error('网络错误，请稍后重试');
      } finally {
        this.loading = false;
      }
    },

    closeMemberModal() {
      this.showMemberModal = false;
      this.currentCourse = null;
      this.studentSearch = '';
    },

    // 移除学生
    async removeStudent(student) {
      try {
        this.$confirm(`确定要将学生 ${student.name} (${student.id}) 移出班级吗？`, '确认移除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(async () => {
          const response = await removeStudentFromClass(this.currentCourse.id, student.id);
          if (response.success) {
            // 重新加载整个课程列表数据，确保数据一致性
            await this.loadTeacherCourses();
            this.$message.success('移除学生成功');
          } else {
            this.$message.error(response.message || '移除学生失败');
          }
        }).catch(() => {
          // 用户取消操作
        });
      } catch (error) {
        console.error('移除学生失败:', error);
        this.$message.error('网络错误，请稍后重试');
      }
    }
  }
}
</script>

<style scoped>
/* 样式体系复用 Admin 风格 */
.manage-container {
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

/* 顶部 */
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

.search-box {
  display: flex;
}

.search-box input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-right: none;
  border-radius: 4px 0 0 4px;
  outline: none;
  font-size: 14px;
  width: 220px;
}

.search-box input:focus {
  border-color: #1890ff;
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

.btn-search:hover {
  background: #e6f7ff;
  color: #1890ff;
}

/* 表格卡片 */
.table-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.data-table th {
  background: #fafafa;
  padding: 16px;
  color: #333;
  font-weight: 600;
  border-bottom: 1px solid #ebeef5;
}

.data-table td {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  color: #606266;
  font-size: 14px;
  vertical-align: middle;
}

.data-table tr:hover {
  background-color: #f5f7fa;
}

/* 列样式 */
.term-tag {
  background: #f0f5ff;
  color: #2f54eb;
  border: 1px solid #adc6ff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  font-family: monospace;
}

.course-name {
  font-weight: bold;
  color: #333;
  font-size: 15px;
}

.class-name {
  color: #606266;
}

/* 进度条 */
.capacity-box {
  width: 100px;
  font-size: 12px;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: #f5f5f5;
  border-radius: 3px;
  margin-top: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #52c41a;
}

/* 邀请码展示 */
.code-display {
  display: inline-flex;
  align-items: center;
  background: #fff7e6;
  border: 1px solid #ffd591;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.code-display:hover {
  transform: scale(1.05);
}

.code-text {
  font-family: monospace;
  font-weight: bold;
  color: #fa8c16;
  margin-right: 5px;
  letter-spacing: 1px;
}

.code-icon {
  font-size: 12px;
}

/* 操作按钮 */
.action-col {
  display: flex;
  gap: 10px;
}

.btn-text {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 13px;
  padding: 0;
}

.btn-edit {
  color: #1890ff;
}

.btn-edit:hover {
  text-decoration: underline;
}

.btn-primary {
  color: #1890ff;
}

.btn-primary:hover {
  text-decoration: underline;
}

.btn-danger {
  color: #ff4d4f;
}

.btn-danger:hover {
  text-decoration: underline;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

/* --- 弹窗通用 --- */
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
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: modalFadeIn 0.3s ease;
  display: flex;
  flex-direction: column;
}

.invite-modal {
  width: 400px;
}

.wide-modal {
  width: 700px;
  max-height: 80vh;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
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
  color: #333;
}

.close-btn {
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

.close-btn:hover {
  color: #333;
}

/* 邀请码弹窗内容 */
.modal-body {
  padding: 25px;
}

.align-center {
  text-align: center;
}

.invite-course-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
}

.big-code {
  font-size: 40px;
  font-weight: bold;
  color: #1890ff;
  letter-spacing: 4px;
  font-family: monospace;
  margin-bottom: 10px;
  background: #f0f5ff;
  padding: 10px;
  border-radius: 8px;
  border: 2px dashed #adc6ff;
  display: inline-block;
}

.invite-expiry {
  color: #ff4d4f;
  font-size: 13px;
  margin-bottom: 20px;
}

.hint {
  font-size: 12px;
  color: #999;
  margin-bottom: 20px;
}

.btn-refresh {
  padding: 8px 20px;
  font-size: 13px;
  border-radius: 4px;
  border: none;
  background: #1890ff;
  color: white;
  cursor: pointer;
}

.btn-refresh:hover {
  background: #40a9ff;
}

/* 成员管理弹窗内容 */
.member-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.mini-search {
  padding: 6px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  width: 200px;
  font-size: 13px;
}

.member-list-container {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.member-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.member-table th {
  background: #f9fafc;
  padding: 10px;
  text-align: left;
  color: #606266;
  position: sticky;
  top: 0;
}

.member-table td {
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
  color: #333;
}

.mono {
  font-family: monospace;
}

.text-center {
  text-align: center;
  color: #999;
  padding: 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .wide-modal {
    width: 95%;
  }

  .table-card {
    overflow-x: auto;
  }
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

/* 刷新按钮 */
.btn-refresh {
  margin-left: 10px;
  padding: 8px 12px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #606266;
  cursor: pointer;
}

.btn-refresh:hover {
  background: #e6f7ff;
  color: #1890ff;
}

.btn-refresh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>