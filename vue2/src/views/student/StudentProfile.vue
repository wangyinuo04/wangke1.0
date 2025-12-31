<template>
  <div class="profile-page">
    <div class="profile-banner">
      <div class="banner-bg"></div>
      <div class="banner-content">
        <h2 class="page-title">学生个人中心</h2>
        <p class="page-subtitle">My Learning Profile & Settings</p>
      </div>
      
      <button class="back-btn" @click="goBack">
        <span class="icon">↩</span> 返回学习中心
      </button>
    </div>

    <div class="main-content">
      <div class="content-wrapper">
        
        <div class="info-card">
          <div class="card-body">
            <div class="avatar-wrapper" @click="triggerFileInput">
              <div class="avatar-circle">
                <img v-if="studentInfo.avatar" :src="studentInfo.avatar" class="avatar-img">
                <span v-else class="avatar-text">{{ studentInfo.name ? studentInfo.name[0] : '学' }}</span>
                
                <div class="avatar-overlay">
                  <span>📷 更换</span>
                </div>
              </div>
              <div class="status-dot" title="账号状态: 正常"></div>
            </div>
            <input type="file" ref="fileInput" accept="image/*" style="display:none" @change="handleAvatarChange">

            <h3 class="user-name">{{ studentInfo.name }}</h3>
            <span class="user-badge">{{ studentInfo.studentId }}</span>
            
            <div class="divider"></div>
            
            <div class="side-info-list">
              <div class="info-row">
                <span class="label">行政班级</span>
                <span class="val">{{ studentInfo.className }}</span>
              </div>
              <div class="info-row">
                <span class="label">入学年份</span>
                <span class="val">{{ studentInfo.enrollmentYear }}级</span>
              </div>
              <div class="info-row">
                <span class="label">所属院系</span>
                <span class="val">{{ studentInfo.department }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="details-card">
          
          <div class="stats-header">
            <div class="stat-item">
              <div class="stat-icon color-blue">📚</div>
              <div class="stat-info">
                <div class="num">{{ stats.courses }}</div>
                <div class="desc">在修课程</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon color-green">🏆</div>
              <div class="stat-info">
                <div class="num">{{ stats.credits }}</div>
                <div class="desc">已修学分</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon color-orange">🔥</div>
              <div class="stat-info">
                <div class="num">{{ stats.gpa }}</div>
                <div class="desc">平均绩点</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon color-purple">📊</div>
              <div class="stat-info">
                <div class="num">第 {{ stats.rank }} 名</div>
                <div class="desc">年级排名</div>
              </div>
            </div>
          </div>

          <div class="form-section">
            <div class="section-title">
              <h3>📝 档案资料设置</h3>
            </div>

            <form @submit.prevent="saveProfile">
              <div class="form-row">
                <div class="form-group">
                  <label>专业方向</label>
                  <input type="text" v-model="studentInfo.major" disabled class="input-disabled">
                </div>
                <div class="form-group">
                  <label>性别</label>
                  <select v-model="studentInfo.gender" disabled class="input-disabled">
                    <option value="男">男</option>
                    <option value="女">女</option>
                  </select>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group">
                  <label>手机号码 <span class="tag-edit">可修改</span></label>
                  <input type="text" v-model="form.phone" placeholder="请输入手机号">
                </div>
                <div class="form-group">
                  <label>电子邮箱 <span class="tag-edit">可修改</span></label>
                  <input type="email" v-model="form.email" placeholder="请输入常用邮箱">
                </div>
              </div>

              <div class="form-actions">
                <button type="submit" class="btn-save" :disabled="loading">
                  {{ loading ? '保存中...' : '保存更改' }}
                </button>
                <button type="button" class="btn-link" @click="openPasswordModal">
                  修改登录密码
                </button>
              </div>
            </form>
          </div>
        </div>

      </div>
    </div>

    <div class="modal-mask" v-if="showPasswordModal">
      <div class="modal-box">
        <div class="modal-header">
          <h3>修改密码</h3>
          <span class="close-btn" @click="showPasswordModal = false">×</span>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>旧密码</label>
            <input type="password" v-model="passwordForm.old" placeholder="请输入当前正在使用的密码">
          </div>
          
          <div class="form-group">
            <label>新密码</label>
            <input type="password" v-model="passwordForm.new" placeholder="6-16位新密码">
          </div>
          <div class="form-group">
            <label>确认密码</label>
            <input type="password" v-model="passwordForm.confirm" placeholder="再次输入">
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showPasswordModal = false">取消</button>
          <button class="btn btn-primary" @click="updatePassword">确认修改</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'StudentProfile',
  data() {
    return {
      loading: false,
      showPasswordModal: false,
      
      // 学生基础信息
      studentInfo: {
        studentId: 'S2023001',
        name: '张三',
        gender: '男',
        department: '计算机学院',
        major: '软件工程',
        className: '软件2301班',
        enrollmentYear: 2023,
        avatar: ''
      },
      
      // 学习统计数据
      stats: {
        courses: 5,
        credits: 12.5,
        gpa: 3.8,
        rank: 12
      },

      // 表单编辑数据
      form: {
        phone: '13800138000',
        email: 'zhangsan@edu.cn'
      },

      // 密码表单 (修改点：增加 old 字段)
      passwordForm: { old: '', new: '', confirm: '' }
    }
  },
  mounted() {
    this.loadLocalData();
  },
  methods: {
    goBack() {
      this.$router.push('/student/home');
    },

    loadLocalData() {
      const userStr = localStorage.getItem('userInfo');
      if (userStr) {
        const user = JSON.parse(userStr);
        if (user.role === 'student') {
          this.studentInfo.name = user.name || this.studentInfo.name;
          this.studentInfo.studentId = user.account || this.studentInfo.studentId;
        }
      }
    },

    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    handleAvatarChange(e) {
      const file = e.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (evt) => {
          this.studentInfo.avatar = evt.target.result;
          this.$message ? this.$message.success('头像更新成功') : alert('头像更新成功');
        };
        reader.readAsDataURL(file);
      }
    },

    saveProfile() {
      this.loading = true;
      setTimeout(() => {
        this.loading = false;
        alert('个人资料保存成功！');
      }, 800);
    },

    // 打开密码弹窗时重置表单
    openPasswordModal() {
      this.passwordForm = { old: '', new: '', confirm: '' };
      this.showPasswordModal = true;
    },

    updatePassword() {
      // 修改点：增加旧密码校验
      if (!this.passwordForm.old) {
        return alert('请输入旧密码');
      }
      
      // 这里应该调用后端接口验证旧密码是否正确，这里仅做模拟
      // 假设旧密码是 '123456' (模拟校验)
      // if (this.passwordForm.old !== '123456') {
      //   return alert('旧密码输入错误');
      // }

      if (!this.passwordForm.new || this.passwordForm.new.length < 6) {
        return alert('新密码长度至少6位');
      }
      
      if (this.passwordForm.old === this.passwordForm.new) {
        return alert('新密码不能与旧密码相同');
      }

      if (this.passwordForm.new !== this.passwordForm.confirm) {
        return alert('两次输入的新密码不一致');
      }

      alert('密码修改成功，请重新登录');
      this.showPasswordModal = false;
      // 实际场景可能需要跳转登录页
      // this.$router.push('/login');
    }
  }
}
</script>

