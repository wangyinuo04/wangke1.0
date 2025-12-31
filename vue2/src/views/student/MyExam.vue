<template>
  <div class="manage-container">
    
    <div class="nav-tabs">
      <div class="tab-item" :class="{ active: currentTab === 'all' }" @click="currentTab = 'all'">
        📋 全部考试
      </div>
      <div class="tab-item" :class="{ active: currentTab === 'todo' }" @click="currentTab = 'todo'">
        🔥 待参加
        <span v-if="todoCount > 0" class="badge-count">{{ todoCount }}</span>
      </div>
      <div class="tab-item" :class="{ active: currentTab === 'done' }" @click="currentTab = 'done'">
        🏁 已结束
      </div>
    </div>

    <div class="action-bar-simple">
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索考试名称或课程..."
          @keyup.enter="handleSearch"
        >
        <button class="btn-search" @click="handleSearch">🔍</button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th width="150">所属课程</th>
            <th>考试名称</th>
            <th width="260">考试时间</th>
            <th width="100">时长</th>
            <th width="100">状态</th>
            <th width="80">成绩</th>
            <th width="140">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="exam in filteredExams" :key="exam.id">
            <td class="course-col">{{ exam.courseName }}</td>
            <td class="title-col">
              {{ exam.title }}
              <span v-if="getExamState(exam) === 'ing'" class="tag-live">进行中</span>
            </td>
            <td class="time-col">
              <div>起：{{ exam.startTime }}</div>
              <div>止：{{ exam.endTime }}</div>
            </td>
            <td>{{ exam.duration }} 分钟</td>
            <td>
              <span class="status-badge" :class="getStatusClass(exam)">
                {{ getStatusText(exam) }}
              </span>
            </td>
            <td>
              <span v-if="exam.isSubmitted && exam.totalScore !== null" class="score-text">{{ exam.totalScore }}</span>
              <span v-else class="text-gray">-</span>
            </td>
            <td>
              <div class="action-col">
                <button 
                  v-if="!exam.isSubmitted"
                  class="btn-op" 
                  :class="getActionBtnClass(exam)"
                  :disabled="getExamState(exam) !== 'ing'"
                  @click="enterExam(exam)"
                >
                  {{ getActionBtnText(exam) }}
                </button>

                <button 
                  v-else
                  class="btn-op op-text"
                  @click="viewResult(exam)"
                >
                  查看成绩
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="filteredExams.length === 0">
            <td colspan="7" class="empty-state">暂无相关考试安排</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="modal-mask" v-if="isTakingExam">
      <div class="modal-box fullscreen-modal">
        <div class="exam-header">
          <div class="exam-info">
            <h3>{{ currentExam.title }}</h3>
            <span class="sub-info">考生：{{ studentName }} | 总分：100分</span>
          </div>
          <div class="timer-box" :class="{ 'urgent': remainingSeconds < 300 }">
            <span class="icon">⏰</span>
            <span class="time-text">{{ formatTime(remainingSeconds) }}</span>
          </div>
        </div>

        <div class="exam-body">
          <div class="question-list">
            <div v-for="(q, index) in examQuestions" :key="q.id" class="exam-question-card">
              <div class="q-header">
                <span class="q-no">{{ index + 1 }}</span>
                <span class="q-type">[{{ q.type }}]</span>
                <span class="q-score">({{ q.score }}分)</span>
              </div>
              <div class="q-stem">{{ q.stem }}</div>
              
              <div class="q-options">
                <div v-if="q.type === '单选'" class="options-group">
                  <label v-for="opt in q.options" :key="opt.key" class="radio-label">
                    <input type="radio" :name="'q'+q.id" :value="opt.key" v-model="answers[q.id]">
                    <span class="opt-key">{{ opt.key }}.</span>
                    <span class="opt-val">{{ opt.val }}</span>
                  </label>
                </div>
                <div v-if="q.type === '多选'" class="options-group">
                  <label v-for="opt in q.options" :key="opt.key" class="checkbox-label">
                    <input type="checkbox" :value="opt.key" v-model="answers[q.id]">
                    <span class="opt-key">{{ opt.key }}.</span>
                    <span class="opt-val">{{ opt.val }}</span>
                  </label>
                </div>
                <div v-if="q.type === '判断'" class="options-group horizontal">
                  <label class="radio-label"><input type="radio" :name="'q'+q.id" value="正确" v-model="answers[q.id]"> 正确</label>
                  <label class="radio-label"><input type="radio" :name="'q'+q.id" value="错误" v-model="answers[q.id]"> 错误</label>
                </div>
                <div v-if="q.type === '简答'" class="text-answer">
                  <textarea v-model="answers[q.id]" rows="4" placeholder="请输入您的答案..."></textarea>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="exam-footer">
          <div class="progress-info">已完成 {{ answeredCount }} / {{ examQuestions.length }} 题</div>
          <button class="btn btn-primary btn-lg" @click="submitExam(false)">交 卷</button>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showResultModal">
      <div class="modal-box wide-modal">
        <div class="modal-header">
          <h3>考试成绩单 - {{ currentExam.title }}</h3>
          <span class="close-btn" @click="closeResultModal">×</span>
        </div>
        
        <div class="modal-body">
          <div class="score-card-panel">
            <div class="score-main">
              <div class="label">总成绩</div>
              <div class="number">{{ currentExam.totalScore }}</div>
            </div>
            <div class="score-detail">
              <div class="detail-item">
                <span>客观题</span>
                <strong>{{ currentExam.objScore }}</strong>
              </div>
              <div class="detail-item">
                <span>主观题</span>
                <strong>{{ currentExam.subjScore || '待批改' }}</strong>
              </div>
            </div>
          </div>

          <div v-if="currentExam.showAnswers" class="analysis-section">
            <h4>📝 错题解析</h4>
            <div class="question-analysis-list">
              <div v-for="(q, idx) in mockExamResultDetail" :key="q.id" class="analysis-item">
                <div class="q-header-sm">
                  <span class="tag-sm">{{ q.type }}</span>
                  <span class="text-sm">{{ idx + 1 }}. {{ q.stem }}</span>
                  <span class="score-badge" :class="q.isCorrect ? 'bg-green' : 'bg-red'">
                    {{ q.isCorrect ? '得分: '+q.score : '得分: 0' }}
                  </span>
                </div>
                <div class="analysis-box">
                  <p><strong>我的答案：</strong><span :class="q.isCorrect ? 'text-green' : 'text-red'">{{ q.myAnswer }}</span></p>
                  <p><strong>正确答案：</strong><span class="text-green">{{ q.correctAnswer }}</span></p>
                  <p class="analysis-text"><strong>解析：</strong>{{ q.analysis }}</p>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-analysis">
            🚧 教师暂未公布试题解析
          </div>
        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeResultModal">关闭</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'MyExam',
  data() {
    return {
      currentTab: 'all', // all, todo, done
      searchQuery: '',
      studentName: '张三', // 模拟当前登录学生
      
      // 状态控制
      isTakingExam: false,
      showResultModal: false,
      
      // 考试数据
      allExams: [
        { 
          id: 1, 
          courseName: '软件工程导论', 
          title: '第一章单元测试', 
          startTime: '2025-10-10 09:00', 
          endTime: '2025-10-10 11:00', 
          duration: 60, // 分钟
          isSubmitted: true, 
          totalScore: 88, 
          objScore: 58, 
          subjScore: 30, 
          showAnswers: true 
        },
        { 
          id: 2, 
          courseName: 'Web前端开发', 
          title: '期中考试', 
          // 模拟一个正在进行的考试时间
          startTime: '2023-01-01 00:00', 
          endTime: '2025-12-31 23:59', 
          duration: 90, 
          isSubmitted: false, 
          totalScore: null, 
          objScore: null, 
          subjScore: null, 
          showAnswers: false 
        },
        { 
          id: 3, 
          courseName: '数据库原理', 
          title: 'SQL 专项测验', 
          startTime: '2025-11-01 14:00', 
          endTime: '2025-11-01 16:00', 
          duration: 45, 
          isSubmitted: false, 
          totalScore: null, 
          objScore: null, 
          subjScore: null, 
          showAnswers: false 
        }
      ],

      currentExam: {},
      
      // 答题相关
      examQuestions: [], // 题目列表
      answers: {}, // 答案存储 {qid: val}
      timer: null,
      remainingSeconds: 0,

      // 模拟试卷题目（参加考试时加载）
      mockPaper: [
        { id: 101, type: '单选', score: 5, stem: 'Vue实例挂载的各种生命周期钩子中，最早触发的是？', options: [{key:'A',val:'created'},{key:'B',val:'beforeCreate'},{key:'C',val:'mounted'}] },
        { id: 102, type: '多选', score: 10, stem: '以下哪些是 HTML5 新增的标签？', options: [{key:'A',val:'header'},{key:'B',val:'div'},{key:'C',val:'footer'},{key:'D',val:'span'}] },
        { id: 103, type: '判断', score: 5, stem: 'v-if 和 v-show 的作用完全相同。', options: [] },
        { id: 104, type: '简答', score: 20, stem: '简述 MVVM 模式的原理。', options: [] }
      ],

      // 模拟错题解析（查看成绩时加载）
      mockExamResultDetail: [
        { id: 101, type: '单选', score: 5, stem: '软件生命周期最长的阶段？', myAnswer: 'C', correctAnswer: 'D', isCorrect: false, analysis: '维护阶段是时间最长的。' },
        { id: 103, type: '判断', score: 5, stem: '需求分析是最后阶段？', myAnswer: '错误', correctAnswer: '错误', isCorrect: true, analysis: '需求分析是定义时期的最后阶段，不是整个周期的最后。' }
      ]
    }
  },
  computed: {
    filteredExams() {
      let list = this.allExams;
      if (this.currentTab === 'todo') {
        list = list.filter(e => !e.isSubmitted);
      } else if (this.currentTab === 'done') {
        list = list.filter(e => e.isSubmitted);
      }
      if (this.searchQuery) {
        const q = this.searchQuery.toLowerCase();
        list = list.filter(e => e.title.includes(q) || e.courseName.includes(q));
      }
      return list;
    },
    todoCount() {
      return this.allExams.filter(e => !e.isSubmitted).length;
    },
    answeredCount() {
      // 简单判断非空
      return Object.values(this.answers).filter(v => {
        if (Array.isArray(v)) return v.length > 0;
        return v !== null && v !== '';
      }).length;
    }
  },
  methods: {
    handleSearch() { console.log('Searching...'); },

    // 辅助状态判断
    getExamState(exam) {
      const now = new Date();
      const start = new Date(exam.startTime);
      const end = new Date(exam.endTime);
      
      if (exam.isSubmitted) return 'done'; // 已交卷
      if (now < start) return 'future'; // 未开始
      if (now > end) return 'ended'; // 已过期
      return 'ing'; // 进行中
    },
    getStatusText(exam) {
      const state = this.getExamState(exam);
      const map = {
        'future': '未开始',
        'ing': '进行中',
        'ended': '已缺考',
        'done': '已交卷'
      };
      return map[state];
    },
    getStatusClass(exam) {
      const state = this.getExamState(exam);
      const map = {
        'future': 'status-gray',
        'ing': 'status-active',
        'ended': 'status-red',
        'done': 'status-green'
      };
      return map[state];
    },
    getActionBtnClass(exam) {
      const state = this.getExamState(exam);
      if (state === 'ing') return 'op-primary'; // 蓝色实心
      return 'op-disabled'; // 灰色禁用
    },
    getActionBtnText(exam) {
      const state = this.getExamState(exam);
      if (state === 'future') return '未开放';
      if (state === 'ing') return '进入考试';
      if (state === 'ended') return '已结束';
      return '操作';
    },

    // --- 3.5.1 参加考试 ---
    enterExam(exam) {
      this.currentExam = exam;
      // 模拟加载试题
      this.examQuestions = JSON.parse(JSON.stringify(this.mockPaper));
      // 初始化答案
      this.answers = {};
      this.examQuestions.forEach(q => {
        this.$set(this.answers, q.id, q.type === '多选' ? [] : '');
      });
      
      // 开始倒计时
      this.remainingSeconds = exam.duration * 60;
      this.isTakingExam = true;
      this.startTimer();
    },
    startTimer() {
      if (this.timer) clearInterval(this.timer);
      this.timer = setInterval(() => {
        if (this.remainingSeconds > 0) {
          this.remainingSeconds--;
        } else {
          this.submitExam(true); // 自动提交
        }
      }, 1000);
    },
    formatTime(seconds) {
      const h = Math.floor(seconds / 3600);
      const m = Math.floor((seconds % 3600) / 60);
      const s = seconds % 60;
      const pad = (n) => n.toString().padStart(2, '0');
      return `${pad(h)}:${pad(m)}:${pad(s)}`;
    },
    submitExam(isAuto) {
      clearInterval(this.timer);
      const msg = isAuto ? '考试时间到，系统已自动交卷！' : '确认提交试卷吗？交卷后无法修改。';
      
      if (isAuto || confirm(msg)) {
        this.isTakingExam = false;
        // 更新状态
        const target = this.allExams.find(e => e.id === this.currentExam.id);
        if (target) {
          target.isSubmitted = true;
          // 模拟立即出分（仅客观题）
          target.totalScore = 65; // 模拟分
          target.objScore = 65;
          target.subjScore = null;
        }
        if (!isAuto) alert('交卷成功！请等待老师批改主观题。');
        else alert('时间到，已自动交卷。');
      } else {
        // 如果取消提交，恢复定时器（如果是手动点的）
        if (!isAuto) this.startTimer();
      }
    },

    // --- 3.5.2 查看成绩 ---
    viewResult(exam) {
      this.currentExam = exam;
      this.showResultModal = true;
    },
    closeResultModal() {
      this.showResultModal = false;
    }
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer);
  }
}
</script>

