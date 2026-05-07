const form = document.querySelector("#event-form");
const formStatus = document.querySelector("#form-status");
const metricsContainer = document.querySelector("#metrics");
const logsContainer = document.querySelector("#logs");
const eventsTable = document.querySelector("#events-table");

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    formStatus.textContent = "Submitting event...";

    const formData = new FormData(form);
    const body = new URLSearchParams(formData).toString();

    const response = await fetch("/api/events", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body
    });

    if (!response.ok) {
        formStatus.textContent = "Submission failed. Check payload and event type.";
        return;
    }

    form.reset();
    formStatus.textContent = "Event submitted successfully.";
    await refreshDashboard();
});

async function refreshDashboard() {
    const [events, metrics, logs] = await Promise.all([
        fetch("/api/events").then((response) => response.json()),
        fetch("/api/metrics").then((response) => response.json()),
        fetch("/api/logs").then((response) => response.json())
    ]);

    renderEvents(events);
    renderMetrics(metrics);
    renderLogs(logs);
}

function renderEvents(events) {
    const rows = [...events]
        .sort((left, right) => right.updatedAt.localeCompare(left.updatedAt))
        .map((record) => `
            <tr>
                <td class="mono">${escapeHtml(record.eventId)}</td>
                <td>${escapeHtml(record.eventType)}</td>
                <td><span class="status-pill status-${escapeHtml(record.status)}">${escapeHtml(record.status)}</span></td>
                <td>${record.retryCount}</td>
                <td class="mono">${record.archiveLocation === "null" ? "-" : escapeHtml(record.archiveLocation)}</td>
                <td>${record.errorMessage === "null" ? "-" : escapeHtml(record.errorMessage)}</td>
                <td>${escapeHtml(formatTime(record.updatedAt))}</td>
            </tr>
        `)
        .join("");

    eventsTable.innerHTML = rows;
}

function renderMetrics(metrics) {
    const priorityOrder = ["events_received", "events_processed", "events_failed", "queue_depth"];
    const orderedEntries = priorityOrder
        .filter((key) => Object.hasOwn(metrics, key))
        .map((key) => [key, metrics[key]]);

    metricsContainer.innerHTML = orderedEntries
        .map(([name, value]) => `
            <div class="metric-box">
                <span class="metric-label">${escapeHtml(name.replaceAll("_", " "))}</span>
                <strong>${value}</strong>
            </div>
        `)
        .join("");
}

function renderLogs(logs) {
    logsContainer.innerHTML = logs
        .slice(-12)
        .reverse()
        .map((line) => `<div class="log-line">${escapeHtml(line)}</div>`)
        .join("");
}

function formatTime(value) {
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

refreshDashboard();
setInterval(refreshDashboard, 2000);