<style scoped>
/* 继承 AdminProfile 的全局容器设置 */
.profile-page {
  background-color: #f5f7fa;
  height: 84vh;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
  display: flex;
  flex-direction: column;
}

/* --- Banner --- */
.profile-banner {
  height: 200px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  position: relative;
  display: flex;
  align-items: center;
  padding: 0 40px;
  color: white;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}
.banner-bg {
  position: absolute; width: 100%; height: 100%; top: 0; left: 0;
  background-image: radial-gradient(circle at 10% 50%, rgba(255,255,255,0.2) 0%, transparent 20%);
  pointer-events: none;
}
.banner-content { position: relative; z-index: 2; margin-left: 20px; }
.page-title { margin: 0; font-size: 28px; font-weight: bold; }
.page-subtitle { margin: 5px 0 0; opacity: 0.9; font-size: 14px; }

.back-btn {
  position: absolute; top: 30px; left: 30px;
  background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.4);
  color: white; padding: 8px 18px; border-radius: 20px;
  cursor: pointer; display: flex; align-items: center; backdrop-filter: blur(5px);
  transition: all 0.3s;
}
.back-btn:hover { background: rgba(255,255,255,0.3); transform: translateX(-3px); }

/* --- 主布局 --- */
.main-content {
  flex: 1;
  padding: 0 40px 40px;
  margin-top: -50px;
  position: relative;
  z-index: 3;
}
.content-wrapper { display: flex; gap: 25px; align-items: flex-start; }

