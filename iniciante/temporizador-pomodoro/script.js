// Estado do Pomodoro
let timerInterval = null;
let timeLeft = 25 * 60; // 25 minutos em segundos
let totalTime = 25 * 60;
let isRunning = false;
let currentMode = 'focus'; // 'focus', 'shortBreak', 'longBreak'
let focusSessionsCompleted = 0;
let totalCycles = 0;

// Elementos DOM
const timeDisplay = document.getElementById('timeDisplay');
const modeLabel = document.getElementById('modeLabel');
const progressBar = document.getElementById('progressBar');
const startBtn = document.getElementById('startBtn');
const pauseBtn = document.getElementById('pauseBtn');
const resetBtn = document.getElementById('resetBtn');
const cyclesCount = document.getElementById('cyclesCount');
const focusCount = document.getElementById('focusCount');
const focusTimeInput = document.getElementById('focusTime');
const shortBreakTimeInput = document.getElementById('shortBreakTime');
const longBreakTimeInput = document.getElementById('longBreakTime');

// Configurações de tempo (em minutos)
const config = {
    focusTime: 25,
    shortBreakTime: 5,
    longBreakTime: 15
};

// Inicialização
function init() {
    updateDisplay();
    updateProgress();
    loadSettings();
}

// Formatar tempo (segundos para MM:SS)
function formatTime(seconds) {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

// Atualizar display
function updateDisplay() {
    timeDisplay.textContent = formatTime(timeLeft);

    const modeLabels = {
        focus: '🎯 Foco',
        shortBreak: '☕ Pausa Curta',
        longBreak: '🌴 Pausa Longa'
    };
    modeLabel.textContent = modeLabels[currentMode];
}

// Atualizar barra de progresso
function updateProgress() {
    const percentage = (timeLeft / totalTime) * 100;
    progressBar.style.width = `${percentage}%`;
}

// Iniciar timer
function startTimer() {
    if (isRunning) return;

    isRunning = true;
    startBtn.disabled = true;
    pauseBtn.disabled = false;

    timerInterval = setInterval(() => {
        timeLeft--;
        updateDisplay();
        updateProgress();

        if (timeLeft <= 0) {
            completeSession();
        }
    }, 1000);
}

// Pausar timer
function pauseTimer() {
    isRunning = false;
    clearInterval(timerInterval);
    startBtn.disabled = false;
    pauseBtn.disabled = true;
}

// Resetar timer
function resetTimer() {
    pauseTimer();
    timeLeft = getTimeForMode(currentMode);
    totalTime = timeLeft;
    updateDisplay();
    updateProgress();
}

// Completar sessão
function completeSession() {
    pauseTimer();
    playSound();
    showNotification();

    if (currentMode === 'focus') {
        focusSessionsCompleted++;
        focusCount.textContent = focusSessionsCompleted;

        // Após 4 sessões de foco, pausa longa
        if (focusSessionsCompleted % 4 === 0) {
            switchMode('longBreak');
            totalCycles++;
            cyclesCount.textContent = totalCycles;
        } else {
            switchMode('shortBreak');
        }
    } else {
        switchMode('focus');
    }
}

// Trocar modo
function switchMode(mode) {
    currentMode = mode;
    timeLeft = getTimeForMode(mode);
    totalTime = timeLeft;
    updateDisplay();
    updateProgress();
}

// Obter tempo para o modo
function getTimeForMode(mode) {
    const times = {
        focus: config.focusTime * 60,
        shortBreak: config.shortBreakTime * 60,
        longBreak: config.longBreakTime * 60
    };
    return times[mode];
}

// Tocar som de notificação
function playSound() {
    // Criar um beep simples usando Web Audio API
    const audioContext = new (window.AudioContext || window.webkitAudioContext)();
    const oscillator = audioContext.createOscillator();
    const gainNode = audioContext.createGain();

    oscillator.connect(gainNode);
    gainNode.connect(audioContext.destination);

    oscillator.frequency.value = 800;
    oscillator.type = 'sine';

    gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
    gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);

    oscillator.start(audioContext.currentTime);
    oscillator.stop(audioContext.currentTime + 0.5);
}

// Mostrar notificação
function showNotification() {
    if ('Notification' in window && Notification.permission === 'granted') {
        const messages = {
            focus: 'Tempo de pausa! Descanse um pouco. ☕',
            shortBreak: 'Pausa acabou! Hora de focar! 🎯',
            longBreak: 'Pausa longa acabou! Pronto para mais? 💪'
        };

        new Notification('⏰ Pomodoro Timer', {
            body: messages[currentMode],
            icon: '🍅'
        });
    } else {
        alert('Sessão completa! ' + (currentMode === 'focus' ? 'Hora de pausar!' : 'Hora de focar!'));
    }
}

// Solicitar permissão para notificações
function requestNotificationPermission() {
    if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission();
    }
}

// Salvar configurações
function saveSettings() {
    config.focusTime = parseInt(focusTimeInput.value);
    config.shortBreakTime = parseInt(shortBreakTimeInput.value);
    config.longBreakTime = parseInt(longBreakTimeInput.value);

    localStorage.setItem('pomodoroConfig', JSON.stringify(config));

    if (currentMode === 'focus') {
        resetTimer();
    }
}

// Carregar configurações
function loadSettings() {
    const saved = localStorage.getItem('pomodoroConfig');
    if (saved) {
        const savedConfig = JSON.parse(saved);
        config.focusTime = savedConfig.focusTime;
        config.shortBreakTime = savedConfig.shortBreakTime;
        config.longBreakTime = savedConfig.longBreakTime;

        focusTimeInput.value = config.focusTime;
        shortBreakTimeInput.value = config.shortBreakTime;
        longBreakTimeInput.value = config.longBreakTime;
    }
}

// Event Listeners
startBtn.addEventListener('click', startTimer);
pauseBtn.addEventListener('click', pauseTimer);
resetBtn.addEventListener('click', resetTimer);

focusTimeInput.addEventListener('change', saveSettings);
shortBreakTimeInput.addEventListener('change', saveSettings);
longBreakTimeInput.addEventListener('change', saveSettings);

// Atalhos de teclado
document.addEventListener('keydown', (e) => {
    if (e.code === 'Space') {
        e.preventDefault();
        if (isRunning) {
            pauseTimer();
        } else {
            startTimer();
        }
    } else if (e.code === 'KeyR') {
        resetTimer();
    }
});

// Inicializar
init();
requestNotificationPermission();

console.log('🍅 Pomodoro Timer carregado!');
console.log('Atalhos: Espaço = Iniciar/Pausar | R = Resetar');
