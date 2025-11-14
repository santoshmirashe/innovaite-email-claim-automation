(function () {

/***** CONFIG *****/
const AUTO_CLOSE_MS = 60000;    // auto-close after 60s
const TYPING_SPEED = 24;        // ms per character for AI typing simulation
const MIN_WIDTH = 260;
const MIN_HEIGHT = 200;

/***** STATE *****/
let inactivityTimer = null;
let isOpen = false;
let firstOpen = true;
let unreadCount = 0;
let typingInterval = null;
let cancelTyping = false;
let isDark = localStorage.getItem('elonChatDark') === 'true';

// ---- per-user keys (computed at init) ----
let USER_ID = null;
let STORAGE_KEY = null;
let LASTSEEN_KEY = null;
let FIRSTOPEN_KEY = null;
let messages = []; // per-user message list
let observer = null;

/***** INJECT HTML + CSS *****/
const html = `
<style>
    :root {
        --bg: #ffffff;
        --panel: #f9fafb;
        --primary: #2563eb;
        --text: #111827;
        --muted: #6b7280;
        --user-bg: #dbeafe;
        --bot-bg: #e5e7eb;
        --shell-shadow: 0 4px 20px rgba(0,0,0,0.18);
    }
    .elon-dark {
        --bg: #0b1220;
        --panel: #071122;
        --primary: #3b82f6;
        --text: #e6eefc;
        --muted: #9aa7bd;
        --user-bg: #1e3a8a;
        --bot-bg: #0f1724;
        --shell-shadow: 0 8px 30px rgba(0,0,0,0.6);
    }

    /* FAB */
    #chatbot-fab {
        position: fixed;
        bottom: 20px; right: 20px;
        width: 58px; height: 58px;
        background: var(--primary);
        color: white;
        border-radius: 50%;
        display: flex; align-items: center; justify-content: center;
        font-size: 26px; cursor: pointer; z-index: 99999;
        box-shadow: var(--shell-shadow);
        transition: transform .14s ease;
    }
    #chatbot-fab:hover { transform: scale(1.08); }

    /* BOX - initially hidden offscreen (slide up) */
    #chatbot-box {
        position: fixed; right: 20px; bottom: -600px;
        width: 360px; height: 460px;
        background: var(--bg); border-radius: 12px;
        box-shadow: var(--shell-shadow);
        display: flex; flex-direction: column; overflow: hidden;
        z-index: 99999; transition: bottom .32s cubic-bezier(.2,.9,.2,1), width .12s ease, height .12s ease;
        border: 1px solid rgba(0,0,0,0.04);
    }
    #chatbot-box.open { bottom: 90px; }

    /* header with controls */
    #chatbot-header {
        display:flex; align-items:center; justify-content:space-between;
        gap:8px;
        background: var(--primary);
        color: white; padding:10px 12px; font-weight:600; font-size:14px;
    }
    #chatbot-title { flex:1; text-align:left; display:flex; align-items:center; gap:8px; }
    #chatbot-title .online-dot { width:10px; height:10px; border-radius:50%; display:inline-block; }
    #chatbot-title .online-dot.online { background: #22c55e; box-shadow:0 0 6px rgba(34,197,94,0.6);}
    #chatbot-title .online-dot.offline { background: rgba(255,255,255,0.35); }

    #chatbot-controls { display:flex; gap:8px; align-items:center; }

    .control-btn {
        background: transparent; border: none; color: white; cursor: pointer; padding:6px;
        border-radius:6px; font-size:14px;
    }
    .control-btn:focus { outline:2px solid rgba(255,255,255,0.18); }

    /* container for messages */
    #chatbot-messages {
        flex:1; padding:12px; overflow-y:auto; overflow-x:hidden; background: var(--panel);
        color: var(--text); scroll-behavior: smooth;
    }

    .chat-msg { margin:8px 0; padding:8px 12px; border-radius:10px; max-width:80%; line-height:1.35; word-break:break-word; position:relative; }
    .chat-user { background: var(--user-bg); margin-left:auto; color: var(--text); }
    .chat-bot  { background: var(--bot-bg); margin-right:auto; color: var(--text); }

    .meta { display:flex; gap:8px; align-items:center; font-size:11px; color:var(--muted); margin-top:6px; }
    .meta .time { font-size:11px; }
    .meta .ticks { font-size:13px; }
    .tick-seen { color:#2563eb; }

    /* typing bubble (messenger-style) */
    #typing-bubble { display:none; margin:8px 0; margin-right:auto; padding:8px; border-radius:12px; background:var(--bot-bg); width:36px; text-align:center; }
    #typing-bubble .dot { display:inline-block; width:6px; height:6px; margin:0 2px; background:var(--muted); border-radius:50%; animation:bounce 1.2s infinite; opacity:0.35; }
    #typing-bubble .dot.d1 { animation-delay:0s; }
    #typing-bubble .dot.d2 { animation-delay:0.12s; }
    #typing-bubble .dot.d3 { animation-delay:0.24s; }
    @keyframes bounce { 0%{transform:translateY(0)} 50%{transform:translateY(-6px)} 100%{transform:translateY(0)} }

    /* loader (pulsing dots) */
    #chatbot-loader { display:none; text-align:center; padding:6px 0; color:var(--primary); font-size:18px; }
    #chatbot-loader span { opacity:0.25; animation:pulse 1.3s infinite; margin:0 2px; }
    @keyframes pulse { 0%{opacity:.2; transform:scale(1)} 50%{opacity:1; transform:scale(1.3)} 100%{opacity:.2; transform:scale(1)} }

    /* input area */
    #chatbot-input-area { display:flex; gap:6px; padding:8px; border-top:1px solid rgba(0,0,0,0.06); background:var(--bg); }
    #chatbot-input { flex:1; padding:10px; border-radius:8px; border:1px solid rgba(0,0,0,0.08); font-size:14px; outline:none; background:transparent; color:var(--text); }
    #chatbot-input::placeholder { color: var(--muted); }
    #chatbot-send { background:var(--primary); color:white; border:none; padding:10px 12px; border-radius:8px; cursor:pointer; }
    #chatbot-send:active { transform:translateY(1px); }

    /* resizer handle bottom-right */
    #chatbot-resizer {
        position:absolute; right:6px; bottom:6px; width:14px; height:14px; cursor:se-resize; opacity:0.6;
    }

    /* accessibility focus */
    #chatbot-box:focus { outline: 2px solid rgba(37,99,235,0.16); }

    /* small responsive adjustments */
    @media (max-width:420px) {
        #chatbot-box { width: 92%; right: 4%; }
    }
</style>

<div id="chatbot-fab" aria-label="Chat with Elon" role="button" tabindex="0">🐶</div>

<div id="chatbot-box" role="dialog" aria-label="Chat with Elon" aria-hidden="true">
    <div id="chatbot-header">
        <div id="chatbot-title"><span class="online-dot offline" aria-hidden="true"></span><span id="chatbot-name">Elon (Support Bot)</span></div>
        <div id="chatbot-controls">
            <button id="darkToggle" class="control-btn" title="Toggle dark mode" aria-pressed="false">🌓</button>
            <button id="clearChat" class="control-btn" title="Clear chat">🗑️</button>
            <button id="closeChat" class="control-btn" title="Close chat">✖</button>
        </div>
    </div>

    <div id="chatbot-messages" tabindex="0" aria-live="polite"></div>

    <div id="typing-bubble" aria-hidden="true"><span class="dot d1"></span><span class="dot d2"></span><span class="dot d3"></span></div>
    <div id="chatbot-loader" aria-hidden="true"><span>•</span><span>•</span><span>•</span></div>

    <div id="chatbot-input-area">
        <input id="chatbot-input" type="text" placeholder="Ask something..." aria-label="Chat input">
        <button id="chatbot-send" aria-label="Send message">➤</button>
    </div>

    <div id="chatbot-resizer" aria-hidden="true" title="Drag to resize"></div>
</div>
`;

// insert into container
document.getElementById("chatbot-container").innerHTML = html;

/***** ELEMENT REFS *****/
const rootEl = document.getElementById("chatbot-container").closest('body') || document.body;
const fab = document.getElementById("chatbot-fab");
const box = document.getElementById("chatbot-box");
const titleEl = document.getElementById("chatbot-title");
const nameEl = document.getElementById("chatbot-name");
const onlineDot = titleEl.querySelector('.online-dot');
const messagesEl = document.getElementById("chatbot-messages");
const inputEl = document.getElementById("chatbot-input");
const sendBtn = document.getElementById("chatbot-send");
const loaderEl = document.getElementById("chatbot-loader");
const typingBubble = document.getElementById("typing-bubble");
const darkToggle = document.getElementById("darkToggle");
const clearChatBtn = document.getElementById("clearChat");
const closeChatBtn = document.getElementById("closeChat");
const resizer = document.getElementById("chatbot-resizer");

/***** UTIL HELPERS *****/
// ESCAPE HTML to prevent XSS
function escapeHtml(unsafe) {
  if (unsafe === null || unsafe === undefined) return '';
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
    '/': '&#x2F;',
    '`': '&#x60;',
    '=': '&#x3D;'
  };
  return String(unsafe).replace(/[&<>"'`=\/]/g, function (s) { return map[s]; });
}

function saveTheme() { localStorage.setItem('elonChatDark', isDark ? 'true' : 'false'); }
function applyTheme() {
    if (isDark) document.documentElement.classList.add('elon-dark');
    else document.documentElement.classList.remove('elon-dark');
    darkToggle.setAttribute('aria-pressed', isDark ? 'true' : 'false');
}

/***** PER-USER STORAGE HELPERS *****/
// Try to detect an application user id to scope localStorage per user.
// Checks common locations, then falls back to 'anon'.
function detectUserId() {
    try {
        if (Auth && typeof Auth.getUsername === "function") {
            const u = Auth.getUsername();
            if (u && typeof u === "string") {
                return u.trim();
            }
        }
    } catch (e) {}

    // Fallback for anonymous visitors
    let anon = localStorage.getItem('elonChatAnonId');
    if (!anon) {
        anon = 'anon_' + Date.now().toString(36) + '_' + Math.random().toString(36).slice(2,8);
        try { localStorage.setItem('elonChatAnonId', anon); } catch (e) {}
    }
    return anon;
}

function enforceDailyReset() {
    const today = new Date().toISOString().slice(0,10); // YYYY-MM-DD

    const LAST_USED_KEY = `elonChatLastDate::${USER_ID}`;
    const lastDate = localStorage.getItem(LAST_USED_KEY);

    if (lastDate !== today) {
        // date changed → wipe everything for this user
        try {
            localStorage.removeItem(STORAGE_KEY);
            localStorage.removeItem(LASTSEEN_KEY);
            localStorage.removeItem(FIRSTOPEN_KEY);
        } catch(e) {}

        messages = [];
        renderMessages();
    }

    // store today for next check
    localStorage.setItem(LAST_USED_KEY, today);
}



function computeKeysForUser() {
    USER_ID = detectUserId();
    // sanitize: replace spaces and special characters
    USER_ID = String(USER_ID).replace(/[^A-Za-z0-9_\-:.]/g, '_').slice(0, 64);
    STORAGE_KEY = 'elonChatHistoryV2::' + USER_ID;
    LASTSEEN_KEY = 'elonChatLastSeen::' + USER_ID;
    FIRSTOPEN_KEY = 'elonChatFirstOpen::' + USER_ID;
}

/***** PERSISTENCE (per user) *****/
function persistMessages() {
    try { localStorage.setItem(STORAGE_KEY, JSON.stringify(messages)); } catch (e) { /* ignore */ }
}
function loadMessages() {
    try {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (raw) messages = JSON.parse(raw) || [];
        else messages = [];
    } catch (e) { messages = []; }
}
function clearChatHistory() {
    messages = [];
    persistMessages();
    renderMessages();
    unreadCount = 0;
    updateTitle();
}

function loadLastSeen() {
    const raw = localStorage.getItem(LASTSEEN_KEY);
    return raw ? Number(raw) : null;
}
function saveLastSeen(ts) {
    try { localStorage.setItem(LASTSEEN_KEY, String(ts)); } catch (e) {}
}

/***** TIME/FORMAT HELPERS *****/
function formatTime(ts) {
    if (!ts) return '';
    const d = new Date(ts);
    const opts = { hour: 'numeric', minute: '2-digit' };
    return d.toLocaleTimeString([], opts);
}

function updateTitle(text) {
    if (typeof text === 'string' && text.length) {
        nameEl.innerHTML = text;
    } else {
        if (isOpen) {
            nameEl.innerHTML = 'Elon (Online)';
            onlineDot.classList.remove('offline'); onlineDot.classList.add('online');
        } else {
            const lastSeenAt = loadLastSeen();
            const last = lastSeenAt ? formatTime(lastSeenAt) : 'away';
            nameEl.innerHTML = `Elon (Last seen ${last})`;
            onlineDot.classList.remove('online'); onlineDot.classList.add('offline');
        }
        if (!isOpen && unreadCount > 0) {
            nameEl.innerHTML += ` — ${unreadCount} new`;
        }
    }
}

function startInactivityTimer() { clearTimeout(inactivityTimer); inactivityTimer = setTimeout(closeChat, AUTO_CLOSE_MS); }
function resetInactivity() { startInactivityTimer(); }

function showTypingBubbleSmall() { typingBubble.style.display = 'block'; typingBubble.setAttribute('aria-hidden','false'); }
function hideTypingBubbleSmall() { typingBubble.style.display = 'none'; typingBubble.setAttribute('aria-hidden','true'); }
function showLoader() { loaderEl.style.display = 'block'; loaderEl.setAttribute('aria-hidden','false'); }
function hideLoader() { loaderEl.style.display = 'none'; loaderEl.setAttribute('aria-hidden','true'); }

function generateId() { return 'm_' + Date.now() + '_' + Math.random().toString(36).slice(2,8); }

/***** RENDERING *****/
function renderMessages() {
    messagesEl.innerHTML = '';
    if (observer) observer.disconnect();

    observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            const el = entry.target;
            const id = el.getAttribute('data-id');
            if (entry.isIntersecting) {
                markMessageSeen(id);
            }
        });
    }, { root: messagesEl, threshold: 0.9 });

    messages.forEach(msg => {
        const node = document.createElement('div');
        node.className = 'chat-msg ' + (msg.sender === 'user' ? 'chat-user' : 'chat-bot');
        node.setAttribute('data-id', msg.id);

        const safeText = escapeHtml(msg.text).replace(/\r?\n/g, '<br>');
        let meta = `<div class="meta"><div class="time">${formatTime(msg.time)}</div>`;

        if (msg.sender === 'user') {
            if (msg.status === 'sent') {
                meta += `<div class="ticks" aria-hidden="true">✓</div>`;
            } else if (msg.status === 'delivered') {
                meta += `<div class="ticks" aria-hidden="true">✓✓</div>`;
            } else if (msg.status === 'seen') {
                meta += `<div class="ticks tick-seen" aria-hidden="true">✓✓</div>`;
                if (msg.seenAt) meta += `<div class="time">Seen ${formatTime(msg.seenAt)}</div>`;
            }
        } else {
            if (msg.status === 'seen' && msg.seenAt) {
                meta += `<div class="time">Seen ${formatTime(msg.seenAt)}</div>`;
            }
        }

        meta += '</div>';

        node.innerHTML = `<div class="text">${safeText}</div>` + meta;
        messagesEl.appendChild(node);

        observer.observe(node);
    });

    messagesEl.scrollTop = messagesEl.scrollHeight;
}