/* 左侧：身份卡 */
.info-card { width: 320px; flex-shrink: 0; background: white; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.card-body { padding: 40px 30px; display: flex; flex-direction: column; align-items: center; }

/* 头像交互 */
.avatar-wrapper { position: relative; cursor: pointer; margin-bottom: 15px; }
.avatar-circle {
  width: 100px; height: 100px; background: #40a9ff; border-radius: 50%;
  display: flex; justify-content: center; align-items: center;
  border: 4px solid white; box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  overflow: hidden; position: relative;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-text { font-size: 32px; color: white; font-weight: bold; }
.avatar-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.5); color: white; display: flex; justify-content: center; align-items: center;
  opacity: 0; transition: opacity 0.3s; font-size: 12px;
}
.avatar-wrapper:hover .avatar-overlay { opacity: 1; }
.status-dot {
  position: absolute; bottom: 5px; right: 5px; width: 18px; height: 18px;
  background: #52c41a; border: 3px solid white; border-radius: 50%;
}

.user-name { margin: 10px 0 5px; font-size: 20px; color: #333; }
.user-badge { background: #e6f7ff; color: #1890ff; padding: 2px 10px; border-radius: 10px; font-size: 12px; font-family: monospace; }

.divider { width: 100%; height: 1px; background: #eee; margin: 25px 0; }

.side-info-list { width: 100%; }
.info-row { display: flex; justify-content: space-between; margin-bottom: 15px; font-size: 14px; }
.info-row .label { color: #909399; }
.info-row .val { color: #333; font-weight: 500; }

/* 右侧：详细卡片 */
.details-card { flex: 1; background: white; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); display: flex; flex-direction: column; }

/* 统计栏 */
.stats-header {
  display: flex; border-bottom: 1px solid #f0f0f0; padding: 25px 40px;
  justify-content: space-between;
}
.stat-item { display: flex; align-items: center; gap: 15px; }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.color-blue { background: #e6f7ff; color: #1890ff; }
.color-green { background: #f6ffed; color: #52c41a; }
.color-orange { background: #fff7e6; color: #fa8c16; }
.color-purple { background: #f9f0ff; color: #722ed1; }
.stat-info .num { font-size: 20px; font-weight: bold; color: #333; line-height: 1.2; }
.stat-info .desc { font-size: 12px; color: #999; }

/* 表单区域 */
.form-section { padding: 30px 50px; }
.section-title { margin-bottom: 25px; display: flex; align-items: center; }
.section-title h3 { margin: 0; font-size: 18px; color: #333; }

.form-row { display: flex; gap: 30px; margin-bottom: 20px; }
.form-group { flex: 1; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 500; font-size: 14px; color: #606266; }
.tag-edit { font-size: 12px; color: #409eff; background: #ecf5ff; padding: 1px 5px; border-radius: 4px; margin-left: 5px; font-weight: normal; }

.form-group input, .form-group select {
  width: 100%; padding: 10px 12px; border: 1px solid #dcdfe6; border-radius: 4px;
  font-size: 14px; transition: border 0.3s; box-sizing: border-box;
}
.form-group input:focus { border-color: #409eff; outline: none; }
.input-disabled { background: #f5f7fa; color: #909399; cursor: not-allowed; }

.form-actions { margin-top: 30px; display: flex; align-items: center; gap: 20px; }
.btn-save {
  padding: 10px 30px; background: #1890ff; color: white; border: none; border-radius: 4px;
  cursor: pointer; font-weight: bold; transition: all 0.3s;
}
.btn-save:hover { background: #40a9ff; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3); }
.btn-link { background: none; border: none; color: #666; cursor: pointer; text-decoration: underline; font-size: 14px; }
.btn-link:hover { color: #1890ff; }

/* 弹窗通用样式 */
.modal-mask { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; display: flex; justify-content: center; align-items: center; }
.modal-box { background: white; width: 400px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); animation: fade 0.3s; }
.modal-header { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 16px; }
.close-btn { font-size: 20px; cursor: pointer; color: #999; }
.modal-body { padding: 20px; }
.modal-footer { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-primary { background: #1890ff; color: white; padding: 6px 15px; border: none; border-radius: 4px; cursor: pointer; }
.btn-secondary { background: white; border: 1px solid #ddd; color: #666; padding: 6px 15px; border-radius: 4px; cursor: pointer; }

@keyframes fade { from { opacity: 0; transform: translateY(-10px); } to { opacity: 1; transform: translateY(0); } }

/* 响应式 */
@media (max-width: 900px) {
  .content-wrapper { flex-direction: column; }
  .info-card { width: 100%; }
  .stats-header { flex-wrap: wrap; gap: 20px; }
  .stat-item { width: 45%; }
}
</style>