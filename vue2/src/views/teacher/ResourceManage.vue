<template>
  <div class="manage-container">
    <div class="action-header">
      <div class="title-section">
        <h2>教学资源管理</h2>
        <p class="subtitle">构建课程章节目录，上传与维护教学课件</p>
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
          <div v-if="chapters.length === 0" class="empty-tree">暂无章节，请添加</div>
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
              <th width="150">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="res in currentResources" :key="res.id">
              <td class="type-icon">{{ getFileIcon(res.type) }}</td>
              <td>
                <div class="res-title">{{ res.title }}</div>
                <div class="res-path">{{ res.filePath }}</div>
              </td>
              <td class="size-col">{{ res.size }}</td>
              <td>
                <span :class="res.allowDownload ? 'tag-success' : 'tag-warn'">
                  {{ res.allowDownload ? '允许' : '禁止' }}
                </span>
              </td>
              <td class="time-col">{{ res.uploadTime }}</td>
              <td class="action-col">
                <button class="btn-text btn-edit" @click="editResource(res)">编辑</button>
                <button class="btn-text btn-danger" @click="deleteResource(res.id)">删除</button>
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
              <input type="text" v-model="chapterForm.name" placeholder="请输入章节名称" required>
            </div>
            <div class="form-group">
              <label>排序号</label>
              <input type="number" v-model.number="chapterForm.sortOrder" placeholder="数字越小越靠前">
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
              <input type="text" v-model="resourceForm.title" placeholder="请输入资源显示标题" required>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label>资源类型 <span class="required">*</span></label>
                <select v-model="resourceForm.type" required>
                  <option value="PPT">PPT 演示文稿</option>
                  <option value="PDF">PDF 文档</option>
                  <option value="Video">Video 教学视频</option>
                  <option value="Word">Word 文档</option>
                  <option value="Audio">Audio 音频</option>
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
              <label>附件文件 <span class="required">*</span></label>
              <div class="file-upload-box">
                <input type="text" v-model="resourceForm.filePath" placeholder="选择文件 (模拟路径)" readonly>
                <button type="button" class="btn btn-secondary btn-sm" @click="mockSelectFile">选择文件</button>
              </div>
              <p class="hint" v-if="resourceForm.size">文件大小: {{ resourceForm.size }}</p>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeResourceModal">取消</button>
              <button type="submit" class="btn btn-primary">
                {{ isEditResource ? '保存修改' : '开始上传' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'ResourceManage',
  data() {
    return {
      // --- 章节数据 ---
      currentChapterId: null,
      showChapterModal: false,
      isEditChapter: false,
      chapterForm: { id: null, name: '', parentId: null, sortOrder: 1 },
      chapters: [
        { id: 1, name: '第一章：软件工程概述', parentId: null, sortOrder: 1 },
        { id: 2, name: '1.1 软件危机与软件工程', parentId: 1, sortOrder: 1 },
        { id: 3, name: '1.2 软件生命周期模型', parentId: 1, sortOrder: 2 },
        { id: 4, name: '第二章：可行性研究', parentId: null, sortOrder: 2 }
      ],

      // --- 资源数据 ---
      showResourceModal: false,
      isEditResource: false,
      resourceForm: { id: null, title: '', type: 'PPT', filePath: '', size: '', allowDownload: true },
      allResources: [
        { id: 101, chapterId: 2, title: '第一章教学课件.pptx', type: 'PPT', filePath: '/uploads/ch1_slides.pptx', size: '2.5MB', allowDownload: true, uploadTime: '2025-08-20' },
        { id: 102, chapterId: 2, title: '软件危机案例分析.pdf', type: 'PDF', filePath: '/uploads/case_study.pdf', size: '1.2MB', allowDownload: false, uploadTime: '2025-08-21' },
        { id: 103, chapterId: 3, title: '瀑布模型讲解视频.mp4', type: 'Video', filePath: '/uploads/waterfall_model.mp4', size: '45MB', allowDownload: true, uploadTime: '2025-08-25' }
      ]
    }
  },
  computed: {
    // 构建章节树（支持二级）
    chapterTree() {
      const roots = this.chapters.filter(c => !c.parentId).sort((a,b) => a.sortOrder - b.sortOrder);
      return roots.map(root => {
        const children = this.chapters.filter(c => c.parentId === root.id).sort((a,b) => a.sortOrder - b.sortOrder);
        return { ...root, children };
      });
    },
    // 当前选中章节名称
    currentChapterName() {
      const ch = this.chapters.find(c => c.id === this.currentChapterId);
      return ch ? ch.name : '未选择章节';
    },
    // 弹窗中显示的父章节名称
    parentChapterName() {
      if (this.chapterForm.parentId) {
        const p = this.chapters.find(c => c.id === this.chapterForm.parentId);
        return p ? p.name : '未知';
      }
      return '根目录 (无父级)';
    },
    // 当前章节下的资源
    currentResources() {
      if (!this.currentChapterId) return [];
      return this.allResources.filter(r => r.chapterId === this.currentChapterId);
    }
  },
  methods: {
    // --- 章节操作 ---
    selectChapter(chapter) {
      this.currentChapterId = chapter.id;
    },
    openChapterModal(parent) {
      this.isEditChapter = false;
      this.chapterForm = {
        id: Date.now(),
        name: '',
        parentId: parent ? parent.id : null, // 传入父节点对象则添加子节点，否则添加根节点
        sortOrder: 1
      };
      this.showChapterModal = true;
    },
    editChapter(chapter) {
      this.isEditChapter = true;
      this.chapterForm = { ...chapter };
      this.showChapterModal = true;
    },
    closeChapterModal() {
      this.showChapterModal = false;
    },
    saveChapter() {
      if (this.isEditChapter) {
        const idx = this.chapters.findIndex(c => c.id === this.chapterForm.id);
        if (idx !== -1) this.chapters.splice(idx, 1, this.chapterForm);
      } else {
        this.chapters.push(this.chapterForm);
      }
      this.closeChapterModal();
    },
    deleteChapter(id) {
      if (confirm('确定删除该章节吗？如果有子章节或资源将一并删除！')) {
        // 级联删除子章节
        const idsToDelete = [id];
        this.chapters.forEach(c => {
          if (c.parentId === id) idsToDelete.push(c.id);
        });
        
        this.chapters = this.chapters.filter(c => !idsToDelete.includes(c.id));
        // 删除关联资源
        this.allResources = this.allResources.filter(r => !idsToDelete.includes(r.chapterId));
        
        if (this.currentChapterId === id) this.currentChapterId = null;
      }
    },

    // --- 资源操作 ---
    getFileIcon(type) {
      const map = { 'PPT': '📊', 'PDF': '📄', 'Video': '🎬', 'Word': '📝', 'Audio': '🎵' };
      return map[type] || '📎';
    },
    openResourceModal() {
      this.isEditResource = false;
      this.resourceForm = { 
        id: Date.now(), 
        title: '', 
        type: 'PPT', 
        filePath: '', 
        size: '', 
        allowDownload: true 
      };
      this.showResourceModal = true;
    },
    mockSelectFile() {
      // 模拟文件选择
      const types = ['demo.pptx', 'guide.pdf', 'intro.mp4'];
      const random = types[Math.floor(Math.random() * types.length)];
      this.resourceForm.filePath = `C:\\fakepath\\${random}`;
      this.resourceForm.size = (Math.random() * 10 + 1).toFixed(1) + ' MB';
      // 自动填入标题
      if (!this.resourceForm.title) this.resourceForm.title = random.split('.')[0];
    },
    editResource(res) {
      this.isEditResource = true;
      this.resourceForm = { ...res };
      this.showResourceModal = true;
    },
    closeResourceModal() {
      this.showResourceModal = false;
    },
    saveResource() {
      if (!this.resourceForm.filePath) return alert('请先上传文件');
      
      const resourceData = {
        ...this.resourceForm,
        chapterId: this.currentChapterId,
        uploadTime: this.isEditResource ? this.resourceForm.uploadTime : new Date().toISOString().split('T')[0]
      };

      if (this.isEditResource) {
        const idx = this.allResources.findIndex(r => r.id === resourceData.id);
        if (idx !== -1) this.allResources.splice(idx, 1, resourceData);
      } else {
        this.allResources.push(resourceData);
      }
      this.closeResourceModal();
    },
    deleteResource(id) {
      if (confirm('确定删除该资源吗？')) {
        this.allResources = this.allResources.filter(r => r.id !== id);
      }
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
.data-table th { background: #fafafa; padding: 15px; text-align: left; color: #606266; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 15px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.data-table tr:hover { background-color: #f9f9f9; }

.type-icon { font-size: 24px; text-align: center; }
.res-title { font-weight: 500; color: #333; margin-bottom: 2px; }
.res-path { font-size: 12px; color: #999; font-family: monospace; }
.size-col { font-family: monospace; }
.time-col { color: #909399; font-size: 13px; }

.tag-success { color: #52c41a; background: #f6ffed; padding: 2px 6px; border-radius: 4px; font-size: 12px; border: 1px solid #b7eb8f; }
.tag-warn { color: #fa8c16; background: #fff7e6; padding: 2px 6px; border-radius: 4px; font-size: 12px; border: 1px solid #ffd591; }

.action-col { display: flex; gap: 10px; }
.btn-text { background: none; border: none; cursor: pointer; font-size: 13px; padding: 0; }
.btn-edit { color: #1890ff; } .btn-edit:hover { text-decoration: underline; }
.btn-danger { color: #f5222d; } .btn-danger:hover { text-decoration: underline; }

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

.file-upload-box { display: flex; gap: 10px; }
.file-upload-box input { flex: 1; cursor: pointer; background: #fff; }
.hint { font-size: 12px; color: #909399; margin-top: 5px; }

.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
</style>