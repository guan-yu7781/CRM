export const modulePermissions = {
  customers: { view: 'CUSTOMER_VIEW', create: 'CUSTOMER_CREATE', edit: 'CUSTOMER_EDIT', delete: 'CUSTOMER_DELETE' },
  contacts: { view: 'CONTACT_VIEW', create: 'CONTACT_CREATE', edit: 'CONTACT_EDIT', delete: 'CONTACT_DELETE' },
  projects: { view: 'PROJECT_VIEW', create: 'PROJECT_CREATE', edit: 'PROJECT_EDIT', delete: 'PROJECT_DELETE' },
  deals: { view: 'DEAL_VIEW', create: 'DEAL_CREATE', edit: 'DEAL_EDIT', delete: 'DEAL_DELETE' },
  tasks: { view: 'TASK_VIEW', create: 'TASK_CREATE', edit: 'TASK_EDIT', delete: 'TASK_DELETE' },
  activities: { view: 'ACTIVITY_VIEW', create: 'ACTIVITY_CREATE', edit: 'ACTIVITY_EDIT', delete: 'ACTIVITY_DELETE' },
  accessControl: { view: 'ACCESS_CONTROL_VIEW', create: 'ACCESS_CONTROL_MANAGE', edit: 'ACCESS_CONTROL_MANAGE', delete: 'ACCESS_CONTROL_MANAGE' },
  auditLogs: { view: 'AUDIT_LOG_VIEW' }
};

export const moduleMenu = [
  { key: 'customers', label: 'Customer Management' },
  { key: 'contacts', label: 'Contacts' },
  { key: 'projects', label: 'Projects' },
  { key: 'deals', label: 'Opportunities' },
  { key: 'tasks', label: 'Service Tasks' },
  { key: 'activities', label: 'Interactions' },
  { key: 'accessControl', label: 'Access Control' },
  { key: 'auditLogs', label: 'Audit Log', routeName: 'audit-logs' }
];
