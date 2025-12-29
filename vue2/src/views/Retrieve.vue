<template>
  <div class="retrieve-container">
    <div class="retrieve-box">
      <h2>找回密码</h2>
      <div class="form-group">
        <label>账号 (教工号/学号):</label>
        <input type="text" v-model="form.id" placeholder="请输入您的ID">
      </div>
      <div class="form-group">
        <label>预留手机号/邮箱:</label>
        <input type="text" v-model="form.contact" placeholder="请输入注册时的手机或邮箱">
      </div>
      <div class="form-group verify-row">
        <input type="text" v-model="form.code" placeholder="输入验证码">
        <button @click="sendCode" :disabled="timer > 0">{{ timer > 0 ? `${timer}s` : '获取验证码' }}</button>
      </div>
      
      <div v-if="verified">
        <div class="form-group">
          <label>新密码:</label>
          <input type="password" v-model="form.newPass" placeholder="设置新密码">
        </div>
        <button class="btn-primary" @click="resetPassword">确认重置</button>
      </div>
      
      <button v-else class="btn-primary" @click="verifyIdentity">下一步</button>
      
      <div class="back-link">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Retrieve',
  data() {
    return {
      form: { id: '', contact: '', code: '', newPass: '' },
      timer: 0,
      verified: false // 模拟验证状态
    }
  },
  methods: {
    sendCode() {
      if(!this.form.contact) return alert('请输入手机/邮箱');
      this.timer = 60;
      let interval = setInterval(() => {
        this.timer--;
        if (this.timer === 0) clearInterval(interval);
      }, 1000);
      alert('模拟验证码已发送: 123456');
    },
    verifyIdentity() {
      if (this.form.code === '123456') {
        this.verified = true;
      } else {
        alert('验证码错误');
      }
    },
    resetPassword() {
      alert('密码重置成功，请重新登录');
      this.$router.push('/login');
    }
  }
}
</script>

<style scoped>
/* 复用部分Login样式，省略... */
.retrieve-container { display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f0f2f5; }
.retrieve-box { width: 350px; padding: 40px; background: white; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1); }
.form-group { margin-bottom: 15px; text-align: left; }
.form-group input { width: 100%; padding: 8px; box-sizing: border-box; }
.verify-row { display: flex; gap: 10px; }
.btn-primary { width: 100%; padding: 10px; background-color: #67C23A; color: white; border: none; cursor: pointer; }
.back-link { margin-top: 15px; font-size: 14px; }
</style>