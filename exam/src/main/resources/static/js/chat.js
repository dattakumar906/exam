// Chatbot knowledge base
const chatbotResponses = {
    'add questions': 'To add questions: \n1. Click on "Add question" in the navigation menu\n2. Fill in the question details\n3. Select the topic and difficulty\n4. Add answer options\n5. Mark the correct answer\n6. Click Save',
    
    'select topic': 'To select a topic:\n1. Use the "Select Topic" dropdown menu\n2. Choose from available subjects\n3. The system will automatically show how many questions are available',
    
    'duration': 'Exam duration tips:\n• Beginner: 30 minutes (recommended)\n• Intermediate: 45 minutes\n• Advanced: 60 minutes\nAdjust based on number of questions (1.5-2 minutes per question)',
    
    'start exam': 'To start an exam:\n1. Select your topic\n2. Choose number of questions\n3. Set duration\n4. Select difficulty level\n5. Click "Generate Exam Link"\nThe exam will begin immediately after',
    
    'difficulty': 'Difficulty levels:\n• Beginner: Basic concepts\n• Intermediate: Applied knowledge\n• Advanced: Complex problems\nChoose based on your preparation level',
    
    'help': 'I can help you with:\n• Adding questions\n• Selecting topics\n• Setting duration\n• Starting exam\n• Choosing difficulty\nWhat would you like to know?'
};

// Initialize WebSocket connection
let ws;
let isConnected = false;

function initializeWebSocket() {
    ws = new WebSocket('wss://your-websocket-server');

    ws.onopen = () => {
        isConnected = true;
        updateConnectionStatus('Connected');
        addSystemMessage('Hello! I\'m your exam portal assistant. How can I help you today?');
    };

    ws.onclose = () => {
        isConnected = false;
        updateConnectionStatus('Disconnected');
        setTimeout(initializeWebSocket, 3000);
    };

    ws.onmessage = (event) => {
        const response = JSON.parse(event.data);
        handleIncomingMessage(response);
    };
}

function updateConnectionStatus(status) {
    const statusIndicator = document.querySelector('.chat-status-indicator');
    const statusText = document.querySelector('.chat-status-text');
    
    if (status === 'Connected') {
        statusIndicator.classList.remove('bg-red-500');
        statusIndicator.classList.add('bg-green-500');
        statusText.textContent = 'Online';
    } else {
        statusIndicator.classList.remove('bg-green-500');
        statusIndicator.classList.add('bg-red-500');
        statusText.textContent = 'Offline';
    }
}

function handleIncomingMessage(response) {
    if (response.type === 'bot') {
        addBotMessage(response.message);
    }
}

function addUserMessage(message) {
    const chatMessages = document.querySelector('.chat-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'chat-message user-message';
    messageDiv.innerHTML = `
        <div class="message-content">
            <p>${message}</p>
            <span class="message-time">${new Date().toLocaleTimeString()}</span>
        </div>
    `;
    chatMessages.appendChild(messageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function addBotMessage(message) {
    const chatMessages = document.querySelector('.chat-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'chat-message bot-message';
    messageDiv.innerHTML = `
        <div class="message-content">
            <p>${message.replace(/\n/g, '<br>')}</p>
            <span class="message-time">${new Date().toLocaleTimeString()}</span>
        </div>
    `;
    chatMessages.appendChild(messageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function addSystemMessage(message) {
    const chatMessages = document.querySelector('.chat-messages');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'chat-message system-message';
    messageDiv.innerHTML = `
        <div class="message-content">
            <p>${message}</p>
        </div>
    `;
    chatMessages.appendChild(messageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

function processUserMessage(message) {
    addUserMessage(message);
    
    // Simple keyword matching
    const lowerMessage = message.toLowerCase();
    let response = '';
    
    if (lowerMessage.includes('add') && lowerMessage.includes('question')) {
        response = chatbotResponses['add questions'];
    } else if (lowerMessage.includes('select') && lowerMessage.includes('topic')) {
        response = chatbotResponses['select topic'];
    } else if (lowerMessage.includes('duration') || lowerMessage.includes('time')) {
        response = chatbotResponses['duration'];
    } else if (lowerMessage.includes('start') && lowerMessage.includes('exam')) {
        response = chatbotResponses['start exam'];
    } else if (lowerMessage.includes('difficulty')) {
        response = chatbotResponses['difficulty'];
    } else if (lowerMessage.includes('help')) {
        response = chatbotResponses['help'];
    } else {
        response = "I'm not sure about that. You can ask me about:\n• Adding questions\n• Selecting topics\n• Setting duration\n• Starting exam\n• Choosing difficulty";
    }
    
    setTimeout(() => addBotMessage(response), 500);
}

// Initialize chat when document is ready
document.addEventListener('DOMContentLoaded', () => {
    initializeWebSocket();
    
    const chatInput = document.getElementById('chat-input');
    const sendButton = document.getElementById('chat-send-button');
    const toggleButton = document.getElementById('chat-toggle');
    const chatWidget = document.getElementById('chat-widget');
    
    sendButton.addEventListener('click', () => {
        const message = chatInput.value.trim();
        if (message) {
            processUserMessage(message);
            chatInput.value = '';
        }
    });
    
    chatInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendButton.click();
        }
    });
    
    toggleButton.addEventListener('click', () => {
        chatWidget.classList.toggle('chat-minimized');
    });
});