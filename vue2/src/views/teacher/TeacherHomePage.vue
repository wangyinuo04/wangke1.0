<template>
  <div class="home-page">
    <div class="dashboard-banner">
      <div class="banner-bg"></div>
      <div class="banner-content">
        <h2 class="welcome-title">你好，李老师</h2>
        <p class="welcome-subtitle">欢迎回到教学工作台，今天是 {{ currentDate }}</p>
      </div>
      <div class="banner-illustration">👩‍🏫</div>
    </div>

    <div class="section-container">
      <h3 class="section-title">个人中心管理</h3>
      <div class="quick-access-grid">
        <div class="access-card" @click="navigateTo('/teacher/profile')">
          <div class="icon-wrapper color-indigo">
            <span>👤</span>
          </div>
          <div class="card-info">
            <h4>教师个人主页</h4>
            <p>查看个人档案与修改密码</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>
      </div>
    </div>

    <div class="section-container">
      <h3 class="section-title">核心教学管理</h3>
      <div class="quick-access-grid">
        
        <div class="access-card" @click="navigateTo('/teacher/my-courses')">
          <div class="icon-wrapper color-blue">
            <span>🏫</span>
          </div>
          <div class="card-info">
            <h4>我的课程班级</h4>
            <p>班级管理、成员与邀请码</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>

        <div class="access-card" @click="navigateTo('/teacher/resources')">
          <div class="icon-wrapper color-green">
            <span>📂</span>
          </div>
          <div class="card-info">
            <h4>教学资源管理</h4>
            <p>上传课件、教案与参考资料</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>

        <div class="access-card" @click="navigateTo('/teacher/homework')">
          <div class="icon-wrapper color-orange">
            <span>📝</span>
          </div>
          <div class="card-info">
            <h4>作业发布与批改</h4>
            <p>布置作业任务、在线批阅</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>

        <div class="access-card" @click="navigateTo('/teacher/exam')">
          <div class="icon-wrapper color-purple">
            <span>📊</span>
          </div>
          <div class="card-info">
            <h4>考试与题库</h4>
            <p>试题录入、组卷与考试安排</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>

        <div class="access-card" @click="navigateTo('/teacher/discussion')">
          <div class="icon-wrapper color-cyan">
            <span>💬</span>
          </div>
          <div class="card-info">
            <h4>互动讨论管理</h4>
            <p>查看学生提问与答疑互动</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>

      </div>
    </div>

    <div class="section-container">
      <h3 class="section-title">教学数据概览</h3>
      <div class="stats-grid">
        
        <div class="stat-card">
          <div class="stat-icon">🎓</div>
          <div class="stat-data">
            <div class="stat-value">3</div>
            <div class="stat-label">负责教学班级</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">👥</div>
          <div class="stat-data">
            <div class="stat-value">142</div>
            <div class="stat-label">覆盖学生总数</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">📚</div>
          <div class="stat-data">
            <div class="stat-value">380</div>
            <div class="stat-label">题库试题总量</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">✍️</div>
          <div class="stat-data">
            <div class="stat-value text-red">28</div>
            <div class="stat-label">待批改作业/试卷</div>
          </div>
        </div>

      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'TeacherHomePage',
  data() {
    return {
      currentDate: new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
    }
  },
  methods: {
    navigateTo(path) {
      this.$router.push(path);
    }
  }
}
</script>

<style scoped>
/* 全局容器样式 */
.home-page {
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
  background-color: #f5f7fa;
  min-height: 84vh;
}

/* --- 1. 顶部 Banner (修改为蓝色系) --- */
.dashboard-banner {
  height: 180px;
  /* 修改点：改为与管理员一致的蓝色渐变 */
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  position: relative;
  display: flex;
  align-items: center;
  padding: 0 40px;
  color: white;
  border-radius: 0 0 20px 20px;
  /* 修改点：改为蓝色阴影 */
  box-shadow: 0 4px 15px rgba(24, 144, 255, 0.2);
  margin-bottom: 30px;
  overflow: hidden;
}

.banner-bg {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background-image: 
    radial-gradient(circle at 10% 20%, rgba(255,255,255,0.1) 0%, transparent 20%),
    radial-gradient(circle at 90% 80%, rgba(255,255,255,0.1) 0%, transparent 20%);
  pointer-events: none;
}

.banner-content { position: relative; z-index: 2; flex: 1; }
.welcome-title { margin: 0 0 10px; font-size: 28px; font-weight: bold; }
.welcome-subtitle { margin: 0; opacity: 0.9; font-size: 14px; }
.banner-illustration { font-size: 80px; opacity: 0.2; transform: rotate(10deg); margin-right: 20px; }

/* --- 2. 通用容器 --- */
.section-container {
  max-width: 1400px;
  margin: 0 auto 30px;
  padding: 0 20px;
}

.section-title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 20px;
  /* 修改点：竖线改为蓝色 */
  border-left: 4px solid #1890ff;
  padding-left: 12px;
  font-weight: 700;
  line-height: 1.2;
}

/* --- 3. Grid 网格布局 --- */
.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.access-card {
  background: white;
  border-radius: 12px;
  padding: 25px 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  border: 1px solid #ebeef5;
  width: 100%;
  box-sizing: border-box;
}

.access-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
  /* 修改点：悬停边框改为浅蓝色 */
  border-color: #c6e2ff;
}

/* 图标配色 */
.icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 28px;
  margin-right: 15px;
  flex-shrink: 0;
}
.color-indigo { background: #f0f5ff; color: #2f54eb; }
.color-blue { background: #e6f7ff; color: #1890ff; }
.color-green { background: #f6ffed; color: #52c41a; }
.color-orange { background: #fff7e6; color: #fa8c16; }
.color-purple { background: #f9f0ff; color: #722ed1; }
.color-cyan { background: #e6fffb; color: #13c2c2; }

.card-info h4 { margin: 0 0 5px; font-size: 16px; color: #333; font-weight: 600; }
.card-info p { margin: 0; font-size: 12px; color: #909399; }

.arrow-icon {
  margin-left: auto;
  color: #c0c4cc;
  font-weight: bold;
  opacity: 0;
  transform: translateX(-10px);
  transition: all 0.3s;
}
/* 修改点：悬停箭头改为蓝色 */
.access-card:hover .arrow-icon { opacity: 1; transform: translateX(0); color: #1890ff; }

/* --- 4. 数据概览 --- */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 25px 30px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
  border: 1px solid #ebeef5;
  transition: all 0.3s;
}
.stat-card:hover { box-shadow: 0 4px 15px rgba(0,0,0,0.08); }

.stat-icon {
  width: 50px;
  height: 50px;
  background: #f5f7fa;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  margin-right: 20px;
  color: #606266;
}

.stat-data { display: flex; flex-direction: column; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; line-height: 1.2; }
.text-red { color: #f5222d; }
.stat-label { font-size: 13px; color: #909399; margin-top: 5px; }

/* 响应式 */
@media (max-width: 1024px) {
  .quick-access-grid, .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
  .quick-access-grid, .stats-grid { grid-template-columns: 1fr; }
  .dashboard-banner { border-radius: 0; height: 150px; }
}
</style>