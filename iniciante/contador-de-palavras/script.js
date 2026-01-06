// Elementos DOM
const textArea = document.getElementById('textArea');
const wordCount = document.getElementById('wordCount');
const charCount = document.getElementById('charCount');
const charNoSpaces = document.getElementById('charNoSpaces');
const lineCount = document.getElementById('lineCount');
const paragraphCount = document.getElementById('paragraphCount');
const readingTime = document.getElementById('readingTime');
const sentenceCount = document.getElementById('sentenceCount');
const vowelCount = document.getElementById('vowelCount');
const consonantCount = document.getElementById('consonantCount');
const longestWord = document.getElementById('longestWord');
const frequencyList = document.getElementById('frequencyList');
const clearBtn = document.getElementById('clearBtn');
const copyStatsBtn = document.getElementById('copyStatsBtn');

// Função principal de análise
function analyzeText() {
    const text = textArea.value;

    // Contagem básica
    const words = countWords(text);
    const chars = text.length;
    const charsNoSpace = text.replace(/\s/g, '').length;
    const lines = countLines(text);
    const paragraphs = countParagraphs(text);
    const sentences = countSentences(text);
    const readTime = calculateReadingTime(words);

    // Contagem avançada
    const vowels = countVowels(text);
    const consonants = countConsonants(text);
    const longest = findLongestWord(text);

    // Atualizar display
    wordCount.textContent = words;
    charCount.textContent = chars;
    charNoSpaces.textContent = charsNoSpace;
    lineCount.textContent = lines;
    paragraphCount.textContent = paragraphs;
    readingTime.textContent = readTime;
    sentenceCount.textContent = sentences;
    vowelCount.textContent = vowels;
    consonantCount.textContent = consonants;
    longestWord.textContent = longest || '-';

    // Frequência de palavras
    updateWordFrequency(text);
}

// Contar palavras
function countWords(text) {
    if (!text.trim()) return 0;
    // Remove múltiplos espaços e quebras de linha, depois conta palavras
    const words = text.trim().split(/\s+/);
    return words.filter(word => word.length > 0).length;
}

// Contar linhas
function countLines(text) {
    if (!text) return 0;
    return text.split('\n').length;
}

// Contar parágrafos
function countParagraphs(text) {
    if (!text.trim()) return 0;
    // Parágrafos são separados por linhas vazias
    const paragraphs = text.split(/\n\s*\n/);
    return paragraphs.filter(p => p.trim().length > 0).length;
}

// Contar frases
function countSentences(text) {
    if (!text.trim()) return 0;
    // Frases terminam com . ! ?
    const sentences = text.split(/[.!?]+/);
    return sentences.filter(s => s.trim().length > 0).length;
}

// Calcular tempo de leitura (assumindo 200 palavras/minuto)
function calculateReadingTime(words) {
    if (words === 0) return '0 min';
    const minutes = Math.ceil(words / 200);
    return minutes === 1 ? '1 min' : `${minutes} min`;
}

// Contar vogais
function countVowels(text) {
    const matches = text.match(/[aeiouáéíóúâêîôûãõàèìòù]/gi);
    return matches ? matches.length : 0;
}

// Contar consoantes
function countConsonants(text) {
    const matches = text.match(/[bcdfghjklmnpqrstvwxyzçñ]/gi);
    return matches ? matches.length : 0;
}

// Encontrar palavra mais longa
function findLongestWord(text) {
    if (!text.trim()) return '';
    const words = text.split(/\s+/);
    const cleanWords = words.map(w => w.replace(/[^\w]/g, ''));
    return cleanWords.reduce((longest, current) =>
        current.length > longest.length ? current : longest, '');
}

// Atualizar frequência de palavras
function updateWordFrequency(text) {
    if (!text.trim()) {
        frequencyList.innerHTML = '<p style="color: #999; text-align: center;">Nenhuma palavra ainda...</p>';
        return;
    }

    // Contar frequência
    const words = text.toLowerCase()
        .replace(/[^\w\sáéíóúâêîôûãõàèìòù]/g, '')
        .split(/\s+/)
        .filter(word => word.length > 2); // Ignorar palavras muito curtas

    const frequency = {};
    words.forEach(word => {
        frequency[word] = (frequency[word] || 0) + 1;
    });

    // Ordenar por frequência (top 10)
    const sorted = Object.entries(frequency)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 10);

    // Renderizar
    if (sorted.length === 0) {
        frequencyList.innerHTML = '<p style="color: #999; text-align: center;">Nenhuma palavra significativa...</p>';
        return;
    }

    frequencyList.innerHTML = sorted.map(([word, count]) => `
        <div class="frequency-item">
            <span class="frequency-word">${word}</span>
            <span class="frequency-count">${count}x</span>
        </div>
    `).join('');
}

// Limpar texto
function clearText() {
    textArea.value = '';
    analyzeText();
    textArea.focus();
}

// Copiar estatísticas
function copyStats() {
    const text = textArea.value;
    const stats = `
📊 ESTATÍSTICAS DO TEXTO

📝 Palavras: ${wordCount.textContent}
🔤 Caracteres: ${charCount.textContent}
📄 Sem Espaços: ${charNoSpaces.textContent}
📋 Linhas: ${lineCount.textContent}
📖 Parágrafos: ${paragraphCount.textContent}
⏱️ Tempo de Leitura: ${readingTime.textContent}

📊 DETALHES AVANÇADOS

Frases: ${sentenceCount.textContent}
Vogais: ${vowelCount.textContent}
Consoantes: ${consonantCount.textContent}
Palavra mais longa: ${longestWord.textContent}
    `.trim();

    navigator.clipboard.writeText(stats).then(() => {
        // Feedback visual
        const originalText = copyStatsBtn.textContent;
        copyStatsBtn.textContent = '✅ Copiado!';
        copyStatsBtn.style.background = '#4caf50';

        setTimeout(() => {
            copyStatsBtn.textContent = originalText;
            copyStatsBtn.style.background = '';
        }, 2000);
    }).catch(err => {
        alert('Erro ao copiar: ' + err);
    });
}

// Event Listeners
textArea.addEventListener('input', analyzeText);
clearBtn.addEventListener('click', clearText);
copyStatsBtn.addEventListener('click', copyStats);

// Atalhos de teclado
document.addEventListener('keydown', (e) => {
    // Ctrl/Cmd + K = Limpar
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        clearText();
    }
    // Ctrl/Cmd + Shift + C = Copiar stats
    if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key === 'C') {
        e.preventDefault();
        copyStats();
    }
});

// Auto-save no localStorage
textArea.addEventListener('input', () => {
    localStorage.setItem('savedText', textArea.value);
});

// Carregar texto salvo
window.addEventListener('load', () => {
    const savedText = localStorage.getItem('savedText');
    if (savedText) {
        textArea.value = savedText;
        analyzeText();
    }
});

// Inicializar
analyzeText();

console.log('✍️ Contador de Palavras carregado!');
console.log('Atalhos: Ctrl+K = Limpar | Ctrl+Shift+C = Copiar Stats');