/***** MESSAGE STATE HELPERS *****/
function addMessageToState(text, sender, opts = {}) {
    const now = Date.now();
    const m = {
        id: opts.id || generateId(),
        sender: sender,
        text: text,
        time: now,
        status: opts.status || 'sent',
        seenAt: opts.seenAt || null
    };
    messages.push(m);
    persistMessages();
    renderMessages();
    return m;
}

function updateMessageStatus(id, status, seenAt = null) {
    const msg = messages.find(m => m.id === id);
    if (!msg) return;
    const order = { 'sent': 0, 'delivered': 1, 'seen': 2 };
    if (order[status] <= order[msg.status]) return;
    msg.status = status;
    if (seenAt) msg.seenAt = seenAt;
    persistMessages();
    renderMessages();
}

function markMessageDelivered(id) { updateMessageStatus(id, 'delivered'); }
function markMessageSeen(id) { updateMessageStatus(id, 'seen', Date.now()); }

/***** OPEN/CLOSE/TOGGLE *****/
function openChat() {
    box.classList.add('open');
    box.setAttribute('aria-hidden','false');
    isOpen = true;
    unreadCount = 0;
    updateTitle();
    loadMessages();
    renderMessages();
    startInactivityTimer();
}

function closeChat() {
    box.classList.remove('open');
    box.setAttribute('aria-hidden','true');
    isOpen = false;
    clearTimeout(inactivityTimer);
    const now = Date.now();
    saveLastSeen(now);
    updateTitle();
}

