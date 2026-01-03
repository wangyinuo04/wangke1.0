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
            <button class="btn btn-search" @click="handleSearch">🔍</button>
          </div>
          <button class="btn btn-primary" @click="openCreateModal"> + 发布新作业</button>
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
              <th width="220">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="hw in filteredHomeworks" :key="hw.homeworkId">
              <td>
                <span class="status-badge" :class="getStatusClass(hw)">
                  {{ getStatusText(hw) }}
                </span>
              </td>
              <td class="title-col">{{ hw.homeworkTitle }}</td>
              <td>{{ hw.className || '未知班级' }}</td>
              <td class="time-col">{{ formatTime(hw.deadline) }}</td>
              <td>
                <span class="text-blue">查看详情</span>
              </td>
              <td>
                <div class="action-col">
                  <button class="btn-op op-primary" @click="openGradingModal(hw)">批改</button>
                  <button class="btn-op op-text" @click="handleEdit(hw)">编辑</button>
                  <button class="btn-op op-text text-red" @click="handleDelete(hw)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-mask" v-if="showCreateModal">
      <div class="modal-box">
          <div class="modal-header"><h3>发布新作业</h3><span class="close-btn" @click="closeCreateModal">×</span></div>
          <div class="modal-body">
              <p style="text-align:center;color:#999;">(此处保留原有发布表单)</p>
          </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showGradingModal">
      <div class="modal-box wide-modal-xl">
        <div class="modal-header">
          <h3>批改作业 - {{ currentHomework.homeworkTitle }}</h3>
          <span class="close-btn" @click="closeGradingModal">×</span>
        </div>
        <div class="modal-body scroll-body">
          <table class="data-table">
            <thead>
              <tr>
                <th width="100">姓名</th>
                <th width="180">提交时间</th>
                <th>作业内容/附件</th>
                <th width="100">状态</th>
                <th width="120">得分</th>
                <th width="100">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="sub in submissionList" :key="sub.submissionId">
                <td class="font-bold">{{ sub.studentName }}</td>
                <td class="time-col">{{ formatTime(sub.submissionTime) }}</td>
                <td>
                  <div v-if="sub.submissionText" class="text-truncate" :title="sub.submissionText">
                    {{ sub.submissionText }}
                  </div>
                  <div v-if="sub.attachmentPath" class="mt-1">
                    <a class="link-btn" @click="handleDownload(sub.attachmentPath)">📎 下载附件</a>
                  </div>
                  <div v-if="!sub.submissionText && !sub.attachmentPath" class="text-gray">无内容</div>
                </td>
                <td>
                  <span class="status-tag" :class="sub.gradingStatus === '已批改' ? 'bg-green' : 'bg-orange'">
                    {{ sub.gradingStatus }}
                  </span>
                </td>
                <td class="font-bold text-blue">{{ sub.score !== null ? sub.score : '-' }}</td>
                <td>
                  <button class="btn-op op-primary" @click="openGradeForm(sub)">评分</button>
                </td>
              </tr>
              <tr v-if="submissionList.length === 0">
                <td colspan="6" class="empty-state">暂无学生提交</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="modal-mask" style="z-index: 2100;" v-if="showGradeForm">
      <div class="modal-box">
        <div class="modal-header">
          <h3>给 {{ currentSubmission.studentName }} 评分</h3>
          <span class="close-btn" @click="closeGradeForm">×</span>
        </div>
        <div class="modal-body">
          <div class="info-block">
            <label>提交内容：</label>
            <div class="preview-box">{{ currentSubmission.submissionText || '无文本内容' }}</div>
          </div>
          
          <div class="form-group">
            <label>得分 (总分: {{ currentHomework.totalScore }})</label>
            <input type="number" v-model="gradeForm.score" class="input-lg" :max="currentHomework.totalScore" min="0">
          </div>
          <div class="form-group">
            <label>评语</label>
            <textarea v-model="gradeForm.feedback" rows="3" placeholder="写点鼓励的话吧..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeGradeForm">取消</button>
          <button class="btn btn-primary" @click="submitGrade">确认提交</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { getHomeworkList, getHomeworkSubmissions, gradeHomework, downloadHomeworkFile, deleteHomework } from '@/api/homework'

