const AFRICA_MARKETS = [
    "Algeria", "Angola", "Benin", "Botswana", "Burkina Faso", "Burundi", "Cabo Verde",
    "Cameroon", "Central African Republic", "Chad", "Comoros", "Congo", "Cote d'Ivoire",
    "Democratic Republic of the Congo", "Djibouti", "Egypt", "Equatorial Guinea", "Eritrea",
    "Eswatini", "Ethiopia", "Gabon", "Gambia", "Ghana", "Guinea", "Guinea-Bissau", "Kenya",
    "Lesotho", "Liberia", "Libya", "Madagascar", "Malawi", "Mali", "Mauritania", "Mauritius",
    "Morocco", "Mozambique", "Namibia", "Niger", "Nigeria", "Rwanda", "Sao Tome and Principe",
    "Senegal", "Seychelles", "Sierra Leone", "Somalia", "South Africa", "South Sudan", "Sudan",
    "Tanzania", "Togo", "Tunisia", "Uganda", "Zambia", "Zimbabwe"
];
const ASIA_MARKETS = [
    "Afghanistan", "Armenia", "Azerbaijan", "Bahrain", "Bangladesh", "Bhutan", "Brunei Darussalam",
    "Cambodia", "China", "Cyprus", "Georgia", "India", "Indonesia", "Iran", "Iraq", "Israel",
    "Japan", "Jordan", "Kazakhstan", "Kuwait", "Kyrgyzstan", "Lao People's Democratic Republic",
    "Lebanon", "Malaysia", "Maldives", "Mongolia", "Myanmar", "Nepal", "Democratic People's Republic of Korea",
    "Oman", "Pakistan", "Philippines", "Qatar", "Republic of Korea", "Saudi Arabia", "Singapore",
    "Sri Lanka", "State of Palestine", "Syrian Arab Republic", "Tajikistan", "Thailand", "Timor-Leste",
    "Turkiye", "Turkmenistan", "United Arab Emirates", "Uzbekistan", "Viet Nam", "Yemen"
];
const MARKET_OPTIONS = [...AFRICA_MARKETS, ...ASIA_MARKETS].sort((a, b) => a.localeCompare(b));

const entityConfig = {
    customers: {
        label: "Customer Management",
        title: "Banking Customer Management",
        description: "Manage customer master data, risk, ownership, and open a dedicated Customer 360 workspace when needed.",
        singular: "Customer",
        path: "/api/customers",
        fields: [
            field("name", "Customer Name", "text", true),
            field("customerType", "Customer Type", "select", false, [
                option("COMMERCIAL_BANK", "Commercial Bank"),
                option("PAYMENT_INSTITUTION", "Payment Institution"),
                option("CENTRAL_BANK", "Central Bank"),
                option("MICROFINANCE_BANK", "Microfinance Bank"),
                option("SACCO", "SACCO")
            ]),
            field("cifNumber", "CIF Number", "text", true),
            field("segment", "Segment", "select", false, ["RETAIL", "SME", "CORPORATE", "WEALTH"]),
            field("status", "Lifecycle Status", "select", false, ["LEAD", "ACTIVE", "INACTIVE"]),
            field("riskLevel", "Risk Level", "select", false, ["LOW", "MEDIUM", "HIGH"]),
            field("notes", "Relationship Notes", "textarea", false, null, true)
        ],
        getTitle: item => item.name,
        getDescription: item => `${item.cifNumber} • ${beautify(item.segment)}`,
        getMeta: item => [item.customerType, item.status, item.riskLevel]
    },
    contacts: {
        label: "Contacts",
        title: "Contact Directory",
        description: "Maintain authorized contacts, decision makers, and service counterparts linked to each customer.",
        singular: "Contact",
        path: "/api/contacts",
        fields: [
            field("firstName", "First Name", "text", true),
            field("lastName", "Last Name", "text", true),
            field("email", "Email", "email", true),
            field("phone", "Phone"),
            field("jobTitle", "Role / Title"),
            relationField("customerId", "Customer", "customers", true),
            field("notes", "Notes", "textarea", false, null, true)
        ],
        getTitle: item => `${item.firstName} ${item.lastName}`,
        getDescription: item => `${item.customerName} • ${item.email}${item.jobTitle ? ` • ${item.jobTitle}` : ""}`,
        getMeta: item => [item.phone || "No phone"]
    },
    projects: {
        label: "Projects",
        title: "Customer Project Management",
        description: "Manage customer projects by market, license value, implementation value, tax rate, and contract status.",
        singular: "Project",
        path: "/api/projects",
        fields: [
            field("projectName", "Project Name", "text", true),
            field("market", "Market", "market-search", true),
            field("licenseAmount", "License Amount", "number", true),
            field("implementationAmount", "Implementation Amount", "number", true),
            field("taxRate", "Tax Rate", "number", true),
            field("status", "Contract Status", "select", false, ["SIGNED_CONTRACT", "UNSIGNED_CONTRACT"]),
            relationField("customerId", "Customer", "customers", true)
        ],
        getTitle: item => item.projectName,
        getDescription: item => `${item.customerName} • ${item.market} • License ${formatMoney(item.licenseAmount)}`,
        getMeta: item => [item.status, `Impl ${formatMoney(item.implementationAmount)}`, `${Number(item.taxRate || 0)}%`]
    },
    deals: {
        label: "Opportunities",
        title: "Revenue Opportunities",
        description: "Track acquisition, cross-sell, lending, and treasury opportunities across the banking pipeline.",
        singular: "Opportunity",
        path: "/api/deals",
        fields: [
            field("title", "Opportunity Name", "text", true),
            field("amount", "Expected Value", "number", true),
            field("stage", "Stage", "select", false, ["NEW", "QUALIFIED", "PROPOSAL_SENT", "NEGOTIATION", "WON", "LOST"]),
            field("expectedCloseDate", "Expected Close", "date"),
            relationField("customerId", "Customer", "customers", true),
            field("notes", "Notes", "textarea", false, null, true)
        ],
        getTitle: item => item.title,
        getDescription: item => `${item.customerName} • ${formatMoney(item.amount)}${item.expectedCloseDate ? ` • ${item.expectedCloseDate}` : ""}`,
        getMeta: item => [item.stage]
    },
    tasks: {
        label: "Service Tasks",
        title: "Service & Fulfilment Tasks",
        description: "Manage onboarding actions, service requests, remediation items, and account servicing work.",
        singular: "Service Task",
        path: "/api/tasks",
        fields: [
            field("title", "Task Title", "text", true),
            field("status", "Status", "select", false, ["OPEN", "IN_PROGRESS", "COMPLETED", "CANCELLED"]),
            field("priority", "Priority", "select", false, ["LOW", "MEDIUM", "HIGH"]),
            field("dueDate", "Due Date", "date"),
            relationField("customerId", "Customer", "customers", true),
            relationField("dealId", "Opportunity", "deals"),
            field("description", "Description", "textarea", false, null, true)
        ],
        getTitle: item => item.title,
        getDescription: item => `${item.customerName}${item.dealTitle ? ` • ${item.dealTitle}` : ""}${item.dueDate ? ` • Due ${item.dueDate}` : ""}`,
        getMeta: item => [item.status, item.priority]
    },
    activities: {
        label: "Interactions",
        title: "Customer Interaction Journal",
        description: "Capture relationship manager interactions, digital follow-ups, branch meetings, and service notes.",
        singular: "Interaction",
        path: "/api/activities",
        fields: [
            field("type", "Type", "select", false, ["CALL", "EMAIL", "MEETING", "NOTE", "FOLLOW_UP"]),
            field("subject", "Subject", "text", true),
            field("activityDate", "Interaction Date", "datetime-local", true),
            relationField("customerId", "Customer", "customers", true),
            relationField("contactId", "Contact", "contacts"),
            relationField("dealId", "Opportunity", "deals"),
            field("details", "Details", "textarea", false, null, true)
        ],
        getTitle: item => item.subject,
        getDescription: item => `${item.customerName} • ${formatDateTime(item.activityDate)}${item.contactName ? ` • ${item.contactName}` : ""}`,
        getMeta: item => [item.type, item.createdBy]
    }
};

const initialQuery = new URLSearchParams(window.location.search);

const state = {
    token: localStorage.getItem("crmToken") || "",
    username: localStorage.getItem("crmUsername") || "",
    currentView: "customers",
    search: "",
    sidebarCollapsed: false,
    customerTab: "overview",
    projectFilters: {
        customerId: "",
        market: "",
        status: ""
    },
    editingId: null,
        selectedId: null,
        data: {
            customers: [],
            contacts: [],
            projects: [],
            deals: [],
            tasks: [],
            activities: []
    }
};

