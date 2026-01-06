import random as rd 
import string

def gerar_senha(tamanho, incluir_maiusculas=True, incluir_minusculas=True, 
                incluir_numeros=True, incluir_simbolos=True): 
    """
    Gera uma senha aleatória com base nos parâmetros fornecidos.
    
    Args:
        tamanho (int): Comprimento da senha
        incluir_maiusculas (bool): Incluir letras maiúsculas
        incluir_minusculas (bool): Incluir letras minúsculas
        incluir_numeros (bool): Incluir números
        incluir_simbolos (bool): Incluir símbolos especiais
    
    Returns:
        str: Senha gerada
    """
    caracteres_disponiveis = ""
    
    if incluir_maiusculas:
        caracteres_disponiveis += string.ascii_uppercase
    if incluir_minusculas:
        caracteres_disponiveis += string.ascii_lowercase
    if incluir_numeros:
        caracteres_disponiveis += string.digits
    if incluir_simbolos:
        caracteres_disponiveis += "!@#$%^&*()_+[]{}|;:,.<>?"
    
    if not caracteres_disponiveis:
        return "Erro: Selecione pelo menos um tipo de caractere!"
    
    senha = ''.join(rd.choice(caracteres_disponiveis) for _ in range(tamanho))
    return senha


def gerar_multiplas_senhas(quantidade, tamanho=12):
    """Gera múltiplas senhas de uma vez."""
    return [gerar_senha(tamanho) for _ in range(quantidade)]


def validar_forca_senha(senha):
    """Valida a força de uma senha."""
    forca = 0
    
    if any(c.isupper() for c in senha):
        forca += 1
    if any(c.islower() for c in senha):
        forca += 1
    if any(c.isdigit() for c in senha):
        forca += 1
    if any(c in "!@#$%^&*()_+[]{}|;:,.<>?" for c in senha):
        forca += 1
    
    if len(senha) >= 12:
        forca += 1
    
    if forca <= 2:
        return "Fraca"
    elif forca <= 3:
        return "Média"
    else:
        return "Forte"


if __name__ == "__main__":
    # Exemplo de uso
    print("=== Gerador de Senhas ===\n")
    
    senha = gerar_senha(16)
    print(f"Senha gerada (16 caracteres): {senha}")
    print(f"Força da senha: {validar_forca_senha(senha)}\n")
    
    senha_sem_simbolos = gerar_senha(12, incluir_simbolos=False)
    print(f"Senha sem símbolos: {senha_sem_simbolos}")
    print(f"Força da senha: {validar_forca_senha(senha_sem_simbolos)}\n")
    
    senhas_multiplas = gerar_multiplas_senhas(3, 10)
    print("3 senhas de 10 caracteres:")
    for i, s in enumerate(senhas_multiplas, 1):
        print(f"  {i}. {s}") 