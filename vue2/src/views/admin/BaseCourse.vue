<template>
  <div class="manage-container">
    <div class="action-header">
      <div class="title-section">
        <h2>课程基础库管理</h2>
        <p class="subtitle">建立学校课程元数据档案</p>
      </div>
      <div class="operation-section">
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="搜索课程名称或代码..."
            @keyup.enter="handleSearch"
          >
          <button class="btn btn-search" @click="handleSearch">🔍 搜索</button>
        </div>
        <button class="btn btn-primary" @click="openAddModal">+ 新增课程</button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th width="100">课程代码</th>
            <th>课程名称</th>
            <th width="80">学分</th>
            <th>所属学院</th>
            <th width="100">课程性质</th>
            <th>课程简介</th>
            <th width="180">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="course in filteredCourses" :key="course.id">
            <td class="code-col">{{ course.id }}</td>
            <td class="name-col">{{ course.name }}</td>
            <td class="credit-col">{{ course.credits }}</td>
            <td>{{ course.department }}</td>
            <td>
              <span class="type-badge" :class="getTypeClass(course.type)">
                {{ course.type }}
              </span>
            </td>
            <td class="desc-col" :title="course.description">
              {{ course.description }}
            </td>
            <td class="action-col">
              <button class="btn-text btn-edit" @click="openEditModal(course)" title="修改课程信息">
                编辑
              </button>
              
              <button class="btn-text btn-danger" @click="deleteCourse(course)" title="删除课程档案">
                删除
              </button>
            </td>
          </tr>
          <tr v-if="filteredCourses.length === 0">
            <td colspan="7" class="empty-state">暂无符合条件的课程数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="modal-mask" v-if="showModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>{{ isEditMode ? '编辑课程档案' : '新增基础课程' }}</h3>
          <span class="close-btn" @click="closeModal">×</span>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveCourse">
            <div class="form-row">
              <div class="form-group">
                <label>课程代码 <span class="required">*</span></label>
                <input type="text" v-model="form.id" :disabled="isEditMode" placeholder="唯一标识(如CS101)" required>
              </div>
              <div class="form-group">
                <label>课程名称 <span class="required">*</span></label>
                <input type="text" v-model="form.name" placeholder="输入课程名称" required>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>学分 <span class="required">*</span></label>
                <input type="number" v-model="form.credits" min="0" step="0.5" placeholder="例: 4.0" required>
              </div>
              <div class="form-group">
                <label>课程性质 <span class="required">*</span></label>
                <select v-model="form.type" required>
                  <option value="必修">必修</option>
                  <option value="选修">选修</option>
                  <option value="通识">通识</option>
                </select>
              </div>
            </div>

            <div class="form-group">
              <label>所属学院 <span class="required">*</span></label>
              <input type="text" v-model="form.department" placeholder="例: 计算机学院" required>
            </div>

            <div class="form-group">
              <label>课程简介</label>
              <textarea 
                v-model="form.description" 
                rows="4" 
                placeholder="简要描述课程内容、教学目标等..."
              ></textarea>
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
  name: 'BaseCourse',
  data() {
    return {
      searchQuery: '',
      showModal: false,
      isEditMode: false,
      // 模拟课程数据
      courses: [
        { id: 'CS101', name: '程序设计基础', credits: 4, department: '计算机学院', type: '必修', description: '本课程介绍C语言编程基础，包括变量、控制结构、函数等。' },
        { id: 'CS102', name: '数据结构', credits: 3.5, department: '计算机学院', type: '必修', description: '深入讲解线性表、树、图等核心数据结构及其算法实现。' },
        { id: 'SE201', name: '软件工程导论', credits: 3, department: '软件学院', type: '必修', description: '系统介绍软件生命周期、开发模型、需求分析与设计模式。' },
        { id: 'ART001', name: '西方美术史', credits: 2, department: '艺术学院', type: '通识', description: '赏析从古希腊到现代主义的西方绘画与雕塑艺术发展历程。' },
        { id: 'MATH202', name: '离散数学', credits: 4, department: '数学学院', type: '必修', description: '涵盖集合论、逻辑、图论等计算机科学的数学基础。' },
        { id: 'CS305', name: '人工智能', credits: 3, department: '计算机学院', type: '选修', description: '探索机器学习、神经网络及智能代理的基本原理与应用。' },
      ],
      // 表单对象
      form: {
        id: '', name: '', credits: '', department: '', type: '必修', description: ''
      }
    }
  },
  computed: {
    // 搜索功能
    filteredCourses() {
      if (!this.searchQuery) return this.courses;
      const query = this.searchQuery.toLowerCase();
      return this.courses.filter(c => 
        c.name.toLowerCase().includes(query) || 
        c.id.toLowerCase().includes(query)
      );
    }
  },
  methods: {
    handleSearch() {
      console.log('Searching course:', this.searchQuery);
    },
    
    // 动态获取课程性质的样式类
    getTypeClass(type) {
      if (type === '必修') return 'badge-required';
      if (type === '选修') return 'badge-elective';
      if (type === '通识') return 'badge-general';
      return 'badge-normal';
    },

    // --- 弹窗逻辑 ---
    openAddModal() {
      this.isEditMode = false;
      this.form = { id: '', name: '', credits: '', department: '', type: '必修', description: '' };
      this.showModal = true;
    },
    openEditModal(course) {
      this.isEditMode = true;
      // 深拷贝，防止修改时表格实时变动
      this.form = JSON.parse(JSON.stringify(course));
      this.showModal = true;
    },
    closeModal() {
      this.showModal = false;
    },
    saveCourse() {
      if (this.isEditMode) {
        // 更新逻辑 (1.4.2)
        const index = this.courses.findIndex(c => c.id === this.form.id);
        if (index !== -1) {
          this.courses.splice(index, 1, { ...this.form }); // 替换数据
          alert('课程档案更新成功！');
        }
      } else {
        // 新增逻辑 (1.4.1)
        if (this.courses.find(c => c.id === this.form.id)) {
          return alert('错误：该课程代码已存在！');
        }
        this.courses.push({ ...this.form });
        alert('基础课程新增成功！');
      }
      this.closeModal();
    },

    // --- 删除课程 (1.4.2) ---
    deleteCourse(course) {
      if (confirm(`【警告】确定要删除课程档案 "${course.name} (${course.id})" 吗？\n此操作可能会影响已排课的班级！`)) {
        this.courses = this.courses.filter(c => c.id !== course.id);
        alert('课程档案已删除。');
      }
    }
  }
}
</script>

