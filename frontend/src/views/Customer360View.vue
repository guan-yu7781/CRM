<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { useCrmStore } from '../stores/crm';
import { beautify, formatMoney } from '../lib/formatters';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const crm = useCrmStore();

const customerTab = ref('overview');

const customerId = computed(() => Number(route.params.customerId));
const customer = computed(() => crm.data.customers.find(item => item.id === customerId.value));
const contacts = computed(() => crm.data.contacts.filter(item => item.customerId === customerId.value));
const projects = computed(() => crm.data.projects.filter(item => item.customerId === customerId.value));
const deals = computed(() => crm.data.deals.filter(item => item.customerId === customerId.value));
const tasks = computed(() => crm.data.tasks.filter(item => item.customerId === customerId.value));
const openDeals = computed(() => deals.value.filter(item => item.stage !== 'WON' && item.stage !== 'LOST'));
const totalPipeline = computed(() => openDeals.value.reduce((sum, item) => sum + Number(item.amount || 0), 0));
const totalProjectValue = computed(() => projects.value.reduce((sum, item) => sum + Number(item.licenseAmount || 0) + Number(item.implementationAmount || 0), 0));
const nextTask = computed(() => tasks.value
  .filter(item => item.status !== 'COMPLETED' && item.status !== 'CANCELLED')
  .sort((a, b) => new Date(a.dueDate || '2999-12-31') - new Date(b.dueDate || '2999-12-31'))[0] || null);
const relationshipPulse = computed(() => {
  if (!customer.value) return 'Monitoring';
  if (openDeals.value.length >= 2 || projects.value.length >= 2) return 'Growth Priority';
  if (customer.value.riskLevel === 'HIGH') return 'Risk Watch';
  return 'Stable Coverage';
});

function customerScore(item) {
  let score = 60;
  if (item.segment === 'CORPORATE') score += 10;
  if (item.status === 'ACTIVE') score += 10;
  if (item.riskLevel === 'LOW') score += 10;
  if (openDeals.value.length) score += 5;
  return `${Math.min(score, 98)}/100`;
}

onMounted(async () => {
  if (!auth.isAuthenticated) {
    router.push({ name: 'login' });
    return;
  }
  if (!crm.data.customers.length) {
    await auth.fetchProfile();
    await crm.loadWorkspace(auth);
  }
});
</script>

