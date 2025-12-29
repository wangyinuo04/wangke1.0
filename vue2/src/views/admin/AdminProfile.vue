<template>
  <div class="admin-profile">
    <div class="profile-header">
      <el-button type="primary" icon="el-icon-back" @click="goBack">返回首页</el-button>
      <h1>管理员个人主页</h1>
    </div>
    <div class="profile-content">
      <p>这是管理员个人主页</p>
      <p>这里可以展示管理员个人信息、修改密码等功能</p>
      <!-- 这里可以添加个人主页的具体内容 -->
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminProfile',
  methods: {
    goBack() {
      this.$router.push('/admin/home');
    }
  }
}
</script>

<style scoped>
.admin-profile {
  padding: 20px;
}
.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}
.profile-header h1 {
  margin-left: 20px;
  color: #304156;
}
.profile-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}
</style>