const loginPanel = document.getElementById("login-panel");
const appShell = document.getElementById("app-shell");
const loginMessage = document.getElementById("login-message");
const appMessage = document.getElementById("app-message");
const modal = document.getElementById("record-modal");
const modalForm = document.getElementById("record-form");
const contactInsightModal = document.getElementById("contact-insight-modal");
const contactInsightBody = document.getElementById("contact-insight-body");
const contactInsightTitle = document.getElementById("contact-insight-title");
const projectInsightModal = document.getElementById("project-insight-modal");
const projectInsightBody = document.getElementById("project-insight-body");
const projectInsightTitle = document.getElementById("project-insight-title");
const dealConversionModal = document.getElementById("deal-conversion-modal");
const dealConversionForm = document.getElementById("deal-conversion-form");
const dealConversionTitle = document.getElementById("deal-conversion-title");
const globalMarketOptions = document.getElementById("global-market-options");
const recordsList = document.getElementById("records-list");
const projectFilters = document.getElementById("project-filters");
const workspaceGrid = document.getElementById("workspace-grid");
const contentShell = document.querySelector(".content");
const sidebarToggle = document.getElementById("sidebar-toggle");
const panelTitle = document.getElementById("panel-title");
const panelSubtitleText = document.getElementById("panel-subtitle-text");
const recordCount = document.getElementById("record-count");
const searchInput = document.getElementById("search-input");
const insightPanel = document.querySelector(".insight-panel");

document.getElementById("login-form").addEventListener("submit", handleLogin);
document.getElementById("logout-button").addEventListener("click", logout);
document.getElementById("refresh-button").addEventListener("click", refreshAll);
document.getElementById("add-record-button").addEventListener("click", () => openModal());
sidebarToggle.addEventListener("click", toggleSidebar);
searchInput.addEventListener("input", handleSearch);
projectFilters.addEventListener("input", handleProjectFilterChange);
projectFilters.addEventListener("change", handleProjectFilterChange);
document.getElementById("close-modal-button").addEventListener("click", closeModal);
document.querySelector(".modal-backdrop").addEventListener("click", closeModal);
document.getElementById("close-contact-insight-button").addEventListener("click", closeContactInsightModal);
document.querySelector("[data-contact-close='true']").addEventListener("click", closeContactInsightModal);
document.getElementById("close-project-insight-button").addEventListener("click", closeProjectInsightModal);
document.querySelector("[data-project-close='true']").addEventListener("click", closeProjectInsightModal);
document.getElementById("close-deal-conversion-button").addEventListener("click", closeDealConversionModal);
document.querySelector("[data-deal-conversion-close='true']").addEventListener("click", closeDealConversionModal);
document.querySelectorAll(".menu-item").forEach(button => {
    button.addEventListener("click", () => switchView(button.dataset.view));
});
recordsList.addEventListener("click", handleRecordListClick);

bootstrap();

function bootstrap() {
    renderGlobalMarketOptions();
    if (state.token) {
        const requestedView = initialQuery.get("view");
        if (requestedView && entityConfig[requestedView]) {
            state.currentView = requestedView;
        }
        showApp();
        syncSidebar();
        refreshAll();
    } else {
        showLogin();
    }
}

async function handleLogin(event) {
    event.preventDefault();
    const payload = Object.fromEntries(new FormData(event.target).entries());
    loginMessage.textContent = "Signing in...";

    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        const data = await response.json();
        if (!response.ok) {
            throw new Error(extractError(data));
        }

        state.token = data.token;
        state.username = data.username;
        localStorage.setItem("crmToken", data.token);
        localStorage.setItem("crmUsername", data.username);
        loginMessage.textContent = "";
        showApp();
        await refreshAll();
    } catch (error) {
        loginMessage.textContent = error.message;
    }
}

function logout() {
    state.token = "";
    state.username = "";
    localStorage.removeItem("crmToken");
    localStorage.removeItem("crmUsername");
    closeModal();
    showLogin();
    appMessage.textContent = "Signed out.";
}

function showLogin() {
    loginPanel.classList.remove("hidden");
    appShell.classList.add("hidden");
}

function showApp() {
    loginPanel.classList.add("hidden");
    appShell.classList.remove("hidden");
    document.getElementById("current-user").textContent = state.username || "admin";
    updateHeader();
}

function switchView(view) {
    state.currentView = view;
    state.search = "";
    state.selectedId = null;
    closeContactInsightModal();
    closeProjectInsightModal();
    state.customerTab = "overview";
    if (view !== "projects") {
        state.projectFilters = {
            customerId: "",
            market: "",
            status: ""
        };
    }
    searchInput.value = "";
    document.querySelectorAll(".menu-item").forEach(button => {
        button.classList.toggle("active", button.dataset.view === view);
    });
    updateHeader();
    renderCurrentView();
}

function updateHeader() {
    const config = entityConfig[state.currentView];
    document.getElementById("view-label").textContent = config.label;
    document.getElementById("view-title").textContent = config.title;
    document.getElementById("view-description").textContent = config.description;
    document.getElementById("add-record-button").textContent = `Add ${config.singular}`;
}

async function refreshAll() {
    try {
        const [customers, contacts, projects, deals, tasks, activities] = await Promise.all([
            api("/api/customers"),
            api("/api/contacts"),
            api("/api/projects"),
            api("/api/deals"),
            api("/api/tasks"),
            api("/api/activities")
        ]);

        state.data.customers = customers;
        state.data.contacts = contacts;
        state.data.projects = projects;
        state.data.deals = deals;
        state.data.tasks = tasks;
        state.data.activities = activities;
        ensureSelectedRecord();
        renderCurrentView();
        appMessage.textContent = "Workspace refreshed.";
    } catch (error) {
        handleAppError(error);
    }
}

function renderCurrentView() {
    const items = filteredItems();
    const selected = resolveSelectedItem(items);
    workspaceGrid.classList.remove("customer-workspace");
    const tableWorkspace = state.currentView === "contacts" || state.currentView === "projects";
    workspaceGrid.classList.toggle("contacts-workspace", tableWorkspace);
    contentShell.classList.toggle("contacts-fullscreen", tableWorkspace);
    insightPanel.classList.toggle("hidden", tableWorkspace);
    panelTitle.textContent = state.currentView === "customers" ? "Customers" : "Records";
    panelSubtitleText.textContent = `${items.length} items in the current module`;
    searchInput.placeholder = state.currentView === "customers"
        ? "Search customer name or CIF"
        : state.currentView === "contacts"
            ? "Search customer, contact name, email, or role"
            : state.currentView === "projects"
                ? "Search customer, project name, market, or status"
            : "Search records";
    renderToolbarExtras(items);

    if (state.currentView === "customers") {
        recordCount.textContent = `${items.length} customer(s)`;
        document.getElementById("records-list").innerHTML = items.length
            ? renderCustomerManagementList(items, selected)
            : renderEmptyState();
    } else if (state.currentView === "contacts") {
        const grouped = groupContactsByCustomer(items);
        recordCount.textContent = `${items.length} contacts across ${grouped.length} customers`;
        document.getElementById("records-list").innerHTML = items.length
            ? renderContactCustomerGroups(grouped, selected)
            : renderContactEmptyState();
    } else if (state.currentView === "projects") {
        const grouped = groupProjectsByCustomer(items);
        recordCount.textContent = `${items.length} projects across ${grouped.length} customers`;
        document.getElementById("records-list").innerHTML = items.length
            ? renderProjectCustomerGroups(grouped, selected)
            : renderProjectEmptyState();
    } else {
        recordCount.textContent = String(items.length);
        document.getElementById("records-list").innerHTML = items.length
            ? items.map(item => renderRecord(state.currentView, item, selected && selected.id === item.id)).join("")
            : renderEmptyState();
    }

    renderSummaryCards(state.currentView);
    renderInsightPanel(state.currentView, selected);
}

function filteredItems() {
    const items = state.data[state.currentView] || [];
    const keyword = state.search.trim().toLowerCase();
    const filteredByView = state.currentView === "projects"
        ? items.filter(item => projectMatchesFilters(item))
        : items;

    if (!keyword) {
        return filteredByView;
    }

    return filteredByView.filter(item => JSON.stringify(item).toLowerCase().includes(keyword));
}

