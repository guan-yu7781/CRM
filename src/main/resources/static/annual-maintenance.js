const query = new URLSearchParams(window.location.search);
const customerId = Number(query.get("customerId"));
const token = localStorage.getItem("crmToken") || "";

const state = {
    customer: null,
    records: [],
    editingId: null,
    selectedId: null
};

const maintenanceList = document.getElementById("maintenance-list");
const maintenanceMessage = document.getElementById("maintenance-message");
const maintenanceModal = document.getElementById("maintenance-modal");
const maintenanceForm = document.getElementById("maintenance-form");

document.getElementById("maintenance-refresh-button").addEventListener("click", loadPage);
document.getElementById("maintenance-add-button").addEventListener("click", () => openModal());
document.getElementById("maintenance-close-button").addEventListener("click", closeModal);
document.querySelector("[data-maintenance-close]").addEventListener("click", closeModal);
maintenanceList.addEventListener("click", handleListClick);

bootstrap();

function bootstrap() {
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

async function loadPage() {
    try {
        const [customer, records] = await Promise.all([
            api(`/api/customers/${customerId}`),
            api(`/api/annual-maintenance?customerId=${customerId}`)
        ]);
        state.customer = customer;
        state.records = records;
        if (!state.records.some(item => item.id === state.selectedId)) {
            state.selectedId = state.records.length ? state.records[0].id : null;
        }
        renderPage();
        maintenanceMessage.textContent = "";
    } catch (error) {
        maintenanceMessage.textContent = error.message;
    }
}

function renderPage() {
    document.getElementById("maintenance-page-title").textContent = `${state.customer.name} Annual Maintenance`;
    document.getElementById("maintenance-page-description").textContent = `Manage annual maintenance records for ${state.customer.name}. Expired records are highlighted in red when end date is before today.`;
    document.getElementById("maintenance-total").textContent = String(state.records.length);
    document.getElementById("maintenance-active").textContent = String(state.records.filter(item => !item.expired).length);
    document.getElementById("maintenance-expired").textContent = String(state.records.filter(item => item.expired).length);
    document.getElementById("maintenance-customer-name").textContent = state.customer.name;

    maintenanceList.innerHTML = state.records.length
        ? state.records.map(renderRecord).join("")
        : `<div class="empty-state"><h3>No maintenance projects</h3><p>Add the first annual maintenance record for this customer.</p></div>`;

    renderDetail();
}

function renderRecord(item) {
    const selected = item.id === state.selectedId;
    return `
        <article class="record-row ${selected ? "selected" : ""} ${item.expired ? "record-expired" : ""}" data-select-id="${item.id}">
            <div class="record-main">
                <div class="record-title-line">
                    <h3>${escapeHtml(item.projectName)}</h3>
                    <span class="status-pill ${item.expired ? "status-expired" : "status-active"}">${item.expired ? "Expired" : "Active"}</span>
                </div>
                <div class="record-meta">
                    <span class="meta-pill">${escapeHtml(item.market)}</span>
                    <span class="meta-pill">Year ${escapeHtml(String(item.maintenanceYear))}</span>
                    <span class="meta-pill">${escapeHtml(item.paymentStatus === "PAID" ? "Paid" : "Not Paid")}</span>
                    <span class="meta-pill">${escapeHtml(formatMoney(item.amount))}</span>
                    <span class="meta-pill">${escapeHtml(item.startDate)}</span>
                    <span class="meta-pill">${escapeHtml(item.endDate)}</span>
                </div>
                <p>${escapeHtml(item.customerName)} annual maintenance record</p>
            </div>
            <div class="record-actions">
                <button class="ghost-button" type="button" data-edit-id="${item.id}">Edit</button>
            </div>
        </article>
    `;
}

function renderDetail() {
    const record = state.records.find(item => item.id === state.selectedId);
    const title = document.getElementById("maintenance-detail-title");
    const content = document.getElementById("maintenance-detail-content");

    if (!record) {
        title.textContent = "Select a project";
        content.innerHTML = `<div class="empty-insight"><p>Choose a maintenance project to view its coverage year, start date, end date, and status.</p></div>`;
        return;
    }

    title.textContent = record.projectName;
    content.innerHTML = `
        <div class="insight-hero ${record.expired ? "hero-expired" : ""}">
            <span class="eyebrow">Annual Maintenance Record</span>
            <h3>${escapeHtml(record.projectName)}</h3>
            <p>${escapeHtml(record.customerName)} • Year ${escapeHtml(String(record.maintenanceYear))}</p>
        </div>
        <div class="insight-grid">
            ${metric("Project Year", `Year ${record.maintenanceYear}`)}
            ${metric("Market", record.market)}
            ${metric("Amount", formatMoney(record.amount))}
            ${metric("Payment", record.paymentStatus === "PAID" ? "Paid" : "Not Paid")}
            ${metric("Start Date", record.startDate)}
            ${metric("End Date", record.endDate)}
            ${metric("Status", record.expired ? "Expired" : "Active")}
        </div>
        <section class="detail-group">
            <h4>Record Summary</h4>
            <div class="detail-list">
                ${detail("Project Name", record.projectName)}
                ${detail("Market", record.market)}
                ${detail("Amount", formatMoney(record.amount))}
                ${detail("Payment Status", record.paymentStatus === "PAID" ? "Paid" : "Not Paid")}
                ${detail("Coverage Window", `${record.startDate} to ${record.endDate}`)}
                ${detail("Customer", record.customerName)}
                ${detail("Lifecycle", record.expired ? "End date has passed current date" : "Within active maintenance period")}
            </div>
        </section>
        <div class="insight-actions">
            <button class="primary-button" type="button" data-edit-id="${record.id}">Edit Record</button>
        </div>
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
}

function openModal(id = null) {
    state.editingId = id;
    const record = id == null ? null : state.records.find(item => item.id === id);
    document.getElementById("maintenance-modal-label").textContent = id == null ? "Add" : "Edit";
    document.getElementById("maintenance-modal-title").textContent = `${id == null ? "Add" : "Edit"} Annual Maintenance`;
    maintenanceForm.innerHTML = `
        <label class="field full"><span>Project Name</span><input name="projectName" value="${record ? escapeHtml(record.projectName) : ""}" required></label>
        <label class="field"><span>Market</span><input name="market" value="${record ? escapeHtml(record.market) : ""}" required></label>
        <label class="field"><span>Maintenance Year</span><input name="maintenanceYear" type="number" min="1" value="${record ? escapeHtml(String(record.maintenanceYear)) : ""}" required></label>
        <label class="field"><span>Amount</span><input name="amount" type="number" min="0.01" step="0.01" value="${record ? escapeHtml(String(record.amount)) : ""}" required></label>
        <label class="field"><span>Start Date</span><input name="startDate" type="date" value="${record ? escapeHtml(record.startDate) : ""}" required></label>
        <label class="field"><span>End Date</span><input name="endDate" type="date" value="${record ? escapeHtml(record.endDate) : ""}" required></label>
        <label class="field"><span>Payment Status</span>
            <select name="paymentStatus" required>
                <option value="PAID" ${record && record.paymentStatus === "PAID" ? "selected" : ""}>Paid</option>
                <option value="NOT_PAID" ${record && record.paymentStatus === "NOT_PAID" ? "selected" : ""}>Not Paid</option>
            </select>
        </label>
        <div class="form-footer">
            <button class="ghost-button" type="button" id="maintenance-cancel-button">Cancel</button>
            <button class="primary-button" type="submit">${id == null ? "Save" : "Update"}</button>
        </div>
    `;
    maintenanceForm.onsubmit = submitForm;
    maintenanceModal.classList.remove("hidden");
}

function closeModal() {
    state.editingId = null;
    maintenanceModal.classList.add("hidden");
}

async function submitForm(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const payload = {
        projectName: formData.get("projectName"),
        market: formData.get("market"),
        maintenanceYear: Number(formData.get("maintenanceYear")),
        amount: Number(formData.get("amount")),
        startDate: formData.get("startDate"),
        endDate: formData.get("endDate"),
        paymentStatus: formData.get("paymentStatus"),
        customerId
    };

    try {
        await api(`/api/annual-maintenance${state.editingId == null ? "" : `/${state.editingId}`}`, {
            method: state.editingId == null ? "POST" : "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });
        closeModal();
        await loadPage();
        maintenanceMessage.textContent = state.editingId == null ? "Annual maintenance record added." : "Annual maintenance record updated.";
    } catch (error) {
        maintenanceMessage.textContent = error.message;
    }
}

document.addEventListener("click", event => {
    if (event.target && event.target.id === "maintenance-cancel-button") {
        closeModal();
    }
    const detailEdit = event.target.closest("#maintenance-detail-content [data-edit-id]");
    if (detailEdit) {
        openModal(Number(detailEdit.dataset.editId));
    }
});

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
