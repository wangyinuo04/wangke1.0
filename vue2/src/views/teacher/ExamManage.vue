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

    <!-- 题库管理模块 -->
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
                <th width="140">
                  <div class="th-content">
                    <span>参考答案</span>
                    <span v-if="['单选', '多选', '判断'].includes(type)" class="header-eye-btn"
                      :class="{ 'active': isAllVisible(type) }" @click="toggleGroupVisibility(type)">
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                        stroke-linecap="round" stroke-linejoin="round">
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
              <tr v-for="q in getQuestionsByType(type)" :key="q.questionId">
                <td><span class="tag-type">{{ q.questionType }}</span></td>
                <td class="text-left text-ellipsis" :title="q.content">{{ q.content }}</td>
                <td>
                  <div v-if="['单选', '多选', '判断'].includes(q.questionType)" class="answer-mask-box">
                    <span class="answer-text" :class="{ 'masked': !visibleAnswers[q.questionId] }">
                      {{ visibleAnswers[q.questionId] ? q.correctAnswer : '******' }}
                    </span>
                    <span class="eye-btn" :class="{ 'active': visibleAnswers[q.questionId] }"
                      @click="toggleAnswerVisibility(q.questionId)">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                        stroke-linecap="round" stroke-linejoin="round">
                        <path d="M3 14 C 6 6, 18 6, 21 14"></path>
                        <circle cx="12" cy="14" r="3"></circle>
                      </svg>
                    </span>
                  </div>
                  <div v-else>
                    <button class="btn-text btn-view-detail" @click="viewAnswerDetail(q)">📄 查看详情</button>
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

    <!-- 试卷组卷模块 -->
    <div v-if="currentTab === 'paper'" class="tab-content fade-in">
      <div class="action-bar">
        <h3>试卷列表</h3>
        <button class="btn btn-primary" @click="openPaperModal()">+ 组建新试卷</button>
      </div>

      <!-- 按课程分组的试卷列表 -->
      <div v-if="groupedPapers && Object.keys(groupedPapers).length > 0">
        <div v-for="(papers, courseId) in groupedPapers" :key="courseId" class="course-section">
          <h4>{{ getCourseName(courseId) }} ({{ papers.length }} 份试卷)</h4>
          <div class="paper-grid">
            <div v-for="p in papers" :key="p.paperId" class="paper-card">
              <div class="paper-icon">📄</div>
              <div class="paper-info">
                <h4>{{ p.paperTitle }}</h4>
                <p>总分: {{ p.totalScore }}分 | 状态: <span :class="getPaperStatusClass(p.paperStatus)">{{ p.paperStatus
                }}</span></p>
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

    <!-- 考试安排模块 -->
    <div v-if="currentTab === 'exam'" class="tab-content fade-in">
      <div class="top-actions">
        <h3>考试安排 ({{ exams.length }})</h3>
        <div class="right-btns">
          <button class="btn btn-primary" @click="openExamModal()">+ 安排考试</button>
        </div>
      </div>

      <!-- 改为表格显示 -->
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
                <button class="btn-text btn-primary" @click="viewExamDetails(exam)">详情</button>
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

    <!-- 阅卷与统计模块 -->
    <div v-if="currentTab === 'result'" class="tab-content fade-in">
      <div class="top-actions">
        <h3>阅卷与统计</h3>
      </div>

      <div class="empty-tip">
        <p>阅卷与统计功能开发中...</p>
      </div>
    </div>

    <!-- 试题详情弹窗 -->
    <div class="modal-mask" v-if="showAnswerDetailModal">
      <div class="modal-box" style="width: 600px;">
        <div class="modal-header">
          <h3>试题详情</h3>
          <span class="close-btn" @click="closeAnswerDetailModal">×</span>
        </div>
        <div class="modal-body">
          <div class="detail-section">
            <h4 class="detail-title">【题目类型】</h4>
            <div class="detail-content">
              <span class="tag-type">{{ currentDetailQuestion.questionType }}</span>
            </div>
          </div>

          <div class="detail-section">
            <h4 class="detail-title">【题干内容】</h4>
            <div class="detail-content highlight-box">
              {{ currentDetailQuestion.content }}
            </div>
          </div>

          <div v-if="currentDetailQuestion.options" class="detail-section">
            <h4 class="detail-title">【选项设置】</h4>
            <div class="detail-content options-display">
              <pre style="white-space: pre-wrap; font-family: inherit;">{{ currentDetailQuestion.options }}</pre>
            </div>
          </div>

          <div class="detail-section">
            <h4 class="detail-title">【参考答案】</h4>
            <div class="detail-content highlight-box">
              <strong>{{ currentDetailQuestion.correctAnswer }}</strong>
            </div>
          </div>

          <div v-if="currentDetailQuestion.explanation" class="detail-section">
            <h4 class="detail-title">【题目解析】</h4>
            <div class="detail-content">
              {{ currentDetailQuestion.explanation }}
            </div>
          </div>

          <div class="detail-section">
            <div class="detail-info-row">
              <span class="info-item">
                <strong>分值：</strong>{{ currentDetailQuestion.score }}分
              </span>
              <span class="info-item">
                <strong>难度：</strong>
                <span :class="getDifficultyClass(currentDetailQuestion.difficulty)">
                  {{ currentDetailQuestion.difficulty }}
                </span>
              </span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-primary" @click="closeAnswerDetailModal">关闭</button>
        </div>
      </div>
    </div>

    <!-- 试题编辑/新增弹窗 -->
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
                  <input type="text" v-model="choiceOptions[index]" :placeholder="`请输入选项${getOptionLetter(index)}的内容`"
                    class="option-input">
                  <button v-if="choiceOptions.length > 2" type="button" class="btn-remove-option"
                    @click="removeChoiceOption(index)">
                    ×
                  </button>
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
              <label>选项</label>
              <div class="judge-options">
                <div class="judge-option">A. 正确</div>
                <div class="judge-option">B. 错误</div>
              </div>

              <div class="form-group">
                <label>正确答案 <span class="text-red">*</span></label>
                <div class="answer-options">
                  <div class="answer-option" :class="{ 'selected': judgeAnswer === 'A' }" @click="judgeAnswer = 'A'">
                    A
                  </div>
                  <div class="answer-option" :class="{ 'selected': judgeAnswer === 'B' }" @click="judgeAnswer = 'B'">
                    B
                  </div>
                </div>
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

    <!-- 智能组卷弹窗 -->
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
                      <div class="info-row">
                        <span class="score-tag">{{ q.score }}分</span>
                        <span :class="getDifficultyClass(q.difficulty)">{{ q.difficulty }}</span>
                      </div>
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

    <!-- 发布考试弹窗 -->
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
              <input type="checkbox" v-model="examForm.showAnswers">
              考试结束后向学生显示答案
            </label>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeExamModal">取消</button>
            <button class="btn btn-primary" @click="saveExam">立即发布</button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
