<template>
  <div class="manage-container">
    <div class="action-header">
      <div class="title-section">
        <h2>学生用户管理</h2>
        <p class="subtitle">维护学生档案信息及账号安全</p>
      </div>
      <div class="operation-section">
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="搜索学号或姓名..."
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
            <th>学号</th>
            <th>姓名</th>
            <th>行政班级</th>
            <th>专业</th>
            <th>入学年份</th>
            <th>联系方式</th>
            <th>状态</th>
            <th width="280">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="student in filteredStudents" :key="student.studentId">
            <td class="id-col">{{ student.studentId }}</td>
            <td class="name-col">{{ student.name }}</td>
            <td>
              <span class="badge-class">{{ student.className }}</span>
            </td>
            <td>{{ student.major }}</td>
            <td>{{ student.enrollmentYear }}级</td>
            <td class="contact-col">
              <div class="contact-item">📱 {{ student.phone }}</div>
              <div class="contact-item">📧 {{ student.email }}</div>
            </td>
            <td>
              <span 
                class="status-badge" 
                :class="student.accountStatus === '正常' ? 'status-active' : 'status-disabled'"
              >
                {{ student.accountStatus === '正常' ? '正常' : '禁用' }}
              </span>
            </td>
            <td>
              <div class="action-col">
                <button class="btn-text btn-edit" @click="openEditModal(student)" title="修改档案">
                  编辑
                </button>
                
                <button 
                  class="btn-text" 
                  :class="student.accountStatus === '正常' ? 'btn-warn' : 'btn-success'"
                  @click="toggleStatus(student)"
                >
                  {{ student.accountStatus === '正常' ? '禁用' : '启用' }}
                </button>

                <button class="btn-text btn-info" @click="resetPassword(student)" title="重置密码">
                  重置
                </button>

                <button class="btn-text btn-danger" @click="deleteStudent(student)" title="删除账号">
                  删除
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="filteredStudents.length === 0">
            <td colspan="8" class="empty-state">暂无符合条件的学生数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="modal-mask" v-if="showModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>修正学生档案</h3>
          <span class="close-btn" @click="closeModal">×</span>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveStudent">
            <div class="form-row">
              <div class="form-group">
                <label>学号</label>
                <input type="text" v-model="form.studentId" disabled>
              </div>
              <div class="form-group">
                <label>姓名 <span class="required">*</span></label>
                <input type="text" v-model="form.name" placeholder="学生姓名" required>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>行政班级 <span class="required">*</span></label>
                <input type="text" v-model="form.className" placeholder="例: 软件2201" required>
              </div>
              <div class="form-group">
                <label>专业 <span class="required">*</span></label>
                <input type="text" v-model="form.major" placeholder="例: 软件工程" required>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>入学年份 <span class="required">*</span></label>
                <input type="number" v-model="form.enrollmentYear" placeholder="例: 2023" required>
              </div>
              <div class="form-group">
                <label>性别</label>
                <select v-model="form.gender">
                  <option value="男">男</option>
                  <option value="女">女</option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>手机号码</label>
                <input type="text" v-model="form.phone" placeholder="11位手机号">
              </div>
              <div class="form-group">
                <label>电子邮箱</label>
                <input type="email" v-model="form.email" placeholder="常用邮箱">
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
              <button type="submit" class="btn btn-primary">保存</button>
            </div>
          </form>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import * as studentApi from '@/api/student'

