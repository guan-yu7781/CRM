import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import DashboardView from '../views/DashboardView.vue';
import WorkspaceView from '../views/WorkspaceView.vue';
import Customer360View from '../views/Customer360View.vue';
import MaintenanceView from '../views/MaintenanceView.vue';
import ErrorView from '../views/ErrorView.vue';

const routes = [
  { path: '/', name: 'root-login', component: LoginView, meta: { public: true } },
  { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
  { path: '/dashboard', name: 'dashboard', component: DashboardView },
  { path: '/app/:module?', name: 'workspace', component: WorkspaceView },
  { path: '/customer-360/:customerId', name: 'customer360', component: Customer360View },
  { path: '/maintenance', name: 'maintenance', component: MaintenanceView },
  { path: '/403', name: 'error-403', component: ErrorView, props: { code: '403' }, meta: { public: true } },
  { path: '/500', name: 'error-500', component: ErrorView, props: { code: '500' }, meta: { public: true } },
  { path: '/:pathMatch(.*)*', name: 'error-404', component: ErrorView, props: { code: '404' }, meta: { public: true } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to) => {
  const token = localStorage.getItem('crmToken');
  if (!to.meta.public && !token) {
    return { name: 'login' };
  }
  return true;
});

export default router;
