<template>
  <div class="manage-container">
    <div class="action-header">
      <div class="title-section">
        <h2>排课与权限分配</h2>
        <p class="subtitle">开设教学班级并分配学生选课权限</p>
      </div>
      <div class="operation-section">
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="搜索教学班名称..."
            @keyup.enter="handleSearch"
          >
          <button class="btn btn-search" @click="handleSearch">🔍 搜索</button>
        </div>
        <button class="btn btn-primary" @click="openClassModal">+ 开设教学班</button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th width="120">开课学期</th>
            <th>教学班名称</th>
            <th>关联课程</th>
            <th>授课教师</th>
            <th width="150">选课人数</th>
            <th width="220">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cls in filteredClasses" :key="cls.id">
            <td><span class="term-tag">{{ cls.semester }}</span></td>
            <td class="name-col">{{ cls.className }}</td>
            <td>
              <div class="course-info">
                <span class="code">{{ cls.courseId }}</span>
                <span>{{ getCourseName(cls.courseId) }}</span>
              </div>
            </td>
            <td>{{ getTeacherName(cls.teacherId) }}</td>
            <td>
              <div class="capacity-box">
                <span :class="{'text-red': cls.students.length >= cls.maxSize}">
                  {{ cls.students.length }} / {{ cls.maxSize }}
                </span>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: (cls.students.length / cls.maxSize * 100) + '%' }"></div>
                </div>
              </div>
            </td>
            <td class="action-col">
              <button class="btn-text btn-edit" @click="openStudentModal(cls)" title="分配学生">
                👥 管理学生
              </button>
              
              <button class="btn-text btn-info" @click="openEditClassModal(cls)" title="修改设置">
                设置
              </button>

              <button class="btn-text btn-danger" @click="deleteClass(cls)" title="解散班级">
                解散
              </button>
            </td>
          </tr>
          <tr v-if="filteredClasses.length === 0">
            <td colspan="6" class="empty-state">暂无排课记录，请点击右上角开设新班级</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="modal-mask" v-if="showClassModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>{{ isEditMode ? '修改排课信息' : '开设新教学班' }}</h3>
          <span class="close-btn" @click="closeClassModal">×</span>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveClass">
            <div class="form-row">
              <div class="form-group">
                <label>基础课程 <span class="required">*</span></label>
                <select v-model="classForm.courseId" :disabled="isEditMode" required>
                  <option value="" disabled>请选择课程</option>
                  <option v-for="c in mockBaseCourses" :key="c.id" :value="c.id">
                    {{ c.name }} ({{ c.id }})
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label>授课教师 <span class="required">*</span></label>
                <select v-model="classForm.teacherId" required>
                  <option value="" disabled>请选择教师</option>
                  <option v-for="t in mockTeachers" :key="t.id" :value="t.id">
                    {{ t.name }} ({{ t.id }})
                  </option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>开课学期 <span class="required">*</span></label>
                <select v-model="classForm.semester" required>
                  <option>2025-2026-1</option>
                  <option>2025-2026-2</option>
                  <option>2024-2025-2</option>
                </select>
              </div>
              <div class="form-group">
                <label>最大人数 <span class="required">*</span></label>
                <input type="number" v-model.number="classForm.maxSize" min="1" max="200" required>
              </div>
            </div>

            <div class="form-group">
              <label>教学班名称 <span class="required">*</span></label>
              <input type="text" v-model="classForm.className" placeholder="例: 软件工程-计算机2班" required>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeClassModal">取消</button>
              <button type="submit" class="btn btn-primary">保存</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showStudentModal">
      <div class="modal-box wide-modal">
        <div class="modal-header">
          <h3>管理班级成员 - {{ currentClass.className }}</h3>
          <span class="close-btn" @click="closeStudentModal">×</span>
        </div>
        <div class="modal-body student-manage-body">
          
          <div class="panel-left">
            <div class="tool-card">
              <h4>单人分配学生</h4>
              <div class="input-row">
                <input type="text" v-model="singleStudentId" placeholder="输入学生学号..." @keyup.enter="addSingleStudent">
                <button class="btn btn-sm btn-primary" @click="addSingleStudent">添加</button>
              </div>
              <p class="hint">输入学号将该学生强制加入班级。</p>
            </div>

            <div class="tool-card">
              <h4>按行政班导入</h4>
              <div class="input-row">
                <input type="text" v-model="targetAdminClass" placeholder="输入行政班级(如软件2201)..." @keyup.enter="batchImport">
                <button class="btn btn-sm btn-success" @click="batchImport">批量导入</button>
              </div>
              <p class="hint">系统将自动查找该行政班级下的所有学生。</p>
            </div>
            
            <div class="stat-info">
              当前人数: <strong>{{ currentClass.students.length }}</strong> / {{ currentClass.maxSize }}
            </div>
          </div>

          <div class="panel-right">
            <h4>已选课学生名单</h4>
            <ul class="student-list">
              <li v-for="(stuId, index) in currentClass.students" :key="stuId">
                <span class="s-index">{{ index + 1 }}.</span>
                <span class="s-info">{{ getStudentName(stuId) }} <span class="s-id">({{ stuId }})</span></span>
                <span class="remove-icon" @click="removeStudent(stuId)" title="移除">×</span>
              </li>
              <li v-if="currentClass.students.length === 0" class="empty-list">暂无学生</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'TeachingClass',
  data() {
    return {
      searchQuery: '',
      showClassModal: false,
      showStudentModal: false,
      isEditMode: false,
      
      // --- 模拟基础数据 (实际应从后台获取) ---
      mockBaseCourses: [
        { id: 'CS101', name: '程序设计基础' },
        { id: 'SE201', name: '软件工程导论' },
        { id: 'MATH202', name: '离散数学' }
      ],
      mockTeachers: [
        { id: 'T2023001', name: '王建国' },
        { id: 'T2023002', name: '李晓梅' }
      ],
      // 模拟全校学生库 (用于1.5.2和1.5.3查找)
      mockAllStudents: [
        { id: 'S2023001', name: '张三', class: '软件2201' },
        { id: 'S2023002', name: '李四', class: '计科2202' },
        { id: 'S2023003', name: '王五', class: '软件2201' },
        { id: 'S2023004', name: '赵六', class: '物联网2301' },
        { id: 'S2023005', name: '钱七', class: '计科2202' },
        { id: 'S2023006', name: '孙悟空', class: '软件2201' }
      ],

      // --- 教学班数据 ---
      teachingClasses: [
        { 
          id: 1, 
          courseId: 'CS101', 
          teacherId: 'T2023001', 
          semester: '2025-2026-1', 
          className: '程序设计-软件1班', 
          maxSize: 60,
          students: ['S2023001', 'S2023003'] // 存储学号
        },
        { 
          id: 2, 
          courseId: 'SE201', 
          teacherId: 'T2023002', 
          semester: '2025-2026-1', 
          className: '软件工程-卓越班', 
          maxSize: 40,
          students: [] 
        }
      ],

      // 表单模型
      classForm: { id: null, courseId: '', teacherId: '', semester: '2025-2026-1', className: '', maxSize: 50 },
      
      // 学生管理相关
      currentClass: null, // 当前正在操作的班级对象
      singleStudentId: '',
      targetAdminClass: ''
    }
  },
  computed: {
    filteredClasses() {
      if (!this.searchQuery) return this.teachingClasses;
      const q = this.searchQuery.toLowerCase();
      return this.teachingClasses.filter(c => c.className.toLowerCase().includes(q));
    }
  },
  methods: {
    handleSearch() { console.log('Searching...'); },
    
    // 辅助获取名称
    getCourseName(id) {
      const c = this.mockBaseCourses.find(x => x.id === id);
      return c ? c.name : id;
    },
    getTeacherName(id) {
      const t = this.mockTeachers.find(x => x.id === id);
      return t ? t.name : id;
    },
    getStudentName(id) {
      const s = this.mockAllStudents.find(x => x.id === id);
      return s ? s.name : '未知学生';
    },

    // --- 1.5.1 开设/编辑班级 ---
    openClassModal() {
      this.isEditMode = false;
      this.classForm = { id: Date.now(), courseId: '', teacherId: '', semester: '2025-2026-1', className: '', maxSize: 50 };
      this.showClassModal = true;
    },
    openEditClassModal(cls) {
      this.isEditMode = true;
      this.classForm = JSON.parse(JSON.stringify(cls));
      this.showClassModal = true;
    },
    closeClassModal() { this.showClassModal = false; },
    
    saveClass() {
      if (this.isEditMode) {
        const idx = this.teachingClasses.findIndex(c => c.id === this.classForm.id);
        if (idx !== -1) {
          // 保留原有的学生列表
          this.classForm.students = this.teachingClasses[idx].students;
          this.teachingClasses.splice(idx, 1, this.classForm);
        }
      } else {
        this.classForm.students = []; // 新班级学生为空
        this.teachingClasses.push(this.classForm);
      }
      this.closeClassModal();
      alert(this.isEditMode ? '修改成功' : '开课成功');
    },
    deleteClass(cls) {
      if (confirm(`确定要解散班级 "${cls.className}" 吗？\n这将移除所有已选课学生的关联！`)) {
        this.teachingClasses = this.teachingClasses.filter(c => c.id !== cls.id);
      }
    },

    // --- 学生分配管理 ---
    openStudentModal(cls) {
      this.currentClass = cls; // 引用传递，直接修改会反应到列表中
      this.singleStudentId = '';
      this.targetAdminClass = '';
      this.showStudentModal = true;
    },
    closeStudentModal() {
      this.showStudentModal = false;
      this.currentClass = null;
    },

    // 1.5.2 单人分配
    addSingleStudent() {
      if (!this.singleStudentId) return alert('请输入学号');
      // 1. 检查是否存在
      const student = this.mockAllStudents.find(s => s.id === this.singleStudentId);
      if (!student) return alert('错误：找不到该学号的学生！');
      
      // 2. 检查是否重复
      if (this.currentClass.students.includes(student.id)) return alert('该学生已在班级中！');
      
      // 3. 检查容量
      if (this.currentClass.students.length >= this.currentClass.maxSize) return alert('错误：班级人数已满！');

      this.currentClass.students.push(student.id);
      this.singleStudentId = ''; // 清空输入
      // alert(`已添加: ${student.name}`);
    },

    // 1.5.3 批量导入
    batchImport() {
      if (!this.targetAdminClass) return alert('请输入行政班级名称');
      
      // 1. 查找该行政班的所有学生
      const targets = this.mockAllStudents.filter(s => s.class === this.targetAdminClass);
      
      if (targets.length === 0) return alert(`未找到行政班级 "${this.targetAdminClass}" 的任何学生`);

      let successCount = 0;
      let fullFlag = false;

      targets.forEach(s => {
        if (fullFlag) return;
        // 检查重复
        if (!this.currentClass.students.includes(s.id)) {
          // 检查容量
          if (this.currentClass.students.length < this.currentClass.maxSize) {
            this.currentClass.students.push(s.id);
            successCount++;
          } else {
            fullFlag = true;
          }
        }
      });

      if (fullFlag) {
        alert(`导入中断：班级容量已满！\n成功导入 ${successCount} 人。`);
      } else {
        alert(`批量导入完成！\n共找到 ${targets.length} 人，成功加入 ${successCount} 人 (自动过滤已存在学生)。`);
      }
      this.targetAdminClass = '';
    },

    removeStudent(stuId) {
      if (confirm('确定将该学生移出班级吗？')) {
        this.currentClass.students = this.currentClass.students.filter(id => id !== stuId);
      }
    }
  }
}
</script>

