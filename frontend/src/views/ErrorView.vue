<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  code: { type: String, default: '404' }
});

const router = useRouter();

const config = {
  '403': {
    title: 'Access Denied',
    description: "Sorry, you don't have permission to access this page.",
    hint: 'If you believe this is a mistake, please contact your system administrator.'
  },
  '404': {
    title: 'Page Not Found',
    description: 'Sorry, the page you visited does not exist.',
    hint: 'It may have been removed, renamed, or is temporarily unavailable.'
  },
  '500': {
    title: 'Internal Server Error',
    description: 'Sorry, something went wrong on the server.',
    hint: 'Please try again later or contact your system administrator.'
  }
};

const current = computed(() => config[props.code] || config['404']);
</script>

<template>
  <div class="error-page">
    <div class="error-layout">
      <div class="error-illustration">
        <!-- 403 lock -->
        <svg v-if="code === '403'" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="60" cy="60" r="56" fill="#f0faf6" stroke="#e2ebe7" stroke-width="2"/>
          <rect x="28" y="58" width="64" height="46" rx="10" fill="#ffffff" stroke="#2f9e87" stroke-width="3"/>
          <path d="M38 58V42C38 28 82 28 82 42V58" stroke="#2f9e87" stroke-width="5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
          <circle cx="60" cy="81" r="8" fill="#2f9e87"/>
          <line x1="60" y1="89" x2="60" y2="96" stroke="#2f9e87" stroke-width="4" stroke-linecap="round"/>
        </svg>
        <!-- 404 compass/search -->
        <svg v-else-if="code === '404'" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="60" cy="60" r="56" fill="#f0faf6" stroke="#e2ebe7" stroke-width="2"/>
          <circle cx="52" cy="52" r="26" stroke="#2f9e87" stroke-width="5" fill="#ffffff"/>
          <line x1="71" y1="71" x2="92" y2="92" stroke="#2f9e87" stroke-width="6" stroke-linecap="round"/>
          <line x1="40" y1="52" x2="64" y2="52" stroke="#2f9e87" stroke-width="3.5" stroke-linecap="round"/>
          <line x1="52" y1="40" x2="52" y2="64" stroke="#2f9e87" stroke-width="3.5" stroke-linecap="round"/>
        </svg>
        <!-- 500 warning -->
        <svg v-else viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="60" cy="60" r="56" fill="#f0faf6" stroke="#e2ebe7" stroke-width="2"/>
          <path d="M60 22L98 88H22Z" stroke="#2f9e87" stroke-width="5" fill="#ffffff" stroke-linejoin="round"/>
          <line x1="60" y1="48" x2="60" y2="70" stroke="#2f9e87" stroke-width="5" stroke-linecap="round"/>
          <circle cx="60" cy="80" r="4.5" fill="#2f9e87"/>
        </svg>
      </div>
      <div class="error-body">
        <span class="error-code-badge">{{ code }}</span>
        <h1 class="error-title">{{ current.title }}</h1>
        <p class="error-description">{{ current.description }}</p>
        <p class="error-hint">{{ current.hint }}</p>
        <div class="error-actions">
          <button class="primary-button" type="button" @click="router.push('/dashboard')">Back to Home</button>
          <button class="ghost-button" type="button" @click="router.back()">Go Back</button>
        </div>
      </div>
    </div>
  </div>
</template>
