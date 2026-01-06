import unittest
from script import gerar_senha, validar_forca_senha, gerar_multiplas_senhas


class TestGeradorSenha(unittest.TestCase):
    
    def test_tamanho_senha(self):
        """Testa se a senha tem o tamanho correto."""
        senha = gerar_senha(10)
        self.assertEqual(len(senha), 10)
    
    def test_senha_sem_opcoes(self):
        """Testa senha quando nenhuma opção é selecionada."""
        resultado = gerar_senha(10, False, False, False, False)
        self.assertIn("Erro", resultado)
    
    def test_multiplas_senhas(self):
        """Testa geração de múltiplas senhas."""
        senhas = gerar_multiplas_senhas(5, 8)
        self.assertEqual(len(senhas), 5)
        for senha in senhas:
            self.assertEqual(len(senha), 8)
    
    def test_validar_forca(self):
        """Testa validação de força da senha."""
        senha_forte = "Abc123!@#XYZ"
        senha_fraca = "abc"
        
        self.assertEqual(validar_forca_senha(senha_forte), "Forte")
        self.assertEqual(validar_forca_senha(senha_fraca), "Fraca")


if __name__ == '__main__':
    unittest.main()
