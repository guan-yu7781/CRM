<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import BrandSidebar from '../components/BrandSidebar.vue';
import EntityDialog from '../components/EntityDialog.vue';
import { useAuthStore } from '../stores/auth';
import { useCrmStore } from '../stores/crm';
import { beautify, formatDate, formatDateTime, formatMoney } from '../lib/formatters';
import { moduleMenu, modulePermissions } from '../lib/permissions';
import { MARKET_OPTIONS } from '../lib/markets';

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const crm = useCrmStore();

const sidebarCollapsed = ref(false);
const search = ref('');
const selectedId = ref(null);
const dialogOpen = ref(false);
const dialogError = ref('');
const editingItem = ref(null);

const configs = {
  customers: {
    label: 'Customer Management',
    title: 'Banking Customer Management',
    description: 'Manage customer master data, risk posture, and launch the dedicated 360 workspace.',
    singular: 'Customer',
    fields: [
      { name: 'name', label: 'Customer Name', type: 'text', required: true },
      { name: 'customerType', label: 'Customer Type', type: 'select', required: true, options: ['COMMERCIAL_BANK', 'PAYMENT_INSTITUTION', 'CENTRAL_BANK', 'MICROFINANCE_BANK', 'SACCO'] },
      { name: 'cifNumber', label: 'CIF Number', type: 'text', required: true },
      { name: 'segment', label: 'Segment', type: 'select', required: true, options: ['RETAIL', 'SME', 'CORPORATE', 'WEALTH'] },
      { name: 'status', label: 'Lifecycle Status', type: 'select', required: true, options: ['LEAD', 'ACTIVE', 'INACTIVE'] },
      { name: 'riskLevel', label: 'Risk Level', type: 'select', required: true, options: ['LOW', 'MEDIUM', 'HIGH'] },
      { name: 'notes', label: 'Relationship Notes', type: 'textarea', full: true }
    ]
  },
  contacts: {
    label: 'Contacts',
    title: 'Contact Directory',
    description: 'Maintain authorized contacts, stakeholders, and decision makers by customer.',
    singular: 'Contact',
    fields: [
      { name: 'firstName', label: 'First Name', type: 'text', required: true },
      { name: 'lastName', label: 'Last Name', type: 'text', required: true },
      { name: 'email', label: 'Email', type: 'email', required: true },
      { name: 'phone', label: 'Phone', type: 'text' },
      { name: 'jobTitle', label: 'Role / Title', type: 'text' },
      { name: 'customerId', label: 'Customer', type: 'select', source: 'customers', required: true },
      { name: 'notes', label: 'Notes', type: 'textarea', full: true }
    ]
  },
  projects: {
    label: 'Projects',
    title: 'Customer Project Management',
    description: 'Manage signed and unsigned project records, commercial values, and maintenance linkage.',
    singular: 'Project',
    fields: [
      { name: 'projectName', label: 'Project Name', type: 'text', required: true },
      { name: 'market', label: 'Market', type: 'market-search', required: true },
      { name: 'licenseAmount', label: 'License Amount', type: 'number', required: true },
      { name: 'implementationAmount', label: 'Implementation Amount', type: 'number', required: true },
      { name: 'taxRate', label: 'Tax Rate', type: 'number', required: true },
      { name: 'status', label: 'Contract Status', type: 'select', required: true, options: ['SIGNED_CONTRACT', 'UNSIGNED_CONTRACT'] },
      { name: 'accountManagerId', label: 'Account Manager', type: 'select', source: 'accountManagers' },
      { name: 'customerId', label: 'Customer', type: 'select', source: 'customers', required: true }
    ]
  },
  deals: {
    label: 'Opportunities',
    title: 'Revenue Opportunities',
    description: 'Track pipeline progression and convert won opportunities into projects.',
    singular: 'Opportunity',
    fields: [
      { name: 'title', label: 'Opportunity Name', type: 'text', required: true },
      { name: 'amount', label: 'Expected Value', type: 'number', required: true },
      { name: 'stage', label: 'Stage', type: 'select', required: true, options: ['NEW', 'QUALIFIED', 'PROPOSAL_SENT', 'NEGOTIATION', 'WON', 'LOST'] },
      { name: 'opportunityType', label: 'Opportunity Type', type: 'select', required: true, options: ['EXPANSION', 'ACQUISITION'] },
      { name: 'expectedCloseDate', label: 'Expected Close', type: 'date' },
      { name: 'customerId', label: 'Customer', type: 'select', source: 'customers', required: true },
      { name: 'notes', label: 'Notes', type: 'textarea', full: true }
    ]
  },
  tasks: {
    label: 'Service Tasks',
    title: 'Service and Fulfilment Tasks',
    description: 'Manage servicing actions, operational follow-ups, and delivery commitments.',
    singular: 'Task',
    fields: [
      { name: 'title', label: 'Task Title', type: 'text', required: true },
      { name: 'status', label: 'Status', type: 'select', required: true, options: ['OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'] },
      { name: 'priority', label: 'Priority', type: 'select', required: true, options: ['LOW', 'MEDIUM', 'HIGH'] },
      { name: 'dueDate', label: 'Due Date', type: 'date' },
      { name: 'customerId', label: 'Customer', type: 'select', source: 'customers', required: true },
      { name: 'dealId', label: 'Opportunity', type: 'select', source: 'deals' },
      { name: 'description', label: 'Description', type: 'textarea', full: true }
    ]
  },
  activities: {
    label: 'Interactions',
    title: 'Customer Interaction Journal',
    description: 'Capture calls, meetings, notes, and follow-up interactions.',
    singular: 'Interaction',
    fields: [
      { name: 'type', label: 'Type', type: 'select', required: true, options: ['CALL', 'EMAIL', 'MEETING', 'NOTE', 'FOLLOW_UP'] },
      { name: 'subject', label: 'Subject', type: 'text', required: true },
      { name: 'activityDate', label: 'Interaction Date', type: 'datetime-local', required: true },
      { name: 'customerId', label: 'Customer', type: 'select', source: 'customers', required: true },
      { name: 'contactId', label: 'Contact', type: 'select', source: 'contacts' },
      { name: 'dealId', label: 'Opportunity', type: 'select', source: 'deals' },
      { name: 'details', label: 'Details', type: 'textarea', full: true }
    ]
  },
  accessControl: {
    label: 'Access Control',
    title: 'Role and Access Control',
    description: 'Manage CRM users, review business roles, and assign permission scope.',
    singular: 'User',
    fields: [
      { name: 'fullName', label: 'Full Name', type: 'text', required: true },
      { name: 'username', label: 'Username', type: 'text', required: true },
      { name: 'email', label: 'Email', type: 'email' },
      { name: 'password', label: 'Password', type: 'password' },
      { name: 'role', label: 'Business Role', type: 'select', required: true, source: 'roles' }
    ]
  }
};