function renderRecord(view, item, selected) {
    const config = entityConfig[view];
    const meta = config.getMeta(item)
        .filter(Boolean)
        .map(value => `<span class="meta-pill">${escapeHtml(String(value))}</span>`)
        .join("");

    return `
        <article class="record-row ${selected ? "selected" : ""}" data-select-id="${item.id}">
            <div class="record-main">
                <div class="record-title-line">
                    <h3>${escapeHtml(config.getTitle(item))}</h3>
                    <span class="record-chevron">View</span>
                </div>
                <div class="record-meta">${meta}</div>
                <p>${escapeHtml(config.getDescription(item))}</p>
            </div>
            <div class="record-actions">${renderRecordActions(view, item)}</div>
        </article>
    `;
}

function renderRecordActions(view, item) {
    if (view === "deals") {
        const conversionAction = item.stage === "WON"
            ? item.convertedProjectId
                ? `<button class="ghost-button" type="button" data-open-project-id="${item.convertedProjectId}">Open Project</button>`
                : `<button class="ghost-button" type="button" data-convert-deal-id="${item.id}">Convert to Project</button>`
            : "";
        return `
            ${conversionAction}
            <button class="ghost-button" type="button" data-edit-id="${item.id}">Edit</button>
            <button class="danger-button" type="button" data-delete-id="${item.id}">Delete</button>
        `;
    }

    return `
        <button class="ghost-button" type="button" data-edit-id="${item.id}">Edit</button>
        <button class="danger-button" type="button" data-delete-id="${item.id}">Delete</button>
    `;
}

function renderEmptyState() {
    return `
        <div class="empty-state">
            <h3>No records found</h3>
            <p>Create the first record to start building your banking customer book.</p>
        </div>
    `;
}

function renderContactEmptyState() {
    return `
        <div class="empty-state">
            <h3>No contacts found</h3>
            <p>Search by customer or create the first contact to build a customer-linked directory.</p>
        </div>
    `;
}

function renderCustomerFinder(items, selected) {
    const hasSearch = state.search.trim().length > 0;
    const matches = hasSearch ? items.slice(0, 8) : [];

    return `
        <section class="customer-finder compact">
            ${selected ? `
                <div class="customer-finder-selected compact">
                    <div>
                        <span>Selected Customer</span>
                        <strong>${escapeHtml(selected.name)}</strong>
                        <small>${escapeHtml(selected.cifNumber || "CIF pending")} • ${escapeHtml(beautify(selected.customerType))}</small>
                    </div>
                    <button class="ghost-button" type="button" data-select-id="${selected.id}">Open 360</button>
                </div>
            ` : `
                <div class="customer-finder-hint">
                    <span class="eyebrow">Keyword Search</span>
                    <p>Search by customer name or CIF to open Customer 360.</p>
                </div>
            `}
            ${hasSearch ? `
                <div class="customer-finder-dropdown">
                    ${matches.length ? matches.map(item => `
                        <button class="customer-finder-option compact ${selected && selected.id === item.id ? "active" : ""}" type="button" data-select-id="${item.id}">
                            <strong>${escapeHtml(item.name)}</strong>
                            <span>${escapeHtml(item.cifNumber || "CIF pending")} • ${escapeHtml(beautify(item.customerType))}</span>
                        </button>
                    `).join("") : `<div class="customer-finder-empty">No customer matched this keyword.</div>`}
                </div>
            ` : ""}
        </section>
    `;
}

function renderCustomerManagementList(items, selected) {
    return items.map(item => {
        const meta = [
            item.customerType,
            item.status,
            item.riskLevel
        ].filter(Boolean).map(value => `<span class="meta-pill">${escapeHtml(beautify(String(value)))}</span>`).join("");

        return `
            <article class="record-row ${selected && selected.id === item.id ? "selected" : ""}" data-select-id="${item.id}">
                <div class="record-main">
                    <div class="record-title-line">
                        <h3>${escapeHtml(item.name)}</h3>
                        <span class="record-chevron">${escapeHtml(item.cifNumber || "CIF pending")}</span>
                    </div>
                    <div class="record-meta">${meta}</div>
                    <p>${escapeHtml(beautify(item.segment || "unassigned"))} • ${escapeHtml(beautify(item.status || "lead"))} • ${escapeHtml(beautify(item.customerType || "commercial_bank"))}</p>
                </div>
                <div class="record-actions">
                    <a class="ghost-button link-button" href="/customer-360.html?customerId=${item.id}">Open 360</a>
                    <button class="ghost-button" type="button" data-edit-id="${item.id}">Edit</button>
                    <button class="danger-button" type="button" data-delete-id="${item.id}">Delete</button>
                </div>
            </article>
        `;
    }).join("");
}

function groupContactsByCustomer(items) {
    const groups = new Map();

    items.forEach(item => {
        const key = item.customerId || `unassigned-${item.id}`;
        if (!groups.has(key)) {
            groups.set(key, {
                customerId: item.customerId || null,
                customerName: item.customerName || "Unassigned Customer",
                customerSummary: resolveCustomerSummary(item.customerId),
                contacts: []
            });
        }
        groups.get(key).contacts.push(item);
    });

    return Array.from(groups.values())
        .map(group => ({
            ...group,
            contacts: group.contacts.sort((left, right) =>
                `${left.firstName} ${left.lastName}`.localeCompare(`${right.firstName} ${right.lastName}`)
            )
        }))
        .sort((left, right) => left.customerName.localeCompare(right.customerName));
}

function renderContactCustomerGroups(groups, selected) {
    return `
        ${groups.map(group => {
        const titled = group.contacts.filter(item => item.jobTitle).length;
        const reachable = group.contacts.filter(item => item.phone).length;

        return `
            <section class="customer-contact-group">
                <header class="customer-contact-header">
                    <div class="customer-contact-identity">
                        <span class="eyebrow">Customer Directory</span>
                        <h3>${escapeHtml(group.customerName)}</h3>
                        <p>${escapeHtml(group.customerSummary || "Customer profile pending")} • ${group.contacts.length} linked contact(s)</p>
                    </div>
                    <div class="customer-contact-summary">
                        <div class="customer-contact-stat">
                            <span>Contacts</span>
                            <strong>${group.contacts.length}</strong>
                        </div>
                        <div class="customer-contact-stat">
                            <span>With Role</span>
                            <strong>${titled}</strong>
                        </div>
                        <div class="customer-contact-stat">
                            <span>Reachable</span>
                            <strong>${reachable}</strong>
                        </div>
                    </div>
                </header>
                ${renderContactDirectoryTable(group.contacts, selected)}
            </section>
        `;
    }).join("")}
    `;
}

function toggleSidebar() {
    state.sidebarCollapsed = !state.sidebarCollapsed;
    syncSidebar();
}

function syncSidebar() {
    appShell.classList.toggle("sidebar-collapsed", state.sidebarCollapsed);
    sidebarToggle.textContent = state.sidebarCollapsed ? "▶" : "◀";
    sidebarToggle.title = state.sidebarCollapsed ? "Show menu" : "Collapse menu";
    sidebarToggle.setAttribute("aria-label", state.sidebarCollapsed ? "Show menu" : "Collapse menu");
    sidebarToggle.setAttribute("aria-expanded", String(!state.sidebarCollapsed));
}

