(ns script
  (:require [clojure.string :as str])
  (:import [javax.swing JFrame JPanel JLabel JButton JTextArea JScrollPane
            SwingUtilities BoxLayout JSplitPane]
           [java.awt BorderLayout GridLayout FlowLayout Font Color Dimension]
           [java.awt.event KeyAdapter]
           [javax.swing.border EmptyBorder TitledBorder]))

;; Estado global
(def app-state
  (atom {:text ""
         :stats {:words 0
                 :chars 0
                 :chars-no-space 0
                 :lines 0
                 :paragraphs 0
                 :sentences 0
                 :vowels 0
                 :consonants 0
                 :longest-word ""
                 :reading-time "0 min"
                 :word-frequency []}}))

;; Componentes UI
(def ui-components (atom {}))

;; Funções de análise

(defn count-words
  "Conta palavras no texto"
  [text]
  (if (str/blank? text)
    0
    (count (re-seq #"\b\w+\b" text))))

(defn count-chars-no-space
  "Conta caracteres sem espaços"
  [text]
  (count (str/replace text #"\s" "")))

(defn count-lines
  "Conta linhas"
  [text]
  (if (empty? text)
    0
    (count (str/split-lines text))))

(defn count-paragraphs
  "Conta parágrafos"
  [text]
  (if (str/blank? text)
    0
    (count (filter #(not (str/blank? %))
                   (str/split text #"\n\s*\n")))))

(defn count-sentences
  "Conta frases"
  [text]
  (if (str/blank? text)
    0
    (count (filter #(not (str/blank? %))
                   (str/split text #"[.!?]+")))))

(defn count-vowels
  "Conta vogais"
  [text]
  (count (re-seq #"[aeiouáéíóúâêîôûãõàèìòù]" (str/lower-case text))))

(defn count-consonants
  "Conta consoantes"
  [text]
  (count (re-seq #"[bcdfghjklmnpqrstvwxyzçñ]" (str/lower-case text))))

(defn find-longest-word
  "Encontra palavra mais longa"
  [text]
  (if (str/blank? text)
    ""
    (let [words (re-seq #"\b\w+\b" text)]
      (if (empty? words)
        ""
        (apply max-key count words)))))

(defn calculate-reading-time
  "Calcula tempo de leitura (200 palavras/min)"
  [word-count]
  (if (zero? word-count)
    "0 min"
    (let [minutes (max 1 (quot word-count 200))]
      (str minutes " min"))))

(defn word-frequency
  "Calcula frequência de palavras"
  [text]
  (if (str/blank? text)
    []
    (let [words (->> (str/lower-case text)
                     (re-seq #"\b\w+\b")
                     (filter #(> (count %) 2)))]
      (->> words
           frequencies
           (sort-by val >)
           (take 10)))))

(defn analyze-text
  "Analisa o texto completo"
  [text]
  (let [words (count-words text)
        chars (count text)
        chars-no-space (count-chars-no-space text)
        lines (count-lines text)
        paragraphs (count-paragraphs text)
        sentences (count-sentences text)
        vowels (count-vowels text)
        consonants (count-consonants text)
        longest (find-longest-word text)
        reading-time (calculate-reading-time words)
        freq (word-frequency text)]
    {:words words
     :chars chars
     :chars-no-space chars-no-space
     :lines lines
     :paragraphs paragraphs
     :sentences sentences
     :vowels vowels
     :consonants consonants
     :longest-word longest
     :reading-time reading-time
     :word-frequency freq}))

(defn update-display
  "Atualiza todos os componentes da UI"
  []
  (SwingUtilities/invokeLater
   (fn []
     (let [stats (:stats @app-state)
           {:keys [word-count char-count char-no-space-count
                   line-count paragraph-count reading-time-label
                   sentence-count vowel-count consonant-count
                   longest-word-label freq-area]} @ui-components]

       (.setText word-count (str (:words stats)))
       (.setText char-count (str (:chars stats)))
       (.setText char-no-space-count (str (:chars-no-space stats)))
       (.setText line-count (str (:lines stats)))
       (.setText paragraph-count (str (:paragraphs stats)))
       (.setText reading-time-label (:reading-time stats))
       (.setText sentence-count (str (:sentences stats)))
       (.setText vowel-count (str (:vowels stats)))
       (.setText consonant-count (str (:consonants stats)))
       (.setText longest-word-label (or (:longest-word stats) "-"))

       ;; Atualizar frequência
       (let [freq-text (if (empty? (:word-frequency stats))
                         "Nenhuma palavra ainda..."
                         (str/join "\n"
                                   (map (fn [[word count]]
                                          (format "%-20s %dx" word count))
                                        (:word-frequency stats))))]
         (.setText freq-area freq-text))))))

(defn on-text-change
  "Callback quando o texto muda"
  [text-area]
  (let [text (.getText text-area)
        stats (analyze-text text)]
    (swap! app-state assoc :text text :stats stats)
    (update-display)))

(defn create-stat-card
  "Cria um card de estatística"
  [icon value-atom label]
  (let [panel (doto (JPanel.)
                (.setLayout (BoxLayout. (JPanel.) BoxLayout/Y_AXIS))
                (.setBackground (Color. 102 126 234))
                (.setBorder (EmptyBorder. 15 15 15 15))
                (.setPreferredSize (Dimension. 150 120)))
        icon-label (doto (JLabel. icon)
                     (.setFont (Font. "Arial" Font/PLAIN 28))
                     (.setForeground Color/WHITE)
                     (.setAlignmentX JLabel/CENTER_ALIGNMENT))
        value-label (doto (JLabel. "0")
                      (.setFont (Font. "Arial" Font/BOLD 24))
                      (.setForeground Color/WHITE)
                      (.setAlignmentX JLabel/CENTER_ALIGNMENT))
        label-label (doto (JLabel. label)
                      (.setFont (Font. "Arial" Font/PLAIN 11))
                      (.setForeground Color/WHITE)
                      (.setAlignmentX JLabel/CENTER_ALIGNMENT))]

    (.add panel icon-label)
    (.add panel (javax.swing.Box/createVerticalStrut 5))
    (.add panel value-label)
    (.add panel (javax.swing.Box/createVerticalStrut 5))
    (.add panel label-label)

    (swap! ui-components assoc value-atom value-label)
    panel))

(defn create-text-area
  "Cria área de texto"
  []
  (let [text-area (doto (JTextArea.)
                    (.setFont (Font. "Arial" Font/PLAIN 13))
                    (.setLineWrap true)
                    (.setWrapStyleWord true)
                    (.setBorder (EmptyBorder. 10 10 10 10)))
        scroll-pane (JScrollPane. text-area)]

    (.addKeyListener text-area
                     (proxy [KeyAdapter] []
                       (keyReleased [_]
                         (on-text-change text-area))))

    (swap! ui-components assoc :text-area text-area)
    scroll-pane))

(defn create-stats-panel
  "Cria painel de estatísticas"
  []
  (let [panel (JPanel. (GridLayout. 2 3 10 10))]
    (.setBackground panel (Color. 240 240 240))
    (.setBorder panel (EmptyBorder. 10 10 10 10))

    (.add panel (create-stat-card "📝" :word-count "Palavras"))
    (.add panel (create-stat-card "🔤" :char-count "Caracteres"))
    (.add panel (create-stat-card "📄" :char-no-space-count "Sem Espaços"))
    (.add panel (create-stat-card "📋" :line-count "Linhas"))
    (.add panel (create-stat-card "📖" :paragraph-count "Parágrafos"))
    (.add panel (create-stat-card "⏱️" :reading-time-label "Tempo Leitura"))

    panel))

(defn create-details-panel
  "Cria painel de detalhes"
  []
  (let [panel (JPanel. (GridLayout. 2 4 10 10))]
    (.setBackground panel Color/WHITE)
    (.setBorder panel (TitledBorder. "📊 Detalhes Avançados"))

    (doseq [[label key] [["Frases:" :sentence-count]
                         ["Vogais:" :vowel-count]
                         ["Consoantes:" :consonant-count]
                         ["Palavra mais longa:" :longest-word-label]]]
      (.add panel (doto (JLabel. label)
                    (.setFont (Font. "Arial" Font/PLAIN 11))))
      (let [value-label (doto (JLabel. "0")
                          (.setFont (Font. "Arial" Font/BOLD 11))
                          (.setForeground (Color. 102 126 234)))]
        (.add panel value-label)
        (swap! ui-components assoc key value-label)))

    panel))

(defn create-frequency-panel
  "Cria painel de frequência"
  []
  (let [panel (JPanel. (BorderLayout.))]
    (.setBackground panel Color/WHITE)
    (.setBorder panel (TitledBorder. "🔝 Palavras Mais Frequentes"))

    (let [freq-area (doto (JTextArea. "Nenhuma palavra ainda...")
                      (.setFont (Font. "Courier New" Font/PLAIN 11))
                      (.setEditable false)
                      (.setBackground Color/WHITE))
          scroll-pane (JScrollPane. freq-area)]
      (swap! ui-components assoc :freq-area freq-area)
      (.add panel scroll-pane BorderLayout/CENTER))

    panel))

(defn create-button-panel
  "Cria painel de botões"
  [text-area]
  (let [panel (JPanel. (FlowLayout. FlowLayout/CENTER 10 10))
        clear-btn (doto (JButton. "🗑️ Limpar Texto")
                    (.setFont (Font. "Arial" Font/BOLD 12))
                    (.setBackground (Color. 244 67 54))
                    (.setForeground Color/WHITE)
                    (.setFocusPainted false)
                    (.addActionListener
                     (reify java.awt.event.ActionListener
                       (actionPerformed [_ _]
                         (.setText text-area "")
                         (on-text-change text-area)))))
        copy-btn (doto (JButton. "📋 Copiar Estatísticas")
                   (.setFont (Font. "Arial" Font/BOLD 12))
                   (.setBackground (Color. 76 175 80))
                   (.setForeground Color/WHITE)
                   (.setFocusPainted false)
                   (.addActionListener
                    (reify java.awt.event.ActionListener
                      (actionPerformed [_ _]
                        (let [stats (:stats @app-state)
                              text (format "📊 ESTATÍSTICAS\n\nPalavras: %d\nCaracteres: %d\nLinhas: %d"
                                           (:words stats)
                                           (:chars stats)
                                           (:lines stats))
                              clipboard (.getSystemClipboard (java.awt.Toolkit/getDefaultToolkit))
                              selection (java.awt.datatransfer.StringSelection. text)]
                          (.setContents clipboard selection nil)
                          (javax.swing.JOptionPane/showMessageDialog
                           nil "Estatísticas copiadas!" "✅ Sucesso"
                           javax.swing.JOptionPane/INFORMATION_MESSAGE))))))]

    (.setBackground panel (Color. 240 240 240))
    (.add panel clear-btn)
    (.add panel copy-btn)
    panel))

(defn create-frame
  "Cria a janela principal"
  []
  (let [frame (JFrame. "✍️ Contador de Palavras")
        text-scroll (create-text-area)
        text-area (:text-area @ui-components)

        top-panel (doto (JPanel.)
                    (.setLayout (BoxLayout. (JPanel.) BoxLayout/Y_AXIS))
                    (.setBackground (Color. 240 240 240)))

        stats-panel (create-stats-panel)
        details-panel (create-details-panel)
        freq-panel (create-frequency-panel)
        button-panel (create-button-panel text-area)

        main-panel (JPanel. (BorderLayout. 10 10))]

    (.setDefaultCloseOperation frame JFrame/EXIT_ON_CLOSE)
    (.setSize frame 950 850)

    ;; Montar painel superior
    (.add top-panel stats-panel)
    (.add top-panel (javax.swing.Box/createVerticalStrut 10))
    (.add top-panel details-panel)
    (.add top-panel (javax.swing.Box/createVerticalStrut 10))
    (.add top-panel freq-panel)
    (.add top-panel (javax.swing.Box/createVerticalStrut 10))
    (.add top-panel button-panel)

    ;; Usar split pane
    (let [split-pane (doto (JSplitPane. JSplitPane/VERTICAL_SPLIT text-scroll top-panel)
                       (.setResizeWeight 0.3)
                       (.setDividerLocation 200))]
      (.add main-panel split-pane BorderLayout/CENTER))

    (.setBackground main-panel (Color. 240 240 240))
    (.setBorder main-panel (EmptyBorder. 20 20 20 20))

    (.add frame main-panel)
    (.setLocationRelativeTo frame nil)
    frame))

(defn -main
  "Função principal"
  [& _args]
  (SwingUtilities/invokeLater
   (fn []
     (let [frame (create-frame)]
       (.setVisible frame true)
       (println "✍️ Contador de Palavras iniciado!")))))

;; Para executar: (script/-main)
