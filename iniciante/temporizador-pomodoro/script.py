import tkinter as tk
from tkinter import ttk, messagebox
import time
import threading
import winsound
import json
import os


class PomodoroTimer:
    def __init__(self, root):
        self.root = root
        self.root.title("🍅 Temporizador Pomodoro")
        self.root.geometry("500x700")
        self.root.resizable(False, False)
        self.root.configure(bg='#f0f0f0')
        
        # Estado
        self.is_running = False
        self.current_mode = 'focus'  # 'focus', 'short_break', 'long_break'
        self.focus_sessions_completed = 0
        self.total_cycles = 0
        self.timer_thread = None
        
        # Configurações padrão
        self.config = {
            'focus_time': 25,
            'short_break_time': 5,
            'long_break_time': 15
        }
        
        self.load_config()
        
        # Tempo inicial
        self.time_left = self.config['focus_time'] * 60
        self.total_time = self.time_left
        
        self.setup_ui()
        self.update_display()
        
    def setup_ui(self):
        # Container principal
        main_frame = tk.Frame(self.root, bg='#f0f0f0')
        main_frame.pack(expand=True, fill='both', padx=20, pady=20)
        
        # Título
        title_label = tk.Label(
            main_frame,
            text="🍅 Temporizador Pomodoro",
            font=('Arial', 24, 'bold'),
            bg='#f0f0f0',
            fg='#333'
        )
        title_label.pack(pady=(0, 20))
        
        # Display do timer
        timer_frame = tk.Frame(main_frame, bg='white', relief='raised', bd=2)
        timer_frame.pack(fill='x', pady=10)
        
        # Label do modo
        self.mode_label = tk.Label(
            timer_frame,
            text="🎯 FOCO",
            font=('Arial', 16, 'bold'),
            bg='white',
            fg='#667eea'
        )
        self.mode_label.pack(pady=(20, 10))
        
        # Display do tempo
        self.time_label = tk.Label(
            timer_frame,
            text="25:00",
            font=('Courier New', 60, 'bold'),
            bg='white',
            fg='#333'
        )
        self.time_label.pack(pady=20)
        
        # Barra de progresso
        self.progress = ttk.Progressbar(
            timer_frame,
            length=400,
            mode='determinate',
            maximum=100
        )
        self.progress.pack(pady=(10, 20))
        self.progress['value'] = 100
        
        # Botões de controle
        control_frame = tk.Frame(main_frame, bg='#f0f0f0')
        control_frame.pack(pady=20)
        
        self.start_btn = tk.Button(
            control_frame,
            text="▶ Iniciar",
            command=self.start_timer,
            font=('Arial', 12, 'bold'),
            bg='#4caf50',
            fg='white',
            width=12,
            height=2,
            relief='raised',
            cursor='hand2'
        )
        self.start_btn.grid(row=0, column=0, padx=5)
        
        self.pause_btn = tk.Button(
            control_frame,
            text="⏸ Pausar",
            command=self.pause_timer,
            font=('Arial', 12, 'bold'),
            bg='#ff9800',
            fg='white',
            width=12,
            height=2,
            relief='raised',
            cursor='hand2',
            state='disabled'
        )
        self.pause_btn.grid(row=0, column=1, padx=5)
        
        reset_btn = tk.Button(
            control_frame,
            text="↻ Resetar",
            command=self.reset_timer,
            font=('Arial', 12, 'bold'),
            bg='#f44336',
            fg='white',
            width=12,
            height=2,
            relief='raised',
            cursor='hand2'
        )
        reset_btn.grid(row=0, column=2, padx=5)
        
        # Estatísticas
        stats_frame = tk.LabelFrame(
            main_frame,
            text="📊 Estatísticas",
            font=('Arial', 12, 'bold'),
            bg='white',
            fg='#333',
            relief='raised',
            bd=2
        )
        stats_frame.pack(fill='x', pady=10)
        
        stats_inner = tk.Frame(stats_frame, bg='white')
        stats_inner.pack(padx=20, pady=15)
        
        tk.Label(
            stats_inner,
            text="Ciclos Completos:",
            font=('Arial', 11),
            bg='white',
            fg='#666'
        ).grid(row=0, column=0, sticky='w', pady=5)
        
        self.cycles_label = tk.Label(
            stats_inner,
            text="0",
            font=('Arial', 11, 'bold'),
            bg='white',
            fg='#667eea'
        )
        self.cycles_label.grid(row=0, column=1, sticky='e', pady=5, padx=(50, 0))
        
        tk.Label(
            stats_inner,
            text="Sessões de Foco:",
            font=('Arial', 11),
            bg='white',
            fg='#666'
        ).grid(row=1, column=0, sticky='w', pady=5)
        
        self.focus_label = tk.Label(
            stats_inner,
            text="0",
            font=('Arial', 11, 'bold'),
            bg='white',
            fg='#667eea'
        )
        self.focus_label.grid(row=1, column=1, sticky='e', pady=5, padx=(50, 0))
        
        # Configurações
        settings_frame = tk.LabelFrame(
            main_frame,
            text="⚙️ Configurações",
            font=('Arial', 12, 'bold'),
            bg='white',
            fg='#333',
            relief='raised',
            bd=2
        )
        settings_frame.pack(fill='x', pady=10)
        
        settings_inner = tk.Frame(settings_frame, bg='white')
        settings_inner.pack(padx=20, pady=15)
        
        # Tempo de foco
        tk.Label(
            settings_inner,
            text="Tempo de Foco (min):",
            font=('Arial', 10),
            bg='white',
            fg='#666'
        ).grid(row=0, column=0, sticky='w', pady=5)
        
        self.focus_time_var = tk.IntVar(value=self.config['focus_time'])
        focus_spinbox = tk.Spinbox(
            settings_inner,
            from_=1,
            to=60,
            textvariable=self.focus_time_var,
            width=10,
            font=('Arial', 10),
            command=self.save_config
        )
        focus_spinbox.grid(row=0, column=1, sticky='e', pady=5, padx=(20, 0))
        
        # Pausa curta
        tk.Label(
            settings_inner,
            text="Pausa Curta (min):",
            font=('Arial', 10),
            bg='white',
            fg='#666'
        ).grid(row=1, column=0, sticky='w', pady=5)
        
        self.short_break_var = tk.IntVar(value=self.config['short_break_time'])
        short_spinbox = tk.Spinbox(
            settings_inner,
            from_=1,
            to=30,
            textvariable=self.short_break_var,
            width=10,
            font=('Arial', 10),
            command=self.save_config
        )
        short_spinbox.grid(row=1, column=1, sticky='e', pady=5, padx=(20, 0))
        
        # Pausa longa
        tk.Label(
            settings_inner,
            text="Pausa Longa (min):",
            font=('Arial', 10),
            bg='white',
            fg='#666'
        ).grid(row=2, column=0, sticky='w', pady=5)
        
        self.long_break_var = tk.IntVar(value=self.config['long_break_time'])
        long_spinbox = tk.Spinbox(
            settings_inner,
            from_=1,
            to=60,
            textvariable=self.long_break_var,
            width=10,
            font=('Arial', 10),
            command=self.save_config
        )
        long_spinbox.grid(row=2, column=1, sticky='e', pady=5, padx=(20, 0))
        
    def format_time(self, seconds):
        """Formata segundos em MM:SS"""
        mins = seconds // 60
        secs = seconds % 60
        return f"{mins:02d}:{secs:02d}"
    
    def update_display(self):
        """Atualiza o display do tempo"""
        self.time_label.config(text=self.format_time(self.time_left))
        
        # Atualizar label do modo
        mode_labels = {
            'focus': '🎯 FOCO',
            'short_break': '☕ PAUSA CURTA',
            'long_break': '🌴 PAUSA LONGA'
        }
        self.mode_label.config(text=mode_labels[self.current_mode])
        
        # Atualizar barra de progresso
        if self.total_time > 0:
            percentage = (self.time_left / self.total_time) * 100
            self.progress['value'] = percentage
    
    def start_timer(self):
        """Inicia o timer"""
        if not self.is_running:
            self.is_running = True
            self.start_btn.config(state='disabled')
            self.pause_btn.config(state='normal')
            
            self.timer_thread = threading.Thread(target=self.run_timer, daemon=True)
            self.timer_thread.start()
    
    def run_timer(self):
        """Loop principal do timer"""
        while self.is_running and self.time_left > 0:
            time.sleep(1)
            self.time_left -= 1
            self.root.after(0, self.update_display)
        
        if self.time_left <= 0 and self.is_running:
            self.root.after(0, self.complete_session)
    
    def pause_timer(self):
        """Pausa o timer"""
        self.is_running = False
        self.start_btn.config(state='normal')
        self.pause_btn.config(state='disabled')
    
    def reset_timer(self):
        """Reseta o timer"""
        self.pause_timer()
        self.time_left = self.get_time_for_mode(self.current_mode)
        self.total_time = self.time_left
        self.update_display()
    
    def complete_session(self):
        """Completa uma sessão"""
        self.pause_timer()
        self.play_sound()
        
        if self.current_mode == 'focus':
            self.focus_sessions_completed += 1
            self.focus_label.config(text=str(self.focus_sessions_completed))
            
            # Após 4 sessões, pausa longa
            if self.focus_sessions_completed % 4 == 0:
                self.switch_mode('long_break')
                self.total_cycles += 1
                self.cycles_label.config(text=str(self.total_cycles))
                messagebox.showinfo("🌴 Pausa Longa!", "Você completou 4 sessões! Hora de uma pausa longa!")
            else:
                self.switch_mode('short_break')
                messagebox.showinfo("☕ Pausa!", "Sessão de foco completa! Hora de pausar!")
        else:
            self.switch_mode('focus')
            messagebox.showinfo("🎯 Foco!", "Pausa terminada! Hora de focar!")
    
    def switch_mode(self, mode):
        """Troca o modo do timer"""
        self.current_mode = mode
        self.time_left = self.get_time_for_mode(mode)
        self.total_time = self.time_left
        self.update_display()
    
    def get_time_for_mode(self, mode):
        """Retorna o tempo em segundos para o modo"""
        times = {
            'focus': self.config['focus_time'] * 60,
            'short_break': self.config['short_break_time'] * 60,
            'long_break': self.config['long_break_time'] * 60
        }
        return times[mode]
    
    def play_sound(self):
        """Toca som de notificação"""
        try:
            # Beep do Windows
            winsound.Beep(800, 500)
        except:
            print("\a")  # Beep alternativo
    
    def save_config(self):
        """Salva configurações"""
        self.config['focus_time'] = self.focus_time_var.get()
        self.config['short_break_time'] = self.short_break_var.get()
        self.config['long_break_time'] = self.long_break_var.get()
        
        try:
            with open('pomodoro_config.json', 'w') as f:
                json.dump(self.config, f)
        except:
            pass
        
        if self.current_mode == 'focus':
            self.reset_timer()
    
    def load_config(self):
        """Carrega configurações"""
        try:
            if os.path.exists('pomodoro_config.json'):
                with open('pomodoro_config.json', 'r') as f:
                    self.config = json.load(f)
        except:
            pass


def main():
    root = tk.Tk()
    app = PomodoroTimer(root)
    root.mainloop()


if __name__ == "__main__":
    main()
