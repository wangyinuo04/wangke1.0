import Vue from 'vue'
import Router from 'vue-router'

// 1. 引入核心页面
import Login from '@/views/Login.vue'
import Retrieve from '@/views/Retrieve.vue'
import AdminDashboard from '@/views/AdminDashboard.vue'
import TeacherDashboard from '@/views/TeacherDashboard.vue'
import StudentDashboard from '@/views/StudentDashboard.vue'

// 2. 引入占位页面 (用于模拟未开发的功能)
// 为了代码不报错，你需要创建这些文件，或者暂时注释掉 component 引用
const Placeholder = { template: '<div style="padding:20px;"><h2>功能开发中...</h2><p>这是具体的功能页面</p></div>' }

Vue.use(Router)

export default new Router({
  mode: 'history', // 去掉 URL 中的 #
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/retrieve',
      name: 'Retrieve',
      component: Retrieve
    },
    // --- 管理员路由 ---
    {
      path: '/admin',
      component: AdminDashboard,
      children: [
        { path: '', redirect: 'teacher-manage' },
        { path: 'teacher-manage', component: () => import('@/views/admin/TeacherManage.vue').catch(() => Placeholder), meta: { title: '教师用户管理' } },
        { path: 'student-manage', component: () => import('@/views/admin/StudentManage.vue').catch(() => Placeholder), meta: { title: '学生用户管理' } },
        { path: 'base-course', component: () => import('@/views/admin/BaseCourse.vue').catch(() => Placeholder), meta: { title: '课程基础库' } },
        { path: 'teaching-class', component: () => import('@/views/admin/TeachingClass.vue').catch(() => Placeholder), meta: { title: '排课与权限' } }
      ]
    },
    // --- 教师路由 ---
    {
      path: '/teacher',
      component: TeacherDashboard,
      children: [
        { path: '', redirect: 'my-courses' },
        { path: 'my-courses', component: () => import('@/views/teacher/MyCourses.vue').catch(() => Placeholder), meta: { title: '我的课程' } },
        { path: 'resources', component: () => import('@/views/teacher/ResourceManage.vue').catch(() => Placeholder), meta: { title: '教学资源' } },
        { path: 'homework', component: () => import('@/views/teacher/HomeworkManage.vue').catch(() => Placeholder), meta: { title: '作业管理' } },
        { path: 'exam', component: () => import('@/views/teacher/ExamManage.vue').catch(() => Placeholder), meta: { title: '考试题库' } },
        { path: 'discussion', component: () => import('@/views/teacher/DiscussionManage.vue').catch(() => Placeholder), meta: { title: '互动讨论' } }
      ]
    },
    // --- 学生路由 ---
    {
      path: '/student',
      component: StudentDashboard,
      children: [
        { path: '', redirect: 'course-list' },
        { path: 'course-list', component: () => import('@/views/student/CourseList.vue').catch(() => Placeholder), meta: { title: '课程学习' } },
        { path: 'my-homework', component: () => import('@/views/student/MyHomework.vue').catch(() => Placeholder), meta: { title: '我的作业' } },
        { path: 'my-exam', component: () => import('@/views/student/MyExam.vue').catch(() => Placeholder), meta: { title: '在线测验' } },
        { path: 'discussion', component: () => import('@/views/student/DiscussionBoard.vue').catch(() => Placeholder), meta: { title: '互动专区' } }
      ]
    }
  ]
})