// 导入 API
import {
  getQuestionList, addQuestion, updateQuestion, deleteQuestion, getQuestionsByCourse
} from '@/api/exam'
import { addPaper, deletePaper, getPapersGroupedByCourse } from '@/api/paper'
import { getExamList, deleteExam } from '@/api/exam'
import { getTeacherCourses } from '@/api/teacher'

export default {
  name: 'ExamManage',
  data() {
    return {
      currentTab: 'bank',
      questionTypes: ['单选', '多选', '判断', '简答'],

      // 数据存储
      questions: [],
      groupedPapers: {},  // 按课程分组的试卷
      exams: [],
      teacherCourses: [],

      // 筛选与用户信息
      selectedCourseId: '',
      currentTeacher: null,

      // 试题表单
      showQuestionModal: false,
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
      showPaperModal: false,
      paperForm: {
        paperTitle: '',
        courseId: '',
        questionIds: [],
        totalScore: 0
      },

      // 考试表单相关
      showExamModal: false,
      examForm: {
        examName: '',
        classId: '',
        paperId: '',
        startTime: '',
        timeLimit: 90,
        showAnswers: false
      },
      teachingClasses: [],  // 教学班数据

      // 控制重点：显隐与详情弹窗
      visibleAnswers: {},
      showAnswerDetailModal: false,
      currentDetailQuestion: {
        content: '',
        correctAnswer: '',
        explanation: '',
        questionType: ''
      }
    }
  },

  computed: {
    getQuestionsByType() {
      return (type) => {
        return this.questions.filter(q => q.questionType === type);
      };
    },
    getOptionLetter() {
      return (index) => String.fromCharCode(65 + index);
    },
    isLetterSelected() {
      return (letter) => this.selectedLetters.includes(letter);
    },
    isAllVisible() {
      return (type) => {
        const typeQuestions = this.getQuestionsByType(type);
        if (typeQuestions.length === 0) return false;
        return typeQuestions.every(q => this.visibleAnswers[q.questionId]);
      };
    },
    // 试卷总分数
    paperTotalScore() {
      return this.questions
        .filter(q => this.paperForm.questionIds.includes(q.questionId))
        .reduce((sum, q) => sum + q.score, 0);
    },
    // 按题型统计已选题数
    getSelectedCountByType() {
      return (type) => this.questions.filter(q =>
        q.questionType === type && this.paperForm.questionIds.includes(q.questionId)
      ).length;
    },
    // 根据选中的教学班过滤试卷
    filteredPapers() {
      if (!this.examForm.classId) return []

      // 找到选中的教学班
      const classInfo = this.teachingClasses.find(tc => tc.classId === this.examForm.classId)
      if (!classInfo) return []

      // 从 groupedPapers 中获取该课程的试卷
      const coursePapers = this.groupedPapers[classInfo.courseId] || []
      return coursePapers
    }
  },

  watch: {
    currentTab(newVal) {
      if (newVal === 'bank') this.fetchQuestions();
      if (newVal === 'paper') this.fetchPapers();
      if (newVal === 'exam') this.fetchExams();
    }
  },

  methods: {
    // 添加这个方法到 methods 中
    getPaperStatusClass(status) {
      const classMap = {
        '草稿': 'status-draft',
        '已发布': 'status-published'
      };
      return classMap[status] || '';
    },
    // --- 1. 题库管理方法 ---
    viewAnswerDetail(question) {
      console.log('查看详情:', question);
      this.currentDetailQuestion = {
        content: '',
        correctAnswer: '',
        explanation: '',
        questionType: '',
        ...question
      };
      this.showAnswerDetailModal = true;
    },

    closeAnswerDetailModal() {
      this.showAnswerDetailModal = false;
      this.currentDetailQuestion = {
        content: '',
        correctAnswer: '',
        explanation: '',
        questionType: ''
      };
    },

    toggleAnswerVisibility(id) {
      const isVisible = !!this.visibleAnswers[id];
      this.$set(this.visibleAnswers, id, !isVisible);
    },

    toggleGroupVisibility(type) {
      const typeQuestions = this.getQuestionsByType(type);
      if (typeQuestions.length === 0) return;
      const allVisible = this.isAllVisible(type);
      typeQuestions.forEach(q => {
        this.$set(this.visibleAnswers, q.questionId, !allVisible);
      });
    },

    // --- 2. 数据加载逻辑 ---
    async fetchInitialData() {
      if (!this.currentTeacher) return;

      try {
        // 获取教师课程
        const courseRes = await getTeacherCourses(this.currentTeacher.teacherId);
        if (courseRes.success) {
          this.teacherCourses = courseRes.data || [];
          if (this.teacherCourses.length > 0 && !this.selectedCourseId) {
            this.selectedCourseId = this.teacherCourses[0].courseId;
          }
        }

        // 加载所有数据
        await Promise.all([
          this.fetchQuestions(),
          this.fetchPapers(),
          this.fetchExams()
        ]);

      } catch (error) {
        console.error('初始化数据失败:', error);
        this.$message.error('加载数据失败，请刷新重试');
      }
    },

    async fetchQuestions() {
      if (!this.currentTeacher) return;
      try {
        let res = this.selectedCourseId
          ? await getQuestionsByCourse(this.selectedCourseId)
          : await getQuestionList('', this.currentTeacher.teacherId);
        if (res.success) {
          this.questions = [...(res.data || [])];
        }
      } catch (e) {
        console.error(e);
        this.$message.error('加载试题失败');
      }
    },

    async fetchQuestionsByCourse() {
      await this.fetchQuestions();
    },

    // --- 3. 试题增删改 ---
    async deleteQuestion(id) {
      if (!confirm('确定删除此题吗？')) return;
      try {
        const res = await deleteQuestion(id);
        if (res.success) {
          this.$message.success('删除成功');
          await this.fetchQuestions();
        }
      } catch (e) {
        console.error(e);
        this.$message.error('删除失败');
      }
    },

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
        this.qForm = {
          questionId: null,
          questionType: '单选',
          content: '',
          score: 5,
          difficulty: '中',
          courseId: this.selectedCourseId,
          options: '',
          correctAnswer: '',
          explanation: ''
        };
        this.choiceOptions = ['', '', '', ''];
        this.selectedLetters = [];
        this.judgeAnswer = '';
      }
      this.showQuestionModal = true;
    },

    closeQuestionModal() {
      this.showQuestionModal = false;
      this.resetQuestionForm();
    },

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
      if (this.choiceOptions.length < 8) {
        this.choiceOptions.push('');
      } else {
        this.$message.warning('最多只能添加8个选项');
      }
    },

    removeChoiceOption(index) {
      if (this.choiceOptions.length > 2) {
        this.choiceOptions.splice(index, 1);
        const removedLetter = this.getOptionLetter(index);
        this.selectedLetters = this.selectedLetters.filter(letter => letter !== removedLetter);
      }
    },

    toggleChoiceAnswer(letter) {
      if (this.qForm.questionType === '单选') {
        this.selectedLetters = [letter];
      } else {
        const index = this.selectedLetters.indexOf(letter);
        if (index === -1) {
          this.selectedLetters.push(letter);
        } else {
          this.selectedLetters.splice(index, 1);
        }
      }
    },

    async saveQuestion() {
      if (!this.qForm.content.trim()) {
        this.$message.error('请输入题干内容');
        return;
      }

      if (this.qForm.score <= 0 || this.qForm.score > 100) {
        this.$message.error('分值必须在1-100之间');
        return;
      }

      if (['单选', '多选'].includes(this.qForm.questionType)) {
        const emptyOption = this.choiceOptions.findIndex(opt => !opt.trim());
        if (emptyOption !== -1) {
          this.$message.error(`选项${this.getOptionLetter(emptyOption)}内容不能为空`);
          return;
        }

        if (this.selectedLetters.length === 0) {
          this.$message.error('请选择正确答案');
          return;
        }

        if (this.qForm.questionType === '单选' && this.selectedLetters.length > 1) {
          this.$message.error('单选题只能选择一个正确答案');
          return;
        }

        this.qForm.options = this.choiceOptions
          .map((opt, i) => `${this.getOptionLetter(i)}. ${opt}`)
          .join('\n');
        this.qForm.correctAnswer = this.selectedLetters.join('');

      } else if (this.qForm.questionType === '判断') {
        if (!this.judgeAnswer) {
          this.$message.error('请选择判断题答案');
          return;
        }
        this.qForm.correctAnswer = this.judgeAnswer === 'A' ? '正确' : '错误';
        this.qForm.options = 'A. 正确\nB. 错误';
      }

      if (!this.qForm.courseId) {
        this.qForm.courseId = this.selectedCourseId;
      }

      try {
        const res = this.qForm.questionId
          ? await updateQuestion(this.qForm)
          : await addQuestion(this.qForm);

        if (res.success) {
          this.$message.success('保存成功');
          this.closeQuestionModal();
          await this.fetchQuestions();
        } else {
          this.$message.error(res.message || '保存失败');
        }
      } catch (e) {
        console.error(e);
        this.$message.error('保存失败');
      }
    },

    editQuestion(question) {
      this.openQuestionModal(question);
    },

    // --- 4. 试卷管理方法 ---
    async fetchPapers() {
      if (!this.currentTeacher) return;
      try {
        const res = await getPapersGroupedByCourse(this.currentTeacher.teacherId);
        if (res.success) {
          this.groupedPapers = res.data || {};
        }
      } catch (e) {
        console.error('加载试卷列表失败:', e);
        this.$message.error('加载试卷列表失败');
      }
    },

    openPaperModal() {
      if (!this.currentTeacher) {
        this.$message.error('请先登录');
        return;
      }
      this.paperForm = {
        paperTitle: '',
        courseId: this.selectedCourseId || (this.teacherCourses.length > 0 ? this.teacherCourses[0].courseId : ''),
        questionIds: [],
        totalScore: 0
      };
      this.showPaperModal = true;
    },

    closePaperModal() {
      this.showPaperModal = false;
    },

    async savePaper() {
      if (!this.paperForm.paperTitle.trim()) {
        this.$message.error('请输入试卷标题');
        return;
      }

      if (!this.paperForm.courseId) {
        this.$message.error('请选择所属课程');
        return;
      }

      if (this.paperForm.questionIds.length === 0) {
        this.$message.error('请至少选择一道试题');
        return;
      }

      // 计算总分
      this.paperForm.totalScore = this.paperTotalScore;

      try {
        const res = await addPaper(this.paperForm);
        if (res.success) {
          this.$message.success('试卷创建成功');
          this.closePaperModal();
          await this.fetchPapers();
        } else {
          this.$message.error(res.message || '创建试卷失败');
        }
      } catch (error) {
        console.error('保存试卷失败:', error);
        this.$message.error('保存试卷失败');
      }
    },

    async deletePaper(id) {
      if (!confirm('确定删除此试卷吗？此操作不可恢复。')) return;
      try {
        const res = await deletePaper(id);
        if (res.success) {
          this.$message.success('试卷已删除');
          await this.fetchPapers();
        }
      } catch (e) {
        console.error('删除试卷失败:', e);
        this.$message.error('删除失败');
      }
    },

    // --- 5. 考试管理方法 ---
    async fetchExams() {
      if (!this.currentTeacher) return;
      try {
        const res = await getExamList(this.currentTeacher.teacherId);
        if (res.success) {
          this.exams = res.data || [];
        }
      } catch (e) {
        console.error('加载考试列表失败:', e);
        this.$message.error('加载考试列表失败');
      }
    },

    viewExamDetails(exam) {
      this.$message.info(`查看考试详情: ${exam.examName}`);
    },

    async deleteExam(id) {
      if (!confirm('确定取消此考试吗？')) return;
      try {
        const res = await deleteExam(id);
        if (res.success) {
          this.$message.success('考试已取消');
          await this.fetchExams();
        }
      } catch (e) {
        console.error('取消考试失败:', e);
        this.$message.error('取消失败');
      }
    },

    getPaperName(paperId) {
      // 在分组试卷中查找试卷
      for (const courseId in this.groupedPapers) {
        const paper = this.groupedPapers[courseId].find(p => p.paperId === paperId);
        if (paper) return paper.paperTitle;
      }
      return '未知试卷';
    },

    getExamStatusClass(exam) {
      const now = new Date();
      const startTime = new Date(exam.startTime);

      if (now < startTime) {
        return 'status-active';
      } else {
        const endTime = new Date(startTime.getTime() + exam.timeLimit * 60000);
        if (now < endTime) {
          return 'status-active';
        } else {
          return 'status-end';
        }
      }
    },

    getExamStatusText(exam) {
      const now = new Date();
      const startTime = new Date(exam.startTime);

      if (now < startTime) {
        return '未开始';
      } else {
        const endTime = new Date(startTime.getTime() + exam.timeLimit * 60000);
        if (now < endTime) {
          return '进行中';
        } else {
          return '已结束';
        }
      }
    },

    // --- 6. 辅助方法 ---
    parseOptions(optionString) {
      if (!optionString) return ['', '', '', ''];
      return optionString.split('\n').map(line => {
        const match = line.match(/^[A-Z]\.\s*(.+)/);
        return match ? match[1] : line;
      });
    },

    getDifficultyClass(difficulty) {
      const classMap = {
        '低': 'text-green',
        '中': 'text-orange',
        '高': 'text-red'
      };
      return classMap[difficulty] || '';
    },

    getCourseName(courseId) {
      const course = this.teacherCourses.find(c => c.courseId === courseId);
      return course ? course.courseName : '未知课程';
    },

    formatDate(dateStr) {
      if (!dateStr) return '-';
      try {
        const date = new Date(dateStr);
        return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
      } catch (e) {
        return dateStr;
      }
    },

    formatDateTime(dateStr) {
      if (!dateStr) return '-';
      try {
        const date = new Date(dateStr);
        return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
      } catch (e) {
        return dateStr;
      }
    },

    // --- 考试管理方法 ---
    openExamModal() {
      // 开发中：实际应该获取教学班数据
      // this.fetchTeachingClasses()
      this.teachingClasses = [
        { classId: 'C001', className: '软件工程2023班', courseId: 'C001', courseName: '软件工程' },
        { classId: 'C002', className: '数据库2023班', courseId: 'C002', courseName: '数据库原理' }
      ]

      this.examForm = {
        examName: '',
        classId: '',
        paperId: '',
        startTime: this.getDefaultStartTime(),
        timeLimit: 90,
        showAnswers: false
      }
      this.showExamModal = true
    },

    closeExamModal() {
      this.showExamModal = false
    },

    getDefaultStartTime() {
      // 默认设置为明天的当前时间
      const tomorrow = new Date()
      tomorrow.setDate(tomorrow.getDate() + 1)
      tomorrow.setHours(10, 0, 0, 0) // 10:00 AM
      return tomorrow.toISOString().slice(0, 16) // 格式: YYYY-MM-DDTHH:mm
    },

    getTeachingClassName(classId) {
      const tc = this.teachingClasses.find(t => t.classId === classId)
      return tc ? tc.className : '未知教学班'
    },

    async saveExam() {
      if (!this.examForm.examName.trim()) {
        this.$message.error('请输入考试名称')
        return
      }

      if (!this.examForm.classId) {
        this.$message.error('请选择教学班')
        return
      }

      if (!this.examForm.paperId) {
        this.$message.error('请选择试卷')
        return
      }

      if (!this.examForm.startTime) {
        this.$message.error('请选择开始时间')
        return
      }

      if (!this.examForm.timeLimit || this.examForm.timeLimit <= 0) {
        this.$message.error('请输入有效的限时时长')
        return
      }

      try {
        // 这里调用实际的API
        // const res = await addExam(this.examForm)
        // if (res.success) {
        //   this.$message.success('考试发布成功')
        //   this.closeExamModal()
        //   await this.fetchExams()
        // }

        this.$message.success('考试安排功能开发中，模拟发布成功')
        this.closeExamModal()
      } catch (error) {
        console.error('发布考试失败:', error)
        this.$message.error('发布考试失败')
      }
    },
  },

  mounted() {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (userInfo.role === 'teacher') {
      this.currentTeacher = userInfo;
      this.fetchInitialData();
    } else {
      this.$message.error('请以教师身份登录');
      this.$router.push('/login');
    }
  }
}
</script>

