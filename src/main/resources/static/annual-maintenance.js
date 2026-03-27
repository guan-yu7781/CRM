const query = new URLSearchParams(window.location.search);
const customerId = Number(query.get("customerId"));
const requestedProjectId = Number(query.get("projectId")) || null;
const source = query.get("source") || "customer360";
const token = localStorage.getItem("crmToken") || "";
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
const MARKET_SET = new Set(MARKET_OPTIONS);

const state = {
    customer: null,
    records: [],
    projects: [],
    availableProjects: [],
    targetProjectId: requestedProjectId,
    editingId: null,
    selectedProjectKey: null
};

const maintenanceList = document.getElementById("maintenance-list");
const maintenanceMessage = document.getElementById("maintenance-message");
const maintenanceOverview = document.getElementById("maintenance-overview");
const maintenanceBackLink = document.getElementById("maintenance-back-link");
const maintenanceModal = document.getElementById("maintenance-modal");
const maintenanceForm = document.getElementById("maintenance-form");
const feedbackModal = document.getElementById("maintenance-feedback-modal");
const feedbackCard = feedbackModal.querySelector(".feedback-card");
const feedbackLabel = document.getElementById("maintenance-feedback-label");
const feedbackTitle = document.getElementById("maintenance-feedback-title");
const feedbackMessage = document.getElementById("maintenance-feedback-message");

document.getElementById("maintenance-refresh-button").addEventListener("click", loadPage);
document.getElementById("maintenance-add-button").addEventListener("click", () => openModal());
document.getElementById("maintenance-close-button").addEventListener("click", closeModal);
document.getElementById("maintenance-feedback-close-button").addEventListener("click", closeFeedbackModal);
document.getElementById("maintenance-feedback-confirm-button").addEventListener("click", closeFeedbackModal);
document.querySelector("[data-maintenance-close]").addEventListener("click", closeModal);
document.querySelector("[data-feedback-close]").addEventListener("click", closeFeedbackModal);
maintenanceList.addEventListener("click", handleListClick);

bootstrap();

function bootstrap() {
    renderMarketOptions();
    configureBackLink();
    if (!token) {
        window.location.href = "/";
        return;
    }
    if (!customerId) {
        maintenanceMessage.textContent = "Customer id is missing.";
        return;
    }
    loadPage();
}

function configureBackLink() {
    if (source === "projects") {
        maintenanceBackLink.href = "/?view=projects";
        maintenanceBackLink.textContent = "Back to Projects";
        return;
    }
    if (source === "customers") {
        maintenanceBackLink.href = "/?view=customers";
        maintenanceBackLink.textContent = "Back to Customer Management";
        return;
    }
    maintenanceBackLink.href = `/customer-360.html?customerId=${customerId}`;
    maintenanceBackLink.textContent = "Back to Customer 360";
}

async function loadPage() {
    try {
        const [customer, records] = await Promise.all([
            api(`/api/customers/${customerId}`),
            api(`/api/annual-maintenance?customerId=${customerId}`)
        ]);
        const availableProjects = await api(`/api/projects?customerId=${customerId}`);
        state.customer = customer;
        state.records = records;
        state.availableProjects = availableProjects;
        state.projects = buildProjects(records, availableProjects);
        const preferredKey = state.targetProjectId ? String(state.targetProjectId) : state.selectedProjectKey;
        if (preferredKey && state.projects.some(item => item.key === preferredKey)) {
            state.selectedProjectKey = preferredKey;
        } else if (!state.projects.some(item => item.key === state.selectedProjectKey)) {
            state.selectedProjectKey = state.projects.length ? state.projects[0].key : null;
        }
        renderPage();
        maintenanceMessage.textContent = "";
    } catch (error) {
        maintenanceMessage.textContent = error.message;
    }
}

