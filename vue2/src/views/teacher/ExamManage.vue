<template>
  <div class="manage-container">

    <div class="nav-tabs">
      <div class="tab-item" :class="{ active: currentTab === 'bank' }" @click="currentTab = 'bank'">
        📖 题库管理
      </div>
      <div class="tab-item" :class="{ active: currentTab === 'paper' }" @click="currentTab = 'paper'">
        📝 试卷组卷
      </div>
      <div class="tab-item" :class="{ active: currentTab === 'exam' }" @click="currentTab = 'exam'">
        ⏰ 考试安排
      </div>
      <div class="tab-item" :class="{ active: currentTab === 'result' }" @click="currentTab = 'result'">
        📊 阅卷与统计
      </div>
    </div>

    <div v-if="currentTab === 'bank'" class="tab-content fade-in">
      <div class="top-actions">
        <div class="filter-row">
          <div class="filter-item">
            <label>课程：</label>
            <select v-model="selectedCourseId" @change="fetchQuestionsByCourse">
              <option value="">全部课程</option>
              <option v-for="course in teacherCourses" :key="course.courseId" :value="course.courseId">
                {{ course.courseName }}
              </option>
            </select>
          </div>
        </div>
        <button class="btn btn-primary" @click="openQuestionModal()">+ 新增试题</button>
      </div>

      <div v-for="type in questionTypes" :key="type" class="question-group">
        <div class="action-bar header-sm">
          <h3>{{ type }}题 ({{ getQuestionsByType(type).length }})</h3>
        </div>

        <div class="table-card">
          <table class="data-table">
            <thead>
              <tr>
                <th width="80">题型</th>
                <th>题干内容</th>
                <th width="140">参考答案</th>
                <th width="80">分值</th>
                <th width="80">难度</th>
                <th width="150">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="q in getQuestionsByType(type)" :key="q.questionId">
                <td><span class="tag-type">{{ q.questionType }}</span></td>
                <td class="text-left text-ellipsis" :title="q.content">{{ q.content }}</td>
                <td>
                  <div class="answer-mask-box">
                    <span class="answer-text text-ellipsis" :title="q.correctAnswer">
                      {{ q.correctAnswer }}
                    </span>
                  </div>
                </td>
                <td>{{ q.score }}分</td>
                <td><span :class="getDifficultyClass(q.difficulty)">{{ q.difficulty }}</span></td>
                <td>
                  <button class="btn-text btn-primary" @click="editQuestion(q)">编辑</button>
                  <button class="btn-text btn-danger" @click="deleteQuestion(q.questionId)">删除</button>
                </td>
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
        <button class="btn btn-primary" @click="openPaperModal()">+ 组建新试卷</button>
      </div>

      <div v-if="groupedPapers && Object.keys(groupedPapers).length > 0">
        <div v-for="(papers, courseId) in groupedPapers" :key="courseId" class="course-section">
          <h4>{{ getCourseName(courseId) }} ({{ papers.length }} 份试卷)</h4>
          <div class="paper-grid">
            <div v-for="p in papers" :key="p.paperId" class="paper-card">
              <div class="paper-icon">📄</div>
              <div class="paper-info">
                <h4>{{ p.paperTitle }}</h4>
                <p>总分: {{ p.totalScore }}分 | 状态: <span :class="getPaperStatusClass(p.paperStatus)">{{ p.paperStatus }}</span></p>
                <p>创建时间: {{ formatDateTime(p.createdTime) }}</p>
              </div>
              <div class="paper-actions">
                <button class="btn-text btn-danger" @click="deletePaper(p.paperId)">删除</button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-tip">
        <p>暂无试卷，请点击右上角创建</p>
      </div>
    </div>

    <div v-if="currentTab === 'exam'" class="tab-content fade-in">
      <div class="top-actions">
        <h3>考试安排 ({{ exams.length }})</h3>
        <div class="right-btns">
          <button class="btn btn-primary" @click="openExamModal()">+ 安排考试</button>
        </div>
      </div>

      <div v-if="exams.length > 0" class="table-card">
        <table class="data-table">
          <thead>
            <tr>
              <th>考试名称</th>
              <th>教学班</th>
              <th>使用试卷</th>
              <th>开始时间</th>
              <th>时长</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="exam in exams" :key="exam.examId">
              <td><strong>{{ exam.examName }}</strong></td>
              <td>{{ getTeachingClassName(exam.classId) }}</td>
              <td>{{ getPaperName(exam.paperId) }}</td>
              <td>{{ formatDateTime(exam.startTime) }}</td>
              <td>{{ exam.timeLimit }}分钟</td>
              <td>
                <span class="status-badge" :class="getExamStatusClass(exam)">
                  {{ getExamStatusText(exam) }}
                </span>
              </td>
              <td>
                <button class="btn-text btn-danger" @click="deleteExam(exam.examId)">取消</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-else class="empty-tip">
        <p>暂无考试安排，请点击右上角新增</p>
      </div>
    </div>

    <div v-if="currentTab === 'result'" class="tab-content fade-in">
      <div class="action-bar">
        <h3>阅卷与统计</h3>
        <div class="right-btns">
          <div class="stat-select">
            <select v-model="selectedExamId" @change="loadExamSubmissions">
              <option value="" disabled>请选择考试场次</option>
              <option v-for="e in exams" :key="e.examId" :value="e.examId">{{ e.examName }}</option>
            </select>
          </div>
        </div>
      </div>

      <div v-if="selectedExamId && examStats" class="stats-panel">
        <div class="stat-box">
          <span class="label">参考人数</span>
          <span class="value">{{ examStats.totalCount || 0 }}</span>
        </div>
        <div class="stat-box">
          <span class="label">批改进度</span>
          <span class="value">{{ examStats.gradedCount || 0 }} / {{ examStats.totalCount || 0 }}</span>
        </div>
        <div class="stat-box">
          <span class="label">平均分</span>
          <span class="value">{{ examStats.avgScore || 0 }}</span>
        </div>
      </div>

      <div v-if="selectedExamId && pendingGradingList.length > 0" class="grading-task-section">
        <div class="task-header">
          <h4>📝 待批阅主观题 ({{ pendingGradingList.length }})</h4>
          <span class="task-tip">点击学生卡片开始批阅</span>
        </div>
        <div class="task-grid">
          <div class="task-card" v-for="sub in pendingGradingList" :key="sub.id">
            <div class="task-info">
              <span class="student-name">{{ sub.studentName || '未知学生' }}</span>
              <span class="student-id">{{ sub.studentId }}</span>
            </div>
            <button class="btn btn-primary btn-sm" @click="openGradingModal(sub)">开始批阅</button>
          </div>
        </div>
      </div>

      <div v-if="selectedExamId && examSubmissions.length > 0">
        <div class="action-bar header-sm">
          <h3>所有考生成绩</h3>
        </div>
        <div class="table-card">
          <table class="data-table">
            <thead>
              <tr>
                <th width="120">学号</th>
                <th width="100">姓名</th>
                <th width="100">客观题得分</th>
                <th width="100">主观题得分</th>
                <th width="100">总分</th>
                <th width="100">状态</th>
                <th width="150">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="sub in examSubmissions" :key="sub.id">
                <td class="mono">{{ sub.studentId }}</td>
                <td>{{ sub.studentName || '未知' }}</td>
                <td>{{ sub.objectiveScore || 0 }}</td>
                <td>
                  <span v-if="sub.subjectiveScore !== null" class="score-green">{{ sub.subjectiveScore }}</span>
                  <span v-else>-</span>
                </td>
                <td>
                  <strong v-if="sub.totalScore !== null" class="score-total">{{ sub.totalScore }}</strong>
                  <span v-else>-</span>
                </td>
                <td>
                  <span class="status-badge" :class="getSubmissionStatusClass(sub)">
                    {{ sub.examStatus || '未知' }}
                  </span>
                </td>
                <td>
                  <button class="btn-text btn-primary" @click="openGradingModal(sub)">
                    {{ (sub.subjectiveScore !== null) ? '复查' : '批改' }}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-else-if="!selectedExamId" class="empty-tip">
        <p>请在上方选择一场考试以开始阅卷工作</p>
      </div>
    </div>

    <div class="modal-mask" v-if="showQuestionModal">
      <div class="modal-box wide-modal">
        <div class="modal-header">
          <h3>{{ qForm.questionId ? '编辑试题' : '新增试题' }}</h3>
          <span class="close-btn" @click="closeQuestionModal">×</span>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveQuestion">
            <div class="form-row">
              <div class="form-group">
                <label>题目类型 <span class="text-red">*</span></label>
                <select v-model="qForm.questionType" @change="handleQuestionTypeChange">
                  <option value="单选">单选题</option>
                  <option value="多选">多选题</option>
                  <option value="判断">判断题</option>
                  <option value="简答">简答题 (主观)</option>
                </select>
              </div>
              <div class="form-group">
                <label>难度 <span class="text-red">*</span></label>
                <select v-model="qForm.difficulty">
                  <option value="低">低</option>
                  <option value="中">中</option>
                  <option value="高">高</option>
                </select>
              </div>
              <div class="form-group">
                <label>分值 <span class="text-red">*</span></label>
                <input type="number" v-model.number="qForm.score" min="1" max="100" required>
              </div>
            </div>

            <div class="form-group">
              <label>题干内容 <span class="text-red">*</span></label>
              <textarea v-model="qForm.content" rows="3" required placeholder="请输入题干内容..."></textarea>
            </div>

            <div v-if="['单选', '多选'].includes(qForm.questionType)" class="form-group">
              <label>
                选项设置 <span class="text-red">*</span>
                <button type="button" class="btn-add-option" @click="addChoiceOption">+ 新增选项</button>
              </label>
              <div class="options-container">
                <div v-for="(option, index) in choiceOptions" :key="index" class="option-item">
                  <span class="option-label">{{ getOptionLetter(index) }}.</span>
                  <input type="text" v-model="choiceOptions[index]" :placeholder="`请输入选项${getOptionLetter(index)}的内容`" class="option-input">
                  <button v-if="choiceOptions.length > 2" type="button" class="btn-remove-option" @click="removeChoiceOption(index)">×</button>
                </div>
              </div>
              <div class="form-group">
                <label>正确答案 <span class="text-red">*</span> <small class="text-gray">(点击字母选择)</small></label>
                <div class="answer-options">
                  <div v-for="(option, index) in choiceOptions" :key="index" class="answer-option"
                    :class="{ 'selected': isLetterSelected(getOptionLetter(index)) }"
                    @click="toggleChoiceAnswer(getOptionLetter(index))">
                    {{ getOptionLetter(index) }}
                  </div>
                </div>
              </div>
            </div>

            <div v-if="qForm.questionType === '判断'" class="form-group">
              <label>正确答案 <span class="text-red">*</span></label>
              <div class="answer-options">
                <div class="answer-option" :class="{ 'selected': judgeAnswer === 'A' }" @click="judgeAnswer = 'A'">A (正确)</div>
                <div class="answer-option" :class="{ 'selected': judgeAnswer === 'B' }" @click="judgeAnswer = 'B'">B (错误)</div>
              </div>
            </div>

            <div class="form-group">
              <label>题目解析</label>
              <textarea v-model="qForm.explanation" rows="2" placeholder="输入题目解析，用于讲解和说明"></textarea>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeQuestionModal">取消</button>
              <button type="submit" class="btn btn-primary">保存</button>
            </div>
          </form>
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
          <div class="form-row">
            <div class="form-group">
              <label>试卷标题 <span class="text-red">*</span></label>
              <input type="text" v-model="paperForm.paperTitle" placeholder="例：期中测试A卷" required>
            </div>
            <div class="form-group">
              <label>所属课程 <span class="text-red">*</span></label>
              <select v-model="paperForm.courseId" required>
                <option value="">请选择课程</option>
                <option v-for="course in teacherCourses" :key="course.courseId" :value="course.courseId">
                  {{ course.courseName }}
                </option>
              </select>
            </div>
          </div>

          <div class="full-height-group">
            <label>
              勾选题目 (当前已选总分: <span class="score-green">{{ paperTotalScore }}</span> 分)
            </label>
            <div class="question-selector-large">
              <div v-for="type in questionTypes" :key="type" class="selector-group">
                <div class="group-title">{{ type }}题</div>
                <div v-if="getQuestionsByType(type).length === 0" class="empty-group">暂无{{ type }}题</div>
                <div v-else>
                  <div v-for="q in getQuestionsByType(type)" :key="q.questionId" class="q-item-row"
                    :class="{ 'selected': paperForm.questionIds.includes(q.questionId) }">
                    <div class="check-col">
                      <input type="checkbox" :value="q.questionId" v-model="paperForm.questionIds">
                    </div>
                    <div class="content-col">
                      <span class="q-text">{{ q.content }}</span>
                    </div>
                    <div class="info-col">
                      <span class="score-tag">{{ q.score }}分</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closePaperModal">取消</button>
          <button type="button" class="btn btn-primary" @click="savePaper"
            :disabled="!paperForm.paperTitle || paperForm.questionIds.length === 0">
            完成组卷
          </button>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showExamModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>发布考试</h3>
          <span class="close-btn" @click="closeExamModal">×</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>考试名称 <span class="text-red">*</span></label>
            <input type="text" v-model="examForm.examName" placeholder="例：2025春季期末考" required>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>选择教学班 <span class="text-red">*</span></label>
              <select v-model="examForm.classId" required>
                <option value="">请选择教学班</option>
                <option v-for="tc in teachingClasses" :key="tc.classId" :value="tc.classId">
                  {{ tc.className }} ({{ tc.courseName }})
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>选择试卷 <span class="text-red">*</span></label>
              <select v-model="examForm.paperId" required>
                <option value="">请选择试卷</option>
                <option v-for="p in filteredPapers" :key="p.paperId" :value="p.paperId">
                  {{ p.paperTitle }} (总分{{ p.totalScore }})
                </option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>开始时间 <span class="text-red">*</span></label>
              <input type="datetime-local" v-model="examForm.startTime" required>
            </div>
            <div class="form-group">
              <label>限时时长 (分钟) <span class="text-red">*</span></label>
              <input type="number" v-model.number="examForm.timeLimit" min="1" max="300" required>
            </div>
          </div>
          <div class="form-group">
            <label>是否显示答案</label>
            <label class="checkbox-label">
              <input type="checkbox" v-model="examForm.showAnswers"> 考试结束后向学生显示答案
            </label>
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
            <span>当前学生：<strong>{{ currentGrading.studentName }}</strong> ({{ currentGrading.studentId }})</span>
            <span class="tag-auto">客观题得分：{{ currentGrading.objectiveScore || 0 }}</span>
          </div>

          <div class="question-review-card">
            <div class="q-title"><span class="tag-type">主观题</span> 简答题答案批改</div>
            <div class="comparison-box">
              <div class="answer-block student">
                <p class="label">🧑‍🎓 学生作答：</p>
                <div class="content">{{ formatSubjectiveAnswer(currentGrading.subjectiveAnswers) }}</div>
              </div>
            </div>
            <div class="grading-input-area">
              <label>打分：</label>
              <input type="number" v-model.number="gradingScore" class="score-input-lg"> 分
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeGradingModal">取消</button>
            <button class="btn btn-primary" @click="submitGradingScore">✅ 确认提交</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
