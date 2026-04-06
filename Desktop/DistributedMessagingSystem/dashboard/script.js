document.addEventListener('DOMContentLoaded', () => {
    // --- State & Config ---
    let state = {
        primaryNodePort: "3001",
        currentClient: "Client A",
        messageCount: 0,
        knownMessages: new Set(),
        serverStatuses: { 3001: false, 3002: false, 3003: false },
        leaderId: null
    };

    // --- DOM Elements ---
    const chatWindow = document.getElementById('chat-window');
    const messageInput = document.getElementById('message-input');
    const sendBtn = document.getElementById('send-btn');
    const clientSelect = document.getElementById('clientSelect');
    const nodeSelect = document.getElementById('nodeSelect');
    const logsWindow = document.getElementById('logs-window');
    const clearLogsBtn = document.getElementById('clear-logs');
    const msgCountEl = document.getElementById('msg-count');

    // --- Core communication ---

    async function apiRequest(port, endpoint, method = 'GET', body = null) {
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 2000);
            
            const options = { method, signal: controller.signal };
            if (body) options.body = body;

            const response = await fetch(`http://localhost:${port}${endpoint}`, options);
            clearTimeout(timeoutId);
            
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return await response.json();
        } catch (err) {
            return null; // Silent failure for polling
        }
    }

    async function updateSystemStatus() {
        const ports = [3001, 3002, 3003];
        let anyLeaderFound = false;

        for (const port of ports) {
            const data = await apiRequest(port, '/status');
            const card = document.getElementById(`server-${port % 10}`);
            const indicator = card.querySelector('.status-indicator');
            const label = card.querySelector('.status-label');
            const badge = card.querySelector('.leader-badge');

            if (data) {
                // Node is ONLINE
                if (!state.serverStatuses[port]) {
                    addLog(`Connection established with Server ${port % 10}.`, 'success');
                    state.serverStatuses[port] = true;
                }
                indicator.className = 'status-indicator online';
                label.textContent = 'Online';
                
                if (data.isLeader) {
                    badge.classList.remove('hidden');
                    state.leaderId = port % 10;
                    anyLeaderFound = true;
                } else {
                    badge.classList.add('hidden');
                }
            } else {
                // Node is OFFLINE
                if (state.serverStatuses[port]) {
                    addLog(`Connection lost to Server ${port % 10}.`, 'failure');
                    state.serverStatuses[port] = false;
                }
                indicator.className = 'status-indicator offline';
                label.textContent = 'Offline';
                badge.classList.add('hidden');
            }
        }
    }

    async function syncMessages() {
        // Fetch from the primary node the user is "connected" to
        const messages = await apiRequest(state.primaryNodePort, '/messages');
        if (!messages) return;

        messages.forEach(msg => {
            const key = `${msg.sender}-${msg.timestamp}-${msg.content}`;
            if (!state.knownMessages.has(key)) {
                state.knownMessages.add(key);
                appendMessageUI(msg.sender, msg.content, msg.sender === state.currentClient, msg.timestamp);
            }
        });
    }

    function appendMessageUI(sender, text, isMe, timestampRaw) {
        const emptyState = chatWindow.querySelector('.empty-state');
        if (emptyState) emptyState.remove();

        const timeString = new Date(timestampRaw).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        const msgDiv = document.createElement('div');
        msgDiv.className = `message ${isMe ? 'me' : 'other'}`;
        
        const routedBy = state.leaderId ? `Server ${state.leaderId}` : "Sync";

        msgDiv.innerHTML = `
            <div class="message-info">
                <span class="sender-name">${sender}</span>
                <span class="timestamp">${timeString}</span>
            </div>
            <div class="message-content">${text}</div>
            <span class="server-routing">via ${routedBy}</span>
        `;
        
        chatWindow.appendChild(msgDiv);
        chatWindow.scrollTop = chatWindow.scrollHeight;
        
        state.messageCount++;
        msgCountEl.textContent = state.messageCount;
    }

    async function handleSend() {
        const text = messageInput.value.trim();
        if (!text) return;

        addLog(`Sending message to Node ${state.primaryNodePort}...`, 'system');
        const body = `${state.currentClient}|${text}`;
        const result = await apiRequest(state.primaryNodePort, '/send', 'POST', body);

        if (result && result.status === 'delivered') {
            messageInput.value = "";
            addLog("Message submitted to cluster consensus.", "success");
        } else {
            addLog("Failed to reach consensus server. Check if primary node is online.", "failure");
        }
    }

    function addLog(text, type = 'system') {
        const timestamp = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        const logEntry = document.createElement('div');
        logEntry.className = `log-entry ${type}`;
        logEntry.textContent = `[${timestamp}] ${text}`;
        logsWindow.appendChild(logEntry);
        logsWindow.scrollTop = logsWindow.scrollHeight;
    }

    // --- Events ---

    sendBtn.addEventListener('click', handleSend);
    messageInput.addEventListener('keypress', (e) => { if (e.key === 'Enter') handleSend(); });

    clientSelect.addEventListener('change', (e) => {
        state.currentClient = e.target.value;
        addLog(`Active Client context: ${state.currentClient}`, 'system');
    });

    nodeSelect.addEventListener('change', (e) => {
        state.primaryNodePort = e.target.value;
        addLog(`Switched primary gateway to Node at Port ${state.primaryNodePort}`, 'warning');
        state.knownMessages.clear();
        chatWindow.innerHTML = '<div class="empty-state"><p>Loading history...</p></div>';
    });

    clearLogsBtn.addEventListener('click', () => logsWindow.innerHTML = "");

    // --- Poll Intervals ---
    setInterval(updateSystemStatus, 2000);
    setInterval(syncMessages, 1000);

    // Initial check
    updateSystemStatus();
    addLog("Web UI connected to local Java cluster.", "success");
});