function renderPage() {
    const visibleProjects = getVisibleProjects();
    const selectedProject = visibleProjects.find(item => item.key === state.selectedProjectKey) || visibleProjects[0] || null;

    document.getElementById("maintenance-page-title").textContent = state.targetProjectId && selectedProject
        ? `${selectedProject.projectName} Maintenance`
        : `${state.customer.name} Annual Maintenance`;
    document.getElementById("maintenance-page-description").textContent = state.targetProjectId && selectedProject
        ? `Manage annual maintenance entries for ${selectedProject.projectName}. The latest end date is used as the comparison rule for status.`
        : `Manage maintenance by project for ${state.customer.name}. Each project can contain 1st Year, 2nd Year, 3rd Year and status is based on the latest end date across that project.`;
    document.getElementById("maintenance-total").textContent = String(visibleProjects.length);
    document.getElementById("maintenance-active").textContent = String(visibleProjects.filter(item => item.alertLevel === "ACTIVE").length);
    document.getElementById("maintenance-expired").textContent = String(visibleProjects.filter(item => item.alertLevel === "RED").length);
    document.getElementById("maintenance-customer-name").textContent = state.customer.name;

    if (state.targetProjectId) {
        maintenanceList.classList.add("hidden");
        maintenanceList.innerHTML = "";
    } else {
        maintenanceList.classList.remove("hidden");
        maintenanceList.innerHTML = visibleProjects.length
            ? visibleProjects
                .filter(item => item.key !== state.selectedProjectKey)
                .map(renderProject)
                .join("")
            : `<div class="empty-state"><h3>No maintenance projects</h3><p>Add the first annual maintenance record for this customer.</p></div>`;
    }

    renderDetail();
}

function getVisibleProjects() {
    if (!state.targetProjectId) {
        return state.projects;
    }
    return state.projects.filter(item => item.projectId === state.targetProjectId);
}

function renderProject(project) {
    const selected = project.key === state.selectedProjectKey;
    const years = project.records
        .map(item => formatMaintenanceYear(item.maintenanceYear))
        .join(", ");
    const metaItems = [
        project.market,
        years || null,
        `Latest End ${project.latestEndDate}`,
        formatMoney(project.totalAmount)
    ].filter(Boolean);
    return `
        <article class="record-row ${selected ? "selected" : ""} ${project.rowClass}" data-select-key="${escapeHtml(project.key)}">
            <div class="record-main">
                <div class="record-title-line">
                    <h3>${escapeHtml(project.projectName)}</h3>
                    <span class="status-pill ${project.statusClass}">${project.statusLabel}</span>
                </div>
                <div class="record-meta">
                    ${metaItems.map(item => `<span class="meta-pill">${escapeHtml(item)}</span>`).join("")}
                </div>
                <p>${escapeHtml(project.customerName)} maintenance project with ${project.records.length} annual record(s)</p>
            </div>
        </article>
    `;
}

function renderDetail() {
    const project = getVisibleProjects().find(item => item.key === state.selectedProjectKey);
    const title = document.getElementById("maintenance-detail-title");
    const content = document.getElementById("maintenance-detail-content");

    if (!project) {
        maintenanceOverview.innerHTML = "";
        title.textContent = "Select a project";
        content.innerHTML = `<div class="empty-insight"><p>Choose a maintenance project to view its yearly coverage timeline.</p></div>`;
        return;
    }

    maintenanceOverview.innerHTML = `
        <section class="maintenance-featured-project">
            ${renderProject(project)}
        </section>
        <section class="maintenance-priority ${project.statusClass}">
            <div class="maintenance-priority-copy">
                <span class="eyebrow">Warning Status</span>
                <h3>${escapeHtml(project.statusLabel)}</h3>
                <p>${escapeHtml(project.alertLevel === "ACTIVE" ? "Active coverage is in effect for the currently effective maintenance year(s)." : project.warningMessage)}</p>
            </div>
            <div class="maintenance-priority-badge">
                <span class="status-pill ${project.statusClass}">${project.statusLabel}</span>
            </div>
        </section>
        ${project.alertLevel !== "ACTIVE" ? `
        <section class="warning-banner ${project.warningClass}">
            <strong>${project.statusLabel}</strong>
            <p>${escapeHtml(project.warningMessage)}</p>
        </section>
        ` : ""}
        <div class="insight-hero ${project.heroClass}">
            <span class="eyebrow">Annual Maintenance Project</span>
            <h3>${escapeHtml(project.projectName)}</h3>
            <p>${escapeHtml(project.customerName)} • ${project.records.length} yearly record(s)</p>
        </div>
        <div class="insight-grid">
            ${metric("Market", project.market)}
            ${metric("Coverage Years", project.records.length)}
            ${metric("Latest End Date", project.latestEndDate)}
            ${metric("Total Amount", formatMoney(project.totalAmount))}
            ${metric("Project Status", project.statusLabel)}
            ${metric("Comparison Rule", "Use effective years only")}
        </div>
        <section class="detail-group">
            <h4>Project Summary</h4>
            <div class="detail-list">
                ${detail("Project Name", project.projectName)}
                ${detail("Market", project.market)}
                ${detail("Customer", project.customerName)}
                ${detail("Latest End Date", project.latestEndDate)}
                ${detail("Lifecycle", project.lifecycleText)}
            </div>
        </section>
    `;

    title.textContent = `${project.projectName} Timeline`;
    content.innerHTML = `
        <section class="detail-group detail-group-compact">
            <h4>Maintenance Timeline</h4>
            ${project.records.length ? `
            <div class="year-timeline">
                ${project.records.map(renderYearRecord).join("")}
            </div>
            ` : `<div class="empty-insight"><p>No annual maintenance entries have been created for this project yet.</p></div>`}
        </section>
        <div class="insight-actions">
            <button class="primary-button" type="button" data-add-year="${escapeHtml(project.projectName)}" data-add-market="${escapeHtml(project.market)}">Add New Year</button>
        </div>
    `;
}

