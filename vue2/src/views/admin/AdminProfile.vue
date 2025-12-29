<template>
  <div class="profile-page">
    <div class="profile-banner">
      <div class="banner-bg"></div>
      <div class="banner-content">
        <h2 class="page-title">管理员个人中心</h2>
        <p class="page-subtitle">Personal Center & Security Settings</p>
      </div>
      
      <button class="back-btn" @click="goBack">
        <span class="icon">↩</span> 返回首页
      </button>
    </div>

    <div class="main-content">
      <div class="content-wrapper">
        
        <div class="info-card">
          <div class="card-body">
            <div class="avatar-wrapper">
              <div class="avatar-circle">
                <span class="avatar-text">Admin</span>
              </div>
              <div class="status-dot"></div>
            </div>
            <h3 class="admin-name">{{ adminInfo.name || '系统管理员' }}</h3>
            <span class="admin-badge">超级管理员</span>
            
            <div class="divider"></div>
            
            <div class="info-list">
              <div class="info-item">
                <span class="label">登录账号</span>
                <span class="value">{{ adminInfo.account || 'admin' }}</span>
              </div>
              <div class="info-item">
                <span class="label">所属部门</span>
                <span class="value">{{ adminInfo.department || '教务处' }}</span>
              </div>
              </div>
          </div>
        </div>

        <div class="security-card">
          <div class="card-header">
            <div class="header-title">
              <h3>账号安全设置</h3>
              <p>为了您的账号安全，建议定期修改登录密码</p>
            </div>
            <div class="security-icon">🛡️</div>
          </div>
          
          <div class="card-body form-body">
            <form @submit.prevent="updatePassword" class="password-form">
              <div class="form-row">
                <div class="form-group full-width">
                  <label>当前旧密码</label>
                  <input 
                    type="password" 
                    v-model="passForm.oldPass" 
                    placeholder="请输入当前正在使用的密码" 
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
                    placeholder="设置新密码 (6-16位)" 
                    minlength="6"
                    required
                  >
                </div>
                <div class="form-group">
                  <label>确认新密码</label>
                  <input 
                    type="password" 
                    v-model="passForm.confirmPass" 
                    placeholder="再次输入以确认" 
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

              <div v-if="submitError" class="error-banner">
                ⚠️ {{ submitError }}
              </div>

              <div v-if="submitSuccess" class="success-banner">
                ✅ {{ submitSuccess }}
              </div>

              <div class="form-actions">
                <button type="button" class="btn-cancel" @click="resetForm">重置</button>
                <button type="submit" class="btn-save" :disabled="submitting">
                  {{ submitting ? '修改中...' : '保存修改' }}
                </button>
              </div>
            </form>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import { changeAdminPassword } from '@/api/login'