export default {
  name: 'StudentManage',
  data() {
    return {
      searchQuery: '',
      showModal: false,
      isEditMode: false,
      students: [],
      loading: false,
      form: {
        studentId: '',
        name: '',
        gender: '男',
        className: '',
        major: '',
        enrollmentYear: new Date().getFullYear(),
        phone: '',
        email: ''
      }
    }
  },
  computed: {
    filteredStudents() {
      if (!this.searchQuery) return this.students;
      const query = this.searchQuery.toLowerCase();
      return this.students.filter(s => 
        (s.studentId && s.studentId.toLowerCase().includes(query)) || 
        (s.name && s.name.toLowerCase().includes(query)) ||
        (s.className && s.className.includes(query))
      );
    }
  },
  mounted() {
    this.loadStudents();
  },
  methods: {
    // 加载学生列表
    async loadStudents() {
      this.loading = true;
      try {
        const response = await studentApi.getStudentList();
        if (response.success) {
          this.students = response.data;
          console.log('加载的学生数据:', this.students);
        } else {
          this.$message.error(response.message || '加载失败');
        }
      } catch (error) {
        console.error('加载学生列表失败:', error);
        this.$message.error('网络错误，请检查后端服务');
      } finally {
        this.loading = false;
      }
    },

    // 搜索学生
    async handleSearch() {
      this.loading = true;
      try {
        const response = await studentApi.getStudentList(this.searchQuery);
        if (response.success) {
          this.students = response.data;
        } else {
          this.$message.error(response.message || '搜索失败');
        }
      } catch (error) {
        console.error('搜索失败:', error);
        this.$message.error('搜索失败');
      } finally {
        this.loading = false;
      }
    },
    
    // 打开编辑模态框
    openEditModal(student) {
      this.form = { 
        studentId: student.studentId,
        name: student.name,
        gender: student.gender,
        className: student.className,
        major: student.major,
        enrollmentYear: student.enrollmentYear,
        phone: student.phone,
        email: student.email,
        department: student.department || '' // 添加院系字段
      };
      this.showModal = true;
    },
    
    closeModal() {
      this.showModal = false;
    },
    
    // 保存学生信息
    async saveStudent() {
      if (!this.form.name.trim()) {
        this.$message.warning('请输入姓名');
        return;
      }
      if (!this.form.className.trim()) {
        this.$message.warning('请输入行政班级');
        return;
      }
      if (!this.form.major.trim()) {
        this.$message.warning('请输入专业');
        return;
      }
      if (!this.form.enrollmentYear) {
        this.$message.warning('请输入入学年份');
        return;
      }

      try {
        const response = await studentApi.updateStudent(this.form);
        if (response.success) {
          this.$message.success(response.message);
          this.closeModal();
          this.loadStudents(); // 重新加载数据
        } else {
          this.$message.error(response.message);
        }
      } catch (error) {
        console.error('更新失败:', error);
        this.$message.error('操作失败，请检查网络连接');
      }
    },

    // 切换账号状态
    async toggleStatus(student) {
      const action = student.accountStatus === '正常' ? '禁用' : '启用';
      try {
        const confirm = await this.$confirm(
          `确定要${action}该学生账号吗？\n${student.name} (${student.studentId})`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).catch(() => false);
        
        if (confirm) {
          const response = await studentApi.toggleStudentStatus(student.studentId);
          if (response.success) {
            this.$message.success(response.message);
            this.loadStudents(); // 重新加载数据
          } else {
            this.$message.error(response.message);
          }
        }
      } catch (error) {
        console.error('状态切换失败:', error);
        this.$message.error('操作失败');
      }
    },

    // 重置密码
    async resetPassword(student) {
      try {
        const confirm = await this.$confirm(
          `确定要重置学生 ${student.name} 的密码吗？\n密码将被重置为默认值: 123456`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).catch(() => false);
        
        if (confirm) {
          const response = await studentApi.resetStudentPassword(student.studentId);
          if (response.success) {
            this.$message.success('密码重置成功，新密码为：123456');
          } else {
            this.$message.error(response.message);
          }
        }
      } catch (error) {
        console.error('密码重置失败:', error);
        this.$message.error('操作失败');
      }
    },

    // 删除学生
    async deleteStudent(student) {
      try {
        const confirm = await this.$confirm(
          `【危险】确定要删除学生 ${student.name} 吗？\n此操作将清除该学生的所有选课和成绩记录！`,
          '警告',
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'error'
          }
        ).catch(() => false);
        
        if (confirm) {
          const response = await studentApi.deleteStudent(student.studentId);
          if (response.success) {
            this.$message.success(response.message);
            this.loadStudents(); // 重新加载数据
          } else {
            this.$message.error(response.message);
          }
        }
      } catch (error) {
        console.error('删除失败:', error);
        this.$message.error('删除失败');
      }
    }
  }
}
</script>

<style scoped>
/* 样式复用 TeacherManage.vue 的设计体系 */
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
.btn-search { border-radius: 0 4px 4px 0; background: #f5f7fa; color: #606266; border: 1px solid #dcdfe6; border-left: none; }
.btn-search:hover { background: #e6f7ff; color: #1890ff; }

/* 按钮通用 */
.btn { padding: 8px 16px; border: none; cursor: pointer; font-size: 14px; border-radius: 4px; transition: all 0.3s; }
.btn-primary { background: #1890ff; color: white; box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3); }
.btn-primary:hover { background: #40a9ff; }
.btn-secondary { background: #fff; border: 1px solid #dcdfe6; color: #606266; }
.btn-secondary:hover { color: #1890ff; border-color: #c6e2ff; background: #ecf5ff; }

/* 表格样式 */
.table-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #fafafa; padding: 16px; color: #333; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 16px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; }
.data-table tr:hover { background-color: #f5f7fa; }

/* 列样式 */
.id-col { font-family: monospace; font-weight: bold; color: #333; }
.name-col { font-weight: 500; }
.contact-item { font-size: 12px; color: #909399; margin-bottom: 2px; }

/* 班级徽标 */
.badge-class { background: #e6f7ff; color: #1890ff; padding: 2px 8px; border-radius: 10px; font-size: 12px; border: 1px solid #91d5ff; }

/* 状态徽标 */
.status-badge { padding: 4px 8px; border-radius: 12px; font-size: 12px; font-weight: bold; }
.status-active { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.status-disabled { background: #fff1f0; color: #f5222d; border: 1px solid #ffa39e; }

/* 操作按钮 */
.action-col { display: flex; gap: 8px; }
.btn-text { background: none; border: none; cursor: pointer; font-size: 13px; padding: 0 4px; }
.btn-edit { color: #1890ff; } .btn-edit:hover { text-decoration: underline; }
.btn-warn { color: #fa8c16; }
.btn-success { color: #52c41a; }
.btn-info { color: #909399; }
.btn-danger { color: #f5222d; }

.empty-state { text-align: center; padding: 40px; color: #999; }

/* 弹窗样式 */
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; width: 550px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: modalFadeIn 0.3s ease; }
@keyframes modalFadeIn { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 16px; color: #333; }
.close-btn { font-size: 20px; cursor: pointer; color: #999; }
.close-btn:hover { color: #333; }
.modal-body { padding: 20px; }
.form-row { display: flex; gap: 15px; }
.form-group { margin-bottom: 15px; flex: 1; }
.form-group label { display: block; margin-bottom: 8px; font-size: 13px; font-weight: 500; color: #606266; }
.required { color: #f56c6c; margin-left: 2px; }
.form-group input, .form-group select { width: 100%; padding: 8px 10px; border: 1px solid #dcdfe6; border-radius: 4px; box-sizing: border-box; font-size: 14px; }
.form-group input:focus, .form-group select:focus { border-color: #1890ff; outline: none; }
.form-group input:disabled { background: #f5f7fa; cursor: not-allowed; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
</style>