function toggleChat() { isOpen ? closeChat() : openChat(); }

/***** EVENT HANDLERS *****/
fab.addEventListener('click', () => {
    toggleChat();
    if (firstOpen && isOpen) {
        firstOpen = false;
        try { localStorage.setItem(FIRSTOPEN_KEY, 'true'); } catch(e) {}
        const g = addMessageToState("👋 Hi, I'm Elon! How can I help you today?", 'bot', { status: 'sent' });
        setTimeout(() => { if (isOpen) markMessageDelivered(g.id); }, 120);
        setTimeout(() => { if (isOpen) markMessageSeen(g.id); }, 420);
        inputEl.focus();
    } else if (isOpen) {
        inputEl.focus();
    }
});
fab.addEventListener('keydown', e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); fab.click(); } });

closeChatBtn.addEventListener('click', () => closeChat());

darkToggle.addEventListener('click', () => {
    isDark = !isDark;
    applyTheme();
    saveTheme();
});

clearChatBtn.addEventListener('click', () => {
    if (!confirm('Clear chat history?')) return;
    clearChatHistory();
});

// ESC to close
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        if (isOpen) closeChat();
    }
});

// input send
sendBtn.addEventListener('click', async () => { await handleSend(); });
inputEl.addEventListener('keydown', async (e) => {
    if (e.key === 'Enter') {
        e.preventDefault();
        await handleSend();
    }
});

