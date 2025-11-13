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
        #chatbot-title { flex:1; text-align:left; }
        #chatbot-controls { display:flex; gap:8px; align-items:center; }

        .control-btn {
            background: transparent; border: none; color: white; cursor: pointer; padding:6px;
            border-radius:6px; font-size:14px;
        }
        .control-btn:focus { outline:2px solid rgba(255,255,255,0.18); }

        /* container for messages */
        #chatbot-messages {
            flex:1; padding:12px; overflow-y:auto; background: var(--panel);
            color: var(--text);
        }

        .chat-msg { margin:8px 0; padding:8px 12px; border-radius:10px; max-width:80%; line-height:1.35; word-break:break-word; }
        .chat-user { background: var(--user-bg); margin-left:auto; color: var(--text); }
        .chat-bot  { background: var(--bot-bg); margin-right:auto; color: var(--text); }

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

    <div id="chatbot-fab" aria-label="Chat with Elon" role="button" tabindex="0">💬</div>

    <div id="chatbot-box" role="dialog" aria-label="Chat with Elon" aria-hidden="true">
        <div id="chatbot-header">
            <div id="chatbot-title">Elon (Support Bot)</div>
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

    document.getElementById("chatbot-container").innerHTML = html;

    /***** ELEMENT REFS *****/
    const rootEl = document.getElementById("chatbot-container").closest('body') || document.body;
    const fab = document.getElementById("chatbot-fab");
    const box = document.getElementById("chatbot-box");
    const titleEl = document.getElementById("chatbot-title");
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
      return String(unsafe).replace(/[&<>"'`=\/]/g, function (s) {
        return ({
          '&': '&amp;',
          '<': '&lt;',
          '>': '&gt;',
          '"': '&quot;',
          "'": '&#39;',
          '/': '&#x2F;',
          '`': '&#x60;',
          '=': '&#x3D;'
        })[s];
      });
    }

    function saveTheme() { localStorage.setItem('elonChatDark', isDark ? 'true' : 'false'); }
    function applyTheme() {
        if (isDark) document.documentElement.classList.add('elon-dark');
        else document.documentElement.classList.remove('elon-dark');
        darkToggle.setAttribute('aria-pressed', isDark ? 'true' : 'false');
    }
    applyTheme();

    function saveChatHistory() {
        try { localStorage.setItem('elonChatHistory', messagesEl.innerHTML); } catch (e) { /* ignore */ }
    }
    function loadChatHistory() {
        const h = localStorage.getItem('elonChatHistory');
        if (h) { messagesEl.innerHTML = h; messagesEl.scrollTop = messagesEl.scrollHeight; }
    }
    function clearChatHistory() {
        messagesEl.innerHTML = ''; localStorage.removeItem('elonChatHistory'); unreadCount = 0; updateTitle();
    }

    function updateTitle(text) {
        if (typeof text === 'string' && text.length) {
            titleEl.innerHTML = text;
        } else {
            titleEl.innerHTML = `Elon (Support Bot)${!isOpen && unreadCount > 0 ? ` — ${unreadCount} new` : ''}`;
        }
    }

    function startInactivityTimer() { clearTimeout(inactivityTimer); inactivityTimer = setTimeout(closeChat, AUTO_CLOSE_MS); }
    function resetInactivity() { startInactivityTimer(); }

    function showTypingBubble() { typingBubble.style.display = 'block'; typingBubble.setAttribute('aria-hidden','false'); }
    function hideTypingBubble() { typingBubble.style.display = 'none'; typingBubble.setAttribute('aria-hidden','true'); }
    function showLoader() { loaderEl.style.display = 'block'; loaderEl.setAttribute('aria-hidden','false'); }
    function hideLoader() { loaderEl.style.display = 'none'; loaderEl.setAttribute('aria-hidden','true'); }

    /* add message and persist */
    function addMessage(text, sender, options = {}) {
        const div = document.createElement('div');
        div.className = 'chat-msg ' + (sender === 'user' ? 'chat-user' : 'chat-bot');
        div.innerHTML = text.replace(/\n/g, '<br>');
        messagesEl.appendChild(div);
        messagesEl.scrollTop = messagesEl.scrollHeight;
        saveChatHistory();

        if (!isOpen && sender === 'bot') {
            unreadCount++;
            updateTitle(); // show unread count
        }
        if (options.focus) messagesEl.focus();
    }

    /* type text char-by-char into messagesEl and persist */
    function appendMessageTyped(text) {
        // create container first
        const div = document.createElement('div');
        div.className = 'chat-msg chat-bot';
        messagesEl.appendChild(div);
        messagesEl.scrollTop = messagesEl.scrollHeight;

        let i = 0;
        cancelTyping = false;
        return new Promise(resolve => {
            typingInterval = setInterval(() => {
                if (cancelTyping) {
                    // abort: print remaining immediately
                    clearInterval(typingInterval);
                    div.innerHTML += text.slice(i);
                    messagesEl.scrollTop = messagesEl.scrollHeight;
                    saveChatHistory();
                    resolve();
                    return;
                }
                div.innerHTML += text.charAt(i);
                i++;
                messagesEl.scrollTop = messagesEl.scrollHeight;
                if (i >= text.length) {
                    clearInterval(typingInterval);
                    saveChatHistory();
                    resolve();
                }
            }, TYPING_SPEED);
        });
    }

    /***** OPEN/CLOSE/TOGGLE *****/
    function openChat() {
        box.classList.add('open');
        box.setAttribute('aria-hidden','false');
        isOpen = true;
        unreadCount = 0;
        updateTitle();
        loadChatHistory();
        startInactivityTimer();
    }

    function closeChat() {
        box.classList.remove('open');
        box.setAttribute('aria-hidden','true');
        isOpen = false;
        clearTimeout(inactivityTimer);
    }

    function toggleChat() { isOpen ? closeChat() : openChat(); }

    /***** EVENT HANDLERS *****/
    fab.addEventListener('click', () => {
        toggleChat();
        // on first open, show greeting message then focus input
        if (firstOpen && isOpen) {
            firstOpen = false;
            addMessage("👋 Hi, I'm <strong>Elon</strong>! How can I help you today?", 'bot', { focus:true });
        } else if (isOpen) {
            inputEl.focus();
        }
    });
    fab.addEventListener('keydown', e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); fab.click(); } });

    closeChatBtn.addEventListener('click', () => closeChat());

    // dark toggle
    darkToggle.addEventListener('click', () => {
        isDark = !isDark;
        applyTheme();
        saveTheme();
    });

    // clear chat
    clearChatBtn.addEventListener('click', () => {
        if (!confirm('Clear chat history?')) return;
        clearChatHistory();
    });

    // ESC to close
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            if (isOpen) closeChat();
            else {
                // if closed, ignore
            }
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
        else updateTitle(); // reset
    }

    async function handleSend() {
        const msg = inputEl.value.trim();
        if (!msg) return;
        // cancel any ongoing typing simulation so new message flows cleanly
        cancelTyping = true;
        clearInterval(typingInterval);

        // escape user input to avoid XSS
        addMessage(escapeHtml(msg), 'user');
        inputEl.value = '';
        resetInactivity();

        // show messenger typing bubble (small) while we await backend
        showTypingBubble();

        try {
            const resp = await Auth.fetchWithAuth('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: msg })
            });

            // hide typing bubble as soon as network responded
            hideTypingBubble();

            // read server JSON
            const data = await resp.json();

            // if server responds with bot name, could use it; but default to Elon
            const botName = (data && data.bot) ? data.bot : 'Elon';
            const reply = (data && data.reply) ? data.reply : "I'm having trouble answering right now.";

            // show big loader then typed text simulation for realism
            showLoader();
            updateTitleOnTyping(true);

            // small delay to simulate thinking then hide loader and type response
            await new Promise(r => setTimeout(r, 420));
            hideLoader();

            // now simulate typing char-by-char (show title and typing bubble)
            updateTitleOnTyping(true);
            showTypingBubble();
            await appendMessageTyped(reply);
            updateTitleOnTyping(false);
            hideTypingBubble();

        } catch (err) {
            hideTypingBubble();
            hideLoader();
            addMessage('❌ Error contacting server.', 'bot');
            console.error('Chatbot send error:', err);
        } finally {
            resetInactivity();
        }
    }

    // helper to show/hide typing bubble (small)
    function showTypingBubble() { typingBubble.style.display = 'block'; typingBubble.setAttribute('aria-hidden','false'); }
    function hideTypingBubble() { typingBubble.style.display = 'none'; typingBubble.setAttribute('aria-hidden','true'); }

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

    /***** INIT *****/
    // restore theme and history
    applyTheme();
    loadChatHistory();

    // update title initial
    updateTitle();

    // expose small API for debugging if needed
    window.ElonChat = {
        open: openChat,
        close: closeChat,
        toggle: toggleChat,
        clearHistory: clearChatHistory,
        setDark: (v) => { isDark = !!v; applyTheme(); saveTheme(); }
    };

})();