export default {
  name: 'HomeworkManage',
  data() {
    return {
      currentView: 'list',
      searchQuery: '',
      teacherId: '', // 从登录信息获取
      homeworks: [],
      
      // 弹窗控制
      showCreateModal: false,
      showGradingModal: false,
      showGradeForm: false,

      // 当前操作数据
      currentHomework: {},
      submissionList: [],
      currentSubmission: {},
      
      // 评分表单
      gradeForm: {
        score: '',
        feedback: ''
      }
    }
  },
  computed: {
    filteredHomeworks() {
      if (!this.searchQuery) return this.homeworks;
      return this.homeworks.filter(h => h.homeworkTitle.includes(this.searchQuery));
    }
  },
  created() {
    // 获取当前用户ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    this.teacherId = userInfo.teacherId || userInfo.username; // 根据实际存储字段调整
    this.fetchData();
  },
  methods: {
    fetchData() {
      if (!this.teacherId) return;
      getHomeworkList(this.teacherId).then(res => {
        if (res.success) {
          this.homeworks = res.data;
        }
      });
    },
    
    // --- 批改流程 ---
    openGradingModal(hw) {
      this.currentHomework = hw;
      this.showGradingModal = true;
      this.loadSubmissions(hw.homeworkId);
    },
    loadSubmissions(hwId) {
      getHomeworkSubmissions(hwId).then(res => {
        if (res.success) {
          this.submissionList = res.data;
        }
      });
    },
    closeGradingModal() {
      this.showGradingModal = false;
      this.submissionList = [];
    },
    
    openGradeForm(submission) {
      this.currentSubmission = submission;
      this.gradeForm.score = submission.score || '';
      this.gradeForm.feedback = submission.teacherFeedback || '';
      this.showGradeForm = true;
    },
    closeGradeForm() {
      this.showGradeForm = false;
    },
    
    submitGrade() {
      if (this.gradeForm.score === '') {
        this.$message.warning('请输入分数'); // 假设用了 ElementUI，如果没有请用 alert
        return;
      }
      
      gradeHomework(
        this.currentSubmission.submissionId,
        this.gradeForm.score,
        this.gradeForm.feedback
      ).then(res => {
        if (res.success) {
          this.$message ? this.$message.success('评分成功') : alert('评分成功');
          this.closeGradeForm();
          this.loadSubmissions(this.currentHomework.homeworkId); // 刷新列表
        } else {
          alert(res.message);
        }
      });
    },

    handleDownload(path) {
      downloadHomeworkFile(path); // 浏览器会自动处理下载
    },

    // --- 其他操作 ---
    handleDelete(hw) {
      if (confirm(`确定要删除作业 "${hw.homeworkTitle}" 吗？`)) {
        deleteHomework(hw.homeworkId).then(res => {
          if (res.success) this.fetchData();
        });
      }
    },
    openCreateModal() { this.showCreateModal = true; },
    closeCreateModal() { this.showCreateModal = false; },
    handleSearch() {}, // computed 自动处理
    
    // 工具函数
    formatTime(t) { return t ? t.replace('T', ' ').substring(0, 16) : '' },
    getStatusText(hw) {
      const now = new Date();
      const deadline = new Date(hw.deadline);
      return now > deadline ? '已截止' : '进行中';
    },
    getStatusClass(hw) {
      return this.getStatusText(hw) === '进行中' ? 'badge-green' : 'badge-gray';
    }
  }
}
</script>

<style scoped>
/* 复用之前的 CSS 样式，并增加部分 */
.manage-container { padding: 20px; background: #f5f7fa; min-height: 100vh; }
.action-header { display: flex; justify-content: space-between; margin-bottom: 20px; background: #fff; padding: 20px; border-radius: 8px; }
.title-section h2 { margin: 0; color: #333; }
.subtitle { color: #999; font-size: 13px; margin-top: 5px; }
.operation-section { display: flex; gap: 10px; }
.search-box { display: flex; }
.search-box input { padding: 8px; border: 1px solid #dcdfe6; border-radius: 4px 0 0 4px; }
.btn-search { border: 1px solid #dcdfe6; border-left: none; background: #f5f7fa; padding: 0 12px; cursor: pointer; }
.table-card { background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 15px; text-align: left; border-bottom: 1px solid #ebeef5; }
.data-table th { background: #fafafa; font-weight: 600; color: #606266; }

.status-badge { padding: 2px 8px; border-radius: 10px; font-size: 12px; }
.badge-green { background: #f0f9eb; color: #67c23a; }
.badge-gray { background: #f4f4f5; color: #909399; }

.btn-op { border: none; background: none; cursor: pointer; font-size: 13px; margin-right: 8px; }
.op-primary { color: #409eff; }
.op-text { color: #606266; }
.text-red { color: #f56c6c; }

/* 弹窗样式 */
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 2000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: #fff; border-radius: 8px; width: 500px; max-height: 90vh; display: flex; flex-direction: column; }
.wide-modal-xl { width: 900px; }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-body { padding: 20px; overflow-y: auto; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.close-btn { font-size: 24px; cursor: pointer; color: #999; }

.info-block { background: #f8f9fa; padding: 10px; border-radius: 4px; margin-bottom: 15px; }
.preview-box { white-space: pre-wrap; font-size: 14px; color: #333; margin-top: 5px; }
.input-lg { font-size: 18px; font-weight: bold; width: 100px; padding: 5px; text-align: center; border: 1px solid #dcdfe6; border-radius: 4px; color: #409eff; }
.link-btn { color: #409eff; cursor: pointer; font-size: 13px; text-decoration: underline; }
.status-tag { font-size: 12px; padding: 2px 6px; border-radius: 4px; color: #fff; }
.bg-green { background: #67c23a; }
.bg-orange { background: #e6a23c; }
.text-truncate { max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.btn { padding: 8px 16px; border-radius: 4px; border: none; cursor: pointer; }
.btn-primary { background: #409eff; color: white; }
.btn-secondary { background: #fff; border: 1px solid #dcdfe6; }
</style>