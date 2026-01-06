(ns script
  (:import [javax.swing JFrame JPanel JLabel JButton JSpinner SpinnerNumberModel
            SwingUtilities JProgressBar BoxLayout Timer]
           [java.awt BorderLayout GridLayout FlowLayout Font Color Dimension]
           [java.awt.event ActionListener]
           [javax.swing.border EmptyBorder TitledBorder]))

;; Estado global do Pomodoro
(def app-state
  (atom {:time-left (* 25 60)        ; 25 minutos em segundos
         :total-time (* 25 60)
         :is-running false
         :current-mode :focus          ; :focus, :short-break, :long-break
         :focus-sessions 0
         :total-cycles 0
         :config {:focus-time 25
                  :short-break-time 5
                  :long-break-time 15}
         :timer nil}))

;; Componentes da UI
(def ui-components (atom {}))

;; Funções auxiliares

(defn format-time
  "Formata segundos em MM:SS"
  [seconds]
  (let [mins (quot seconds 60)
        secs (mod seconds 60)]
    (format "%02d:%02d" mins secs)))

(defn get-time-for-mode
  "Retorna tempo em segundos para o modo"
  [config mode]
  (* 60 (case mode
          :focus (:focus-time config)
          :short-break (:short-break-time config)
          :long-break (:long-break-time config))))

(defn play-beep
  "Toca um beep de notificação"
  []
  (try
    (java.awt.Toolkit/getDefaultToolkit)
    (.beep (java.awt.Toolkit/getDefaultToolkit))
    (catch Exception e
      (println "Erro ao tocar som:" (.getMessage e)))))

(defn update-display
  "Atualiza os componentes da UI"
  []
  (SwingUtilities/invokeLater
   (fn []
     (let [state @app-state
           {:keys [time-left current-mode focus-sessions total-cycles]} state
           {:keys [time-label mode-label progress-bar
                   focus-count-label cycles-count-label]} @ui-components
           mode-labels {:focus "🎯 FOCO"
                        :short-break "☕ PAUSA CURTA"
                        :long-break "🌴 PAUSA LONGA"}
           percentage (if (> (:total-time state) 0)
                        (int (* 100 (/ time-left (:total-time state))))
                        100)]

       (.setText time-label (format-time time-left))
       (.setText mode-label (mode-labels current-mode))
       (.setValue progress-bar percentage)
       (.setText focus-count-label (str focus-sessions))
       (.setText cycles-count-label (str total-cycles))))))

(defn switch-mode
  "Troca o modo do timer"
  [mode]
  (let [config (:config @app-state)
        new-time (get-time-for-mode config mode)]
    (swap! app-state assoc
           :current-mode mode
           :time-left new-time
           :total-time new-time)
    (update-display)))

(defn complete-session
  "Completa uma sessão"
  []
  (play-beep)
  (let [state @app-state
        mode (:current-mode state)]
    (if (= mode :focus)
      (do
        (swap! app-state update :focus-sessions inc)
        (let [sessions (:focus-sessions @app-state)]
          (if (zero? (mod sessions 4))
            (do
              (swap! app-state update :total-cycles inc)
              (switch-mode :long-break)
              (javax.swing.JOptionPane/showMessageDialog
               nil
               "Você completou 4 sessões! Hora de uma pausa longa!"
               "🌴 Pausa Longa"
               javax.swing.JOptionPane/INFORMATION_MESSAGE))
            (do
              (switch-mode :short-break)
              (javax.swing.JOptionPane/showMessageDialog
               nil
               "Sessão de foco completa! Hora de pausar!"
               "☕ Pausa"
               javax.swing.JOptionPane/INFORMATION_MESSAGE)))))
      (do
        (switch-mode :focus)
        (javax.swing.JOptionPane/showMessageDialog
         nil
         "Pausa terminada! Hora de focar!"
         "🎯 Foco"
         javax.swing.JOptionPane/INFORMATION_MESSAGE)))))

