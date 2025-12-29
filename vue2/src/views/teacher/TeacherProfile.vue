<template>
  <div class="teacher-profile">
    <div class="profile-header">
      <el-button type="primary" icon="el-icon-back" @click="goBack">返回首页</el-button>
      <h1>教师个人主页</h1>
    </div>
    <div class="profile-content">
      <p>这里是教师个人主页</p>
      <p>这里可以展示教师个人信息、教学统计、修改密码等功能</p>
      <!-- 这里可以添加个人主页的具体内容 -->
    </div>
  </div>
</template>

<script>
export default {
  name: 'TeacherProfile',
  methods: {
    goBack() {
      this.$router.push('/teacher/home');
    }
  }
}
</script>

<style scoped>
.teacher-profile {
  padding: 20px;
}
.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
}
.profile-header h1 {
  margin-left: 20px;
  color: #1890ff; /* 改为蓝色与主题一致 */
}
.profile-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
}
</style>