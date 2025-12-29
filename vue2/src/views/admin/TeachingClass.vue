<template>
  <div class="manage-container">
    <div class="action-header">
      <div class="title-section">
        <h2>排课与权限分配</h2>
        <p class="subtitle">开设教学班级并分配学生选课权限</p>
      </div>
      <div class="operation-section">
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="搜索教学班名称..."
            @keyup.enter="handleSearch"
          >
          <button class="btn btn-search" @click="handleSearch">🔍 搜索</button>
        </div>
        <button class="btn btn-primary" @click="openClassModal">+ 开设教学班</button>
      </div>
    </div>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th width="120">教学班ID</th>
            <th width="120">开课学期</th>
            <th>教学班名称</th>
            <th>关联课程</th>
            <th>授课教师</th>
            <th width="150">选课人数</th>
            <th width="220">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cls in teachingClasses" :key="cls.classId">
            <td><span class="code-tag">{{ cls.classId }}</span></td>
            <td><span class="term-tag">{{ cls.semester }}</span></td>
            <td class="name-col">{{ cls.className }}</td>
            <td>
              <div class="course-info">
                <span class="code">{{ cls.courseId }}</span>
                <span>{{ getCourseName(cls.courseId) }}</span>
              </div>
            </td>
            <td>{{ getTeacherName(cls.teacherId) }}</td>
            <td>
              <div class="capacity-box">
                <span :class="{'text-red': currentStudentCounts[cls.classId] >= cls.maxStudents}">
                  {{ currentStudentCounts[cls.classId] || 0 }} / {{ cls.maxStudents }}
                </span>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ 
                    width: Math.min((currentStudentCounts[cls.classId] || 0) / cls.maxStudents * 100, 100) + '%' 
                  }"></div>
                </div>
              </div>
            </td>
            <td>
              <div class="action-col">
                <button class="btn-text btn-edit" @click="openStudentModal(cls)" title="分配学生">
                  👥 管理学生
                </button>
                
                <button class="btn-text btn-info" @click="openEditClassModal(cls)" title="修改设置">
                  设置
                </button>

                <button class="btn-text btn-danger" @click="deleteClass(cls)" title="解散班级">
                  解散
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="teachingClasses.length === 0">
            <td colspan="7" class="empty-state">暂无排课记录，请点击右上角开设新班级</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="modal-mask" v-if="showClassModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>{{ isEditMode ? '修改排课信息' : '开设新教学班' }}</h3>
          <span class="close-btn" @click="closeClassModal">×</span>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveClass">
            <div class="form-row">
              <div class="form-group">
                <label>基础课程 <span class="required">*</span></label>
                <select v-model="classForm.courseId" :disabled="isEditMode" required>
                  <option value="" disabled>请选择课程</option>
                  <option v-for="c in courseList" :key="c.courseId" :value="c.courseId">
                    {{ c.courseName }} ({{ c.courseId }})
                  </option>
                </select>
              </div>
              <div class="form-group">
                <label>授课教师 <span class="required">*</span></label>
                <select v-model="classForm.teacherId" required>
                  <option value="" disabled>请选择教师</option>
                  <option v-for="t in teacherList" :key="t.teacherId" :value="t.teacherId">
                    {{ t.name }} ({{ t.teacherId }})
                  </option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>开课学期 <span class="required">*</span></label>
                <select v-model="classForm.semester" required>
                  <option>2025-2026-1</option>
                  <option>2025-2026-2</option>
                  <option>2024-2025-1</option>
                  <option>2024-2025-2</option>
                  <option>2023-2024-1</option>
                  <option>2023-2024-2</option>
                </select>
              </div>
              <div class="form-group">
                <label>最大人数 <span class="required">*</span></label>
                <input type="number" v-model.number="classForm.maxStudents" min="1" max="200" required>
              </div>
            </div>

            <div class="form-group">
              <label>教学班名称 <span class="required">*</span></label>
              <input type="text" v-model="classForm.className" placeholder="例: 软件工程-计算机2班" required>
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" @click="closeClassModal">取消</button>
              <button type="submit" class="btn btn-primary" :disabled="loading">
                {{ loading ? '保存中...' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showStudentModal">
      <div class="modal-box wide-modal">
        <div class="modal-header">
          <h3>管理班级成员 - {{ currentClass.className }}</h3>
          <span class="close-btn" @click="closeStudentModal">×</span>
        </div>
        <div class="modal-body student-manage-body">
          
          <div class="panel-left">
            <div class="tool-card">
              <h4>单人分配学生</h4>
              <div class="input-row">
                <input 
                  type="text" 
                  v-model="singleStudentId" 
                  placeholder="输入学生学号..." 
                  @keyup.enter="addSingleStudent"
                  :disabled="loading"
                >
                <button 
                  class="btn btn-sm btn-primary" 
                  @click="addSingleStudent"
                  :disabled="loading || !singleStudentId"
                >
                  {{ loading ? '添加中...' : '添加' }}
                </button>
              </div>
              <p class="hint">输入学号将该学生强制加入班级。</p>
            </div>

            <div class="tool-card">
              <h4>按行政班导入</h4>
              <div class="input-row">
                <input 
                  type="text" 
                  v-model="targetAdminClass" 
                  placeholder="输入行政班级(如软件2201)..." 
                  @keyup.enter="searchStudentsByClass"
                  :disabled="loading"
                >
                <button 
                  class="btn btn-sm btn-success" 
                  @click="searchStudentsByClass"
                  :disabled="loading || !targetAdminClass"
                >
                  {{ loading ? '查询中...' : '查询' }}
                </button>
              </div>
              <p class="hint">系统将自动查找该行政班级下的所有学生。</p>
              
              <div v-if="searchedStudents.length > 0" class="search-results">
                <div class="search-header">
                  <label class="checkbox-container">
                    <input 
                      type="checkbox" 
                      v-model="selectAllStudents" 
                      @change="toggleSelectAllStudents"
                    >
                    <span class="checkmark"></span>
                    全选
                  </label>
                  <span class="count">找到 {{ searchedStudents.length }} 名学生</span>
                </div>
                <div class="student-list-container">
                  <div 
                    v-for="student in searchedStudents" 
                    :key="student.studentId" 
                    class="student-item"
                  >
                    <label class="checkbox-container">
                      <input 
                        type="checkbox" 
                        :value="student.studentId" 
                        v-model="selectedStudentIds"
                      >
                      <span class="checkmark"></span>
                    </label>
                    <span class="student-name">{{ student.name }}</span>
                    <span class="student-id">{{ student.studentId }}</span>
                    <span class="student-class">{{ student.className }}</span>
                  </div>
                </div>
                <div class="batch-actions">
                  <button 
                    class="btn btn-sm btn-primary" 
                    @click="batchImportSelected"
                    :disabled="loading || selectedStudentIds.length === 0"
                  >
                    {{ loading ? '导入中...' : `批量导入(${selectedStudentIds.length})` }}
                  </button>
                  <button 
                    class="btn btn-sm btn-secondary" 
                    @click="clearSearchResults"
                    :disabled="loading"
                  >
                    清空
                  </button>
                </div>
              </div>
            </div>
            
            <div class="stat-info">
              当前人数: <strong>{{ currentClassStudents.length }}</strong> / {{ currentClass.maxStudents }}
              <div v-if="currentAdmin" class="admin-info">
                操作管理员: {{ currentAdmin }}
              </div>
            </div>
          </div>

          <div class="panel-right">
            <div class="panel-header">
              <h4>已选课学生名单</h4>
              <button 
                class="btn btn-sm btn-refresh" 
                @click="refreshClassStudents"
                :disabled="loading"
                title="刷新列表"
              >
                🔄
              </button>
            </div>
            <ul class="student-list">
              <li v-for="(student, index) in currentClassStudents" :key="student.studentId">
                <span class="s-index">{{ index + 1 }}.</span>
                <span class="s-info">
                  <span class="s-name">{{ student.name }}</span>
                  <span class="s-id">({{ student.studentId }})</span>
                  <span class="s-class">{{ student.className }}</span>
                </span>
                <span class="remove-icon" @click="removeStudent(student.studentId)" title="移除">×</span>
              </li>
              <li v-if="currentClassStudents.length === 0" class="empty-list">暂无学生</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { getCourseList } from '@/api/course'
import { getTeacherList } from '@/api/teacher'
import { getStudentList } from '@/api/student'
import { 
  getTeachingClassList, 
  addTeachingClass, 
  updateTeachingClass, 
  deleteTeachingClass,
  enrollSingleStudent,
  enrollBatchStudents,
  removeStudentFromClass,
  getClassStudents,
  searchStudentsByAdminClass
} from '@/api/teachingClass'

export default {
  name: 'TeachingClass',
  data() {
    return {
      searchQuery: '',
      showClassModal: false,
      showStudentModal: false,
      isEditMode: false,
      loading: false,
      
      // 数据列表
      courseList: [],
      teacherList: [],
      studentList: [],
      teachingClasses: [],
      
      // 表单模型
      classForm: {
        classId: '',
        courseId: '',
        teacherId: '',
        semester: '2025-2026-1',
        className: '',
        maxStudents: 50
      },
      
      // 学生管理相关
      currentClass: null,
      currentClassStudents: [],
      singleStudentId: '',
      targetAdminClass: '',
      searchedStudents: [],
      selectedStudentIds: [],
      selectAllStudents: false,
      currentStudentCounts: {},
      
      // 当前管理员信息
      currentAdmin: ''
    }
  },
  created() {
    this.loadAllData()
    this.getCurrentAdmin()
  },
  methods: {
    // 获取当前管理员信息
    getCurrentAdmin() {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr)
          if (userInfo.role === 'admin' && userInfo.account) {
            this.currentAdmin = userInfo.account
          }
        } catch (e) {
          console.error('解析用户信息失败:', e)
          this.currentAdmin = 'admin001'
        }
      } else {
        this.currentAdmin = 'admin001'
      }
    },

    // 加载所有数据
    async loadAllData() {
      this.loading = true
      try {
        await Promise.all([
          this.loadCourses(),
          this.loadTeachers(),
          this.loadTeachingClasses()
        ])
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },

    // 加载课程列表
    async loadCourses() {
      try {
        const response = await getCourseList()
        if (response.success) {
          this.courseList = response.data
        } else {
          this.$message.error(response.message)
        }
      } catch (error) {
        console.error('加载课程失败:', error)
        this.$message.error('加载课程失败')
      }
    },

    // 加载教师列表
    async loadTeachers() {
      try {
        const response = await getTeacherList()
        if (response.success) {
          this.teacherList = response.data
        } else {
          this.$message.error(response.message)
        }
      } catch (error) {
        console.error('加载教师失败:', error)
        this.$message.error('加载教师失败')
      }
    },

    // 加载教学班列表
    async loadTeachingClasses() {
      try {
        const response = await getTeachingClassList(this.searchQuery)
        if (response.success) {
          this.teachingClasses = response.data
          // 为每个教学班加载学生数量
          this.loadStudentCounts()
        } else {
          this.$message.error(response.message)
        }
      } catch (error) {
        console.error('加载教学班失败:', error)
        this.$message.error('加载教学班失败')
      }
    },

    // 加载各班级学生数量
    async loadStudentCounts() {
      for (const cls of this.teachingClasses) {
        try {
          const response = await getClassStudents(cls.classId)
          if (response.success) {
            this.$set(this.currentStudentCounts, cls.classId, response.data.length)
          }
        } catch (error) {
          console.error(`加载班级${cls.classId}学生数量失败:`, error)
          this.$set(this.currentStudentCounts, cls.classId, 0)
        }
      }
    },

    // 辅助方法：获取课程名称
    getCourseName(courseId) {
      const course = this.courseList.find(c => c.courseId === courseId)
      return course ? course.courseName : courseId
    },

    // 辅助方法：获取教师名称
    getTeacherName(teacherId) {
      const teacher = this.teacherList.find(t => t.teacherId === teacherId)
      return teacher ? teacher.name : teacherId
    },

    handleSearch() {
      this.loadTeachingClasses()
    },

    // --- 1.5.1 开设/编辑教学班 ---
    openClassModal() {
      this.isEditMode = false
      this.classForm = {
        classId: '',
        courseId: '',
        teacherId: '',
        semester: '2025-2026-1',
        className: '',
        maxStudents: 50
      }
      this.showClassModal = true
    },

    openEditClassModal(cls) {
      this.isEditMode = true
      this.classForm = {
        classId: cls.classId,
        courseId: cls.courseId,
        teacherId: cls.teacherId,
        semester: cls.semester,
        className: cls.className,
        maxStudents: cls.maxStudents
      }
      this.showClassModal = true
    },

    closeClassModal() {
      this.showClassModal = false
    },

    async saveClass() {
      if (this.loading) return
      
      this.loading = true
      try {
        if (this.isEditMode) {
          // 更新教学班
          const response = await updateTeachingClass(this.classForm)
          if (response.success) {
            this.$message.success('修改成功')
            await this.loadTeachingClasses()
            this.closeClassModal()
          } else {
            this.$message.error(response.message)
          }
        } else {
          // 开设新教学班
          const response = await addTeachingClass(this.classForm)
          if (response.success) {
            this.$message.success('开课成功')
            await this.loadTeachingClasses()
            this.closeClassModal()
          } else {
            this.$message.error(response.message)
          }
        }
      } catch (error) {
        console.error('保存教学班失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.loading = false
      }
    },

    async deleteClass(cls) {
      if (!confirm(`确定要解散班级 "${cls.className}" 吗？\n这将移除所有已选课学生的关联！`)) {
        return
      }

      this.loading = true
      try {
        const response = await deleteTeachingClass(cls.classId)
        if (response.success) {
          this.$message.success('解散成功')
          await this.loadTeachingClasses()
        } else {
          this.$message.error(response.message)
        }
      } catch (error) {
        console.error('解散班级失败:', error)
        this.$message.error('解散失败')
      } finally {
        this.loading = false
      }
    },

    // --- 学生分配管理 ---
    async openStudentModal(cls) {
      this.currentClass = cls
      this.singleStudentId = ''
      this.targetAdminClass = ''
      this.searchedStudents = []
      this.selectedStudentIds = []
      this.selectAllStudents = false
      
      // 加载该班级的学生列表
      await this.loadClassStudents()
      this.showStudentModal = true
    },

    closeStudentModal() {
      this.showStudentModal = false
      this.currentClass = null
      this.currentClassStudents = []
    },

    // 加载班级学生列表
    async loadClassStudents() {
      if (!this.currentClass) return
      
      this.loading = true
      try {
        const response = await getClassStudents(this.currentClass.classId)
        if (response.success) {
          // 获取学生详细信息
          const studentIds = response.data
          if (studentIds.length > 0) {
            const studentResponse = await getStudentList()
            if (studentResponse.success) {
              this.currentClassStudents = studentResponse.data.filter(student => 
                studentIds.includes(student.studentId)
              )
            }
          } else {
            this.currentClassStudents = []
          }
          this.$set(this.currentStudentCounts, this.currentClass.classId, studentIds.length)
        } else {
          this.$message.error(response.message)
        }
      } catch (error) {
        console.error('加载班级学生失败:', error)
        this.$message.error('加载学生列表失败')
      } finally {
        this.loading = false
      }
    },

    // 刷新班级学生列表
    async refreshClassStudents() {
      await this.loadClassStudents()
    },

    // 1.5.2 单人分配
    async addSingleStudent() {
      if (!this.singleStudentId || this.loading) return
      
      // 验证输入
      if (!this.singleStudentId.trim()) {
        this.$message.warning('请输入学号')
        return
      }

      this.loading = true
      try {
        // 先检查学生是否存在
        const studentResponse = await getStudentList(this.singleStudentId)
        if (!studentResponse.success || studentResponse.data.length === 0) {
          this.$message.error('找不到该学号的学生')
          return
        }

        const response = await enrollSingleStudent(this.currentClass.classId, this.singleStudentId)
        if (response.success) {
          this.$message.success('添加学生成功')
          this.singleStudentId = ''
          await this.loadClassStudents()
        } else {
          this.$message.error(response.message)
        }
      } catch (error) {
        console.error('添加学生失败:', error)
        this.$message.error('添加失败')
      } finally {
        this.loading = false
      }
    },

    // 搜索行政班级的学生
    async searchStudentsByClass() {
      if (!this.targetAdminClass || this.loading) return
      
      this.loading = true
      try {
        const response = await searchStudentsByAdminClass(this.targetAdminClass)
        if (response.success) {
          this.searchedStudents = response.data
          // 过滤掉已经在班级中的学生
          this.searchedStudents = this.searchedStudents.filter(student => 
            !this.currentClassStudents.some(s => s.studentId === student.studentId)
          )
          
          if (this.searchedStudents.length === 0) {
            this.$message.warning(`未找到行政班级 "${this.targetAdminClass}" 的可用学生`)
          } else {
            this.$message.success(`找到 ${this.searchedStudents.length} 名学生`)
            this.selectedStudentIds = []
            this.selectAllStudents = false
          }
        } else {
          this.$message.error(response.message)
        }
      } catch (error) {
        console.error('搜索学生失败:', error)
        this.$message.error('搜索失败')
      } finally {
        this.loading = false
      }
    },

    // 全选/取消全选学生
    toggleSelectAllStudents() {
      if (this.selectAllStudents) {
        this.selectedStudentIds = this.searchedStudents.map(student => student.studentId)
      } else {
        this.selectedStudentIds = []
      }
    },

    // 批量导入选中的学生
    async batchImportSelected() {
      if (this.selectedStudentIds.length === 0 || this.loading) return
      
      this.loading = true
      try {
        const response = await enrollBatchStudents(this.currentClass.classId, this.selectedStudentIds)
        if (response.success) {
          this.$message.success(response.message)
          await this.loadClassStudents()
          this.clearSearchResults()
        } else {
          this.$message.error(response.message)
        }
      } catch (error) {
        console.error('批量导入失败:', error)
        this.$message.error('批量导入失败')
      } finally {
        this.loading = false
      }
    },

    // 清空搜索结果
    clearSearchResults() {
      this.searchedStudents = []
      this.selectedStudentIds = []
      this.selectAllStudents = false
      this.targetAdminClass = ''
    },

    // 从班级中移除学生
    async removeStudent(studentId) {
      if (!confirm('确定将该学生移出班级吗？')) return
      
      this.loading = true
      try {
        const response = await removeStudentFromClass(this.currentClass.classId, studentId)
        if (response.success) {
          this.$message.success('移除学生成功')
          await this.loadClassStudents()
        } else {
          this.$message.error(response.message)
        }
      } catch (error) {
        console.error('移除学生失败:', error)
        this.$message.error('移除失败')
      } finally {
        this.loading = false
      }
    }
  },
  watch: {
    // 监听选中的学生变化，更新全选状态
    selectedStudentIds(newVal) {
      this.selectAllStudents = newVal.length === this.searchedStudents.length && this.searchedStudents.length > 0
    }
  }
}
</script>

<style scoped>
/* 样式复用 */
.manage-container { padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; }
.action-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.title-section h2 { margin: 0; font-size: 20px; color: #333; }
.subtitle { margin: 5px 0 0; font-size: 13px; color: #999; }
.operation-section { display: flex; gap: 15px; }

/* 搜索 & 按钮 */
.search-box { display: flex; }
.search-box input { padding: 8px 12px; border: 1px solid #dcdfe6; border-right: none; border-radius: 4px 0 0 4px; outline: none; font-size: 14px; width: 200px; }
.search-box input:focus { border-color: #1890ff; }
.btn-search { border-radius: 0 4px 4px 0; background: #f5f7fa; color: #606266; border: 1px solid #dcdfe6; border-left: none; cursor: pointer; }
.btn-search:hover { background: #e6f7ff; color: #1890ff; }
.btn { padding: 8px 16px; border: none; cursor: pointer; font-size: 14px; border-radius: 4px; transition: all 0.3s; }
.btn-sm { padding: 6px 12px; font-size: 12px; }
.btn-primary { background: #1890ff; color: white; box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3); }
.btn-primary:hover:not(:disabled) { background: #40a9ff; }
.btn-primary:disabled { background: #bae7ff; cursor: not-allowed; }
.btn-success { background: #52c41a; color: white; }
.btn-success:hover:not(:disabled) { background: #73d13d; }
.btn-success:disabled { background: #d9f7be; cursor: not-allowed; }
.btn-secondary { background: #fff; border: 1px solid #dcdfe6; color: #606266; }
.btn-secondary:hover:not(:disabled) { background: #f5f5f5; }
.btn-secondary:disabled { background: #fafafa; cursor: not-allowed; }

/* 表格 */
.table-card { background: #fff; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { background: #fafafa; padding: 16px; color: #333; font-weight: 600; border-bottom: 1px solid #ebeef5; }
.data-table td { padding: 16px; border-bottom: 1px solid #ebeef5; color: #606266; font-size: 14px; vertical-align: middle; }
.data-table tr:hover { background-color: #f5f7fa; }

/* 表格内元素 */
.code-tag { background: #f6ffed; color: #389e0d; border: 1px solid #b7eb8f; padding: 2px 6px; border-radius: 4px; font-size: 12px; font-family: monospace; }
.term-tag { background: #f0f5ff; color: #2f54eb; border: 1px solid #adc6ff; padding: 2px 6px; border-radius: 4px; font-size: 12px; font-family: monospace; }
.name-col { font-weight: 500; color: #333; }
.course-info { display: flex; flex-direction: column; font-size: 13px; }
.course-info .code { font-weight: bold; color: #909399; font-size: 12px; }

/* 容量进度条 */
.capacity-box { width: 100px; font-size: 12px; text-align: center; }
.text-red { color: #f5222d; font-weight: bold; }
.progress-bar { width: 100%; height: 6px; background: #f5f5f5; border-radius: 3px; margin-top: 4px; overflow: hidden; }
.progress-fill { height: 100%; background: #52c41a; transition: width 0.3s; }
.text-red ~ .progress-bar .progress-fill { background: #f5222d; } /* 满员变红 */

.action-col { display: flex; gap: 8px; }
.btn-text { background: none; border: none; cursor: pointer; font-size: 13px; padding: 0; }
.btn-text:disabled { color: #ccc; cursor: not-allowed; }
.btn-edit { color: #1890ff; }
.btn-edit:hover:not(:disabled) { text-decoration: underline; }
.btn-info { color: #909399; }
.btn-info:hover:not(:disabled) { text-decoration: underline; }
.btn-danger { color: #f5222d; }
.btn-danger:hover:not(:disabled) { text-decoration: underline; }
.empty-state { text-align: center; padding: 40px; color: #999; }

/* 弹窗通用 */
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; width: 550px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: modalFadeIn 0.3s ease; display: flex; flex-direction: column; max-height: 90vh; }
.wide-modal { width: 900px; height: 700px; } /* 学生管理弹窗更宽更高 */
@keyframes modalFadeIn { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; flex-shrink: 0; }
.modal-header h3 { margin: 0; font-size: 16px; color: #333; }
.close-btn { font-size: 20px; cursor: pointer; color: #999; }
.close-btn:hover { color: #666; }
.modal-body { padding: 20px; overflow-y: auto; flex: 1; }

/* 表单样式 */
.form-row { display: flex; gap: 15px; }
.form-group { margin-bottom: 15px; flex: 1; }
.form-group label { display: block; margin-bottom: 8px; font-size: 13px; font-weight: 500; color: #606266; }
.required { color: #f56c6c; margin-left: 2px; }
.form-group input, .form-group select { width: 100%; padding: 8px 10px; border: 1px solid #dcdfe6; border-radius: 4px; box-sizing: border-box; font-size: 14px; }
.form-group input:disabled, .form-group select:disabled { background: #f5f5f5; cursor: not-allowed; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; margin-top: auto; }

/* --- 学生管理弹窗专用样式 --- */
.student-manage-body { display: flex; gap: 20px; height: 100%; padding: 0; }
.panel-left { width: 380px; border-right: 1px solid #eee; padding: 20px; background: #f9fafc; overflow-y: auto; }
.panel-right { flex: 1; padding: 20px; overflow-y: auto; display: flex; flex-direction: column; }

.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.panel-header h4 { margin: 0; font-size: 14px; color: #333; border-left: 3px solid #1890ff; padding-left: 8px; }
.btn-refresh { background: none; border: 1px solid #dcdfe6; padding: 4px 8px; border-radius: 4px; cursor: pointer; }
.btn-refresh:hover:not(:disabled) { background: #f5f5f5; }
.btn-refresh:disabled { color: #ccc; cursor: not-allowed; }

.tool-card { background: #fff; border: 1px solid #e4e7ed; border-radius: 4px; padding: 15px; margin-bottom: 15px; box-shadow: 0 1px 4px rgba(0,0,0,0.03); }
.tool-card h4 { margin: 0 0 10px; font-size: 14px; color: #333; border-left: 3px solid #1890ff; padding-left: 8px; }
.input-row { display: flex; gap: 5px; margin-bottom: 8px; }
.input-row input { flex: 1; padding: 6px; border: 1px solid #dcdfe6; border-radius: 3px; font-size: 13px; }
.input-row input:disabled { background: #f5f5f5; }
.hint { font-size: 12px; color: #999; margin: 5px 0 0; }

/* 搜索结果显示 */
.search-results { margin-top: 15px; border: 1px solid #e8e8e8; border-radius: 4px; overflow: hidden; }
.search-header { display: flex; justify-content: space-between; align-items: center; padding: 10px; background: #fafafa; border-bottom: 1px solid #e8e8e8; }
.search-header .count { font-size: 12px; color: #666; }
.student-list-container { max-height: 250px; overflow-y: auto; }
.student-item { display: flex; align-items: center; padding: 8px 10px; border-bottom: 1px solid #f0f0f0; }
.student-item:hover { background: #f5f7fa; }
.student-name { flex: 1; font-size: 13px; font-weight: 500; }
.student-id { width: 100px; font-size: 12px; color: #666; text-align: right; }
.student-class { width: 80px; font-size: 12px; color: #999; text-align: right; }
.batch-actions { padding: 10px; background: #fafafa; border-top: 1px solid #e8e8e8; display: flex; gap: 8px; justify-content: flex-end; }

/* 复选框样式 */
.checkbox-container { display: flex; align-items: center; cursor: pointer; font-size: 12px; }
.checkbox-container input { position: absolute; opacity: 0; cursor: pointer; }
.checkmark { width: 16px; height: 16px; margin-right: 8px; background-color: #fff; border: 1px solid #dcdfe6; border-radius: 3px; display: inline-block; position: relative; }
.checkbox-container input:checked ~ .checkmark { background-color: #1890ff; border-color: #1890ff; }
.checkmark:after { content: ""; position: absolute; display: none; left: 5px; top: 2px; width: 4px; height: 8px; border: solid white; border-width: 0 2px 2px 0; transform: rotate(45deg); }
.checkbox-container input:checked ~ .checkmark:after { display: block; }

.stat-info { margin-top: 20px; padding: 15px; background: #fff; border: 1px solid #e8e8e8; border-radius: 4px; text-align: center; font-size: 14px; color: #606266; }
.stat-info strong { color: #1890ff; font-size: 18px; }
.admin-info { margin-top: 8px; padding-top: 8px; border-top: 1px dashed #e8e8e8; font-size: 12px; color: #52c41a; }

/* 已选课学生列表 */
.student-list { list-style: none; padding: 0; margin: 0; flex: 1; overflow-y: auto; }
.student-list li { display: flex; align-items: center; padding: 12px; border-bottom: 1px solid #f0f0f0; transition: background 0.2s; }
.student-list li:hover { background: #f5f7fa; }
.s-index { width: 30px; color: #999; font-size: 12px; }
.s-info { flex: 1; display: flex; align-items: center; }
.s-name { font-weight: 500; color: #333; margin-right: 8px; }
.s-id { color: #999; font-weight: normal; font-size: 13px; margin-right: 12px; }
.s-class { color: #666; font-size: 12px; background: #f0f0f0; padding: 1px 6px; border-radius: 3px; }
.remove-icon { color: #ff4d4f; cursor: pointer; font-size: 18px; font-weight: bold; padding: 0 5px; }
.remove-icon:hover { background: #fff1f0; border-radius: 4px; }
.empty-list { color: #ccc; text-align: center; padding: 40px; font-style: italic; }
</style>