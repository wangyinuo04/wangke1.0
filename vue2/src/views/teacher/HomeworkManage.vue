<template>
  <div class="manage-container">
    
    <div v-if="currentView === 'list'" class="view-wrapper fade-in">
      <div class="action-header">
        <div class="title-section">
          <h2>作业管理</h2>
          <p class="subtitle">发布课程作业、在线批阅及成绩统计</p>
        </div>
        <div class="operation-section">
          <div class="search-box">
            <input type="text" v-model="searchQuery" placeholder="搜索作业标题..." @keyup.enter="handleSearch">
            <button class="btn btn-search" @click="handleSearch">🔍 搜索</button>
          </div>
          <button class="btn btn-primary" @click="openCreateModal">+ 发布新作业</button>
        </div>
      </div>

      <div class="table-card">
        <table class="data-table">
          <thead>
            <tr>
              <th>状态</th>
              <th>作业标题</th>
              <th>关联班级</th>
              <th>截止时间</th>
              <th width="120">提交进度</th>
              <th width="200">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="hw in filteredHomeworks" :key="hw.id">
              <td>
                <span class="status-badge" :class="getStatusClass(hw)">
                  {{ getStatusText(hw) }}
                </span>
              </td>
              <td class="title-col">{{ hw.title }}</td>
              <td>{{ getClassName(hw.classId) }}</td>
              <td class="time-col">{{ hw.deadline }}</td>
              <td>
                <div class="progress-box">
                  <span class="progress-text">{{ hw.submittedCount }}/{{ hw.totalCount }}</span>
                  <div class="progress-bar">
                    <div class="fill" :style="{ width: (hw.submittedCount / hw.totalCount * 100) + '%' }"></div>
                  </div>
                </div>
              </td>
              <td>
                <div class="action-col">
                  <button class="btn-text btn-primary" @click="enterGrading(hw)">批阅管理</button>
                  <button class="btn-text btn-edit" @click="editHomework(hw)">编辑</button>
                  <button class="btn-text btn-danger" @click="deleteHomework(hw.id)">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredHomeworks.length === 0">
              <td colspan="6" class="empty-state">暂无作业记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-else class="view-wrapper fade-in">
      <div class="detail-header">
        <button class="btn-back" @click="currentView = 'list'">← 返回列表</button>
        <div class="detail-info">
          <h3>{{ currentHomework.title }}</h3>
          <span class="detail-meta">截止: {{ currentHomework.deadline }} | 总分: {{ currentHomework.totalScore }}分</span>
        </div>
        <div class="detail-actions">
          <button class="btn btn-secondary" @click="batchDownload">
            📦 批量下载附件 (ZIP)
          </button>
        </div>
      </div>

      <div class="stats-panel">
        <div class="stat-item">
          <label>提交率</label>
          <div class="value">{{ currentHomework.submittedCount }} / {{ currentHomework.totalCount }}</div>
        </div>
        <div class="stat-item">
          <label>已批改</label>
          <div class="value">{{ getGradedCount() }} 人</div>
        </div>
        <div class="stat-item">
          <label>平均分</label>
          <div class="value highlight">{{ getAverageScore() }} 分</div>
        </div>
      </div>

      <div class="table-card">
        <table class="data-table">
          <thead>
            <tr>
              <th>学号</th>
              <th>姓名</th>
              <th>提交状态</th>
              <th>提交时间</th>
              <th>附件</th>
              <th>成绩</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="sub in currentSubmissions" :key="sub.studentId">
              <td class="mono">{{ sub.studentId }}</td>
              <td>{{ sub.studentName }}</td>
              <td>
                <span :class="sub.status === '已提交' ? 'text-green' : 'text-gray'">
                  {{ sub.status }}
                </span>
              </td>
              <td class="time-col">{{ sub.submitTime || '-' }}</td>
              <td>
                <a v-if="sub.attachment" href="#" class="file-link" @click.prevent="downloadFile(sub.attachment)">
                  📄 下载
                </a>
                <span v-else class="text-gray">-</span>
              </td>
              <td>
                <span v-if="sub.score !== null" class="score-tag">{{ sub.score }}</span>
                <span v-else class="text-gray">-</span>
              </td>
              <td>
                <button 
                  class="btn-text btn-primary" 
                  :disabled="sub.status !== '已提交'"
                  @click="openGradeModal(sub)"
                >
                  {{ sub.score !== null ? '修改评分' : '批改' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-mask" v-if="showCreateModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>{{ isEditMode ? '编辑作业' : '发布新作业' }}</h3>
          <span class="close-btn" @click="closeCreateModal">×</span>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveHomework">
            <div class="form-group">
              <label>作业标题 <span class="required">*</span></label>
              <input type="text" v-model="form.title" placeholder="例：第一次实验报告" required>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>关联教学班 <span class="required">*</span></label>
                <select v-model="form.classId" :disabled="isEditMode" required>
                  <option value="" disabled>请选择班级</option>
                  <option v-for="cls in mockClasses" :key="cls.id" :value="cls.id">{{ cls.name }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>作业总分 <span class="required">*</span></label>
                <input type="number" v-model.number="form.totalScore" min="1" max="100" required>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>发布时间</label>
                <input type="datetime-local" v-model="form.publishTime" required>
              </div>
              <div class="form-group">
                <label>截止时间 <span class="required">*</span></label>
                <input type="datetime-local" v-model="form.deadline" required>
              </div>
            </div>

            <div class="form-group">
              <label>作业说明 (富文本)</label>
              <textarea v-model="form.description" rows="4" placeholder="请输入作业要求、注意事项等..."></textarea>
            </div>

            <div class="form-group">
              <label>附件资料</label>
              <div class="file-box">
                <input type="text" v-model="form.attachment" placeholder="无附件" readonly>
                <button type="button" class="btn btn-sm btn-secondary" @click="mockUpload">上传</button>
              </div>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeCreateModal">取消</button>
              <button type="submit" class="btn btn-primary">发布</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showGradeModal">
      <div class="modal-box small-modal">
        <div class="modal-header">
          <h3>作业批改 - {{ currentStudentSub.studentName }}</h3>
          <span class="close-btn" @click="closeGradeModal">×</span>
        </div>
        <div class="modal-body">
          <div class="submission-info">
            <p><strong>提交时间：</strong>{{ currentStudentSub.submitTime }}</p>
            <p><strong>附件内容：</strong>{{ currentStudentSub.attachment }}</p>
          </div>
          
          <div class="form-group">
            <label>得分 (满分 {{ currentHomework.totalScore }}) <span class="required">*</span></label>
            <input type="number" v-model.number="gradeForm.score" :max="currentHomework.totalScore" min="0" required>
          </div>
          
          <div class="form-group">
            <label>教师评语</label>
            <textarea v-model="gradeForm.comment" rows="3" placeholder="请输入评语..."></textarea>
          </div>

          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeGradeModal">取消</button>
            <button class="btn btn-primary" @click="submitGrade">确认打分</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'HomeworkManage',
  data() {
    return {
      currentView: 'list', // 'list' | 'detail'
      searchQuery: '',
      showCreateModal: false,
      showGradeModal: false,
      isEditMode: false,

      // --- 模拟数据: 班级列表 ---
      mockClasses: [
        { id: 101, name: '软件工程-2201班' },
        { id: 102, name: 'Web前端-卓越班' }
      ],

      // --- 模拟数据: 作业列表 ---
      homeworks: [
        { 
          id: 1, 
          title: '需求分析规格说明书', 
          classId: 101, 
          publishTime: '2025-09-01 08:00', 
          deadline: '2025-09-15 23:59', 
          totalScore: 100,
          description: '请按照模板完成SRS编写。',
          attachment: '模板.doc',
          submittedCount: 28,
          totalCount: 30
        },
        { 
          id: 2, 
          title: 'Vue3 组件化开发实验', 
          classId: 102, 
          publishTime: '2025-09-10 10:00', 
          deadline: '2025-09-20 18:00', 
          totalScore: 10,
          description: '',
          attachment: '',
          submittedCount: 5,
          totalCount: 25
        }
      ],

      // --- 模拟数据: 某个作业的提交记录 (进入详情时加载) ---
      currentHomework: null,
      currentSubmissions: [],
      mockSubmissionsRepo: [
        { studentId: 'S2023001', studentName: '张三', status: '已提交', submitTime: '2025-09-14 10:00', attachment: '张三_SRS.pdf', score: null, comment: '' },
        { studentId: 'S2023002', studentName: '李四', status: '已提交', submitTime: '2025-09-15 09:30', attachment: '李四_SRS.pdf', score: 85, comment: '写得不错' },
        { studentId: 'S2023003', studentName: '王五', status: '未提交', submitTime: '', attachment: '', score: null, comment: '' }
      ],

      // 表单对象
      form: { id: null, title: '', classId: '', totalScore: 100, publishTime: '', deadline: '', description: '', attachment: '' },
      gradeForm: { score: '', comment: '' },
      currentStudentSub: null
    }
  },
  computed: {
    filteredHomeworks() {
      if (!this.searchQuery) return this.homeworks;
      return this.homeworks.filter(h => h.title.includes(this.searchQuery));
    }
  },
  methods: {
    handleSearch() { console.log('Searching...'); },
    
    // 辅助函数
    getClassName(id) {
      const cls = this.mockClasses.find(c => c.id === id);
      return cls ? cls.name : '未知班级';
    },
    getStatusText(hw) {
      const now = new Date();
      const start = new Date(hw.publishTime);
      const end = new Date(hw.deadline);
      if (now < start) return '未发布';
      if (now > end) return '已结束';
      return '进行中';
    },
    getStatusClass(hw) {
      const text = this.getStatusText(hw);
      if (text === '进行中') return 'status-active';
      if (text === '已结束') return 'status-end';
      return 'status-wait';
    },

    // --- 2.4.1 发布作业逻辑 ---
    openCreateModal() {
      this.isEditMode = false;
      this.form = { id: Date.now(), title: '', classId: '', totalScore: 100, publishTime: '', deadline: '', description: '', attachment: '' };
      this.showCreateModal = true;
    },
    editHomework(hw) {
      this.isEditMode = true;
      this.form = { ...hw };
      this.showCreateModal = true;
    },
    mockUpload() {
      this.form.attachment = 'uploaded_file_' + Math.floor(Math.random() * 1000) + '.zip';
    },
    closeCreateModal() { this.showCreateModal = false; },
    saveHomework() {
      if (this.isEditMode) {
        const idx = this.homeworks.findIndex(h => h.id === this.form.id);
        if (idx !== -1) this.homeworks.splice(idx, 1, this.form);
      } else {
        this.form.submittedCount = 0;
        this.form.totalCount = 30; // 模拟班级人数
        this.homeworks.push(this.form);
      }
      this.closeCreateModal();
    },
    deleteHomework(id) {
      if (confirm('确定删除该作业吗？所有提交记录也将被删除！')) {
        this.homeworks = this.homeworks.filter(h => h.id !== id);
      }
    },

    // --- 进入批改详情页 (2.4.4) ---
    enterGrading(hw) {
      this.currentHomework = hw;
      // 模拟加载该作业的提交数据
      this.currentSubmissions = JSON.parse(JSON.stringify(this.mockSubmissionsRepo)); 
      this.currentView = 'detail';
    },
    
    // --- 2.4.2 在线批改逻辑 ---
    openGradeModal(sub) {
      this.currentStudentSub = sub;
      this.gradeForm = { score: sub.score, comment: sub.comment };
      this.showGradeModal = true;
    },
    closeGradeModal() { this.showGradeModal = false; },
    submitGrade() {
      // 更新本地数据
      this.currentStudentSub.score = this.gradeForm.score;
      this.currentStudentSub.comment = this.gradeForm.comment;
      
      // 更新状态（模拟）
      alert(`批改成功！\n得分: ${this.gradeForm.score}`);
      this.closeGradeModal();
    },

    // --- 2.4.3 批量下载 ---
    batchDownload() {
      if (this.currentHomework.submittedCount === 0) return alert('暂无学生提交作业');
      alert(`正在打包下载... \n文件: ${this.currentHomework.title}_批量作业.zip`);
    },
    downloadFile(fileName) {
      alert(`下载文件: ${fileName}`);
    },

    // --- 统计计算 ---
    getGradedCount() {
      return this.currentSubmissions.filter(s => s.score !== null).length;
    },
    getAverageScore() {
      const graded = this.currentSubmissions.filter(s => s.score !== null);
      if (graded.length === 0) return 0;
      const sum = graded.reduce((a, b) => a + Number(b.score), 0);
      return (sum / graded.length).toFixed(1);
    }
  }
}
</script>

<style scoped>
/* 基础样式复用 */
.manage-container { padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; height: 100%; }
.action-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.title-section h2 { margin: 0; font-size: 20px; color: #333; }
.subtitle { margin: 5px 0 0; font-size: 13px; color: #999; }
.operation-section { display: flex; gap: 15px; }

/* 搜索 & 按钮 */
.search-box { display: flex; }
.search-box input { padding: 8px 12px; border: 1px solid #dcdfe6; border-right: none; border-radius: 4px 0 0 4px; width: 200px; }
.btn-search { border-radius: 0 4px 4px 0; background: #f5f7fa; color: #606266; border: 1px solid #dcdfe6; border-left: none; cursor: pointer; padding: 8px 12px; }
.btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; transition: all 0.3s; }
.btn-primary { background: #1890ff; color: white; }
.btn-secondary { background: white; border: 1px solid #dcdfe6; color: #606266; }
.btn-sm { padding: 4px 10px; font-size: 12px; }

/* 列表视图 */
.table-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #fafafa; padding: 15px; color: #606266; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 15px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.data-table tr:hover { background-color: #f9f9f9; }

.title-col { font-weight: bold; color: #333; }
.time-col { font-family: monospace; font-size: 13px; }
.status-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.status-active { background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; }
.status-end { background: #f5f5f5; color: #909399; border: 1px solid #e9e9eb; }
.status-wait { background: #fff7e6; color: #fa8c16; border: 1px solid #ffd591; }

.progress-box { width: 100px; }
.progress-text { font-size: 12px; display: block; margin-bottom: 2px; }
.progress-bar { width: 100%; height: 6px; background: #f0f0f0; border-radius: 3px; overflow: hidden; }
.fill { height: 100%; background: #52c41a; }

.action-col { display: flex; gap: 8px; }
.btn-text { background: none; border: none; cursor: pointer; font-size: 13px; padding: 0; }
.btn-text.btn-primary { color: #1890ff; font-weight: bold; }
.btn-text.btn-edit { color: #909399; }
.btn-text.btn-danger { color: #f5222d; }

/* 详情视图 (Detail) */
.detail-header { background: #fff; padding: 20px; border-radius: 8px; margin-bottom: 20px; display: flex; align-items: center; gap: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.btn-back { border: none; background: none; color: #606266; font-size: 14px; cursor: pointer; font-weight: bold; }
.btn-back:hover { color: #1890ff; }
.detail-info h3 { margin: 0 0 5px; font-size: 20px; color: #333; }
.detail-meta { color: #909399; font-size: 13px; }
.detail-actions { margin-left: auto; }

.stats-panel { display: flex; gap: 20px; margin-bottom: 20px; }
.stat-item { flex: 1; background: #fff; padding: 20px; border-radius: 8px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.03); }
.stat-item label { color: #909399; font-size: 13px; display: block; margin-bottom: 5px; }
.stat-item .value { font-size: 24px; font-weight: bold; color: #333; }
.stat-item .value.highlight { color: #fa8c16; }

.file-link { color: #1890ff; text-decoration: none; font-size: 13px; }
.file-link:hover { text-decoration: underline; }
.text-green { color: #52c41a; }
.text-gray { color: #ccc; }
.score-tag { background: #f6ffed; color: #52c41a; padding: 2px 6px; border-radius: 4px; font-weight: bold; border: 1px solid #b7eb8f; }
.mono { font-family: monospace; }

/* 弹窗样式 */
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; width: 600px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: fade 0.3s; }
.small-modal { width: 400px; }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 16px; }
.close-btn { font-size: 20px; cursor: pointer; color: #999; }
.modal-body { padding: 20px; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }

.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; font-weight: 500; font-size: 13px; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 8px; border: 1px solid #dcdfe6; border-radius: 4px; box-sizing: border-box; }
.form-row { display: flex; gap: 15px; }
.form-row .form-group { flex: 1; }
.file-box { display: flex; gap: 10px; }
.submission-info { background: #f5f7fa; padding: 10px; border-radius: 4px; margin-bottom: 15px; font-size: 13px; color: #606266; }
.submission-info p { margin: 5px 0; }

@keyframes fade { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
.fade-in { animation: fade 0.3s; }
</style>