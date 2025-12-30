<template>
  <div class="manage-container">
    
    <div class="nav-tabs">
      <div 
        class="tab-item" 
        :class="{ active: currentTab === 'bank' }" 
        @click="currentTab = 'bank'"
      >
        📖 题库管理
      </div>
      <div 
        class="tab-item" 
        :class="{ active: currentTab === 'paper' }" 
        @click="currentTab = 'paper'"
      >
        📝 试卷组卷
      </div>
      <div 
        class="tab-item" 
        :class="{ active: currentTab === 'exam' }" 
        @click="currentTab = 'exam'"
      >
        ⏰ 考试安排
      </div>
      <div 
        class="tab-item" 
        :class="{ active: currentTab === 'result' }" 
        @click="currentTab = 'result'"
      >
        📊 阅卷与统计
      </div>
    </div>

    <div v-if="currentTab === 'bank'" class="tab-content fade-in">
      <div class="top-actions">
        <button class="btn btn-primary" @click="openQuestionModal">+ 新增试题</button>
      </div>

      <div v-for="type in questionTypes" :key="type" class="question-group">
        
        <div class="action-bar header-sm">
          <h3>{{ type }}题</h3>
        </div>

        <div class="table-card">
          <table class="data-table">
            <thead>
              <tr>
                <th width="80">题型</th>
                <th>题干内容</th>
                
                <th width="140">
                  <div class="th-content">
                    <span>参考答案</span>
                    <span 
                      v-if="['单选', '多选', '判断'].includes(type)" 
                      class="header-eye-btn"
                      :class="{ 'active': isAllVisible(type) }"
                      @click="toggleGroupVisibility(type)"
                      title="点击统一显示/隐藏本组答案"
                    >
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M3 14 C 6 6, 18 6, 21 14"></path>
                        <circle cx="12" cy="14" r="3"></circle>
                      </svg>
                    </span>
                  </div>
                </th>
                
                <th width="80">分值</th>
                <th width="80">难度</th>
                <th width="150">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="q in getQuestionsByType(type)" :key="q.id">
                <td><span class="tag-type">{{ q.type }}</span></td>
                <td class="text-left text-ellipsis" :title="q.stem">{{ q.stem }}</td>
                
                <td>
                  <div v-if="['单选', '多选', '判断'].includes(q.type)" class="answer-mask-box">
                    <span class="answer-text" :class="{ 'masked': !visibleAnswers[q.id] }">
                      {{ visibleAnswers[q.id] ? q.analysis : '******' }}
                    </span>
                    
                    <span 
                      class="eye-btn" 
                      :class="{ 'active': visibleAnswers[q.id] }"
                      @click="toggleAnswerVisibility(q.id)"
                    >
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M3 14 C 6 6, 18 6, 21 14"></path>
                        <circle cx="12" cy="14" r="3"></circle>
                      </svg>
                    </span>
                  </div>

                  <div v-else>
                    <button class="btn-text btn-view-detail" @click="viewAnswerDetail(q)">
                      📄 查看详情
                    </button>
                  </div>
                </td>

                <td>{{ q.score }}分</td>
                <td><span :class="getDifficultyClass(q.difficulty)">{{ q.difficulty }}</span></td>
                <td><button class="btn-text btn-danger" @click="deleteQuestion(q.id)">删除</button></td>
              </tr>
              <tr v-if="getQuestionsByType(type).length === 0">
                <td colspan="6" class="empty-row">暂无{{ type }}题数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div v-if="currentTab === 'paper'" class="tab-content fade-in">
      <div class="action-bar">
        <h3>试卷列表</h3>
        <button class="btn btn-primary" @click="openPaperModal">+ 组建新试卷</button>
      </div>
      <div class="paper-grid">
        <div class="paper-card" v-for="paper in papers" :key="paper.id">
          <div class="paper-icon">📄</div>
          <div class="paper-info">
            <h4>{{ paper.title }}</h4>
            <p>总分: {{ paper.totalScore }}分 | 题目数: {{ paper.questionIds.length }}</p>
          </div>
          <button class="btn-text btn-danger" @click="deletePaper(paper.id)">删除</button>
        </div>
        <div v-if="papers.length === 0" class="empty-tip">暂无试卷，请点击右上角创建</div>
      </div>
    </div>

    <div v-if="currentTab === 'exam'" class="tab-content fade-in">
      <div class="action-bar">
        <h3>考试场次</h3>
        <button class="btn btn-primary" @click="openExamModal">+ 发布考试</button>
      </div>
      <div class="table-card">
        <table class="data-table">
          <thead>
            <tr>
              <th>考试名称</th>
              <th>使用试卷</th>
              <th>开始时间</th>
              <th>时长</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="exam in exams" :key="exam.id">
              <td><strong>{{ exam.title }}</strong></td>
              <td>{{ getPaperTitle(exam.paperId) }}</td>
              <td>{{ exam.startTime }}</td>
              <td>{{ exam.duration }}分钟</td>
              <td>
                <span class="status-badge" :class="getExamStatusClass(exam)">{{ getExamStatusText(exam) }}</span>
              </td>
              <td><button class="btn-text btn-danger" @click="deleteExam(exam.id)">取消</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="currentTab === 'result'" class="tab-content fade-in">
      <div class="action-bar">
        <h3>阅卷工作台</h3>
        <div class="stat-select">
          <select v-model="selectedExamId">
            <option value="" disabled>请选择考试场次</option>
            <option v-for="e in exams" :key="e.id" :value="e.id">{{ e.title }}</option>
          </select>
        </div>
      </div>

      <div v-if="selectedExamId" class="result-dashboard">
        <div class="grading-task-section" v-if="pendingGradingList.length > 0">
          <div class="task-header">
            <h4>📝 待批阅主观题 ({{ pendingGradingList.length }})</h4>
            <span class="task-tip">包含文字或图片答案，请老师完成人工评分</span>
          </div>
          <div class="task-grid">
            <div class="task-card" v-for="sub in pendingGradingList" :key="sub.studentId">
              <div class="task-avatar">{{ sub.name[0] }}</div>
              <div class="task-info">
                <span class="student-name">{{ sub.name }}</span>
                <span class="student-id">{{ sub.studentId }}</span>
              </div>
              <button class="btn btn-primary btn-sm" @click="openGradingModal(sub)">开始批阅</button>
            </div>
          </div>
        </div>

        <div class="stats-panel">
          <div class="stat-box">
            <span class="label">参考人数</span>
            <span class="value">32</span>
          </div>
          <div class="stat-box">
            <span class="label">批改进度</span>
            <span class="value">{{ completedGradingCount }} / 32</span>
          </div>
          <button class="btn btn-success" @click="exportExcel">📥 导出最终成绩单</button>
        </div>

        <div class="action-bar header-sm"><h3>所有考生成绩</h3></div>
        <div class="table-card">
          <table class="data-table">
            <thead>
              <tr>
                <th>学号</th>
                <th>姓名</th>
                <th>客观题 (系统)</th>
                <th>主观题 (教师)</th>
                <th>总分</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="sub in filteredSubmissions" :key="sub.studentId">
                <td class="mono">{{ sub.studentId }}</td>
                <td>{{ sub.name }}</td>
                <td>{{ sub.objScore }}</td>
                <td>
                  <span v-if="sub.subjScore !== null" class="score-green">+{{ sub.subjScore }}</span>
                  <span v-else class="text-gray">-</span>
                </td>
                <td>
                  <strong v-if="sub.subjScore !== null" class="score-total">{{ calculateTotal(sub) }}</strong>
                  <span v-else class="text-gray">计算中...</span>
                </td>
                <td>
                  <span class="status-badge" :class="sub.subjScore !== null ? 'status-active' : 'status-gray'">
                    {{ sub.subjScore !== null ? '已完成' : '待批改' }}
                  </span>
                </td>
                <td>
                  <button class="btn-text btn-primary" @click="openGradingModal(sub)">
                    {{ sub.subjScore !== null ? '复查' : '批改' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div v-else class="empty-tip">请在上方选择一场考试以开始阅卷工作</div>
    </div>

    <div class="modal-mask" v-if="showQuestionModal">
      <div class="modal-box">
        <div class="modal-header"><h3>{{ qForm.id ? '编辑试题' : '新增试题' }}</h3><span class="close-btn" @click="closeQuestionModal">×</span></div>
        <div class="modal-body">
          <form @submit.prevent="saveQuestion">
            <div class="form-row">
              <div class="form-group">
                <label>题目类型</label>
                <select v-model="qForm.type">
                  <option value="单选">单选题</option>
                  <option value="多选">多选题</option>
                  <option value="判断">判断题</option>
                  <option value="简答">简答题 (主观)</option>
                </select>
              </div>
              <div class="form-group">
                <label>难度</label>
                <select v-model="qForm.difficulty">
                  <option value="低">低</option>
                  <option value="中">中</option>
                  <option value="高">高</option>
                </select>
              </div>
              <div class="form-group">
                <label>分值</label>
                <input type="number" v-model.number="qForm.score" min="1" required>
              </div>
            </div>
            <div class="form-group">
              <label>题干内容</label>
              <textarea v-model="qForm.stem" rows="3" required></textarea>
            </div>
            <div class="form-group" v-if="['单选','多选'].includes(qForm.type)">
              <label>选项 (以换行分隔 A/B/C/D...)</label>
              <textarea v-model="qForm.options" placeholder="A. 选项一&#10;B. 选项二" rows="4"></textarea>
            </div>
            <div class="form-group">
              <label>参考答案 / 解析</label>
              <textarea v-model="qForm.analysis" rows="2" placeholder="输入标准答案，用于批改参考"></textarea>
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeQuestionModal">取消</button>
              <button type="submit" class="btn btn-primary">保存</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showAnswerDetailModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>参考答案详情</h3>
          <span class="close-btn" @click="closeAnswerDetailModal">×</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>题目内容：</label>
            <div class="static-text">{{ currentDetailQuestion.stem }}</div>
          </div>
          <div class="form-group">
            <label>参考答案/解析：</label>
            <div class="static-text highlight-box">{{ currentDetailQuestion.analysis }}</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="closeAnswerDetailModal">关闭</button>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showPaperModal">
      <div class="modal-box wide-modal-xl">
        <div class="modal-header">
          <h3>组建新试卷</h3>
          <span class="close-btn" @click="closePaperModal">×</span>
        </div>
        
        <div class="modal-body paper-modal-body">
          <div class="form-group">
            <label>试卷标题 <span class="text-red">*</span></label>
            <input type="text" v-model="paperForm.title" placeholder="例：期中测试A卷" required>
          </div>
          
          <div class="form-group full-height-group">
            <label>
              勾选题目 
              (当前已选总分: <span class="score-green">{{ paperTotalScore }}</span> 分
              <span class="separator">|</span>
              共选中 <span class="score-blue">{{ paperForm.questionIds.length }}</span> 题)
            </label>
            
            <div class="question-selector-large">
              <div v-for="type in questionTypes" :key="type" class="selector-group">
                <div class="group-title">
                  {{ type }}题
                  <span class="small-count" v-if="getSelectedCountByType(type) > 0">
                    (已选 {{ getSelectedCountByType(type) }} 题)
                  </span>
                </div>
                
                <div v-if="getQuestionsByType(type).length === 0" class="empty-group">
                  暂无{{ type }}题
                </div>

                <div 
                  v-for="q in getQuestionsByType(type)" 
                  :key="q.id" 
                  class="q-item-row"
                  :class="{ 'selected': paperForm.questionIds.includes(q.id) }"
                >
                  <div class="check-col">
                    <input type="checkbox" :value="q.id" v-model="paperForm.questionIds">
                  </div>
                  <div class="content-col">
                    <span class="q-text">{{ q.stem }}</span>
                  </div>
                  <div class="info-col">
                    <span class="score-tag">{{ q.score }}分</span>
                    <span :class="getDifficultyClass(q.difficulty)">{{ q.difficulty }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closePaperModal">取消</button>
          <button class="btn btn-primary" @click="savePaper">完成组卷</button>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showExamModal">
      <div class="modal-box">
        <div class="modal-header"><h3>发布考试</h3><span class="close-btn" @click="closeExamModal">×</span></div>
        <div class="modal-body">
          <div class="form-group">
            <label>考试名称</label>
            <input type="text" v-model="examForm.title" placeholder="例：2025春季期末考" required>
          </div>
          <div class="form-group">
            <label>选择试卷</label>
            <select v-model="examForm.paperId" required>
              <option v-for="p in papers" :key="p.id" :value="p.id">
                {{ p.title }} (总分{{ p.totalScore }})
              </option>
            </select>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>开始时间</label>
              <input type="datetime-local" v-model="examForm.startTime" required>
            </div>
            <div class="form-group">
              <label>限时时长 (分钟)</label>
              <input type="number" v-model.number="examForm.duration" required>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeExamModal">取消</button>
            <button class="btn btn-primary" @click="saveExam">立即发布</button>
          </div>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showGradingModal">
      <div class="modal-box grading-modal">
        <div class="modal-header">
          <h3>人工评卷 (主观题)</h3>
          <span class="close-btn" @click="closeGradingModal">×</span>
        </div>
        <div class="modal-body">
          <div class="student-bar">
            <span>当前学生：<strong>{{ currentStudent.name }}</strong> ({{ currentStudent.studentId }})</span>
            <span class="tag-auto">客观题得分：{{ currentStudent.objScore }}</span>
          </div>

          <div class="question-review-card">
            <div class="q-title">
              <span class="tag-type">简答题</span> 
              简述软件工程的定义。（本题满分 10 分）
            </div>
            
            <div class="comparison-box">
              <div class="answer-block student">
                <p class="label">🧑‍🎓 学生作答：</p>
                <div v-if="currentStudent.answerContent" class="text-content">
                  {{ currentStudent.answerContent }}
                </div>
                <div v-if="currentStudent.answerImg" class="img-content">
                  <p class="img-hint">（学生上传了图片，点击可预览）</p>
                  <img 
                    :src="currentStudent.answerImg" 
                    alt="学生答案图片" 
                    class="student-img-preview"
                    @click="previewImage(currentStudent.answerImg)"
                  >
                </div>
                <div v-if="!currentStudent.answerContent && !currentStudent.answerImg" class="text-gray">
                  (学生未作答)
                </div>
              </div>

              <div class="answer-block ref">
                <p class="label">🔑 参考答案：</p>
                <div class="content text-green">软件工程是将系统化的、规范的、可度量的方法应用于软件的开发、运行和维护的过程，即将工程化应用于软件中。</div>
              </div>
            </div>

            <div class="grading-input-area">
              <label>请打分：</label>
              <input type="number" v-model.number="gradingScore" max="10" min="0" class="score-input-lg">
              <span class="suffix">/ 10 分</span>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeGradingModal">取消</button>
            <button class="btn btn-primary" @click="submitSubjScore">✅ 确认提交</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'ExamManage',
  data() {
    return {
      currentTab: 'bank', 
      questionTypes: ['单选', '多选', '判断', '简答'], 

      showQuestionModal: false,
      showAnswerDetailModal: false,
      currentDetailQuestion: {},
      visibleAnswers: {},

      questions: [
        { id: 1, type: '单选', stem: '软件生命周期中时间最长的阶段是？', score: 2, difficulty: '低', analysis: 'D. 维护阶段' },
        { id: 2, type: '判断', stem: '需求分析是软件定义时期的最后一个阶段。', score: 2, difficulty: '中', analysis: '正确' },
        { id: 3, type: '简答', stem: '简述软件工程的定义。', score: 10, difficulty: '高', analysis: '见参考答案' },
        { id: 4, type: '多选', stem: '软件危机的主要表现包括？', score: 4, difficulty: '中', analysis: 'AB' },
        { id: 5, type: '单选', stem: '瀑布模型本质上是一种？', score: 2, difficulty: '低', analysis: '线性顺序模型' },
        { id: 6, type: '判断', stem: '黑盒测试不需要了解内部代码结构。', score: 2, difficulty: '低', analysis: '正确' }
      ],
      qForm: { id: null, type: '单选', stem: '', score: 5, difficulty: '中', analysis: '', options: '' },

      showPaperModal: false,
      papers: [
        { id: 101, title: '第一章单元测试', totalScore: 14, questionIds: [1, 2, 3] }
      ],
      paperForm: { title: '', questionIds: [] },

      showExamModal: false,
      exams: [
        { id: 201, title: '2025期中考试', paperId: 101, startTime: '2025-11-10 09:00', duration: 90 }
      ],
      examForm: { title: '', paperId: '', startTime: '', duration: 90 },

      selectedExamId: '',
      showGradingModal: false,
      currentStudent: null,
      gradingScore: 0,
      
      mockSubmissions: [
        { studentId: 'S202301', name: '张三', objScore: 38, subjScore: null, answerContent: '软件工程是指导计算机软件开发和维护的一门工程学科。', answerImg: null }, 
        { studentId: 'S202302', name: '李四', objScore: 40, subjScore: 15, answerContent: '已完成批改。', answerImg: null },
        { studentId: 'S202303', name: '王五', objScore: 30, subjScore: null, answerContent: '老师，我的答案写在纸上了，请看图：', answerImg: 'https://via.placeholder.com/400x300?text=Student+Handwriting+Answer' },
        { studentId: 'S202304', name: '赵六', objScore: 42, subjScore: 18, answerContent: '略。', answerImg: null }
      ]
    }
  },
  computed: {
    pendingGradingList() {
      if (!this.selectedExamId) return [];
      return this.mockSubmissions.filter(s => s.subjScore === null);
    },
    completedGradingCount() {
      if (!this.selectedExamId) return 0;
      return this.mockSubmissions.filter(s => s.subjScore !== null).length;
    },
    filteredSubmissions() {
      return this.selectedExamId ? this.mockSubmissions : [];
    },
    paperTotalScore() {
      return this.questions
        .filter(q => this.paperForm.questionIds.includes(q.id))
        .reduce((sum, q) => sum + q.score, 0);
    }
  },
  methods: {
    getQuestionsByType(type) { return this.questions.filter(q => q.type === type); },
    // 修改点：新增获取该类型已选数量的方法
    getSelectedCountByType(type) {
      return this.questions.filter(q => q.type === type && this.paperForm.questionIds.includes(q.id)).length;
    },
    getDifficultyClass(d) { return { '低': 'text-green', '中': 'text-orange', '高': 'text-red' }[d]; },
    getPaperTitle(id) { const p = this.papers.find(x => x.id === id); return p ? p.title : '未知试卷'; },
    
    getExamStatusText(exam) {
      if (!exam.startTime) return '未开始';
      const now = new Date();
      const start = new Date(exam.startTime.replace(/-/g, '/'));
      const end = new Date(start.getTime() + exam.duration * 60000);
      if (now < start) return '未开始';
      if (now > end) return '已结束';
      return '进行中'; 
    },
    getExamStatusClass(exam) {
      const status = this.getExamStatusText(exam);
      if (status === '未开始') return 'status-gray';
      if (status === '进行中') return 'status-active';
      if (status === '已结束') return 'status-end';
      return '';
    },
    calculateTotal(sub) { return sub.objScore + (sub.subjScore || 0); },

    previewImage(url) { window.open(url, '_blank'); },

    isAllVisible(type) {
      const qs = this.getQuestionsByType(type);
      if (qs.length === 0) return false;
      return qs.every(q => this.visibleAnswers[q.id]);
    },
    toggleGroupVisibility(type) {
      const qs = this.getQuestionsByType(type);
      const targetState = !this.isAllVisible(type);
      qs.forEach(q => {
        this.$set(this.visibleAnswers, q.id, targetState);
      });
    },
    toggleAnswerVisibility(id) {
      this.$set(this.visibleAnswers, id, !this.visibleAnswers[id]);
    },
    
    viewAnswerDetail(q) {
      this.currentDetailQuestion = q;
      this.showAnswerDetailModal = true;
    },
    closeAnswerDetailModal() {
      this.showAnswerDetailModal = false;
      this.currentDetailQuestion = {};
    },

    openQuestionModal() { this.qForm = { id: Date.now(), type: '单选', stem: '', score: 2, difficulty: '中', analysis: '', options: '' }; this.showQuestionModal = true; },
    closeQuestionModal() { this.showQuestionModal = false; },
    saveQuestion() { this.questions.push(this.qForm); this.closeQuestionModal(); },
    deleteQuestion(id) { this.questions = this.questions.filter(q => q.id !== id); },

    openPaperModal() { this.paperForm = { title: '', questionIds: [] }; this.showPaperModal = true; },
    closePaperModal() { this.showPaperModal = false; },
    savePaper() {
      if (this.paperForm.questionIds.length === 0) return alert('请至少选择一道题目');
      const newPaper = { id: Date.now(), title: this.paperForm.title, questionIds: this.paperForm.questionIds, totalScore: this.paperTotalScore };
      this.papers.push(newPaper);
      this.closePaperModal();
    },
    deletePaper(id) { this.papers = this.papers.filter(p => p.id !== id); },

    openExamModal() { this.examForm = { title: '', paperId: '', startTime: '', duration: 90 }; this.showExamModal = true; },
    closeExamModal() { this.showExamModal = false; },
    saveExam() { this.exams.push({ id: Date.now(), ...this.examForm }); this.closeExamModal(); },
    deleteExam(id) { this.exams = this.exams.filter(e => e.id !== id); },

    openGradingModal(sub) {
      this.currentStudent = sub;
      this.gradingScore = sub.subjScore || 0;
      this.showGradingModal = true;
    },
    closeGradingModal() { this.showGradingModal = false; },
    submitSubjScore() {
      this.currentStudent.subjScore = this.gradingScore;
      this.closeGradingModal();
      if (this.pendingGradingList.length === 0) {
        setTimeout(() => alert('🎉 所有试卷批改完成！'), 500);
      }
    },
    exportExcel() { alert('正在导出成绩单 Excel...'); }
  }
}
</script>

<style scoped>
/* 保持原有基础布局 (通用样式部分省略重复，直接使用你现有的) */
.manage-container { padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; height: 100%; display: flex; flex-direction: column; }
.nav-tabs { display: flex; background: #fff; padding: 0 20px; border-bottom: 1px solid #e4e7ed; gap: 30px; }
.tab-item { padding: 15px 5px; cursor: pointer; font-size: 15px; color: #606266; font-weight: 500; border-bottom: 3px solid transparent; transition: all 0.3s; }
.tab-item:hover { color: #1890ff; }
.tab-item.active { color: #1890ff; border-bottom-color: #1890ff; }
.tab-content { padding: 20px 0; flex: 1; display: flex; flex-direction: column; animation: fade 0.3s; }
.action-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.action-bar h3 { margin: 0; border-left: 4px solid #1890ff; padding-left: 10px; font-size: 18px; color: #333; }
.header-sm { margin-top: 20px; margin-bottom: 10px !important; }
.header-sm h3 { font-size: 16px; border-left-width: 3px; }

/* 题库分类区块 */
.question-group { margin-bottom: 30px; }
.top-actions { text-align: right; margin-bottom: 10px; }
.table-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #fafafa; padding: 15px; color: #606266; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 15px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.text-left { text-align: left; }
.text-ellipsis { max-width: 300px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; }
.tag-type { background: #f0f5ff; color: #2f54eb; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
.empty-row { text-align: center; color: #ccc; padding: 20px !important; }

/* 答案交互 */
.th-content { display: flex; align-items: center; gap: 8px; }
.answer-mask-box { display: flex; align-items: center; gap: 10px; font-family: monospace; }
.answer-text.masked { letter-spacing: 2px; color: #ccc; }
.header-eye-btn, .eye-btn { cursor: pointer; color: #bbb; display: inline-flex; align-items: center; justify-content: center; padding: 4px; border-radius: 50%; transition: all 0.2s; }
.header-eye-btn:hover, .eye-btn:hover { background-color: #f0f7ff; color: #1890ff; }
.header-eye-btn.active, .eye-btn.active { color: #555; }
.btn-view-detail { color: #1890ff; font-weight: 500; }
.btn-view-detail:hover { text-decoration: underline; }

/* 详情文本 */
.static-text { background: #f5f7fa; padding: 10px; border-radius: 4px; border: 1px solid #e4e7ed; line-height: 1.6; color: #333; }
.highlight-box { background: #f6ffed; border-color: #b7eb8f; color: #333; }

/* 通用颜色/状态 */
.text-green { color: #52c41a; } .text-orange { color: #fa8c16; } .text-red { color: #f5222d; } .text-gray { color: #ccc; }
.status-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; border: 1px solid #ddd; }
.status-gray { background: #f4f4f5; color: #909399; border-color: #e9e9eb; }
.status-active { background: #e6f7ff; color: #1890ff; border-color: #91d5ff; }
.status-end { background: #fdf6ec; color: #e6a23c; border-color: #faecd8; }

/* 试卷 Grid */
.paper-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 20px; }
.paper-card { background: #fff; padding: 20px; border-radius: 8px; border: 1px solid #ebeef5; display: flex; align-items: center; transition: all 0.3s; position: relative; }
.paper-card:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); border-color: #c6e2ff; }
.paper-icon { font-size: 30px; margin-right: 15px; opacity: 0.8; }
.paper-info h4 { margin: 0 0 5px; font-size: 16px; color: #333; }
.paper-info p { margin: 0; font-size: 12px; color: #999; }
.paper-card .btn-danger { position: absolute; right: 15px; top: 15px; font-size: 12px; }

/* 阅卷面板 */
.result-dashboard { display: flex; flex-direction: column; gap: 20px; }
.stats-panel { display: flex; gap: 20px; align-items: center; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.stat-box { display: flex; flex-direction: column; align-items: center; padding: 0 30px; border-right: 1px solid #eee; }
.stat-box:last-child { border-right: none; }
.stat-box .label { font-size: 13px; color: #909399; }
.stat-box .value { font-size: 24px; font-weight: bold; color: #333; margin-top: 5px; }
.stat-select select { padding: 8px; border-radius: 4px; border: 1px solid #dcdfe6; min-width: 200px; }

/* 批改任务 */
.grading-task-section { background: #fff; padding: 20px; border-radius: 8px; border: 1px solid #e6f7ff; box-shadow: 0 2px 12px rgba(24, 144, 255, 0.05); }
.task-header { display: flex; align-items: baseline; gap: 10px; margin-bottom: 15px; }
.task-header h4 { margin: 0; color: #333; }
.task-tip { font-size: 12px; color: #999; }
.task-grid { display: flex; gap: 15px; overflow-x: auto; padding-bottom: 5px; }
.task-card { min-width: 180px; background: #fafafa; border: 1px solid #eee; padding: 15px; border-radius: 6px; display: flex; flex-direction: column; align-items: center; gap: 10px; }
.task-avatar { width: 40px; height: 40px; background: #87d068; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; }
.task-info { text-align: center; display: flex; flex-direction: column; }
.student-name { font-weight: bold; font-size: 14px; }
.student-id { font-size: 12px; color: #999; }

.score-green { color: #52c41a; font-weight: bold; }
.score-blue { color: #1890ff; font-weight: bold; margin: 0 3px; }
.score-total { font-size: 16px; color: #333; }
.mono { font-family: monospace; }
.separator { color: #ddd; margin: 0 8px; font-weight: normal; }

/* 弹窗通用 */
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; width: 550px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: fade 0.3s; display: flex; flex-direction: column; max-height: 85vh; }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 16px; }
.close-btn { font-size: 20px; cursor: pointer; color: #999; }
.modal-body { padding: 20px; overflow-y: auto; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }

/* ---------------------- 重点修改：大号试卷组建弹窗样式 ---------------------- */
.wide-modal-xl { 
  width: 1000px; 
  height: 85vh; 
  max-height: 900px;
  display: flex;
  flex-direction: column;
}

.paper-modal-body {
  flex: 1;
  overflow-y: hidden; 
  display: flex;
  flex-direction: column;
}

.full-height-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0; 
}

.question-selector-large {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  flex: 1; 
  overflow-y: auto; 
  padding: 10px 15px;
  background-color: #fafafa;
}

.selector-group { margin-bottom: 20px; }
.group-title {
  font-weight: bold;
  color: #1890ff;
  border-bottom: 2px solid #e6f7ff;
  padding-bottom: 5px;
  margin-bottom: 10px;
  font-size: 14px;
}
.small-count {
  font-size: 12px;
  color: #666;
  font-weight: normal;
  margin-left: 8px;
}

.q-item-row {
  display: flex;
  align-items: flex-start;
  padding: 10px;
  background: white;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  margin-bottom: 8px;
  transition: all 0.2s;
  cursor: pointer;
}
.q-item-row:hover { background-color: #f0f9ff; border-color: #b3e19d; }
.q-item-row.selected { background-color: #e6f7ff; border-color: #1890ff; }

/* 三列布局 */
.check-col { width: 30px; display: flex; align-items: center; padding-top: 2px; }
.content-col { flex: 1; font-size: 13px; color: #333; line-height: 1.5; padding-right: 15px; }
.info-col { width: 100px; display: flex; flex-direction: column; align-items: flex-end; gap: 4px; font-size: 12px; }

.q-text { display: block; word-break: break-all; }
.score-tag { background: #f4f4f5; padding: 1px 5px; border-radius: 3px; color: #666; }
.empty-group { font-size: 12px; color: #999; padding-left: 10px; font-style: italic; margin-bottom: 10px; }

/* 批改弹窗特殊样式 */
.grading-modal { width: 700px; } 
.student-bar { background: #f0f7ff; padding: 10px 15px; border-radius: 4px; margin-bottom: 15px; display: flex; justify-content: space-between; font-size: 14px; border: 1px solid #bae7ff; }
.tag-auto { color: #1890ff; font-weight: bold; }
.question-review-card { border: 1px solid #eee; border-radius: 6px; padding: 15px; }
.q-title { font-weight: bold; margin-bottom: 15px; line-height: 1.5; }
.comparison-box { display: flex; flex-direction: column; gap: 15px; margin-bottom: 20px; }
.answer-block .label { margin: 0 0 5px 0; font-size: 12px; color: #666; font-weight: bold; }
.answer-block .content { padding: 10px; border-radius: 4px; font-size: 14px; line-height: 1.6; }
.answer-block.student .content { background: #f5f7fa; color: #333; border: 1px solid #e4e7ed; }
.answer-block.ref .content { background: #f6ffed; border: 1px solid #b7eb8f; }
.img-content { padding: 10px; border: 1px dashed #dcdfe6; border-radius: 4px; background: #fafafa; }
.img-hint { font-size: 12px; color: #999; margin-bottom: 5px; }
.student-img-preview { max-width: 100%; max-height: 200px; border-radius: 4px; border: 1px solid #eee; cursor: zoom-in; transition: transform 0.2s; }
.student-img-preview:hover { transform: scale(1.02); }
.text-content { margin-bottom: 10px; }
.grading-input-area { display: flex; align-items: center; justify-content: flex-end; gap: 10px; padding-top: 15px; border-top: 1px dashed #eee; }
.score-input-lg { width: 80px !important; font-size: 18px; font-weight: bold; text-align: center; color: #1890ff; border: 2px solid #1890ff !important; }
.suffix { font-size: 14px; color: #666; }

/* 表单元素基础 (保留) */
.form-group { margin-bottom: 15px; }
.form-row { display: flex; gap: 15px; }
.form-group label { display: block; margin-bottom: 5px; font-weight: 500; font-size: 13px; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 8px; border: 1px solid #dcdfe6; border-radius: 4px; box-sizing: border-box; }

/* 按钮通用 */
.btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; }
.btn-primary { background: #1890ff; color: white; }
.btn-secondary { background: white; border: 1px solid #dcdfe6; color: #606266; }
.btn-success { background: #52c41a; color: white; }
.btn-text { background: none; border: none; cursor: pointer; font-size: 13px; padding: 0 5px; }
.btn-danger { color: #f5222d; }
.empty-tip { text-align: center; color: #999; padding: 40px; }
@keyframes fade { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
.fade-in { animation: fade 0.3s; }
</style>