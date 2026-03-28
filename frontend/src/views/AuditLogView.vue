<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import BrandSidebar from '../components/BrandSidebar.vue';
import { useAuthStore } from '../stores/auth';
import { modulePermissions } from '../lib/permissions';
import { formatDateTime } from '../lib/formatters';
import api from '../lib/api';

const router = useRouter();
const auth = useAuthStore();

const sidebarCollapsed = ref(false);
const loading = ref(false);
const records = ref([]);
const totalElements = ref(0);
const totalPages = ref(0);
const isSuperAdmin = ref(false);

const filterObjectType = ref('');
const filterAction = ref('');
const filterActorUsername = ref('');
const filterStartDate = ref('');
const filterEndDate = ref('');
const page = ref(0);
const pageSize = 50;

const OBJECT_TYPES = ['CUSTOMER', 'CONTACT', 'PROJECT', 'DEAL', 'TASK', 'USER'];
const ACTIONS = ['CREATE', 'UPDATE', 'DELETE'];

const ACTION_COLORS = {
  CREATE: '#22c55e',
  UPDATE: '#f59e0b',
  DELETE: '#ef4444'
};

const totalCount = computed(() => totalElements.value);
const startIndex = computed(() => page.value * pageSize + 1);
const endIndex = computed(() => Math.min((page.value + 1) * pageSize, totalElements.value));

function canAccess(moduleKey) {
  return auth.hasPermission(modulePermissions[moduleKey]?.view);
}

function signOut() {
  auth.logout();
  router.push({ name: 'login' });
}

async function loadLogs() {
  loading.value = true;
  try {
    const params = { page: page.value, size: pageSize };
    if (filterObjectType.value) params.objectType = filterObjectType.value;
    if (filterAction.value) params.action = filterAction.value;
    if (filterActorUsername.value && isSuperAdmin.value) params.actorUsername = filterActorUsername.value;
    if (filterStartDate.value) params.startDate = filterStartDate.value;
    if (filterEndDate.value) params.endDate = filterEndDate.value;

    const res = await api.get('/api/audit-logs', { params });
    records.value = res.data.content;
    totalElements.value = res.data.totalElements;
    totalPages.value = res.data.totalPages;
    isSuperAdmin.value = res.data.isSuperAdmin;
  } catch (e) {
    records.value = [];
  } finally {
    loading.value = false;
  }
}

function applyFilters() {
  page.value = 0;
  loadLogs();
}

function clearFilters() {
  filterObjectType.value = '';
  filterAction.value = '';
  filterActorUsername.value = '';
  filterStartDate.value = '';
  filterEndDate.value = '';
  page.value = 0;
  loadLogs();
}

function prevPage() {
  if (page.value > 0) {
    page.value--;
    loadLogs();
  }
}

function nextPage() {
  if (page.value < totalPages.value - 1) {
    page.value++;
    loadLogs();
  }
}

function shortUA(ua) {
  if (!ua) return '—';
  const m = ua.match(/(Chrome|Firefox|Safari|Edge|OPR|Opera)\/[\d.]+/);
  return m ? m[0] : ua.substring(0, 40);
}

onMounted(async () => {
  try {
    if (!auth.isAuthenticated) {
      router.push({ name: 'login' });
      return;
    }
    await auth.fetchProfile();
    await loadLogs();
  } catch {
    auth.logout();
    router.push({ name: 'login' });
  }
});
</script>