<template>
  <div class="customer360-shell customer-360-page">
    <main class="customer-360-page-shell">
      <header class="customer-360-page-header vue-page-header">
        <div>
          <span class="eyebrow">Customer 360</span>
          <h1>{{ customer ? `${customer.name} Customer 360` : 'Customer 360 Workspace' }}</h1>
          <p>{{ customer ? `${customer.cifNumber || 'CIF pending'} • ${beautify(customer.customerType)} • Strategic Relationship View` : 'Detailed relationship view for the selected customer.' }}</p>
        </div>
        <div class="header-actions">
          <router-link class="ghost-button link-button" :to="{ name: 'workspace', params: { module: 'customers' } }">Back to Customer Management</router-link>
          <router-link v-if="customer" class="primary-button link-button" :to="{ name: 'maintenance', query: { customerId: customer.id, source: 'customer360' } }">Annual Maintenance</router-link>
        </div>
      </header>

      <section v-if="customer" class="customer-360-page-content vue-customer360-surface">
        <div class="customer-360-shell">
          <aside class="customer-360-core">
            <div class="customer-core-hero">
              <div class="customer-core-logo">{{ customer.name.slice(0, 1) }}</div>
              <div>
                <span class="eyebrow">{{ beautify(customer.customerType) }}</span>
                <h3>{{ customer.name }}</h3>
                <p>{{ customer.cifNumber || 'CIF pending' }} • {{ beautify(customer.status) }} • {{ beautify(customer.segment) }}</p>
              </div>
            </div>
            <div class="customer-core-grid">
              <div class="detail-item"><span>Tier</span><strong>{{ beautify(customer.segment) }}</strong></div>
              <div class="detail-item"><span>Customer Score</span><strong>{{ customerScore(customer) }}</strong></div>
              <div class="detail-item"><span>Lifecycle</span><strong>{{ beautify(customer.status) }}</strong></div>
              <div class="detail-item"><span>Open Pipeline</span><strong>{{ formatMoney(totalPipeline) }}</strong></div>
            </div>
            <section class="detail-group">
              <h4>Executive Summary</h4>
              <p class="detail-narrative">
                {{ customer.name }} is currently managed as a {{ beautify(customer.segment) }} relationship with {{ contacts.length }} known contact(s),
                {{ openDeals.length }} open opportunity(ies), and {{ projects.length }} active project record(s).
              </p>
            </section>
            <section class="detail-group">
              <h4>Commercial Snapshot</h4>
              <div class="customer-snapshot-grid">
                <article class="customer-snapshot-card">
                  <span>Relationship Pulse</span>
                  <strong>{{ relationshipPulse }}</strong>
                  <small>Commercial signal for current account plan</small>
                </article>
                <article class="customer-snapshot-card">
                  <span>Project Book</span>
                  <strong>{{ formatMoney(totalProjectValue) }}</strong>
                  <small>License and implementation total</small>
                </article>
              </div>
            </section>
          </aside>

          <section class="customer-360-main">
            <section class="customer-360-metrics">
              <article class="customer-360-metric-card">
                <span>Customer Score</span>
                <strong>{{ customerScore(customer) }}</strong>
                <small>Relationship strength indicator</small>
              </article>
              <article class="customer-360-metric-card">
                <span>Open Pipeline</span>
                <strong>{{ formatMoney(totalPipeline) }}</strong>
                <small>Commercial value in motion</small>
              </article>
              <article class="customer-360-metric-card">
                <span>Projects</span>
                <strong>{{ projects.length }}</strong>
                <small>Execution footprint</small>
              </article>
            </section>

            <div class="customer-tab-bar">
              <button class="customer-tab" :class="{ active: customerTab === 'overview' }" @click="customerTab = 'overview'">Overview</button>
              <button class="customer-tab" :class="{ active: customerTab === 'contacts' }" @click="customerTab = 'contacts'">Contacts</button>
              <button class="customer-tab" :class="{ active: customerTab === 'opportunities' }" @click="customerTab = 'opportunities'">Opportunities</button>
              <button class="customer-tab" :class="{ active: customerTab === 'projects' }" @click="customerTab = 'projects'">Projects</button>
              <button class="customer-tab" :class="{ active: customerTab === 'products' }" @click="customerTab = 'products'">Products</button>
            </div>

            <section class="customer-tab-panel">
              <template v-if="customerTab === 'overview'">
                <section class="customer-tab-section">
                  <div class="customer-overview-grid">
                    <div class="detail-item"><span>Contacts</span><strong>{{ contacts.length }}</strong></div>
                    <div class="detail-item"><span>Open Deals</span><strong>{{ openDeals.length }}</strong></div>
                    <div class="detail-item"><span>Projects</span><strong>{{ projects.length }}</strong></div>
                    <div class="detail-item"><span>Service Tasks</span><strong>{{ tasks.length }}</strong></div>
                  </div>
                </section>
              </template>

              <template v-else-if="customerTab === 'contacts'">
                <div class="customer-collection">
                  <article v-for="item in contacts" :key="item.id" class="customer-collection-item">
                    <strong>{{ item.firstName }} {{ item.lastName }}</strong>
                    <span>{{ item.jobTitle || item.email }}</span>
                  </article>
                </div>
              </template>

              <template v-else-if="customerTab === 'opportunities'">
                <div class="customer-collection">
                  <article v-for="item in openDeals" :key="item.id" class="customer-collection-item">
                    <strong>{{ item.title }}</strong>
                    <span>{{ formatMoney(item.amount) }} • {{ beautify(item.stage) }}</span>
                  </article>
                </div>
              </template>

              <template v-else-if="customerTab === 'projects'">
                <div class="customer-collection">
                  <article v-for="item in projects" :key="item.id" class="customer-collection-item">
                    <strong>{{ item.projectName }}</strong>
                    <span>{{ item.market }} • {{ beautify(item.status) }}</span>
                  </article>
                </div>
              </template>

              <template v-else>
                <div class="customer-product-grid">
                  <article class="customer-product-card">
                    <span>Open Pipeline</span>
                    <strong>{{ formatMoney(totalPipeline) }}</strong>
                    <p>Open commercial opportunities</p>
                  </article>
                  <article class="customer-product-card">
                    <span>Project Book</span>
                    <strong>{{ formatMoney(totalProjectValue) }}</strong>
                    <p>Combined license and implementation value</p>
                  </article>
                </div>
              </template>
            </section>
          </section>

          <aside class="customer-360-actions">
            <section class="detail-group">
              <h4>Next Action</h4>
              <div class="detail-list detail-list-emphasis">
                <div class="detail-item detail-item-accent">
                  <span>Upcoming Task</span>
                  <strong>{{ nextTask ? `${nextTask.title} • ${nextTask.dueDate || 'No due date'}` : 'No open next action' }}</strong>
                </div>
              </div>
            </section>
            <section class="detail-group">
              <h4>Key Contacts</h4>
              <div class="detail-list">
                <div v-for="item in contacts.slice(0, 3)" :key="item.id" class="detail-item">
                  <span>{{ item.firstName }} {{ item.lastName }}</span>
                  <strong>{{ item.jobTitle || item.email }}</strong>
                </div>
              </div>
            </section>
            <section class="detail-group">
              <h4>Open Deals</h4>
              <div class="detail-list">
                <div v-for="item in openDeals.slice(0, 3)" :key="item.id" class="detail-item">
                  <span>{{ item.title }}</span>
                  <strong>{{ formatMoney(item.amount) }} • {{ beautify(item.stage) }}</strong>
                </div>
              </div>
            </section>
            <section class="detail-group">
              <h4>Alerts</h4>
              <div class="detail-list">
                <div class="detail-item" :class="customer.riskLevel === 'HIGH' ? 'detail-item-accent' : ''">
                  <span>Risk Posture</span>
                  <strong>{{ beautify(customer.riskLevel) }}</strong>
                </div>
                <div class="detail-item" :class="!openDeals.length ? 'detail-item-accent' : ''">
                  <span>Coverage Signal</span>
                  <strong>{{ openDeals.length ? 'Pipeline exists for follow-up' : 'No active opportunity in pipeline' }}</strong>
                </div>
              </div>
            </section>
          </aside>
        </div>
      </section>
    </main>
  </div>
</template>
