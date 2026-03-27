const customer360State = {
    token: localStorage.getItem("crmToken") || "",
    customerId: Number(new URLSearchParams(window.location.search).get("customerId")) || null,
    customerTab: "overview",
    data: {
        customers: [],
        contacts: [],
        projects: [],
        deals: [],
        tasks: [],
        activities: []
    }
};

const customer360Title = document.getElementById("customer-360-title");
const customer360Subtitle = document.getElementById("customer-360-subtitle");
const customer360Status = document.getElementById("customer-360-status");
const customer360Content = document.getElementById("customer-360-content");
const customer360MaintenanceLink = document.getElementById("customer-360-maintenance-link");

document.addEventListener("click", event => {
    const customerTab = event.target.closest("[data-customer-tab]");
    if (customerTab) {
        customer360State.customerTab = customerTab.dataset.customerTab;
        renderCustomer360Page();
    }
});

bootstrapCustomer360();

async function bootstrapCustomer360() {
    if (!customer360State.token) {
        customer360Status.textContent = "Please sign in from the main CRM page first.";
        return;
    }

    try {
        const [customers, contacts, projects, deals, tasks, activities] = await Promise.all([
            api("/api/customers"),
            api("/api/contacts"),
            api("/api/projects"),
            api("/api/deals"),
            api("/api/tasks"),
            api("/api/activities")
        ]);

        customer360State.data.customers = customers;
        customer360State.data.contacts = contacts;
        customer360State.data.projects = projects;
        customer360State.data.deals = deals;
        customer360State.data.tasks = tasks;
        customer360State.data.activities = activities;

        if (!customer360State.customerId && customers.length) {
            customer360State.customerId = customers[0].id;
        }

        renderCustomer360Page();
    } catch (error) {
        customer360Status.textContent = error.message;
    }
}

function renderCustomer360Page() {
    const customer = customer360State.data.customers.find(item => item.id === customer360State.customerId);
    if (!customer) {
        customer360Title.textContent = "Customer 360 Workspace";
        customer360Subtitle.textContent = "No customer was selected.";
        customer360Content.innerHTML = `<div class="empty-insight"><p>Select a customer from Customer Management and open 360 again.</p></div>`;
        return;
    }

    customer360Title.textContent = `${customer.name} Customer 360`;
    customer360Subtitle.textContent = `${customer.cifNumber || "CIF pending"} • ${beautify(customer.customerType)} • Strategic Relationship View`;
    customer360MaintenanceLink.href = `/annual-maintenance.html?customerId=${customer.id}&source=customer360`;
    customer360Content.innerHTML = renderCustomerInsight(customer);
}

function renderCustomerInsight(item) {
    const contacts = customer360State.data.contacts.filter(contact => contact.customerId === item.id);
    const projects = customer360State.data.projects.filter(project => project.customerId === item.id);
    const deals = customer360State.data.deals.filter(deal => deal.customerId === item.id);
    const tasks = customer360State.data.tasks.filter(task => task.customerId === item.id);
    const openDeals = deals.filter(deal => deal.stage !== "WON" && deal.stage !== "LOST");
    const totalPipeline = openDeals.reduce((sum, deal) => sum + Number(deal.amount || 0), 0);
    const totalProjectValue = projects.reduce(
        (sum, project) => sum + Number(project.licenseAmount || 0) + Number(project.implementationAmount || 0),
        0
    );
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
    const executiveBrief = buildExecutiveBrief(item, contacts, projects, openDeals, tasks);

    return `
        <div class="customer-360-shell">
            <aside class="customer-360-core">
                <div class="customer-core-hero">
                    <div class="customer-core-logo">${escapeHtml((item.name || "C").slice(0, 1))}</div>
                    <div>
                        <span class="eyebrow">${escapeHtml(beautify(item.customerType))}</span>
                        <h3>${escapeHtml(item.name)}</h3>
                        <p>${escapeHtml(item.cifNumber || "CIF pending")} • ${escapeHtml(beautify(item.status))} • ${escapeHtml(beautify(item.segment || "unassigned"))}</p>
                    </div>
                </div>
                <div class="customer-core-grid">
                    ${coreMetric("Tier", item.segment || "Unassigned")}
                    ${coreMetric("Customer Score", customerScore(item))}
                    ${coreMetric("Lifecycle", beautify(item.status))}
                    ${coreMetric("Open Pipeline", formatMoney(totalPipeline))}
                </div>
                ${detailGroup("Core Details", [
                    detailItem("Customer Type", beautify(item.customerType)),
                    detailItem("Risk", beautify(item.riskLevel)),
                    detailItem("Lifecycle", beautify(item.status))
                ])}
                ${narrativeGroup("Executive Summary", executiveBrief)}
            </aside>
            <section class="customer-360-main">
                <div class="customer-tab-bar">
                    ${renderCustomerTab("overview", "Overview")}
                    ${renderCustomerTab("contacts", "Contacts")}
                    ${renderCustomerTab("opportunities", "Opportunities")}
                    ${renderCustomerTab("projects", "Projects")}
                    ${renderCustomerTab("products", "Products")}
                </div>
                <div class="customer-tab-panel">
                    ${renderCustomerTabContent(item, contacts, openDeals, projects, tasks)}
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
                ${detailGroup("Commercial Snapshot", [
                    detailItem("Open Pipeline", formatMoney(totalPipeline)),
                    detailItem("Project Book", formatMoney(totalProjectValue)),
                    detailItem("Active Projects", String(projects.length))
                ])}
                ${detailGroup("Alerts", alerts.map(alert => detailItem(alert.label, alert.value)))}
                <div class="insight-actions">
                    <a class="primary-button link-button" href="/annual-maintenance.html?customerId=${item.id}&source=customer360">Open Annual Maintenance</a>
                </div>
            </aside>
        </div>
    `;
}