export default {
  name: 'AdminProfile',
  data() {
    return {
      adminInfo: {
        account: '',
        name: '系统管理员',
        department: '教务处'
      },
      passForm: {
        oldPass: '',
        newPass: '',
        confirmPass: ''
      },
      submitting: false,
      submitError: '',
      submitSuccess: ''
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
  mounted() {
    this.loadAdminInfo();
  },
  methods: {
    goBack() {
      this.$router.push('/admin');
    },
    
    loadAdminInfo() {
      // 从localStorage获取管理员信息
      const userInfoStr = localStorage.getItem('userInfo');
      if (userInfoStr) {
        try {
          const userInfo = JSON.parse(userInfoStr);
          if (userInfo.role === 'admin') {
            this.adminInfo.account = userInfo.account || 'admin';
            this.adminInfo.name = userInfo.name || '系统管理员';
          }
        } catch (e) {
          console.error('解析管理员信息失败:', e);
        }
      }
    },
    
    resetForm() {
      this.passForm = { oldPass: '', newPass: '', confirmPass: '' };
      this.submitError = '';
      this.submitSuccess = '';
    },
    
    async updatePassword() {
      // 前端验证
      if (this.passwordMismatch) {
        this.submitError = '错误：两次输入的新密码不一致！';
        return;
      }
      
      if (this.passForm.oldPass === this.passForm.newPass) {
        this.submitError = '错误：新密码不能与旧密码相同！';
        return;
      }
      
      if (this.passForm.newPass.length < 6) {
        this.submitError = '错误：新密码长度不能少于6位！';
        return;
      }
      
      // 获取当前管理员账号
      const adminAccount = this.adminInfo.account || 'admin';
      
      this.submitting = true;
      this.submitError = '';
      this.submitSuccess = '';
      
      try {
        const requestData = {
          account: adminAccount,
          oldPassword: this.passForm.oldPass,
          newPassword: this.passForm.newPass
        };
        
        console.log('发送密码修改请求:', requestData);
        
        const response = await changeAdminPassword(requestData);
        console.log('密码修改响应:', response);
        
        if (response.success) {
          this.submitSuccess = response.message || '密码修改成功！请重新登录。';
          
          // 3秒后跳转到登录页
          setTimeout(() => {
            // 清除本地存储的用户信息
            localStorage.removeItem('userInfo');
            // 跳转到登录页
            this.$router.push('/login');
          }, 3000);
        } else {
          this.submitError = response.message || '密码修改失败，请检查旧密码是否正确';
        }
      } catch (error) {
        console.error('密码修改请求失败:', error);
        this.submitError = '网络错误，请检查后端服务是否正常运行';
      } finally {
        this.submitting = false;
      }
    }
  }
}
</script>

<style scoped>
/* 全局容器 */
.profile-page {
  height: 84vh;
  background-color: #f0f2f5;
  font-family: 'Helvetica Neue', Helvetica, "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
  display: flex;
  flex-direction: column;
}

/* --- 1. 顶部 Banner 区域 (已修改颜色) --- */
.profile-banner {
  height: 220px;
  /* 修改处：使用更明亮的渐变色 */
  background: linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%); /* 备选清新绿蓝 */
  background: linear-gradient(120deg, #a1c4fd 0%, #c2e9fb 100%); /* 备选淡蓝 */
  background: linear-gradient(135deg, #69c0ff 0%, #1890ff 100%); /* 最终选择：明亮科技蓝 */
  
  position: relative;
  display: flex;
  align-items: center;
  padding: 0 40px;
  color: white;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

/* 装饰背景图形 */
.banner-bg {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background-image: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.2) 0%, transparent 20%),
                    radial-gradient(circle at 80% 80%, rgba(255,255,255,0.1) 0%, transparent 30%);
  pointer-events: none;
}

.banner-content {
  position: relative;
  z-index: 2;
  margin-left: 20px;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.page-title { margin: 0; font-size: 28px; font-weight: bold; letter-spacing: 1px; }
.page-subtitle { margin: 8px 0 0; opacity: 0.9; font-size: 14px; font-weight: 400; }

/* 返回按钮 */
.back-btn {
  position: absolute;
  top: 30px;
  left: 30px;
  background: rgba(255, 255, 255, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: white;
  padding: 8px 18px;
  border-radius: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 14px;
  backdrop-filter: blur(5px);
  transition: all 0.3s;
  z-index: 10;
  font-weight: 500;
}
.back-btn:hover { background: rgba(255, 255, 255, 0.4); transform: translateX(-3px); }
.back-btn .icon { margin-right: 6px; font-size: 16px; }


/* --- 2. 主内容区域 --- */
.main-content {
  flex: 1;
  padding: 0 40px 40px;
  margin-top: -60px; /* 向上重叠 Banner */
  position: relative;
  z-index: 3;
}

.content-wrapper {
  display: flex;
  gap: 25px;
  height: 100%;
  align-items: stretch; 
}

/* --- 卡片通用样式 --- */
.info-card, .security-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
}

/* 左侧：个人信息 */
.info-card {
  width: 320px;
  flex-shrink: 0;
}

.card-body {
  padding: 40px 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  box-sizing: border-box;
}

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
.info-item {
  display: flex; justify-content: space-between;
  margin-bottom: 18px; font-size: 14px;
}
.info-item .label { color: #909399; }
.info-item .value { color: #333; font-weight: 500; font-family: monospace; }


/* 右侧：安全设置 */
.security-card {
  flex: 1;
  min-height: 500px;
}

.card-header {
  padding: 25px 40px;
  border-bottom: 1px solid #f0f0f0;
  display: flex; justify-content: space-between; align-items: center;
}
.header-title h3 { margin: 0 0 6px; font-size: 18px; color: #333; }
.header-title p { margin: 0; color: #999; font-size: 13px; }
.security-icon { font-size: 32px; opacity: 0.8; }

.form-body {
  align-items: flex-start;
  padding: 40px 60px;
  width: 100%;
}

.password-form { width: 100%; max-width: 800px; }

.form-row { display: flex; gap: 30px; margin-bottom: 25px; }
.form-group { flex: 1; }
.form-group.full-width { max-width: 100%; }

.form-group label {
  display: block; margin-bottom: 10px; font-weight: 500; color: #606266; font-size: 14px;
}

.form-group input {
  width: 100%; padding: 12px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px; background: #fff;
  transition: all 0.3s; box-sizing: border-box;
}
.form-group input:focus {
  border-color: #1890ff; box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1); outline: none;
}

.password-strength {
  display: flex; align-items: center; gap: 10px; margin-bottom: 25px;
}
.strength-bar {
  flex: 1; height: 6px; background: #f0f0f0; border-radius: 3px; overflow: hidden; max-width: 200px;
}
.strength-bar .fill { height: 100%; transition: width 0.3s, background 0.3s; }
.strength-text { font-size: 12px; color: #999; width: 30px; }

.error-banner {
  background: #fff1f0; border: 1px solid #ffccc7; color: #ff4d4f;
  padding: 10px 15px; border-radius: 6px; font-size: 13px; margin-bottom: 25px;
  display: flex; align-items: center;
}

.success-banner {
  background: #f6ffed; border: 1px solid #b7eb8f; color: #52c41a;
  padding: 10px 15px; border-radius: 6px; font-size: 13px; margin-bottom: 25px;
  display: flex; align-items: center;
}

.form-actions { margin-top: 20px; display: flex; gap: 15px; }
.btn-save {
  padding: 12px 35px; background: #1890ff; color: white;
  border: none; border-radius: 6px; font-size: 15px; font-weight: bold;
  cursor: pointer; box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3); transition: all 0.3s;
}
.btn-save:hover { background: #40a9ff; transform: translateY(-2px); }
.btn-save:disabled {
  background: #bae7ff;
  cursor: not-allowed;
  transform: none;
}

.btn-cancel {
  padding: 12px 30px; background: white; border: 1px solid #dcdfe6;
  color: #606266; border-radius: 6px; cursor: pointer; font-size: 15px;
  transition: all 0.3s;
}
.btn-cancel:hover { border-color: #1890ff; color: #1890ff; }

/* 响应式 */
@media (max-width: 900px) {
  .content-wrapper { flex-direction: column; }
  .info-card { width: 100%; }
  .main-content { padding: 0 20px 20px; }
  .form-row { flex-direction: column; gap: 20px; }
}

</style>