import re

# --- Tokenização: transforma expressão em números e operadores ---
def tokenize(expr):
    tokens = []
    current = ''

    for char in expr.replace(" ", ""):  # remove espaços
        if char.isdigit() or char == '.':
            current += char
        elif char in '+-*/':
            if current:
                tokens.append(float(current))
            tokens.append(char)
            current = ''
        else:
            raise ValueError(f"Caractere inválido: {char}")

    if current:
        tokens.append(float(current))
    return tokens


# --- Resolve operações de alta precedência: * e / ---
def resolve_high_precedence(tokens):
    result = []
    i = 0

    while i < len(tokens):
        token = tokens[i]

        if token in ('*', '/'):
            prev = result.pop()
            next_value = tokens[i + 1]

            calc = prev * next_value if token == '*' else prev / next_value
            result.append(calc)
            i += 2
        else:
            result.append(token)
            i += 1

    return result


# --- Resolve baixa precedência: + e - ---
def resolve_low_precedence(tokens):
    result = tokens[0]

    for i in range(1, len(tokens), 2):
        operator = tokens[i]
        next_value = tokens[i + 1]

        if operator == '+':
            result += next_value
        elif operator == '-':
            result -= next_value

    return result


# --- Função principal ---
def calcular(expressao):
    tokens = tokenize(expressao)
    etapa1 = resolve_high_precedence(tokens)
    resultado = resolve_low_precedence(etapa1)
    return resultado


# --- CLI / Loop principal ---
if __name__ == "__main__":
    print("Calculadora CLI Simples | digite expressões como: 12+7*3-4/2")
    print("Para sair use CTRL + C\n")

    while True:
        try:
            entrada = input("> ")
            if entrada.strip() == "":
                continue

            resultado = calcular(entrada)
            print(f"= {resultado}\n")

        except ZeroDivisionError:
            print("Erro: divisão por zero não é permitida.\n")

        except Exception as e:
            print(f"Erro: {e}\n")
