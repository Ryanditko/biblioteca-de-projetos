// --- Função para transformar a string em tokens (números e operadores) ---
function tokenize(expr) {
    const tokens = [];
    let current = '';

    for (let char of expr) {
        if (/[0-9.]/.test(char)) {          // número ou ponto decimal
            current += char;
        } else if (/[+\-*/]/.test(char)) {  // operador
            if (current) tokens.push(parseFloat(current));
            tokens.push(char);
            current = '';
        }
    }
    if (current) tokens.push(parseFloat(current));
    return tokens;
}

// --- Resolve operações de alta precedência: * e / ---
function resolveHighPrecedence(tokens) {
    const result = [];
    let i = 0;

    while (i < tokens.length) {
        const token = tokens[i];

        if (token === '*' || token === '/') {
            const prev = result.pop();
            const next = tokens[i + 1];
            const calc = token === '*'
                ? prev * next
                : prev / next;

            result.push(calc);
            i += 2; // pulamos o próximo porque já calculamos
        } else {
            result.push(token);
            i++;
        }
    }

    return result;
}

// --- Resolve baixa precedência: + e - ---
function resolveLowPrecedence(tokens) {
    let result = tokens[0];

    for (let i = 1; i < tokens.length; i += 2) {
        const operator = tokens[i];
        const next = tokens[i + 1];

        if (operator === '+') result += next;
        if (operator === '-') result -= next;
    }

    return result;
}

// --- Função principal que junta tudo ---
function calcular(expressao) {
    const tokens = tokenize(expressao);
    const etapa1 = resolveHighPrecedence(tokens);
    const resultado = resolveLowPrecedence(etapa1);
    return resultado;
}

// --- CLI ---
const readline = require("readline").createInterface({
    input: process.stdin,
    output: process.stdout
});

console.log("Calculadora Simples | digite expressões como: 12+7*3-4/2");
console.log("Para sair, pressione CTRL + C\n");

function loop() {
    readline.question("> ", (input) => {
        try {
            const resultado = calcular(input);
            console.log("= " + resultado + "\n");
        } catch (e) {
            console.log("Erro na expressão\n");
        }
        loop(); // mantém a calculadora ativa
    });
}

loop();
