<template>
  <div class="student-profile">
    <div class="profile-header">
      <el-button type="primary" icon="el-icon-back" @click="goBack">返回首页</el-button>
      <h1>学生个人主页</h1>
    </div>
    <div class="profile-content">
      <p>这里是学生个人主页</p>
      <p>这里可以展示学生个人信息、学习统计、修改头像等功能</p>
      <!-- 这里可以添加个人主页的具体内容 -->
      <div class="avatar-section">
        <h3>修改头像</h3>
        <el-avatar :size="100" :src="avatarUrl" icon="el-icon-user-solid"></el-avatar>
        <div style="margin-top: 20px;">
          <el-button type="primary" @click="changeAvatar">修改头像</el-button>
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
      avatarUrl: '' // 头像URL，可以上传后更新
    }
  },
  methods: {
    goBack() {
      this.$router.push('/student/home');
    },
    changeAvatar() {
      // 这里实现修改头像的逻辑
      this.$message.info('修改头像功能开发中...');
    }
  }
}
</script>

<style scoped>
.student-profile {
  padding: 20px;
}
.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}
.profile-header h1 {
  margin-left: 20px;
  color: #1890ff;
}
.profile-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}
.avatar-section {
  margin-top: 30px;
  text-align: center;
}
</style>