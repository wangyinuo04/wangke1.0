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
            <tr v-for="hw in filteredHomeworks" :key="hw.homeworkId">
              <td>
                <span class="status-badge" :class="getStatusClass(hw)">
                  {{ hw.status || getStatusText(hw) }}
                </span>
              </td>
              <td class="title-col">{{ hw.homeworkTitle }}</td>
              <td>{{ hw.className || getClassName(hw.classId) }}</td>
              <td class="time-col">{{ formatDateTime(hw.deadline) }}</td>
              <td>
                <div class="progress-box">
                  <span class="progress-text">{{ hw.submittedCount || 0 }}/{{ hw.totalCount || 0 }}</span>
                  <div class="progress-bar">
                    <div class="fill" :style="{ width: getProgressWidth(hw) + '%' }"></div>
                  </div>
                </div>
              </td>
              <td>
                <div class="action-col">
                  <button class="btn-text btn-primary" @click="enterGrading(hw)">批阅管理</button>
                  <button class="btn-text btn-edit" @click="editHomework(hw)">编辑</button>
                  <button class="btn-text btn-danger" @click="deleteHomework(hw.homeworkId)">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredHomeworks.length === 0 && !loading">
              <td colspan="6" class="empty-state">暂无作业记录</td>
            </tr>
            <tr v-if="loading">
              <td colspan="6" class="empty-state">加载中...</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-else class="view-wrapper fade-in">
      <div class="detail-header">
        <button class="btn-back" @click="currentView = 'list'">← 返回列表</button>
        <div class="detail-info">
          <h3>{{ currentHomework.homeworkTitle }}</h3>
          <span class="detail-meta">截止: {{ formatDateTime(currentHomework.deadline) }} | 总分: {{ currentHomework.totalScore }}分</span>
        </div>
        <div class="detail-actions">
          <button class="btn btn-secondary" @click="batchDownload(currentHomework.homeworkId)" :disabled="currentHomework.submittedCount === 0">
            📦 批量下载附件 (ZIP)
          </button>
        </div>
      </div>

      <div class="stats-panel">
        <div class="stat-item">
          <label>提交率</label>
          <div class="value">{{ currentHomework.submittedCount || 0 }} / {{ currentHomework.totalCount || 0 }}</div>
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
            <tr v-for="sub in currentSubmissions" :key="sub.submissionId">
              <td class="mono">{{ sub.studentId }}</td>
              <td>{{ sub.studentName }}</td>
              <td>
                <span :class="sub.submissionStatus === '已提交' ? 'text-green' : 'text-gray'">
                  {{ sub.submissionStatus || '未提交' }}
                </span>
              </td>
              <td class="time-col">{{ sub.submissionTime ? formatDateTime(sub.submissionTime) : '-' }}</td>
              <td>
                <a v-if="sub.attachmentPath" href="#" class="file-link" @click.prevent="downloadFile(sub.attachmentPath)">
                  📄 下载
                </a>
                <span v-else class="text-gray">-</span>
              </td>
              <td>
                <span v-if="sub.score !== null && sub.score !== undefined" class="score-tag">{{ sub.score }}</span>
                <span v-else class="text-gray">-</span>
              </td>
              <td>
                <button 
                  class="btn-text btn-primary" 
                  :disabled="sub.submissionStatus !== '已提交'"
                  @click="openGradeModal(sub)"
                >
                  {{ sub.score !== null && sub.score !== undefined ? '修改评分' : '批改' }}
                </button>
              </td>
            </tr>
            <tr v-if="currentSubmissions.length === 0 && !submissionLoading">
              <td colspan="7" class="empty-state">暂无提交记录</td>
            </tr>
            <tr v-if="submissionLoading">
              <td colspan="7" class="empty-state">加载中...</td>
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
              <input type="text" v-model="form.homeworkTitle" placeholder="例：第一次实验报告">
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>关联教学班 <span class="required">*</span></label>
                <select 
                  v-model="form.classId" 
                  @change="onClassChange"
                  :disabled="isEditMode" 
                  v-if="teacherClasses.length > 0"
                >
                  <option value="">请选择班级</option>
                  <option 
                    v-for="cls in teacherClasses" 
                    :key="cls.id" 
                    :value="cls.id"
                  >
                    {{ cls.className }} ({{ cls.courseName }})
                  </option>
                </select>
                <div v-else class="loading-classes">
                  <span>正在加载班级列表...</span>
                </div>
                <p v-if="teacherClasses.length === 0 && !isEditMode" class="error-msg">
                  您暂无负责的教学班，请先联系管理员创建教学班并分配给您。
                </p>
              </div>
              <div class="form-group">
                <label>作业总分 <span class="required">*</span></label>
                <input type="number" v-model.number="form.totalScore" min="1" max="100">
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>发布时间</label>
                <input type="datetime-local" v-model="form.publishTime">
              </div>
              <div class="form-group">
                <label>截止时间 <span class="required">*</span></label>
                <input type="datetime-local" v-model="form.deadline">
              </div>
            </div>

            <div class="form-group">
              <label>作业说明 (富文本)</label>
              <textarea v-model="form.description" rows="4" placeholder="请输入作业要求、注意事项等..."></textarea>
            </div>

            <div class="form-group">
              <label>附件资料</label>
              <div class="file-box">
                <input type="file" ref="attachmentFile" @change="handleFileUpload" style="display: none">
                <input type="text" :value="form.attachmentName || '未选择文件'" readonly>
                <button type="button" class="btn btn-sm btn-secondary" @click="triggerFileUpload">选择文件</button>
                <button v-if="form.attachmentFile" type="button" class="btn btn-sm btn-danger" @click="clearFile">清除</button>
              </div>
              <p v-if="form.attachmentFile" class="file-info">
                已选择: {{ form.attachmentFile.name }} ({{ formatFileSize(form.attachmentFile.size) }})
              </p>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeCreateModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="saving || (teacherClasses.length === 0 && !isEditMode)">
                {{ saving ? '保存中...' : '发布' }}
              </button>
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
            <p><strong>提交时间：</strong>{{ currentStudentSub.submissionTime ? formatDateTime(currentStudentSub.submissionTime) : '-' }}</p>
            <p v-if="currentStudentSub.submissionText"><strong>提交内容：</strong>{{ currentStudentSub.submissionText }}</p>
            <p v-if="currentStudentSub.attachmentPath"><strong>附件：</strong>{{ currentStudentSub.attachmentPath }}</p>
          </div>
          
          <div class="form-group">
            <label>得分 (满分 {{ currentHomework.totalScore }}) <span class="required">*</span></label>
            <input type="number" v-model.number="gradeForm.score" :max="currentHomework.totalScore" min="0" step="0.1">
          </div>
          
          <div class="form-group">
            <label>教师评语</label>
            <textarea v-model="gradeForm.comment" rows="3" placeholder="请输入评语..."></textarea>
          </div>

          <div class="modal-footer">
            <button class="btn btn-secondary" @click="closeGradeModal">取消</button>
            <button class="btn btn-primary" @click="submitGrade" :disabled="grading">
              {{ grading ? '批改中...' : '确认打分' }}
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import {
  getHomeworkList,
  publishHomework,
  getHomeworkSubmissions,
  gradeHomework,
  downloadHomeworkFile,
  batchDownloadHomework,
  deleteHomework
} from '@/api/homework'

