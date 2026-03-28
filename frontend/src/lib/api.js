import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || ''
});

export function setApiToken(token) {
  if (token) {
    api.defaults.headers.common.Authorization = `Bearer ${token}`;
  } else {
    delete api.defaults.headers.common.Authorization;
  }
}

/**
 * Wire up global error navigation. Called from main.js after the router is ready
 * to avoid circular dependency between api.js and router/index.js.
 */
export function setupApiInterceptors(router) {
  api.interceptors.response.use(
    res => res,
    err => {
      const url = err.config?.url || '';
      const status = err.response?.status;

      // Auth endpoints handle their own errors inline — skip global handling
      if (url.includes('/api/auth/')) return Promise.reject(err);

      if (status === 401) {
        // Token expired or invalid — clear session and go to login
        Object.keys(localStorage)
          .filter(k => k.startsWith('crm'))
          .forEach(k => localStorage.removeItem(k));
        router.push({ name: 'login' });
      } else if (status === 403) {
        router.push({ name: 'error-403' });
      } else if (status >= 500) {
        router.push({ name: 'error-500' });
      }

      return Promise.reject(err);
    }
  );
}

export default api;