messagesEl.addEventListener('scroll', resetInactivity);
inputEl.addEventListener('input', resetInactivity);

function updateTitleOnTyping(on) {
    if (on) updateTitle('Elon is typing...');
    else updateTitle();
}

/***** BACKEND SEND — Option B typing flow *****/
async function handleSend() {
    const msg = inputEl.value.trim();
    if (!msg) return;

    // cancel any ongoing typing simulation
    cancelTyping = true;
    clearInterval(typingInterval);

    // add user message
    const userMsg = addMessageToState(msg, 'user', { status: 'sent' });
    inputEl.value = '';
    resetInactivity();

    // STEP 1: show small typing bubble while awaiting server
    showTypingBubbleSmall();

    try {
        // perform real backend call (use your Auth.fetchWithAuth wrapper)
        const resp = await Auth.fetchWithAuth('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: msg })
        });

        // hide small typing bubble as soon as network responded
        hideTypingBubbleSmall();

        const data = await resp.json();

        const reply = (data && data.reply) ? data.reply : "I'm having trouble answering right now.";

        // mark user's message delivered
        markMessageDelivered(userMsg.id);

        // STEP 2: show big loader briefly (simulate thinking)
        showLoader();
        updateTitleOnTyping(true);
        await new Promise(r => setTimeout(r, 420));
        hideLoader();

        // STEP 3: add bot message and type out
        updateTitleOnTyping(true);
        showTypingBubbleSmall(); // small bubble can be shown while typing simulation happens
        const botMsg = addMessageToState(reply, 'bot', { status: 'delivered' });
        await appendMessageTyped(botMsg.id, reply);
        updateTitleOnTyping(false);
        hideTypingBubbleSmall();

        // proactively mark delivered
        markMessageDelivered(botMsg.id);

        // If chat open and visible, observer will handle seen; otherwise it's delivered only

    } catch (err) {
        hideTypingBubbleSmall();
        hideLoader();
        addMessageToState('❌ Error contacting server.', 'bot', { status: 'sent' });
        console.error('Chatbot send error:', err);
    } finally {
        resetInactivity();
    }
}

