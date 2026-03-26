const state = {
    token: localStorage.getItem("crmToken") || "",
    username: localStorage.getItem("crmUsername") || "",
    customers: [],
    contacts: [],
    tasks: [],
    activities: []
};

const loginPanel = document.getElementById("login-panel");
const dashboard = document.getElementById("dashboard");
const loginMessage = document.getElementById("login-message");
const appMessage = document.getElementById("app-message");

document.getElementById("login-form").addEventListener("submit", handleLogin);
document.getElementById("logout-button").addEventListener("click", logout);
document.getElementById("refresh-button").addEventListener("click", refreshDashboard);
document.getElementById("customer-form").addEventListener("submit", handleCustomerSubmit);
document.getElementById("contact-form").addEventListener("submit", handleContactSubmit);
document.getElementById("task-form").addEventListener("submit", handleTaskSubmit);
document.getElementById("activity-form").addEventListener("submit", handleActivitySubmit);

bootstrap();

function bootstrap() {
    if (state.token) {
        showDashboard();
        refreshDashboard();
    } else {
        showLogin();
    }
}

async function handleLogin(event) {
    event.preventDefault();
    const form = new FormData(event.target);
    const body = Object.fromEntries(form.entries());

    loginMessage.textContent = "Signing in...";

    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(body)
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
        showDashboard();
        await refreshDashboard();
    } catch (error) {
        loginMessage.textContent = error.message;
    }
}

function logout() {
    state.token = "";
    state.username = "";
    localStorage.removeItem("crmToken");
    localStorage.removeItem("crmUsername");
    showLogin();
    appMessage.textContent = "Signed out.";
}

function showLogin() {
    loginPanel.classList.remove("hidden");
    dashboard.classList.add("hidden");
}

function showDashboard() {
    loginPanel.classList.add("hidden");
    dashboard.classList.remove("hidden");
    document.getElementById("current-user").textContent = state.username || "admin";
}

async function refreshDashboard() {
    try {
        const [customers, contacts, tasks, activities] = await Promise.all([
            api("/api/customers"),
            api("/api/contacts"),
            api("/api/tasks"),
            api("/api/activities")
        ]);

        state.customers = customers;
        state.contacts = contacts;
        state.tasks = tasks;
        state.activities = activities;

        updateStats();
        updateSelectors();
        renderLists();
        appMessage.textContent = "Dashboard refreshed.";
    } catch (error) {
        if (error.message === "Unauthorized") {
            logout();
            loginMessage.textContent = "Session expired. Please sign in again.";
            return;
        }
        appMessage.textContent = error.message;
    }
}

async function handleCustomerSubmit(event) {
    event.preventDefault();
    const form = event.target;
    const payload = objectFromForm(form);
    await submitEntity("/api/customers", payload, form, "Customer saved.");
}

async function handleContactSubmit(event) {
    event.preventDefault();
    const form = event.target;
    const payload = objectFromForm(form, ["customerId"]);
    await submitEntity("/api/contacts", payload, form, "Contact saved.");
}

async function handleTaskSubmit(event) {
    event.preventDefault();
    const form = event.target;
    const payload = objectFromForm(form, ["customerId"]);
    await submitEntity("/api/tasks", payload, form, "Task saved.");
}

async function handleActivitySubmit(event) {
    event.preventDefault();
    const form = event.target;
    const payload = objectFromForm(form, ["customerId"]);
    await submitEntity("/api/activities", payload, form, "Activity logged.");
}

async function submitEntity(path, payload, form, successMessage) {
    try {
        await api(path, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${state.token}`
            },
            body: JSON.stringify(payload)
        });
        form.reset();
        await refreshDashboard();
        appMessage.textContent = successMessage;
    } catch (error) {
        appMessage.textContent = error.message;
    }
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

function objectFromForm(form, numberKeys = []) {
    const formData = new FormData(form);
    const result = {};
    for (const [key, value] of formData.entries()) {
        if (value === "") {
            continue;
        }
        result[key] = numberKeys.includes(key) ? Number(value) : value;
    }
    return result;
}

function updateStats() {
    document.getElementById("customer-count").textContent = state.customers.length;
    document.getElementById("contact-count").textContent = state.contacts.length;
    document.getElementById("task-count").textContent = state.tasks.filter(task => task.status !== "COMPLETED").length;
    document.getElementById("activity-count").textContent = state.activities.length;
}

function updateSelectors() {
    const options = state.customers.map(customer =>
        `<option value="${customer.id}">${escapeHtml(customer.name)}</option>`
    ).join("");

    ["contact-customer-id", "task-customer-id", "activity-customer-id"].forEach(id => {
        const select = document.getElementById(id);
        select.innerHTML = options || `<option value="">No customers yet</option>`;
    });
}

function renderLists() {
    renderCollection("customers-list", state.customers.map(customer => `
        <div class="list-item">
            <h4>${escapeHtml(customer.name)}</h4>
            <p>${escapeHtml(customer.company || "No company")} • ${escapeHtml(customer.email)}</p>
            <div class="pill-row">
                <span class="pill">${escapeHtml(customer.status)}</span>
            </div>
        </div>
    `));

    renderCollection("contacts-list", state.contacts.map(contact => `
        <div class="list-item">
            <h4>${escapeHtml(contact.firstName)} ${escapeHtml(contact.lastName)}</h4>
            <p>${escapeHtml(contact.jobTitle || "No title")} • ${escapeHtml(contact.customerName)}</p>
            <div class="pill-row">
                <span class="pill">${escapeHtml(contact.email)}</span>
            </div>
        </div>
    `));

    renderCollection("tasks-list", state.tasks.map(task => `
        <div class="list-item">
            <h4>${escapeHtml(task.title)}</h4>
            <p>${escapeHtml(task.description || "No description")}</p>
            <div class="pill-row">
                <span class="pill">${escapeHtml(task.status)}</span>
                <span class="pill">${escapeHtml(task.priority)}</span>
            </div>
        </div>
    `));

    renderCollection("activities-list", state.activities.map(activity => `
        <div class="list-item">
            <h4>${escapeHtml(activity.subject)}</h4>
            <p>${escapeHtml(activity.customerName)} • ${formatDateTime(activity.activityDate)}</p>
            <div class="pill-row">
                <span class="pill">${escapeHtml(activity.type)}</span>
                <span class="pill">${escapeHtml(activity.createdBy)}</span>
            </div>
        </div>
    `));
}

function renderCollection(elementId, items) {
    document.getElementById(elementId).innerHTML = items.length
        ? items.join("")
        : `<div class="list-item"><p>No records yet.</p></div>`;
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

function formatDateTime(value) {
    if (!value) {
        return "No date";
    }
    return new Date(value).toLocaleString();
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}