function renderContactDirectoryTable(items, selected) {
    return `
        <div class="customer-contact-table-shell">
            <table class="contact-directory-table">
                <thead>
                    <tr>
                        <th>Contact Name</th>
                        <th>Role</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Customer</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    ${items.map(item => renderContactDirectoryRow(item, selected && selected.id === item.id)).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function renderContactDirectoryRow(item, selected) {
    return `
        <tr class="${selected ? "selected" : ""}" data-select-id="${item.id}">
            <td data-label="Contact Name">
                <strong>${escapeHtml(`${item.firstName} ${item.lastName}`)}</strong>
            </td>
            <td data-label="Role">${escapeHtml(item.jobTitle || "Role not captured")}</td>
            <td data-label="Email">${escapeHtml(item.email || "Not captured")}</td>
            <td data-label="Phone">${escapeHtml(item.phone || "Not captured")}</td>
            <td data-label="Customer">${escapeHtml(item.customerName || "Unassigned")}</td>
            <td data-label="Actions">
                <div class="contact-table-actions">
                    <button class="ghost-button" type="button" data-edit-id="${item.id}">Edit</button>
                    <button class="danger-button" type="button" data-delete-id="${item.id}">Delete</button>
                </div>
            </td>
        </tr>
    `;
}

function resolveCustomerSummary(customerId) {
    if (!customerId) {
        return "";
    }
    const customer = state.data.customers.find(item => item.id === customerId);
    if (!customer) {
        return "";
    }
    return [customer.cifNumber || "CIF pending", beautify(customer.status || "lead")]
        .filter(Boolean)
        .join(" • ");
}

function handleRecordListClick(event) {
    const contactInsightButton = event.target.closest("[data-contact-insight-id]");
    if (contactInsightButton) {
        openContactInsightModal(Number(contactInsightButton.dataset.contactInsightId));
        return;
    }

    const projectInsightButton = event.target.closest("[data-project-insight-id]");
    if (projectInsightButton) {
        openProjectInsightModal(Number(projectInsightButton.dataset.projectInsightId));
        return;
    }

    const editButton = event.target.closest("[data-edit-id]");
    if (editButton) {
        openModal(Number(editButton.dataset.editId));
        return;
    }

    const convertDealButton = event.target.closest("[data-convert-deal-id]");
    if (convertDealButton) {
        openDealConversionModal(Number(convertDealButton.dataset.convertDealId));
        return;
    }

    const openProjectButton = event.target.closest("[data-open-project-id]");
    if (openProjectButton) {
        state.currentView = "projects";
        state.selectedId = Number(openProjectButton.dataset.openProjectId);
        closeProjectInsightModal();
        closeContactInsightModal();
        document.querySelectorAll(".menu-item").forEach(button => {
            button.classList.toggle("active", button.dataset.view === "projects");
        });
        updateHeader();
        renderCurrentView();
        return;
    }

    const deleteButton = event.target.closest("[data-delete-id]");
    if (deleteButton) {
        removeRecord(Number(deleteButton.dataset.deleteId));
        return;
    }

    const row = event.target.closest("[data-select-id]");
    if (row) {
        state.selectedId = Number(row.dataset.selectId);
        if (state.currentView === "contacts") {
            openContactInsightModal(state.selectedId);
            renderCurrentView();
            return;
        }
        if (state.currentView === "projects") {
            openProjectInsightModal(state.selectedId);
            renderCurrentView();
            return;
        }
        renderCurrentView();
    }
}

function openContactInsightModal(contactId) {
    const item = (state.data.contacts || []).find(contact => contact.id === contactId);
    if (!item) {
        return;
    }
    state.selectedId = contactId;
    contactInsightTitle.textContent = `${item.firstName} ${item.lastName}`;
    contactInsightBody.innerHTML = renderContactInsight(item);
    contactInsightModal.classList.remove("hidden");
    contactInsightModal.setAttribute("aria-hidden", "false");
}

function closeContactInsightModal() {
    contactInsightModal.classList.add("hidden");
    contactInsightModal.setAttribute("aria-hidden", "true");
}

function openProjectInsightModal(projectId) {
    const item = (state.data.projects || []).find(project => project.id === projectId);
    if (!item) {
        return;
    }
    state.selectedId = projectId;
    projectInsightTitle.textContent = item.projectName;
    projectInsightBody.innerHTML = renderProjectInsight(item);
    projectInsightModal.classList.remove("hidden");
    projectInsightModal.setAttribute("aria-hidden", "false");
}

function closeProjectInsightModal() {
    projectInsightModal.classList.add("hidden");
    projectInsightModal.setAttribute("aria-hidden", "true");
}

function openDealConversionModal(dealId) {
    const deal = (state.data.deals || []).find(item => item.id === dealId);
    if (!deal) {
        return;
    }
    if (deal.stage !== "WON") {
        appMessage.textContent = "Only won opportunities can be converted into a project.";
        return;
    }
    if (deal.convertedProjectId) {
        appMessage.textContent = "This opportunity has already been converted into a project.";
        return;
    }

    dealConversionTitle.textContent = `Convert ${deal.title} to Project`;
    dealConversionForm.innerHTML = buildDealConversionForm(deal);
    dealConversionForm.onsubmit = submitDealConversionForm;
    dealConversionModal.dataset.dealId = String(deal.id);
    dealConversionModal.classList.remove("hidden");
    dealConversionModal.setAttribute("aria-hidden", "false");
}

function closeDealConversionModal() {
    dealConversionModal.classList.add("hidden");
    dealConversionModal.setAttribute("aria-hidden", "true");
    dealConversionModal.dataset.dealId = "";
}

function buildDealConversionForm(deal) {
    return `
        <div class="field full">
            <span>Opportunity</span>
            <input value="${escapeHtml(deal.title)}" type="text" disabled>
        </div>
        <label class="field">
            <span>Project Name</span>
            <input name="projectName" type="text" value="${escapeHtml(deal.title)}" required>
        </label>
        <label class="field">
            <span>Market</span>
            <input name="market" type="text" list="global-market-options" placeholder="Search Africa or Asia market" required>
        </label>
        <label class="field">
            <span>License Amount</span>
            <input name="licenseAmount" type="number" min="0" step="0.01" value="${escapeHtml(String(Number(deal.amount || 0)))}" required>
        </label>
        <label class="field">
            <span>Implementation Amount</span>
            <input name="implementationAmount" type="number" min="0" step="0.01" value="0" required>
        </label>
        <label class="field">
            <span>Tax Rate</span>
            <input name="taxRate" type="number" min="0" step="0.01" value="0" required>
        </label>
        <div class="field full">
            <span>Conversion Rule</span>
            <input value="Won opportunities convert into signed projects." type="text" disabled>
        </div>
        <div class="form-footer">
            <button class="ghost-button" type="button" id="cancel-deal-conversion-button">Cancel</button>
            <button class="primary-button" type="submit">Create Project</button>
        </div>
    `;
}

async function submitDealConversionForm(event) {
    event.preventDefault();
    const dealId = Number(dealConversionModal.dataset.dealId);
    if (!dealId) {
        return;
    }

    const payload = {
        projectName: event.target.projectName.value,
        market: event.target.market.value,
        licenseAmount: Number(event.target.licenseAmount.value),
        implementationAmount: Number(event.target.implementationAmount.value),
        taxRate: Number(event.target.taxRate.value)
    };

    try {
        const project = await api(`/api/deals/${dealId}/convert-to-project`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${state.token}`
            },
            body: JSON.stringify(payload)
        });
        closeDealConversionModal();
        state.currentView = "projects";
        state.selectedId = project.id;
        document.querySelectorAll(".menu-item").forEach(button => {
            button.classList.toggle("active", button.dataset.view === "projects");
        });
        updateHeader();
        await refreshAll();
        openProjectInsightModal(project.id);
        appMessage.textContent = "Opportunity converted into project successfully.";
    } catch (error) {
        handleAppError(error);
    }
}

function renderSummaryCards(view) {
    const summary = buildSummary(view);
    summary.forEach((card, index) => {
        document.getElementById(`summary-label-${index + 1}`).textContent = card.label;
        document.getElementById(`summary-value-${index + 1}`).textContent = card.value;
        document.getElementById(`summary-note-${index + 1}`).textContent = card.note;
    });
}

