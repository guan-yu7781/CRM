<script setup>
import { computed } from 'vue';
import { moduleMenu } from '../lib/permissions';

const props = defineProps({
  collapsed: Boolean,
  currentModule: { type: String, required: true },
  canAccess: { type: Function, required: true },
  username: String,
  roleLabel: String
});

const emit = defineEmits(['toggle', 'logout']);

const visibleModules = computed(() => moduleMenu.filter(item => props.canAccess(item.key)));

// Feather-style SVG path data (viewBox 0 0 24 24, stroke-only)
const menuIcons = {
  dashboard:
    '<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>',
  customers:
    '<path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>',
  contacts:
    '<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>',
  projects:
    '<path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>',
  deals:
    '<polyline points="22 7 13.5 15.5 8.5 10.5 2 17"/><polyline points="16 7 22 7 22 13"/>',
  tasks:
    '<path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>',
  activities:
    '<path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>',
  accessControl:
    '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>',
  auditLogs:
    '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/>',
};
</script>

<template>
  <aside class="sidebar" :class="{ 'sidebar-collapsed': collapsed }">

    <!-- ── Brand ── -->
    <div class="sidebar-brand">
      <div class="brand-logo-wrap">
        <img
          v-if="!collapsed"
          src="/assets/murong-logo.png"
          alt="FinLink CRM"
          class="brand-logo-full"
        />
        <img
          v-else
          src="/assets/murong-mark.png"
          alt="FinLink"
          class="brand-logo-mark"
        />
      </div>
      <span v-if="!collapsed" class="brand-product-name">FinLink CRM</span>
      <button
        class="sidebar-toggle-btn sidebar-toggle-brand"
        type="button"
        :title="collapsed ? 'Expand menu' : 'Collapse menu'"
        :aria-expanded="String(!collapsed)"
        @click="emit('toggle')"
      >
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
             stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <polyline v-if="!collapsed" points="15 18 9 12 15 6"/>
          <polyline v-else points="9 18 15 12 9 6"/>
        </svg>
      </button>
    </div>

    <!-- ── Navigation ── -->
    <nav class="menu">
      <div v-if="!collapsed" class="menu-section-label">Main</div>
      <router-link
        class="menu-item"
        :class="{ active: currentModule === 'dashboard' }"
        :to="{ name: 'dashboard' }"
        :title="collapsed ? 'Dashboard' : undefined"
      >
        <span class="menu-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75"
               stroke-linecap="round" stroke-linejoin="round" v-html="menuIcons.dashboard" />
        </span>
        <span v-if="!collapsed" class="menu-item-label">Dashboard</span>
      </router-link>

      <div v-if="!collapsed" class="menu-section-label">Workspace</div>
      <router-link
        v-for="module in visibleModules"
        :key="module.key"
        class="menu-item"
        :class="{ active: currentModule === module.key }"
        :to="module.routeName ? { name: module.routeName } : { name: 'workspace', params: { module: module.key } }"
        :title="collapsed ? module.label : undefined"
      >
        <span class="menu-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75"
               stroke-linecap="round" stroke-linejoin="round"
               v-html="menuIcons[module.key] || menuIcons.tasks" />
        </span>
        <span v-if="!collapsed" class="menu-item-label">{{ module.label }}</span>
      </router-link>
    </nav>

    <!-- ── Footer ── -->
    <div class="sidebar-footer">
      <div class="user-box" :class="{ 'user-box-collapsed': collapsed }">
        <template v-if="!collapsed">
          <div class="user-box-copy">
            <span class="user-label">Signed in</span>
            <strong>{{ username }}</strong>
            <small>{{ roleLabel }}</small>
          </div>
          <button class="sidebar-logout" type="button" title="Log Out" @click="emit('logout')">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
          </button>
        </template>
        <template v-else>
          <button class="sidebar-logout sidebar-logout-icon" type="button" title="Log Out" @click="emit('logout')">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
          </button>
        </template>
      </div>

    </div>

  </aside>
</template>