<style scoped>
  /* 状态标签样式 */
.status-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  border: 1px solid #ddd;
}

.status-gray {
  background: #f4f4f5;
  color: #909399;
  border-color: #e9e9eb;
}

.status-active {
  background: #e6f7ff;
  color: #1890ff;
  border-color: #91d5ff;
}

.status-end {
  background: #fdf6ec;
  color: #e6a23c;
  border-color: #faecd8;
}

/* 复选框样式 */
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: normal;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  margin: 0;
}

/* 表格中的操作按钮间距 */
.data-table td .btn-text + .btn-text {
  margin-left: 5px;
}

/* 题目信息行 - 水平排列 */
.info-row {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
}

/* 调整 info-col 宽度 */
.info-col {
  width: 120px;
  /* 从 100px 调整为 120px */
  display: flex;
  align-items: center;
  justify-content: flex-end;
  /* 改为右对齐 */
}

/* 调整题目字号 */
.q-text {
  display: block;
  word-break: break-all;
  font-size: 14px;
  /* 从 13px 调整为 14px */
  line-height: 1.6;
}

/* 调整 content-col 的右边距 */
.content-col {
  flex: 1;
  color: #333;
  line-height: 1.5;
  padding-right: 15px;
}

/* 调整分数标签和难度显示 */
.score-tag {
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 3px;
  color: #666;
  font-size: 12px;
}

