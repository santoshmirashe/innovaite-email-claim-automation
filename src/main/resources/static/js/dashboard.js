const navReports = document.getElementById('nav-reports');
const navClaim = document.getElementById('nav-claim');
const panelReports = document.getElementById('panel-reports');
const panelClaim = document.getElementById('panel-claim');

[navReports, navClaim].forEach(nav =>
nav.addEventListener('click', () => {
  [navReports, navClaim].forEach(n => n.classList.remove('active'));
  [panelReports, panelClaim].forEach(p => p.classList.remove('active'));
  nav.classList.add('active');
  if (nav === navReports) panelReports.classList.add('active');
  else panelClaim.classList.add('active');
  if (nav === navReports) fetchClaimStats();
})
);

// --- DB Chart ---
function setDefaultDates() {
const today = new Date().toISOString().split('T')[0];
document.getElementById('fromDate').value = today;
document.getElementById('toDate').value = today;
}

async function fetchClaimStats() {
document.getElementById("mask").style.display = "flex";
const loading = document.getElementById('loading');
const ctx = document.getElementById('claimChart').getContext('2d');
const fromDate = document.getElementById('fromDate').value;
const toDate = document.getElementById('toDate').value;
try {
  const res = await fetch(`/api/claim-stats?from=${fromDate}&to=${toDate}`);
  if (!res.ok) throw new Error();
  const data = await res.json();
  loading.style.display = 'none';
  renderChart(data);
} catch {
  loading.textContent = '⚠️ Failed to load data';
} finally {
  document.getElementById("mask").style.display = "none";
}
}

let chartInstance;
function renderChart(data) {
const ctx = document.getElementById('claimChart').getContext('2d');
if (chartInstance) chartInstance.destroy();
chartInstance = new Chart(ctx, {
type: 'bar',
data: {
  labels: ['Total Created', 'Successful', 'Failed'],
  datasets: [{
    label: 'Claims',
    data: [data.created, data.success, data.failed],
    backgroundColor: [
      'rgba(37,99,235,0.7)',
      'rgba(16,185,129,0.7)',
      'rgba(239,68,68,0.7)'
    ],
    borderColor: [
      'rgba(37,99,235,1)',
      'rgba(16,185,129,1)',
      'rgba(239,68,68,1)'
    ],
    borderWidth: 1,
    borderRadius: 6,
    barThickness: 60,
    maxBarThickness: 70,
    categoryPercentage: 0.6,
    barPercentage: 0.7
  }]
},
options: {
  maintainAspectRatio: false,
  responsive: true,
  scales: {
    y: {
      beginAtZero: true,
      grid: { color: '#e2e8f0' },
      ticks: { color: '#475569', stepSize: 10 }
    },
    x: {
      grid: { color: 'transparent' },
      ticks: { color: '#475569' }
    }
  },
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: '#1e293b',
      titleColor: '#fff',
      bodyColor: '#e2e8f0',
      padding: 10
    }
  },
  animation: {
    duration: 1000,
    easing: 'easeOutQuart'
  }
}
});
}

document.getElementById('filterBtn').addEventListener('click', fetchClaimStats);
setDefaultDates();
fetchClaimStats();

const fileInput = document.getElementById("fileInput");
const fileName = document.getElementById("fileName");
const progressBar = document.getElementById("progressBar");
const progressBarFill = progressBar.querySelector("div");
const resultDiv = document.getElementById("result");

fileInput.addEventListener("change", () => {
const file = fileInput.files[0];
if (!file) return;

fileName.textContent = `📁 ${file.name}`;
progressBar.style.display = "block";
progressBarFill.style.width = "0%";
resultDiv.style.display = "none";
resultDiv.textContent = "";

const formData = new FormData();
formData.append("file", file);

const xhr = new XMLHttpRequest();
xhr.open("POST", "/api/ocr", true);

document.getElementById("mask").style.display = "flex";

xhr.upload.addEventListener("progress", (event) => {
if (event.lengthComputable) {
  const percent = Math.round((event.loaded / event.total) * 100);
  progressBarFill.style.width = percent + "%";
}
});

xhr.onload = () => {
document.getElementById("mask").style.display = "none";

if (xhr.status === 200) {
  try {
    const responseData = JSON.parse(xhr.responseText);

    Object.entries(responseData).forEach(([key, value]) => {
      const field = document.getElementById(key);
      if (field) field.value = value ?? "";
    });

    resultDiv.style.display = "block";
    resultDiv.textContent = "✅ File processed successfully — fields populated.";
  } catch (err) {
    console.error("Invalid JSON response:", err);
    resultDiv.style.display = "block";
    resultDiv.textContent = "⚠️ Error: Server response was not valid JSON.";
  }
} else {
  resultDiv.style.display = "block";
  resultDiv.textContent = `❌ Upload failed — ${xhr.statusText || xhr.responseText}`;
}

setTimeout(() => {
  progressBar.style.display = "none";
  progressBarFill.style.width = "0%";
}, 1500);
};

xhr.onerror = () => {
document.getElementById("mask").style.display = "none";
resultDiv.style.display = "block";
resultDiv.textContent = "⚠️ Network or server error during upload.";
};

xhr.send(formData);
});