<style scoped>
/* 样式复用 */
.manage-container { padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; }
.action-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.title-section h2 { margin: 0; font-size: 20px; color: #333; }
.subtitle { margin: 5px 0 0; font-size: 13px; color: #999; }
.operation-section { display: flex; gap: 15px; }

/* 搜索 & 按钮 */
.search-box { display: flex; }
.search-box input { padding: 8px 12px; border: 1px solid #dcdfe6; border-right: none; border-radius: 4px 0 0 4px; outline: none; font-size: 14px; width: 200px; }
.search-box input:focus { border-color: #1890ff; }
.btn-search { border-radius: 0 4px 4px 0; background: #f5f7fa; color: #606266; border: 1px solid #dcdfe6; border-left: none; }
.btn-search:hover { background: #e6f7ff; color: #1890ff; }
.btn { padding: 8px 16px; border: none; cursor: pointer; font-size: 14px; border-radius: 4px; transition: all 0.3s; }
.btn-sm { padding: 6px 12px; font-size: 12px; }
.btn-primary { background: #1890ff; color: white; box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3); }
.btn-primary:hover { background: #40a9ff; }
.btn-success { background: #52c41a; color: white; }
.btn-secondary { background: #fff; border: 1px solid #dcdfe6; color: #606266; }

/* 表格 */
.table-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #fafafa; padding: 16px; color: #333; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 16px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.data-table tr:hover { background-color: #f5f7fa; }

/* 表格内元素 */
.term-tag { background: #f0f5ff; color: #2f54eb; border: 1px solid #adc6ff; padding: 2px 6px; border-radius: 4px; font-size: 12px; font-family: monospace; }
.name-col { font-weight: 500; color: #333; }
.course-info { display: flex; flex-direction: column; font-size: 13px; }
.course-info .code { font-weight: bold; color: #909399; font-size: 12px; }

/* 容量进度条 */
.capacity-box { width: 100px; font-size: 12px; text-align: center; }
.text-red { color: #f5222d; font-weight: bold; }
.progress-bar { width: 100%; height: 6px; background: #f5f5f5; border-radius: 3px; margin-top: 4px; overflow: hidden; }
.progress-fill { height: 100%; background: #52c41a; transition: width 0.3s; }
.text-red ~ .progress-bar .progress-fill { background: #f5222d; } /* 满员变红 */

.action-col { display: flex; gap: 8px; }
.btn-text { background: none; border: none; cursor: pointer; font-size: 13px; padding: 0; }
.btn-edit { color: #1890ff; }
.btn-info { color: #909399; }
.btn-danger { color: #f5222d; }
.empty-state { text-align: center; padding: 40px; color: #999; }

/* 弹窗通用 */
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; width: 550px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: modalFadeIn 0.3s ease; display: flex; flex-direction: column; max-height: 90vh; }
.wide-modal { width: 800px; height: 600px; } /* 学生管理弹窗更宽更高 */
@keyframes modalFadeIn { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; flex-shrink: 0; }
.modal-header h3 { margin: 0; font-size: 16px; color: #333; }
.close-btn { font-size: 20px; cursor: pointer; color: #999; }
.modal-body { padding: 20px; overflow-y: auto; }

/* 表单样式 */
.form-row { display: flex; gap: 15px; }
.form-group { margin-bottom: 15px; flex: 1; }
.form-group label { display: block; margin-bottom: 8px; font-size: 13px; font-weight: 500; color: #606266; }
.required { color: #f56c6c; margin-left: 2px; }
.form-group input, .form-group select { width: 100%; padding: 8px 10px; border: 1px solid #dcdfe6; border-radius: 4px; box-sizing: border-box; font-size: 14px; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; margin-top: auto; }

/* --- 学生管理弹窗专用样式 --- */
.student-manage-body { display: flex; gap: 20px; height: 100%; padding: 0; }
.panel-left { width: 320px; border-right: 1px solid #eee; padding: 20px; background: #f9fafc; }
.panel-right { flex: 1; padding: 20px; overflow-y: auto; }

.tool-card { background: #fff; border: 1px solid #e4e7ed; border-radius: 4px; padding: 15px; margin-bottom: 15px; box-shadow: 0 1px 4px rgba(0,0,0,0.03); }
.tool-card h4 { margin: 0 0 10px; font-size: 14px; color: #333; border-left: 3px solid #1890ff; padding-left: 8px; }
.input-row { display: flex; gap: 5px; }
.input-row input { flex: 1; padding: 6px; border: 1px solid #dcdfe6; border-radius: 3px; font-size: 13px; }
.hint { font-size: 12px; color: #999; margin: 5px 0 0; }
.stat-info { margin-top: 20px; text-align: center; font-size: 14px; color: #606266; }

.student-list { list-style: none; padding: 0; margin: 0; }
.student-list li { display: flex; align-items: center; padding: 10px; border-bottom: 1px solid #f0f0f0; transition: background 0.2s; }
.student-list li:hover { background: #f5f7fa; }
.s-index { width: 30px; color: #999; font-size: 12px; }
.s-info { flex: 1; font-weight: 500; color: #333; }
.s-id { color: #999; font-weight: normal; font-size: 13px; margin-left: 5px; }
.remove-icon { color: #ff4d4f; cursor: pointer; font-size: 18px; font-weight: bold; padding: 0 5px; }
.remove-icon:hover { background: #fff1f0; border-radius: 4px; }
.empty-list { color: #ccc; text-align: center; padding: 20px; font-style: italic; }
</style>