// 导入 API
import {
  getQuestionList, addQuestion, updateQuestion, deleteQuestion, getQuestionsByCourse,
  getExamList, addExam, deleteExam,
  getExamSubmissions, getPendingGrading, getExamStats, submitSubjectiveScore
} from '@/api/exam'

import { addPaper, deletePaper, getPapersGroupedByCourse } from '@/api/paper'
import { getTeacherCourses, getTeachingClassesByTeacher } from '@/api/teacher'

export default {
  name: 'ExamManage',
  data() {
    return {
      currentTab: 'bank',
      questionTypes: ['单选', '多选', '判断', '简答'],
      
      // 用户信息
      currentTeacher: null,
      selectedCourseId: '',
      
      // 数据列表
      teacherCourses: [],
      questions: [],
      groupedPapers: {},
      exams: [],
      teachingClasses: [],

      // 阅卷相关
      selectedExamId: '',
      examSubmissions: [],
      pendingGradingList: [],
      examStats: null,
      
      // 批改状态
      showGradingModal: false,
      currentGrading: {
        studentId: '',
        examId: '',
        studentName: '',
        objectiveScore: 0,
        subjectiveAnswers: '',
        subjectiveScore: null
      },
      gradingScore: 0,

      // 表单控制
      showQuestionModal: false,
      showPaperModal: false,
      showExamModal: false,

      // 试题表单
      qForm: {
        questionId: null,
        questionType: '单选',
        content: '',
        options: '',
        correctAnswer: '',
        explanation: '',
        score: 5,
        difficulty: '中',
        courseId: ''
      },
      choiceOptions: ['', '', '', ''],
      selectedLetters: [],
      judgeAnswer: '',

      // 试卷表单
      paperForm: {
        paperTitle: '',
        courseId: '',
        questionIds: [],
        totalScore: 0
      },

      // 考试表单
      examForm: {
        examName: '',
        classId: '',
        paperId: '',
        startTime: '',
        timeLimit: 90,
        showAnswers: false
      }
    }
  },

  computed: {
    getQuestionsByType() {
      return (type) => this.questions.filter(q => q.questionType === type);
    },
    getOptionLetter() {
      return (index) => String.fromCharCode(65 + index);
    },
    isLetterSelected() {
      return (letter) => this.selectedLetters.includes(letter);
    },
    paperTotalScore() {
      return this.questions
        .filter(q => this.paperForm.questionIds.includes(q.questionId))
        .reduce((sum, q) => sum + q.score, 0);
    },
    filteredPapers() {
      if (!this.examForm.classId) return []
      const classInfo = this.teachingClasses.find(tc => tc.classId === this.examForm.classId)
      if (!classInfo) return []
      return this.groupedPapers[classInfo.courseId] || []
    }
  },

  watch: {
    currentTab(newVal) {
      if (newVal === 'bank') this.fetchQuestions();
      if (newVal === 'paper') this.fetchPapers();
      if (newVal === 'exam') this.fetchExams();
      if (newVal === 'result') {
        this.selectedExamId = '';
        this.examSubmissions = [];
        this.pendingGradingList = [];
        this.examStats = null;
      }
    }
  },

  methods: {
    // --- 1. 数据加载 ---
    async fetchInitialData() {
      if (!this.currentTeacher) return;
      
      const courseRes = await getTeacherCourses(this.currentTeacher.teacherId);
      if (courseRes.success) {
        this.teacherCourses = courseRes.data || [];
        if (this.teacherCourses.length > 0) this.selectedCourseId = this.teacherCourses[0].courseId;
      }

      await this.fetchTeachingClasses();
      await this.fetchQuestions();
      await this.fetchPapers();
      await this.fetchExams();
    },

    async fetchTeachingClasses() {
      if (!this.currentTeacher) return;
      const res = await getTeachingClassesByTeacher(this.currentTeacher.teacherId);
      if (res.success) {
        this.teachingClasses = res.data.map(c => ({
          classId: c.id || c.classId,
          className: c.className,
          courseId: this.getCourseIdByClassName(c.courseName),
          courseName: c.courseName
        }));
      }
    },

    getCourseIdByClassName(courseName) {
      const course = this.teacherCourses.find(c => c.courseName === courseName);
      return course ? course.courseId : '';
    },

    async fetchQuestions() {
      if (!this.currentTeacher) return;
      try {
        let res = this.selectedCourseId 
          ? await getQuestionsByCourse(this.selectedCourseId)
          : await getQuestionList('', this.currentTeacher.teacherId);
        if (res.success) this.questions = [...(res.data || [])];
      } catch (e) {
        console.error(e);
      }
    },

    async fetchQuestionsByCourse() { await this.fetchQuestions(); },

    async fetchPapers() {
      if (!this.currentTeacher) return;
      const res = await getPapersGroupedByCourse(this.currentTeacher.teacherId);
      if (res.success) this.groupedPapers = res.data || {};
    },

    async fetchExams() {
      if (!this.currentTeacher) return;
      const res = await getExamList(this.currentTeacher.teacherId);
      if (res.success) this.exams = res.data || [];
    },

    // --- 2. 试题操作 ---
    openQuestionModal(question = null) {
      if (question) {
        this.qForm = { ...question };
        if (['单选', '多选'].includes(question.questionType)) {
          this.choiceOptions = this.parseOptions(question.options);
          this.selectedLetters = question.correctAnswer.split('');
        } else if (question.questionType === '判断') {
          this.judgeAnswer = question.correctAnswer === '正确' ? 'A' : 'B';
        }
      } else {
        this.resetQuestionForm();
      }
      this.showQuestionModal = true;
    },
    
    closeQuestionModal() { this.showQuestionModal = false; },
    
    resetQuestionForm() {
      this.qForm = {
        questionId: null,
        questionType: '单选',
        content: '',
        options: '',
        correctAnswer: '',
        explanation: '',
        score: 5,
        difficulty: '中',
        courseId: this.selectedCourseId
      };
      this.choiceOptions = ['', '', '', ''];
      this.selectedLetters = [];
      this.judgeAnswer = '';
    },

    handleQuestionTypeChange() {
      if (['单选', '多选'].includes(this.qForm.questionType)) {
        this.choiceOptions = ['', '', '', ''];
        this.selectedLetters = [];
      } else if (this.qForm.questionType === '判断') {
        this.judgeAnswer = 'A';
      }
    },

    addChoiceOption() {
      if (this.choiceOptions.length < 8) this.choiceOptions.push('');
      else this.$message.warning('最多8个选项');
    },

    removeChoiceOption(index) {
      if (this.choiceOptions.length > 2) {
        this.choiceOptions.splice(index, 1);
        const letter = this.getOptionLetter(index);
        this.selectedLetters = this.selectedLetters.filter(l => l !== letter);
      }
    },

    toggleChoiceAnswer(letter) {
      if (this.qForm.questionType === '单选') {
        this.selectedLetters = [letter];
      } else {
        const index = this.selectedLetters.indexOf(letter);
        if (index === -1) this.selectedLetters.push(letter);
        else this.selectedLetters.splice(index, 1);
      }
    },

    async saveQuestion() {
      if (!this.qForm.content.trim()) return this.$message.error('请输入题干');
      
      if (['单选', '多选'].includes(this.qForm.questionType)) {
        if (this.selectedLetters.length === 0) return this.$message.error('请选择答案');
        this.qForm.options = this.choiceOptions.map((o, i) => `${this.getOptionLetter(i)}. ${o}`).join('\n');
        this.qForm.correctAnswer = this.selectedLetters.join('');
      } else if (this.qForm.questionType === '判断') {
        this.qForm.correctAnswer = this.judgeAnswer === 'A' ? '正确' : '错误';
        this.qForm.options = 'A. 正确\nB. 错误';
      }

      if (!this.qForm.courseId) this.qForm.courseId = this.selectedCourseId;

      try {
        const res = this.qForm.questionId ? await updateQuestion(this.qForm) : await addQuestion(this.qForm);
        if (res.success) {
          this.$message.success('保存成功');
          this.closeQuestionModal();
          this.fetchQuestions();
        }
      } catch (e) { this.$message.error('保存失败'); }
    },

    editQuestion(q) { this.openQuestionModal(q); },
    
    async deleteQuestion(id) {
      if (!confirm('确定删除?')) return;
      const res = await deleteQuestion(id);
      if (res.success) {
        this.$message.success('删除成功');
        this.fetchQuestions();
      }
    },

    // --- 3. 试卷操作 ---
    openPaperModal() {
      if (!this.currentTeacher) return this.$message.error('请登录');
      this.paperForm = {
        paperTitle: '',
        courseId: this.selectedCourseId || (this.teacherCourses[0]?.courseId || ''),
        questionIds: [],
        totalScore: 0
      };
      this.showPaperModal = true;
    },
    closePaperModal() { this.showPaperModal = false; },

    async savePaper() {
      this.paperForm.totalScore = this.paperTotalScore;
      const res = await addPaper(this.paperForm);
      if (res.success) {
        this.$message.success('组卷成功');
        this.closePaperModal();
        this.fetchPapers();
      }
    },

    async deletePaper(id) {
      if (!confirm('确定删除?')) return;
      const res = await deletePaper(id);
      if (res.success) {
        this.$message.success('删除成功');
        this.fetchPapers();
      }
    },

    // --- 4. 考试操作 ---
    openExamModal() {
      this.examForm = {
        examName: '',
        classId: '',
        paperId: '',
        startTime: '',
        timeLimit: 90,
        showAnswers: false
      };
      this.showExamModal = true;
    },
    closeExamModal() { this.showExamModal = false; },

    async saveExam() {
      // 复制表单数据
      const examData = { ...this.examForm };
      
      // ✅ 修改点 1: 保持 ISO 格式 (带 T)，只补全秒数
      // 浏览器 datetime-local 控件的值格式为 "yyyy-MM-ddTHH:mm"
      // 后端默认 LocalDateTime 解析需要 "yyyy-MM-ddTHH:mm:ss"
      if (examData.startTime && examData.startTime.length === 16) {
         examData.startTime = examData.startTime + ':00';
      }
      
      // ✅ 修改点 2: 确保 timeLimit 是数字类型
      examData.timeLimit = parseInt(examData.timeLimit);

      // 发送请求
      const res = await addExam(examData);
      if (res.success) {
        this.$message.success('发布成功');
        this.closeExamModal();
        this.fetchExams();
      }
    },

    async deleteExam(id) {
      if (!confirm('确定取消?')) return;
      const res = await deleteExam(id);
      if (res.success) {
        this.$message.success('已取消');
        this.fetchExams();
      }
    },

    // --- 5. 阅卷操作 ---
    async loadExamSubmissions() {
      if (!this.selectedExamId) return;
      
      const [subRes, pendingRes, statsRes] = await Promise.all([
        getExamSubmissions(this.selectedExamId, this.currentTeacher.teacherId),
        getPendingGrading(this.selectedExamId),
        getExamStats(this.selectedExamId)
      ]);

      if (subRes.success) this.examSubmissions = subRes.data || [];
      if (pendingRes.success) this.pendingGradingList = pendingRes.data || [];
      if (statsRes.success) this.examStats = statsRes.data || {};
    },

    openGradingModal(sub) {
      this.currentGrading = {
        studentId: sub.studentId,
        examId: sub.examId || this.selectedExamId,
        studentName: sub.studentName,
        objectiveScore: sub.objectiveScore,
        subjectiveAnswers: sub.subjectiveAnswers,
        subjectiveScore: sub.subjectiveScore
      };
      this.gradingScore = sub.subjectiveScore || 0;
      this.showGradingModal = true;
    },
    closeGradingModal() { this.showGradingModal = false; },

    async submitGradingScore() {
      const res = await submitSubjectiveScore(
        this.currentGrading.studentId,
        this.currentGrading.examId,
        this.gradingScore
      );
      if (res.success) {
        this.$message.success('评分成功');
        this.closeGradingModal();
        this.loadExamSubmissions();
      } else {
        this.$message.error('评分失败');
      }
    },

    // --- 辅助工具 ---
    getCourseName(id) { return this.teacherCourses.find(c => c.courseId === id)?.courseName || '未知'; },
    getPaperName(id) { 
      for(let cid in this.groupedPapers) {
        const p = this.groupedPapers[cid].find(x => x.paperId === id);
        if(p) return p.paperTitle;
      }
      return '未知试卷';
    },
    getTeachingClassName(id) { return this.teachingClasses.find(t => t.classId === id)?.className || '未知班级'; },
    
    getPaperStatusClass(status) { return status === '已发布' ? 'status-published' : 'status-draft'; },
    getExamStatusClass(exam) {
      const now = new Date();
      const start = new Date(exam.startTime);
      return now < start ? 'status-draft' : 'status-published';
    },
    getExamStatusText(exam) {
      const now = new Date();
      const start = new Date(exam.startTime);
      return now < start ? '未开始' : '进行中/已结束';
    },
    getDifficultyClass(d) { return d === '高' ? 'text-red' : (d === '中' ? 'text-orange' : 'text-green'); },
    getSubmissionStatusClass(sub) { return sub.examStatus === '已批改' ? 'status-published' : 'status-draft'; },
    
    formatDateTime(str) { return str ? str.replace('T', ' ').substring(0, 16) : '-'; },
    parseOptions(str) {
      if(!str) return ['', '', '', ''];
      return str.split('\n').map(l => { const m = l.match(/^[A-Z]\.\s*(.+)/); return m ? m[1] : l; });
    },
    formatSubjectiveAnswer(jsonStr) {
      try {
        const obj = JSON.parse(jsonStr);
        if(typeof obj === 'string') return obj;
        // 如果是对象 {Q_01: "答案"}，取值展示
        return Object.values(obj).join('\n\n') || '未作答';
      } catch(e) {
        return jsonStr || '未作答';
      }
    }
  },

  mounted() {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (userInfo.role === 'teacher') {
      this.currentTeacher = userInfo;
      this.fetchInitialData();
    }
  }
}
</script>

