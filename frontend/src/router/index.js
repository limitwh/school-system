import { createRouter, createWebHistory } from 'vue-router'
import { getToken, getUser } from '@/utils/auth'
import { getHomePath } from '@/utils/role'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'MessageCenter',
    component: () => import('@/views/message/MessageCenter.vue'),
    meta: { title: '消息中心', requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: { title: '管理后台', requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/reset-user-password',
    name: 'AdminResetUserPassword',
    component: () => import('@/views/admin/ResetUserPassword.vue'),
    meta: { title: '重置用户密码', requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/logs',
    name: 'AdminOperationLogs',
    component: () => import('@/views/admin/OperationLogs.vue'),
    meta: { title: '操作日志', requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/academic',
    name: 'AdminAcademic',
    component: () => import('@/views/admin/AcademicManagement.vue'),
    meta: { title: '学籍管理', requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/timetable',
    name: 'AdminTimetable',
    component: () => import('@/views/admin/TimetableManagement.vue'),
    meta: { title: '课程表', requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/principal',
    name: 'PrincipalDashboard',
    component: () => import('@/views/principal/Dashboard.vue'),
    meta: { title: '校长工作台', requiresAuth: true, roles: ['PRINCIPAL'] }
  },
  {
    path: '/grade-admin',
    name: 'GradeAdminDashboard',
    component: () => import('@/views/grade-admin/Dashboard.vue'),
    meta: { title: '年级管理工作台', requiresAuth: true, roles: ['GRADE_ADMIN'] }
  },
  {
    path: '/teacher',
    name: 'TeacherDashboard',
    component: () => import('@/views/teacher/Dashboard.vue'),
    meta: { title: '教师工作台', requiresAuth: true, roles: ['TEACHER'] }
  },
  {
    path: '/student',
    name: 'StudentDashboard',
    component: () => import('@/views/student/Dashboard.vue'),
    meta: { title: '学生中心', requiresAuth: true, roles: ['STUDENT'] }
  },
  {
    path: '/set-password',
    name: 'SetPassword',
    component: () => import('@/views/auth/SetPassword.vue'),
    meta: { title: '设置初始密码', requiresAuth: true }
  },
  {
    path: '/change-password',
    name: 'ChangePassword',
    component: () => import('@/views/auth/ChangePassword.vue'),
    meta: { title: '修改密码', requiresAuth: true }
  },
  {
    path: '/admin/reset-password',
    name: 'AdminResetPassword',
    component: () => import('@/views/admin/ResetPassword.vue'),
    meta: { title: '锁定用户管理', requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：登录校验 + 初始/密码过期强制处理 + 角色首页重定向 + 角色权限校验 + 页面标题
router.beforeEach((to, from, next) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 校务管理系统`
    : '校务管理系统'

  const token = getToken()
  const user = getUser()
  const status = user?.status

  // 需要登录：未登录则跳登录页
  if (to.meta.requiresAuth && !token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  // 已登录访问登录页 → 按状态跳转（初始→设密码，密码过期→改密码，否则角色首页）
  if (to.path === '/login' && token) {
    if (status === 0) return next('/set-password')
    if (status === 10) return next('/change-password')
    return next(getHomePath(user?.roles))
  }

  // 已登录访问默认首页 '/' → 按状态重定向
  if (to.path === '/' && token) {
    if (status === 0) return next('/set-password')
    if (status === 10) return next('/change-password')
    return next(getHomePath(user?.roles))
  }

  // 初始状态(0)用户：仅允许访问设置初始密码页
  if (status === 0 && to.path !== '/set-password') {
    return next('/set-password')
  }

  // 密码过期(10)用户：仅允许访问修改密码页
  if (status === 10 && to.path !== '/change-password') {
    return next('/change-password')
  }

  // 正常状态用户：不允许再进入「设置初始密码」；「修改密码」是主动行为，允许访问
  if (status !== 0 && status !== 10 && to.path === '/set-password') {
    return next(getHomePath(user?.roles))
  }

  // 角色权限校验：无权访问时跳转回自己的角色首页
  if (to.meta.roles && token) {
    const roles = user?.roles || []
    if (!to.meta.roles.some((r) => roles.includes(r))) {
      return next(getHomePath(roles))
    }
  }

  next()
})

export default router
