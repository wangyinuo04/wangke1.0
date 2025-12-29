<template>
  <div class="profile-page">
    <div class="profile-banner">
      <div class="banner-bg"></div>
      <div class="banner-content">
        <h2 class="page-title">教师个人中心</h2>
        <p class="page-subtitle">Personal Center & Account Settings</p>
      </div>
      
      <button class="back-btn" @click="goBack">
        <span class="icon">↩</span> 返回工作台
      </button>
    </div>

    <div class="main-content">
      <div class="content-wrapper">
        
        <div class="info-card">
          <div class="card-body">
            <div class="avatar-wrapper">
              <div class="avatar-circle">
                <span class="avatar-text">Teacher</span>
              </div>
              <div class="status-dot"></div>
            </div>
            <h3 class="admin-name">{{ teacherInfo.name }}</h3>
            <span class="admin-badge">授课教师</span>
            
            <div class="divider"></div>
            
            <div class="info-list">
              <div class="info-item">
                <span class="label">教职工号</span>
                <span class="value">{{ teacherInfo.id }}</span>
              </div>
              <div class="info-item">
                <span class="label">所属学院</span>
                <span class="value">{{ teacherInfo.college }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="right-column">
          
          <div class="settings-card mb-20">
            <div class="card-header">
              <div class="header-title">
                <h3>基本资料设置</h3>
                <p>更新您的联系方式，以便接收重要通知</p>
              </div>
              <div class="card-icon">📝</div>
            </div>
            <div class="card-body form-body">
              <form @submit.prevent="updateContactInfo">
                <div class="form-row">
                  <div class="form-group">
                    <label>手机号码</label>
                    <input 
                      type="tel" 
                      v-model="contactForm.phone" 
                      placeholder="请输入11位手机号" 
                      pattern="[0-9]{11}"
                      required
                    >
                  </div>
                  <div class="form-group">
                    <label>电子邮箱</label>
                    <input 
                      type="email" 
                      v-model="contactForm.email" 
                      placeholder="例如：teacher@school.edu.cn" 
                      required
                    >
                  </div>
                </div>
                <div class="form-actions right-align">
                  <button type="submit" class="btn-save">保存基本信息</button>
                </div>
              </form>
            </div>
          </div>

          <div class="settings-card">
            <div class="card-header">
              <div class="header-title">
                <h3>账号安全设置</h3>
                <p>为了您的账号安全，建议定期修改登录密码</p>
              </div>
              <div class="card-icon">🛡️</div>
            </div>
            
            <div class="card-body form-body">
              <form @submit.prevent="updatePassword" class="password-form">
                <div class="form-row">
                  <div class="form-group full-width">
                    <label>当前旧密码</label>
                    <input 
                      type="password" 
                      v-model="passForm.oldPass" 
                      placeholder="验证当前密码" 
                      required
                    >
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label>新密码</label>
                    <input 
                      type="password" 
                      v-model="passForm.newPass" 
                      placeholder="6-16位新密码" 
                      minlength="6"
                      required
                    >
                  </div>
                  <div class="form-group">
                    <label>确认新密码</label>
                    <input 
                      type="password" 
                      v-model="passForm.confirmPass" 
                      placeholder="再次输入确认" 
                      required
                    >
                  </div>
                </div>

                <div class="password-strength" v-if="passForm.newPass">
                  <div class="strength-bar">
                    <div class="fill" :style="{ width: passwordStrength + '%', background: strengthColor }"></div>
                  </div>
                  <span class="strength-text">{{ strengthText }}</span>
                </div>

                <div v-if="passwordMismatch" class="error-banner">
                  ⚠️ 两次输入的密码不一致
                </div>

                <div class="form-actions right-align">
                  <button type="submit" class="btn-save btn-danger-style">修改密码</button>
                </div>
              </form>
            </div>
          </div>

        </div>

      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TeacherProfile',
  data() {
    return {
      // 左侧固定信息 (已移除入职时间和职称)
      teacherInfo: {
        name: '李老师',
        id: 'T202301',
        college: '软件学院'
      },
      // 右侧可编辑信息 - 联系方式
      contactForm: {
        phone: '13800138000',
        email: 'li.teacher@university.edu.cn'
      },
      // 右侧可编辑信息 - 密码
      passForm: {
        oldPass: '',
        newPass: '',
        confirmPass: ''
      }
    }
  },
  computed: {
    passwordMismatch() {
      return this.passForm.newPass && this.passForm.confirmPass && (this.passForm.newPass !== this.passForm.confirmPass);
    },
    passwordStrength() {
      const len = this.passForm.newPass.length;
      if (len === 0) return 0;
      if (len < 6) return 30;
      if (len < 10) return 70;
      return 100;
    },
    strengthColor() {
      if (this.passwordStrength <= 30) return '#ff4d4f';
      if (this.passwordStrength <= 70) return '#faad14';
      return '#52c41a';
    },
    strengthText() {
      if (this.passwordStrength === 0) return '';
      if (this.passwordStrength <= 30) return '弱';
      if (this.passwordStrength <= 70) return '中';
      return '强';
    }
  },
  methods: {
    goBack() {
      this.$router.push('/teacher/home');
    },
    // 更新基本信息
    updateContactInfo() {
      console.log('Update Contact:', this.contactForm);
      alert('🎉 基本资料保存成功！');
    },
    // 修改密码
    updatePassword() {
      if (this.passwordMismatch) {
        return alert('错误：两次输入的新密码不一致！');
      }
      if (this.passForm.oldPass === this.passForm.newPass) {
        return alert('错误：新密码不能与旧密码相同！');
      }
      
      console.log('Update Password:', this.passForm);
      alert('🔒 密码修改成功！请重新登录。');
      this.$router.push('/login');
    }
  }
}
</script>

