<template>
  <div class="dashboard-layout">
    <!-- 固定侧边栏 -->
    <aside class="sidebar">
      <!-- 管理员信息区域 -->
      <div class="admin-info" @click="goToProfile">
        <el-avatar size="medium" icon="el-icon-user-solid" class="admin-avatar"></el-avatar>
        <span class="admin-name">管理员</span>
      </div>
      
      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <router-link to="/admin/home" class="nav-item">
          <i class="el-icon-s-home"></i>
          <span>首页</span>
        </router-link>
        <router-link to="/admin/teacher-manage" class="nav-item">
          <i class="el-icon-user"></i>
          <span>教师用户管理</span>
        </router-link>
        <router-link to="/admin/student-manage" class="nav-item">
          <i class="el-icon-user"></i>
          <span>学生用户管理</span>
        </router-link>
        <router-link to="/admin/base-course" class="nav-item">
          <i class="el-icon-notebook-2"></i>
          <span>课程基础库管理</span>
        </router-link>
        <router-link to="/admin/teaching-class" class="nav-item">
          <i class="el-icon-s-order"></i>
          <span>排课与权限分配</span>
        </router-link>
      </nav>
      
      <!-- 退出登录按钮 -->
      <div class="logout-container">
        <el-button 
          type="danger" 
          icon="el-icon-switch-button" 
          @click="logout"
          class="logout-btn"
        >
          退出登录
        </el-button>
      </div>
    </aside>

    <!-- 主内容区域 -->
    <main class="main-content">
      <header class="top-header">
        <span class="current-user">当前用户: 管理员 (Admin)</span>
      </header>
      <div class="content-view">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script>
export default {
  name: 'AdminDashboard',
  methods: {
    goToProfile() {
      // 使用编程式导航代替事件冒泡
      this.$router.push('/admin/profile').catch(err => {
        // 避免重复导航的错误
        if (err.name !== 'NavigationDuplicated') {
          console.error('导航错误:', err);
        }
      });
    },
    logout() {
      // 这里可以添加退出登录的逻辑，比如清除token等
      this.$router.push('/login');
    }
  }
}
</script>

<style scoped>
/* 通用布局样式 */
.dashboard-layout {
  min-height: 100vh;
  position: relative;
}

/* 固定侧边栏样式 - 浅蓝色主题 */
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  width: 240px;
  height: 100vh;
  background: linear-gradient(180deg, #e6f7ff 0%, #bae7ff 100%);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  z-index: 100;
  overflow-y: auto; /* 如果内容过多，侧边栏内部可滚动 */
}

/* 管理员信息区域 */
.admin-info {
  display: flex;
  align-items: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid #91d5ff;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0; /* 防止压缩 */
}

.admin-info:hover {
  background: rgba(255, 255, 255, 1);
  transform: translateX(5px);
}

.admin-avatar {
  background-color: #1890ff;
  color: white;
  margin-right: 12px;
}

.admin-name {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

/* 导航菜单 */
.nav-menu {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto; /* 导航菜单可滚动 */
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: #595959;
  text-decoration: none;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
  white-space: nowrap; /* 防止文字换行 */
}

.nav-item:hover {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.nav-item.router-link-active {
  background: rgba(24, 144, 255, 0.15);
  color: #1890ff;
  border-left-color: #1890ff;
}

.nav-item i {
  margin-right: 10px;
  font-size: 18px;
}

.nav-item span {
  font-size: 14px;
}

/* 退出登录按钮容器 */
.logout-container {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-top: 1px solid #91d5ff;
  background: rgba(255, 255, 255, 0.8);
  flex-shrink: 0; /* 防止压缩 */
}

.logout-btn {
  width: 160px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 主内容区域 */
.main-content {
  margin-left: 240px; /* 给侧边栏留出空间 */
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.top-header {
  height: 60px;
  background: white;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  border-bottom: 1px solid #e8e8e8;
  position: sticky;
  top: 0;
  z-index: 99;
}

.current-user {
  font-size: 14px;
  color: #595959;
  font-weight: 500;
}

.content-view {
  padding: 24px;
  flex: 1;
  overflow-y: auto; /* 主内容区域可滚动 */
  background: white;
  margin: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 200px;
  }
  
  .main-content {
    margin-left: 200px;
  }
  
  .admin-name {
    font-size: 14px;
  }
  
  .nav-item span {
    font-size: 13px;
  }
}
</style>