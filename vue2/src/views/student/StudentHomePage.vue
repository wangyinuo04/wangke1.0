<template>
  <div class="home-page">
    <div class="dashboard-banner">
      <div class="banner-bg"></div>
      <div class="banner-content">
        <h2 class="welcome-title">你好，同学</h2>
        <p class="welcome-subtitle">书山有路勤为径，今天是 {{ currentDate }}</p>
      </div>
      <div class="banner-illustration">🎓</div>
    </div>

    <div class="section-container">
      <h3 class="section-title">个人中心</h3>
      <div class="quick-access-grid">
        <div class="access-card" @click="navigateTo('/student/profile')">
          <div class="icon-wrapper color-indigo">
            <span>👤</span>
          </div>
          <div class="card-info">
            <h4>学生个人主页</h4>
            <p>查看档案、修改头像与密码</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>
      </div>
    </div>

    <div class="section-container">
      <h3 class="section-title">学习中心</h3>
      <div class="quick-access-grid">
        
        <div class="access-card" @click="navigateTo('/student/course-list')">
          <div class="icon-wrapper color-blue">
            <span>📖</span>
          </div>
          <div class="card-info">
            <h4>我的课程列表</h4>
            <p>查看已选修课程、进入学习</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>

        <div class="access-card" @click="navigateTo('/student/my-homework')">
          <div class="icon-wrapper color-orange">
            <span>✍️</span>
          </div>
          <div class="card-info">
            <h4>我的作业任务</h4>
            <p>查看待办作业、上传与提交</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>

        <div class="access-card" @click="navigateTo('/student/my-exam')">
          <div class="icon-wrapper color-red">
            <span>💯</span>
          </div>
          <div class="card-info">
            <h4>在线考试测验</h4>
            <p>参加期中期末考试、查看成绩</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>

        <div class="access-card" @click="navigateTo('/student/discussion')">
          <div class="icon-wrapper color-cyan">
            <span>💬</span>
          </div>
          <div class="card-info">
            <h4>互动讨论专区</h4>
            <p>课程答疑、交流与帖子回复</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>

      </div>
    </div>

    <div class="section-container">
      <h3 class="section-title">学习状态概览</h3>
      <div class="stats-grid">
        
        <div class="stat-card">
          <div class="stat-icon">🏫</div>
          <div class="stat-data">
            <div class="stat-value">{{ stats.courseCount }}</div>
            <div class="stat-label">已修读课程</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">📝</div>
          <div class="stat-data">
            <div class="stat-value text-orange">{{ stats.homeworkPending }}</div>
            <div class="stat-label">待提交作业</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">⏳</div>
          <div class="stat-data">
            <div class="stat-value">{{ stats.examCount }}</div>
            <div class="stat-label">近期考试安排</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">🏆</div>
          <div class="stat-data">
            <div class="stat-value">{{ stats.avgScore }}</div>
            <div class="stat-label">本学期平均分</div>
          </div>
        </div>

      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'StudentHomePage',
  data() {
    return {
      currentDate: new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }),
      // 模拟统计数据，实际可调用后端API获取
      stats: {
        courseCount: 5,
        homeworkPending: 3,
        examCount: 1,
        avgScore: 88.5
      }
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
/* 全局容器 */
.home-page {
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
  background-color: #f5f7fa;
  min-height: 84vh;
}

/* --- 1. 顶部 Banner (学生版使用清新的青蓝色) --- */
.dashboard-banner {
  height: 180px;
  /* 渐变色：清新的青色到蓝色 */
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  position: relative;
  display: flex;
  align-items: center;
  padding: 0 40px;
  color: white;
  border-radius: 0 0 20px 20px;
  box-shadow: 0 4px 15px rgba(79, 172, 254, 0.3);
  margin-bottom: 30px;
  overflow: hidden;
}

.banner-bg {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background-image: 
    radial-gradient(circle at 15% 50%, rgba(255,255,255,0.2) 0%, transparent 25%),
    radial-gradient(circle at 85% 30%, rgba(255,255,255,0.2) 0%, transparent 25%);
  pointer-events: none;
}

.banner-content { position: relative; z-index: 2; flex: 1; }
.welcome-title { margin: 0 0 10px; font-size: 28px; font-weight: bold; text-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.welcome-subtitle { margin: 0; opacity: 0.95; font-size: 14px; font-weight: 500; }
.banner-illustration { font-size: 80px; opacity: 0.3; transform: rotate(-10deg); margin-right: 20px; }

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
  /* 竖线颜色与 Banner 主色调呼应 */
  border-left: 4px solid #4facfe;
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
  border-color: #b3e5fc;
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
.color-orange { background: #fff7e6; color: #fa8c16; }
.color-red { background: #fff1f0; color: #f5222d; }
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
.access-card:hover .arrow-icon { opacity: 1; transform: translateX(0); color: #4facfe; }

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
.text-orange { color: #fa8c16; } /* 待提交作业高亮 */
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