<template>
  <div class="manage-container">
    <div class="action-header">
      <div class="title-section">
        <h2>教师用户管理</h2>
        <p class="subtitle">管理教师基础档案及账号权限</p>
      </div>
      <div class="operation-section">
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="搜索姓名或教工号..."
            @keyup.enter="handleSearch"
          >
          <button class="btn btn-search" @click="handleSearch">🔍 搜索</button>
        </div>
        <button class="btn btn-primary" @click="openAddModal">+ 新增教师</button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>教工号</th>
            <th>姓名</th>
            <th>性别</th>
            <th>所属院系</th>
            <th>职称</th>
            <th>联系方式</th>
            <th>账号状态</th>
            <th width="280">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="teacher in filteredTeachers" :key="teacher.id">
            <td class="id-col">{{ teacher.id }}</td>
            <td class="name-col">{{ teacher.name }}</td>
            <td>{{ teacher.gender }}</td>
            <td>{{ teacher.department }}</td>
            <td>
              <span class="badge-title" :class="getTitleClass(teacher.title)">{{ teacher.title }}</span>
            </td>
            <td class="contact-col">
              <div class="contact-item">📱 {{ teacher.phone }}</div>
              <div class="contact-item">📧 {{ teacher.email }}</div>
            </td>
            <td>
              <span 
                class="status-badge" 
                :class="teacher.status === 'active' ? 'status-active' : 'status-disabled'"
              >
                {{ teacher.status === 'active' ? '正常' : '已禁用' }}
              </span>
            </td>
            <td class="action-col">
              <button class="btn-text btn-edit" @click="openEditModal(teacher)" title="编辑信息">
                编辑
              </button>
              
              <button 
                class="btn-text" 
                :class="teacher.status === 'active' ? 'btn-warn' : 'btn-success'"
                @click="toggleStatus(teacher)"
              >
                {{ teacher.status === 'active' ? '禁用' : '启用' }}
              </button>

              <button class="btn-text btn-info" @click="resetPassword(teacher)" title="重置为默认密码">
                重置
              </button>

              <button class="btn-text btn-danger" @click="deleteTeacher(teacher)" title="删除账号">
                删除
              </button>
            </td>
          </tr>
          <tr v-if="filteredTeachers.length === 0">
            <td colspan="8" class="empty-state">暂无符合条件的教师数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="modal-mask" v-if="showModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>{{ isEditMode ? '编辑教师信息' : '新增教师账户' }}</h3>
          <span class="close-btn" @click="closeModal">×</span>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveTeacher">
            <div class="form-row">
              <div class="form-group">
                <label>教工号 <span class="required">*</span></label>
                <input type="text" v-model="form.id" :disabled="isEditMode" placeholder="唯一工号" required>
              </div>
              <div class="form-group">
                <label>姓名 <span class="required">*</span></label>
                <input type="text" v-model="form.name" placeholder="教师姓名" required>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>性别</label>
                <select v-model="form.gender">
                  <option value="男">男</option>
                  <option value="女">女</option>
                </select>
              </div>
              <div class="form-group">
                <label>职称</label>
                <select v-model="form.title">
                  <option value="助教">助教</option>
                  <option value="讲师">讲师</option>
                  <option value="副教授">副教授</option>
                  <option value="教授">教授</option>
                </select>
              </div>
            </div>
            <div class="form-group">
              <label>所属院系 <span class="required">*</span></label>
              <input type="text" v-model="form.department" placeholder="例: 计算机学院" required>
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
  name: 'TeacherManage',
  data() {
    return {
      searchQuery: '',
      showModal: false,
      isEditMode: false,
      // 模拟数据库数据 (原3条 + 新增10条)
      teachers: [
        { id: 'T2023001', name: '王建国', gender: '男', department: '计算机学院', title: '教授', phone: '13800138000', email: 'wang@edu.cn', status: 'active' },
        { id: 'T2023002', name: '李晓梅', gender: '女', department: '软件学院', title: '讲师', phone: '13912345678', email: 'lixm@edu.cn', status: 'active' },
        { id: 'T2023003', name: '张伟', gender: '男', department: '人工智能学院', title: '副教授', phone: '13666666666', email: 'zhangw@edu.cn', status: 'disabled' },
        // --- 新增数据 ---
        { id: 'T2023004', name: '刘志远', gender: '男', department: '电子信息学院', title: '副教授', phone: '13812341234', email: 'liuzy@edu.cn', status: 'active' },
        { id: 'T2023005', name: '陈静', gender: '女', department: '外国语学院', title: '讲师', phone: '13987654321', email: 'chenj@edu.cn', status: 'active' },
        { id: 'T2023006', name: '赵强', gender: '男', department: '土木工程学院', title: '教授', phone: '13700001111', email: 'zhaoq@edu.cn', status: 'active' },
        { id: 'T2023007', name: '孙丽', gender: '女', department: '经济管理学院', title: '副教授', phone: '13655556666', email: 'sunl@edu.cn', status: 'disabled' },
        { id: 'T2023008', name: '周杰', gender: '男', department: '艺术设计学院', title: '讲师', phone: '13588889999', email: 'zhouj@edu.cn', status: 'active' },
        { id: 'T2023009', name: '吴刚', gender: '男', department: '数学科学学院', title: '教授', phone: '13422223333', email: 'wug@edu.cn', status: 'active' },
        { id: 'T2023010', name: '郑薇', gender: '女', department: '法学院', title: '讲师', phone: '13344445555', email: 'zhengv@edu.cn', status: 'active' },
        { id: 'T2023011', name: '冯敏', gender: '女', department: '马克思主义学院', title: '助教', phone: '13266667777', email: 'fengm@edu.cn', status: 'active' },
        { id: 'T2023012', name: '褚卫', gender: '男', department: '体育学院', title: '讲师', phone: '13188880000', email: 'chuw@edu.cn', status: 'disabled' },
        { id: 'T2023013', name: '蒋欣', gender: '女', department: '生命科学学院', title: '副教授', phone: '13099998888', email: 'jiangx@edu.cn', status: 'active' }
      ],
      form: { id: '', name: '', gender: '男', department: '', title: '讲师', phone: '', email: '' }
    }
  },
  computed: {
    filteredTeachers() {
      if (!this.searchQuery) return this.teachers;
      const query = this.searchQuery.toLowerCase();
      return this.teachers.filter(t => 
        t.name.includes(query) || t.id.toLowerCase().includes(query)
      );
    }
  },
  methods: {
    handleSearch() { console.log('Searching for:', this.searchQuery); },
    
    // 简单的职称颜色区分
    getTitleClass(title) {
      if (title === '教授') return 'badge-prof';
      if (title === '副教授') return 'badge-assoc';
      return 'badge-normal';
    },

    openAddModal() {
      this.isEditMode = false;
      this.form = { id: '', name: '', gender: '男', department: '', title: '讲师', phone: '', email: '' };
      this.showModal = true;
    },
    openEditModal(teacher) {
      this.isEditMode = true;
      this.form = { ...teacher };
      this.showModal = true;
    },
    closeModal() { this.showModal = false; },
    
    saveTeacher() {
      if (this.isEditMode) {
        const index = this.teachers.findIndex(t => t.id === this.form.id);
        if (index !== -1) {
          this.teachers.splice(index, 1, { ...this.teachers[index], ...this.form });
          alert('修改成功！');
        }
      } else {
        if (this.teachers.find(t => t.id === this.form.id)) return alert('错误：该教工号已存在！');
        this.teachers.push({ ...this.form, status: 'active' });
        alert(`新增成功！\n初始密码已设置为系统默认: 123456`);
      }
      this.closeModal();
    },

    toggleStatus(teacher) {
      const action = teacher.status === 'active' ? '禁用' : '启用';
      if (confirm(`确定要${action}该教师账号吗？\n${teacher.name} (${teacher.id})`)) {
        teacher.status = teacher.status === 'active' ? 'disabled' : 'active';
      }
    },
    resetPassword(teacher) {
      if (confirm(`确定要重置 ${teacher.name} 的密码吗？\n密码将被强制恢复为: 123456`)) {
        alert('操作成功！密码已重置。');
      }
    },
    deleteTeacher(teacher) {
      if (confirm(`【危险操作】确定要删除教师 ${teacher.name} 吗？\n此操作不可恢复！`)) {
        this.teachers = this.teachers.filter(t => t.id !== teacher.id);
        alert('账号已删除。');
      }
    }
  }
}
</script>

