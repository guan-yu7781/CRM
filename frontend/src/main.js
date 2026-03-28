import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import { setupApiInterceptors } from './lib/api';
import './assets/frontend.css';

const app = createApp(App);
app.use(createPinia());
app.use(router);
setupApiInterceptors(router);
app.mount('#app');