import { getTeacherCourses } from '@/api/teachingClass'

export default {
  name: 'HomeworkManage',
  data() {
    return {
      currentView: 'list',
      searchQuery: '',
      showCreateModal: false,
      showGradeModal: false,
      isEditMode: false,
      loading: false,
      saving: false,
      grading: false,
      submissionLoading: false,

      teacherClasses: [],
      homeworks: [],

      currentHomework: {},
      currentSubmissions: [],

      form: {
        homeworkId: null,
        homeworkTitle: '',
        classId: '',  // 初始化为空字符串
        totalScore: 100,
        publishTime: this.formatDateTimeForInput(new Date()),
        deadline: this.formatDateTimeForInput(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)),
        description: '',
        attachmentFile: null,
        attachmentName: ''
      },

      gradeForm: {
        submissionId: '',
        score: '',
        comment: ''
      },

      currentStudentSub: null,
      currentTeacherId: ''
    }
  },
  computed: {
    filteredHomeworks() {
      if (!this.searchQuery) return this.homeworks;
      return this.homeworks.filter(h =>
        h.homeworkTitle.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        (h.className && h.className.toLowerCase().includes(this.searchQuery.toLowerCase()))
      );
    }
  },
  created() {
    this.initData();
  },
  methods: {
    // 初始化数据
    async initData() {
      await this.getCurrentTeacher();
      if (this.currentTeacherId) {
        // 先加载班级，再加载作业
        await this.loadTeacherClasses();
        await this.loadHomeworks();
      }
    },

    // 获取当前登录教师信息
    getCurrentTeacher() {
      return new Promise((resolve) => {
        const userInfoStr = localStorage.getItem('userInfo');
        if (userInfoStr) {
          try {
            const userInfo = JSON.parse(userInfoStr);
            if (userInfo.role === 'teacher') {
              this.currentTeacherId = userInfo.teacher_id || userInfo.id || userInfo.teacherId || userInfo.userId || '';
              console.log('当前教师ID:', this.currentTeacherId);
            }
          } catch (e) {
            console.error('解析用户信息失败:', e);
          }
        }
        resolve();
      });
    },

    // 加载教师负责的班级
    async loadTeacherClasses() {
      if (!this.currentTeacherId) {
        console.error('当前教师ID为空，无法加载班级');
        this.$message.error('无法获取教师信息，请重新登录');
        return;
      }

      console.log('开始加载教师班级，教师ID:', this.currentTeacherId);

      try {
        const response = await getTeacherCourses(this.currentTeacherId);
        console.log('班级加载原始响应:', response);

        if (response.success) {
          this.teacherClasses = response.data || [];
          console.log('加载到的班级数据（详细）:');
          if (this.teacherClasses.length > 0) {
            this.teacherClasses.forEach((cls, index) => {
              console.log(`班级 ${index + 1}:`, cls);
              console.log(`  - 所有属性:`, Object.keys(cls));
            });
          } else {
            console.log('班级数据为空数组');
          }

          if (this.teacherClasses.length === 0) {
            console.warn('教师暂无负责的教学班');
            this.$message.warning('您暂无负责的教学班');
          } else {
            console.log('班级加载完成，第一个班级详细信息:', this.teacherClasses[0]);
          }
        } else {
          console.error('加载班级失败:', response.message);
          this.$message.error(response.message || '加载班级列表失败');
        }
      } catch (error) {
        console.error('加载班级列表异常:', error);
        console.error('错误详情:', error.response);
        this.$message.error('加载班级列表失败，请检查网络连接');
      }
    },

    // 加载作业列表
    async loadHomeworks() {
      if (!this.currentTeacherId) return;

      this.loading = true;
      try {
        const response = await getHomeworkList(this.currentTeacherId);
        if (response.success) {
          this.homeworks = response.data || [];
          console.log('加载到的作业数据:', this.homeworks);
        } else {
          this.$message.error(response.message || '加载作业列表失败');
        }
      } catch (error) {
        console.error('加载作业列表失败:', error);
        this.$message.error('加载作业列表失败');
      } finally {
        this.loading = false;
      }
    },

    // 搜索
    handleSearch() {
      console.log('搜索关键词:', this.searchQuery);
    },

    // 辅助函数
    getClassName(classId) {
      const cls = this.teacherClasses.find(c => c.classId === classId);
      if (cls) {
        return cls.className || `班级 ${classId}`;
      }

      // 如果班级列表中找不到，从作业数据中查找
      const homework = this.homeworks.find(h => h.classId === classId);
      if (homework && homework.className) {
        return homework.className;
      }

      return `班级 ${classId}`;
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
      const status = hw.status || this.getStatusText(hw);
      if (status === '进行中') return 'status-active';
      if (status === '已结束') return 'status-end';
      return 'status-wait';
    },

    getProgressWidth(hw) {
      const submitted = hw.submittedCount || 0;
      const total = hw.totalCount || 1;
      return Math.min(100, (submitted / total) * 100);
    },

    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '-';
      const date = new Date(dateTimeStr);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      }).replace(/\//g, '-');
    },

    formatDateTimeForInput(date) {
      const d = new Date(date);
      const year = d.getFullYear();
      const month = (d.getMonth() + 1).toString().padStart(2, '0');
      const day = d.getDate().toString().padStart(2, '0');
      const hours = d.getHours().toString().padStart(2, '0');
      const minutes = d.getMinutes().toString().padStart(2, '0');
      return `${year}-${month}-${day}T${hours}:${minutes}`;
    },

    formatFileSize(bytes) {
      if (bytes < 1024) return bytes + ' B';
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
      if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
      return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB';
    },

    // --- 发布作业逻辑 ---
    async openCreateModal() {
      console.log('=== 打开创建作业模态框 ===');
      console.log('teacherClasses 数据:', this.teacherClasses);

      // 如果班级数据为空，重新加载
      if (this.teacherClasses.length === 0) {
        console.log('班级数据为空，重新加载...');
        try {
          await this.loadTeacherClasses();
        } catch (error) {
          console.error('加载班级失败:', error);
        }
      }

      if (this.teacherClasses.length === 0) {
        this.$message.error('您暂无负责的教学班，无法发布作业');
        return;
      }

      this.isEditMode = false;

      // 获取第一个班级的ID - 注意：这里使用的是 `id` 而不是 `classId`
      const firstClass = this.teacherClasses[0];
      const defaultClassId = firstClass?.id || '';  // 关键：使用 id 字段

      console.log('默认班级ID:', defaultClassId);

      // 使用 this.$set 确保响应式
      this.form = {
        homeworkId: null,
        homeworkTitle: '',
        classId: defaultClassId,  // 这里设置
        totalScore: 100,
        publishTime: this.formatDateTimeForInput(new Date()),
        deadline: this.formatDateTimeForInput(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)),
        description: '',
        attachmentFile: null,
        attachmentName: ''
      };

      // 关键：使用 $set 确保响应式
      this.$set(this.form, 'classId', defaultClassId);

      console.log('表单初始化完成，form.classId:', this.form.classId);
      this.showCreateModal = true;

      // 确保DOM更新后再次检查
      this.$nextTick(() => {
        console.log('模态框打开后，form.classId:', this.form.classId);
        // 强制更新选择框
        const selectElement = document.querySelector('select[v-model="form.classId"]');
        if (selectElement) {
          selectElement.value = defaultClassId;
          console.log('设置选择框值为:', defaultClassId);
        }
      });
    },

    editHomework(hw) {
      console.log('编辑作业:', hw);
      this.isEditMode = true;
      this.form = {
        homeworkId: hw.homeworkId,
        homeworkTitle: hw.homeworkTitle,
        classId: hw.classId || '',
        totalScore: hw.totalScore,
        publishTime: this.formatDateTimeForInput(new Date(hw.publishTime)),
        deadline: this.formatDateTimeForInput(new Date(hw.deadline)),
        description: hw.description || '',
        attachmentFile: null,
        attachmentName: hw.attachmentPath ? hw.attachmentPath.split('/').pop() : ''
      };
      console.log('编辑表单数据:', this.form);
      this.showCreateModal = true;
    },

    triggerFileUpload() {
      this.$refs.attachmentFile.click();
    },

    handleFileUpload(event) {
      const file = event.target.files[0];
      if (file) {
        this.form.attachmentFile = file;
        this.form.attachmentName = file.name;
      }
    },

    clearFile() {
      this.form.attachmentFile = null;
      this.form.attachmentName = '';
      this.$refs.attachmentFile.value = '';
    },

    closeCreateModal() {
      console.log('关闭模态框');
      this.showCreateModal = false;
      this.clearFile();
    },

    async saveHomework() {
      console.log('=== 开始保存作业 ===');
      console.log('当前表单数据:', this.form);
      console.log('classId 值:', this.form.classId, '类型:', typeof this.form.classId);

      // 验证标题
      if (!this.form.homeworkTitle || this.form.homeworkTitle.trim() === '') {
        this.$message.error('请输入作业标题');
        return;
      }

      // 验证班级ID - 使用严格检查
      if (!this.form.classId) {
        console.error('班级ID为空');
        console.error('当前 form.classId:', this.form.classId);
        console.error('teacherClasses:', this.teacherClasses);

        // 尝试从下拉菜单获取当前值
        const selectElement = document.querySelector('select[v-model="form.classId"]');
        if (selectElement) {
          const selectedValue = selectElement.value;
          console.log('从DOM获取的选中值:', selectedValue);
          if (selectedValue) {
            this.form.classId = selectedValue;
            console.log('使用DOM值更新 form.classId:', this.form.classId);
          }
        }

        // 再次检查
        if (!this.form.classId) {
          this.$message.error('请选择关联班级');
          return;
        }
      }

      // 确保 classId 是字符串
      const classIdStr = String(this.form.classId).trim();
      if (!classIdStr) {
        this.$message.error('班级ID无效');
        return;
      }

      this.form.classId = classIdStr;
      console.log('验证后的 classId:', this.form.classId);

      this.saving = true;

      try {
        // 创建 FormData
        const formData = new FormData();

        // 添加所有必填字段
        formData.append('homeworkTitle', this.form.homeworkTitle);
        formData.append('description', this.form.description || '');
        formData.append('classId', this.form.classId);
        formData.append('totalScore', String(this.form.totalScore || 100));
        formData.append('publishTime', this.form.publishTime.replace('T', ' '));
        formData.append('deadline', this.form.deadline.replace('T', ' '));

        console.log('FormData 内容:');
        for (let [key, value] of formData.entries()) {
          console.log(`${key}: ${value}`);
        }

        if (this.form.attachmentFile) {
          formData.append('attachmentFile', this.form.attachmentFile);
          console.log('附件文件:', this.form.attachmentFile.name);
        }

        console.log('调用 publishHomework API...');
        const response = await publishHomework(formData);
        console.log('API 响应:', response);

        if (response.success) {
          this.$message.success(response.message || '作业发布成功');
          this.closeCreateModal();
          await this.loadHomeworks(); // 重新加载列表
        } else {
          this.$message.error(response.message || '作业发布失败');
        }
      } catch (error) {
        console.error('保存作业失败:', error);
        this.$message.error('操作失败，请稍后重试');
      } finally {
        this.saving = false;
      }
    },

    async deleteHomework(homeworkId) {
      if (!confirm('确定删除该作业吗？所有提交记录也将被删除！')) {
        return;
      }

      try {
        const response = await deleteHomework(homeworkId);
        if (response.success) {
          this.$message.success('删除成功');
          this.loadHomeworks();
        } else {
          this.$message.error(response.message || '删除失败');
        }
      } catch (error) {
        console.error('删除作业失败:', error);
        this.$message.error('删除失败');
      }
    },

    // --- 进入批改详情页 ---
    async enterGrading(hw) {
      this.currentHomework = hw;
      this.currentView = 'detail';
      await this.loadSubmissions(hw.homeworkId);
    },

    async loadSubmissions(homeworkId) {
      this.submissionLoading = true;
      try {
        const response = await getHomeworkSubmissions(homeworkId);
        if (response.success) {
          this.currentSubmissions = response.data || [];
          console.log('加载到的提交记录:', this.currentSubmissions);
        } else {
          this.$message.error(response.message || '加载提交记录失败');
        }
      } catch (error) {
        console.error('加载提交记录失败:', error);
        this.$message.error('加载提交记录失败');
      } finally {
        this.submissionLoading = false;
      }
    },

    // --- 在线批改逻辑 ---
    openGradeModal(sub) {
      this.currentStudentSub = sub;
      this.gradeForm = {
        submissionId: sub.submissionId,
        score: sub.score || '',
        comment: sub.teacherFeedback || ''
      };
      this.showGradeModal = true;
    },

    closeGradeModal() {
      this.showGradeModal = false;
    },

    async submitGrade() {
      if (this.gradeForm.score === '' || this.gradeForm.score === null) {
        this.$message.error('请输入得分');
        return;
      }

      if (this.gradeForm.score > this.currentHomework.totalScore) {
        this.$message.error(`得分不能超过总分 ${this.currentHomework.totalScore}`);
        return;
      }

      this.grading = true;

      try {
        console.log('提交批改数据:', this.gradeForm);
        const response = await gradeHomework(
          this.gradeForm.submissionId,
          this.gradeForm.score,
          this.gradeForm.comment
        );
        console.log('批改响应:', response);

        if (response.success) {
          this.$message.success('批改成功');

          // 更新本地数据
          const subIndex = this.currentSubmissions.findIndex(
            s => s.submissionId === this.gradeForm.submissionId
          );

          if (subIndex !== -1) {
            this.currentSubmissions[subIndex].score = this.gradeForm.score;
            this.currentSubmissions[subIndex].teacherFeedback = this.gradeForm.comment;
            this.currentSubmissions[subIndex].gradingStatus = '已批改';

            // 触发UI更新
            this.currentSubmissions = [...this.currentSubmissions];
          }

          this.closeGradeModal();
        } else {
          this.$message.error(response.message || '批改失败');
        }
      } catch (error) {
        console.error('批改作业失败:', error);
        this.$message.error('批改失败，请稍后重试');
      } finally {
        this.grading = false;
      }
    },

    // --- 批量下载 ---
    async batchDownload(homeworkId) {
      try {
        const response = await batchDownloadHomework(homeworkId);

        // 创建Blob对象
        const blob = new Blob([response], { type: 'application/zip' });
        const url = window.URL.createObjectURL(blob);

        // 创建下载链接
        const link = document.createElement('a');
        link.href = url;
        link.download = `作业_${homeworkId}_批量下载.zip`;
        document.body.appendChild(link);
        link.click();

        // 清理
        setTimeout(() => {
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);
        }, 100);

      } catch (error) {
        console.error('批量下载失败:', error);
        if (error.response && error.response.status === 404) {
          this.$message.warning('暂无已提交的作业文件');
        } else {
          this.$message.error('批量下载失败');
        }
      }
    },

    async downloadFile(filePath) {
      try {
        const response = await downloadHomeworkFile(filePath);

        // 从文件路径中提取文件名
        const fileName = filePath.split('/').pop() || '作业文件';

        // 创建Blob对象
        const blob = new Blob([response], { type: response.type });
        const url = window.URL.createObjectURL(blob);

        // 创建下载链接
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();

        // 清理
        setTimeout(() => {
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);
        }, 100);

      } catch (error) {
        console.error('下载文件失败:', error);
        this.$message.error('下载文件失败');
      }
    },

    // --- 统计计算 ---
    getGradedCount() {
      return this.currentSubmissions.filter(s =>
        s.gradingStatus === '已批改' || (s.score !== null && s.score !== undefined)
      ).length;
    },

    getAverageScore() {
      const graded = this.currentSubmissions.filter(s =>
        s.score !== null && s.score !== undefined
      );

      if (graded.length === 0) return 0;

      const sum = graded.reduce((total, s) => total + Number(s.score), 0);
      return (sum / graded.length).toFixed(1);
    },

    // 添加班级选择变化事件
    onClassChange(event) {
      console.log('班级选择变化，新值:', event.target.value);
      console.log('form.classId 当前值:', this.form.classId);

      // 手动更新 form.classId
      this.form.classId = event.target.value;

      // 使用 $set 确保响应式
      this.$set(this.form, 'classId', event.target.value);

      console.log('更新后的 form.classId:', this.form.classId);
    }
  }
}
</script>

