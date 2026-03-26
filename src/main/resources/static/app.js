const entityConfig = {
    customers: {
        label: "Customer 360",
        title: "Banking Customer Master",
        description: "Manage CIF, KYC, onboarding, risk rating, and relationship ownership in one banking workspace.",
        singular: "Customer",
        path: "/api/customers",
        fields: [
            field("name", "Customer Name", "text", true),
            field("customerType", "Customer Type", "select", false, [
                option("COMMERCIAL_BANK", "商业银行客户"),
                option("PAYMENT_CUSTOMER", "支付客户"),
                option("MICROFINANCE_BANK", "Microfinance Bank"),
                option("SACCO", "SACCO")
            ]),
            field("cifNumber", "CIF Number", "text", true),
            field("email", "Email", "email", true),
            field("phone", "Phone"),
            field("company", "Legal Entity / Company"),
            field("segment", "Segment", "select", false, ["RETAIL", "SME", "CORPORATE", "WEALTH"]),
            field("status", "Lifecycle Status", "select", false, ["LEAD", "ACTIVE", "INACTIVE"]),
            field("kycStatus", "KYC Status", "select", false, ["PENDING", "IN_REVIEW", "VERIFIED", "EXPIRED", "REJECTED"]),
            field("riskLevel", "Risk Level", "select", false, ["LOW", "MEDIUM", "HIGH"]),
            field("preferredChannel", "Preferred Channel", "select", false, ["MOBILE_APP", "INTERNET_BANKING", "BRANCH", "CALL_CENTER", "RELATIONSHIP_MANAGER", "EMAIL"]),
            field("onboardingStage", "Onboarding Stage", "select", false, ["PROSPECT", "DOCUMENT_COLLECTION", "COMPLIANCE_REVIEW", "APPROVED", "ACTIVE"]),
            field("residencyCountry", "Residency Country"),
            field("relationshipManager", "Relationship Manager"),
            field("notes", "Relationship Notes", "textarea", false, null, true)
        ],
        getTitle: item => item.name,
        getDescription: item => `${item.cifNumber} • ${beautify(item.segment)}${item.relationshipManager ? ` • RM ${item.relationshipManager}` : ""}`,
        getMeta: item => [item.customerType, item.kycStatus, item.riskLevel]
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

const state = {
    token: localStorage.getItem("crmToken") || "",
    username: localStorage.getItem("crmUsername") || "",
    currentView: "customers",
    search: "",
    editingId: null,
    selectedId: null,
    data: {
        customers: [],
        contacts: [],
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
const recordsList = document.getElementById("records-list");

document.getElementById("login-form").addEventListener("submit", handleLogin);
document.getElementById("logout-button").addEventListener("click", logout);
document.getElementById("refresh-button").addEventListener("click", refreshAll);
document.getElementById("add-record-button").addEventListener("click", () => openModal());
document.getElementById("search-input").addEventListener("input", handleSearch);
document.getElementById("close-modal-button").addEventListener("click", closeModal);
document.querySelector(".modal-backdrop").addEventListener("click", closeModal);
document.querySelectorAll(".menu-item").forEach(button => {
    button.addEventListener("click", () => switchView(button.dataset.view));
});
recordsList.addEventListener("click", handleRecordListClick);

bootstrap();

function bootstrap() {
    if (state.token) {
        showApp();
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
    document.getElementById("search-input").value = "";
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
        const [customers, contacts, deals, tasks, activities] = await Promise.all([
            api("/api/customers"),
            api("/api/contacts"),
            api("/api/deals"),
            api("/api/tasks"),
            api("/api/activities")
        ]);

        state.data.customers = customers;
        state.data.contacts = contacts;
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
    document.getElementById("record-count").textContent = String(items.length);
    document.getElementById("records-list").innerHTML = items.length
        ? items.map(item => renderRecord(state.currentView, item, selected && selected.id === item.id)).join("")
        : renderEmptyState();

    renderSummaryCards(state.currentView);
    renderInsightPanel(state.currentView, selected);
}

function filteredItems() {
    const items = state.data[state.currentView] || [];
    const keyword = state.search.trim().toLowerCase();
    if (!keyword) {
        return items;
    }

    return items.filter(item => JSON.stringify(item).toLowerCase().includes(keyword));
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
            <div class="record-actions">
                <button class="ghost-button" type="button" data-edit-id="${item.id}">Edit</button>
                <button class="danger-button" type="button" data-delete-id="${item.id}">Delete</button>
            </div>
        </article>
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

function handleRecordListClick(event) {
    const editButton = event.target.closest("[data-edit-id]");
    if (editButton) {
        openModal(Number(editButton.dataset.editId));
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
        renderCurrentView();
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
        const verified = customers.filter(item => item.kycStatus === "VERIFIED").length;
        const highRisk = customers.filter(item => item.riskLevel === "HIGH").length;
        const active = customers.filter(item => item.status === "ACTIVE").length;
        return [
            { label: "Customers", value: String(customers.length), note: "Total customer master records" },
            { label: "KYC Verified", value: String(verified), note: "Customers ready for servicing" },
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
        panel.innerHTML = renderCustomerInsight(item);
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

    if (view === "tasks") {
        panel.innerHTML = renderTaskInsight(item);
        return;
    }

    panel.innerHTML = renderActivityInsight(item);
}

function renderCustomerInsight(item) {
    const contacts = state.data.contacts.filter(contact => contact.customerId === item.id).length;
    const deals = state.data.deals.filter(deal => deal.customerId === item.id).length;
    const tasks = state.data.tasks.filter(task => task.customerId === item.id).length;
    const activities = state.data.activities.filter(activity => activity.customerId === item.id).length;

    return `
        <div class="insight-hero">
            <span class="eyebrow">${escapeHtml(beautify(item.segment))} Customer</span>
            <h3>${escapeHtml(item.name)}</h3>
            <p>${escapeHtml(item.cifNumber)} • ${escapeHtml(beautify(item.customerType))} • ${escapeHtml(beautify(item.status))}</p>
        </div>
        <div class="insight-grid">
            ${insightMetric("KYC", beautify(item.kycStatus))}
            ${insightMetric("Risk", beautify(item.riskLevel))}
            ${insightMetric("Stage", beautify(item.onboardingStage))}
            ${insightMetric("Channel", beautify(item.preferredChannel))}
        </div>
        ${detailGroup("Relationship", [
            detailItem("Relationship Manager", item.relationshipManager || "Unassigned"),
            detailItem("Residency", item.residencyCountry || "Not captured"),
            detailItem("Email", item.email || "Not captured"),
            detailItem("Phone", item.phone || "Not captured")
        ])}
        ${detailGroup("Portfolio Context", [
            detailItem("Contacts", String(contacts)),
            detailItem("Opportunities", String(deals)),
            detailItem("Service Tasks", String(tasks)),
            detailItem("Interactions", String(activities))
        ])}
        ${detailGroup("Relationship Notes", [
            detailItem("Notes", item.notes || "No notes recorded")
        ])}
        <div class="insight-actions">
            <a class="primary-button link-button" href="/annual-maintenance.html?customerId=${item.id}">Open Annual Maintenance</a>
        </div>
    `;
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
            label: item.name || item.title || `${item.firstName} ${item.lastName}`
        }))
    };
}

document.addEventListener("click", event => {
    if (event.target && event.target.id === "cancel-form-button") {
        closeModal();
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
