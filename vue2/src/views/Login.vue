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
        <div class="login-card" :class="{ 'wide-card': currentView === 'register' }">
          
          <div v-if="currentView === 'login'" class="fade-in-animation">
            <h2 class="form-title">用户登录</h2>
            <p class="form-subtitle">欢迎回到在线课程管理系统</p>

            <div class="role-tabs">
              <div class="role-tab" :class="{ active: loginForm.role === 'student' }" @click="switchRole('student')">学生</div>
              <div class="role-tab" :class="{ active: loginForm.role === 'teacher' }" @click="switchRole('teacher')">教师</div>
              <div class="role-tab" :class="{ active: loginForm.role === 'admin' }" @click="switchRole('admin')">管理员</div>
            </div>

            <form @submit.prevent="handleLogin">
              <div class="input-group">
                <label>{{ accountLabel }}</label>
                <input type="text" v-model="loginForm.username" :placeholder="'请输入' + accountLabel" required>
              </div>
              <div class="input-group">
                <label>密码</label>
                <input type="password" v-model="loginForm.password" placeholder="请输入密码" required>
              </div>
              <div class="form-options">
                <!-- 改为路由链接 -->
                <router-link class="forgot-link" to="/retrieve">忘记密码？</router-link>
              </div>
              <button type="submit" class="submit-btn" :disabled="loading">
                {{ loading ? '登录中...' : '立即登录' }}
              </button>
            </form>

            <div class="register-footer" v-if="loginForm.role === 'student'">
              还没有账号？ <span class="register-link" @click="currentView = 'register'">注册</span>
            </div>
          </div>

          <!-- 注册部分 -->
          <div v-else-if="currentView === 'register'" class="fade-in-animation register-view">
            <h2 class="form-title">学生注册</h2>
            <p class="form-subtitle">创建账号以开启学习之旅</p>
            
            <div class="scroll-container">
              <div class="input-group compact">
                <label>学号 (唯一ID)</label>
                <input type="text" v-model="registerForm.studentId" placeholder="请输入学号" required>
              </div>

              <div class="form-row">
                <div class="input-group compact half">
                  <label>姓名</label>
                  <input type="text" v-model="registerForm.name" placeholder="真实姓名" required>
                </div>
                <div class="input-group compact half">
                  <label>性别</label>
                  <select v-model="registerForm.gender">
                    <option value="男">男</option>
                    <option value="女">女</option>
                  </select>
                </div>
              </div>

              <div class="form-row">
                <div class="input-group compact half">
                  <label>所属院系</label>
                  <input type="text" v-model="registerForm.department" placeholder="例: 软件学院" required>
                </div>
                <div class="input-group compact half">
                  <label>行政班级</label>
                  <input type="text" v-model="registerForm.className" placeholder="例: 软件2201" required>
                </div>
              </div>

              <div class="form-row">
                <div class="input-group compact half">
                  <label>手机号码</label>
                  <input type="text" v-model="registerForm.phone" placeholder="11位手机号" required>
                </div>
                <div class="input-group compact half">
                  <label>电子邮箱</label>
                  <input type="email" v-model="registerForm.email" placeholder="常用邮箱" required>
                </div>
              </div>

              <div class="input-group compact">
                <label>登录密码</label>
                <input type="password" v-model="registerForm.password" placeholder="设置密码" required>
              </div>

              <div class="input-group compact">
                <label>确认密码</label>
                <input type="password" v-model="registerForm.confirmPassword" placeholder="再次输入密码" required>
              </div>
            </div>

            <button class="submit-btn" @click="handleRegister" :disabled="registerLoading">
              {{ registerLoading ? '注册中...' : '立即注册' }}
            </button>
            
            <div class="back-link-wrapper">
              <span style="font-size:13px; color:#666;">已有账号？</span>
              <a class="register-link" @click.prevent="backToLogin">登录</a>
            </div>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<script>
import { login, register } from '@/api/login'