<style scoped>
/* 全局容器 */
.manage-container { padding: 0; font-family: 'Helvetica Neue', Helvetica, "PingFang SC", "Microsoft YaHei", Arial, sans-serif; }

/* 顶部操作栏 */
.action-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.title-section h2 { margin: 0; font-size: 20px; color: #333; }
.subtitle { margin: 5px 0 0; font-size: 13px; color: #999; }
.operation-section { display: flex; gap: 15px; }

/* 搜索框 */
.search-box { display: flex; }
.search-box input { padding: 8px 12px; border: 1px solid #dcdfe6; border-right: none; border-radius: 4px 0 0 4px; outline: none; font-size: 14px; width: 200px; transition: border 0.3s; }
.search-box input:focus { border-color: #1890ff; }
.btn-search { border-radius: 0 4px 4px 0; background: #f5f7fa; color: #606266; border: 1px solid #dcdfe6; border-left: none; }
.btn-search:hover { background: #e6f7ff; color: #1890ff; }

/* 按钮通用 */
.btn { padding: 8px 16px; border: none; cursor: pointer; font-size: 14px; border-radius: 4px; transition: all 0.3s; }
.btn-primary { background: #1890ff; color: white; box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3); }
.btn-primary:hover { background: #40a9ff; }
.btn-secondary { background: #fff; border: 1px solid #dcdfe6; color: #606266; }

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

/* 状态徽标 */
.status-badge { padding: 4px 8px; border-radius: 12px; font-size: 12px; font-weight: bold; }
.status-active { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.status-disabled { background: #fff1f0; color: #f5222d; border: 1px solid #ffa39e; }

/* 职称徽标 (新增) */
.badge-title { padding: 2px 6px; border-radius: 4px; font-size: 12px; }
.badge-prof { background: #fff7e6; color: #fa8c16; border: 1px solid #ffd591; }
.badge-assoc { background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }
.badge-normal { background: #f4f4f5; color: #909399; border: 1px solid #e9e9eb; }

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
.modal-box { background: white; width: 500px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: modalFadeIn 0.3s ease; }
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