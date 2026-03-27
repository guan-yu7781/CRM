<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '../lib/api';
import { useAuthStore } from '../stores/auth';
import { useCrmStore } from '../stores/crm';
import EntityDialog from '../components/EntityDialog.vue';
import { formatDate, formatMoney, maintenanceYearLabel } from '../lib/formatters';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const crm = useCrmStore();

const customer = ref(null);
const records = ref([]);
const projects = ref([]);
const selectedProjectId = ref(Number(route.query.projectId) || null);
const dialogOpen = ref(false);
const editingRecord = ref(null);
const dialogError = ref('');
const fromProjects = computed(() => route.query.source === 'projects');

const fields = [
  { name: 'projectId', label: 'Project', type: 'select', required: true, source: 'projects', hidden: false },
  { name: 'maintenanceYear', label: 'Maintenance Year', type: 'select', required: true, options: [1, 2, 3, 4, 5, 6] },
  { name: 'amount', label: 'Amount', type: 'number', required: true },
  { name: 'startDate', label: 'Start Date', type: 'date', required: true },
  { name: 'endDate', label: 'End Date', type: 'date', required: true },
  { name: 'renewStatus', label: 'Renew Status', type: 'select', required: true, options: ['RENEWED', 'NOT_RENEWED'] },
  { name: 'paymentStatus', label: 'Payment Status', type: 'select', required: true, options: ['PAID', 'NOT_PAID'] }
];

const groupedProjects = computed(() => {
  const map = new Map();
  records.value
    .slice()
    .sort((a, b) => a.maintenanceYear - b.maintenanceYear)
    .forEach(record => {
      const key = record.projectId;
      if (!map.has(key)) {
        map.set(key, {
          projectId: key,
          projectName: record.projectName,
          market: record.market,
          customerName: record.customerName,
          records: []
        });
      }
      map.get(key).records.push(record);
    });

  projects.value.forEach(project => {
    if (!map.has(project.id)) {
      map.set(project.id, {
        projectId: project.id,
        projectName: project.projectName,
        market: project.market,
        customerName: project.customerName,
        records: []
      });
    }
  });
  return Array.from(map.values());
});

const visibleProjects = computed(() => {
  if (!fromProjects.value || !selectedProjectId.value) {
    return groupedProjects.value;
  }
  return groupedProjects.value.filter(item => item.projectId === selectedProjectId.value);
});

const selectedProject = computed(() => visibleProjects.value.find(item => item.projectId === selectedProjectId.value) || visibleProjects.value[0] || null);

function getAlert(project) {
  const effective = project.records.filter(item => item.startDate && new Date(item.startDate) <= new Date());
  if (!effective.length) {
    return { label: 'Pending Activation', message: 'No maintenance year is effective yet.' };
  }
  const pendingRenew = effective.find(item => item.renewStatus !== 'RENEWED');
  if (pendingRenew) {
    return { label: 'Renewal Warning', message: `Maintenance from ${pendingRenew.startDate} is effective but still not renewed.` };
  }
  const pendingPayment = effective.find(item => item.paymentStatus === 'NOT_PAID');
  if (pendingPayment) {
    return { label: 'Payment Warning', message: `Payment collection is required for ${maintenanceYearLabel(pendingPayment.maintenanceYear)}.` };
  }
  const latest = effective.reduce((acc, item) => new Date(item.endDate) > new Date(acc.endDate) ? item : acc);
  const days = Math.ceil((new Date(latest.endDate) - new Date()) / 86400000);
  if (days <= 30) {
    return { label: 'Renewal Warning', message: `Current effective coverage ends on ${latest.endDate}.` };
  }
  return { label: 'Active Coverage', message: `Coverage is active through ${latest.endDate}.` };
}

function alertClass(project) {
  const label = getAlert(project).label;
  if (label === 'Payment Warning') return 'status-orange';
  if (label === 'Renewal Warning') return 'status-yellow';
  if (label === 'Pending Activation') return 'status-active';
  return 'status-active';
}

function warningBannerClass(project) {
  const label = getAlert(project).label;
  if (label === 'Payment Warning') return 'warning-orange';
  if (label === 'Renewal Warning') return 'warning-yellow';
  return '';
}

function rowAlertClass(project) {
  const label = getAlert(project).label;
  if (label === 'Payment Warning') return 'record-orange';
  if (label === 'Renewal Warning') return 'record-yellow';
  return '';
}

function renewClass(status) {
  return status === 'RENEWED' ? 'status-active' : 'status-yellow';
}

function paymentClass(status) {
  return status === 'PAID' ? 'status-active' : 'status-orange';
}