// --- Submit Claim ---
document.getElementById('claimForm').addEventListener('submit', async (e) => {
e.preventDefault();
document.getElementById("mask").style.display = "flex";
const payload = {
  policyNumber: policyNumber.value,
  contactName: contactName.value,
  fromEmail: fromEmail.value,
  contactPhone: contactPhone.value,
  incidentDate: incidentDate.value,
  claimAmount: claimAmount.value ? parseFloat(claimAmount.value) : null,
  claimDescription: claimDescription.value,
  summary: summary.value
};
try {
  const res = await fetch('/api/create-claim', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  const text = await res.text();
  manualResult.textContent = res.ok ? `✅ Claim created: ${text}` : `❌ Error: ${text}`;
} catch {
  manualResult.textContent = '⚠️ Network or server error.';
} finally {
  document.getElementById("mask").style.display = "none";
}
});
//Table scripts
const API_URL = "/api/claims-list"; // Adjust path if needed

// DOM elements
const tbody = document.getElementById("claimTableBody");
const prevBtn = document.getElementById("prevPage");
const nextBtn = document.getElementById("nextPage");
const pageInfo = document.getElementById("pageInfo");

// Pagination state
let currentPage = 0;
const pageSize = 5;
let totalRecords = 0;
let totalPages = 0;

// Fetch & render data
async function fetchClaims(page = 0) {
const fromDate = document.getElementById("fromDate")?.value;
const toDate = document.getElementById("toDate")?.value;

const params = new URLSearchParams();
if (fromDate) params.append("from", fromDate);
if (toDate) params.append("to", toDate);
params.append("offset", page * pageSize);
params.append("limit", pageSize);

try {
tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;color:#64748b;">Loading...</td></tr>`;
const res = await fetch(`${API_URL}?${params.toString()}`);
if (!res.ok) throw new Error("Failed to fetch claim list");
const data = await res.json();

renderTable(data.claims);
updatePagination(data);
} catch (err) {
console.error("Error loading claims:", err);
tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;color:red;">Error loading data</td></tr>`;
}
}

function renderTable(claims) {
if (!claims || claims.length === 0) {
tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;color:#64748b;">No claims found</td></tr>`;
return;
}

tbody.innerHTML = claims.map(c => `
<tr>
  <td>${c.policyNumber || '-'}</td>
  <td>${c.customerName || '-'}</td>
  <td>${c.claimNumber || '-'}</td>
  <td>${c.createdDate || '-'}</td>
  <td>${c.success ? '✅' : '❌'}</td>
</tr>
`).join("");
}

function updatePagination(data) {
totalRecords = data.totalRecords;
totalPages = data.totalPages;
currentPage = data.currentPage;

const start = currentPage * pageSize + 1;
const end = Math.min(start + pageSize - 1, totalRecords);

pageInfo.textContent = `Showing ${start}–${end} of ${totalRecords} results`;

prevBtn.disabled = currentPage <= 0;
nextBtn.disabled = currentPage >= totalPages - 1;
}

// Event listeners
prevBtn.addEventListener("click", () => {
if (currentPage > 0) fetchClaims(currentPage - 1);
});

nextBtn.addEventListener("click", () => {
if (currentPage < totalPages - 1) fetchClaims(currentPage + 1);
});

// Filter button triggers reload
document.getElementById("filterBtn").addEventListener("click", () => fetchClaims(0));

// Initial load
fetchClaims(0);
//Table scripts end