const currentModule = computed(() => {
  const key = route.params.module || 'customers';
  return configs[key] ? key : 'customers';
});

const currentConfig = computed(() => configs[currentModule.value]);

const items = computed(() => crm.data[currentModule.value] || []);

// Deals: opportunity-type filter tab  ('ALL' | 'EXPANSION' | 'ACQUISITION')
const dealTypeFilter = ref('ALL');
watch(currentModule, () => { dealTypeFilter.value = 'ALL'; });

const filteredItems = computed(() => {
  let list = items.value;
  // Apply opportunity-type tab filter for deals
  if (currentModule.value === 'deals' && dealTypeFilter.value !== 'ALL') {
    list = list.filter(item => item.opportunityType === dealTypeFilter.value);
  }
  const keyword = search.value.trim().toLowerCase();
  if (!keyword) return list;
  return list.filter(item => JSON.stringify(item).toLowerCase().includes(keyword));
});

const selectedItem = computed(() => filteredItems.value.find(item => item.id === selectedId.value) || filteredItems.value[0] || null);

const summaryCards = computed(() => {
  const module = currentModule.value;
  if (module === 'customers') {
    return [
      { label: 'Customers', value: String(crm.data.customers.length), note: 'Total master records' },
      { label: 'Corporate', value: String(crm.data.customers.filter(item => item.segment === 'CORPORATE').length), note: 'Corporate segment' },
      { label: 'High Risk', value: String(crm.data.customers.filter(item => item.riskLevel === 'HIGH').length), note: 'Priority monitoring' },
      { label: 'Active', value: String(crm.data.customers.filter(item => item.status === 'ACTIVE').length), note: 'Live relationships' }
    ];
  }
  if (module === 'projects') {
    return [
      { label: 'Projects', value: String(crm.data.projects.length), note: 'Managed project book' },
      { label: 'Signed', value: String(crm.data.projects.filter(item => item.status === 'SIGNED_CONTRACT').length), note: 'Contracts signed' },
      { label: 'Unsigned', value: String(crm.data.projects.filter(item => item.status === 'UNSIGNED_CONTRACT').length), note: 'Awaiting signature' },
      { label: 'License Value', value: formatMoney(crm.data.projects.reduce((sum, item) => sum + Number(item.licenseAmount || 0), 0)), note: 'Combined license amount' }
    ];
  }
  if (module === 'deals') {
    const openDeals = crm.data.deals.filter(item => item.stage !== 'WON' && item.stage !== 'LOST');
    const expansion = openDeals.filter(item => item.opportunityType === 'EXPANSION');
    const acquisition = openDeals.filter(item => item.opportunityType === 'ACQUISITION');
    return [
      { label: 'Pipeline', value: formatMoney(openDeals.reduce((sum, item) => sum + Number(item.amount || 0), 0)), note: 'Open pipeline value' },
      { label: 'Expansion', value: formatMoney(expansion.reduce((sum, item) => sum + Number(item.amount || 0), 0)), note: `${expansion.length} existing-customer deals` },
      { label: 'Acquisition', value: formatMoney(acquisition.reduce((sum, item) => sum + Number(item.amount || 0), 0)), note: `${acquisition.length} new-customer deals` },
      { label: 'Won', value: String(crm.data.deals.filter(item => item.stage === 'WON').length), note: 'Closed won' }
    ];
  }
  if (module === 'accessControl') {
    return [
      { label: 'Users', value: String(crm.data.accessControl.length), note: 'Configured CRM users' },
      { label: 'Roles', value: String(crm.accessRoles.length), note: 'Business roles' },
      { label: 'Super Admins', value: String(crm.data.accessControl.filter(item => item.role === 'SUPER_ADMIN').length), note: 'Full control users' },
      { label: 'Finance Users', value: String(crm.data.accessControl.filter(item => item.role === 'FINANCE_OFFICER').length), note: 'Collection and renew users' }
    ];
  }
  return [
    { label: 'Records', value: String(items.value.length), note: 'Current module volume' },
    { label: 'Platform', value: 'MySQL', note: 'Primary operational datastore' },
    { label: 'Role', value: auth.roleLabel || 'User', note: 'Signed-in business role' },
    { label: 'Scope', value: auth.dataScope || 'N/A', note: 'Effective data scope' }
  ];
});

