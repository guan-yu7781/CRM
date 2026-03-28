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
    router.push({ name: 'dashboard' });
  } catch (exception) {
    const status = exception.response?.status;
    if (status === 401 || status === 403 || status === 400) {
      error.value = '用户名或密码错误，请重试。';
    } else {
      error.value = exception.response?.data?.details?.[0] || exception.message || 'Unable to sign in.';
    }
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-root">

    <!-- ── Left: brand panel ── -->
    <div class="brand-panel">
      <div class="brand-inner">
        <div class="logo-card">
          <img src="/assets/murong-logo.png" alt="MuRong Technology" />
        </div>
        <div class="brand-copy">
          <h1>FinLink CRM</h1>
          <p>Banking relationship workspace for modern financial institutions.</p>
        </div>
        <div class="brand-tags" aria-hidden="true">
          <span class="brand-tag">Customer 360</span>
          <span class="brand-tag">Pipeline</span>
          <span class="brand-tag">Maintenance</span>
          <span class="brand-tag">Analytics</span>
        </div>
      </div>
      <div class="brand-footer">MuRong Technology · Banking Solutions</div>
    </div>

    <!-- ── Right: form panel ── -->
    <div class="form-panel">
      <div class="form-inner">

        <div class="form-header">
          <h2>Sign In</h2>
          <p>Welcome back. Enter your credentials to continue.</p>
        </div>

        <form @submit.prevent="submit" class="sign-in-form">
          <div class="field">
            <label for="login-username">Username</label>
            <input
              id="login-username"
              v-model="form.username"
              autocomplete="username"
              placeholder="Enter username"
              required
            />
          </div>

          <div class="field">
            <label for="login-password">Password</label>
            <input
              id="login-password"
              v-model="form.password"
              type="password"
              autocomplete="current-password"
              placeholder="Enter password"
              required
            />
          </div>

          <button class="sign-in-btn" type="submit" :disabled="loading">
            <span>{{ loading ? 'Signing In…' : 'Sign In' }}</span>
          </button>

          <div v-if="error" class="error-bar">{{ error }}</div>
        </form>

        <div class="form-footer">
          FinLink CRM · MuRong Technology · Secure Access
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.login-root {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
  font-family: "IBM Plex Sans", "Segoe UI", sans-serif;
  background: #f0f2f5;
}

/* ── Left brand panel ── */
.brand-panel {
  background: #001529;
  display: flex;
  flex-direction: column;
  padding: 48px 52px;
  position: relative;
}

.brand-inner {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 32px;
}

.logo-card {
  display: inline-flex;
  align-items: center;
  padding: 12px 18px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 6px;
  width: fit-content;
}
.logo-card img {
  height: 36px;
  width: auto;
  object-fit: contain;
}

.brand-copy h1 {
  margin: 0 0 10px;
  font-size: clamp(1.8rem, 3vw, 2.6rem);
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.02em;
  line-height: 1.1;
}
.brand-copy p {
  margin: 0;
  font-size: 0.92rem;
  color: rgba(255, 255, 255, 0.45);
  max-width: 320px;
  line-height: 1.6;
}

.brand-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.brand-tag {
  padding: 5px 12px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.45);
  letter-spacing: 0.04em;
}

.brand-footer {
  font-size: 0.7rem;
  color: rgba(255, 255, 255, 0.2);
  letter-spacing: 0.04em;
}

/* ── Right form panel ── */
.form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  background: #f0f2f5;
}

.form-inner {
  width: 100%;
  max-width: 360px;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.form-header h2 {
  margin: 0 0 6px;
  font-size: 1.75rem;
  font-weight: 600;
  color: #262626;
  letter-spacing: -0.02em;
}
.form-header p {
  margin: 0;
  font-size: 0.88rem;
  color: #8c8c8c;
}

.sign-in-form {
  background: #ffffff;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 28px 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field label {
  font-size: 0.82rem;
  font-weight: 500;
  color: #595959;
}
.field input {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  font: inherit;
  font-size: 0.9rem;
  background: #ffffff;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  color: #262626;
  outline: none;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.field input::placeholder {
  color: #bfbfbf;
}
.field input:focus {
  border-color: #1677ff;
  box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.10);
}

.sign-in-btn {
  height: 40px;
  width: 100%;
  background: #1677ff;
  color: #ffffff;
  font: inherit;
  font-size: 0.9rem;
  font-weight: 600;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
  margin-top: 4px;
}
.sign-in-btn:hover:not(:disabled) {
  background: #4096ff;
}
.sign-in-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-bar {
  padding: 8px 12px;
  background: #fff1f0;
  border: 1px solid #ffa39e;
  border-radius: 6px;
  font-size: 0.83rem;
  color: #cf1322;
}

.form-footer {
  font-size: 0.72rem;
  color: #bfbfbf;
  text-align: center;
  letter-spacing: 0.02em;
}

@media (max-width: 768px) {
  .login-root {
    grid-template-columns: 1fr;
  }
  .brand-panel {
    padding: 32px 28px;
    min-height: 200px;
  }
  .brand-inner {
    gap: 18px;
    justify-content: flex-start;
  }
  .brand-copy h1 {
    font-size: 1.6rem;
  }
  .brand-tags { display: none; }
  .form-panel {
    padding: 32px 20px;
    background: #f0f2f5;
  }
}
</style>
