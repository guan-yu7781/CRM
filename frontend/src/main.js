import { createApp } from 'vue';
import { createPinia } from 'pinia';
import App from './App.vue';
import router from './router';
import '../../src/main/resources/static/styles.css';
import './assets/frontend.css';

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.mount('#app');
