<template>
  <div class="dashboard-layout">
    <aside class="sidebar teacher-theme">
      <div class="logo">教师教学中心</div>
      <nav>
        <router-link to="/teacher/my-courses">我的课程班级</router-link>
        <router-link to="/teacher/resources">教学资源管理</router-link>
        <router-link to="/teacher/homework">作业发布与批改</router-link>
        <router-link to="/teacher/exam">考试与题库管理</router-link>
        <router-link to="/teacher/discussion">互动讨论管理</router-link>
      </nav>
      <div class="logout">
        <button @click="$router.push('/login')">退出登录</button>
      </div>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <span>欢迎您，李老师 (工号: T202301)</span>
      </header>
      <div class="content-view">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* 复用 Admin 布局，改变侧边栏颜色区分角色 */
.dashboard-layout { display: flex; height: 100vh; }
.sidebar { width: 220px; background-color: #545c64; color: white; display: flex; flex-direction: column; } /* 灰色系 */
.logo { height: 60px; line-height: 60px; text-align: center; background: #434a50; }
.sidebar nav a { display: block; padding: 15px 20px; color: #fff; text-decoration: none; }
.sidebar nav a.router-link-active { background-color: #ffd04b; color: #333; }
.main-content { flex: 1; display: flex; flex-direction: column; background: #f0f2f5; }
.top-header { height: 60px; background: white; padding: 0 20px; display: flex; align-items: center; box-shadow: 0 1px 4px rgba(0,0,0,0.1); }
.content-view { padding: 20px; }
</style>