import { createRouter, createWebHistory } from 'vue-router'

// history: createWebHistory(import.meta.env.BASE_URL),
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home',
    },
    {
      path: '/authLayout',
      redirect: '/login',
      component: () => import('@/views/login/LayoutView.vue'),
      children: [
        {
          path: '/login',
          name: 'login',
          // route level code-splitting
          // this generates a separate chunk (About.[hash].js) for this route
          // which is lazy-loaded when the route is visited.
          component: () => import('@/views/login/LoginView.vue')
        },
        {
          path: '/registration',
          name: 'registration',
          component: () => import('@/views/login/RegistrationView.vue')
        },
        {
          path: '/requestResetPassword',
          name: 'requestResetPassword',
          component: () => import('@/views/login/RequestResetPasswordView.vue')
        },
        {
          path: '/resetPassVerification',
          name: 'resetPassVerification',
          component: () => import('@/views/login/ResetPassVerificationView.vue')
        },
        {
          path: '/resetPassword',
          name: 'resetPassword',
          component: () => import('@/views/login/ResetPasswordView.vue')
        },
        {
          path: '/emailVerification',
          name: 'emailVerification',
          component: () => import('@/views/login/EmailVerificationView.vue')
        }
      ]
    },
    {
      path: '/studentLayout',
      redirect: '/student/home',
      component: () => import('@/views/student/LayoutView.vue'),
      meta: { requireAuth: true, role: 'Student' },
      children: [
        {
          path: '/student/home',
          name: 'studentHome',
          component: () => import('@/views/student/HomeView.vue'),
        },
        {
          path: '/student/application',
          name: 'studentApplication',
          component: () => import('@/views/student/ApplicationView.vue'),
        },
        {
          path: '/student/profile',
          name: 'studentProfile',
          component: () => import('@/views/student/ProfileView.vue'),
        }
      ]
    },
    {
      path: '/adminLayout',
      redirect: '/admin/studentsList',
      component: () => import('@/views/admin/LayoutView.vue'),
      meta: { requireAuth: true, role: 'Admin' },
      children: [
        {
          path: '/admin/studentsList',
          name: 'studentsList',
          component: () => import('@/views/admin/StudentsListView.vue')
        },
        {
          path: '/admin/studentsList/:id',
          name: 'studentDetails',
          component: () => import('@/views/admin/StudentsDetailsView.vue'),
          // Allow Router Parameter
          props: true
        }
      ]
    },
    {
      path: '/clientLayout',
      redirect: '/client/candidatesList',
      component: () => import('@/views/client/LayoutView.vue'),
      meta: { requireAuth: true, role: 'Industry' },
      children: [
        {
          path: '/client/candidatesList',
          name: 'candidateList',
          component: () => import('@/views/client/CandidatesListView.vue')
        },
        {
          path: '/client/candidatesList/:id',
          name: 'candidateDetails',
          component: () => import('@/views/client/CandidatesDetailView.vue'),
          props: true
        }
      ]
    },
    {
      path: '/home',
      name: 'home',
      component: () => import('@/views/HomeView.vue')
    },
    {
      path: '/unauthorized',
      name: 'unauthorized',
      component: () => import('@/views/401View.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'notFound',
      component: () => import('@/views/404View.vue')
    }
  ]
})

export default router