function renderYearRecord(record) {
    return `
        <article class="timeline-record ${record.id === state.editingId ? "selected" : ""}">
            <div class="timeline-record-main">
                <div class="record-title-line">
                    <h3>${escapeHtml(formatMaintenanceYear(record.maintenanceYear))}</h3>
                    <div class="timeline-status-cluster">
                        <span class="status-pill ${renewStatusClass(record.renewStatus)}">${renewStatusLabel(record.renewStatus)}</span>
                        <span class="status-pill ${record.paymentStatus === "PAID" ? "status-active" : "status-pending"}">${record.paymentStatus === "PAID" ? "Paid" : "Not Paid"}</span>
                    </div>
                </div>
                <div class="record-meta">
                    <span class="meta-pill">${escapeHtml(formatMoney(record.amount))}</span>
                    <span class="meta-pill">${escapeHtml(record.startDate)}</span>
                    <span class="meta-pill">${escapeHtml(record.endDate)}</span>
                </div>
            </div>
            <div class="record-actions">
                <button class="ghost-button" type="button" data-edit-id="${record.id}">Edit</button>
            </div>
        </article>
    `;
}

function handleListClick(event) {
    const editButton = event.target.closest("[data-edit-id]");
    if (editButton) {
        openModal(Number(editButton.dataset.editId));
        return;
    }

    const row = event.target.closest("[data-select-id]");
    if (row) {
        state.selectedId = Number(row.dataset.selectId);
        renderPage();
    }

    const projectRow = event.target.closest("[data-select-key]");
    if (projectRow) {
        state.selectedProjectKey = projectRow.dataset.selectKey;
        renderPage();
    }
}