const modalDraft = reactive({});

function canAccess(moduleKey) {
  return auth.hasPermission(modulePermissions[moduleKey]?.view);
}

function canCreate() {
  return auth.hasPermission(modulePermissions[currentModule.value]?.create);
}

function canEdit() {
  return auth.hasPermission(modulePermissions[currentModule.value]?.edit);
}

function canDelete() {
  return auth.hasPermission(modulePermissions[currentModule.value]?.delete);
}

function tableStatusLabel(item) {
  if (currentModule.value === 'contacts') return item.jobTitle || 'Primary Contact';
  if (currentModule.value === 'projects') return beautify(item.status);
  if (currentModule.value === 'deals') return beautify(item.stage);
  if (currentModule.value === 'tasks') return beautify(item.status);
  if (currentModule.value === 'activities') return beautify(item.type);
  return item.roleLabel;
}

function tableStatusClass(item) {
  if (currentModule.value === 'projects') {
    return item.status === 'SIGNED_CONTRACT' ? 'status-active' : 'status-yellow';
  }
  if (currentModule.value === 'deals') {
    if (item.stage === 'WON') return 'status-active';
    if (item.stage === 'LOST') return 'status-orange';
    return 'status-yellow';
  }
  if (currentModule.value === 'tasks') {
    if (item.status === 'COMPLETED') return 'status-active';
    if (item.status === 'CANCELLED') return 'status-orange';
    return 'status-yellow';
  }
  if (currentModule.value === 'activities') return 'status-active';
  if (currentModule.value === 'accessControl') {
    return item.role === 'SUPER_ADMIN' || item.role === 'CRM_ADMIN' ? 'status-active' : 'status-yellow';
  }
  return 'status-active';
}