<style scoped>
/* 基础容器 */
.manage-container { padding: 0; background: #f5f7fa; min-height: 100vh; display: flex; flex-direction: column; }

/* Tabs */
.nav-tabs { display: flex; background: #fff; padding: 0 20px; border-bottom: 1px solid #e4e7ed; gap: 30px; }
.tab-item { padding: 15px 5px; cursor: pointer; font-size: 15px; color: #606266; border-bottom: 3px solid transparent; transition: all 0.3s; }
.tab-item.active { color: #1890ff; border-bottom-color: #1890ff; font-weight: 600; }
.tab-content { padding: 20px; flex: 1; overflow-y: auto; }

/* 顶部操作栏 */
.top-actions, .action-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.header-sm { margin-top: 20px; margin-bottom: 10px; border-left: 3px solid #1890ff; padding-left: 10px; }
.header-sm h3 { margin: 0; font-size: 16px; }

/* 表格通用 */
.table-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #fafafa; padding: 12px 15px; color: #606266; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 12px 15px; border-bottom: 1px solid #ebeef5; color: #333; font-size: 14px; }
.empty-row, .empty-tip { text-align: center; color: #999; padding: 30px; }

/* 按钮通用 */
.btn-primary { background: #1890ff; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; }
.btn-primary:hover { background: #40a9ff; }
.btn-secondary { background: white; border: 1px solid #dcdfe6; padding: 8px 16px; border-radius: 4px; cursor: pointer; }
.btn-text { background: none; border: none; cursor: pointer; font-size: 13px; margin-right: 5px; }
.btn-text.btn-primary { color: #1890ff; }
.btn-text.btn-danger { color: #f5222d; }

/* 状态标签 */
.tag-type { background: #e6f7ff; color: #1890ff; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
.status-badge { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.status-draft { background: #f4f4f5; color: #909399; }
.status-published { background: #f6ffed; color: #52c41a; }
.text-green { color: #52c41a; }
.text-red { color: #f5222d; }
.text-orange { color: #e6a23c; }

/* 试卷列表 */
.course-section { margin-bottom: 20px; background: #fff; padding: 15px; border-radius: 8px; }
.paper-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 15px; margin-top: 10px; }
.paper-card { border: 1px solid #eee; padding: 15px; border-radius: 6px; display: flex; align-items: center; position: relative; transition: all 0.3s; }
.paper-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.paper-icon { font-size: 24px; margin-right: 10px; }
.paper-info h4 { margin: 0 0 5px 0; font-size: 15px; }
.paper-info p { margin: 0; font-size: 12px; color: #999; }
.paper-actions { position: absolute; right: 10px; top: 10px; }

/* 阅卷面板 */
.stats-panel { display: flex; gap: 20px; background: #fff; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
.stat-box { flex: 1; text-align: center; border-right: 1px solid #eee; }
.stat-box:last-child { border: 0; }
.stat-box .value { font-size: 20px; font-weight: bold; display: block; margin-top: 5px; }

.grading-task-section { background: #fff; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
.task-grid { display: flex; gap: 15px; overflow-x: auto; padding-bottom: 5px; }
.task-card { min-width: 160px; border: 1px solid #eee; padding: 15px; border-radius: 6px; text-align: center; background: #fafafa; }
.task-card .student-name { font-weight: bold; display: block; margin-bottom: 5px; }
.task-card .btn { margin-top: 10px; font-size: 12px; padding: 4px 10px; }

/* 弹窗样式 */
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; width: 600px; max-height: 85vh; display: flex; flex-direction: column; border-radius: 8px; }
.wide-modal { width: 700px; }
.wide-modal-xl { width: 900px; }
.grading-modal { width: 800px; }
.modal-header { padding: 15px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-body { padding: 20px; overflow-y: auto; }
.modal-footer { padding: 15px; border-top: 1px solid #eee; text-align: right; }
.close-btn { cursor: pointer; font-size: 20px; }

/* 表单元素 */
.form-row { display: flex; gap: 20px; margin-bottom: 15px; }
.form-group { margin-bottom: 15px; flex: 1; }
.form-group label { display: block; margin-bottom: 5px; font-weight: 500; }
.form-group input, .form-group select, .form-group textarea { width: 100%; padding: 8px; border: 1px solid #dcdfe6; border-radius: 4px; box-sizing: border-box; }

/* 组卷选择器 */
.question-selector-large { height: 400px; overflow-y: auto; border: 1px solid #eee; padding: 10px; margin-top: 10px; }
.q-item-row { display: flex; padding: 8px; border-bottom: 1px solid #eee; align-items: center; }
.q-item-row:hover { background: #f5f7fa; }
.check-col { margin-right: 10px; }
.content-col { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.score-green { color: #52c41a; font-weight: bold; }

/* 批改详情 */
.student-bar { background: #e6f7ff; padding: 10px; border-radius: 4px; margin-bottom: 15px; display: flex; justify-content: space-between; }
.question-review-card { border: 1px solid #eee; padding: 15px; border-radius: 6px; }
.answer-block.student { background: #f9f9f9; padding: 10px; border-radius: 4px; margin-top: 10px; }
.grading-input-area { margin-top: 15px; text-align: right; border-top: 1px dashed #eee; padding-top: 15px; }
.score-input-lg { width: 80px; font-size: 18px; text-align: center; border: 2px solid #1890ff; padding: 5px; border-radius: 4px; color: #1890ff; font-weight: bold; }

/* 选项管理 */
.options-container { margin-bottom: 10px; }
.option-item { display: flex; align-items: center; margin-bottom: 8px; }
.option-label { width: 30px; font-weight: bold; }
.btn-remove-option { color: red; cursor: pointer; margin-left: 5px; border: none; background: none; font-size: 16px; }
.answer-options { display: flex; gap: 10px; }
.answer-option { width: 30px; height: 30px; border: 1px solid #ddd; display: flex; align-items: center; justify-content: center; cursor: pointer; border-radius: 4px; }
.answer-option.selected { background: #1890ff; color: white; border-color: #1890ff; }
</style>