/***** Type-out helper for existing bot message element *****/
function appendMessageTyped(id, text) {
    // find DOM node for this message
    const node = messagesEl.querySelector(`[data-id="${id}"]`);
    if (!node) {
        // If node doesn't exist yet (shouldn't happen), just ensure it's in state and render
        renderMessages();
        return Promise.resolve();
    }
    const textDiv = node.querySelector('.text');
    if (!textDiv) return Promise.resolve();

    textDiv.innerHTML = '';

    let i = 0;
    cancelTyping = false;
    return new Promise(resolve => {
        typingInterval = setInterval(() => {
            if (cancelTyping) {
                clearInterval(typingInterval);
                textDiv.innerHTML += escapeHtml(text).slice(i).replace(/\r?\n/g, '<br>');
                persistMessages();
                resolve();
                return;
            }
            textDiv.innerHTML += escapeHtml(text.charAt(i));
            i++;
            messagesEl.scrollTop = messagesEl.scrollHeight;
            if (i >= text.length) {
                clearInterval(typingInterval);
                persistMessages();
                resolve();
            }
        }, TYPING_SPEED);
    });
}

/***** RESIZER (bottom-right drag) *****/
(function setupResizer() {
    let dragging = false;
    let startX, startY, startW, startH;

    resizer.addEventListener('pointerdown', (e) => {
        e.preventDefault();
        dragging = true;
        startX = e.clientX; startY = e.clientY;
        const rect = box.getBoundingClientRect();
        startW = rect.width; startH = rect.height;
        resizer.setPointerCapture && resizer.setPointerCapture(e.pointerId);
    });

    document.addEventListener('pointermove', (e) => {
        if (!dragging) return;
        let newW = startW + (startX - e.clientX) * -1;
        let newH = startH + (startY - e.clientY) * -1;
        newW = Math.max(MIN_WIDTH, newW);
        newH = Math.max(MIN_HEIGHT, newH);
        box.style.width = newW + 'px';
        box.style.height = newH + 'px';
    });

    document.addEventListener('pointerup', (e) => {
        if (dragging) {
            dragging = false;
            resizer.releasePointerCapture && resizer.releasePointerCapture(e.pointerId);
        }
    });

    // keyboard accessibility: arrow keys while resizer focused
    resizer.tabIndex = 0;
    resizer.addEventListener('keydown', (e) => {
        const step = 12;
        const rect = box.getBoundingClientRect();
        let w = rect.width, h = rect.height;
        if (e.key === 'ArrowUp') h = Math.max(MIN_HEIGHT, h - step);
        if (e.key === 'ArrowDown') h = h + step;
        if (e.key === 'ArrowLeft') w = Math.max(MIN_WIDTH, w - step);
        if (e.key === 'ArrowRight') w = w + step;
        box.style.width = w + 'px'; box.style.height = h + 'px';
    });
})();