function tableContextLabel(item) {
  if (currentModule.value === 'contacts') return item.customerName;
  if (currentModule.value === 'projects') return item.customerName;
  if (currentModule.value === 'deals') return item.customerName;
  if (currentModule.value === 'tasks') return item.customerName;
  return item.customerName || item.createdBy;
}

function tableMetaLabel(item) {
  if (currentModule.value === 'contacts') return item.phone || 'No direct line';
  if (currentModule.value === 'projects') return item.market || 'Market pending';
  if (currentModule.value === 'deals') return item.expectedCloseDate ? `Expected ${formatDate(item.expectedCloseDate)}` : 'Close date pending';
  if (currentModule.value === 'tasks') return item.dueDate ? `Due ${formatDate(item.dueDate)}` : 'No due date';
  if (currentModule.value === 'activities') return formatDateTime(item.activityDate);
  return `${item.dataScope || auth.dataScope || 'Scoped'} access`;
}

function accessRoleSummary(item) {
  const role = crm.accessRoles.find(entry => entry.role === item.role);
  return {
    label: role?.label || item.roleLabel || beautify(item.role),
    description: role?.description || 'Configured CRM business role.',
    permissions: role?.permissions?.slice(0, 4) || []
  };
}

function syncAllowedModule() {
  if (!canAccess(currentModule.value)) {
    const first = moduleMenu.find(item => canAccess(item.key));
    if (first) {
      router.replace({ name: 'workspace', params: { module: first.key } });
    } else {
      router.replace({ name: 'error-403' });
    }
  }
}

function optionLabel(field, value) {
  if (!value) return 'Not set';
  if (field.name === 'customerId') return crm.data.customers.find(item => item.id === Number(value))?.name || value;
  if (field.name === 'dealId') return crm.data.deals.find(item => item.id === Number(value))?.title || value;
  if (field.name === 'contactId') return crm.data.contacts.find(item => item.id === Number(value))?.firstName || value;
  if (field.name === 'accountManagerId') return crm.accountManagers.find(item => item.id === Number(value))?.fullName || value;
  if (field.name === 'role') return crm.accessRoles.find(item => item.role === value)?.label || value;
  return beautify(value);
}

function resolveFieldOptions(field) {
  if (field.type !== 'select') return [];
  if (field.source === 'customers') {
    return crm.data.customers.map(item => ({ value: item.id, label: `${item.name} • ${item.cifNumber}` }));
  }
  if (field.source === 'deals') {
    return crm.data.deals.map(item => ({ value: item.id, label: `${item.title} • ${item.customerName}` }));
  }
  if (field.source === 'contacts') {
    return crm.data.contacts.map(item => ({ value: item.id, label: `${item.firstName} ${item.lastName} • ${item.customerName}` }));
  }
  if (field.source === 'accountManagers') {
    return crm.accountManagers.map(item => ({ value: item.id, label: `${item.fullName} · ${item.roleLabel}` }));
  }
  if (field.source === 'roles') {
    return crm.accessRoles.map(item => ({ value: item.role, label: item.label }));
  }
  if (Array.isArray(field.options)) {
    return field.options.map(value => ({ value, label: beautify(value) }));
  }
  return [];
}