<style scoped>
/* 全局布局 */
.profile-page {
  min-height: 100vh;
  background-color: #f0f2f5;
  font-family: 'Helvetica Neue', Helvetica, "PingFang SC", Arial, sans-serif;
  display: flex;
  flex-direction: column;
}

/* Banner */
.profile-banner {
  height: 220px;
  background: linear-gradient(135deg, #69c0ff 0%, #1890ff 100%);
  position: relative;
  display: flex;
  align-items: center;
  padding: 0 40px;
  color: white;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  flex-shrink: 0;
}

.banner-bg {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background-image: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.2) 0%, transparent 20%),
                    radial-gradient(circle at 80% 80%, rgba(255,255,255,0.1) 0%, transparent 30%);
  pointer-events: none;
}

.banner-content { position: relative; z-index: 2; margin-left: 20px; }
.page-title { margin: 0; font-size: 28px; font-weight: bold; letter-spacing: 1px; }
.page-subtitle { margin: 8px 0 0; opacity: 0.9; font-size: 14px; }

.back-btn {
  position: absolute;
  top: 30px; left: 30px;
  background: rgba(255, 255, 255, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: white;
  padding: 8px 18px;
  border-radius: 20px;
  cursor: pointer;
  display: flex; align-items: center; font-size: 14px;
  backdrop-filter: blur(5px);
  transition: all 0.3s;
  z-index: 10;
}
.back-btn:hover { background: rgba(255, 255, 255, 0.4); transform: translateX(-3px); }
.back-btn .icon { margin-right: 6px; }

/* 主内容布局 */
.main-content {
  flex: 1;
  padding: 0 40px 40px;
  margin-top: -60px; /* 向上覆盖 Banner */
  position: relative;
  z-index: 3;
}

.content-wrapper {
  display: flex;
  gap: 25px;
  align-items: flex-start; /* 顶部对齐 */
}

/* --- 左侧信息卡片 --- */
.info-card {
  width: 300px;
  flex-shrink: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  overflow: hidden;
}

.card-body { padding: 40px 30px; display: flex; flex-direction: column; align-items: center; }

.avatar-wrapper { position: relative; margin-bottom: 15px; }
.avatar-circle {
  width: 100px; height: 100px;
  background: #1890ff;
  border-radius: 50%;
  display: flex; justify-content: center; align-items: center;
  border: 4px solid rgba(255,255,255,0.8);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}
.avatar-text { font-size: 24px; color: white; font-weight: bold; }
.status-dot {
  position: absolute; bottom: 5px; right: 5px;
  width: 20px; height: 20px; background: #52c41a;
  border: 3px solid white; border-radius: 50%;
}

.admin-name { margin: 10px 0 5px; font-size: 20px; color: #333; }
.admin-badge {
  background: #e6f7ff; color: #1890ff;
  padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: bold;
}

.divider { width: 100%; height: 1px; background: #f0f0f0; margin: 30px 0; }

.info-list { width: 100%; }
.info-item { display: flex; justify-content: space-between; margin-bottom: 18px; font-size: 14px; }
.info-item .label { color: #909399; }
.info-item .value { color: #333; font-weight: 500; font-family: monospace; }

/* --- 右侧布局 --- */
.right-column {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.mb-20 { margin-bottom: 25px; }

/* 通用设置卡片 */
.settings-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
}

.card-header {
  padding: 20px 30px;
  border-bottom: 1px solid #f0f0f0;
  display: flex; justify-content: space-between; align-items: center;
}
.header-title h3 { margin: 0 0 6px; font-size: 18px; color: #333; }
.header-title p { margin: 0; color: #999; font-size: 13px; }
.card-icon { font-size: 28px; opacity: 0.8; }

.form-body {
  padding: 30px 40px !important;
  align-items: stretch !important;
}

/* 表单样式 */
.form-row { display: flex; gap: 30px; margin-bottom: 20px; }
.form-group { flex: 1; }
.form-group.full-width { max-width: 100%; }

.form-group label {
  display: block; margin-bottom: 8px; font-weight: 500; color: #606266; font-size: 14px;
}

.form-group input {
  width: 100%; padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px; background: #fff;
  transition: all 0.3s; box-sizing: border-box;
}
.form-group input:focus {
  border-color: #1890ff; box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1); outline: none;
}

/* 密码强度 */
.password-strength { display: flex; align-items: center; gap: 10px; margin-bottom: 20px; }
.strength-bar { flex: 1; height: 6px; background: #f0f0f0; border-radius: 3px; overflow: hidden; max-width: 200px; }
.strength-bar .fill { height: 100%; transition: width 0.3s, background 0.3s; }
.strength-text { font-size: 12px; color: #999; width: 30px; }

.error-banner {
  background: #fff1f0; border: 1px solid #ffccc7; color: #ff4d4f;
  padding: 8px 15px; border-radius: 6px; font-size: 13px; margin-bottom: 20px;
}

.form-actions { margin-top: 10px; }
.right-align { text-align: right; }

.btn-save {
  padding: 10px 25px; background: #1890ff; color: white;
  border: none; border-radius: 6px; font-size: 14px; font-weight: bold;
  cursor: pointer; box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3); transition: all 0.3s;
}
.btn-save:hover { background: #40a9ff; transform: translateY(-2px); }

/* 修改密码按钮稍微区分一下颜色 */
.btn-danger-style { background: #ff4d4f; box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3); }
.btn-danger-style:hover { background: #ff7875; }

/* 响应式 */
@media (max-width: 900px) {
  .content-wrapper { flex-direction: column; }
  .info-card { width: 100%; margin-bottom: 20px; }
  .main-content { padding: 0 20px 20px; }
  .form-row { flex-direction: column; gap: 15px; }
}
</style>