/***** OBSERVER: mark messages seen when in view *****/
function markMessageSeen(id) {
    if (!isOpen) return;
    const msg = messages.find(m => m.id === id);
    if (!msg) return;
    if (msg.status === 'seen') return;
    // mark as seen
    updateMessageStatus(id, 'seen', Date.now());
    // update lastSeen for this user
    saveLastSeen(Date.now());
    // optional: send seen event to backend (uncomment and implement endpoint)
    // fetch('/api/chat/seen', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ id, userId: USER_ID }) }).catch(()=>{});
}

/***** INIT *****/
// compute user-specific keys and load per-user state
computeKeysForUser();
enforceDailyReset();

// restore theme and history
applyTheme();
loadMessages();

// set firstOpen based on per-user flag
try {
    firstOpen = !(localStorage.getItem(FIRSTOPEN_KEY) === 'true');
} catch (e) { firstOpen = true; }

// initial title update
updateTitle();

// render loaded messages
renderMessages();

// Expose lightweight API
window.ElonChat = {
    open: openChat,
    close: closeChat,
    toggle: toggleChat,
    clearHistory: clearChatHistory,
    setDark: (v) => { isDark = !!v; applyTheme(); saveTheme(); },
    getUserId: () => USER_ID,
    // debug helper: reset all data for current user
    _resetUserStorage: () => {
        try {
            localStorage.removeItem(STORAGE_KEY);
            localStorage.removeItem(LASTSEEN_KEY);
            localStorage.removeItem(FIRSTOPEN_KEY);
            messages = [];
            renderMessages();
        } catch (e) {}
    }
};

})();
