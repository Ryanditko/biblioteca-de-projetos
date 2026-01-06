const letrasMaiusculas = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
const letrasMinusculas = "abcdefghijklmnopqrstuvwxyz";
const numeros = "0123456789";
const simbolos = "!@#$%^&*()_+[]{}|;:,.<>?";

function gerarSenha(tamanho, incluirMaiusculas = true, incluirMinusculas = true, incluirNumeros = true, incluirSimbolos = true) {
    let caracteresDisponiveis = "";

    if (incluirMaiusculas) caracteresDisponiveis += letrasMaiusculas;
    if (incluirMinusculas) caracteresDisponiveis += letrasMinusculas;
    if (incluirNumeros) caracteresDisponiveis += numeros;
    if (incluirSimbolos) caracteresDisponiveis += simbolos;

    if (caracteresDisponiveis === "") {
        return "Erro: Selecione pelo menos um tipo de caractere!";
    }

    let senha = "";
    for (let i = 0; i < tamanho; i++) {
        senha += caracteresDisponiveis.charAt(Math.floor(Math.random() * caracteresDisponiveis.length));
    }

    return senha;
}

// Gerar e exibir senha com 12 caracteres
const senhaGerada = gerarSenha(12);
console.log("Senha gerada:", senhaGerada);