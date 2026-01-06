(ns script
  (:require [clojure.string :as str]))

;; Definição dos conjuntos de caracteres
(def letras-maiusculas "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
(def letras-minusculas "abcdefghijklmnopqrstuvwxyz")
(def numeros "0123456789")
(def simbolos "!@#$%^&*()_+[]{}|;:,.<>?")

(defn gerar-senha
  "Gera uma senha aleatória com base nos parâmetros fornecidos.
  
  Parâmetros:
    tamanho - Comprimento da senha (número)
    opcoes - Map com chaves booleanas:
      :maiusculas? - Incluir letras maiúsculas (padrão: true)
      :minusculas? - Incluir letras minúsculas (padrão: true)
      :numeros? - Incluir números (padrão: true)
      :simbolos? - Incluir símbolos especiais (padrão: true)
  
  Retorna:
    String com a senha gerada ou mensagem de erro"
  ([tamanho]
   (gerar-senha tamanho {}))
  ([tamanho {:keys [maiusculas? minusculas? numeros? simbolos?]
             :or {maiusculas? true
                  minusculas? true
                  numeros? true
                  simbolos? true}}]
   (let [caracteres-disponiveis (str/join ""
                                          (cond-> []
                                            maiusculas? (conj letras-maiusculas)
                                            minusculas? (conj letras-minusculas)
                                            numeros? (conj numeros)
                                            simbolos? (conj simbolos)))]
     (if (empty? caracteres-disponiveis)
       "Erro: Selecione pelo menos um tipo de caractere!"
       (str/join (repeatedly tamanho
                             #(rand-nth caracteres-disponiveis)))))))

(defn gerar-multiplas-senhas
  "Gera múltiplas senhas de uma vez.
  
  Parâmetros:
    quantidade - Número de senhas a gerar
    tamanho - Comprimento de cada senha (padrão: 12)
    opcoes - Map de opções (mesmas da função gerar-senha)
  
  Retorna:
    Vetor de strings com as senhas geradas"
  ([quantidade]
   (gerar-multiplas-senhas quantidade 12 {}))
  ([quantidade tamanho]
   (gerar-multiplas-senhas quantidade tamanho {}))
  ([quantidade tamanho opcoes]
   (vec (repeatedly quantidade #(gerar-senha tamanho opcoes)))))

(defn validar-forca-senha
  "Valida a força de uma senha com base em critérios.
  
  Parâmetros:
    senha - String da senha a validar
  
  Retorna:
    String: 'Fraca', 'Média' ou 'Forte'"
  [senha]
  (let [tem-maiuscula? (some #(Character/isUpperCase %) senha)
        tem-minuscula? (some #(Character/isLowerCase %) senha)
        tem-numero? (some #(Character/isDigit %) senha)
        tem-simbolo? (some #(str/includes? simbolos (str %)) senha)
        tamanho-bom? (>= (count senha) 12)
        forca (cond-> 0
                tem-maiuscula? inc
                tem-minuscula? inc
                tem-numero? inc
                tem-simbolo? inc
                tamanho-bom? inc)]
    (cond
      (<= forca 2) "Fraca"
      (<= forca 3) "Média"
      :else "Forte")))

(defn exibir-senha-formatada
  "Exibe a senha com informações de força."
  [senha]
  (println (str "Senha gerada: " senha))
  (println (str "Força: " (validar-forca-senha senha)))
  (println (str "Tamanho: " (count senha) " caracteres\n")))

;; Exemplos de uso
(defn -main
  "Função principal com exemplos de uso."
  [& _args]
  (println "=== Gerador de Senhas em Clojure ===\n")

  ;; Exemplo 1: Senha padrão com 16 caracteres
  (println "1. Senha com 16 caracteres (todos os tipos):")
  (let [senha1 (gerar-senha 16)]
    (exibir-senha-formatada senha1))

  ;; Exemplo 2: Senha sem símbolos
  (println "2. Senha de 12 caracteres sem símbolos:")
  (let [senha2 (gerar-senha 12 {:simbolos? false})]
    (exibir-senha-formatada senha2))

  ;; Exemplo 3: Apenas letras e números
  (println "3. Senha de 10 caracteres (apenas letras e números):")
  (let [senha3 (gerar-senha 10 {:simbolos? false})]
    (exibir-senha-formatada senha3))

  ;; Exemplo 4: Apenas números (PIN)
  (println "4. PIN de 6 dígitos:")
  (let [pin (gerar-senha 6 {:maiusculas? false
                            :minusculas? false
                            :simbolos? false})]
    (exibir-senha-formatada pin))

  ;; Exemplo 5: Múltiplas senhas
  (println "5. Gerando 3 senhas de 12 caracteres:")
  (doseq [[idx senha] (map-indexed vector (gerar-multiplas-senhas 3 12))]
    (println (str "   " (inc idx) ". " senha " - Força: " (validar-forca-senha senha)))))

;; Executar exemplos (descomente para testar)
;; (-main)

;; REPL Usage Examples:
;; (gerar-senha 12)
;; (gerar-senha 16 {:simbolos? false})
;; (gerar-multiplas-senhas 5 10)
;; (validar-forca-senha "Abc123!@#XYZ etc")