function buildSummary(view) {
    if (view === "customers") {
        const customers = state.data.customers;
        const corporate = customers.filter(item => item.segment === "CORPORATE").length;
        const highRisk = customers.filter(item => item.riskLevel === "HIGH").length;
        const active = customers.filter(item => item.status === "ACTIVE").length;
        return [
            { label: "Customers", value: String(customers.length), note: "Total customer master records" },
            { label: "Corporate", value: String(corporate), note: "Customers in the corporate segment" },
            { label: "High Risk", value: String(highRisk), note: "Customers requiring monitoring" },
            { label: "Active", value: String(active), note: "Live customer relationships" }
        ];
    }

    if (view === "contacts") {
        const contacts = state.data.contacts;
        const titled = contacts.filter(item => item.jobTitle).length;
        const customers = new Set(contacts.map(item => item.customerId)).size;
        const phones = contacts.filter(item => item.phone).length;
        return [
            { label: "Contacts", value: String(contacts.length), note: "Known external stakeholders" },
            { label: "With Titles", value: String(titled), note: "Role-aware coverage" },
            { label: "Customer Coverage", value: String(customers), note: "Customers with contacts assigned" },
            { label: "Reachable", value: String(phones), note: "Contacts with phone numbers" }
        ];
    }

    if (view === "deals") {
        const deals = state.data.deals;
        const pipeline = deals
            .filter(item => item.stage !== "WON" && item.stage !== "LOST")
            .reduce((sum, item) => sum + Number(item.amount || 0), 0);
        const won = deals.filter(item => item.stage === "WON").length;
        const open = deals.filter(item => item.stage !== "WON" && item.stage !== "LOST").length;
        return [
            { label: "Pipeline", value: formatMoney(pipeline), note: "Open opportunity value" },
            { label: "Open Deals", value: String(open), note: "Active revenue motions" },
            { label: "Won", value: String(won), note: "Closed business won" },
            { label: "Total", value: String(deals.length), note: "All opportunity records" }
        ];
    }

    if (view === "projects") {
        const projects = state.data.projects;
        const signed = projects.filter(item => item.status === "SIGNED_CONTRACT").length;
        const unsigned = projects.filter(item => item.status === "UNSIGNED_CONTRACT").length;
        const totalLicense = projects.reduce((sum, item) => sum + Number(item.licenseAmount || 0), 0);
        const totalImplementation = projects.reduce((sum, item) => sum + Number(item.implementationAmount || 0), 0);
        return [
            { label: "Projects", value: String(projects.length), note: "Total managed customer projects" },
            { label: "Signed", value: String(signed), note: "Projects with signed contract" },
            { label: "Unsigned", value: String(unsigned), note: "Projects pending contract signature" },
            { label: "License Value", value: formatMoney(totalLicense), note: "Combined license amount" },
            { label: "Implementation Value", value: formatMoney(totalImplementation), note: "Combined implementation amount" }
        ];
    }

    if (view === "tasks") {
        const tasks = state.data.tasks;
        const overdue = tasks.filter(item => item.status !== "COMPLETED" && item.dueDate && new Date(item.dueDate) < startOfToday()).length;
        const open = tasks.filter(item => item.status === "OPEN" || item.status === "IN_PROGRESS").length;
        const high = tasks.filter(item => item.priority === "HIGH").length;
        const completed = tasks.filter(item => item.status === "COMPLETED").length;
        return [
            { label: "Open Tasks", value: String(open), note: "Operational workload in flight" },
            { label: "Overdue", value: String(overdue), note: "Tasks beyond due date" },
            { label: "High Priority", value: String(high), note: "Urgent customer work items" },
            { label: "Completed", value: String(completed), note: "Tasks completed successfully" }
        ];
    }

    const activities = state.data.activities;
    const today = activities.filter(item => isToday(item.activityDate)).length;
    const meetings = activities.filter(item => item.type === "MEETING").length;
    const followUps = activities.filter(item => item.type === "FOLLOW_UP").length;
    return [
        { label: "Interactions", value: String(activities.length), note: "Captured customer touchpoints" },
        { label: "Today", value: String(today), note: "Interactions logged today" },
        { label: "Meetings", value: String(meetings), note: "Advisory or service meetings" },
        { label: "Follow-Ups", value: String(followUps), note: "Next-step commitments" }
    ];
}

function renderInsightPanel(view, item) {
    const panel = document.getElementById("insight-content");
    const heading = document.getElementById("insight-title");

    if (!item) {
        heading.textContent = "Record Insight";
        panel.innerHTML = `
            <div class="empty-insight">
                <p>Select a record to see its customer 360 context, linked activity, and operational details.</p>
            </div>
        `;
        return;
    }

    heading.textContent = `${entityConfig[view].singular} Insight`;

    if (view === "customers") {
        panel.innerHTML = renderCustomerManagementInsight(item);
        return;
    }

    if (view === "contacts") {
        panel.innerHTML = renderContactInsight(item);
        return;
    }

    if (view === "deals") {
        panel.innerHTML = renderDealInsight(item);
        return;
    }

    if (view === "projects") {
        panel.innerHTML = renderProjectInsight(item);
        return;
    }

    if (view === "tasks") {
        panel.innerHTML = renderTaskInsight(item);
        return;
    }

    panel.innerHTML = renderActivityInsight(item);
}

function renderCustomerInsight(item) {
    const contacts = state.data.contacts.filter(contact => contact.customerId === item.id);
    const projects = state.data.projects.filter(project => project.customerId === item.id);
    const deals = state.data.deals.filter(deal => deal.customerId === item.id);
    const tasks = state.data.tasks.filter(task => task.customerId === item.id);
    const activities = state.data.activities.filter(activity => activity.customerId === item.id);
    const openDeals = deals.filter(deal => deal.stage !== "WON" && deal.stage !== "LOST");
    const nextTask = tasks
        .filter(task => task.status !== "COMPLETED" && task.status !== "CANCELLED")
        .sort((a, b) => {
            if (!a.dueDate) {
                return 1;
            }
            if (!b.dueDate) {
                return -1;
            }
            return new Date(a.dueDate) - new Date(b.dueDate);
        })[0];
    const alerts = buildCustomerAlerts(item, openDeals, tasks);

    return `
        <div class="customer-360-shell">
            <aside class="customer-360-core">
                <div class="customer-core-hero">
                    <div class="customer-core-logo">${escapeHtml((item.name || "C").slice(0, 1))}</div>
                    <div>
                        <span class="eyebrow">${escapeHtml(beautify(item.customerType))}</span>
                        <h3>${escapeHtml(item.name)}</h3>
                        <p>${escapeHtml(item.cifNumber || "CIF pending")} • ${escapeHtml(beautify(item.status))}</p>
                    </div>
                </div>
                <div class="customer-core-grid">
                    ${coreMetric("Tier", item.segment || "Unassigned")}
                    ${coreMetric("Customer Score", customerScore(item))}
                    ${coreMetric("Lifecycle", beautify(item.status))}
                </div>
                ${detailGroup("Core Details", [
                    detailItem("Risk", beautify(item.riskLevel)),
                    detailItem("Lifecycle", beautify(item.status))
                ])}
            </aside>
            <section class="customer-360-main">
                <div class="customer-tab-bar">
                    ${renderCustomerTab("overview", "Overview")}
                    ${renderCustomerTab("contacts", "Contacts")}
                    ${renderCustomerTab("opportunities", "Opportunities")}
                    ${renderCustomerTab("projects", "Projects")}
                    ${renderCustomerTab("products", "Products")}
                    ${renderCustomerTab("activities", "Activities")}
                </div>
                <div class="customer-tab-panel">
                    ${renderCustomerTabContent(item, contacts, openDeals, projects, activities, tasks)}
                </div>
            </section>
            <aside class="customer-360-actions">
                ${detailGroup("Next Action", [
                    detailItem("Upcoming Task", nextTask ? `${nextTask.title}${nextTask.dueDate ? ` • ${nextTask.dueDate}` : ""}` : "No open next action")
                ])}
                ${detailGroup("Key Contacts", contacts.length ? contacts.slice(0, 3).map(contact =>
                    detailItem(contact.firstName + " " + contact.lastName, contact.jobTitle || contact.email || "Key contact")
                ) : [
                    detailItem("Coverage", "No key contacts added")
                ])}
                ${detailGroup("Open Deals", openDeals.length ? openDeals.slice(0, 3).map(deal =>
                    detailItem(deal.title, `${formatMoney(deal.amount)} • ${beautify(deal.stage)}`)
                ) : [
                    detailItem("Pipeline", "No open deals")
                ])}
                ${detailGroup("Alerts", alerts.map(alert => detailItem(alert.label, alert.value)))}
                <div class="insight-actions">
                    <a class="primary-button link-button" href="/annual-maintenance.html?customerId=${item.id}&source=customers">Open Annual Maintenance</a>
                </div>
            </aside>
        </div>
    `;
}

