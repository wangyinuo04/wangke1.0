<template>
  <div class="dashboard-layout">
    <aside class="sidebar student-theme">
      <div class="logo">在线学习空间</div>
      <nav>
        <router-link to="/student/course-list">我的课程列表</router-link>
        <router-link to="/student/my-homework">我的作业</router-link>
        <router-link to="/student/my-exam">在线考试</router-link>
        <router-link to="/student/discussion">互动讨论区</router-link>
      </nav>
      <div class="logout">
        <button @click="$router.push('/login')">退出登录</button>
      </div>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <span>你好，张同学 (软件2201班)</span>
        <div class="actions">
           <button>修改个人信息</button>
        </div>
      </header>
      <div class="content-view">
         <div style="margin-bottom: 20px; padding: 10px; background: #e6f7ff; border: 1px dashed #1890ff;">
            快速操作: <input placeholder="输入6位邀请码加入班级" style="padding: 5px;"> <button>加入</button>
         </div>
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* 蓝色系主题 */
.dashboard-layout { display: flex; height: 100vh; }
.sidebar { width: 220px; background-color: #001529; color: white; display: flex; flex-direction: column; }
.logo { height: 60px; line-height: 60px; text-align: center; background: #002140; }
.sidebar nav a { display: block; padding: 15px 20px; color: #fff; text-decoration: none; }
.sidebar nav a.router-link-active { background-color: #1890ff; }
.main-content { flex: 1; display: flex; flex-direction: column; background: #f0f2f5; }
.top-header { height: 60px; background: white; padding: 0 20px; display: flex; justify-content: space-between; align-items: center; }
.content-view { padding: 20px; }
</style>