(defn tick-timer
  "Decrementa o timer"
  []
  (when (:is-running @app-state)
    (swap! app-state update :time-left dec)
    (update-display)
    (when (<= (:time-left @app-state) 0)
      (swap! app-state assoc :is-running false)
      (when-let [timer (:timer @app-state)]
        (.stop timer))
      (complete-session))))

(defn start-timer
  "Inicia o timer"
  []
  (when-not (:is-running @app-state)
    (swap! app-state assoc :is-running true)
    (let [timer (Timer. 1000 (reify ActionListener
                               (actionPerformed [_ _]
                                 (tick-timer))))]
      (swap! app-state assoc :timer timer)
      (.start timer)
      (let [{:keys [start-btn pause-btn]} @ui-components]
        (.setEnabled start-btn false)
        (.setEnabled pause-btn true)))))

(defn pause-timer
  "Pausa o timer"
  []
  (when (:is-running @app-state)
    (swap! app-state assoc :is-running false)
    (when-let [timer (:timer @app-state)]
      (.stop timer))
    (let [{:keys [start-btn pause-btn]} @ui-components]
      (.setEnabled start-btn true)
      (.setEnabled pause-btn false))))

(defn reset-timer
  "Reseta o timer"
  []
  (pause-timer)
  (let [config (:config @app-state)
        mode (:current-mode @app-state)
        new-time (get-time-for-mode config mode)]
    (swap! app-state assoc
           :time-left new-time
           :total-time new-time)
    (update-display)))

(defn save-config
  "Salva configurações"
  [focus short-break long-break]
  (swap! app-state assoc-in [:config :focus-time] focus)
  (swap! app-state assoc-in [:config :short-break-time] short-break)
  (swap! app-state assoc-in [:config :long-break-time] long-break)
  (when (= (:current-mode @app-state) :focus)
    (reset-timer)))

;; Criação da UI

(defn create-button
  "Cria um botão customizado"
  [text bg-color action]
  (doto (JButton. text)
    (.setFont (Font. "Arial" Font/BOLD 14))
    (.setBackground bg-color)
    (.setForeground Color/WHITE)
    (.setFocusPainted false)
    (.setPreferredSize (Dimension. 120 50))
    (.addActionListener (reify ActionListener
                          (actionPerformed [_ _]
                            (action))))))

(defn create-timer-panel
  "Cria o painel do timer"
  []
  (let [panel (JPanel.)
        mode-label (doto (JLabel. "🎯 FOCO")
                     (.setFont (Font. "Arial" Font/BOLD 18))
                     (.setForeground (Color. 102 126 234))
                     (.setHorizontalAlignment JLabel/CENTER))
        time-label (doto (JLabel. "25:00")
                     (.setFont (Font. "Courier New" Font/BOLD 60))
                     (.setHorizontalAlignment JLabel/CENTER))
        progress-bar (doto (JProgressBar. 0 100)
                       (.setValue 100)
                       (.setPreferredSize (Dimension. 400 15)))]

    (.setLayout panel (BoxLayout. panel BoxLayout/Y_AXIS))
    (.setBackground panel Color/WHITE)
    (.setBorder panel (EmptyBorder. 20 20 20 20))

    (.add panel mode-label)
    (.add panel (javax.swing.Box/createVerticalStrut 10))
    (.add panel time-label)
    (.add panel (javax.swing.Box/createVerticalStrut 20))
    (.add panel progress-bar)

    (swap! ui-components assoc
           :mode-label mode-label
           :time-label time-label
           :progress-bar progress-bar)

    panel))

(defn create-control-panel
  "Cria o painel de controles"
  []
  (let [panel (JPanel. (FlowLayout. FlowLayout/CENTER 10 10))
        start-btn (create-button "▶ Iniciar" (Color. 76 175 80) start-timer)
        pause-btn (create-button "⏸ Pausar" (Color. 255 152 0) pause-timer)
        reset-btn (create-button "↻ Resetar" (Color. 244 67 54) reset-timer)]

    (.setEnabled pause-btn false)
    (.setBackground panel (Color. 240 240 240))

    (.add panel start-btn)
    (.add panel pause-btn)
    (.add panel reset-btn)

    (swap! ui-components assoc
           :start-btn start-btn
           :pause-btn pause-btn)

    panel))

