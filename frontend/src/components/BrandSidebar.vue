<script setup>
import { computed } from 'vue';
import { moduleMenu } from '../lib/permissions';

const props = defineProps({
  collapsed: Boolean,
  currentModule: {
    type: String,
    required: true
  },
  canAccess: {
    type: Function,
    required: true
  },
  username: String,
  roleLabel: String
});

const emit = defineEmits(['toggle', 'logout']);

const visibleModules = computed(() => moduleMenu.filter(item => props.canAccess(item.key)));

const menuIconMap = {
  customers: 'CM',
  contacts: 'CT',
  projects: 'PJ',
  deals: 'OP',
  tasks: 'TS',
  activities: 'IN',
  accessControl: 'AC'
};
</script>

<template>
  <aside class="sidebar">
    <button
      class="ghost-button sidebar-toggle icon-button"
      type="button"
      :aria-expanded="String(!collapsed)"
      :title="collapsed ? 'Show menu' : 'Collapse menu'"
      @click="emit('toggle')"
    >
      {{ collapsed ? '▶' : '◀' }}
    </button>
    <div class="sidebar-brand">
      <div class="brand-logo-frame brand-logo-frame-sidebar">
        <img v-if="!collapsed" src="/assets/murong-logo.png" alt="MuRong Technology logo" class="brand-logo brand-logo-sidebar-full" />
        <img v-else src="/assets/murong-mark.png" alt="MuRong Technology mark" class="brand-logo brand-logo-sidebar-mark" style="display:block" />
      </div>
      <span class="eyebrow">FinLink CRM</span>
      <h2>Banking Workspace</h2>
      <p>Customer, delivery, and maintenance operations in one secure console.</p>
    </div>

    <nav class="menu">
      <div class="menu-section-label" v-if="!collapsed">Workspace</div>
      <router-link
        v-for="module in visibleModules"
        :key="module.key"
        class="menu-item"
        :class="{ active: currentModule === module.key }"
        :to="{ name: 'workspace', params: { module: module.key } }"
      >
        <span class="menu-item-icon">{{ menuIconMap[module.key] || '•' }}</span>
        <span class="menu-item-label">{{ module.label }}</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <div class="user-box">
        <div class="user-box-copy">
          <span>Signed in as</span>
          <strong>{{ username }}</strong>
          <small>{{ roleLabel }}</small>
        </div>
        <button class="ghost-button sidebar-logout" type="button" @click="emit('logout')">
          {{ collapsed ? '⎋' : 'Log Out' }}
        </button>
      </div>
    </div>
  </aside>
</template>
