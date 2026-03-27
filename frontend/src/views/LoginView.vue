<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const router = useRouter();
const auth = useAuthStore();
const form = reactive({ username: '', password: '' });
const loading = ref(false);
const error = ref('');

async function submit() {
  loading.value = true;
  error.value = '';
  try {
    await auth.login(form);
    router.push({ name: 'workspace', params: { module: 'customers' } });
  } catch (exception) {
    error.value = exception.response?.data?.details?.[0] || exception.message || 'Unable to sign in.';
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="auth-login-page">
    <section class="auth-login-shell">
      <div class="auth-brand-panel">
        <div class="auth-brand-header">
          <div class="auth-brand-mark">
            <img src="/assets/murong-logo.png" alt="MuRong Technology logo" />
          </div>
          <div class="auth-brand-copy">
            <span class="eyebrow">FinLink CRM</span>
            <h1>FinLink CRM</h1>
            <p>Relationship workspace for banking teams.</p>
          </div>
        </div>
      </div>

      <div class="auth-panel">
        <div class="login-form-head">
          <span class="eyebrow">Sign In</span>
          <h2>Welcome back</h2>
        </div>

        <form class="auth-form" @submit.prevent="submit">
          <label class="auth-field">
            <span>Username</span>
            <input v-model="form.username" required />
          </label>
          <label class="auth-field">
            <span>Password</span>
            <input v-model="form.password" type="password" required />
          </label>
          <button class="primary-button" type="submit" :disabled="loading">
            {{ loading ? 'Signing In...' : 'Sign In' }}
          </button>
          <p v-if="error" class="status-text">{{ error }}</p>
        </form>
      </div>
    </section>
  </div>
</template>