function renderCustomerManagementInsight(item) {
    const contacts = state.data.contacts.filter(contact => contact.customerId === item.id).length;
    const projects = state.data.projects.filter(project => project.customerId === item.id).length;
    const deals = state.data.deals.filter(deal => deal.customerId === item.id && deal.stage !== "WON" && deal.stage !== "LOST").length;
    const tasks = state.data.tasks.filter(task => task.customerId === item.id && task.status !== "COMPLETED").length;

    return `
        <div class="insight-hero">
            <span class="eyebrow">Customer Snapshot</span>
            <h3>${escapeHtml(item.name)}</h3>
            <p>${escapeHtml(item.cifNumber || "CIF pending")} • ${escapeHtml(beautify(item.customerType))}</p>
        </div>
        <div class="insight-grid">
            ${insightMetric("Tier", beautify(item.segment || "Unassigned"))}
            ${insightMetric("Lifecycle", beautify(item.status))}
            ${insightMetric("Risk", beautify(item.riskLevel))}
        </div>
        ${detailGroup("Relationship Summary", [
            detailItem("Lifecycle", beautify(item.status)),
            detailItem("Customer Type", beautify(item.customerType)),
            detailItem("CIF", item.cifNumber || "Not captured")
        ])}
        ${detailGroup("Coverage", [
            detailItem("Contacts", String(contacts)),
            detailItem("Projects", String(projects)),
            detailItem("Open Deals", String(deals)),
            detailItem("Open Tasks", String(tasks))
        ])}
        <div class="insight-actions">
            <a class="primary-button link-button" href="/customer-360.html?customerId=${item.id}">Open 360</a>
            <a class="ghost-button link-button" href="/annual-maintenance.html?customerId=${item.id}&source=customers">Annual Maintenance</a>
        </div>
    `;
}

function renderCustomerTab(tab, label) {
    return `<button class="customer-tab ${state.customerTab === tab ? "active" : ""}" type="button" data-customer-tab="${tab}">${escapeHtml(label)}</button>`;
}

function renderCustomerTabContent(item, contacts, openDeals, projects, activities, tasks) {
    if (state.customerTab === "contacts") {
        return renderCustomerCollection("Contacts", contacts, contact =>
            `<strong>${escapeHtml(contact.firstName)} ${escapeHtml(contact.lastName)}</strong><span>${escapeHtml(contact.jobTitle || contact.email || "No role captured")}</span>`
        );
    }

    if (state.customerTab === "opportunities") {
        return renderCustomerCollection("Opportunities", openDeals, deal =>
            `<strong>${escapeHtml(deal.title)}</strong><span>${formatMoney(deal.amount)} • ${escapeHtml(beautify(deal.stage))}</span>`
        );
    }

    if (state.customerTab === "projects") {
        return renderCustomerCollection("Projects", projects, project =>
            `<strong>${escapeHtml(project.projectName)}</strong><span>${escapeHtml(project.market)} • License ${formatMoney(project.licenseAmount)} • Implementation ${formatMoney(project.implementationAmount)} • ${escapeHtml(beautify(project.status))}</span>`
        );
    }

    if (state.customerTab === "products") {
        return `
            <section class="customer-tab-section">
                <div class="customer-tab-section-header">
                    <span class="eyebrow">Products</span>
                    <h4>Product Holdings</h4>
                </div>
                <div class="customer-product-grid">
                    ${productCard("Projects", String(projects.length), "Mapped from active customer projects")}
                    ${productCard("Opportunities", String(openDeals.length), "Pipeline that can convert into product revenue")}
                    ${productCard("Service Tasks", String(tasks.length), "Operational servicing workload")}
                </div>
            </section>
        `;
    }

    if (state.customerTab === "activities") {
        return renderCustomerCollection("Activities", activities.slice().sort((a, b) => new Date(b.activityDate) - new Date(a.activityDate)), activity =>
            `<strong>${escapeHtml(activity.subject)}</strong><span>${escapeHtml(beautify(activity.type))} • ${escapeHtml(formatDateTime(activity.activityDate))}</span>`
        );
    }

    return `
        <section class="customer-tab-section">
            <div class="customer-tab-section-header">
                <span class="eyebrow">Overview</span>
                <h4>Relationship Summary</h4>
            </div>
            <div class="customer-overview-grid">
                ${insightMetric("Contacts", String(contacts.length))}
                ${insightMetric("Open Deals", String(openDeals.length))}
                ${insightMetric("Projects", String(projects.length))}
                ${insightMetric("Activities", String(activities.length))}
            </div>
            ${detailGroup("Customer Summary", [
                detailItem("CIF", item.cifNumber || "Not captured"),
                detailItem("Notes", item.notes || "No notes recorded")
            ])}
        </section>
    `;
}

function renderCustomerCollection(title, items, renderer) {
    return `
        <section class="customer-tab-section">
            <div class="customer-tab-section-header">
                <span class="eyebrow">${escapeHtml(title)}</span>
                <h4>${escapeHtml(title)} Workspace</h4>
            </div>
            <div class="customer-collection">
                ${items.length ? items.map(item => `<article class="customer-collection-item">${renderer(item)}</article>`).join("") : `<div class="empty-insight"><p>No ${escapeHtml(title.toLowerCase())} available for this customer.</p></div>`}
            </div>
        </section>
    `;
}

function productCard(label, value, note) {
    return `
        <article class="customer-product-card">
            <span>${escapeHtml(label)}</span>
            <strong>${escapeHtml(value)}</strong>
            <p>${escapeHtml(note)}</p>
        </article>
    `;
}

function coreMetric(label, value) {
    return `
        <article class="customer-core-metric">
            <span>${escapeHtml(label)}</span>
            <strong>${escapeHtml(typeof value === "string" ? beautify(value) : String(value))}</strong>
        </article>
    `;
}

function customerScore(item) {
    let score = 55;
    if (item.riskLevel === "LOW") {
        score += 10;
    }
    if (item.status === "ACTIVE") {
        score += 10;
    }
    return `${Math.min(score, 95)}/100`;
}

function buildCustomerAlerts(item, openDeals, tasks) {
    const alerts = [];
    if (item.riskLevel === "HIGH") {
        alerts.push({ label: "Risk", value: "High risk customer" });
    }
    const overdueTask = tasks.find(task => task.status !== "COMPLETED" && task.dueDate && new Date(task.dueDate) < startOfToday());
    if (overdueTask) {
        alerts.push({ label: "Task", value: `Overdue: ${overdueTask.title}` });
    }
    if (openDeals.length > 0) {
        alerts.push({ label: "Pipeline", value: `${openDeals.length} open opportunity(ies)` });
    }
    return alerts.length ? alerts : [{ label: "Status", value: "No immediate risk alerts" }];
}

function renderContactInsight(item) {
    return `
        <div class="insight-hero">
            <span class="eyebrow">Contact Profile</span>
            <h3>${escapeHtml(item.firstName)} ${escapeHtml(item.lastName)}</h3>
            <p>${escapeHtml(item.customerName)}${item.jobTitle ? ` • ${escapeHtml(item.jobTitle)}` : ""}</p>
        </div>
        ${detailGroup("Contact Details", [
            detailItem("Email", item.email || "Not captured"),
            detailItem("Phone", item.phone || "Not captured"),
            detailItem("Role", item.jobTitle || "Not captured"),
            detailItem("Customer", item.customerName || "Not linked")
        ])}
        ${detailGroup("Notes", [
            detailItem("Commentary", item.notes || "No notes recorded")
        ])}
    `;
}

function renderDealInsight(item) {
    return `
        <div class="insight-hero">
            <span class="eyebrow">Opportunity Overview</span>
            <h3>${escapeHtml(item.title)}</h3>
            <p>${escapeHtml(item.customerName)} • ${formatMoney(item.amount)}</p>
        </div>
        <div class="insight-grid">
            ${insightMetric("Stage", beautify(item.stage))}
            ${insightMetric("Value", formatMoney(item.amount))}
            ${insightMetric("Expected Close", item.expectedCloseDate || "TBD")}
            ${insightMetric("Linked Tasks", String(countLinkedTasks(item.id)))}
        </div>
        ${detailGroup("Notes", [
            detailItem("Opportunity Notes", item.notes || "No notes recorded")
        ])}
    `;
}

function renderProjectInsight(item) {
    const customerProjects = state.data.projects.filter(project => project.customerId === item.customerId).length;
    const maintenanceCount = 0;
    return `
        <div class="insight-hero">
            <span class="eyebrow">Project Overview</span>
            <h3>${escapeHtml(item.projectName)}</h3>
            <p>${escapeHtml(item.customerName)} • ${escapeHtml(item.market)}</p>
        </div>
        <div class="insight-grid">
            ${insightMetric("Contract Status", beautify(item.status))}
            ${insightMetric("License Amount", formatMoney(item.licenseAmount))}
            ${insightMetric("Implementation Amount", formatMoney(item.implementationAmount))}
            ${insightMetric("Tax Rate", `${Number(item.taxRate || 0)}%`)}
            ${insightMetric("Market", item.market)}
        </div>
        ${detailGroup("Project Detail", [
            detailItem("Customer", item.customerName || "Not linked"),
            detailItem("Market", item.market || "Not captured"),
            detailItem("Contract Status", beautify(item.status)),
            detailItem("License Amount", formatMoney(item.licenseAmount)),
            detailItem("Implementation Amount", formatMoney(item.implementationAmount)),
            detailItem("Tax Rate", `${Number(item.taxRate || 0)}%`),
            detailItem("Customer Project Count", String(customerProjects))
        ])}
        ${detailGroup("Maintenance Linkage", [
            detailItem("Annual Maintenance", maintenanceCount ? String(maintenanceCount) : "Managed from Annual Maintenance page")
        ])}
    `;
}

