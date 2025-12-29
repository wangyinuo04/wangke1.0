<template>
  <div class="login-wrapper">
    <div class="animated-background">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    <div class="content-container">
      
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-container">
            <img src="@/assets/logo.png" alt="今课堂 Logo" class="brand-logo">
          </div>
          <h1 class="brand-title">
            <span 
              v-for="(char, index) in titleChars" 
              :key="index" 
              class="char-bounce"
              :style="{ animationDelay: index * 0.15 + 's' }"
            >
              {{ char }}
            </span>
          </h1>
          <div class="slogan-container">
            <p class="brand-slogan">把握当下学习时光</p>
            <div class="slogan-line"></div>
          </div>
        </div>
      </div>

      <div class="form-section">
        <div class="login-card">
          <h2 class="form-title">找回密码</h2>
          <p class="form-subtitle">验证身份以重置您的账户密码</p>

          <div class="role-tabs">
            <div class="role-tab" :class="{ active: retrieveForm.role === 'student' }" @click="retrieveForm.role = 'student'">学生</div>
            <div class="role-tab" :class="{ active: retrieveForm.role === 'teacher' }" @click="retrieveForm.role = 'teacher'">教师</div>
          </div>

          <div v-if="retrieveStep === 1">
            <div class="input-group">
              <label>{{ roleAccountLabel }}</label>
              <input type="text" v-model="retrieveForm.id" :placeholder="'请输入' + roleAccountLabel" @blur="autoFillEmail">
            </div>
            <div class="input-group">
              <label>验证邮箱</label>
              <input type="text" v-model="userEmail" placeholder="系统将自动匹配邮箱" disabled>
              <div v-if="emailLoading" class="email-loading">正在获取邮箱...</div>
            </div>
            <div class="input-group">
              <label>验证码</label>
              <div class="verify-row">
                <input type="text" v-model="retrieveForm.code" placeholder="6位验证码">
                <button class="send-btn" @click="sendCode" :disabled="timer > 0 || !userEmail" :class="{ 'disabled': timer > 0 || !userEmail }">
                  {{ timer > 0 ? `${timer}s` : '获取验证码' }}
                </button>
              </div>
            </div>
            <button class="submit-btn" @click="verifyIdentity">下一步</button>
          </div>

          <div v-else>
            <div class="success-tip"><span>✓</span> 身份验证通过</div>
            <div class="input-group">
              <label>设置新密码</label>
              <input type="password" v-model="retrieveForm.newPass" placeholder="请输入新密码">
            </div>
            <div class="input-group">
              <label>确认新密码</label>
              <input type="password" v-model="retrieveForm.confirmPass" placeholder="请再次输入新密码">
            </div>
            <button class="submit-btn" @click="resetPassword">确认重置</button>
          </div>

          <div class="back-link-wrapper">
            <router-link class="back-link" to="/login">← 返回登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { sendCode, verifyCode, resetPassword, checkUser } from '@/api/retrieve'

