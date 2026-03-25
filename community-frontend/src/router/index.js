import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        { path: '', name: 'home', component: () => import('@/views/HomeView.vue') },
        { path: 'posts/:id', name: 'post-detail', component: () => import('@/views/PostDetailView.vue') },
        { path: 'create-post', name: 'create-post', component: () => import('@/views/PostCreateView.vue'), meta: { requiresAuth: true } },
        { path: 'edit-post/:id', name: 'edit-post', component: () => import('@/views/PostEditView.vue'), meta: { requiresAuth: true } },
        { path: 'me', name: 'personal-center', component: () => import('@/views/PersonalCenterView.vue'), meta: { requiresAuth: true } },
        { path: 'users/:id', name: 'user-profile', component: () => import('@/views/UserProfileView.vue') },
      ],
    },
    {
      path: '/login',
      component: () => import('@/layouts/BlankLayout.vue'),
      children: [
        { path: '', name: 'login', component: () => import('@/views/LoginView.vue') },
      ],
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', name: 'admin-dashboard', component: () => import('@/views/admin/DashboardView.vue') },
        { path: 'categories', name: 'admin-categories', component: () => import('@/views/admin/CategoryManageView.vue') },
        { path: 'users', name: 'admin-users', component: () => import('@/views/admin/UserManageView.vue') },
        { path: 'posts', name: 'admin-posts', component: () => import('@/views/admin/PostReviewView.vue') },
      ],
    },
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    next({ name: 'home' })
    return
  }

  next()
})

export default router
