import { createRouter, createWebHistory } from 'vue-router';
import LoginView from '../views/LoginView.vue';
import WorkspaceView from '../views/WorkspaceView.vue';
import Customer360View from '../views/Customer360View.vue';
import MaintenanceView from '../views/MaintenanceView.vue';

const routes = [
  { path: '/', redirect: '/app/customers' },
  { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
  { path: '/app/:module?', name: 'workspace', component: WorkspaceView },
  { path: '/customer-360/:customerId', name: 'customer360', component: Customer360View },
  { path: '/maintenance', name: 'maintenance', component: MaintenanceView }
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
  if (to.meta.public && token && to.name === 'login') {
    return { name: 'workspace', params: { module: 'customers' } };
  }
  return true;
});

export default router;