<template>
  <div class="app-shell vue-app-shell" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <BrandSidebar
      :collapsed="sidebarCollapsed"
      current-module="auditLogs"
      :can-access="canAccess"
      :username="auth.username"
      :role-label="auth.roleLabel"
      @toggle="sidebarCollapsed = !sidebarCollapsed"
      @logout="signOut"
    />

    <main class="content vue-page">
      <header class="page-header vue-page-header">
        <div class="workspace-header-copy">
          <span class="eyebrow">Security &amp; Compliance</span>
          <h1>Audit Log</h1>
        </div>
        <div class="header-actions">
          <button class="ghost-button" type="button" @click="loadLogs">Refresh</button>
        </div>
      </header>

      <!-- Filter Bar -->
      <section class="audit-filter-bar">
        <div class="audit-filters">
          <div class="audit-filter-item">
            <label class="audit-filter-label">Object Type</label>
            <select v-model="filterObjectType" class="audit-filter-select">
              <option value="">All Types</option>
              <option v-for="t in OBJECT_TYPES" :key="t" :value="t">{{ t }}</option>
            </select>
          </div>
          <div class="audit-filter-item">
            <label class="audit-filter-label">Action</label>
            <select v-model="filterAction" class="audit-filter-select">
              <option value="">All Actions</option>
              <option v-for="a in ACTIONS" :key="a" :value="a">{{ a }}</option>
            </select>
          </div>
          <div class="audit-filter-item">
            <label class="audit-filter-label">From</label>
            <input v-model="filterStartDate" type="date" class="audit-filter-input" />
          </div>
          <div class="audit-filter-item">
            <label class="audit-filter-label">To</label>
            <input v-model="filterEndDate" type="date" class="audit-filter-input" />
          </div>
          <div v-if="isSuperAdmin" class="audit-filter-item">
            <label class="audit-filter-label">Operator</label>
            <input v-model="filterActorUsername" type="text" class="audit-filter-input" placeholder="Username" />
          </div>
          <div class="audit-filter-actions">
            <button class="primary-button" type="button" @click="applyFilters">Search</button>
            <button class="ghost-button" type="button" @click="clearFilters">Clear</button>
          </div>
        </div>
      </section>

      <!-- Table -->
      <section class="shell-content workspace-single">
        <section class="records-panel vue-records-panel">
          <div class="records-toolbar">
            <div>
              <strong class="panel-title">Operation Records</strong>
              <span class="panel-subtitle">
                <template v-if="totalElements > 0">
                  Showing {{ startIndex }}–{{ endIndex }} of {{ totalCount }} entries
                </template>
                <template v-else>No records found</template>
              </span>
            </div>
          </div>

          <div v-if="loading" class="audit-empty">Loading...</div>
          <div v-else-if="!records.length" class="audit-empty">No audit records match the current filters.</div>
          <div v-else class="module-table-wrap">
            <table class="module-table">
              <thead>
                <tr>
                  <th>Time</th>
                  <th>Operator</th>
                  <th>Action</th>
                  <th>Object</th>
                  <th>Change Summary</th>
                  <th>IP Address</th>
                  <th>Browser</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in records" :key="row.id">
                  <td>
                    <div class="table-primary">
                      <small style="white-space: nowrap;">{{ formatDateTime(row.createdAt) }}</small>
                    </div>
                  </td>
                  <td>
                    <div class="table-primary">
                      <strong>{{ row.actorUsername }}</strong>
                      <small v-if="row.actorFullName">{{ row.actorFullName }}</small>
                    </div>
                  </td>
                  <td>
                    <span class="audit-action-badge" :style="{ backgroundColor: ACTION_COLORS[row.action] + '22', color: ACTION_COLORS[row.action], border: '1px solid ' + ACTION_COLORS[row.action] + '55' }">
                      {{ row.action }}
                    </span>
                  </td>
                  <td>
                    <div class="table-primary">
                      <strong>{{ row.objectName || '—' }}</strong>
                      <small>{{ row.objectType }} #{{ row.objectId }}</small>
                    </div>
                  </td>
                  <td>
                    <span class="audit-summary">{{ row.changeSummary || '—' }}</span>
                  </td>
                  <td>
                    <small>{{ row.ipAddress || '—' }}</small>
                  </td>
                  <td>
                    <small :title="row.userAgent || ''">{{ shortUA(row.userAgent) }}</small>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="audit-pagination">
            <button class="ghost-button" type="button" :disabled="page === 0" @click="prevPage">← Prev</button>
            <span class="audit-page-info">Page {{ page + 1 }} of {{ totalPages }}</span>
            <button class="ghost-button" type="button" :disabled="page >= totalPages - 1" @click="nextPage">Next →</button>
          </div>
        </section>
      </section>
    </main>
  </div>
</template>

<style scoped>
.audit-filter-bar {
  padding: 12px 24px;
  border-bottom: 1px solid var(--border, #e5e7eb);
  background: var(--surface, #fff);
}

.audit-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-end;
}

.audit-filter-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.audit-filter-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--text-muted, #6b7280);
}

.audit-filter-select,
.audit-filter-input {
  height: 32px;
  padding: 0 10px;
  border: 1px solid var(--border, #d1d5db);
  border-radius: 6px;
  font-size: 13px;
  background: var(--surface, #fff);
  color: var(--text, #111827);
  min-width: 140px;
}

.audit-filter-input {
  min-width: 130px;
}

.audit-filter-actions {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.audit-empty {
  padding: 40px 24px;
  text-align: center;
  color: var(--text-muted, #6b7280);
  font-size: 14px;
}

.audit-action-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.05em;
  white-space: nowrap;
}

.audit-summary {
  font-size: 13px;
  color: var(--text, #374151);
  max-width: 280px;
  display: block;
}

.audit-pagination {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid var(--border, #e5e7eb);
}

.audit-page-info {
  font-size: 13px;
  color: var(--text-muted, #6b7280);
}
</style>
