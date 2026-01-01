<template>
  <div class="manage-container">
    <div class="nav-tabs">
      <div class="tab-item" :class="{ active: currentTab === 'all' }" @click="currentTab = 'all'">
        📑 全部作业
      </div>
      <div class="tab-item" :class="{ active: currentTab === 'pending' }" @click="currentTab = 'pending'">
        📝 待提交
        <span v-if="pendingCount > 0" class="badge-count">{{ pendingCount }}</span>
      </div>
      <div class="tab-item" :class="{ active: currentTab === 'graded' }" @click="currentTab = 'graded'">
        ✅ 已批改
      </div>
    </div>

    <div class="action-bar-simple">
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索作业标题或课程名称..."
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
            <th>作业标题</th>
            <th width="160">截止时间</th>
            <th width="100">状态</th>
            <th width="80">成绩</th>
            <th width="140">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="hw in filteredHomeworks" :key="hw.id">
            <td class="course-col">{{ hw.courseName }}</td>
            <td class="title-col">
              {{ hw.title }}
              <span v-if="isNearDeadline(hw.deadline) && hw.status === '未提交'" class="tag-urgent">即将截止</span>
            </td>
            <td class="time-col">{{ hw.deadline }}</td>
            <td>
              <span class="status-badge" :class="getStatusClass(hw.status)">{{ hw.status }}</span>
            </td>
            <td>
              <span v-if="hw.score !== null" class="score-text">{{ hw.score }}</span>
              <span v-else class="text-gray">-</span>
            </td>
            <td>
              <div class="action-col">
                <button 
                  class="btn-op" 
                  :class="getActionBtnClass(hw.status)"
                  @click="openDetailModal(hw)"
                >
                  {{ getActionBtnText(hw.status) }}
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="filteredHomeworks.length === 0">
            <td colspan="6" class="empty-state">
              {{ isLoading ? '加载中...' : '暂无相关作业记录' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="modal-mask" v-if="showModal">
      <div class="modal-box wide-modal">
        <div class="modal-header">
          <h3>
            {{ currentHomework.title }}
            <span class="header-tag">{{ currentHomework.courseName }}</span>
          </h3>
          <span class="close-btn" @click="closeModal">×</span>
        </div>
        
        <div class="modal-body scroll-body">
          <div class="section-block">
            <div class="block-title">📋 作业要求</div>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">截止时间：</span>
                <span class="value text-red">{{ currentHomework.deadline }}</span>
              </div>
              <div class="info-item">
                <span class="label">总分：</span>
                <span class="value">{{ currentHomework.totalScore }} 分</span>
              </div>
            </div>
            <div class="desc-content">{{ currentHomework.description || '暂无描述' }}</div>
            
            <div v-if="currentHomework.attachment" class="attachment-box">
              <span class="icon">📎</span>
              <span class="filename">{{ getFileName(currentHomework.attachment) }}</span>
              <button class="btn-link" @click="downloadFile(currentHomework.attachment)">下载附件</button>
            </div>
          </div>

          <div class="section-block">
            <div class="block-title">
              📤 我的提交
              <span class="status-tag" :class="getStatusClass(currentHomework.status)">
                {{ currentHomework.status }}
              </span>
            </div>

            <div v-if="canSubmit" class="submit-form">
              <div class="form-group">
                <label>文本内容：</label>
                <textarea 
                  v-model="submitForm.content" 
                  rows="5" 
                  placeholder="在此输入作业内容..."
                  class="rich-input"
                ></textarea>
              </div>
              <div class="form-group">
                <label>附件上传：</label>
                <div class="upload-area">
                  <input 
                    type="file" 
                    ref="fileInput" 
                    @change="handleFileChange" 
                    style="display:none"
                  >
                  <input type="text" :value="submitForm.fileName" placeholder="暂无文件" readonly>
                  <button class="btn btn-secondary btn-sm" @click="triggerFileUpload">选择文件</button>
                </div>
              </div>
            </div>

            <div v-else class="submission-view">
              <div v-if="currentHomework.status === '未提交' || currentHomework.status === '已逾期'">
                 <p class="text-gray">您尚未提交作业。</p>
              </div>
              <div v-else>
                <div class="view-item">
                  <span class="label">提交时间：</span>
                  <span>{{ currentHomework.submitTime }}</span>
                </div>
                <div class="view-content" v-if="currentHomework.submissionContent">
                  {{ currentHomework.submissionContent }}
                </div>
                <div v-if="currentHomework.submissionFile" class="attachment-box bg-gray">
                  <span class="icon">📄</span>
                  <span>{{ getFileName(currentHomework.submissionFile) }}</span>
                  <button class="btn-link" @click="downloadFile(currentHomework.submissionFile)">下载</button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="currentHomework.status === '已批改'" class="section-block feedback-block">
            <div class="block-title">👩‍🏫 教师反馈</div>
            <div class="feedback-content">
              <div class="score-display">
                <div class="score-num">{{ currentHomework.score }}</div>
                <div class="score-label">最终得分</div>
              </div>
              <div class="comment-box">
                <p><strong>评语：</strong>{{ currentHomework.feedback || '老师暂无评语' }}</p>
                <p class="time-tip">批改时间：{{ currentHomework.gradeTime }}</p>
              </div>
            </div>
          </div>

        </div>

        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">关闭</button>
          <button 
            v-if="canSubmit" 
            class="btn btn-primary" 
            @click="handleSubmit"
            :disabled="isSubmitting"
          >
            {{ isSubmitting ? '提交中...' : '确认提交' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
// 引入 API
import { getMyHomeworkList, submitHomework, downloadHomeworkFile } from '@/api/homework'

export default {
  name: 'MyHomework',
  data() {
    return {
      currentTab: 'all',
      searchQuery: '',
      showModal: false,
      isSubmitting: false,
      isLoading: false,
      
      allHomeworks: [], // 真实数据
      currentHomework: {}, // 当前选中的作业
      
      submitForm: { 
        content: '', 
        file: null,    // 实际的 File 对象
        fileName: ''   // 显示用的文件名
      }
    }
  },
  computed: {
    filteredHomeworks() {
      let list = this.allHomeworks;
      // 状态筛选
      if (this.currentTab === 'pending') {
        list = list.filter(h => h.status === '未提交');
      } else if (this.currentTab === 'graded') {
        list = list.filter(h => h.status === '已批改');
      }
      // 搜索筛选
      if (this.searchQuery) {
        const q = this.searchQuery.toLowerCase();
        list = list.filter(h => 
          (h.title && h.title.toLowerCase().includes(q)) || 
          (h.courseName && h.courseName.toLowerCase().includes(q))
        );
      }
      return list;
    },
    pendingCount() {
      return this.allHomeworks.filter(h => h.status === '未提交').length;
    },
    canSubmit() {
      // 只有 "未提交" 或 "已提交"(允许重新提交) 且 未过期 才能提交
      // 这里简化逻辑：只要不是 "已逾期" 和 "已批改"，都可以提交
      const status = this.currentHomework.status;
      return status !== '已逾期' && status !== '已批改';
    }
  },
  created() {
    this.fetchData();
  },
  methods: {
    // 获取列表数据
    fetchData() {
      this.isLoading = true;
      getMyHomeworkList().then(res => {
        if (res.success) {
          this.allHomeworks = res.data;
        } else {
          console.error(res.message);
        }
      }).finally(() => {
        this.isLoading = false;
      });
    },

    handleSearch() {
      // computed 自动处理
    },
    
    // 样式辅助方法
    getStatusClass(status) {
      const map = {
        '未提交': 'status-gray',
        '已提交': 'status-blue',
        '已批改': 'status-green',
        '已逾期': 'status-red'
      };
      return map[status] || '';
    },
    getActionBtnClass(status) {
      if (status === '未提交') return 'op-primary'; 
      if (status === '已逾期') return 'op-disabled'; 
      return 'op-text'; 
    },
    getActionBtnText(status) {
      if (status === '未提交') return '去提交';
      if (status === '已提交') return '查看/修改'; // 允许重新提交
      if (status === '已批改') return '查看反馈';
      return '查看详情';
    },
    isNearDeadline(deadlineStr) {
      if(!deadlineStr) return false;
      const now = new Date();
      const deadline = new Date(deadlineStr.replace(/-/g, '/'));
      const diffHours = (deadline - now) / (1000 * 60 * 60);
      return diffHours > 0 && diffHours < 24;
    },
    getFileName(path) {
      if (!path) return '';
      // 如果是路径，只显示文件名
      return path.split('/').pop().split('\\').pop();
    },

    // 弹窗操作
    openDetailModal(hw) {
      this.currentHomework = { ...hw }; // 复制一份
      this.submitForm = { content: '', file: null, fileName: '' };
      
      // 如果已经提交过，回显文本内容（可选，如果后端返回了的话）
      if (hw.status === '已提交') {
         this.submitForm.content = hw.submissionContent || '';
      }
      
      this.showModal = true;
    },
    closeModal() {
      this.showModal = false;
    },

    // 文件上传处理
    triggerFileUpload() {
      this.$refs.fileInput.click();
    },
    handleFileChange(e) {
      const file = e.target.files[0];
      if (file) {
        // 简单限制大小 (例如 10MB)
        if (file.size > 10 * 1024 * 1024) {
          alert("文件大小不能超过 10MB");
          return;
        }
        this.submitForm.file = file;
        this.submitForm.fileName = file.name;
      }
    },

    // 下载文件
    downloadFile(filePath) {
      if (!filePath) return;
      // 调用 API 下载
      downloadHomeworkFile(filePath).then(blob => {
        const url = window.URL.createObjectURL(new Blob([blob]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', this.getFileName(filePath));
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      }).catch(() => {
        alert("下载失败，文件可能不存在");
      });
    },
    
    // 提交作业
    handleSubmit() {
      if (!this.submitForm.content && !this.submitForm.file) {
        return alert('请至少填写内容或上传附件');
      }

      this.isSubmitting = true;

      // 使用 FormData 构建请求体
      const formData = new FormData();
      formData.append('homeworkId', this.currentHomework.id);
      formData.append('content', this.submitForm.content);
      if (this.submitForm.file) {
        formData.append('file', this.submitForm.file);
      }

      submitHomework(formData).then(res => {
        if (res.success) {
          alert('🎉 提交成功！');
          this.closeModal();
          this.fetchData(); // 刷新列表
        } else {
          alert('提交失败：' + res.message);
        }
      }).catch(err => {
        alert('网络错误，请稍后重试');
        console.error(err);
      }).finally(() => {
        this.isSubmitting = false;
      });
    }
  }
}
</script>

<style scoped>
/* 保持原有样式，无需修改 */
/* 请直接复制原有 CSS */
.manage-container { padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background: #f5f7fa; min-height: 100vh; }
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
.table-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; margin: 0 30px; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #fafafa; padding: 15px; color: #606266; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 15px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.data-table tr:hover { background-color: #f9f9f9; }
.course-col { font-weight: 500; color: #333; }
.title-col { color: #303133; font-weight: 500; }
.tag-urgent { background: #fff1f0; color: #f5222d; font-size: 12px; padding: 2px 4px; border-radius: 4px; border: 1px solid #ffa39e; margin-left: 5px; display: inline-block; }
.time-col { font-family: monospace; font-size: 13px; color: #909399; }
.score-text { font-weight: bold; color: #1890ff; font-size: 16px; }
.status-badge { padding: 3px 8px; border-radius: 12px; font-size: 12px; }
.status-gray { background: #f4f4f5; color: #909399; }
.status-blue { background: #e6f7ff; color: #1890ff; }
.status-green { background: #f6ffed; color: #52c41a; }
.status-red { background: #fff1f0; color: #f5222d; }
.action-col { display: flex; gap: 8px; align-items: center; }
.btn-op { border: none; cursor: pointer; font-size: 13px; padding: 5px 12px; border-radius: 4px; transition: all 0.2s; background: transparent; }
.op-primary { background-color: #1890ff; color: white; }
.op-primary:hover { background-color: #40a9ff; }
.op-text { color: #1890ff; background: transparent; padding: 0; }
.op-text:hover { text-decoration: underline; background: transparent; }
.op-disabled { color: #ccc; cursor: not-allowed; padding: 0; }
.empty-state { text-align: center; padding: 40px; color: #999; }
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.15); animation: fade 0.3s; display: flex; flex-direction: column; max-height: 85vh; }
.wide-modal { width: 700px; }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 18px; color: #333; display: flex; align-items: center; gap: 10px; }
.header-tag { font-size: 12px; font-weight: normal; background: #f0f2f5; padding: 2px 8px; border-radius: 10px; color: #666; }
.close-btn { font-size: 24px; cursor: pointer; color: #999; }
.close-btn:hover { color: #333; }
.modal-body { padding: 20px 30px; overflow-y: auto; }
.modal-footer { padding: 15px 30px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.section-block { margin-bottom: 25px; }
.block-title { font-weight: bold; font-size: 15px; color: #333; margin-bottom: 12px; border-left: 4px solid #1890ff; padding-left: 10px; display: flex; justify-content: space-between; align-items: center; }
.info-grid { display: flex; gap: 40px; margin-bottom: 10px; font-size: 14px; }
.info-item .label { color: #909399; }
.desc-content { background: #f9f9f9; padding: 15px; border-radius: 6px; font-size: 14px; line-height: 1.6; color: #333; border: 1px solid #eee; }
.attachment-box { margin-top: 10px; display: flex; align-items: center; gap: 10px; font-size: 13px; }
.attachment-box.bg-gray { background: #f5f7fa; padding: 8px; border-radius: 4px; display: inline-flex; }
.btn-link { color: #1890ff; padding: 0; background: none; border: none; cursor: pointer; }
.btn-link:hover { text-decoration: underline; }
.submit-form { border: 1px dashed #dcdfe6; padding: 20px; border-radius: 6px; background: #fcfcfc; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; font-weight: 500; font-size: 14px; }
.rich-input { width: 100%; padding: 10px; border: 1px solid #dcdfe6; border-radius: 4px; box-sizing: border-box; resize: vertical; font-family: inherit; }
.upload-area { display: flex; gap: 10px; }
.upload-area input { flex: 1; padding: 8px; border: 1px solid #dcdfe6; border-radius: 4px; background: #fff; }
.submission-view { background: #f0f9eb; border: 1px solid #e1f3d8; padding: 15px; border-radius: 6px; }
.view-item { margin-bottom: 8px; font-size: 13px; color: #666; }
.view-content { margin: 10px 0; font-size: 14px; color: #333; }
.feedback-block { background: #fffbf0; border: 1px solid #faecd8; padding: 15px; border-radius: 6px; }
.feedback-content { display: flex; gap: 20px; align-items: flex-start; }
.score-display { text-align: center; min-width: 80px; border-right: 1px solid #faecd8; padding-right: 20px; }
.score-num { font-size: 32px; font-weight: bold; color: #ff9900; line-height: 1; }
.score-label { font-size: 12px; color: #999; margin-top: 5px; }
.comment-box { flex: 1; font-size: 14px; line-height: 1.6; }
.time-tip { font-size: 12px; color: #999; margin-top: 8px; }
.btn { padding: 8px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; }
.btn-primary { background: #1890ff; color: white; }
.btn-primary:hover { background: #40a9ff; }
.btn-secondary { background: white; border: 1px solid #dcdfe6; color: #606266; }
.btn-sm { padding: 6px 12px; font-size: 13px; }
@keyframes fade { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }
</style>