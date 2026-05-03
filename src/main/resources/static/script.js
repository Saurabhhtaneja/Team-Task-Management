const BASE_URL = "http://localhost:8080";
const TOKEN_KEY = "ttm_token";
const USER_KEY = "ttm_user";

const loginView = document.getElementById("loginView");
const appView = document.getElementById("appView");
const loginMessage = document.getElementById("loginMessage");
const globalMessage = document.getElementById("globalMessage");

function token() {
  return localStorage.getItem(TOKEN_KEY);
}

function headers(auth = false) {
  const h = { "Content-Type": "application/json" };
  if (auth) {
    const t = token();
    if (t) h.Authorization = `Bearer ${t}`;
  }
  return h;
}

function showToast(message) {
  globalMessage.textContent = message;
  globalMessage.classList.remove("hidden");
  clearTimeout(showToast._timer);
  showToast._timer = setTimeout(() => {
    globalMessage.classList.add("hidden");
  }, 2800);
}

function showLogin() {
  loginView.classList.remove("hidden");
  appView.classList.add("hidden");
}

function showApp() {
  loginView.classList.add("hidden");
  appView.classList.remove("hidden");
}

function showSection(id) {
  document.querySelectorAll(".content-section").forEach(section => {
    section.classList.add("hidden");
  });
  document.getElementById(id).classList.remove("hidden");

  document.querySelectorAll(".nav-item").forEach(btn => btn.classList.remove("active"));
  const map = {
    overview: 0,
    projects: 1,
    tasks: 2,
    activity: 3
  };
  document.querySelectorAll(".nav-item")[map[id]].classList.add("active");
}

async function login() {
  loginMessage.textContent = "";

  const username = document.getElementById("loginUsername").value.trim();
  const password = document.getElementById("loginPassword").value.trim();

  if (!username || !password) {
    loginMessage.textContent = "Username and password are required.";
    return;
  }

  try {
    const res = await fetch(`${BASE_URL}/api/auth/login`, {
      method: "POST",
      headers: headers(false),
      body: JSON.stringify({ username, password })
    });

    const data = await res.json();

    if (!res.ok || !data.token) {
      loginMessage.textContent = data.message || "Login failed.";
      return;
    }

    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(USER_KEY, JSON.stringify(data));
    setupUserUI(data);
    showApp();
    showSection("overview");
    await refreshAll();
    showToast("Logged in successfully.");
  } catch (err) {
    loginMessage.textContent = "Backend not reachable. Start Spring Boot first.";
  }
}

function setupUserUI(data) {
  const displayName = data.fullName || data.username || "User";
  document.getElementById("userLine").textContent = displayName;
  document.getElementById("topSubtitle").textContent = `Signed in as ${displayName}`;
  document.getElementById("roleChip").textContent = (data.roles && data.roles.join(", ")) || "Authenticated";
}

function logout() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  showLogin();
}

async function createProject() {
  const name = document.getElementById("projectName").value.trim();
  const description = document.getElementById("projectDescription").value.trim();
  const deadline = document.getElementById("projectDeadline").value;

  if (!name) {
    showToast("Project name is required.");
    return;
  }

  const payload = { name };
  if (description) payload.description = description;
  if (deadline) payload.deadline = deadline;

  const res = await fetch(`${BASE_URL}/api/projects`, {
    method: "POST",
    headers: headers(true),
    body: JSON.stringify(payload)
  });

  if (!res.ok) {
    const err = await safeJson(res);
    showToast(err.message || "Could not create project.");
    return;
  }

  document.getElementById("projectName").value = "";
  document.getElementById("projectDescription").value = "";
  document.getElementById("projectDeadline").value = "";

  showToast("Project created.");
  await refreshAll();
}

async function createTask() {
  const title = document.getElementById("taskTitle").value.trim();
  const description = document.getElementById("taskDescription").value.trim();
  const projectId = document.getElementById("taskProjectId").value.trim();
  const assigneeId = document.getElementById("taskAssigneeId").value.trim();
  const priority = document.getElementById("taskPriority").value;
  const dueDate = document.getElementById("taskDueDate").value;

  if (!title || !projectId) {
    showToast("Task title and project ID are required.");
    return;
  }

  const payload = {
    title,
    projectId: Number(projectId),
    priority
  };

  if (description) payload.description = description;
  if (assigneeId) payload.assigneeId = Number(assigneeId);
  if (dueDate) payload.dueDate = dueDate;

  const res = await fetch(`${BASE_URL}/api/tasks`, {
    method: "POST",
    headers: headers(true),
    body: JSON.stringify(payload)
  });

  if (!res.ok) {
    const err = await safeJson(res);
    showToast(err.message || "Could not create task.");
    return;
  }

  document.getElementById("taskTitle").value = "";
  document.getElementById("taskDescription").value = "";
  document.getElementById("taskProjectId").value = "";
  document.getElementById("taskAssigneeId").value = "";
  document.getElementById("taskDueDate").value = "";

  showToast("Task created.");
  await refreshAll();
}