export default {
  name: 'Retrieve',
  data() {
    return {
      titleText: '今课堂',
      retrieveStep: 1,
      timer: 0,
      userEmail: '',
      emailLoading: false,
      retrieveForm: {
        id: '',
        code: '',
        newPass: '',
        confirmPass: '',
        role: 'student'
      }
    }
  },
  computed: {
    titleChars() { 
      return this.titleText.split(''); 
    },
    roleAccountLabel() {
      const map = { 
        teacher: '教工号', 
        student: '学号' 
      };
      return map[this.retrieveForm.role];
    }
  },
  methods: {
    async autoFillEmail() {
      if (!this.retrieveForm.id) {
        this.userEmail = '';
        return;
      }
      
      this.emailLoading = true;
      try {
        const response = await checkUser({
          username: this.retrieveForm.id,
          role: this.retrieveForm.role
        });
        
        if (response.success) {
          this.userEmail = response.email;
        } else {
          this.userEmail = '';
          this.$message.warning(response.message);
        }
      } catch (error) {
        console.error('获取邮箱失败:', error);
        this.userEmail = '';
        this.$message.error('获取邮箱失败');
      } finally {
        this.emailLoading = false;
      }
    },
    
    async sendCode() {
      if (!this.retrieveForm.id) {
        this.$message.error('请输入' + this.roleAccountLabel);
        return;
      }
      
      if (!this.userEmail) {
        this.$message.error('请先输入' + this.roleAccountLabel + '获取邮箱');
        return;
      }
      
      try {
        const response = await sendCode({
          username: this.retrieveForm.id,
          role: this.retrieveForm.role
        });
        
        if (response.success) {
          this.timer = 60;
          let interval = setInterval(() => {
            this.timer--;
            if (this.timer === 0) {
              clearInterval(interval);
            }
          }, 1000);
          
          this.$message.success(response.message);
        } else {
          this.$message.error(response.message);
        }
      } catch (error) {
        console.error('发送验证码失败:', error);
        this.$message.error('发送失败，请稍后重试');
      }
    },
    
    async verifyIdentity() {
      if (!this.retrieveForm.code) {
        this.$message.error('请输入验证码');
        return;
      }
      
      try {
        const response = await verifyCode({
          username: this.retrieveForm.id,
          role: this.retrieveForm.role,
          code: this.retrieveForm.code
        });
        
        if (response.success) {
          this.retrieveStep = 2;
          this.$message.success('身份验证通过');
        } else {
          this.$message.error(response.message);
        }
      } catch (error) {
        console.error('验证失败:', error);
        this.$message.error('验证失败，请稍后重试');
      }
    },
    
    async resetPassword() {
      if (!this.retrieveForm.newPass || !this.retrieveForm.confirmPass) {
        this.$message.error('请输入新密码');
        return;
      }
      
      if (this.retrieveForm.newPass !== this.retrieveForm.confirmPass) {
        this.$message.error('两次密码输入不一致');
        return;
      }
      
      if (this.retrieveForm.newPass.length < 6) {
        this.$message.error('密码长度至少6位');
        return;
      }
      
      try {
        const response = await resetPassword({
          username: this.retrieveForm.id,
          newPassword: this.retrieveForm.newPass,
          role: this.retrieveForm.role
        });
        
        if (response.success) {
          this.$message.success('密码重置成功，请重新登录');
          this.$router.push('/login');
        } else {
          this.$message.error(response.message);
        }
      } catch (error) {
        console.error('重置密码失败:', error);
        this.$message.error('重置失败，请稍后重试');
      }
    }
  }
}
</script>

<style scoped>
/* ================= 布局与背景 ================= */
.login-wrapper {
  width: 100%; height: 100vh; position: relative; overflow: hidden;
  font-family: 'Helvetica Neue', Helvetica, "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
}

/* 动态流光背景 */
.animated-background {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: linear-gradient(135deg, #1890ff 0%, #003eb3 100%);
  background-size: 200% 200%; animation: gradientFlow 15s ease infinite; z-index: 0;
}
@keyframes gradientFlow {
  0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; }
}

/* 漂浮粒子 */
.shape { position: absolute; background: rgba(255, 255, 255, 0.08); border-radius: 50%; animation: floatShape 20s infinite linear; }
.shape-1 { width: 150px; height: 150px; top: 10%; left: 10%; animation-duration: 25s; }
.shape-2 { width: 100px; height: 100px; bottom: 20%; right: 20%; animation-duration: 15s; animation-delay: -5s; }
.shape-3 { width: 60px; height: 60px; top: 40%; left: 45%; animation-duration: 18s; animation-delay: -10s; }
.shape-4 { width: 200px; height: 200px; bottom: -50px; left: -50px; animation-duration: 30s; }
@keyframes floatShape {
  0% { transform: translate(0, 0) rotate(0deg); } 50% { transform: translate(30px, 50px) rotate(180deg); } 100% { transform: translate(0, 0) rotate(360deg); }
}

/* ================= 内容容器 ================= */
.content-container {
  position: relative; z-index: 1; width: 100%; height: 100%;
  display: flex; justify-content: space-between; align-items: center;
  max-width: 1400px; margin: 0 auto; padding: 0 50px; box-sizing: border-box;
}

/* ================= 左侧品牌区 ================= */
.brand-section { flex: 1; display: flex; justify-content: center; align-items: center; color: #fff; }
.brand-content { text-align: center; }

.logo-container { margin-bottom: 30px; animation: logoEntrance 1s ease-out forwards, breathe 4s ease-in-out infinite 1s; }
.brand-logo { width: 110px; height: 110px; border-radius: 24px; background: rgba(255, 255, 255, 0.95); padding: 8px; box-shadow: 0 8px 30px rgba(0,0,0,0.25); object-fit: contain; }

.brand-title { margin: 0; margin-bottom: 20px; font-size: 72px; font-weight: 800; letter-spacing: 8px; display: flex; justify-content: center; text-shadow: 0 4px 10px rgba(0,0,0,0.2); }
.char-bounce { display: inline-block; opacity: 0; transform: translateY(20px) scale(0.8); animation: popUp 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55) forwards; }

