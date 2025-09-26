import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/', //重定向到首页
      name: 'home',
      component: HomeView,//一开始就加载 组件
    },
    {
      path: '/posts',  //当用户访问 /posts 时，加载 PostsView 组件
      name: 'posts',
      component: () => import('../views/PostListView.vue'),//用懒加载引入
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/create-post',
      name: 'create-post',
      component: () => import('../views/PostCreateView.vue'),
    },
    {
      path: '/posts/:id', //动态路由
      name: 'post-detail',
      component: () => import('../views/PostDetailView.vue'),
    }
  ],
})

import {useAuthStore} from '@/stores/auth';

// 全局前置守卫
router.beforeEach((to,from,next) => {
  const authStore = useAuthStore();

  const requiresAuth = ['/create-post']; // 需要认证的路由列表

  if(requiresAuth.includes(to.name) && !authStore.isAuthenticated) {
    // 如果访问需要认证页面且用户未认证，重定向到登录页面
    next({ name: 'login' });
  }else{
    next(); // 直接next()放行路由
  }

})

export default router
