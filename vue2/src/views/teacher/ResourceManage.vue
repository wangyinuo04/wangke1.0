<template>
  <div class="manage-container">
    <div class="action-header">
      <div class="title-section">
        <h2>教学资源管理</h2>
        <p class="subtitle">构建课程章节目录，上传与维护教学课件</p>
      </div>
      <div class="class-selector">
        <select v-model="selectedClassId" @change="selectClass(selectedClassId)" class="class-select">
          <option value="" disabled>请选择教学班</option>
          <option v-for="cls in teacherClasses" :key="cls.id" :value="cls.id">
            {{ cls.className }} ({{ cls.courseName }}) - {{ cls.semester }}
          </option>
        </select>
      </div>
    </div>

    <div class="content-layout">
      <div class="sidebar-card">
        <div class="sidebar-header">
          <h3>章节目录</h3>
          <button class="btn-icon-add" @click="openChapterModal(null)" title="添加根章节">+</button>
        </div>
        <div class="tree-container">
          <ul class="chapter-tree">
            <li v-for="chapter in chapterTree" :key="chapter.id">
              <div 
                class="tree-node" 
                :class="{ active: currentChapterId === chapter.id }"
                @click="selectChapter(chapter)"
              >
                <span class="node-icon">📂</span>
                <span class="node-name">{{ chapter.name }}</span>
                <div class="node-actions">
                  <span @click.stop="openChapterModal(chapter)" title="添加子章节">➕</span>
                  <span @click.stop="editChapter(chapter)" title="编辑">✎</span>
                  <span @click.stop="deleteChapter(chapter.id)" title="删除" class="text-red">×</span>
                </div>
              </div>
              
              <ul v-if="chapter.children && chapter.children.length > 0" class="sub-tree">
                <li v-for="sub in chapter.children" :key="sub.id">
                  <div 
                    class="tree-node sub-node" 
                    :class="{ active: currentChapterId === sub.id }"
                    @click="selectChapter(sub)"
                  >
                    <span class="node-icon">📄</span>
                    <span class="node-name">{{ sub.name }}</span>
                    <div class="node-actions">
                      <span @click.stop="editChapter(sub)" title="编辑">✎</span>
                      <span @click.stop="deleteChapter(sub.id)" title="删除" class="text-red">×</span>
                    </div>
                  </div>
                </li>
              </ul>
            </li>
          </ul>
          <div v-if="chapters.length === 0" class="empty-tree">请先选择教学班</div>
        </div>
      </div>

      <div class="main-card">
        <div class="main-header">
          <div class="header-left">
            <h3>{{ currentChapterName }}</h3>
            <span class="resource-count">共 {{ currentResources.length }} 个文件</span>
          </div>
          <button class="btn btn-primary" @click="openResourceModal" :disabled="!currentChapterId">
            <span class="icon">☁️</span> 上传资源
          </button>
        </div>

        <table class="data-table">
          <thead>
            <tr>
              <th width="60">类型</th>
              <th>资源标题</th>
              <th>文件大小</th>
              <th>允许下载</th>
              <th>上传时间</th>
              <th width="180">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="res in currentResources" :key="res.resourceId">
              <td class="type-icon">{{ getFileIcon(res.resourceType) }}</td>
              <td>
                <div class="res-title">{{ res.resourceName }}</div>
                <div class="res-path">{{ getResourceTypeText(res.resourceType) }}</div>
              </td>
              <td class="size-col">{{ res.formattedSize || res.fileSize }}</td>
              <td>
                <span :class="res.allowDownload ? 'tag-success' : 'tag-warn'">
                  {{ res.allowDownload ? '允许' : '禁止' }}
                </span>
              </td>
              <td class="time-col">{{ formatTime(res.formattedUploadTime || res.uploadTime) }}</td>
              <td>
                <div class="action-col">
                  <button class="btn-text btn-download" @click="downloadResource(res.resourceId, res.resourceName)" 
                          :disabled="!res.allowDownload" title="下载">
                    下载
                  </button>
                  <button class="btn-text btn-edit" @click="editResource(res)">编辑</button>
                  <button class="btn-text btn-danger" @click="deleteResource(res.resourceId)">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="currentResources.length === 0">
              <td colspan="6" class="empty-state">
                {{ currentChapterId ? '该章节暂无资源' : '请先选择左侧章节' }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-mask" v-if="showChapterModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>{{ isEditChapter ? '编辑章节' : '新增章节' }}</h3>
          <span class="close-btn" @click="closeChapterModal">×</span>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveChapter">
            <div class="form-group">
              <label>父级章节</label>
              <input type="text" :value="parentChapterName" disabled class="input-disabled">
            </div>
            <div class="form-group">
              <label>章节名称 <span class="required">*</span></label>
              <input type="text" v-model="chapterForm.chapterName" placeholder="请输入章节名称" required>
            </div>
            <div class="form-group">
              <label>排序号</label>
              <input type="number" v-model.number="chapterForm.sortOrder" placeholder="数字越小越靠前" min="1">
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeChapterModal">取消</button>
              <button type="submit" class="btn btn-primary">保存</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showResourceModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>{{ isEditResource ? '编辑资源属性' : '上传教学资源' }}</h3>
          <span class="close-btn" @click="closeResourceModal">×</span>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveResource">
            <div class="form-group">
              <label>所属章节</label>
              <input type="text" :value="currentChapterName" disabled class="input-disabled">
            </div>
            <div class="form-group">
              <label>资源标题 <span class="required">*</span></label>
              <input type="text" v-model="resourceForm.resourceName" placeholder="请输入资源显示标题" required>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>资源类型 <span class="required">*</span></label>
                <select v-model="resourceForm.resourceType" required>
                  <option value="PPT">PPT 演示文稿</option>
                  <option value="PDF">PDF 文档</option>
                  <option value="视频">教学视频</option>
                  <option value="Word">Word 文档</option>
                  <option value="音频">音频文件</option>
                </select>
              </div>
              <div class="form-group">
                <label>允许下载</label>
                <select v-model="resourceForm.allowDownload">
                  <option :value="true">允许学生下载</option>
                  <option :value="false">仅在线预览</option>
                </select>
              </div>
            </div>

            <div class="form-group">
              <label>附件文件 <span class="required" v-if="!isEditResource">*</span></label>
              <div class="file-upload-area">
                <!-- 编辑模式：显示现有文件信息 -->
                <div v-if="isEditResource && resourceForm.filePath" class="file-info">
                  <div class="file-info-item">
                    <span class="file-icon">📄</span>
                    <div class="file-details">
                      <div class="file-name">{{ resourceForm.resourceName }}</div>
                      <div class="file-meta">{{ resourceForm.fileSize }} · {{ resourceForm.filePath }}</div>
                    </div>
                  </div>
                  <p class="hint">注：编辑模式下不能修改文件，如需更换文件请删除后重新上传。</p>
                </div>
                
                <!-- 新增模式：显示文件上传框 -->
                <div v-else class="file-upload-box">
                  <input 
                    type="file" 
                    @change="handleFileSelect" 
                    :disabled="uploading" 
                    accept=".pdf,.ppt,.pptx,.doc,.docx,.txt,.mp4,.avi,.mov,.mp3,.wav,.jpg,.jpeg,.png"
                    ref="fileInput"
                  >
                  <div v-if="resourceForm.file" class="selected-file">
                    <span class="file-icon">📎</span>
                    <span class="file-name">{{ resourceForm.file.name }}</span>
                    <span class="file-size">({{ resourceForm.fileSize }})</span>
                  </div>
                </div>
              </div>
              <p class="hint" v-if="!isEditResource">支持 PDF, PPT, Word, 视频, 音频等格式，最大100MB</p>
            </div>

            <div v-if="uploading" class="upload-progress">
              <div class="progress-bar">
                <div class="progress" :style="{ width: uploadProgress + '%' }"></div>
              </div>
              <span>上传中... {{ uploadProgress }}%</span>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeResourceModal" :disabled="uploading">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="uploading">
                {{ uploading ? '上传中...' : (isEditResource ? '保存修改' : '开始上传') }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { 
  getChapterTree, 
  addChapter, 
  updateChapter, 
  deleteChapter,
  getResourcesByChapter,
  uploadResource,
  updateResource,
  deleteResource,
  downloadResource,
  getTeacherClasses
} from '@/api/resource'
import { Message, MessageBox } from 'element-ui'

export default {
  name: 'ResourceManage',
  data() {
    return {
      // --- 章节数据 ---
      currentChapterId: null,
      currentClassId: null, // 当前教学班ID
      showChapterModal: false,
      isEditChapter: false,
      chapterForm: { 
        chapterId: null, 
        chapterName: '', 
        parentChapterId: null, 
        sortOrder: 1,
        classId: null 
      },
      chapters: [], // 从后端获取
      teacherClasses: [], // 教师的教学班列表
      selectedClassId: '', // 下拉框选中的教学班ID

      // --- 资源数据 ---
      showResourceModal: false,
      isEditResource: false,
      resourceForm: { 
        resourceId: null, 
        resourceName: '', 
        resourceType: 'PPT', 
        file: null,
        filePath: '', 
        fileSize: null, 
        allowDownload: true,
        chapterId: null
      },
      allResources: [], // 从后端获取
      uploading: false, // 上传状态
      uploadProgress: 0 // 上传进度
    }
  },
  computed: {
    // 构建章节树（支持二级）
    chapterTree() {
      return this.chapters; // 现在直接从后端获取树形结构
    },
    // 当前选中章节名称
    currentChapterName() {
      const findChapter = (tree, id) => {
        for (let chapter of tree) {
          if (chapter.id === id) {
            return chapter.name;
          }
          if (chapter.children && chapter.children.length > 0) {
            const found = findChapter(chapter.children, id);
            if (found) return found;
          }
        }
        return null;
      };
      return findChapter(this.chapters, this.currentChapterId) || '未选择章节';
    },
    // 弹窗中显示的父章节名称
    parentChapterName() {
      const findChapter = (tree, id) => {
        for (let chapter of tree) {
          if (chapter.id === id) {
            return chapter.name;
          }
          if (chapter.children && chapter.children.length > 0) {
            const found = findChapter(chapter.children, id);
            if (found) return found;
          }
        }
        return null;
      };
      return this.chapterForm.parentChapterId 
        ? (findChapter(this.chapters, this.chapterForm.parentChapterId) || '未知') 
        : '根目录 (无父级)';
    },
    // 当前章节下的资源
    currentResources() {
      return this.allResources;
    }
  },
  mounted() {
    this.loadTeacherClasses();
  },
  methods: {
    // --- 加载数据 ---
    async loadTeacherClasses() {
      try {
        // 从localStorage获取教师信息
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        if (userInfo.role === 'teacher' && userInfo.teacherId) {
          const response = await getTeacherClasses(userInfo.teacherId);
          if (response.success) {
            this.teacherClasses = response.data || [];
            if (this.teacherClasses.length > 0) {
              // 默认选择第一个教学班
              this.selectedClassId = this.teacherClasses[0].id;
              this.loadChapters(this.selectedClassId);
            }
          }
        }
      } catch (error) {
        console.error('加载教学班列表失败:', error);
        Message.error('加载教学班列表失败');
      }
    },

    // 选择教学班
    async selectClass(classId) {
      this.selectedClassId = classId;
      this.currentChapterId = null;
      this.allResources = [];
      await this.loadChapters(classId);
    },

    // 加载章节树
    async loadChapters(classId) {
      try {
        const response = await getChapterTree(classId);
        if (response.success) {
          this.chapters = response.data || [];
          // 默认选中第一个章节
          if (this.chapters.length > 0) {
            this.selectChapter(this.chapters[0]);
          }
        } else {
          Message.error(response.message || '加载章节失败');
        }
      } catch (error) {
        console.error('加载章节失败:', error);
        Message.error('加载章节失败');
      }
    },

    // --- 章节操作 ---
    async selectChapter(chapter) {
      this.currentChapterId = chapter.id;
      await this.loadResources(chapter.id);
    },

    async openChapterModal(parent) {
      this.isEditChapter = false;
      this.chapterForm = {
        chapterId: null,
        chapterName: '',
        parentChapterId: parent ? parent.id : null,
        sortOrder: 1,
        classId: this.selectedClassId
      };
      this.showChapterModal = true;
    },

    async editChapter(chapter) {
      this.isEditChapter = true;
      try {
        this.chapterForm = {
          chapterId: chapter.id,
          chapterName: chapter.name,
          parentChapterId: chapter.parentId,
          sortOrder: chapter.sortOrder || 1,
          classId: this.selectedClassId
        };
        this.showChapterModal = true;
      } catch (error) {
        console.error('编辑章节失败:', error);
        Message.error('编辑章节失败');
      }
    },

    closeChapterModal() {
      this.showChapterModal = false;
      this.chapterForm = {
        chapterId: null,
        chapterName: '',
        parentChapterId: null,
        sortOrder: 1,
        classId: null
      };
    },

    async saveChapter() {
      if (!this.chapterForm.chapterName.trim()) {
        Message.error('请输入章节名称');
        return;
      }

      try {
        if (this.isEditChapter) {
          const response = await updateChapter(this.chapterForm);
          if (response.success) {
            Message.success('更新章节成功');
            await this.loadChapters(this.selectedClassId);
          } else {
            Message.error(response.message || '更新章节失败');
          }
        } else {
          const response = await addChapter(this.chapterForm);
          if (response.success) {
            Message.success('添加章节成功');
            await this.loadChapters(this.selectedClassId);
          } else {
            Message.error(response.message || '添加章节失败');
          }
        }
        this.closeChapterModal();
      } catch (error) {
        console.error('保存章节失败:', error);
        Message.error('保存章节失败');
      }
    },

    async deleteChapter(id) {
      try {
        await MessageBox.confirm('确定删除该章节吗？如果有子章节或资源将一并删除！', '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });

        const response = await deleteChapter(id);
        if (response.success) {
          Message.success('删除章节成功');
          // 重新加载章节
          await this.loadChapters(this.selectedClassId);
          if (this.currentChapterId === id) {
            this.currentChapterId = null;
            this.allResources = [];
          }
        } else {
          Message.error(response.message || '删除章节失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除章节失败:', error);
          Message.error('删除章节失败');
        }
      }
    },

    // --- 资源操作 ---
    getFileIcon(type) {
      const map = { 
        'PPT': '📊', 
        'PDF': '📄', 
        '视频': '🎬', 
        'Word': '📝', 
        '音频': '🎵',
        'PDF文档': '📄',
        'PPT演示文稿': '📊',
        '教学视频': '🎬',
        'Word文档': '📝',
        '音频文件': '🎵'
      };
      return map[type] || '📎';
    },

    openResourceModal() {
      if (!this.currentChapterId) {
        Message.warning('请先选择章节');
        return;
      }
      
      this.isEditResource = false;
      this.resourceForm = { 
        resourceId: null, 
        resourceName: '', 
        resourceType: 'PPT', 
        file: null,
        filePath: '', 
        fileSize: null, 
        allowDownload: true,
        chapterId: this.currentChapterId
      };
      this.showResourceModal = true;
    },

    handleFileSelect(event) {
      const file = event.target.files[0];
      if (!file) return;

      // 检查文件大小 (限制为100MB)
      const maxSize = 100 * 1024 * 1024; // 100MB
      if (file.size > maxSize) {
        Message.error('文件大小不能超过100MB');
        event.target.value = '';
        return;
      }

      // 检查文件类型
      const allowedTypes = [
        'application/pdf',
        'application/vnd.ms-powerpoint',
        'application/vnd.openxmlformats-officedocument.presentationml.presentation',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'video/mp4',
        'video/avi',
        'video/quicktime',
        'audio/mpeg',
        'audio/wav',
        'text/plain',
        'image/jpeg',
        'image/png'
      ];

      if (!allowedTypes.includes(file.type) && !file.name.match(/\.(pdf|ppt|pptx|doc|docx|txt|mp4|avi|mov|mp3|wav|jpg|jpeg|png)$/i)) {
        Message.error('不支持的文件类型');
        event.target.value = '';
        return;
      }

      this.resourceForm.file = file;
      this.resourceForm.resourceName = file.name;
      
      // 设置文件大小显示
      let sizeStr = '';
      if (file.size < 1024) {
        sizeStr = file.size + ' B';
      } else if (file.size < 1024 * 1024) {
        sizeStr = (file.size / 1024).toFixed(1) + ' KB';
      } else if (file.size < 1024 * 1024 * 1024) {
        sizeStr = (file.size / (1024 * 1024)).toFixed(1) + ' MB';
      } else {
        sizeStr = (file.size / (1024 * 1024 * 1024)).toFixed(1) + ' GB';
      }
      this.resourceForm.fileSize = sizeStr;

      // 根据文件类型自动设置资源类型
      if (file.name.match(/\.(ppt|pptx)$/i)) {
        this.resourceForm.resourceType = 'PPT';
      } else if (file.name.match(/\.(pdf)$/i)) {
        this.resourceForm.resourceType = 'PDF';
      } else if (file.name.match(/\.(doc|docx)$/i)) {
        this.resourceForm.resourceType = 'Word';
      } else if (file.name.match(/\.(mp4|avi|mov)$/i)) {
        this.resourceForm.resourceType = '视频';
      } else if (file.name.match(/\.(mp3|wav)$/i)) {
        this.resourceForm.resourceType = '音频';
      }
    },

    async editResource(res) {
      this.isEditResource = true;
      this.resourceForm = {
        resourceId: res.resourceId,
        resourceName: res.resourceName,
        resourceType: res.resourceType,
        file: null,
        filePath: res.filePath,
        fileSize: res.formattedSize || res.fileSize,
        allowDownload: res.allowDownload,
        chapterId: this.currentChapterId
      };
      this.showResourceModal = true;
    },

    closeResourceModal() {
      this.showResourceModal = false;
      this.resourceForm = { 
        resourceId: null, 
        resourceName: '', 
        resourceType: 'PPT', 
        file: null,
        filePath: '', 
        fileSize: null, 
        allowDownload: true,
        chapterId: null
      };
      this.uploading = false;
      this.uploadProgress = 0;
    },

    async saveResource() {
      if (!this.resourceForm.resourceName.trim()) {
        Message.error('请输入资源标题');
        return;
      }

      if (this.isEditResource) {
        // 编辑资源
        try {
          const response = await updateResource({
            resourceId: this.resourceForm.resourceId,
            resourceName: this.resourceForm.resourceName,
            allowDownload: this.resourceForm.allowDownload
          });
          
          if (response.success) {
            Message.success('更新资源成功');
            await this.loadResources(this.currentChapterId);
            this.closeResourceModal();
          } else {
            Message.error(response.message || '更新资源失败');
          }
        } catch (error) {
          console.error('更新资源失败:', error);
          Message.error('更新资源失败');
        }
      } else {
        // 上传新资源
        if (!this.resourceForm.file) {
          Message.error('请选择要上传的文件');
          return;
        }

        this.uploading = true;
        try {
          const formData = new FormData();
          formData.append('file', this.resourceForm.file);
          formData.append('resourceName', this.resourceForm.resourceName);
          formData.append('resourceType', this.resourceForm.resourceType);
          formData.append('chapterId', this.resourceForm.chapterId);
          formData.append('allowDownload', this.resourceForm.allowDownload);

          const response = await uploadResource(formData);
          
          if (response.success) {
            Message.success('资源上传成功');
            await this.loadResources(this.currentChapterId);
            this.closeResourceModal();
          } else {
            Message.error(response.message || '资源上传失败');
          }
        } catch (error) {
          console.error('上传资源失败:', error);
          Message.error('上传资源失败');
        } finally {
          this.uploading = false;
        }
      }
    },

    async deleteResource(resourceId) {
      try {
        await MessageBox.confirm('确定删除该资源吗？', '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });

        const response = await deleteResource(resourceId);
        if (response.success) {
          Message.success('删除资源成功');
          await this.loadResources(this.currentChapterId);
        } else {
          Message.error(response.message || '删除资源失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除资源失败:', error);
          Message.error('删除资源失败');
        }
      }
    },

    // 下载资源
    async downloadResource(resourceId, resourceName) {
      try {
        const response = await downloadResource(resourceId);
        
        // 创建blob对象
        const blob = new Blob([response]);
        
        // 创建下载链接
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = resourceName || 'resource';
        document.body.appendChild(link);
        link.click();
        
        // 清理
        window.URL.revokeObjectURL(url);
        document.body.removeChild(link);
        
        Message.success('开始下载');
      } catch (error) {
        console.error('下载资源失败:', error);
        Message.error('下载资源失败');
      }
    },

    // 加载资源列表
    async loadResources(chapterId) {
      if (!chapterId) return;
      
      try {
        const response = await getResourcesByChapter(chapterId);
        if (response.success) {
          this.allResources = response.data || [];
        } else {
          Message.error(response.message || '加载资源失败');
          this.allResources = [];
        }
      } catch (error) {
        console.error('加载资源失败:', error);
        this.allResources = [];
        Message.error('加载资源失败');
      }
    },

    // 格式化时间
    formatTime(time) {
      if (!time) return '';
      return time.length > 10 ? time.substring(0, 10) : time;
    },

    // 获取资源类型显示文本
    getResourceTypeText(type) {
      const map = {
        'PPT': 'PPT演示文稿',
        'PDF': 'PDF文档',
        '视频': '教学视频',
        'Word': 'Word文档',
        '音频': '音频文件'
      };
      return map[type] || type;
    }
  }
}
</script>

<style scoped>
/* 基础容器 */
.manage-container { padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; height: 100%; display: flex; flex-direction: column; }

/* 头部 */
.action-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.title-section h2 { margin: 0; font-size: 20px; color: #333; }
.subtitle { margin: 5px 0 0; font-size: 13px; color: #999; }

/* 左右布局容器 */
.content-layout { display: flex; gap: 20px; flex: 1; align-items: flex-start; }

/* 左侧边栏 */
.sidebar-card { width: 300px; background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); display: flex; flex-direction: column; min-height: 500px; }
.sidebar-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.sidebar-header h3 { margin: 0; font-size: 16px; color: #333; }
.btn-icon-add { background: #1890ff; color: white; border: none; width: 24px; height: 24px; border-radius: 4px; cursor: pointer; display: flex; justify-content: center; align-items: center; font-size: 18px; line-height: 1; }
.btn-icon-add:hover { background: #40a9ff; }

.tree-container { padding: 10px; flex: 1; }
.chapter-tree, .sub-tree { list-style: none; padding: 0; margin: 0; }
.sub-tree { padding-left: 20px; border-left: 1px solid #f0f0f0; margin-left: 10px; }

.tree-node { display: flex; align-items: center; padding: 10px; border-radius: 4px; cursor: pointer; transition: background 0.2s; position: relative; }
.tree-node:hover { background: #f5f7fa; }
.tree-node.active { background: #e6f7ff; color: #1890ff; font-weight: 500; }
.tree-node.active .node-icon { opacity: 1; }

.node-icon { margin-right: 8px; font-size: 16px; opacity: 0.7; }
.node-name { flex: 1; font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.node-actions { display: none; margin-left: 5px; }
.tree-node:hover .node-actions { display: flex; gap: 6px; }
.node-actions span { font-size: 14px; color: #909399; padding: 2px; }
.node-actions span:hover { color: #1890ff; }
.node-actions span.text-red:hover { color: #f5222d; }

.empty-tree { text-align: center; color: #ccc; margin-top: 50px; font-size: 13px; }

/* 右侧主内容 */
.main-card { flex: 1; background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); min-height: 500px; padding-bottom: 20px; }
.main-header { padding: 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.header-left h3 { margin: 0 0 5px; font-size: 18px; color: #333; }
.resource-count { font-size: 13px; color: #909399; }

/* 表格样式 */
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { background: #fafafa; padding: 16px; text-align: left; color: #333; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 16px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.data-table tr:hover { background-color: #f5f7fa; }

.type-icon { font-size: 24px; text-align: center; min-width: 50px; }
.res-title { font-weight: 500; color: #333; margin-bottom: 2px; }
.res-path { font-size: 12px; color: #999; font-family: monospace; }
.size-col { font-family: monospace; }
.time-col { color: #909399; font-size: 13px; }

.tag-success { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; padding: 2px 8px; border-radius: 12px; font-size: 12px; }
.tag-warn { background: #fff1f0; color: #f5222d; border: 1px solid #ffa39e; padding: 2px 8px; border-radius: 12px; font-size: 12px; }

/* 操作按钮 - 修改为与教师管理页面一致 */
.action-col { display: flex; gap: 8px; }
.btn-text { background: none; border: none; cursor: pointer; font-size: 13px; padding: 0 4px; }
.btn-edit { color: #1890ff; } .btn-edit:hover { text-decoration: underline; }
.btn-download { color: #52c41a; } .btn-download:hover { text-decoration: underline; }
.btn-danger { color: #f5222d; } .btn-danger:hover { text-decoration: underline; }
.btn-download:disabled { color: #ccc; cursor: not-allowed; }

.empty-state { text-align: center; padding: 60px; color: #ccc; font-style: italic; }

/* 通用按钮 */
.btn { padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; transition: all 0.3s; display: inline-flex; align-items: center; }
.btn .icon { margin-right: 6px; }
.btn-primary { background: #1890ff; color: white; }
.btn-primary:hover { background: #40a9ff; }
.btn-primary:disabled { background: #a0cfff; cursor: not-allowed; }
.btn-secondary { background: white; border: 1px solid #dcdfe6; color: #606266; }
.btn-secondary:hover { color: #1890ff; border-color: #c6e2ff; background: #ecf5ff; }
.btn-sm { padding: 4px 10px; font-size: 12px; }

/* 弹窗样式 */
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; width: 500px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: modalFadeIn 0.3s ease; }
@keyframes modalFadeIn { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 16px; color: #333; }
.close-btn { font-size: 20px; cursor: pointer; color: #999; }
.close-btn:hover { color: #333; }
.modal-body { padding: 20px; }

.form-group { margin-bottom: 15px; }
.form-row { display: flex; gap: 15px; }
.form-group label { display: block; margin-bottom: 8px; font-size: 13px; font-weight: 500; color: #606266; }
.required { color: #f56c6c; margin-left: 2px; }
.form-group input, .form-group select { width: 100%; padding: 8px 10px; border: 1px solid #dcdfe6; border-radius: 4px; box-sizing: border-box; font-size: 14px; }
.form-group input:focus, .form-group select:focus { border-color: #1890ff; outline: none; }
.input-disabled { background: #f5f7fa; color: #909399; cursor: not-allowed; }

/* 文件上传区域样式 */
.file-upload-area { width: 100%; }
.file-upload-box { width: 100%; }
.file-upload-box input[type="file"] { width: 100%; padding: 8px; border: 1px solid #dcdfe6; border-radius: 4px; box-sizing: border-box; }

/* 文件信息显示 */
.file-info { padding: 12px; background: #f5f7fa; border-radius: 4px; border: 1px solid #e9e9eb; }
.file-info-item { display: flex; align-items: flex-start; gap: 10px; }
.file-icon { font-size: 24px; color: #606266; }
.file-details { flex: 1; }
.file-name { font-weight: 500; color: #333; margin-bottom: 4px; }
.file-meta { font-size: 12px; color: #909399; }

/* 已选择的文件样式 */
.selected-file { margin-top: 8px; padding: 8px 12px; background: #f5f7fa; border-radius: 4px; border: 1px solid #e9e9eb; display: flex; align-items: center; gap: 8px; }
.selected-file .file-icon { font-size: 16px; }
.selected-file .file-name { flex: 1; font-size: 13px; color: #606266; }
.selected-file .file-size { font-size: 12px; color: #909399; }

.hint { font-size: 12px; color: #909399; margin-top: 5px; line-height: 1.4; }

.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }

/* 教学班选择器样式 */
.class-selector {
  margin-left: auto;
}

.class-select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  color: #606266;
  font-size: 14px;
  min-width: 300px;
}

.class-select:focus {
  outline: none;
  border-color: #1890ff;
}

/* 上传进度条样式 */
.upload-progress {
  width: 100%;
  margin-bottom: 15px;
}

.progress-bar {
  height: 6px;
  background: #e8e8e8;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 5px;
}

.progress {
  height: 100%;
  background: #1890ff;
  transition: width 0.3s;
}
</style>