<style scoped>
/* 全局样式复用 */
.manage-container { padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background: #f5f7fa; min-height: 100vh; }

/* Tab & Search */
.nav-tabs { display: flex; background: #fff; padding: 0 30px; border-bottom: 1px solid #e4e7ed; margin-bottom: 20px; }
.tab-item { padding: 15px 20px; cursor: pointer; font-size: 15px; color: #606266; border-bottom: 3px solid transparent; position: relative; transition: all 0.3s; }
.tab-item:hover { color: #1890ff; }
.tab-item.active { color: #1890ff; border-bottom-color: #1890ff; font-weight: 600; }
.badge-count { background: #ff4d4f; color: white; font-size: 12px; padding: 0 6px; border-radius: 10px; margin-left: 5px; position: absolute; top: 8px; right: 5px; }

.action-bar-simple { padding: 0 30px; margin-bottom: 15px; display: flex; justify-content: flex-end; }
.search-box { display: flex; }
.search-box input { padding: 8px 12px; border: 1px solid #dcdfe6; border-right: none; border-radius: 4px 0 0 4px; width: 250px; outline: none; }
.search-box input:focus { border-color: #1890ff; }
.btn-search { border: 1px solid #dcdfe6; border-left: none; background: #f5f7fa; border-radius: 0 4px 4px 0; cursor: pointer; width: 40px; }
.btn-search:hover { background: #e6f7ff; color: #1890ff; }

/* 表格 */
.table-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; margin: 0 30px; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #fafafa; padding: 15px; color: #606266; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 15px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.data-table tr:hover { background-color: #f9f9f9; }

.course-col { font-weight: 500; color: #333; }
.title-col { color: #303133; font-weight: 500; }
.tag-live { background: #e6f7ff; color: #1890ff; font-size: 12px; padding: 1px 4px; border-radius: 2px; margin-left: 5px; border: 1px solid #91d5ff; }
.time-col { font-family: monospace; font-size: 12px; color: #909399; line-height: 1.4; }
.score-text { font-weight: bold; color: #1890ff; font-size: 16px; }

/* 状态徽标 */
.status-badge { padding: 3px 8px; border-radius: 12px; font-size: 12px; }
.status-gray { background: #f4f4f5; color: #909399; }
.status-active { background: #e6f7ff; color: #1890ff; }
.status-red { background: #fff1f0; color: #f5222d; }
.status-green { background: #f0f9eb; color: #67c23a; }

/* 操作按钮 */
.action-col { display: flex; gap: 8px; align-items: center; }
.btn-op { border: none; cursor: pointer; font-size: 13px; padding: 5px 12px; border-radius: 4px; transition: all 0.2s; background: transparent; }
.op-primary { background-color: #1890ff; color: white; }
.op-primary:hover { background-color: #40a9ff; }
.op-text { color: #1890ff; padding: 0; }
.op-text:hover { text-decoration: underline; }
.op-disabled { color: #ccc; cursor: not-allowed; background: #f5f5f5; }

.empty-state { text-align: center; padding: 40px; color: #999; }

/* ---------------- 考试全屏弹窗 ---------------- */
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 2000; display: flex; justify-content: center; align-items: center; }
.fullscreen-modal { width: 95%; height: 95%; background: #f7f9fc; border-radius: 8px; display: flex; flex-direction: column; overflow: hidden; box-shadow: 0 0 20px rgba(0,0,0,0.2); }

.exam-header { background: #fff; padding: 15px 30px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 4px rgba(0,0,0,0.02); }
.exam-info h3 { margin: 0 0 5px; color: #333; }
.sub-info { font-size: 14px; color: #666; }
.timer-box { font-size: 24px; font-family: monospace; font-weight: bold; color: #333; background: #f0f2f5; padding: 5px 15px; border-radius: 4px; display: flex; align-items: center; gap: 10px; }
.timer-box.urgent { color: #f5222d; background: #fff1f0; animation: pulse 1s infinite; }

.exam-body { flex: 1; overflow-y: auto; padding: 30px; display: flex; justify-content: center; }
.question-list { width: 800px; max-width: 100%; }
.exam-question-card { background: #fff; padding: 25px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.03); }
.q-header { margin-bottom: 15px; border-bottom: 1px solid #f0f0f0; padding-bottom: 10px; }
.q-no { font-size: 18px; font-weight: bold; color: #1890ff; margin-right: 10px; }
.q-type { background: #e6f7ff; color: #1890ff; font-size: 12px; padding: 2px 6px; border-radius: 4px; }
.q-score { float: right; color: #999; font-size: 13px; }
.q-stem { font-size: 16px; color: #333; line-height: 1.6; margin-bottom: 20px; font-weight: 500; }

.options-group { display: flex; flex-direction: column; gap: 12px; }
.options-group.horizontal { flex-direction: row; gap: 30px; }
.radio-label, .checkbox-label { display: flex; align-items: center; padding: 10px; border: 1px solid #ebeef5; border-radius: 6px; cursor: pointer; transition: all 0.2s; }
.radio-label:hover, .checkbox-label:hover { background: #f9f9f9; border-color: #c6e2ff; }
.radio-label input, .checkbox-label input { margin-right: 10px; transform: scale(1.2); }
.opt-key { font-weight: bold; margin-right: 8px; color: #666; }
.text-answer textarea { width: 100%; border: 1px solid #dcdfe6; border-radius: 4px; padding: 10px; font-size: 14px; resize: vertical; }

.exam-footer { background: #fff; padding: 15px 30px; border-top: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.progress-info { color: #666; }
.btn-lg { padding: 10px 40px; font-size: 16px; border-radius: 20px; }

/* ---------------- 成绩详情弹窗 ---------------- */
.wide-modal { background: white; width: 600px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: fade 0.3s; display: flex; flex-direction: column; max-height: 85vh; }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 18px; }
.close-btn { font-size: 24px; cursor: pointer; color: #999; }
.modal-body { padding: 20px; overflow-y: auto; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; }

.score-card-panel { display: flex; background: #f0f9eb; padding: 20px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #e1f3d8; }
.score-main { flex: 1; text-align: center; border-right: 1px solid #d1e7c4; }
.score-main .number { font-size: 40px; font-weight: bold; color: #67c23a; line-height: 1.2; }
.score-detail { flex: 2; display: flex; justify-content: space-around; align-items: center; }
.detail-item span { display: block; font-size: 12px; color: #666; margin-bottom: 5px; }
.detail-item strong { font-size: 20px; color: #333; }

.analysis-item { border-bottom: 1px dashed #eee; padding-bottom: 15px; margin-bottom: 15px; }
.q-header-sm { margin-bottom: 8px; font-size: 14px; }
.tag-sm { background: #f0f2f5; color: #909399; font-size: 12px; padding: 1px 4px; border-radius: 2px; margin-right: 5px; }
.score-badge { font-size: 12px; color: white; padding: 1px 6px; border-radius: 4px; float: right; }
.bg-green { background: #67c23a; }
.bg-red { background: #f5222d; }
.analysis-box p { margin: 4px 0; font-size: 13px; }
.text-green { color: #67c23a; font-weight: bold; }
.text-red { color: #f5222d; font-weight: bold; }
.analysis-text { color: #666; background: #f9f9f9; padding: 8px; border-radius: 4px; margin-top: 8px; }
.no-analysis { text-align: center; color: #999; padding: 20px; font-style: italic; }

.btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; transition: all 0.3s; }
.btn-primary { background: #1890ff; color: white; }
.btn-primary:hover { background: #40a9ff; }
.btn-secondary { background: white; border: 1px solid #dcdfe6; color: #606266; }

@keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }
@keyframes fade { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
</style>