.slogan-container { position: relative; opacity: 0; animation: slideUpFade 0.8s ease-out forwards; animation-delay: 0.8s; }
.brand-slogan { font-size: 28px; font-weight: 300; letter-spacing: 4px; margin: 0; text-shadow: 0 2px 4px rgba(0,0,0,0.15); }
.slogan-line { width: 60px; height: 3px; background: rgba(255,255,255,0.8); margin: 15px auto 0; border-radius: 2px; }

/* 动效 */
@keyframes logoEntrance { 0% { opacity: 0; transform: scale(0.5) translateY(-50px); } 100% { opacity: 1; transform: scale(1) translateY(0); } }
@keyframes breathe { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.05); } }
@keyframes popUp { 0% { opacity: 0; transform: translateY(40px) scale(0.5); } 100% { opacity: 1; transform: translateY(0) scale(1); } }
@keyframes slideUpFade { 0% { opacity: 0; transform: translateY(30px); } 100% { opacity: 1; transform: translateY(0); } }

/* ================= 右侧表单区 ================= */
.form-section { flex: 1; display: flex; justify-content: center; align-items: center; }

.login-card {
  width: 400px; background: #ffffff; border-radius: 20px; padding: 40px 50px;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.3); display: flex; flex-direction: column;
  transition: transform 0.3s;
  min-height: 480px; /* 设定最小高度，防止切换时卡片大小跳动太大 */
}
.login-card:hover { transform: translateY(-5px); }

/* 切换时的淡入动画 */
.fade-in-animation { animation: fadeContent 0.4s ease-in-out; }
@keyframes fadeContent { from { opacity: 0; transform: translateX(10px); } to { opacity: 1; transform: translateX(0); } }

.form-title { font-size: 28px; font-weight: bold; color: #333; margin-bottom: 5px; text-align: center; }
.form-subtitle { font-size: 14px; color: #888; margin-bottom: 25px; text-align: center; }

/* Role Tabs */
.role-tabs { display: flex; background: #f0f2f5; border-radius: 10px; padding: 5px; margin-bottom: 25px; }
.role-tab { flex: 1; text-align: center; padding: 10px 0; font-size: 14px; color: #666; cursor: pointer; border-radius: 8px; transition: all 0.3s; font-weight: 500; }
.role-tab.active { background: #fff; color: #1890ff; font-weight: bold; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }

/* Inputs */
.input-group { margin-bottom: 20px; }
.input-group label { display: block; font-size: 13px; color: #666; margin-bottom: 8px; font-weight: 600; }
.input-group input {
  width: 100%; padding: 14px 15px; border: 1px solid #eee; border-radius: 10px;
  font-size: 14px; background: #f9f9f9; transition: all 0.3s; box-sizing: border-box;
}
.input-group input:focus { border-color: #1890ff; background: #fff; outline: none; box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1); }

/* 验证码行 */
.verify-row { display: flex; gap: 10px; }
.verify-row input { flex: 1; }
.send-btn {
  padding: 0 12px; background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff;
  border-radius: 10px; cursor: pointer; font-size: 13px; transition: all 0.3s; white-space: nowrap;
}
.send-btn:hover { background: #1890ff; color: white; border-color: #1890ff; }
.send-btn.disabled { background: #f5f5f5; color: #ccc; border-color: #eee; cursor: not-allowed; }

/* Links & Buttons */
.form-options { display: flex; justify-content: flex-end; margin-bottom: 25px; }
.forgot-link { font-size: 13px; color: #999; text-decoration: none; cursor: pointer; }
.forgot-link:hover { color: #1890ff; }

.submit-btn {
  width: 100%; padding: 15px; background: #1890ff; color: white; border: none;
  border-radius: 10px; font-size: 16px; font-weight: bold; cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s; box-shadow: 0 5px 15px rgba(24, 144, 255, 0.3);
}
.submit-btn:hover { background: #096dd9; transform: translateY(-2px); box-shadow: 0 8px 20px rgba(24, 144, 255, 0.4); }

.back-link-wrapper { margin-top: 20px; text-align: center; }
.back-link { font-size: 13px; color: #666; cursor: pointer; text-decoration: none; }
.back-link:hover { color: #1890ff; }

.register-footer { margin-top: 25px; text-align: center; font-size: 13px; color: #666; }
.register-link { color: #1890ff; font-weight: bold; cursor: pointer; margin-left: 5px; }
.register-link:hover { text-decoration: underline; }

.success-tip { background: #f6ffed; border: 1px solid #b7eb8f; color: #52c41a; padding: 10px; border-radius: 6px; text-align: center; margin-bottom: 15px; font-size: 13px; }

/* 响应式 */
@media (max-width: 900px) {
  .content-container { flex-direction: column; justify-content: center; }
  .brand-section { display: none; }
  .login-card { width: 100%; max-width: 350px; }
}
</style>