function openModal(id = null, defaults = {}) {
    state.editingId = id;
    const record = id == null ? null : state.records.find(item => item.id === id);
    const projectId = record ? record.projectId : resolveDefaultProjectId(defaults.projectName);
    const isEditing = id != null;
    document.getElementById("maintenance-modal-label").textContent = isEditing ? "Edit" : "Batch Add";
    document.getElementById("maintenance-modal-title").textContent = isEditing ? "Edit Annual Maintenance" : "Batch Add Annual Maintenance";
    maintenanceForm.innerHTML = isEditing ? `
        <section class="form-section form-section-full">
            <div class="form-section-header">
                <span class="eyebrow">Record Context</span>
                <h3>Edit Maintenance Entry</h3>
                <p>Update the selected project year, renewal status, commercial amount, coverage dates, and payment status.</p>
            </div>
            <div class="form-grid">
                ${renderProjectSelectField(projectId, true)}
            </div>
        </section>
        <section class="form-section form-section-full">
            <div class="form-section-header">
                <span class="eyebrow">Maintenance Details</span>
                <h3>Coverage and Billing</h3>
            </div>
            <div class="form-grid">
        ${renderMaintenanceYearField(record ? String(record.maintenanceYear) : "")}
        <label class="field"><span>Amount</span><input name="amount" type="number" min="0.01" step="0.01" value="${record ? escapeHtml(String(record.amount)) : ""}" required></label>
        <label class="field"><span>Start Date</span><input name="startDate" type="date" value="${record ? escapeHtml(record.startDate) : ""}" required></label>
        <label class="field"><span>End Date</span><input name="endDate" type="date" value="${record ? escapeHtml(record.endDate) : ""}" required></label>
        <label class="field"><span>Renew Status</span>
            <select name="renewStatus" required>
                <option value="">Select renew status</option>
                <option value="RENEWED" ${record && record.renewStatus === "RENEWED" ? "selected" : ""}>Renewed</option>
                <option value="NOT_RENEWED" ${record && record.renewStatus === "NOT_RENEWED" ? "selected" : ""}>Not Renewed</option>
            </select>
        </label>
        <label class="field"><span>Payment Status</span>
            <select name="paymentStatus" required>
                <option value="PAID" ${record && record.paymentStatus === "PAID" ? "selected" : ""}>Paid</option>
                <option value="NOT_PAID" ${record && record.paymentStatus === "NOT_PAID" ? "selected" : ""}>Not Paid</option>
            </select>
        </label>
            </div>
        </section>
        <div class="form-footer">
            <button class="ghost-button" type="button" id="maintenance-cancel-button">Cancel</button>
            <button class="primary-button" type="submit">${id == null ? "Save" : "Update"}</button>
        </div>
    ` : `
        ${renderProjectSelectField(projectId, true)}
        <section class="field full batch-section">
            <div class="batch-header">
                <div>
                    <span>Maintenance Entries</span>
                    <small>Add 1st Year, 2nd Year, 3rd Year for the selected project in one submission.</small>
                </div>
                <button class="ghost-button" type="button" id="maintenance-add-row-button">Add Year</button>
            </div>
            <div id="maintenance-batch-rows" class="batch-rows"></div>
        </section>
        <div class="form-footer">
            <button class="ghost-button" type="button" id="maintenance-cancel-button">Cancel</button>
            <button class="primary-button" type="submit">Save All</button>
        </div>
    `;

    if (!isEditing) {
        renderBatchRows();
    }
    maintenanceForm.onsubmit = submitForm;
    maintenanceModal.classList.remove("hidden");
}

function closeModal() {
    state.editingId = null;
    maintenanceModal.classList.add("hidden");
}

function showFeedbackDialog(title, message, variant = "error") {
    feedbackCard.classList.remove("feedback-error", "feedback-warning", "feedback-success");
    feedbackCard.classList.add(`feedback-${variant}`);
    feedbackLabel.textContent = variant === "warning" ? "Validation Warning" : variant === "success" ? "Operation Status" : "System Error";
    feedbackTitle.textContent = title;
    feedbackMessage.textContent = message;
    feedbackModal.classList.remove("hidden");
}

function closeFeedbackModal() {
    feedbackModal.classList.add("hidden");
}

