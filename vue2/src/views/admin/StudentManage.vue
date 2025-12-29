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
            placeholder="搜索学号或行政班级..."
            @keyup.enter="handleSearch"
          >
          <button class="btn btn-search" @click="handleSearch">🔍 搜索</button>
        </div>
        <button class="btn btn-primary" @click="openAddModal">+ 新增学生</button>
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
          <tr v-for="student in filteredStudents" :key="student.id">
            <td class="id-col">{{ student.id }}</td>
            <td class="name-col">{{ student.name }}</td>
            <td>
              <span class="badge-class">{{ student.class }}</span>
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
                :class="student.status === 'active' ? 'status-active' : 'status-disabled'"
              >
                {{ student.status === 'active' ? '正常' : '异常' }}
              </span>
            </td>
            <td class="action-col">
              <button class="btn-text btn-edit" @click="openEditModal(student)" title="修改档案">
                编辑
              </button>
              
              <button 
                class="btn-text" 
                :class="student.status === 'active' ? 'btn-warn' : 'btn-success'"
                @click="toggleStatus(student)"
              >
                {{ student.status === 'active' ? '冻结' : '解冻' }}
              </button>

              <button class="btn-text btn-info" @click="resetPassword(student)" title="重置密码">
                重置
              </button>

              <button class="btn-text btn-danger" @click="deleteStudent(student)" title="删除账号">
                删除
              </button>
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
          <h3>{{ isEditMode ? '修正学生档案' : '录入新学生' }}</h3>
          <span class="close-btn" @click="closeModal">×</span>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveStudent">
            <div class="form-row">
              <div class="form-group">
                <label>学号 <span class="required">*</span></label>
                <input type="text" v-model="form.id" :disabled="isEditMode" placeholder="唯一学号" required>
              </div>
              <div class="form-group">
                <label>姓名 <span class="required">*</span></label>
                <input type="text" v-model="form.name" placeholder="学生姓名" required>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>行政班级 <span class="required">*</span></label>
                <input type="text" v-model="form.class" placeholder="例: 软件2201" required>
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
export default {
  name: 'StudentManage',
  data() {
    return {
      searchQuery: '',
      showModal: false,
      isEditMode: false,
      // 模拟学生数据
      students: [
        { id: 'S2023001', name: '张三', gender: '男', class: '软件2201', major: '软件工程', enrollmentYear: '2022', phone: '13811112222', email: 'zhang3@stu.edu.cn', status: 'active' },
        { id: 'S2023002', name: '李四', gender: '女', class: '计科2202', major: '计算机科学', enrollmentYear: '2022', phone: '13933334444', email: 'li4@stu.edu.cn', status: 'active' },
        { id: 'S2023003', name: '王五', gender: '男', class: '软件2201', major: '软件工程', enrollmentYear: '2022', phone: '13655556666', email: 'wang5@stu.edu.cn', status: 'disabled' },
        { id: 'S2023004', name: '赵六', gender: '女', class: '物联网2301', major: '物联网工程', enrollmentYear: '2023', phone: '13577778888', email: 'zhao6@stu.edu.cn', status: 'active' },
        { id: 'S2023005', name: '钱七', gender: '男', class: '计科2202', major: '计算机科学', enrollmentYear: '2022', phone: '13499990000', email: 'qian7@stu.edu.cn', status: 'active' },
      ],
      // 表单对象 (对应功能 1.3.1 修改项)
      form: {
        id: '', name: '', gender: '男', class: '', major: '', enrollmentYear: '', phone: '', email: ''
      }
    }
  },
  computed: {
    // 1.3.1 查询功能：支持学号或班级过滤
    filteredStudents() {
      if (!this.searchQuery) return this.students;
      const query = this.searchQuery.toLowerCase();
      return this.students.filter(s => 
        s.id.toLowerCase().includes(query) || 
        s.class.includes(query)
      );
    }
  },
  methods: {
    handleSearch() {
      console.log('Searching student:', this.searchQuery);
    },
    
    // --- 弹窗逻辑 ---
    openAddModal() {
      this.isEditMode = false;
      this.form = { id: '', name: '', gender: '男', class: '', major: '', enrollmentYear: new Date().getFullYear(), phone: '', email: '' };
      this.showModal = true;
    },
    openEditModal(student) {
      this.isEditMode = true;
      // 深拷贝，防止修改时影响列表显示
      this.form = { ...student };
      this.showModal = true;
    },
    closeModal() {
      this.showModal = false;
    },
    saveStudent() {
      if (this.isEditMode) {
        // 更新逻辑 (1.3.1 修正档案)
        const index = this.students.findIndex(s => s.id === this.form.id);
        if (index !== -1) {
          this.students.splice(index, 1, { ...this.students[index], ...this.form });
          alert('学生档案更新成功！');
        }
      } else {
        // 新增逻辑
        if (this.students.find(s => s.id === this.form.id)) {
          return alert('错误：该学号已存在！');
        }
        this.students.push({ ...this.form, status: 'active' });
        alert(`新增成功！\n初始密码已设置为: 123456`);
      }
      this.closeModal();
    },

    // --- 账号安全管理功能 (1.3.2) ---
    toggleStatus(student) {
      const action = student.status === 'active' ? '冻结' : '解冻';
      if (confirm(`确定要${action}该学生账号吗？\n${student.name} (${student.id})`)) {
        student.status = student.status === 'active' ? 'disabled' : 'active';
      }
    },
    resetPassword(student) {
      // 1.3.2 重置学生密码
      if (confirm(`确定要重置学生 ${student.name} 的密码吗？\n密码将被重置为默认值: 123456`)) {
        alert('操作成功！密码已重置。');
      }
    },
    deleteStudent(student) {
      // 1.3.2 删除非法注册账号
      if (confirm(`【危险】确定要删除学生 ${student.name} 吗？\n此操作将清除该学生的所有选课和成绩记录！`)) {
        this.students = this.students.filter(s => s.id !== student.id);
        alert('账号已删除。');
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