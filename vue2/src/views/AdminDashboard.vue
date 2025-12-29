<template>
  <div class="dashboard-layout">
    <aside class="sidebar">
      <div class="logo">系统管理后台</div>
      <nav>
        <router-link to="/admin/teacher-manage">教师用户管理</router-link>
        <router-link to="/admin/student-manage">学生用户管理</router-link>
        <router-link to="/admin/base-course">课程基础库管理</router-link>
        <router-link to="/admin/teaching-class">排课与权限分配</router-link>
      </nav>
      <div class="logout">
        <button @click="$router.push('/login')">退出登录</button>
      </div>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <span>当前用户: 管理员 (Admin)</span>
        <button>修改密码</button>
      </header>
      <div class="content-view">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* 通用布局样式 */
.dashboard-layout { display: flex; height: 100vh; }
.sidebar { width: 220px; background-color: #304156; color: white; display: flex; flex-direction: column; }
.logo { height: 60px; line-height: 60px; text-align: center; font-size: 18px; font-weight: bold; background: #263445; }
.sidebar nav a { display: block; padding: 15px 20px; color: #bfcbd9; text-decoration: none; }
.sidebar nav a.router-link-active { color: #409EFF; background-color: #1f2d3d; }
.logout { margin-top: auto; padding: 20px; }
.main-content { flex: 1; display: flex; flex-direction: column; background: #f0f2f5; }
.top-header { height: 60px; background: white; padding: 0 20px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 4px rgba(0,21,41,.08); }
.content-view { padding: 20px; flex: 1; overflow-y: auto; }
</style>