async function submitForm(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const isEditing = state.editingId != null;
    const editingId = state.editingId;
    const projectId = Number(formData.get("projectId"));
    const selectedProject = state.availableProjects.find(item => item.id === projectId);
    if (!selectedProject) {
        throw new Error("Please select a project.");
    }
    const payload = isEditing
        ? {
            projectId,
            maintenanceYear: Number(formData.get("maintenanceYear")),
            amount: Number(formData.get("amount")),
            startDate: formData.get("startDate"),
            endDate: formData.get("endDate"),
            renewStatus: formData.get("renewStatus"),
            paymentStatus: formData.get("paymentStatus"),
            customerId
        }
        : collectBatchPayload(projectId);

    try {
        maintenanceMessage.textContent = isEditing ? "Updating annual maintenance record..." : "Saving annual maintenance records...";
        await api(`/api/annual-maintenance${isEditing ? `/${editingId}` : "/batch"}`, {
            method: isEditing ? "PUT" : "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        state.selectedProjectKey = String(selectedProject.id);
        closeModal();
        await loadPage();
        maintenanceMessage.textContent = isEditing ? "Annual maintenance record updated." : "Annual maintenance records added.";
    } catch (error) {
        maintenanceMessage.textContent = error.message;
        showFeedbackDialog(
            "Annual Maintenance Error",
            `Annual Maintenance ${isEditing ? "update" : "save"} failed.\n\n${error.message}`,
            "error"
        );
    }
}

document.addEventListener("click", event => {
    if (event.target && event.target.id === "maintenance-cancel-button") {
        closeModal();
    }
    if (event.target && event.target.id === "maintenance-add-row-button") {
        appendBatchRow();
    }
    const removeRow = event.target.closest("[data-remove-batch-row]");
    if (removeRow) {
        removeBatchRow(removeRow.dataset.removeBatchRow);
    }
    const detailEdit = event.target.closest("#maintenance-detail-content [data-edit-id]");
    if (detailEdit) {
        openModal(Number(detailEdit.dataset.editId));
    }
    const addYear = event.target.closest("#maintenance-detail-content [data-add-year]");
    if (addYear) {
        openModal(null, {
            projectName: addYear.dataset.addYear
        });
    }
});

function renderBatchRows() {
    const container = document.getElementById("maintenance-batch-rows");
    container.innerHTML = "";
    appendBatchRow(1);
    refreshBatchRowActions();
}

function appendBatchRow(defaultYear = "") {
    const container = document.getElementById("maintenance-batch-rows");
    if (!container) {
        return;
    }
    const rowId = String(Date.now() + Math.random());
    const row = document.createElement("article");
    row.className = "batch-row";
    row.dataset.batchRow = rowId;
    row.innerHTML = `
        ${renderMaintenanceYearField(String(defaultYear || ""))}
        <label class="field"><span>Amount</span><input name="amount" type="number" min="0.01" step="0.01" required></label>
        <label class="field"><span>Start Date</span><input name="startDate" type="date" required></label>
        <label class="field"><span>End Date</span><input name="endDate" type="date" required></label>
        <label class="field"><span>Renew Status</span>
            <select name="renewStatus" required>
                <option value="RENEWED">Renewed</option>
                <option value="NOT_RENEWED">Not Renewed</option>
            </select>
        </label>
        <label class="field"><span>Payment Status</span>
            <select name="paymentStatus" required>
                <option value="PAID">Paid</option>
                <option value="NOT_PAID">Not Paid</option>
            </select>
        </label>
        <div class="batch-row-actions">
            <button class="ghost-button" type="button" data-remove-batch-row="${rowId}">Remove</button>
        </div>
    `;
    container.appendChild(row);
    refreshBatchRowActions();
}

function removeBatchRow(rowId) {
    const rows = document.querySelectorAll("[data-batch-row]");
    if (rows.length <= 1) {
        showFeedbackDialog("Cannot Remove Entry", "At least one maintenance entry is required.", "warning");
        return;
    }
    document.querySelector(`[data-batch-row="${rowId}"]`)?.remove();
    refreshBatchRowActions();
}

function collectBatchPayload(projectId) {
    const rows = Array.from(document.querySelectorAll("[data-batch-row]"));
    if (!rows.length) {
        throw new Error("At least one yearly record is required.");
    }

    return rows.map(row => ({
        projectId,
        maintenanceYear: Number(row.querySelector('[name="maintenanceYear"]').value),
        amount: Number(row.querySelector('[name="amount"]').value),
        startDate: row.querySelector('[name="startDate"]').value,
        endDate: row.querySelector('[name="endDate"]').value,
        renewStatus: row.querySelector('[name="renewStatus"]').value,
        paymentStatus: row.querySelector('[name="paymentStatus"]').value,
        customerId
    }));
}

function refreshBatchRowActions() {
    const rows = Array.from(document.querySelectorAll("[data-batch-row]"));
    rows.forEach(row => {
        const removeButton = row.querySelector("[data-remove-batch-row]");
        if (!removeButton) {
            return;
        }
        const disabled = rows.length <= 1;
        removeButton.disabled = disabled;
        removeButton.textContent = disabled ? "Remove Disabled" : "Remove";
    });
}

function renderMaintenanceYearField(value) {
    const options = Array.from({ length: 10 }, (_, index) => {
        const year = String(index + 1);
        return `<option value="${year}" ${String(value) === year ? "selected" : ""}>${escapeHtml(formatMaintenanceYear(index + 1))}</option>`;
    }).join("");

    return `
        <label class="field">
            <span>Maintenance Year</span>
            <select name="maintenanceYear" required>
                <option value="">Select year</option>
                ${options}
            </select>
        </label>
    `;
}

function formatMaintenanceYear(value) {
    const year = Number(value);
    if (!Number.isFinite(year) || year <= 0) {
        return "Unknown Year";
    }

    const mod10 = year % 10;
    const mod100 = year % 100;
    let suffix = "th";
    if (mod10 === 1 && mod100 !== 11) {
        suffix = "st";
    } else if (mod10 === 2 && mod100 !== 12) {
        suffix = "nd";
    } else if (mod10 === 3 && mod100 !== 13) {
        suffix = "rd";
    }
    return `${year}${suffix} Year`;
}

function renewStatusLabel(value) {
    if (value === "RENEWED") {
        return "Renewed";
    }
    if (value === "NOT_RENEWED") {
        return "Not Renewed";
    }
    return "Not Set";
}

function renewStatusClass(value) {
    if (value === "RENEWED") {
        return "status-active";
    }
    return "status-pending";
}

function renderMarketOptions() {
    const datalist = document.getElementById("annual-maintenance-market-options");
    if (!datalist) {
        return;
    }
    datalist.innerHTML = MARKET_OPTIONS
        .map(option => `<option value="${escapeHtml(option)}"></option>`)
        .join("");
}

function renderMarketField(value) {
    return `
        <label class="field">
            <span>Market</span>
            <input name="market" list="annual-maintenance-market-options" value="${escapeHtml(value)}" placeholder="Search Africa or Asia market" required>
            <small class="field-help">Select from UN-recognized African and Asian markets.</small>
        </label>
    `;
}

function renderProjectSelectField(selectedProjectId, full = false) {
    const locked = state.targetProjectId != null;
    const options = state.availableProjects
        .map(project => `<option value="${project.id}" ${Number(selectedProjectId) === project.id ? "selected" : ""}>${escapeHtml(project.projectName)} • ${escapeHtml(project.market)} • ${escapeHtml(project.customerName)}</option>`)
        .join("");
    return `
        <label class="field ${full ? "full" : ""}">
            <span>Project</span>
            ${locked ? `<input type="hidden" name="projectId" value="${escapeHtml(String(selectedProjectId || state.targetProjectId || ""))}">` : ""}
            <select name="projectId" ${locked ? "disabled" : ""} required>
                <option value="">Select project</option>
                ${options}
            </select>
            <small class="field-help">${locked ? "This page is scoped to the selected project from Project Management." : "Annual Maintenance is linked to an existing project. Create the project first in the Projects module if needed."}</small>
        </label>
    `;
}

function resolveDefaultProjectId(projectName) {
    if (state.targetProjectId) {
        return state.targetProjectId;
    }
    if (!projectName) {
        return "";
    }
    const match = state.availableProjects.find(item => item.projectName === projectName);
    return match ? match.id : "";
}

function buildProjects(records, availableProjects) {
    const map = new Map();

    records
        .slice()
        .sort((a, b) => a.maintenanceYear - b.maintenanceYear || a.id - b.id)
        .forEach(record => {
            const key = String(record.projectId ?? `${record.projectName}::${record.market}`);
            if (!map.has(key)) {
                map.set(key, {
                    key,
                    projectId: record.projectId,
                    projectName: record.projectName,
                    market: record.market,
                    customerName: record.customerName,
                    records: []
                });
            }
            const project = map.get(key);
            const existingIndex = project.records.findIndex(item => item.maintenanceYear === record.maintenanceYear);
            if (existingIndex === -1) {
                project.records.push(record);
                return;
            }
            const existing = project.records[existingIndex];
            const existingUpdatedAt = existing.updatedAt ? new Date(existing.updatedAt).getTime() : 0;
            const nextUpdatedAt = record.updatedAt ? new Date(record.updatedAt).getTime() : 0;
            if (nextUpdatedAt > existingUpdatedAt || (nextUpdatedAt === existingUpdatedAt && record.id > existing.id)) {
                project.records[existingIndex] = record;
            }
        });

    const projects = Array.from(map.values())
        .map(project => {
            if (!project.records.length) {
                return {
                    ...project,
                    latestEndDate: "Not available",
                    expired: false,
                    totalAmount: 0,
                    alertLevel: "NONE",
                    statusLabel: "No Records",
                    statusClass: "status-pending",
                    rowClass: "",
                    heroClass: "",
                    warningClass: "",
                    warningMessage: "No annual maintenance entries have been created for this project yet.",
                    lifecycleText: "Maintenance setup pending for this project"
                };
            }
            project.records.sort((a, b) => a.maintenanceYear - b.maintenanceYear || a.id - b.id);
            const latestRecord = project.records.reduce((latest, current) => {
                if (!latest) {
                    return current;
                }
                return new Date(current.endDate) > new Date(latest.endDate) ? current : latest;
            }, null);
            const totalAmount = project.records.reduce((sum, current) => sum + Number(current.amount || 0), 0);
            const alert = buildAlert(project.records, latestRecord.endDate);
            return {
                ...project,
                latestEndDate: latestRecord.endDate,
                expired: alert.level === "RED",
                totalAmount,
                alertLevel: alert.level,
                statusLabel: alert.label,
                statusClass: alert.statusClass,
                rowClass: alert.rowClass,
                heroClass: alert.heroClass,
                warningClass: alert.warningClass,
                warningMessage: alert.message,
                lifecycleText: alert.lifecycleText
            };
        })
        .sort((a, b) => {
            if (a.alertLevel !== b.alertLevel) {
                return alertPriority(a.alertLevel) - alertPriority(b.alertLevel);
            }
            return a.projectName.localeCompare(b.projectName);
        });

    if (state.targetProjectId && availableProjects) {
        const key = String(state.targetProjectId);
        const exists = projects.some(item => item.key === key);
        if (!exists) {
            const project = availableProjects.find(item => item.id === state.targetProjectId);
            if (project) {
                projects.unshift({
                    key,
                    projectId: project.id,
                    projectName: project.projectName,
                    market: project.market,
                    customerName: project.customerName,
                    records: [],
                    latestEndDate: "Not available",
                    expired: false,
                    totalAmount: 0,
                    alertLevel: "NONE",
                    statusLabel: "No Records",
                    statusClass: "status-pending",
                    rowClass: "",
                    heroClass: "",
                    warningClass: "",
                    warningMessage: "No annual maintenance entries have been created for this project yet.",
                    lifecycleText: "Maintenance setup pending for this project"
                });
            }
        }
    }

    return projects;
}

function buildAlert(records, endDate) {
    const effectiveItems = records
        .filter(record => record.startDate)
        .filter(record => {
            const start = new Date(record.startDate);
            start.setHours(0, 0, 0, 0);
            return start.getTime() <= startOfToday().getTime();
        });

    if (effectiveItems.length === 0) {
        return {
            level: "NONE",
            label: "Pending Activation",
            statusClass: "status-pending",
            rowClass: "",
            heroClass: "",
            warningClass: "",
            message: "No maintenance year is effective yet. The configured records are scheduled for future activation.",
            lifecycleText: "Maintenance is scheduled but not yet effective based on start date"
        };
    }

    const renewItems = effectiveItems
        .filter(record => record.renewStatus !== "RENEWED")
        .sort((left, right) => new Date(left.startDate) - new Date(right.startDate));

    if (renewItems.length > 0) {
        const firstPendingRenewal = renewItems[0];
        const impactedYears = renewItems.map(item => formatMaintenanceYear(item.maintenanceYear)).join(", ");
        return {
            level: "ORANGE",
            label: "Renewal Warning",
            statusClass: "status-orange",
            rowClass: "record-orange",
            heroClass: "hero-orange",
            warningClass: "warning-orange",
            message: `Renewal action required. ${impactedYears} is already effective but still marked Not Renewed.`,
            lifecycleText: `Renewal warning because effective maintenance from ${firstPendingRenewal.startDate} is still Not Renewed`
        };
    }

    const collectionItems = effectiveItems
        .filter(record => record.paymentStatus === "NOT_PAID")
        .sort((left, right) => new Date(left.startDate) - new Date(right.startDate));

    if (collectionItems.length > 0) {
        const firstDue = collectionItems[0];
        const impactedYears = collectionItems.map(item => formatMaintenanceYear(item.maintenanceYear)).join(", ");
        return {
            level: "RED",
            label: "Payment Warning",
            statusClass: "status-expired",
            rowClass: "record-expired",
            heroClass: "hero-expired",
            warningClass: "warning-red",
            message: `Collection action required. ${impactedYears} is renewed and effective, but still marked Not Paid. Please contact the customer for payment collection.`,
            lifecycleText: `Payment collection required from ${firstDue.startDate} because started maintenance is still Not Paid`
        };
    }

    const latestEffectiveRecord = effectiveItems.reduce((latest, current) => {
        if (!latest) {
            return current;
        }
        return new Date(current.endDate) > new Date(latest.endDate) ? current : latest;
    }, null);
    const end = new Date((latestEffectiveRecord && latestEffectiveRecord.endDate) || endDate);
    end.setHours(0, 0, 0, 0);
    const daysRemaining = Math.ceil((end.getTime() - startOfToday().getTime()) / 86400000);

    if (daysRemaining < 0) {
        return {
            level: "RED",
            label: "Renewal Warning",
            statusClass: "status-expired",
            rowClass: "record-expired",
            heroClass: "hero-expired",
            warningClass: "warning-red",
            message: `Renewal action required. The current effective maintenance coverage ended on ${latestEffectiveRecord.endDate}.`,
            lifecycleText: `Renewal warning because the effective maintenance year ended on ${latestEffectiveRecord.endDate}`
        };
    }

    if (daysRemaining <= 7) {
        return {
            level: "ORANGE",
            label: "Renewal Warning",
            statusClass: "status-orange",
            rowClass: "record-orange",
            heroClass: "hero-orange",
            warningClass: "warning-orange",
            message: `Renewal warning. The current effective maintenance coverage will end within 7 days on ${latestEffectiveRecord.endDate}.`,
            lifecycleText: `Renewal warning because the effective maintenance year ends within 7 days on ${latestEffectiveRecord.endDate}`
        };
    }

    if (daysRemaining <= 30) {
        return {
            level: "YELLOW",
            label: "Renewal Warning",
            statusClass: "status-yellow",
            rowClass: "record-yellow",
            heroClass: "hero-yellow",
            warningClass: "warning-yellow",
            message: `Renewal warning. The current effective maintenance coverage will end within 30 days on ${latestEffectiveRecord.endDate}.`,
            lifecycleText: `Renewal warning because the effective maintenance year ends within 30 days on ${latestEffectiveRecord.endDate}`
        };
    }

    return {
        level: "ACTIVE",
        label: "Active Coverage",
        statusClass: "status-active",
        rowClass: "",
        heroClass: "",
        warningClass: "",
        message: "",
        lifecycleText: `Active coverage is in effect through ${latestEffectiveRecord.endDate}`
    };
}

function alertPriority(level) {
    return {
        RED: 0,
        ORANGE: 1,
        YELLOW: 2,
        ACTIVE: 3,
        NONE: 4
    }[level] ?? 4;
}

function startOfToday() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return today;
}

async function api(path, options = {}) {
    const headers = options.headers || {};
    headers.Authorization = `Bearer ${token}`;

    const response = await fetch(path, { ...options, headers });
    const contentType = response.headers.get("content-type") || "";
    const data = contentType.includes("application/json") ? await response.json() : null;
    if (!response.ok) {
        throw new Error(extractError(data));
    }
    return data;
}

function extractError(data) {
    if (!data) {
        return "Request failed.";
    }
    if (Array.isArray(data.details) && data.details.length > 0) {
        return data.details.join(", ");
    }
    return data.error || "Request failed.";
}

function metric(label, value) {
    return `<article class="insight-metric"><span>${escapeHtml(label)}</span><strong>${escapeHtml(String(value))}</strong></article>`;
}

function detail(label, value) {
    return `<div class="detail-item"><span>${escapeHtml(label)}</span><strong>${escapeHtml(String(value))}</strong></div>`;
}

function formatMoney(value) {
    return new Intl.NumberFormat(undefined, {
        style: "currency",
        currency: "USD",
        maximumFractionDigits: 2
    }).format(Number(value || 0));
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}
