<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import BrandSidebar from '../components/BrandSidebar.vue';
import { useAuthStore } from '../stores/auth';
import { useCrmStore } from '../stores/crm';
import { formatMoney, formatDate, formatDateTime, beautify } from '../lib/formatters';
import { modulePermissions } from '../lib/permissions';
import WorldMapPanel from '../components/WorldMapPanel.vue';

const router = useRouter();
const auth = useAuthStore();
const crm = useCrmStore();

const sidebarCollapsed = ref(false);

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 17) return 'Good afternoon';
  return 'Good evening';
});

const todayLabel = computed(() =>
  new Date().toLocaleDateString('en-GB', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })
);

// ── Derived metrics ──────────────────────────────────────────────────────────

const totalCustomers = computed(() => crm.data.customers.length);
const activeCustomers = computed(() => crm.data.customers.filter(c => c.status === 'ACTIVE').length);

const openDeals = computed(() => crm.data.deals.filter(d => d.stage !== 'WON' && d.stage !== 'LOST'));
const pipelineValue = computed(() => openDeals.value.reduce((s, d) => s + Number(d.amount || 0), 0));
const wonDeals = computed(() => crm.data.deals.filter(d => d.stage === 'WON'));
const wonValue = computed(() => wonDeals.value.reduce((s, d) => s + Number(d.amount || 0), 0));

const activeProjects = computed(() => crm.data.projects.filter(p => p.status === 'LIVE' || p.status === 'IN_PROGRESS'));

const openTasks = computed(() => crm.data.tasks.filter(t => t.status === 'OPEN' || t.status === 'IN_PROGRESS'));
const highPriorityTaskCount = computed(() => openTasks.value.filter(t => t.priority === 'HIGH').length);

const urgentTasks = computed(() => {
  const order = { HIGH: 0, MEDIUM: 1, LOW: 2 };
  return [...openTasks.value]
    .sort((a, b) => (order[a.priority] ?? 3) - (order[b.priority] ?? 3))
    .slice(0, 6);
});

const DEAL_STAGES = ['NEW', 'QUALIFIED', 'PROPOSAL_SENT', 'NEGOTIATION', 'WON', 'LOST'];
const pipelineByStage = computed(() =>
  DEAL_STAGES.map(stage => ({
    stage,
    count: crm.data.deals.filter(d => d.stage === stage).length,
    value: crm.data.deals.filter(d => d.stage === stage).reduce((s, d) => s + Number(d.amount || 0), 0)
  })).filter(s => s.count > 0)
);

const recentActivities = computed(() =>
  [...crm.data.activities]
    .sort((a, b) => new Date(b.activityDate) - new Date(a.activityDate))
    .slice(0, 6)
);

const highRisk = computed(() => crm.data.customers.filter(c => c.riskLevel === 'HIGH').length);
const mediumRisk = computed(() => crm.data.customers.filter(c => c.riskLevel === 'MEDIUM').length);
const lowRisk = computed(() => crm.data.customers.filter(c => c.riskLevel === 'LOW').length);

const totalUsers = computed(() => crm.data.accessControl.length);

const activeMarkets = computed(() => {
  const counts = new Map();
  crm.data.projects.forEach(p => { if (p.market) counts.set(p.market, (counts.get(p.market) || 0) + 1); });
  return Array.from(counts.entries()).map(([name, count]) => ({ name, count }));
});

const dealMarkets = computed(() => {
  const counts = new Map();
  crm.data.deals
    .filter(d => d.stage !== 'WON' && d.stage !== 'LOST' && d.market)
    .forEach(d => counts.set(d.market, (counts.get(d.market) || 0) + 1));
  return Array.from(counts.entries()).map(([name, count]) => ({ name, count }));
});

// ── Summary cards (permission-aware) ─────────────────────────────────────────

const summaryCards = computed(() => {
  const cards = [];
  if (auth.hasPermission('CUSTOMER_VIEW')) {
    cards.push({
      label: 'Total Customers',
      value: String(totalCustomers.value),
      note: `${activeCustomers.value} active relationships`,
      accent: false
    });
  }
  if (auth.hasPermission('DEAL_VIEW')) {
    cards.push({
      label: 'Open Pipeline',
      value: formatMoney(pipelineValue.value),
      note: `${openDeals.value.length} active opportunities`,
      accent: true
    });
  }
  if (auth.hasPermission('PROJECT_VIEW')) {
    cards.push({
      label: 'Live Projects',
      value: String(activeProjects.value.length),
      note: `of ${crm.data.projects.length} total`,
      accent: false
    });
  }
  if (auth.hasPermission('TASK_VIEW')) {
    cards.push({
      label: 'Open Tasks',
      value: String(openTasks.value.length),
      note: `${highPriorityTaskCount.value} high priority`,
      accent: highPriorityTaskCount.value > 0
    });
  }
  return cards;
});