function renderCustomerTab(tab, label) {
    return `<button class="customer-tab ${customer360State.customerTab === tab ? "active" : ""}" type="button" data-customer-tab="${tab}">${escapeHtml(label)}</button>`;
}

function renderCustomerTabContent(item, contacts, openDeals, projects, tasks) {
    if (customer360State.customerTab === "contacts") {
        return renderCustomerCollection("Contacts", contacts, contact =>
            `<strong>${escapeHtml(contact.firstName)} ${escapeHtml(contact.lastName)}</strong><span>${escapeHtml(contact.jobTitle || contact.email || "No role captured")}</span>`
        );
    }

    if (customer360State.customerTab === "opportunities") {
        return renderCustomerCollection("Opportunities", openDeals, deal =>
            `<strong>${escapeHtml(deal.title)}</strong><span>${formatMoney(deal.amount)} • ${escapeHtml(beautify(deal.stage))}</span>`
        );
    }

    if (customer360State.customerTab === "projects") {
        return renderCustomerCollection("Projects", projects, project =>
            `<strong>${escapeHtml(project.projectName)}</strong><span>${escapeHtml(project.market)} • License ${formatMoney(project.licenseAmount)} • Implementation ${formatMoney(project.implementationAmount)} • ${escapeHtml(beautify(project.status))}</span>`
        );
    }

    if (customer360State.customerTab === "products") {
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

    return `
        <section class="customer-tab-section">
            <div class="customer-tab-section-header">
                <span class="eyebrow">Overview</span>
                <h4>Executive Relationship Summary</h4>
            </div>
            <div class="customer-overview-grid">
                ${insightMetric("Contacts", String(contacts.length))}
                ${insightMetric("Open Deals", String(openDeals.length))}
                ${insightMetric("Projects", String(projects.length))}
                ${insightMetric("Service Tasks", String(tasks.length))}
            </div>
            ${detailGroup("Commercial Posture", [
                detailItem("Pipeline Coverage", openDeals.length ? `${openDeals.length} qualified opportunity(ies)` : "No open commercial opportunity"),
                detailItem("Service Load", tasks.length ? `${tasks.length} service task(s) under management` : "No active servicing tasks"),
                detailItem("Engagement Depth", contacts.length ? `${contacts.length} known stakeholder contact(s)` : "Stakeholder coverage still light")
            ])}
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

function narrativeGroup(title, text) {
    return `
        <section class="detail-group">
            <h4>${escapeHtml(title)}</h4>
            <p class="detail-narrative">${escapeHtml(text)}</p>
        </section>
    `;
}

function buildExecutiveBrief(item, contacts, projects, openDeals, tasks) {
    const parts = [];
    parts.push(`${beautify(item.customerType)} relationship`);
    parts.push(`${beautify(item.status)} lifecycle`);
    parts.push(`${openDeals.length} open opportunity(ies)`);
    parts.push(`${projects.length} active project(s)`);
    if (tasks.length) {
        parts.push(`${tasks.length} servicing task(s)`);
    }
    if (contacts.length) {
        parts.push(`${contacts.length} stakeholder contact(s)`);
    }
    return parts.join(" • ");
}

function coreMetric(label, value) {
    return `
        <article class="customer-core-metric">
            <span>${escapeHtml(label)}</span>
            <strong>${escapeHtml(typeof value === "string" ? beautify(value) : String(value))}</strong>
        </article>
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

async function api(path, options = {}) {
    const headers = options.headers || {};
    if (customer360State.token && !headers.Authorization) {
        headers.Authorization = `Bearer ${customer360State.token}`;
    }
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

function formatMoney(value) {
    const amount = Number(value || 0);
    return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD", maximumFractionDigits: 2 }).format(amount);
}

function formatDateTime(value) {
    if (!value) {
        return "Not captured";
    }
    return new Intl.DateTimeFormat("en-GB", {
        year: "numeric",
        month: "short",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    }).format(new Date(value));
}

function beautify(value) {
    return String(value || "")
        .replace(/_/g, " ")
        .toLowerCase()
        .replace(/\b\w/g, char => char.toUpperCase());
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function startOfToday() {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return today;
}