function renderToolbarExtras(items) {
    if (state.currentView !== "projects") {
        projectFilters.classList.add("hidden");
        projectFilters.innerHTML = "";
        return;
    }

    const customerOptions = state.data.customers
        .slice()
        .sort((a, b) => a.name.localeCompare(b.name))
        .map(customer => `<option value="${customer.id}" ${String(state.projectFilters.customerId) === String(customer.id) ? "selected" : ""}>${escapeHtml(customer.name)}</option>`)
        .join("");
    const marketOptions = [...new Set(state.data.projects.map(item => item.market).filter(Boolean))]
        .sort((a, b) => a.localeCompare(b))
        .map(market => `<option value="${escapeHtml(market)}" ${state.projectFilters.market === market ? "selected" : ""}>${escapeHtml(market)}</option>`)
        .join("");

    projectFilters.classList.remove("hidden");
    projectFilters.innerHTML = `
        <div class="project-filter-head">
            <div>
                <span class="eyebrow">Project Explorer</span>
                <strong>Customer-centered project book</strong>
            </div>
            <div class="project-filter-note">
                <span>${items.length} project(s) matched</span>
                <small>Grouped by customer relationship.</small>
            </div>
        </div>
        <div class="project-filter-grid">
            <label class="field">
                <span>Customer</span>
                <select name="customerId">
                    <option value="">All Customers</option>
                    ${customerOptions}
                </select>
            </label>
            <label class="field">
                <span>Market</span>
                <select name="market">
                    <option value="">All Markets</option>
                    ${marketOptions}
                </select>
            </label>
            <label class="field">
                <span>Status</span>
                <select name="status">
                    <option value="">All Statuses</option>
                    <option value="SIGNED_CONTRACT" ${state.projectFilters.status === "SIGNED_CONTRACT" ? "selected" : ""}>Signed Contract</option>
                    <option value="UNSIGNED_CONTRACT" ${state.projectFilters.status === "UNSIGNED_CONTRACT" ? "selected" : ""}>Unsigned Contract</option>
                </select>
            </label>
            <label class="field">
                <span>Keyword</span>
                <input value="${escapeHtml(state.search)}" disabled placeholder="Use the main search box above">
            </label>
        </div>
    `;
}

function renderProjectCustomerGroups(groups, selected) {
    return groups.map(group => `
        <section class="customer-project-group">
            <header class="customer-project-header">
                <div class="customer-project-identity">
                    <span class="eyebrow">Customer</span>
                    <h3>${escapeHtml(group.customerName)}</h3>
                    <p>${group.projects.length} active project record${group.projects.length === 1 ? "" : "s"} in this customer book.</p>
                </div>
                <div class="customer-project-summary">
                    <article class="customer-project-stat">
                        <span>Projects</span>
                        <strong>${group.projects.length}</strong>
                    </article>
                    <article class="customer-project-stat">
                        <span>Total Value</span>
                        <strong>${formatMoney(group.totalAmount)}</strong>
                    </article>
                </div>
            </header>
            ${renderProjectDirectoryTable(group.projects, selected)}
        </section>
    `).join("");
}

function renderProjectDirectoryTable(items, selected) {
    return `
        <div class="customer-contact-table-shell">
            <table class="contact-directory-table">
                <thead>
                    <tr>
                        <th>Project Name</th>
                        <th>Market</th>
                        <th>Contract Status</th>
                        <th>Customer</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    ${items.map(item => renderProjectDirectoryRow(item, selected && selected.id === item.id)).join("")}
                </tbody>
            </table>
        </div>
    `;
}

function renderProjectDirectoryRow(item, selected) {
    return `
        <tr class="${selected ? "selected" : ""}" data-select-id="${item.id}">
            <td data-label="Project Name"><strong>${escapeHtml(item.projectName)}</strong></td>
            <td data-label="Market">${escapeHtml(item.market || "Not captured")}</td>
            <td data-label="Contract Status">${escapeHtml(beautify(item.status || "UNSIGNED_CONTRACT"))}</td>
            <td data-label="Customer">${escapeHtml(item.customerName || "Unassigned")}</td>
            <td data-label="Actions">
                <div class="contact-table-actions">
                    <a class="ghost-button link-button" href="/annual-maintenance.html?customerId=${item.customerId}&projectId=${item.id}&source=projects">Maintenance</a>
                    <button class="ghost-button" type="button" data-edit-id="${item.id}">Edit</button>
                    <button class="danger-button" type="button" data-delete-id="${item.id}">Delete</button>
                </div>
            </td>
        </tr>
    `;
}

function renderProjectEmptyState() {
    return `
        <div class="empty-state">
            <h3>No projects found</h3>
            <p>Adjust the customer, market, or contract status filters, or create the first project for a customer.</p>
        </div>
    `;
}

function groupProjectsByCustomer(items) {
    const groups = new Map();
    items
        .slice()
        .sort((a, b) => a.customerName.localeCompare(b.customerName) || a.projectName.localeCompare(b.projectName))
        .forEach(item => {
            if (!groups.has(item.customerId)) {
                groups.set(item.customerId, {
                    customerId: item.customerId,
                    customerName: item.customerName,
                    projects: [],
                    totalAmount: 0
                });
            }
            const group = groups.get(item.customerId);
            group.projects.push(item);
            group.totalAmount += Number(item.licenseAmount || 0) + Number(item.implementationAmount || 0);
        });
    return Array.from(groups.values());
}

function projectMatchesFilters(item) {
    if (state.projectFilters.customerId && String(item.customerId) !== String(state.projectFilters.customerId)) {
        return false;
    }
    if (state.projectFilters.market && item.market !== state.projectFilters.market) {
        return false;
    }
    if (state.projectFilters.status && item.status !== state.projectFilters.status) {
        return false;
    }
    return true;
}

function renderTaskInsight(item) {
    return `
        <div class="insight-hero">
            <span class="eyebrow">Service Execution</span>
            <h3>${escapeHtml(item.title)}</h3>
            <p>${escapeHtml(item.customerName)}${item.dealTitle ? ` • ${escapeHtml(item.dealTitle)}` : ""}</p>
        </div>
        <div class="insight-grid">
            ${insightMetric("Status", beautify(item.status))}
            ${insightMetric("Priority", beautify(item.priority))}
            ${insightMetric("Due Date", item.dueDate || "Not set")}
            ${insightMetric("Age", relativeDate(item.dueDate))}
        </div>
        ${detailGroup("Description", [
            detailItem("Task Detail", item.description || "No description recorded")
        ])}
    `;
}

function renderActivityInsight(item) {
    return `
        <div class="insight-hero">
            <span class="eyebrow">Interaction Detail</span>
            <h3>${escapeHtml(item.subject)}</h3>
            <p>${escapeHtml(item.customerName)} • ${formatDateTime(item.activityDate)}</p>
        </div>
        <div class="insight-grid">
            ${insightMetric("Type", beautify(item.type))}
            ${insightMetric("Contact", item.contactName || "N/A")}
            ${insightMetric("Opportunity", item.dealTitle || "N/A")}
            ${insightMetric("Created By", item.createdBy || "System")}
        </div>
        ${detailGroup("Interaction Notes", [
            detailItem("Details", item.details || "No details recorded")
        ])}
    `;
}

function insightMetric(label, value) {
    return `
        <article class="insight-metric">
            <span>${escapeHtml(label)}</span>
            <strong>${escapeHtml(value)}</strong>
        </article>
    `;
}

function detailGroup(title, items) {
    return `
        <section class="detail-group">
            <h4>${escapeHtml(title)}</h4>
            <div class="detail-list">${items.join("")}</div>
        </section>
    `;
}

function detailItem(label, value) {
    return `
        <div class="detail-item">
            <span>${escapeHtml(label)}</span>
            <strong>${escapeHtml(value)}</strong>
        </div>
    `;
}

