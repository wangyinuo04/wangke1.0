<template>
  <div class="dashboard-layout">
    <!-- 固定侧边栏 -->
    <aside class="sidebar">
      <!-- 学生信息区域 -->
      <div class="student-info" @click="goToProfile">
        <el-avatar size="medium" icon="el-icon-user-solid" class="student-avatar"></el-avatar>
        <span class="student-id">{{ studentId }}</span>
      </div>
      
      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <router-link to="/student/home" class="nav-item">
          <i class="el-icon-s-home"></i>
          <span>首页</span>
        </router-link>
        <router-link to="/student/course-list" class="nav-item">
          <i class="el-icon-notebook-1"></i>
          <span>我的课程列表</span>
        </router-link>
        <router-link to="/student/my-homework" class="nav-item">
          <i class="el-icon-edit"></i>
          <span>我的作业</span>
        </router-link>
        <router-link to="/student/my-exam" class="nav-item">
          <i class="el-icon-tickets"></i>
          <span>在线考试</span>
        </router-link>
        <router-link to="/student/discussion" class="nav-item">
          <i class="el-icon-chat-line-round"></i>
          <span>互动讨论区</span>
        </router-link>
      </nav>
      
      <!-- 退出登录按钮 -->
      <div class="logout-container">
        <el-button 
          type="primary" 
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
        <span class="welcome-text">你好，张同学 (软件2201班)</span>
        <div class="header-right">
          <!-- 邀请码输入框 -->
          <div class="invite-code-container">
            <el-input
              v-if="showInviteInput"
              v-model="inviteCode"
              placeholder="输入6位邀请码"
              size="small"
              style="width: 140px; margin-right: 10px;"
              @keyup.enter.native="joinClass"
              @blur="handleInviteInputBlur"
            >
              <template #append>
                <el-button size="small" @click.stop="joinClass">加入</el-button>
              </template>
            </el-input>
            <el-button
              v-else
              icon="el-icon-plus"
              size="small"
              type="primary"
              @click.stop="showInviteInput = true"
            >
              邀请码
            </el-button>
          </div>
          
          <el-badge :value="2" class="badge-item">
            <el-button icon="el-icon-message" size="small" circle @click="handleMessageClick"></el-button>
          </el-badge>
          <el-badge :value="8" class="badge-item">
            <el-button icon="el-icon-bell" size="small" circle @click="handleNotificationClick"></el-button>
          </el-badge>
        </div>
      </header>
      <div class="content-view">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* 通用布局样式 */
.dashboard-layout {
  min-height: 100vh;
  position: relative;
}

/* 固定侧边栏样式 */
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
  overflow-y: auto;
}

/* 学生信息区域 */
.student-info {
  display: flex;
  align-items: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid #91d5ff;
  cursor: pointer;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.student-info:hover {
  background: rgba(255, 255, 255, 1);
  transform: translateX(5px);
}

.student-avatar {
  background-color: #1890ff;
  color: white;
  margin-right: 12px;
}

.student-id {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

/* 导航菜单 */
.nav-menu {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: #595959;
  text-decoration: none;
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
  white-space: nowrap;
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
  flex-shrink: 0;
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
  margin-left: 240px;
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

.welcome-text {
  font-size: 16px;
  color: #1890ff;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

/* 邀请码容器 */
.invite-code-container {
  display: flex;
  align-items: center;
}

.badge-item {
  cursor: pointer;
}

.content-view {
  padding: 24px;
  flex: 1;
  overflow-y: auto;
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
  
  .student-id {
    font-size: 14px;
  }
  
  .nav-item span {
    font-size: 13px;
  }
  
  .header-right {
    gap: 10px;
  }
}
</style>