<style scoped>
/* 样式保持不变 */
.error-msg {
  color: #f5222d;
  font-size: 12px;
  margin-top: 5px;
}

/* 基础样式复用 */
.manage-container {
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  height: 100%;
}

.action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.title-section h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.subtitle {
  margin: 5px 0 0;
  font-size: 13px;
  color: #999;
}

.operation-section {
  display: flex;
  gap: 15px;
}

/* 搜索 & 按钮 */
.search-box {
  display: flex;
}

.search-box input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-right: none;
  border-radius: 4px 0 0 4px;
  width: 200px;
}

.btn-search {
  border-radius: 0 4px 4px 0;
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6;
  border-left: none;
  cursor: pointer;
  padding: 8px 12px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

.btn-secondary {
  background: white;
  border: 1px solid #dcdfe6;
  color: #606266;
}

.btn-sm {
  padding: 4px 10px;
  font-size: 12px;
}

/* 列表视图 */
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
  font-weight: 600;
  border-bottom: 1px solid #ebeef5;
}

.data-table td {
  padding: 15px;
  border-bottom: 1px solid #ebeef5;
  color: #606266;
  font-size: 14px;
  vertical-align: middle;
}

.data-table tr:hover {
  background-color: #f9f9f9;
}

.title-col {
  font-weight: bold;
  color: #333;
}