(defn create-stats-panel
  "Cria o painel de estatísticas"
  []
  (let [panel (JPanel. (GridLayout. 2 2 20 10))
        cycles-label (JLabel. "0")
        focus-label (JLabel. "0")]

    (.setBorder panel (TitledBorder. "📊 Estatísticas"))
    (.setBackground panel Color/WHITE)

    (.setFont cycles-label (Font. "Arial" Font/BOLD 14))
    (.setFont focus-label (Font. "Arial" Font/BOLD 14))
    (.setForeground cycles-label (Color. 102 126 234))
    (.setForeground focus-label (Color. 102 126 234))

    (.add panel (JLabel. "Ciclos Completos:"))
    (.add panel cycles-label)
    (.add panel (JLabel. "Sessões de Foco:"))
    (.add panel focus-label)

    (swap! ui-components assoc
           :cycles-count-label cycles-label
           :focus-count-label focus-label)

    panel))

(defn create-settings-panel
  "Cria o painel de configurações"
  []
  (let [panel (JPanel. (GridLayout. 3 2 10 10))
        config (:config @app-state)
        focus-spinner (JSpinner. (SpinnerNumberModel. (:focus-time config) 1 60 1))
        short-spinner (JSpinner. (SpinnerNumberModel. (:short-break-time config) 1 30 1))
        long-spinner (JSpinner. (SpinnerNumberModel. (:long-break-time config) 1 60 1))
        update-config #(save-config
                        (.getValue focus-spinner)
                        (.getValue short-spinner)
                        (.getValue long-spinner))]

    (.setBorder panel (TitledBorder. "⚙️ Configurações"))
    (.setBackground panel Color/WHITE)

    (.addChangeListener focus-spinner (reify javax.swing.event.ChangeListener
                                        (stateChanged [_ _] (update-config))))
    (.addChangeListener short-spinner (reify javax.swing.event.ChangeListener
                                        (stateChanged [_ _] (update-config))))
    (.addChangeListener long-spinner (reify javax.swing.event.ChangeListener
                                       (stateChanged [_ _] (update-config))))

    (.add panel (JLabel. "Tempo de Foco (min):"))
    (.add panel focus-spinner)
    (.add panel (JLabel. "Pausa Curta (min):"))
    (.add panel short-spinner)
    (.add panel (JLabel. "Pausa Longa (min):"))
    (.add panel long-spinner)

    panel))

(defn create-frame
  "Cria a janela principal"
  []
  (let [frame (JFrame. "🍅 Temporizador Pomodoro")
        main-panel (JPanel. (BorderLayout. 10 10))]

    (.setDefaultCloseOperation frame JFrame/EXIT_ON_CLOSE)
    (.setSize frame 550 700)
    (.setResizable frame false)

    (.setBackground main-panel (Color. 240 240 240))
    (.setBorder main-panel (EmptyBorder. 20 20 20 20))

    ;; Adicionar componentes
    (.add main-panel (create-timer-panel) BorderLayout/NORTH)
    (.add main-panel (create-control-panel) BorderLayout/CENTER)

    (let [bottom-panel (JPanel.)
          _ (.setLayout bottom-panel (BoxLayout. bottom-panel BoxLayout/Y_AXIS))
          _ (.setBackground bottom-panel (Color. 240 240 240))]
      (.add bottom-panel (create-stats-panel))
      (.add bottom-panel (javax.swing.Box/createVerticalStrut 10))
      (.add bottom-panel (create-settings-panel))
      (.add main-panel bottom-panel BorderLayout/SOUTH))

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
       (println "🍅 Temporizador Pomodoro iniciado!")
       (println "Configurações:")
       (println "  Foco: 25 minutos")
       (println "  Pausa curta: 5 minutos")
       (println "  Pausa longa: 15 minutos")))))

;; Para executar: (script/-main)