.text-green,
.text-orange,
.text-red {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 3px;
}

/* 详情弹窗样式 */
.detail-section {
  margin-bottom: 20px;
}

.detail-title {
  color: #1890ff;
  font-size: 14px;
  margin-bottom: 8px;
  border-left: 3px solid #1890ff;
  padding-left: 8px;
}

.detail-content {
  background: #fafafa;
  padding: 15px;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-info-row {
  display: flex;
  gap: 20px;
}

.info-item {
  padding: 8px 12px;
  background: white;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

/* 选项展示样式 */
.options-display {
  background: #fff;
  border-color: #d9d9d9;
  line-height: 1.8;
}

/* 调整弹窗宽度 */
.modal-box[style*="width: 600px"] {
  width: 600px !important;
  max-width: 95vw;
}

/* 选项容器 */
.options-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
  padding: 15px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

/* 选项项 */
.option-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.option-label {
  width: 30px;
  font-weight: bold;
  color: #1890ff;
  font-size: 16px;
}

.option-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
  min-width: 200px;
}

.option-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

/* 删除按钮 */
.btn-remove-option {
  width: 28px;
  height: 28px;
  background: #fff1f0;
  border: 1px solid #ffa39e;
  color: #f5222d;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
  transition: all 0.3s;
  padding: 0;
  margin: 0;
}

.btn-remove-option:hover {
  background: #ffccc7;
  border-color: #ff7875;
  transform: scale(1.1);
}

/* 添加选项按钮 */
.btn-add-option {
  background: #f0f7ff;
  border: 1px solid #91d5ff;
  color: #1890ff;
  padding: 6px 15px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
  margin-left: 10px;
}

.btn-add-option:hover {
  background: #e6f7ff;
  border-color: #1890ff;
}

/* 判断题选项 */
.judge-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
  padding: 15px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.judge-option {
  padding: 10px 12px;
  background: white;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  font-weight: bold;
  color: #333;
}

/* 参考答案选项 */
.answer-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  padding: 15px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.answer-option {
  width: 45px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 2px solid #d9d9d9;
  border-radius: 50%;
  font-weight: bold;
  font-size: 16px;
  color: #333;
  cursor: pointer;
  transition: all 0.3s;
  user-select: none;
}

.answer-option:hover {
  border-color: #1890ff;
  color: #1890ff;
  transform: scale(1.05);
}

.answer-option.selected {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.answer-option.selected:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

/* 正确答案选项样式 */
.answer-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  padding: 15px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.answer-option {
  width: 45px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 2px solid #d9d9d9;
  border-radius: 50%;
  font-weight: bold;
  font-size: 16px;
  color: #333;
  cursor: pointer;
  transition: all 0.3s;
  user-select: none;
}

.answer-option:hover {
  border-color: #1890ff;
  color: #1890ff;
  transform: scale(1.05);
}

.answer-option.selected {
  background: #1890ff;
  border-color: #1890ff;
  color: white;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.answer-option.selected:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

/* 选项容器 */
.options-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
  padding: 15px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

/* 选项项 */
.option-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.option-label {
  width: 30px;
  font-weight: bold;
  color: #1890ff;
  font-size: 16px;
}

.option-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
}

.option-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

/* 删除按钮 */
.btn-remove-option {
  width: 28px;
  height: 28px;
  background: #fff1f0;
  border: 1px solid #ffa39e;
  color: #f5222d;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
  transition: all 0.3s;
  padding: 0;
  margin: 0;
}

.btn-remove-option:hover {
  background: #ffccc7;
  border-color: #ff7875;
  transform: scale(1.1);
}

/* 添加选项按钮 */
.btn-add-option {
  background: #f0f7ff;
  border: 1px solid #91d5ff;
  color: #1890ff;
  padding: 6px 15px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
  margin-left: 10px;
}

.btn-add-option:hover {
  background: #e6f7ff;
  border-color: #1890ff;
}

/* 判断题选项 */
.judge-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
  padding: 15px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.judge-option {
  padding: 10px 12px;
  background: white;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  font-weight: bold;
  color: #333;
}

/* 教学班筛选样式 */
.filter-row {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.filter-item select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-width: 200px;
}

/* 删除图标样式 */
.remove-option {
  color: #f5222d;
  cursor: pointer;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.remove-option:hover {
  background-color: #fff2f0;
}

.remove-option svg {
  display: block;
}

.top-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
  gap: 10px;
}

/* 保持原有基础布局 */
.manage-container {
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.nav-tabs {
  display: flex;
  background: #fff;
  padding: 0 20px;
  border-bottom: 1px solid #e4e7ed;
  gap: 30px;
}

.tab-item {
  padding: 15px 5px;
  cursor: pointer;
  font-size: 15px;
  color: #606266;
  font-weight: 500;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
}

.tab-item:hover {
  color: #1890ff;
}

.tab-item.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
}

.tab-content {
  padding: 20px 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  animation: fade 0.3s;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.action-bar h3 {
  margin: 0;
  border-left: 4px solid #1890ff;
  padding-left: 10px;
  font-size: 18px;
  color: #333;
}

.header-sm {
  margin-top: 20px;
  margin-bottom: 10px !important;
}

.header-sm h3 {
  font-size: 16px;
  border-left-width: 3px;
}

/* 题库分类区块 */
.question-group {
  margin-bottom: 30px;
}

.top-actions {
  text-align: right;
  margin-bottom: 10px;
}

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
  padding: 15px;
  color: #606266;
  border-bottom: 1px solid #ebeef5;
}

.data-table td {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  color: #606266;
  font-size: 14px;
  vertical-align: middle;
}

.text-left {
  text-align: left;
}

.text-ellipsis {
  max-width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}

.tag-type {
  background: #f0f5ff;
  color: #2f54eb;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.empty-row {
  text-align: center;
  color: #ccc;
  padding: 20px !important;
}

/* 答案交互 */
.th-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.answer-mask-box {
  display: flex;
  align-items: center;
  gap: 10px;
  font-family: monospace;
}

.answer-text.masked {
  letter-spacing: 2px;
  color: #ccc;
}

.header-eye-btn,
.eye-btn {
  cursor: pointer;
  color: #bbb;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.2s;
}

.header-eye-btn:hover,
.eye-btn:hover {
  background-color: #f0f7ff;
  color: #1890ff;
}

.header-eye-btn.active,
.eye-btn.active {
  color: #555;
}

.btn-view-detail {
  color: #1890ff;
  font-weight: 500;
}

.btn-view-detail:hover {
  text-decoration: underline;
}

/* 详情文本 */
.static-text {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  line-height: 1.6;
  color: #333;
}

.highlight-box {
  background: #f6ffed;
  border-color: #b7eb8f;
  color: #333;
}

/* 通用颜色/状态 */
.text-green {
  color: #52c41a;
}

.text-orange {
  color: #fa8c16;
}

.text-red {
  color: #f5222d;
}

.text-gray {
  color: #ccc;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  border: 1px solid #ddd;
}

.status-gray {
  background: #f4f4f5;
  color: #909399;
  border-color: #e9e9eb;
}

.status-active {
  background: #e6f7ff;
  color: #1890ff;
  border-color: #91d5ff;
}

.status-end {
  background: #fdf6ec;
  color: #e6a23c;
  border-color: #faecd8;
}

/* 试卷 Grid */
.paper-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.paper-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  transition: all 0.3s;
  position: relative;
}

.paper-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #c6e2ff;
}

