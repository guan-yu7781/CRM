import { computed, ref } from 'vue';
import { defineStore } from 'pinia';
import api, { setApiToken } from '../lib/api';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('crmToken') || '');
  const username = ref(localStorage.getItem('crmUsername') || '');
  const role = ref(localStorage.getItem('crmRole') || '');
  const roleLabel = ref(localStorage.getItem('crmRoleLabel') || '');
  const dataScope = ref(localStorage.getItem('crmDataScope') || '');
  const permissions = ref(JSON.parse(localStorage.getItem('crmPermissions') || '[]'));

  setApiToken(token.value);

  const isAuthenticated = computed(() => !!token.value);

  function applyProfile(data) {
    token.value = data.token || token.value;
    username.value = data.username || '';
    role.value = data.role || '';
    roleLabel.value = data.roleLabel || data.role || '';
    dataScope.value = data.dataScope || '';
    permissions.value = Array.isArray(data.permissions) ? data.permissions : [];
    localStorage.setItem('crmToken', token.value);
    localStorage.setItem('crmUsername', username.value);
    localStorage.setItem('crmRole', role.value);
    localStorage.setItem('crmRoleLabel', roleLabel.value);
    localStorage.setItem('crmDataScope', dataScope.value);
    localStorage.setItem('crmPermissions', JSON.stringify(permissions.value));
    setApiToken(token.value);
  }

  async function login(payload) {
    const { data } = await api.post('/api/auth/login', payload);
    applyProfile(data);
    return data;
  }

  async function fetchProfile() {
    if (!token.value) return null;
    const { data } = await api.get('/api/auth/me');
    applyProfile({ ...data, token: token.value });
    return data;
  }

  function logout() {
    token.value = '';
    username.value = '';
    role.value = '';
    roleLabel.value = '';
    dataScope.value = '';
    permissions.value = [];
    localStorage.removeItem('crmToken');
    localStorage.removeItem('crmUsername');
    localStorage.removeItem('crmRole');
    localStorage.removeItem('crmRoleLabel');
    localStorage.removeItem('crmDataScope');
    localStorage.removeItem('crmPermissions');
    setApiToken('');
  }

  function hasPermission(permission) {
    return permissions.value.includes(permission);
  }

  return {
    token,
    username,
    role,
    roleLabel,
    dataScope,
    permissions,
    isAuthenticated,
    applyProfile,
    login,
    fetchProfile,
    logout,
    hasPermission
  };
});