function normalizePayload(fields, payload, editing) {
  const output = {};
  fields.forEach(field => {
    const raw = payload[field.name];
    if (raw === '' || raw == null) {
      if (field.name === 'password' && editing) return;
      return;
    }
    if (['customerId', 'dealId', 'contactId', 'accountManagerId'].includes(field.name)) {
      output[field.name] = Number(raw);
    } else if (['licenseAmount', 'implementationAmount', 'taxRate', 'amount'].includes(field.name)) {
      output[field.name] = Number(raw);
    } else {
      output[field.name] = raw;
    }
  });
  return output;
}

function openCreate() {
  editingItem.value = null;
  dialogError.value = '';
  dialogOpen.value = true;
}

function openEdit(item) {
  editingItem.value = item;
  dialogError.value = '';
  dialogOpen.value = true;
}

async function saveModal(payload) {
  try {
    const response = await crm.saveRecord(
      currentModule.value,
      normalizePayload(currentConfig.value.fields, payload, !!editingItem.value),
      editingItem.value?.id || null
    );
    dialogOpen.value = false;
    dialogError.value = '';
    await crm.loadWorkspace(auth);
    selectedId.value = response.id || selectedId.value;
  } catch (exception) {
    dialogError.value = exception.response?.data?.details?.[0] || exception.message || 'Unable to save record.';
  }
}

async function remove(item) {
  if (!window.confirm(`Delete this ${currentConfig.value.singular.toLowerCase()}?`)) return;
  try {
    await crm.deleteRecord(currentModule.value, item.id);
    await crm.loadWorkspace(auth);
  } catch (exception) {
    window.alert(exception.response?.data?.details?.[0] || exception.message || 'Unable to delete record.');
  }
}

async function refresh() {
  await crm.loadWorkspace(auth);
}

function signOut() {
  auth.logout();
  router.push({ name: 'login' });
}

function openCustomer360(customerId) {
  router.push({ name: 'customer360', params: { customerId } });
}

function openMaintenance(item) {
  router.push({ name: 'maintenance', query: { customerId: item.customerId, projectId: item.id, source: 'projects' } });
}

function projectInsight(item) {
  return [
    ['Contract Status', beautify(item.status)],
    ['Market', item.market],
    ['Account Manager', item.accountManagerName || 'Not assigned'],
    ...(auth.hasPermission('PROJECT_VIEW_FINANCIALS')
      ? [
          ['License Amount', formatMoney(item.licenseAmount)],
          ['Implementation Amount', formatMoney(item.implementationAmount)],
          ['Tax Rate', `${Number(item.taxRate || 0)}%`]
        ]
      : [])
  ];
}

function userInsight(item) {
  return [
    ['Username', item.username],
    ['Email', item.email || 'Not configured'],
    ['Business Role', item.roleLabel],
    ['Effective Scope', item.dataScope],
    ['Created', formatDateTime(item.createdAt)]
  ];
}

onMounted(async () => {
  try {
    if (!auth.isAuthenticated) {
      router.push({ name: 'login' });
      return;
    }
    await auth.fetchProfile();
    syncAllowedModule();
    await crm.loadWorkspace(auth);
  } catch (exception) {
    auth.logout();
    router.push({ name: 'login' });
  }
});

watch(() => route.params.module, syncAllowedModule, { immediate: true });
watch(filteredItems, (next) => {
  if (!next.length) {
    selectedId.value = null;
  } else if (!next.some(item => item.id === selectedId.value)) {
    selectedId.value = next[0].id;
  }
}, { immediate: true });
</script>