.paper-icon {
  font-size: 30px;
  margin-right: 15px;
  opacity: 0.8;
}

.paper-info h4 {
  margin: 0 0 5px;
  font-size: 16px;
  color: #333;
}

.paper-info p {
  margin: 0;
  font-size: 12px;
  color: #999;
}

.paper-card .btn-danger {
  position: absolute;
  right: 15px;
  top: 15px;
  font-size: 12px;
}

/* 弹窗通用 */
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
  width: 700px;
  max-width: 95vw;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: fade 0.3s;
  display: flex;
  flex-direction: column;
  max-height: 85vh;
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
}

.close-btn {
  font-size: 20px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  max-height: 70vh;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.wide-modal {
  width: 700px;
}

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

.selector-group {
  margin-bottom: 20px;
}

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

.q-item-row:hover {
  background-color: #f0f9ff;
  border-color: #b3e19d;
}

.q-item-row.selected {
  background-color: #e6f7ff;
  border-color: #1890ff;
}

.check-col {
  width: 30px;
  display: flex;
  align-items: center;
  padding-top: 2px;
}

.content-col {
  flex: 1;
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  padding-right: 15px;
}

.info-col {
  width: 100px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  font-size: 12px;
}

.q-text {
  display: block;
  word-break: break-all;
}

.score-tag {
  background: #f4f4f5;
  padding: 1px 5px;
  border-radius: 3px;
  color: #666;
}

.empty-group {
  font-size: 12px;
  color: #999;
  padding-left: 10px;
  font-style: italic;
  margin-bottom: 10px;
}

/* 表单元素基础 */
.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

/* 试卷统计样式 */
.score-green {
  color: #52c41a;
  font-weight: bold;
}

.score-blue {
  color: #1890ff;
  font-weight: bold;
}

.separator {
  margin: 0 8px;
  color: #ccc;
}

/* 试题表单样式 */
textarea {
  resize: vertical;
  min-height: 80px;
}

/* 模态框表单间距优化 */
.modal-body form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 按钮样式 */
.btn {
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s;
  border: none;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary {
  background: #1890ff;
  color: white;
  border: 1px solid #1890ff;
}

.btn-primary:hover {
  background: #40a9ff;
  border-color: #40a9ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.btn-secondary {
  background: white;
  border: 1px solid #d9d9d9;
  color: #666;
}

.btn-secondary:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.btn-success {
  background: #52c41a;
  color: white;
}

.btn-text {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 13px;
  padding: 0 5px;
}

.btn-danger {
  color: #f5222d;
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 40px;
}

@keyframes fade {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in {
  animation: fade 0.3s;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .modal-box {
    width: 95vw;
    margin: 10px;
  }

  .wide-modal {
    width: 95%;
    max-width: 500px;
  }

  .form-row {
    flex-direction: column;
    gap: 15px;
  }

  .option-item {
    flex-wrap: wrap;
  }

  .option-input {
    min-width: 200px;
  }

  .answer-option {
    width: 40px;
    height: 40px;
    font-size: 14px;
  }

  .top-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-row {
    flex-direction: column;
    align-items: flex-start;
  }
}

/* 课程分组样式 */
.course-section {
  margin-bottom: 30px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #1890ff;
}

.course-section h4 {
  margin: 0 0 15px 0;
  color: #1890ff;
  font-size: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

/* 试卷卡片内的操作按钮 */
.paper-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 试卷状态标签 */
.paper-status {
  display: inline-block;
  padding: 2px 6px;
  font-size: 12px;
  border-radius: 3px;
  margin-left: 10px;
}

.status-draft {
  background: #f0f5ff;
  color: #2f54eb;
  border: 1px solid #adc6ff;
}

.status-published {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}
</style>