<style scoped>
/* 样式体系与 TeacherManage/StudentManage 保持一致 */
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
.data-table { width: 100%; border-collapse: collapse; text-align: left; table-layout: fixed; /* 固定布局，防止简介撑开 */ }
.data-table th { background: #fafafa; padding: 16px; color: #333; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 16px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.data-table tr:hover { background-color: #f5f7fa; }

/* 列样式 */
.code-col { font-family: monospace; font-weight: bold; color: #333; }
.name-col { font-weight: 500; color: #303133; }
.credit-col { font-weight: bold; color: #1890ff; }
.desc-col { 
  white-space: nowrap; 
  overflow: hidden; 
  text-overflow: ellipsis; 
  color: #999; 
  font-size: 13px;
  max-width: 200px; /* 限制最大宽度 */
}

/* 课程性质徽标 */
.type-badge { padding: 3px 8px; border-radius: 4px; font-size: 12px; font-weight: 500; border: 1px solid transparent; }
.badge-required { background: #e6f7ff; color: #1890ff; border-color: #91d5ff; } /* 必修 - 蓝 */
.badge-elective { background: #f6ffed; color: #52c41a; border-color: #b7eb8f; } /* 选修 - 绿 */
.badge-general { background: #fff7e6; color: #fa8c16; border-color: #ffd591; } /* 通识 - 橙 */
.badge-normal { background: #f4f4f5; color: #909399; border-color: #e9e9eb; }

/* 操作按钮 */
.action-col { display: flex; gap: 12px; }
.btn-text { background: none; border: none; cursor: pointer; font-size: 13px; padding: 0; }
.btn-edit { color: #1890ff; } .btn-edit:hover { text-decoration: underline; }
.btn-danger { color: #f5222d; } .btn-danger:hover { text-decoration: underline; }

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
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 8px 10px; border: 1px solid #dcdfe6; border-radius: 4px; box-sizing: border-box; font-size: 14px; font-family: inherit; }
.form-group input:focus, .form-group select:focus, .form-group textarea:focus { border-color: #1890ff; outline: none; }
.form-group input:disabled { background: #f5f7fa; cursor: not-allowed; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
</style>