// ── Helpers ───────────────────────────────────────────────────────────────────

function stageStatusClass(stage) {
  if (stage === 'WON') return 'status-active';
  if (stage === 'LOST') return 'status-orange';
  if (stage === 'NEGOTIATION' || stage === 'PROPOSAL_SENT') return 'status-yellow';
  return 'status-neutral';
}

function taskPriorityClass(task) {
  if (task.priority === 'HIGH') return 'status-orange';
  if (task.priority === 'MEDIUM') return 'status-yellow';
  return 'status-neutral';
}

function isOverdue(dateStr) {
  if (!dateStr) return false;
  return new Date(dateStr) < new Date(new Date().toDateString());
}

function activityTypeAbbr(type) {
  const map = { CALL: 'CA', EMAIL: 'EM', MEETING: 'MT', NOTE: 'NT', FOLLOW_UP: 'FU' };
  return map[type] || '??';
}

function canAccess(moduleKey) {
  return auth.hasPermission(modulePermissions[moduleKey]?.view);
}

function signOut() {
  auth.logout();
  router.push({ name: 'login' });
}

function goToModule(key) {
  router.push({ name: 'workspace', params: { module: key } });
}

onMounted(async () => {
  try {
    if (!auth.isAuthenticated) {
      router.push({ name: 'login' });
      return;
    }
    await auth.fetchProfile();
    await crm.loadWorkspace(auth);
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
      current-module="dashboard"
      :can-access="canAccess"
      :username="auth.username"
      :role-label="auth.roleLabel"
      @toggle="sidebarCollapsed = !sidebarCollapsed"
      @logout="signOut"
    />

    <main class="content vue-page dashboard-page">

      <!-- ── Page Header ─────────────────────────────────────────────────── -->
      <header class="page-header vue-page-header dashboard-header">
        <div class="workspace-header-copy">
          <span class="eyebrow">Overview</span>
          <h1>{{ greeting }}, {{ auth.username }}</h1>
          <p>{{ todayLabel }}</p>
        </div>
        <div class="workspace-header-meta">
          <div class="workspace-meta-card">
            <span>Business Role</span>
            <strong>{{ auth.roleLabel || 'User' }}</strong>
            <small>{{ auth.dataScope || 'Scoped' }} access</small>
          </div>
          <div class="workspace-meta-card">
            <span>Workspace</span>
            <strong>FinLink CRM</strong>
            <small>Banking operations</small>
          </div>
        </div>
        <div class="header-actions">
          <button class="ghost-button" type="button" @click="crm.loadWorkspace(auth)">Refresh</button>
        </div>
      </header>

      <!-- ── Loading State ───────────────────────────────────────────────── -->
      <div v-if="crm.loading" class="dashboard-loading">
        <span>Loading workspace data…</span>
      </div>

      <template v-else>

        <!-- ── Summary Bar ─────────────────────────────────────────────── -->
        <section v-if="summaryCards.length" class="summary-bar vue-summary-bar">
          <article
            v-for="card in summaryCards"
            :key="card.label"
            class="summary-card"
            :class="{ 'summary-card-accent': card.accent }"
          >
            <span>{{ card.label }}</span>
            <strong>{{ card.value }}</strong>
            <small>{{ card.note }}</small>
          </article>
        </section>

        <!-- ── Main Content ────────────────────────────────────────────── -->
        <section class="shell-content dashboard-grid">

          <!-- ── Market Presence Map ─────────────────────────────────── -->
          <section v-if="auth.hasPermission('PROJECT_VIEW')" class="records-panel vue-records-panel dashboard-panel dashboard-map-panel">
            <div class="records-toolbar">
              <div>
                <strong class="panel-title">Global Market Presence</strong>
                <span class="panel-subtitle">Countries with active projects or customer relationships</span>
              </div>
            </div>
            <WorldMapPanel :market-counts="activeMarkets" :deal-counts="dealMarkets" />
          </section>

          <!-- ── Left column ─────────────────────────────────────────── -->
          <div class="dashboard-left">

            <!-- Pipeline by Stage -->
            <section v-if="auth.hasPermission('DEAL_VIEW')" class="records-panel vue-records-panel dashboard-panel">
              <div class="records-toolbar">
                <div>
                  <strong class="panel-title">Pipeline Overview</strong>
                  <span class="panel-subtitle">Revenue opportunities by stage</span>
                </div>
                <button class="ghost-button" type="button" @click="goToModule('deals')">View All</button>
              </div>

              <div v-if="pipelineByStage.length" class="dashboard-stage-list">
                <div v-for="row in pipelineByStage" :key="row.stage" class="dashboard-stage-row">
                  <div class="dashboard-stage-left">
                    <span class="status-pill" :class="stageStatusClass(row.stage)">{{ beautify(row.stage) }}</span>
                    <span class="dashboard-stage-count">{{ row.count }} deal{{ row.count !== 1 ? 's' : '' }}</span>
                  </div>
                  <strong class="dashboard-stage-value">{{ formatMoney(row.value) }}</strong>
                </div>
              </div>
              <p v-else class="dashboard-empty">No opportunities on record.</p>

              <div v-if="auth.hasPermission('DEAL_VIEW') && wonDeals.length" class="dashboard-stage-total">
                <span>Closed Won</span>
                <strong>{{ formatMoney(wonValue) }} across {{ wonDeals.length }} deal{{ wonDeals.length !== 1 ? 's' : '' }}</strong>
              </div>
            </section>

            <!-- Open Tasks -->
            <section v-if="auth.hasPermission('TASK_VIEW')" class="records-panel vue-records-panel dashboard-panel">
              <div class="records-toolbar">
                <div>
                  <strong class="panel-title">Urgent Tasks</strong>
                  <span class="panel-subtitle">Open and in-progress, sorted by priority</span>
                </div>
                <button class="ghost-button" type="button" @click="goToModule('tasks')">View All</button>
              </div>

              <div v-if="urgentTasks.length" class="stack-list">
                <article
                  v-for="task in urgentTasks"
                  :key="task.id"
                  class="stack-card dashboard-task-card"
                >
                  <div class="stack-card-head">
                    <div class="dashboard-task-info">
                      <strong>{{ task.title }}</strong>
                      <small>{{ task.customerName || 'No customer' }}</small>
                    </div>
                    <div class="dashboard-task-meta">
                      <span class="status-pill" :class="taskPriorityClass(task)">{{ beautify(task.priority) }}</span>
                      <span
                        v-if="task.dueDate"
                        class="dashboard-due-date"
                        :class="{ 'dashboard-overdue': isOverdue(task.dueDate) }"
                      >{{ isOverdue(task.dueDate) ? 'Overdue' : formatDate(task.dueDate) }}</span>
                    </div>
                  </div>
                </article>
              </div>
              <p v-else class="dashboard-empty">No open tasks.</p>
            </section>

          </div>

          <!-- ── Right column ────────────────────────────────────────── -->
          <div class="dashboard-right dashboard-right-sticky">

            <!-- Recent Interactions -->
            <section v-if="auth.hasPermission('ACTIVITY_VIEW')" class="insight-panel vue-insight-panel dashboard-panel">
              <div class="insight-header">
                <span class="eyebrow">Activity Feed</span>
                <h2>Recent Interactions</h2>
              </div>
              <div v-if="recentActivities.length" class="detail-list">
                <div v-for="activity in recentActivities" :key="activity.id" class="detail-item dashboard-activity-item">
                  <div class="dashboard-activity-type-badge">{{ activityTypeAbbr(activity.type) }}</div>
                  <div class="dashboard-activity-body">
                    <strong>{{ activity.subject }}</strong>
                    <small>{{ activity.customerName || 'Unknown' }} &middot; {{ formatDateTime(activity.activityDate) }}</small>
                  </div>
                </div>
              </div>
              <p v-else class="dashboard-empty">No interactions logged yet.</p>
              <div class="dashboard-panel-footer">
                <button class="ghost-button" type="button" @click="goToModule('activities')">View All Interactions</button>
              </div>
            </section>

          </div>
        </section>

      </template>
    </main>
  </div>
</template>
