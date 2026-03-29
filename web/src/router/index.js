import { createRouter as createVueRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import MainLayout from '../layouts/MainLayout.vue';
import Login from '../pages/Login.vue';
import Register from '../pages/Register.vue';
import ForgotPassword from '../pages/ForgotPassword.vue';
import Dashboard from '../pages/Dashboard.vue';
import ExpressManage from '../pages/express/ExpressManage.vue';
import ExpressCheckin from '../pages/express/ExpressCheckin.vue';
import SendOrderManage from '../pages/send/SendOrderManage.vue';
import ShelfManage from '../pages/shelf/ShelfManage.vue';
import UserManage from '../pages/user/UserManage.vue';
import NotFound from '../pages/NotFound.vue';

const publicRoutes = ['/login', '/register', '/forgot'];

const routes = [
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/forgot', component: ForgotPassword },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', component: Dashboard },
      { path: 'expresses', component: ExpressManage, meta: { roles: ['USER', 'STAFF', 'ADMIN'] } },
      { path: 'send-orders', component: SendOrderManage, meta: { roles: ['USER', 'STAFF', 'ADMIN'] } },
      { path: 'express/manage', redirect: '/expresses' },
      { path: 'express/checkin', component: ExpressCheckin, meta: { roles: ['STAFF', 'ADMIN'] } },
      { path: 'shelves', component: ShelfManage, meta: { roles: ['STAFF', 'ADMIN'] } },
      { path: 'users', component: UserManage, meta: { roles: ['ADMIN'] } }
    ]
  },
  { path: '/:pathMatch(.*)*', component: NotFound }
];

export const createRouter = () => {
  const router = createVueRouter({
    history: createWebHistory(),
    routes
  });

  router.beforeEach((to) => {
    const auth = useAuthStore();
    if (publicRoutes.includes(to.path)) {
      return true;
    }
    if (!auth.token) {
      return '/login';
    }
    auth.restore();
    const roles = to.meta?.roles;
    if (roles && !roles.includes(auth.role)) {
      return '/';
    }
    return true;
  });

  return router;
};