<template>
  <div class="app-shell vue-app-shell" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <BrandSidebar
      :collapsed="sidebarCollapsed"
      :current-module="currentModule"
      :can-access="canAccess"
      :username="auth.username"
      :role-label="auth.roleLabel"
      @toggle="sidebarCollapsed = !sidebarCollapsed"
      @logout="signOut"
    />

    <main class="content vue-page">
      <header class="page-header vue-page-header">
        <div class="workspace-header-copy">
          <span class="eyebrow">{{ currentConfig.label }}</span>
          <h1>{{ currentConfig.title }}</h1>
          <p>{{ currentConfig.description }}</p>
        </div>
        <div class="workspace-header-meta">
          <div class="workspace-meta-card">
            <span>Signed In As</span>
            <strong>{{ auth.roleLabel || 'User' }}</strong>
            <small>{{ auth.username }}</small>
          </div>
          <div class="workspace-meta-card">
            <span>Data Scope</span>
            <strong>{{ auth.dataScope || 'Scoped' }}</strong>
            <small>Permission-aware workspace</small>
          </div>
        </div>
        <div class="header-actions">
          <button class="ghost-button" type="button" @click="refresh">Refresh</button>
          <button v-if="canCreate()" class="primary-button" type="button" @click="openCreate">
            Add {{ currentConfig.singular }}
          </button>
        </div>
      </header>

      <section class="summary-bar vue-summary-bar">
        <article v-for="card in summaryCards" :key="card.label" class="summary-card">
          <span>{{ card.label }}</span>
          <strong>{{ card.value }}</strong>
          <small>{{ card.note }}</small>
        </article>
      </section>

      <section class="shell-content" :class="currentModule === 'contacts' || currentModule === 'projects' || currentModule === 'accessControl' ? 'workspace-single' : 'workspace-two-column'">
        <section class="records-panel vue-records-panel">
          <div class="records-toolbar">
            <div>
              <strong class="panel-title">{{ currentModule === 'customers' ? 'Customers' : 'Records' }}</strong>
              <span class="panel-subtitle">{{ filteredItems.length }} items in the current module</span>
            </div>
            <div class="toolbar-inline">
              <input v-model="search" class="search-input" type="search" placeholder="Search records" />
              <span class="toolbar-count">{{ filteredItems.length }} visible</span>
            </div>
          </div>

          <div v-if="currentModule === 'customers'" class="stack-list">
            <article
              v-for="item in filteredItems"
              :key="item.id"
              class="stack-card"
              :class="{ selected: selectedItem?.id === item.id }"
              @click="selectedId = item.id"
            >
              <div class="stack-card-head">
                <div>
                  <h3>{{ item.name }}</h3>
                  <p>{{ item.cifNumber }} • {{ beautify(item.segment) }} • {{ beautify(item.customerType) }}</p>
                </div>
                <div class="inline-actions">
                  <button class="ghost-button" type="button" @click.stop="openCustomer360(item.id)">Open 360</button>
                  <button v-if="canEdit()" class="ghost-button" type="button" @click.stop="openEdit(item)">Edit</button>
                  <button v-if="canDelete()" class="danger-button" type="button" @click.stop="remove(item)">Delete</button>
                </div>
              </div>
            </article>
          </div>

          <div v-else class="module-table-wrap" :class="`module-table-wrap-${currentModule}`">

            <!-- Deals: opportunity-type filter tabs -->
            <div v-if="currentModule === 'deals'" class="deal-type-tabs">
              <button
                v-for="tab in [
                  { key: 'ALL', label: 'All' },
                  { key: 'EXPANSION', label: 'Expansion — Existing' },
                  { key: 'ACQUISITION', label: 'Acquisition — New' }
                ]"
                :key="tab.key"
                class="deal-type-tab"
                :class="{ active: dealTypeFilter === tab.key }"
                type="button"
                @click="dealTypeFilter = tab.key"
              >{{ tab.label }}</button>
            </div>

            <table class="module-table" :class="`module-table-${currentModule}`">
              <thead>
                <tr>
                  <th v-if="currentModule === 'contacts'">Contact</th>
                  <th v-else-if="currentModule === 'projects'">Project</th>
                  <th v-else-if="currentModule === 'deals'">Opportunity</th>
                  <th v-else-if="currentModule === 'tasks'">Task</th>
                  <th v-else-if="currentModule === 'activities'">Interaction</th>
                  <th v-else-if="currentModule === 'accessControl'">User</th>
                  <th>Status</th>
                  <th v-if="currentModule !== 'accessControl'">Customer / Scope</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in filteredItems" :key="item.id" :class="{ selected: selectedItem?.id === item.id }" @click="selectedId = item.id">
                  <td v-if="currentModule === 'contacts'">
                    <div class="table-primary">
                      <strong>{{ item.firstName }} {{ item.lastName }}</strong>
                      <small>{{ item.email }}</small>
                    </div>
                  </td>
                  <td v-else-if="currentModule === 'projects'">
                    <div class="table-primary">
                      <strong>{{ item.projectName }}</strong>
                      <small>{{ item.market }}</small>
                    </div>
                  </td>
                  <td v-else-if="currentModule === 'deals'">
                    <div class="table-primary">
                      <strong>{{ item.title }}</strong>
                      <small>
                        {{ formatMoney(item.amount) }}
                        <span
                          class="opp-type-badge"
                          :class="item.opportunityType === 'EXPANSION' ? 'opp-type-expansion' : 'opp-type-acquisition'"
                        >{{ item.opportunityType === 'EXPANSION' ? 'Expansion' : 'Acquisition' }}</span>
                      </small>
                    </div>
                  </td>
                  <td v-else-if="currentModule === 'tasks'">
                    <div class="table-primary">
                      <strong>{{ item.title }}</strong>
                      <small>{{ item.dueDate || 'No due date' }}</small>
                    </div>
                  </td>
                  <td v-else-if="currentModule === 'activities'">
                    <div class="table-primary">
                      <strong>{{ item.subject }}</strong>
                      <small>{{ formatDateTime(item.activityDate) }}</small>
                    </div>
                  </td>
                  <td v-else>
                    <div class="table-primary">
                      <strong>{{ item.fullName }}</strong>
                      <small>{{ item.email || item.username }}</small>
                    </div>
                  </td>
                  <td>
                    <span class="status-pill" :class="tableStatusClass(item)">{{ tableStatusLabel(item) }}</span>
                  </td>
                  <td v-if="currentModule !== 'accessControl'">
                    <div class="table-context">
                      <strong>{{ tableContextLabel(item) }}</strong>
                      <small>{{ tableMetaLabel(item) }}</small>
                    </div>
                  </td>
                  <td>
                    <div class="inline-actions">
                      <button v-if="currentModule === 'projects'" class="ghost-button" type="button" @click.stop="openMaintenance(item)">Maintenance</button>
                      <button v-if="currentModule === 'deals' && item.stage === 'WON' && auth.hasPermission('DEAL_CONVERT_TO_PROJECT')" class="ghost-button" type="button">
                        Convert
                      </button>
                      <button v-if="canEdit()" class="ghost-button" type="button" @click.stop="openEdit(item)">Edit</button>
                      <button v-if="canDelete() && currentModule !== 'accessControl'" class="danger-button" type="button" @click.stop="remove(item)">Delete</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <aside v-if="currentModule !== 'contacts' && currentModule !== 'projects' && currentModule !== 'accessControl'" class="insight-panel vue-insight-panel">
          <div class="insight-header">
            <span class="eyebrow">Selected Record</span>
            <h2>{{ currentConfig.singular }} Insight</h2>
          </div>
          <div v-if="selectedItem" class="insight-content">
            <div class="insight-hero">
              <span class="eyebrow">{{ currentConfig.label }}</span>
              <h3>{{ selectedItem.name || selectedItem.title || selectedItem.subject || selectedItem.firstName + ' ' + selectedItem.lastName }}</h3>
              <p>
                {{ selectedItem.cifNumber || selectedItem.customerName || selectedItem.email || selectedItem.createdBy || 'CRM record' }}
              </p>
            </div>
            <div class="insight-metrics">
              <article class="insight-metric-card">
                <span>Workspace Role</span>
                <strong>{{ auth.roleLabel || 'User' }}</strong>
              </article>
              <article class="insight-metric-card">
                <span>Current Scope</span>
                <strong>{{ auth.dataScope || 'Scoped' }}</strong>
              </article>
            </div>
            <div class="detail-group">
              <h4>Record Detail</h4>
              <div class="detail-list">
                <div v-for="field in currentConfig.fields" :key="field.name" class="detail-item">
                  <span>{{ field.label }}</span>
                  <strong>{{ optionLabel(field, selectedItem[field.name]) }}</strong>
                </div>
              </div>
            </div>
          </div>
        </aside>

        <aside v-else-if="currentModule === 'accessControl'" class="insight-panel vue-insight-panel">
          <div class="insight-header">
            <span class="eyebrow">Role Catalog</span>
            <h2>Access Design</h2>
          </div>
          <div class="insight-content">
            <div v-if="selectedItem" class="detail-group">
              <h4>User Summary</h4>
              <div class="detail-list">
                <div v-for="[label, value] in userInsight(selectedItem)" :key="label" class="detail-item">
                  <span>{{ label }}</span>
                  <strong>{{ value }}</strong>
                </div>
              </div>
              <div class="access-role-hero">
                <div>
                  <span class="eyebrow">Assigned Role</span>
                  <h3>{{ accessRoleSummary(selectedItem).label }}</h3>
                  <p>{{ accessRoleSummary(selectedItem).description }}</p>
                </div>
                <div class="permission-grid">
                  <div v-for="permission in accessRoleSummary(selectedItem).permissions" :key="permission" class="permission-chip">
                    {{ permission }}
                  </div>
                </div>
              </div>
            </div>

            <div class="detail-group">
              <h4>Business Roles</h4>
              <div class="stack-list access-role-stack">
                <article v-for="role in crm.accessRoles" :key="role.role" class="stack-card">
                  <div class="stack-card-head">
                    <div>
                      <h3>{{ role.label }}</h3>
                      <p>{{ role.description }}</p>
                    </div>
                    <span class="meta-pill">{{ role.dataScope }}</span>
                  </div>
                  <div class="permission-grid">
                    <div v-for="permission in role.permissions.slice(0, 8)" :key="permission" class="permission-chip">
                      {{ permission }}
                    </div>
                  </div>
                </article>
              </div>
            </div>
          </div>
        </aside>

        <aside v-else-if="currentModule === 'projects'" class="insight-panel vue-insight-panel">
          <div class="insight-header">
            <span class="eyebrow">Project Insight</span>
            <h2>{{ selectedItem?.projectName || 'Project Overview' }}</h2>
          </div>
          <div v-if="selectedItem" class="insight-content">
            <div class="detail-group">
              <h4>Project Detail</h4>
              <div class="detail-list">
                <div v-for="[label, value] in projectInsight(selectedItem)" :key="label" class="detail-item">
                  <span>{{ label }}</span>
                  <strong>{{ value }}</strong>
                </div>
              </div>
            </div>
          </div>
        </aside>
      </section>

      <EntityDialog
        :open="dialogOpen"
        :title="`${editingItem ? 'Edit' : 'Add'} ${currentConfig.singular}`"
        :fields="currentConfig.fields"
        :model-value="editingItem"
        :submit-label="editingItem ? 'Update' : 'Save'"
        :options-resolver="resolveFieldOptions"
        :error="dialogError"
        @close="dialogOpen = false"
        @submit="saveModal"
      />

      <datalist id="frontend-market-options">
        <option v-for="market in MARKET_OPTIONS" :key="market" :value="market" />
      </datalist>
    </main>
  </div>
</template>