function timelineTone(record) {
  if (record.renewStatus !== 'RENEWED') return 'timeline-renew';
  if (record.paymentStatus === 'NOT_PAID') return 'timeline-payment';
  return 'timeline-active';
}

function backTarget() {
  if (route.query.source === 'projects') {
    return { name: 'workspace', params: { module: 'projects' } };
  }
  return { name: 'customer360', params: { customerId: route.query.customerId } };
}

async function loadPage() {
  const customerId = Number(route.query.customerId);
  const projectId = Number(route.query.projectId) || null;
  const [customerResponse, recordResponse, projectResponse] = await Promise.all([
    api.get(`/api/customers/${customerId}`),
    api.get(`/api/annual-maintenance?customerId=${customerId}`),
    api.get(`/api/projects?customerId=${customerId}`)
  ]);
  customer.value = customerResponse.data;
  records.value = fromProjects.value && projectId
    ? recordResponse.data.filter(item => item.projectId === projectId)
    : recordResponse.data;
  projects.value = fromProjects.value && projectId
    ? projectResponse.data.filter(item => item.id === projectId)
    : projectResponse.data;
  if (!selectedProjectId.value && visibleProjects.value.length) {
    selectedProjectId.value = visibleProjects.value[0].projectId;
  }
}

function openCreate() {
  editingRecord.value = null;
  dialogError.value = '';
  dialogOpen.value = true;
}

function openEdit(record) {
  editingRecord.value = record;
  dialogError.value = '';
  dialogOpen.value = true;
}

async function saveRecord(payload) {
  try {
    const effectiveCustomerId = Number(payload.customerId || editingRecord.value?.customerId || customer.value?.id || route.query.customerId);
    const normalized = {
      projectId: Number(payload.projectId),
      maintenanceYear: Number(payload.maintenanceYear),
      amount: Number(payload.amount),
      startDate: payload.startDate,
      endDate: payload.endDate,
      renewStatus: payload.renewStatus,
      paymentStatus: payload.paymentStatus,
      customerId: effectiveCustomerId
    };
    if (editingRecord.value) {
      await api.put(`/api/annual-maintenance/${editingRecord.value.id}`, normalized);
    } else {
      await api.post('/api/annual-maintenance', normalized);
    }
    dialogOpen.value = false;
    await loadPage();
  } catch (exception) {
    dialogError.value = exception.response?.data?.details?.[0] || exception.message || 'Unable to save maintenance record.';
  }
}

function projectOptions(field) {
  if (field.source === 'projects') {
    return projects.value.map(item => ({ value: item.id, label: `${item.projectName} • ${item.market}` }));
  }
  return (field.options || []).map(value => ({
    value,
    label: field.name === 'maintenanceYear' ? maintenanceYearLabel(value) : String(value).replace(/_/g, ' ')
  }));
}

onMounted(async () => {
  if (!auth.isAuthenticated) {
    router.push({ name: 'login' });
    return;
  }
  if (!crm.data.customers.length) {
    await auth.fetchProfile();
  }
  await loadPage();
});
</script>

