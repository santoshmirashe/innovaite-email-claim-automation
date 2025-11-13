(function () {

    const CHAT_AUTO_CLOSE_MS = 60000; // 10 seconds
    let inactivityTimer = null;
    let isOpen = false;
    let firstOpen = true; // first-time greeting only once

    // ---------------- CHAT BUTTON + WIDGET HTML ----------------
    const chatbotHTML = `
    <style>
        #chatbot-fab {
            position: fixed;
            bottom: 20px;
            right: 20px;
            width: 58px;
            height: 58px;
            background: #2563eb;
            border-radius: 50%;
            box-shadow: 0 3px 12px rgba(0,0,0,0.2);
            display: flex;
            justify-content: center;
            align-items: center;
            color: white;
            cursor: pointer;
            font-size: 26px;
            z-index: 99999;
            transition: transform 0.2s ease;
        }

        #chatbot-fab:hover {
            transform: scale(1.1);
        }

        #chatbot-box {
            position: fixed;
            bottom: 90px;
            right: 20px;
            width: 330px;
            height: 420px;
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.18);
            display: none;
            flex-direction: column;
            z-index: 99999;
            overflow: hidden;
            animation: fadeIn 0.2s;
        }

        @keyframes fadeIn {
            from{opacity:0; transform:translateY(10px);}
            to{opacity:1; transform:translateY(0);}
        }

        #chatbot-header {
            background: #2563eb;
            color: white;
            padding: 12px;
            font-size: 15px;
            font-weight: bold;
            text-align: center;
        }

        #chatbot-messages {
            flex: 1;
            padding: 10px;
            overflow-y: auto;
            background: #f9fafb;
        }

        .chat-msg {
            margin: 6px 0;
            padding: 8px 12px;
            border-radius: 10px;
            max-width: 80%;
            line-height: 1.4;
        }

        .chat-user {
            background: #dbeafe;
            margin-left: auto;
        }

        .chat-bot {
            background: #e5e7eb;
            margin-right: auto;
        }

        #chatbot-input-area {
            display: flex;
            border-top: 1px solid #ddd;
        }

        #chatbot-input {
            flex: 1;
            border: none;
            padding: 10px;
            font-size: 14px;
            outline: none;
        }

        #chatbot-send {
            background: #2563eb;
            color: white;
            border: none;
            padding: 10px 15px;
            cursor: pointer;
            transition: background 0.2s;
        }

        #chatbot-send:hover {
            background: #1e40af;
        }

        #chatbot-loader {
            display: none;
            text-align: center;
            padding: 6px;
            color: #2563eb;
            font-size: 12px;
        }
    </style>

    <div id="chatbot-fab" title="Chat Support">💬</div>

    <div id="chatbot-box">
        <div id="chatbot-header">Elon (Support Bot)</div>
        <div id="chatbot-messages"></div>
        <div id="chatbot-loader">Thinking...</div>
        <div id="chatbot-input-area">
            <input id="chatbot-input" type="text" placeholder="Ask something..." />
            <button id="chatbot-send">➤</button>
        </div>
    </div>
    `;

    document.getElementById("chatbot-container").innerHTML = chatbotHTML;

    // ---------------- DOM ELEMENTS ----------------
    const fab = document.getElementById("chatbot-fab");
    const box = document.getElementById("chatbot-box");
    const messagesBox = document.getElementById("chatbot-messages");
    const input = document.getElementById("chatbot-input");
    const sendBtn = document.getElementById("chatbot-send");
    const loader = document.getElementById("chatbot-loader");

    // ---------------- HELPERS ----------------
    function addMessage(text, sender) {
        const div = document.createElement("div");
        div.className = `chat-msg ${sender}`;
        div.innerHTML = text.replace(/\n/g, "<br>");
        messagesBox.appendChild(div);
        messagesBox.scrollTop = messagesBox.scrollHeight;
    }

    function greetOnFirstOpen() {
        addMessage("👋 Hi, I'm <b>Elon</b>!<br>How can I help you today?", "chat-bot");
    }

    function startAutoCloseTimer() {
        clearTimeout(inactivityTimer);
        inactivityTimer = setTimeout(closeChat, CHAT_AUTO_CLOSE_MS);
    }

    function resetTimerOnActivity() {
        startAutoCloseTimer();
    }

    function openChat() {
        box.style.display = "flex";
        isOpen = true;

        if (firstOpen) {
            greetOnFirstOpen();
            firstOpen = false;
        }

        startAutoCloseTimer();
    }

    function closeChat() {
        box.style.display = "none";
        isOpen = false;
        clearTimeout(inactivityTimer);
    }

    function toggleChat() {
        if (isOpen) closeChat();
        else openChat();
    }

    function showLoader() {
        loader.style.display = "block";
    }

    function hideLoader() {
        loader.style.display = "none";
    }

    // ---------------- SEND MESSAGE TO BACKEND ----------------
    async function sendMessage() {
        const msg = input.value.trim();
        if (!msg) return;

        addMessage(msg, "chat-user");
        input.value = "";

        resetTimerOnActivity();
        showLoader();

        try {
            const resp = await Auth.fetchWithAuth("/api/chat", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ message: msg })
            });

            const data = await resp.json();
            hideLoader();

            if (data.reply) {
                addMessage(data.reply, "chat-bot");
            } else {
                addMessage("⚠️ No response from server.", "chat-bot");
            }

        } catch (e) {
            hideLoader();
            addMessage("❌ Something went wrong.", "chat-bot");
            console.error(e);
        }

        resetTimerOnActivity();
    }

    // ---------------- EVENTS ----------------
    fab.addEventListener("click", toggleChat);
    sendBtn.addEventListener("click", sendMessage);

    input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") sendMessage();
    });

    messagesBox.addEventListener("scroll", resetTimerOnActivity);
    input.addEventListener("input", resetTimerOnActivity);

})();
