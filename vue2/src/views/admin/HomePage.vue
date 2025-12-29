<template>
  <div class="home-page">
    <div class="dashboard-banner">
      <div class="banner-bg"></div>
      <div class="banner-content">
        <h2 class="welcome-title">你好，管理员</h2>
        <p class="welcome-subtitle">欢迎回到在线课程管理系统，今天是 {{ currentDate }}</p>
      </div>
      <div class="banner-illustration">📊</div>
    </div>

    <div class="section-container">
      <h3 class="section-title">个人中心管理</h3>
      <div class="quick-access-grid">
        <div class="access-card" @click="navigateTo('/admin/profile')">
          <div class="icon-wrapper color-indigo">
            <span>👤</span>
          </div>
          <div class="card-info">
            <h4>管理员个人主页</h4>
            <p>个人档案与密码修改</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>
      </div>
    </div>

    <div class="section-container">
      <h3 class="section-title">核心功能管理</h3>
      <div class="quick-access-grid">
        
        <div class="access-card" @click="navigateTo('/admin/teacher-manage')">
          <div class="icon-wrapper color-blue">
            <span>👨‍🏫</span>
          </div>
          <div class="card-info">
            <h4>教师用户管理</h4>
            <p>录入档案、维护信息及权限</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>

        <div class="access-card" @click="navigateTo('/admin/student-manage')">
          <div class="icon-wrapper color-green">
            <span>👨‍🎓</span>
          </div>
          <div class="card-info">
            <h4>学生用户管理</h4>
            <p>学生档案与账号安全设置</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>

        <div class="access-card" @click="navigateTo('/admin/base-course')">
          <div class="icon-wrapper color-orange">
            <span>📚</span>
          </div>
          <div class="card-info">
            <h4>课程基础库</h4>
            <p>建立全校课程元数据标准</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>

        <div class="access-card" @click="navigateTo('/admin/teaching-class')">
          <div class="icon-wrapper color-purple">
            <span>📅</span>
          </div>
          <div class="card-info">
            <h4>排课与权限</h4>
            <p>开设教学班级并分配学生</p>
          </div>
          <div class="arrow-icon">→</div>
        </div>

      </div>
    </div>

    <div class="section-container">
      <h3 class="section-title">数据概览</h3>
      <div class="stats-grid">
        
        <div class="stat-card">
          <div class="stat-icon">👥</div>
          <div class="stat-data">
            <div class="stat-value">128</div>
            <div class="stat-label">在职教师总数</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">🎓</div>
          <div class="stat-data">
            <div class="stat-value">3,562</div>
            <div class="stat-label">在校学生总数</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">📖</div>
          <div class="stat-data">
            <div class="stat-value">480</div>
            <div class="stat-label">基础课程门数</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">🏫</div>
          <div class="stat-data">
            <div class="stat-value">86</div>
            <div class="stat-label">本学期开设班级</div>
          </div>
        </div>

      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'HomePage',
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
.home-page {
  padding: 0;
  font-family: 'Helvetica Neue', Helvetica, "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
  background-color: #f5f7fa;
  height: 84vh;
}

/* --- 1. 顶部 Banner --- */
.dashboard-banner {
  height: 180px;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  position: relative;
  display: flex;
  align-items: center;
  padding: 0 40px;
  color: white;
  border-radius: 0 0 20px 20px;
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
.banner-illustration { font-size: 80px; opacity: 0.2; transform: rotate(15deg); margin-right: 20px; }

/* --- 2. 通用容器 --- */
.section-container {
  max-width: 1400px;
  margin: 0 auto 30px; /* 板块间距 */
  padding: 0 20px;
}

/* 蓝色竖线标题风格 */
.section-title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 20px;
  border-left: 4px solid #1890ff; /* 蓝色竖线核心代码 */
  padding-left: 12px;
  font-weight: 700;
  line-height: 1.2;
}

/* --- 3. 核心功能与个人中心入口 (Grid 布局) --- */
/* 使用 Grid 布局替代 Flex，确保所有卡片（无论是一行几个）宽度严格一致 */
.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* 强制分为4列，保证宽度一致 */
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
  width: 100%; /* 占满 Grid 单元格 */
  box-sizing: border-box;
}

.access-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
  border-color: #c6e2ff;
}

/* 图标样式 */
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
.color-indigo { background: #f0f5ff; color: #2f54eb; } /* 个人中心图标色 */
.color-blue { background: #e6f7ff; color: #1890ff; }
.color-green { background: #f6ffed; color: #52c41a; }
.color-orange { background: #fff7e6; color: #fa8c16; }
.color-purple { background: #f9f0ff; color: #722ed1; }

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
.access-card:hover .arrow-icon { opacity: 1; transform: translateX(0); color: #1890ff; }

/* --- 4. 数据概览 (Stats Grid) --- */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* 同样使用 Grid 对齐 */
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
.stat-label { font-size: 13px; color: #909399; margin-top: 5px; }

/* 响应式适配 */
@media (max-width: 1024px) {
  .quick-access-grid, .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
  .quick-access-grid, .stats-grid { grid-template-columns: 1fr; }
  .dashboard-banner { border-radius: 0; height: 150px; }
}
</style>