function openModal(id = null) {
    state.editingId = id;
    const config = entityConfig[state.currentView];
    const item = id == null ? null : (state.data[state.currentView] || []).find(entry => entry.id === id);

    document.getElementById("modal-label").textContent = id == null ? "Add" : "Edit";
    document.getElementById("modal-title").textContent = `${id == null ? "Add" : "Edit"} ${config.singular}`;
    modalForm.innerHTML = buildFormMarkup(config.fields, item);
    modalForm.onsubmit = submitModalForm;
    modal.classList.remove("hidden");
}

function closeModal() {
    state.editingId = null;
    modal.classList.add("hidden");
}

async function submitModalForm(event) {
    event.preventDefault();
    const config = entityConfig[state.currentView];
    const payload = formToPayload(new FormData(event.target), config.fields);
    const editing = state.editingId != null;

    try {
        await api(`${config.path}${editing ? `/${state.editingId}` : ""}`, {
            method: editing ? "PUT" : "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${state.token}`
            },
            body: JSON.stringify(payload)
        });
        closeModal();
        await refreshAll();
        appMessage.textContent = `${editing ? "Updated" : "Added"} ${config.singular.toLowerCase()} successfully.`;
    } catch (error) {
        handleAppError(error);
    }
}

async function removeRecord(id) {
    const config = entityConfig[state.currentView];
    const confirmed = window.confirm(`Delete this ${config.singular.toLowerCase()} record?`);
    if (!confirmed) {
        return;
    }

    try {
        await api(`${config.path}/${id}`, {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${state.token}` }
        });
        if (state.selectedId === id) {
            state.selectedId = null;
        }
        await refreshAll();
        appMessage.textContent = `${config.singular} deleted.`;
    } catch (error) {
        handleAppError(error);
    }
}

function buildFormMarkup(fields, item) {
    const inputs = fields.map(fieldConfig => {
        const value = item ? resolveFormValue(item, fieldConfig) : "";
        const input = renderField(fieldConfig, value);
        return `<label class="field ${fieldConfig.full ? "full" : ""}"><span>${fieldConfig.label}</span>${input}</label>`;
    }).join("");

    return `
        ${inputs}
        <div class="form-footer">
            <button class="ghost-button" type="button" id="cancel-form-button">Cancel</button>
            <button class="primary-button" type="submit">${state.editingId == null ? "Save" : "Update"}</button>
        </div>
    `;
}

function renderField(fieldConfig, value) {
    if (fieldConfig.type === "textarea") {
        return `<textarea name="${fieldConfig.name}" rows="4" ${fieldConfig.required ? "required" : ""}>${escapeHtml(value)}</textarea>`;
    }

    if (fieldConfig.type === "select") {
        const optionList = typeof fieldConfig.options === "function"
            ? fieldConfig.options()
            : (fieldConfig.options || []);
        const options = optionList.map(option => `
            <option value="${escapeHtml(option.value)}" ${String(value) === String(option.value) ? "selected" : ""}>${escapeHtml(option.label)}</option>
        `).join("");
        return `<select name="${fieldConfig.name}" ${fieldConfig.required ? "required" : ""}>${fieldConfig.required ? "" : `<option value="">None</option>`}${options}</select>`;
    }

    if (fieldConfig.type === "market-search") {
        return `
            <input
                name="${fieldConfig.name}"
                type="text"
                list="global-market-options"
                value="${escapeHtml(value)}"
                placeholder="Search Africa or Asia market"
                ${fieldConfig.required ? "required" : ""}
            >
        `;
    }

    return `<input name="${fieldConfig.name}" type="${fieldConfig.type}" value="${escapeHtml(value)}" ${fieldConfig.required ? "required" : ""}>`;
}

function resolveFormValue(item, fieldConfig) {
    const value = item[fieldConfig.name];
    if (value == null) {
        return "";
    }
    if (fieldConfig.type === "datetime-local") {
        return String(value).slice(0, 16);
    }
    return value;
}

function formToPayload(formData, fields) {
    const payload = {};
    fields.forEach(fieldConfig => {
        const raw = formData.get(fieldConfig.name);
        if (raw == null || raw === "") {
            return;
        }
        if (fieldConfig.kind === "relation") {
            payload[fieldConfig.name] = Number(raw);
            return;
        }
        if (fieldConfig.type === "number") {
            payload[fieldConfig.name] = Number(raw);
            return;
        }
        payload[fieldConfig.name] = raw;
    });
    return payload;
}

async function api(path, options = {}) {
    const headers = options.headers || {};
    if (state.token && !headers.Authorization) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(path, { ...options, headers });
    const contentType = response.headers.get("content-type") || "";
    const data = contentType.includes("application/json") ? await response.json() : null;

    if (!response.ok) {
        if (response.status === 401) {
            throw new Error("Unauthorized");
        }
        throw new Error(extractError(data));
    }
    return data;
}

function handleSearch(event) {
    state.search = event.target.value;
    ensureSelectedRecord();
    renderCurrentView();
}

function handleProjectFilterChange(event) {
    const target = event.target;
    if (!(target instanceof HTMLInputElement || target instanceof HTMLSelectElement)) {
        return;
    }
    state.projectFilters[target.name] = target.value;
    ensureSelectedRecord();
    renderCurrentView();
}

function handleAppError(error) {
    if (error.message === "Unauthorized") {
        logout();
        loginMessage.textContent = "Session expired. Please sign in again.";
        return;
    }
    appMessage.textContent = error.message;
}

function ensureSelectedRecord() {
    const items = filteredItems();
    if (items.length === 0) {
        state.selectedId = null;
        return;
    }
    if (!items.some(item => item.id === state.selectedId)) {
        state.selectedId = items[0].id;
    }
}

function resolveSelectedItem(items) {
    if (!items.length) {
        return null;
    }
    return items.find(item => item.id === state.selectedId) || items[0];
}

function countLinkedTasks(dealId) {
    return state.data.tasks.filter(task => task.dealId === dealId).length;
}

function renderGlobalMarketOptions() {
    if (!globalMarketOptions) {
        return;
    }
    globalMarketOptions.innerHTML = MARKET_OPTIONS
        .map(option => `<option value="${escapeHtml(option)}"></option>`)
        .join("");
}

function field(name, label, type = "text", required = false, options = null, full = false) {
    return {
        kind: "field",
        name,
        label,
        type,
        required,
        full,
        options: options ? options.map(item => typeof item === "string" ? ({ value: item, label: beautify(item) }) : item) : null
    };
}

function option(value, label) {
    return { value, label };
}

function relationField(name, label, source, required = false) {
    return {
        kind: "relation",
        name,
        label,
        type: "select",
        required,
        full: false,
        options: () => (state.data[source] || []).map(item => ({
            value: item.id,
            label: item.name || item.projectName || item.title || `${item.firstName} ${item.lastName}`
        }))
    };
}

document.addEventListener("click", event => {
    if (event.target && event.target.id === "cancel-form-button") {
        closeModal();
    }
    if (event.target && event.target.id === "cancel-deal-conversion-button") {
        closeDealConversionModal();
    }

    const customerTab = event.target.closest("[data-customer-tab]");
    if (customerTab) {
        state.customerTab = customerTab.dataset.customerTab;
        renderCurrentView();
    }
});

function extractError(data) {
    if (!data) {
        return "Request failed.";
    }
    if (Array.isArray(data.details) && data.details.length > 0) {
        return data.details.join(", ");
    }
    return data.error || "Request failed.";
}

function formatDateTime(value) {
    return value ? new Date(value).toLocaleString() : "No date";
}

function formatMoney(value) {
    if (value == null || value === "") {
        return "$0.00";
    }
    return new Intl.NumberFormat(undefined, {
        style: "currency",
        currency: "USD",
        maximumFractionDigits: 2
    }).format(Number(value));
}

function relativeDate(value) {
    if (!value) {
        return "No due date";
    }
    const due = new Date(value);
    const diffDays = Math.round((due - startOfToday()) / 86400000);
    if (diffDays === 0) {
        return "Due today";
    }
    if (diffDays > 0) {
        return `Due in ${diffDays} day${diffDays === 1 ? "" : "s"}`;
    }
    const past = Math.abs(diffDays);
    return `${past} day${past === 1 ? "" : "s"} overdue`;
}

function startOfToday() {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), now.getDate());
}

function isToday(value) {
    if (!value) {
        return false;
    }
    const date = new Date(value);
    const today = startOfToday();
    return date >= today && date < new Date(today.getTime() + 86400000);
}

function beautify(value) {
    return String(value || "")
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, char => char.toUpperCase());
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}
