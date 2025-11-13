document.addEventListener("DOMContentLoaded", () => {
  const role = Auth.getRole();
  const username = Auth.getUsername();

  const tabAnalytics = document.getElementById("tab-analytics");
  const tabHistory   = document.getElementById("tab-history");
  const tabReport    = document.getElementById("tab-report");
  const tabAdmin     = document.getElementById("tab-admin");

  if (role !== "ROLE_ADMIN") {
    /*tabAnalytics?.classList.add("hidden");
    tabHistory?.classList.add("hidden");*/
    tabAdmin?.classList.add("hidden");
    tabAnalytics.style.display = "inline-block";
    tabHistory.style.display = "inline-block";
    tabReport.style.display = "inline-block";
  } else {
    tabAdmin.style.display = "inline-block";
  }

  const style = document.createElement("style");
  style.innerHTML = `.hidden { display: none !important; }`;
  document.head.appendChild(style);

const panelAnalytics = document.getElementById("panel-analytics");
const panelHistory   = document.getElementById("panel-history");
const panelReport    = document.getElementById("panel-report");
const panelAdmin     = document.getElementById("panel-admin");


function activateTab(tabName) {
  [tabAnalytics, tabHistory, tabReport, tabAdmin].forEach(t => t.classList.remove("active"));
  [panelAnalytics, panelHistory, panelReport, panelAdmin].forEach(p => p.classList.remove("active"));

  if (tabName === "admin") {
    tabAdmin.classList.add("active");
    panelAdmin.classList.add("active");
  } else if (tabName === "analytics") {
    tabAnalytics.classList.add("active");
    panelAnalytics.classList.add("active");
    setDefaultDates?.();
    fetchClaimStats?.();
  } else if (tabName === "history") {
    tabHistory.classList.add("active");
    panelHistory.classList.add("active");
    setDefaultDates?.();
    fetchClaims?.(0);
  } else {
    tabReport.classList.add("active");
    panelReport.classList.add("active");
  }
}

tabAnalytics.addEventListener("click", () => activateTab("analytics"));
tabHistory.addEventListener("click",   () => activateTab("history"));
tabReport.addEventListener("click",    () => activateTab("report"));
tabAdmin.addEventListener("click", () => activateTab("admin"));

// Default tab on load
activateTab("analytics");
//////
function showPdfAnalysis(result) {

    const panel = document.getElementById("pdfAnalysisPanel");
    const score = document.getElementById("pdfFraudScore");
    const status = document.getElementById("pdfFraudStatus");
    const list = document.getElementById("pdfFindingsList");

    // Make panel visible
    panel.style.display = "block";

    // Set score
    score.textContent = result.fraudScore;

    // Determine status
    let fraudStatus = "";
    let color = "";

    if (result.fraudScore <= 5) {
        fraudStatus = "Clean / Safe";
        color = "green";
    } else if (result.fraudScore <= 15) {
        fraudStatus = "Suspicious – Review Needed";
        color = "orange";
    } else {
        fraudStatus = "High Fraud Risk!";
        color = "red";
    }

    status.textContent = fraudStatus;
    status.style.color = color;
    score.style.color = color;

    // Populate findings
    list.innerHTML = "";
    result.findings.forEach(f => {
        const li = document.createElement("li");
        li.textContent = f;
        list.appendChild(li);
    });
}

document.getElementById("clearBtn").addEventListener("click", function () {
    // 1. Clear all input fields & textareas
    document.querySelectorAll("#claimForm input, #claimForm textarea").forEach(el => {
        el.value = "";
    });

    // 2. Clear file selector (inside your upload-section fragment)
    const fileInput = document.querySelector("#uploadFile"); // <-- update this ID to match your file input
    if (fileInput) {
        fileInput.value = ""; // resets input
    }

    // 3. Clear filename display element if you have one
    const fileNameLabel = document.querySelector("#fileName");
    if (fileNameLabel) {
        fileNameLabel.textContent = "";
    }

    // 4. Hide PDF Analysis Panel
    const panel = document.getElementById("pdfAnalysisPanel");
    if (panel) {
        panel.style.display = "none";
    }

    // 5. Clear panel values
    document.getElementById("pdfFraudScore").textContent = "";
    document.getElementById("pdfFraudStatus").textContent = "";
    document.getElementById("pdfFindingsList").innerHTML = "";

    // 6. Clear manual result block
    const manualResult = document.getElementById("manualResult");
    if (manualResult) {
        manualResult.innerHTML = "";
    }
     // 7. Hide the result section (from your backend response)
     const resultDiv = document.getElementById("result");
     if (resultDiv) {
         resultDiv.style.display = "none";
         resultDiv.innerHTML = ""; // clear previous content
     }

    console.log("✔ All fields cleared");
});
///////


//Analytics (Claim Stats)

function setDefaultDates() {
    const today = new Date();
    const fiveDaysBefore = new Date(today);
    fiveDaysBefore.setDate(today.getDate() - 5);

    const formattedFrom = fiveDaysBefore.toISOString().split("T")[0];
    const formattedTo = today.toISOString().split("T")[0];

    function setIfEmpty(id, value) {
        const el = document.getElementById(id);
        if (el && (!el.value || el.value.trim() === "")) {
            el.value = value;
        }
    }

    setIfEmpty('fromDate', formattedFrom);
    setIfEmpty('toDate', formattedTo);
    setIfEmpty('fromDateHistory', formattedFrom);
    setIfEmpty('toDateHistory', formattedTo);
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
        if (responseData.pdfAnalysisResult) {
            showPdfAnalysis(responseData.pdfAnalysisResult);
        }
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
    incidentDate: incidentDate.value
      ? (
          incidentDate.value.includes('.')   // already has milliseconds
            ? incidentDate.value
            : incidentDate.value.length === 16
              ? incidentDate.value + ":00.000" // missing seconds
              : incidentDate.value + ".000"    // has seconds, missing ms
        )
      : null,
    claimAmount: claimAmount.value ? parseFloat(claimAmount.value) : null,
    claimDescription: claimDescription.value,
    summary: summary.value
  };

  try {
    console.log("incidentDate being sent:", payload.incidentDate);
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
        ${
          (!c.success /*&& Auth.isAdmin()*/)
            ? `<button class="retry-btn" title="Retry Claim" data-policy-number="${c.policyNumber}" data-id="${c.id}">🔄</button>`
            : '-'
        }
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
/*Search*/
const claimHistorySearch = {
        input: document.getElementById("claimHistorySearchInput"),
        tableBody: document.getElementById("claimTableBody"),
        pageInfo: document.getElementById("pageInfo"),
        btnPrev: document.getElementById("prevPage"),
        btnNext: document.getElementById("nextPage"),

        // Pagination / search state
        limit: 5,
        offset: 0,
        totalCount: 0,
        totalPages: 0,
        currentSearch: "",
        debounceTimer: null,

        async fetchData() {
            const from = document.getElementById("fromDateHistory")?.value || "";
            const to = document.getElementById("toDateHistory")?.value || "";
            const search = this.currentSearch?.trim() || "";

            const params = new URLSearchParams({
                search,
                from,
                to,
                offset: this.offset.toString(),
                limit: this.limit.toString()
            });

            try {
                const response = await fetch(`/api/search?${params.toString()}`);
                if (!response.ok) throw new Error(`HTTP ${response.status}`);
                const data = await response.json();

                // ✅ Correct mapping for backend structure
                this.totalCount = data.totalRecords || 0;
                this.totalPages = data.totalPages || 0;

                // ✅ Render the actual claim list
                this.renderTable(data.claims || []);
                updatePagination(data);

                console.log(`Fetched ${this.totalCount} records, rendering ${data.claims?.length || 0}`);
            } catch (error) {
                console.error("Error fetching claims:", error);
                this.tableBody.innerHTML = `<tr><td colspan="6" style="text-align:center;">Error loading claims</td></tr>`;
            }
        },

        renderTable(rows) {
            if (!rows.length) {
                this.tableBody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No results found</td></tr>`;
                return;
            }

            this.tableBody.innerHTML = rows.map(c => `
                <tr>
                    <td>${c.policyNumber || "-"}</td>
                    <td>${c.customerName || "-"}</td>
                    <td>${c.claimNumber || "-"}</td>
                    <td>${c.createdDate || "-"}</td>
                    <td>${c.success ? "✅" : "❌"}</td>
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
        },

        handleSearchInput() {
            clearTimeout(this.debounceTimer);
            this.debounceTimer = setTimeout(() => {
                const value = this.input.value.trim();

                if (value.length >= 3) {
                    this.currentSearch = value;
                    this.offset = 0;
                    this.fetchData();
                } else if (value.length === 0) {
                    this.currentSearch = "";
                    this.offset = 0;
                    this.fetchData(); // only date filter
                }
                // <3 chars: do nothing
            }, 400);
        },

        handlePrevPage() {
            if (this.offset >= this.limit) {
                this.offset -= this.limit;
                this.fetchData();
            }
        },

        handleNextPage() {
            const nextOffset = this.offset + this.limit;
            if (nextOffset < this.totalCount) {
                this.offset = nextOffset;
                this.fetchData();
            }
        },

        init() {
            this.input.addEventListener("keyup", () => this.handleSearchInput());
            this.btnPrev.addEventListener("click", () => this.handlePrevPage());
            this.btnNext.addEventListener("click", () => this.handleNextPage());
            this.fetchData(); // initial load
        }
    };

    claimHistorySearch.init();
});