export default {
  name: 'Login',
  data() {
    return {
      titleText: '今课堂',
      currentView: 'login',
      loading: false,
      registerLoading: false,
      
      // --- 登录数据 ---
      loginForm: { role: 'student', username: '', password: '' },

      // --- 注册数据 ---
      registerForm: {
        studentId: '',
        name: '',
        gender: '男',
        department: '',
        className: '',
        phone: '',
        email: '',
        password: '',
        confirmPassword: ''
      }
    }
  },
  computed: {
    titleChars() { return this.titleText.split(''); },
    accountLabel() {
      const map = { admin: '管理员账号', teacher: '教工号', student: '学号' };
      return map[this.loginForm.role];
    }
  },
  methods: {
    switchRole(role) {
      this.loginForm.role = role;
      this.loginForm.username = '';
      this.loginForm.password = '';
    },
    
    backToLogin() {
      this.currentView = 'login';
      this.clearForms();
    },
    
    clearForms() {
      this.registerForm = { 
        studentId: '', 
        name: '', 
        gender: '男', 
        department: '', 
        className: '', 
        phone: '', 
        email: '', 
        password: '', 
        confirmPassword: '' 
      };
    },

    // --- 登录 ---
    async handleLogin() {
      if (!this.loginForm.username || !this.loginForm.password) {
        this.$message.error('请输入完整的账号和密码');
        return;
      }
      
      this.loading = true;
      
      try {
        const response = await login(this.loginForm);
        
        if (response.success) {
          this.$message.success(response.message);
          
          // 保存用户信息到localStorage
          const userInfo = {
            role: this.loginForm.role,
            id: this.loginForm.username,
            ...response.userInfo
          };
          localStorage.setItem('userInfo', JSON.stringify(userInfo));
          
          // 根据角色跳转
          if (this.loginForm.role === 'admin') {
            this.$router.push('/admin');
          } else if (this.loginForm.role === 'teacher') {
            this.$router.push('/teacher');
          } else if (this.loginForm.role === 'student') {
            this.$router.push('/student');
          }
        } else {
          this.$message.error(response.message);
        }
      } catch (error) {
        console.error('登录失败:', error);
        this.$message.error('登录失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },

    // --- 注册 ---
    async handleRegister() {
      const f = this.registerForm;
      
      // 验证必填字段
      if (!f.studentId || !f.name || !f.department || !f.className || 
          !f.phone || !f.email || !f.password || !f.confirmPassword) {
        this.$message.error('请填写完整的注册信息');
        return;
      }
      
      // 验证手机号格式
      const phoneRegex = /^1[3-9]\d{9}$/;
      if (!phoneRegex.test(f.phone)) {
        this.$message.error('请输入正确的手机号');
        return;
      }
      
      // 验证邮箱格式
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(f.email)) {
        this.$message.error('请输入正确的邮箱地址');
        return;
      }
      
      // 验证密码一致
      if (f.password !== f.confirmPassword) {
        this.$message.error('两次输入的密码不一致');
        return;
      }
      
      // 密码强度验证（至少6位）
      if (f.password.length < 6) {
        this.$message.error('密码长度至少6位');
        return;
      }
      
      this.registerLoading = true;
      
      try {
        const registerData = {
          studentId: f.studentId,
          name: f.name,
          gender: f.gender,
          department: f.department,
          className: f.className,
          phone: f.phone,
          email: f.email,
          loginPassword: f.password,
          enrollmentYear: new Date().getFullYear(),
          major: f.department,
          accountStatus: '正常'
        };
        
        const response = await register(registerData);
        
        if (response.success) {
          this.$message.success(response.message);
          this.backToLogin();
        } else {
          this.$message.error(response.message);
        }
      } catch (error) {
        console.error('注册失败:', error);
        this.$message.error('注册失败，请稍后重试');
      } finally {
        this.registerLoading = false;
      }
    }
  }
}
</script>

<style scoped>
/* ================= 布局与背景 (不变) ================= */
.login-wrapper { width: 100%; height: 100vh; position: relative; overflow: hidden; font-family: 'Helvetica Neue', Helvetica, "PingFang SC", "Microsoft YaHei", Arial, sans-serif; }
.animated-background { position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: linear-gradient(135deg, #1890ff 0%, #003eb3 100%); background-size: 200% 200%; animation: gradientFlow 15s ease infinite; z-index: 0; }
@keyframes gradientFlow { 0% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } 100% { background-position: 0% 50%; } }
.shape { position: absolute; background: rgba(255, 255, 255, 0.08); border-radius: 50%; animation: floatShape 20s infinite linear; }
.shape-1 { width: 150px; height: 150px; top: 10%; left: 10%; animation-duration: 25s; }
.shape-2 { width: 100px; height: 100px; bottom: 20%; right: 20%; animation-duration: 15s; animation-delay: -5s; }
.shape-3 { width: 60px; height: 60px; top: 40%; left: 45%; animation-duration: 18s; animation-delay: -10s; }
.shape-4 { width: 200px; height: 200px; bottom: -50px; left: -50px; animation-duration: 30s; }
@keyframes floatShape { 0% { transform: translate(0, 0) rotate(0deg); } 50% { transform: translate(30px, 50px) rotate(180deg); } 100% { transform: translate(0, 0) rotate(360deg); } }

/* ================= 内容容器 ================= */
.content-container { position: relative; z-index: 1; width: 100%; height: 100%; display: flex; justify-content: space-between; align-items: center; max-width: 1400px; margin: 0 auto; padding: 0 50px; box-sizing: border-box; }

/* ================= 左侧品牌区 (不变) ================= */
.brand-section { flex: 1; display: flex; justify-content: center; align-items: center; color: #fff; }
.brand-content { text-align: center; }
.logo-container { margin-bottom: 30px; animation: logoEntrance 1s ease-out forwards, breathe 4s ease-in-out infinite 1s; }
.brand-logo { width: 110px; height: 110px; border-radius: 24px; background: rgba(255, 255, 255, 0.95); padding: 8px; box-shadow: 0 8px 30px rgba(0,0,0,0.25); object-fit: contain; }
.brand-title { margin: 0; margin-bottom: 20px; font-size: 72px; font-weight: 800; letter-spacing: 8px; display: flex; justify-content: center; text-shadow: 0 4px 10px rgba(0,0,0,0.2); }
.char-bounce { display: inline-block; opacity: 0; transform: translateY(20px) scale(0.8); animation: popUp 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55) forwards; }
.slogan-container { position: relative; opacity: 0; animation: slideUpFade 0.8s ease-out forwards; animation-delay: 0.8s; }
.brand-slogan { font-size: 28px; font-weight: 300; letter-spacing: 4px; margin: 0; text-shadow: 0 2px 4px rgba(0,0,0,0.15); }
.slogan-line { width: 60px; height: 3px; background: rgba(255,255,255,0.8); margin: 15px auto 0; border-radius: 2px; }

@keyframes logoEntrance { 0% { opacity: 0; transform: scale(0.5) translateY(-50px); } 100% { opacity: 1; transform: scale(1) translateY(0); } }
@keyframes breathe { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.05); } }
@keyframes popUp { 0% { opacity: 0; transform: translateY(40px) scale(0.5); } 100% { opacity: 1; transform: translateY(0) scale(1); } }
@keyframes slideUpFade { 0% { opacity: 0; transform: translateY(30px); } 100% { opacity: 1; transform: translateY(0); } }

/* ================= 右侧表单区 ================= */
.form-section { flex: 1; display: flex; justify-content: center; align-items: center; }

.login-card {
  width: 400px; background: #ffffff; border-radius: 20px; padding: 40px 50px;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.3); display: flex; flex-direction: column;
  transition: all 0.4s ease;
  min-height: 480px;
}
/* 注册模式下卡片变宽 */
.login-card.wide-card { width: 480px; padding: 30px 40px; }

.fade-in-animation { animation: fadeContent 0.4s ease-in-out; width: 100%; }
@keyframes fadeContent { from { opacity: 0; transform: translateX(10px); } to { opacity: 1; transform: translateX(0); } }

.form-title { font-size: 28px; font-weight: bold; color: #333; margin-bottom: 5px; text-align: center; }
.form-subtitle { font-size: 14px; color: #888; margin-bottom: 25px; text-align: center; }

/* 角色 Tabs */
.role-tabs { display: flex; background: #f0f2f5; border-radius: 10px; padding: 5px; margin-bottom: 25px; }
.role-tab { flex: 1; text-align: center; padding: 10px 0; font-size: 14px; color: #666; cursor: pointer; border-radius: 8px; transition: all 0.3s; font-weight: 500; }
.role-tab.active { background: #fff; color: #1890ff; font-weight: bold; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }

/* Inputs 标准样式 */
.input-group { margin-bottom: 20px; }
.input-group label { display: block; font-size: 13px; color: #666; margin-bottom: 8px; font-weight: 600; }
.input-group input, .input-group select {
  width: 100%; padding: 14px 15px; border: 1px solid #eee; border-radius: 10px;
  font-size: 14px; background: #f9f9f9; transition: all 0.3s; box-sizing: border-box;
}
.input-group input:focus, .input-group select:focus { border-color: #1890ff; background: #fff; outline: none; box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.1); }
.input-group select { appearance: none; background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%23666' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E"); background-repeat: no-repeat; background-position: right 15px center; padding-right: 40px; }

/* 注册表单专用样式 */
.register-view { display: flex; flex-direction: column; height: 100%; }
.scroll-container {
  overflow-y: auto;
  max-height: 400px;
  padding-right: 5px;
  margin-bottom: 15px;
}
/* 自定义滚动条 */
.scroll-container::-webkit-scrollbar { width: 4px; }
.scroll-container::-webkit-scrollbar-thumb { background: #ddd; border-radius: 4px; }

.form-row { display: flex; gap: 15px; margin-bottom: 0; }
.input-group.compact { margin-bottom: 15px; }
.input-group.half { flex: 1; }
.input-group.compact label { font-size: 12px; margin-bottom: 5px; }
.input-group.compact input, .input-group.compact select { padding: 10px 12px; font-size: 13px; }
.input-group.compact select { padding-right: 35px; background-position: right 12px center; }

/* Buttons & Links */
.form-options { display: flex; justify-content: flex-end; margin-bottom: 25px; }
.forgot-link { font-size: 13px; color: #999; text-decoration: none; cursor: pointer; }
.forgot-link:hover { color: #1890ff; }
.submit-btn {
  width: 100%; padding: 15px; background: #1890ff; color: white; border: none;
  border-radius: 10px; font-size: 16px; font-weight: bold; cursor: pointer;
  transition: all 0.3s; box-shadow: 0 5px 15px rgba(24, 144, 255, 0.3); margin-top: 10px;
}
.submit-btn:hover { background: #096dd9; transform: translateY(-2px); box-shadow: 0 8px 20px rgba(24, 144, 255, 0.4); }
.submit-btn:disabled { background: #c0c0c0; cursor: not-allowed; transform: none; box-shadow: none; }
.back-link-wrapper { margin-top: 20px; text-align: center; }
.back-link { font-size: 13px; color: #666; cursor: pointer; text-decoration: none; }
.back-link:hover { color: #1890ff; }
.register-footer { margin-top: 25px; text-align: center; font-size: 13px; color: #666; }
.register-link { color: #1890ff; font-weight: bold; cursor: pointer; margin-left: 5px; }
.register-link:hover { text-decoration: underline; }
.verify-row { display: flex; gap: 10px; } .verify-row input { flex: 1; }
.send-btn { padding: 0 12px; background: #e6f7ff; color: #1890ff; border: 1px solid #91d5ff; border-radius: 10px; cursor: pointer; font-size: 13px; white-space: nowrap; height: 44px; }
.send-btn:hover { background: #1890ff; color: white; border-color: #1890ff; }
.send-btn.disabled { background: #f5f5f5; color: #ccc; border-color: #eee; cursor: not-allowed; }
.success-tip { background: #f6ffed; border: 1px solid #b7eb8f; color: #52c41a; padding: 10px; border-radius: 6px; text-align: center; margin-bottom: 15px; font-size: 13px; }
.success-tip span { display: inline-block; margin-right: 5px; }

/* 响应式 */
@media (max-width: 900px) {
  .content-container { flex-direction: column; justify-content: center; padding: 0 20px; }
  .brand-section { display: none; }
  .login-card { width: 100% !important; max-width: 350px; padding: 30px; }
  .login-card.wide-card { width: 100% !important; max-width: 380px; }
  .brand-title { font-size: 56px; letter-spacing: 6px; }
  .brand-slogan { font-size: 22px; letter-spacing: 3px; }
}
</style>