.time-col {
  font-family: monospace;
  font-size: 13px;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-active {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.status-end {
  background: #f5f5f5;
  color: #909399;
  border: 1px solid #e9e9eb;
}

.status-wait {
  background: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.progress-box {
  width: 100px;
}

.progress-text {
  font-size: 12px;
  display: block;
  margin-bottom: 2px;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.fill {
  height: 100%;
  background: #52c41a;
}

.action-col {
  display: flex;
  gap: 8px;
}

.btn-text {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 13px;
  padding: 0;
}

.btn-text.btn-primary {
  color: #1890ff;
  font-weight: bold;
}

.btn-text.btn-edit {
  color: #909399;
}

.btn-text.btn-danger {
  color: #f5222d;
}

/* 详情视图 (Detail) */
.detail-header {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.btn-back {
  border: none;
  background: none;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  font-weight: bold;
}

.btn-back:hover {
  color: #1890ff;
}

.detail-info h3 {
  margin: 0 0 5px;
  font-size: 20px;
  color: #333;
}

.detail-meta {
  color: #909399;
  font-size: 13px;
}

.detail-actions {
  margin-left: auto;
}

.stats-panel {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  flex: 1;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

.stat-item label {
  color: #909399;
  font-size: 13px;
  display: block;
  margin-bottom: 5px;
}

.stat-item .value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-item .value.highlight {
  color: #fa8c16;
}

.file-link {
  color: #1890ff;
  text-decoration: none;
  font-size: 13px;
}

.file-link:hover {
  text-decoration: underline;
}

.text-green {
  color: #52c41a;
}

.text-gray {
  color: #ccc;
}

.score-tag {
  background: #f6ffed;
  color: #52c41a;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: bold;
  border: 1px solid #b7eb8f;
}

.mono {
  font-family: monospace;
}

/* 弹窗样式 */
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
  width: 600px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: fade 0.3s;
}

.small-modal {
  width: 400px;
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
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  font-size: 13px;
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

.form-row {
  display: flex;
  gap: 15px;
}

.form-row .form-group {
  flex: 1;
}

.file-box {
  display: flex;
  gap: 10px;
  align-items: center;
}

.file-box input[type="text"] {
  flex: 1;
}

.file-info {
  margin-top: 5px;
  font-size: 12px;
  color: #666;
}

.submission-info {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
  font-size: 13px;
  color: #606266;
}

.submission-info p {
  margin: 5px 0;
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

/* 加载状态 */
.empty-state {
  text-align: center;
  color: #999;
  padding: 40px !important;
}

/* 禁用状态 */
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>