<template>
  <div class="maintenance-shell-vue maintenance-page">
    <main class="maintenance-shell">
      <header class="maintenance-header">
        <div>
          <router-link class="text-link" :to="backTarget()">Back</router-link>
          <span class="eyebrow">Annual Maintenance</span>
          <h1>{{ selectedProject ? `${selectedProject.projectName} Maintenance` : 'Project Maintenance Records' }}</h1>
          <p>{{ customer ? `Track annual maintenance records for ${customer.name}.` : 'Track project maintenance coverage, renewal, and collection.' }}</p>
        </div>
        <div class="header-actions">
          <button class="ghost-button" type="button" @click="loadPage">Refresh</button>
          <button class="primary-button" type="button" @click="openCreate">Add Record</button>
        </div>
      </header>

      <section class="summary-bar">
        <article class="summary-card">
          <span>Total Projects</span>
          <strong>{{ visibleProjects.length }}</strong>
          <small>{{ fromProjects ? 'Project-focused view' : 'Grouped by project' }}</small>
        </article>
        <article class="summary-card">
          <span>Customer</span>
          <strong>{{ customer?.name || 'Unknown' }}</strong>
          <small>Selected customer context</small>
        </article>
        <article class="summary-card">
          <span>Records</span>
          <strong>{{ records.length }}</strong>
          <small>Maintenance entries loaded</small>
        </article>
        <article v-if="selectedProject" class="summary-card">
          <span>Status</span>
          <strong>{{ getAlert(selectedProject).label }}</strong>
          <small>{{ getAlert(selectedProject).message }}</small>
        </article>
      </section>

      <section class="maintenance-layout">
        <section class="records-panel">
          <div v-if="selectedProject" class="records-insight-stack">
            <section class="maintenance-priority" :class="alertClass(selectedProject)">
              <div class="maintenance-priority-copy">
                <span class="eyebrow">Warning Status</span>
                <h3>{{ getAlert(selectedProject).label }}</h3>
                <p>{{ getAlert(selectedProject).message }}</p>
              </div>
              <div class="maintenance-priority-badge">
                <span class="status-pill" :class="alertClass(selectedProject)">{{ getAlert(selectedProject).label }}</span>
              </div>
            </section>

            <section v-if="warningBannerClass(selectedProject)" class="warning-banner" :class="warningBannerClass(selectedProject)">
              <strong>{{ getAlert(selectedProject).label }}</strong>
              <p>{{ getAlert(selectedProject).message }}</p>
            </section>

            <section class="detail-group">
              <h4>Project Summary</h4>
              <div class="detail-list">
                <div class="detail-item"><span>Market</span><strong>{{ selectedProject.market }}</strong></div>
                <div class="detail-item"><span>Customer</span><strong>{{ selectedProject.customerName }}</strong></div>
                <div class="detail-item"><span>Status</span><strong>{{ getAlert(selectedProject).label }}</strong></div>
                <div class="detail-item"><span>Message</span><strong>{{ getAlert(selectedProject).message }}</strong></div>
              </div>
            </section>
          </div>

          <div class="records-toolbar">
            <div>
              <strong class="panel-title">Maintenance Projects</strong>
              <span class="panel-subtitle">Grouped by project and managed year by year.</span>
            </div>
          </div>
          <div class="stack-list">
            <article
              v-for="project in visibleProjects"
              :key="project.projectId"
              class="stack-card"
              :class="[rowAlertClass(project), { selected: selectedProject?.projectId === project.projectId }]"
              @click="selectedProjectId = project.projectId"
            >
              <div class="stack-card-head">
                <div>
                  <h3>{{ project.projectName }}</h3>
                  <p>{{ project.customerName }} • {{ project.market }} • {{ project.records.length }} year record(s)</p>
                </div>
                <span class="status-pill" :class="alertClass(project)">{{ getAlert(project).label }}</span>
              </div>
            </article>
          </div>
        </section>

        <aside class="insight-panel">
          <div class="insight-header">
            <span class="eyebrow">Maintenance Timeline</span>
            <h2>{{ selectedProject?.projectName || 'Select a project' }}</h2>
          </div>
          <div class="insight-content">
            <template v-if="selectedProject">
              <section class="detail-group detail-group-compact">
                <h4>Maintenance Timeline</h4>
                <div class="timeline-list">
                  <article v-for="record in selectedProject.records" :key="record.id" class="year-record" :class="timelineTone(record)">
                    <div class="timeline-rail">
                      <span class="timeline-dot" />
                      <span class="timeline-line" />
                    </div>
                    <div class="timeline-card">
                      <div class="year-record-head">
                        <div>
                          <span class="timeline-label">Coverage Year</span>
                          <h3>{{ maintenanceYearLabel(record.maintenanceYear) }}</h3>
                        </div>
                        <div class="inline-actions">
                          <span class="status-pill" :class="renewClass(record.renewStatus)">{{ record.renewStatus === 'RENEWED' ? 'Renewed' : 'Not Renewed' }}</span>
                          <span class="status-pill" :class="paymentClass(record.paymentStatus)">{{ record.paymentStatus === 'PAID' ? 'Paid' : 'Not Paid' }}</span>
                        </div>
                      </div>
                      <div class="record-meta">
                        <span class="meta-pill">{{ formatMoney(record.amount) }}</span>
                        <span class="meta-pill">Start {{ record.startDate }}</span>
                        <span class="meta-pill">End {{ record.endDate }}</span>
                      </div>
                      <div class="inline-actions timeline-actions">
                        <button class="ghost-button" type="button" @click="openEdit(record)">Edit</button>
                      </div>
                    </div>
                  </article>
                </div>
              </section>
            </template>
          </div>
        </aside>
      </section>
    </main>

    <EntityDialog
      :open="dialogOpen"
      :title="`${editingRecord ? 'Edit' : 'Add'} Annual Maintenance`"
      :fields="fields"
      :model-value="editingRecord || { projectId: selectedProject?.projectId || '' }"
      :submit-label="editingRecord ? 'Update' : 'Save'"
      :options-resolver="projectOptions"
      :error="dialogError"
      @close="dialogOpen = false"
      @submit="saveRecord"
    />
  </div>
</template>
