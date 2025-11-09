document.addEventListener("DOMContentLoaded", () => {
const tabAnalytics = document.getElementById("tab-analytics");
const tabHistory   = document.getElementById("tab-history");
const tabReport    = document.getElementById("tab-report");

const panelAnalytics = document.getElementById("panel-analytics");
const panelHistory   = document.getElementById("panel-history");
const panelReport    = document.getElementById("panel-report");

function activateTab(tabName) {
  // Remove 'active' from all tabs and panels
  [tabAnalytics, tabHistory, tabReport].forEach(t => t.classList.remove("active"));
  [panelAnalytics, panelHistory, panelReport].forEach(p => p.classList.remove("active"));

  // Activate chosen tab
  if (tabName === "analytics") {
    tabAnalytics.classList.add("active");
    panelAnalytics.classList.add("active");
    fetchClaimStats?.(); // refresh analytics data
  } else if (tabName === "history") {
    tabHistory.classList.add("active");
    panelHistory.classList.add("active");
    fetchClaims?.(0); // load paginated history
  } else {
    tabReport.classList.add("active");
    panelReport.classList.add("active");
  }
}

tabAnalytics.addEventListener("click", () => activateTab("analytics"));
tabHistory.addEventListener("click",   () => activateTab("history"));
tabReport.addEventListener("click",    () => activateTab("report"));

// Default tab on load
activateTab("analytics");



//Analytics (Claim Stats)

function setDefaultDates() {
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('fromDate').value = today;
  document.getElementById('toDate').value = today;
  document.getElementById('fromDateHistory').value = today;
    document.getElementById('toDateHistory').value = today;
}

async function fetchClaimStats() {
  document.getElementById("mask").style.display = "flex";
  const loading = document.getElementById('loading');
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
        barThickness: 60
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



// File Upload + OCR
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



//Manual Claim Submission

document.getElementById('claimForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  document.getElementById("mask").style.display = "flex";

  const payload = {
    policyNumber: policyNumber.value,
    contactName: contactName.value,
    fromEmail: fromEmail.value,
    contactPhone: contactPhone.value,
    incidentDate: incidentDate.value ? `${incidentDate.value}T00:00:00.000` : null,
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


// Claim History Table
const API_URL = "/api/claims-list";
const tbody = document.getElementById("claimTableBody");
const prevBtn = document.getElementById("prevPage");
const nextBtn = document.getElementById("nextPage");
const pageInfo = document.getElementById("pageInfo");

let currentPage = 0;
const pageSize = 5;
let totalRecords = 0;
let totalPages = 0;

async function fetchClaims(page = 0) {
  const fromDate = document.getElementById("fromDateHistory")?.value;
  const toDate = document.getElementById("toDateHistory")?.value;

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
      <td style="text-align:center;">
              ${!c.success ? `<button class="retry-btn" title="Retry Claim" data-policy-number="${c.policyNumber}" data-id="${c.id}">🔄</button>` : '-'}
        </td>
    </tr>
  `).join("");

  document.querySelectorAll(".retry-btn").forEach(btn => {
      btn.addEventListener("click", async (e) => {
        const id = e.target.dataset.id;
        const policyNumber = e.target.dataset.policyNumber;
        await retryClaim(policyNumber,id);
      });
  });
}

async function retryClaim(policyNumber,id) {
  if (!confirm(`Reprocess claim by ${policyNumber}?`)) return;

  document.getElementById("mask").style.display = "flex"; // show loader

  try {
    const res = await fetch(`/api/retry-claim/${id}`, {
      method: "POST"
    });

    if (!res.ok) throw new Error("Server error during retry");

    const updatedClaim = await res.json();

    alert(`✅ Claim reprocessed successfully.\nNew Claim Number: ${updatedClaim.claimNumber}`);

    // Refresh the table to reflect updated status
    fetchClaims(currentPage);
  } catch (err) {
    console.error(err);
    alert("⚠️ Failed to reprocess claim. Check server logs.");
  } finally {
    document.getElementById("mask").style.display = "none"; // hide loader
  }
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

prevBtn.addEventListener("click", () => {
  if (currentPage > 0) fetchClaims(currentPage - 1);
});

nextBtn.addEventListener("click", () => {
  if (currentPage < totalPages - 1) fetchClaims(currentPage + 1);
});

document.getElementById("filterBtnHistory").addEventListener("click", () => fetchClaims(0));
document.getElementById("refreshBtnHistory").addEventListener("click", () => fetchClaims(currentPage));
document.getElementById("refreshBtn").addEventListener("click", () => fetchClaimStats());

});