async function loadDashboard() {
  const res = await fetch(`${BASE_URL}/api/dashboard`, {
    headers: headers(true)
  });

  if (!res.ok) {
    showToast("Could not load dashboard.");
    return;
  }

  const data = await res.json();

  document.getElementById("totalProjects").textContent = data.totalProjects ?? 0;
  document.getElementById("totalTasks").textContent = data.totalTasks ?? 0;
  document.getElementById("doneTasks").textContent = data.doneTasks ?? 0;
  document.getElementById("overdueTasks").textContent = data.overdueTasks ?? 0;

  renderTasks("myTasksList", data.myAssignedTasks || [], "No assigned tasks yet.");
  renderProjects("recentProjectsList", data.recentProjects || [], "No recent projects.");
  renderTasks("overdueTasksList", data.overdueTaskList || [], "No overdue tasks.");
}

async function loadProjects() {
  const res = await fetch(`${BASE_URL}/api/projects`, {
    headers: headers(true)
  });

  if (!res.ok) {
    showToast("Could not load projects.");
    return;
  }

  const data = await res.json();
  renderProjects("projectsList", data || [], "No projects found.");
}

function renderProjects(elementId, items, emptyText) {
  const root = document.getElementById(elementId);
  if (!items.length) {
    root.innerHTML = `<div class="list-item"><div class="list-meta">${emptyText}</div></div>`;
    return;
  }

  root.innerHTML = items.map(project => {
    const owner = project.owner?.username || project.owner?.fullName || "Unknown";
    const memberCount = project.members ? project.members.length : 0;
    const totalTasks = project.totalTasks ?? 0;
    const done = project.completedTasks ?? 0;
    const pending = project.pendingTasks ?? 0;

    return `
      <div class="list-item">
        <div class="list-title">${escapeHtml(project.name)}</div>
        <div class="list-meta">
          ID: ${project.id || "-"} • Owner: ${escapeHtml(owner)}<br/>
          ${escapeHtml(project.description || "No description")}<br/>
          Deadline: ${formatDate(project.deadline)} • Members: ${memberCount}
        </div>
        <div class="pill-row">
          <span class="pill">Tasks: ${totalTasks}</span>
          <span class="pill success">Done: ${done}</span>
          <span class="pill danger">Pending: ${pending}</span>
        </div>
      </div>
    `;
  }).join("");
}

function renderTasks(elementId, items, emptyText) {
  const root = document.getElementById(elementId);
  if (!items.length) {
    root.innerHTML = `<div class="list-item"><div class="list-meta">${emptyText}</div></div>`;
    return;
  }

  root.innerHTML = items.map(task => {
    const overdue = task.overdue ? "Overdue" : "On track";
    const overdueClass = task.overdue ? "danger" : "success";

    return `
      <div class="list-item">
        <div class="list-title">${escapeHtml(task.title)}</div>
        <div class="list-meta">
          Project: ${escapeHtml(task.projectName || "-")} • Task ID: ${task.id || "-"}<br/>
          ${escapeHtml(task.description || "No description")}<br/>
          Due: ${formatDate(task.dueDate)}
        </div>
        <div class="pill-row">
          <span class="pill ${overdueClass}">${overdue}</span>
          <span class="pill">${task.priority || "MEDIUM"}</span>
          <span class="pill">${task.status || "TODO"}</span>
        </div>
      </div>
    `;
  }).join("");
}

async function refreshAll() {
  await Promise.allSettled([
    loadDashboard(),
    loadProjects()
  ]);
}

async function safeJson(res) {
  try {
    return await res.json();
  } catch {
    return {};
  }
}

function formatDate(value) {
  if (!value) return "Not set";
  try {
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return String(value);
    return d.toLocaleString();
  } catch {
    return String(value);
  }
}

function escapeHtml(str) {
  return String(str)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

window.addEventListener("load", async () => {
  const savedToken = token();
  if (!savedToken) {
    showLogin();
    return;
  }

  const user = JSON.parse(localStorage.getItem(USER_KEY) || "{}");
  setupUserUI(user);
  showApp();
  showSection("overview");
  await refreshAll();
});