import { defineStore } from 'pinia';
import { reactive, ref } from 'vue';
import api from '../lib/api';

export const useCrmStore = defineStore('crm', () => {
  const loading = ref(false);
  const accessRoles = ref([]);
  const accountManagers = ref([]);
  const data = reactive({
    customers: [],
    contacts: [],
    projects: [],
    deals: [],
    tasks: [],
    activities: [],
    accessControl: []
  });

  async function loadWorkspace(authStore) {
    loading.value = true;
    try {
      const requests = [];
      const bindings = [];
      const tryLoad = (key, url, permission) => {
        if (authStore.hasPermission(permission)) {
          bindings.push(key);
          requests.push(api.get(url));
        } else {
          data[key] = [];
        }
      };

      // Account managers are available to any authenticated user
      try {
        accountManagers.value = (await api.get('/api/users/account-managers')).data;
      } catch (_) {
        accountManagers.value = [];
      }

      tryLoad('customers', '/api/customers', 'CUSTOMER_VIEW');
      tryLoad('contacts', '/api/contacts', 'CONTACT_VIEW');
      tryLoad('projects', '/api/projects', 'PROJECT_VIEW');
      tryLoad('deals', '/api/deals', 'DEAL_VIEW');
      tryLoad('tasks', '/api/tasks', 'TASK_VIEW');
      tryLoad('activities', '/api/activities', 'ACTIVITY_VIEW');

      if (authStore.hasPermission('ACCESS_CONTROL_VIEW')) {
        bindings.push('accessControl');
        requests.push(api.get('/api/admin/access-control/users'));
        accessRoles.value = (await api.get('/api/admin/access-control/roles')).data;
      } else {
        data.accessControl = [];
        accessRoles.value = [];
      }

      const responses = await Promise.all(requests);
      responses.forEach((response, index) => {
        data[bindings[index]] = response.data;
      });
    } finally {
      loading.value = false;
    }
  }

  async function saveRecord(moduleKey, payload, id = null) {
    const baseMap = {
      customers: '/api/customers',
      contacts: '/api/contacts',
      projects: '/api/projects',
      deals: '/api/deals',
      tasks: '/api/tasks',
      activities: '/api/activities',
      accessControl: '/api/admin/access-control/users'
    };
    const url = id ? `${baseMap[moduleKey]}/${id}` : baseMap[moduleKey];
    const method = id ? 'put' : 'post';
    const { data: response } = await api[method](url, payload);
    return response;
  }

  async function deleteRecord(moduleKey, id) {
    const baseMap = {
      customers: '/api/customers',
      contacts: '/api/contacts',
      projects: '/api/projects',
      deals: '/api/deals',
      tasks: '/api/tasks',
      activities: '/api/activities'
    };
    await api.delete(`${baseMap[moduleKey]}/${id}`);
  }

  return {
    loading,
    data,
    accessRoles,
    accountManagers,
    loadWorkspace,
    saveRecord,
    deleteRecord
  };
});
