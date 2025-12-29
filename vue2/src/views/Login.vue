<template>
  <div class="login-container">
    <div class="login-box">
      <h2>在线课程管理系统</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>登录角色:</label>
          <select v-model="loginForm.role">
            <option value="admin">管理员 (Administrator)</option>
            <option value="teacher">教师 (Teacher)</option>
            <option value="student">学生 (Student)</option>
          </select>
        </div>

        <div class="form-group">
          <label>{{ accountLabel }}:</label>
          <input type="text" v-model="loginForm.username" required :placeholder="'请输入' + accountLabel">
        </div>

        <div class="form-group">
          <label>密码:</label>
          <input type="password" v-model="loginForm.password" required placeholder="请输入密码">
        </div>

        <div class="action-buttons">
          <button type="submit" class="btn-primary">登录</button>
          <router-link to="/retrieve" class="link-text">忘记密码?</router-link>
        </div>
      </form>
      
      <div v-if="loginForm.role === 'student'" class="register-hint">
        没有账号? <a href="#" @click.prevent="alert('跳转到注册页面')">立即注册</a>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        role: 'student', // 默认选中学生
        username: '',
        password: ''
      }
    }
  },
  computed: {
    // 根据角色动态显示账号提示 
    accountLabel() {
      const map = {
        admin: '管理员账号',
        teacher: '教工号',
        student: '学号'
      }
      return map[this.loginForm.role]
    }
  },
  methods: {
    handleLogin() {
      // 模拟登录验证
      if (this.loginForm.username && this.loginForm.password) {
        alert(`登录成功！欢迎 ${this.accountLabel}: ${this.loginForm.username}`);
        // 根据角色跳转到对应 Dashboard
        if (this.loginForm.role === 'admin') this.$router.push('/admin');
        if (this.loginForm.role === 'teacher') this.$router.push('/teacher');
        if (this.loginForm.role === 'student') this.$router.push('/student');
      } else {
        alert('请输入完整的账号和密码');
      }
    }
  }
}
</script>

<style scoped>
/* 简单的样式美化 */
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f0f2f5; }
.login-box { width: 350px; padding: 40px; background: white; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1); }
.form-group { margin-bottom: 20px; text-align: left; }
.form-group label { display: block; margin-bottom: 5px; font-weight: bold; }
.form-group input, .form-group select { width: 100%; padding: 8px; box-sizing: border-box; }
.btn-primary { width: 100%; padding: 10px; background-color: #409EFF; color: white; border: none; border-radius: 4px; cursor: pointer; }
.action-buttons { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; }
.link-text { font-size: 12px; color: #409EFF; text-decoration: none; }
.register-hint { margin-top: 20px; font-size: 14px; color: #666; }
</style>