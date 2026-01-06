import tkinter as tk
from tkinter import ttk, messagebox, scrolledtext
from collections import Counter
import re


class WordCounter:
    def __init__(self, root):
        self.root = root
        self.root.title("✍️ Contador de Palavras")
        self.root.geometry("900x800")
        self.root.configure(bg='#f0f0f0')
        
        self.setup_ui()
        
    def setup_ui(self):
        # Container principal
        main_frame = tk.Frame(self.root, bg='#f0f0f0')
        main_frame.pack(expand=True, fill='both', padx=20, pady=20)
        
        # Título
        title_label = tk.Label(
            main_frame,
            text="✍️ Contador de Palavras",
            font=('Arial', 28, 'bold'),
            bg='#f0f0f0',
            fg='#333'
        )
        title_label.pack(pady=(0, 5))
        
        subtitle = tk.Label(
            main_frame,
            text="Digite ou cole seu texto abaixo",
            font=('Arial', 12),
            bg='#f0f0f0',
            fg='#666'
        )
        subtitle.pack(pady=(0, 20))
        
        # Área de texto
        text_frame = tk.Frame(main_frame, bg='#f0f0f0')
        text_frame.pack(fill='both', expand=True, pady=(0, 20))
        
        self.text_area = scrolledtext.ScrolledText(
            text_frame,
            wrap=tk.WORD,
            font=('Arial', 11),
            bg='white',
            fg='#333',
            relief='solid',
            borderwidth=2,
            padx=10,
            pady=10
        )
        self.text_area.pack(fill='both', expand=True)
        self.text_area.bind('<KeyRelease>', lambda e: self.analyze_text())
        
        # Grid de estatísticas
        stats_frame = tk.Frame(main_frame, bg='#f0f0f0')
        stats_frame.pack(fill='x', pady=(0, 20))
        
        # Criar cards de estatísticas
        self.stat_cards = {}
        stats = [
            ('📝', 'words', 'Palavras'),
            ('🔤', 'chars', 'Caracteres'),
            ('📄', 'chars_no_space', 'Sem Espaços'),
            ('📋', 'lines', 'Linhas'),
            ('📖', 'paragraphs', 'Parágrafos'),
            ('⏱️', 'reading_time', 'Tempo Leitura')
        ]
        
        for i, (icon, key, label) in enumerate(stats):
            card = self.create_stat_card(stats_frame, icon, label)
            card.grid(row=i//3, column=i%3, padx=5, pady=5, sticky='nsew')
            self.stat_cards[key] = card['value_label']
        
        # Configurar grid
        for i in range(3):
            stats_frame.columnconfigure(i, weight=1)
        
        # Detalhes avançados
        details_frame = tk.LabelFrame(
            main_frame,
            text="📊 Detalhes Avançados",
            font=('Arial', 12, 'bold'),
            bg='white',
            fg='#333',
            relief='raised',
            bd=2
        )
        details_frame.pack(fill='x', pady=(0, 20))
        
        details_inner = tk.Frame(details_frame, bg='white')
        details_inner.pack(padx=15, pady=15, fill='x')
        
        self.detail_labels = {}
        details = [
            ('Frases:', 'sentences'),
            ('Vogais:', 'vowels'),
            ('Consoantes:', 'consonants'),
            ('Palavra mais longa:', 'longest')
        ]
        
        for i, (label, key) in enumerate(details):
            tk.Label(
                details_inner,
                text=label,
                font=('Arial', 10),
                bg='white',
                fg='#666'
            ).grid(row=i//2, column=(i%2)*2, sticky='w', padx=10, pady=5)
            
            value_label = tk.Label(
                details_inner,
                text='0',
                font=('Arial', 10, 'bold'),
                bg='white',
                fg='#667eea'
            )
            value_label.grid(row=i//2, column=(i%2)*2+1, sticky='e', padx=10, pady=5)
            self.detail_labels[key] = value_label
        
        # Configurar grid de detalhes
        for i in range(4):
            details_inner.columnconfigure(i, weight=1)
        
        # Frequência de palavras
        freq_frame = tk.LabelFrame(
            main_frame,
            text="🔝 Palavras Mais Frequentes",
            font=('Arial', 12, 'bold'),
            bg='white',
            fg='#333',
            relief='raised',
            bd=2
        )
        freq_frame.pack(fill='both', expand=True, pady=(0, 20))
        
        # Listbox para frequência
        self.freq_listbox = tk.Listbox(
            freq_frame,
            font=('Arial', 10),
            bg='white',
            fg='#333',
            height=8,
            relief='flat'
        )
        self.freq_listbox.pack(fill='both', expand=True, padx=15, pady=15)
        
        # Botões
        button_frame = tk.Frame(main_frame, bg='#f0f0f0')
        button_frame.pack(fill='x')
        
        clear_btn = tk.Button(
            button_frame,
            text="🗑️ Limpar Texto",
            command=self.clear_text,
            font=('Arial', 11, 'bold'),
            bg='#f44336',
            fg='white',
            relief='raised',
            cursor='hand2',
            padx=20,
            pady=10
        )
        clear_btn.pack(side='left', expand=True, fill='x', padx=(0, 5))
        
        copy_btn = tk.Button(
            button_frame,
            text="📋 Copiar Estatísticas",
            command=self.copy_stats,
            font=('Arial', 11, 'bold'),
            bg='#4caf50',
            fg='white',
            relief='raised',
            cursor='hand2',
            padx=20,
            pady=10
        )
        copy_btn.pack(side='right', expand=True, fill='x', padx=(5, 0))
        
        # Inicializar
        self.analyze_text()
    
    def create_stat_card(self, parent, icon, label):
        """Cria um card de estatística"""
        card_frame = tk.Frame(parent, bg='#667eea', relief='raised', bd=0)
        card_frame.pack_propagate(False)
        
        tk.Label(
            card_frame,
            text=icon,
            font=('Arial', 24),
            bg='#667eea',
            fg='white'
        ).pack(pady=(10, 5))
        
        value_label = tk.Label(
            card_frame,
            text='0',
            font=('Arial', 20, 'bold'),
            bg='#667eea',
            fg='white'
        )
        value_label.pack()
        
        tk.Label(
            card_frame,
            text=label,
            font=('Arial', 9),
            bg='#667eea',
            fg='white'
        ).pack(pady=(5, 10))
        
        return {'frame': card_frame, 'value_label': value_label}
    
    def analyze_text(self):
        """Analisa o texto e atualiza estatísticas"""
        text = self.text_area.get('1.0', tk.END)
        
        # Contagem básica
        words = self.count_words(text)
        chars = len(text) - 1  # -1 para remover o \n final do tkinter
        chars_no_space = len(re.sub(r'\s', '', text))
        lines = text.count('\n')
        paragraphs = self.count_paragraphs(text)
        reading_time = self.calculate_reading_time(words)
        
        # Contagem avançada
        sentences = self.count_sentences(text)
        vowels = len(re.findall(r'[aeiouáéíóúâêîôûãõàèìòù]', text, re.IGNORECASE))
        consonants = len(re.findall(r'[bcdfghjklmnpqrstvwxyzçñ]', text, re.IGNORECASE))
        longest = self.find_longest_word(text)
        
        # Atualizar displays
        self.stat_cards['words'].config(text=str(words))
        self.stat_cards['chars'].config(text=str(chars))
        self.stat_cards['chars_no_space'].config(text=str(chars_no_space))
        self.stat_cards['lines'].config(text=str(lines))
        self.stat_cards['paragraphs'].config(text=str(paragraphs))
        self.stat_cards['reading_time'].config(text=reading_time)
        
        self.detail_labels['sentences'].config(text=str(sentences))
        self.detail_labels['vowels'].config(text=str(vowels))
        self.detail_labels['consonants'].config(text=str(consonants))
        self.detail_labels['longest'].config(text=longest or '-')
        
        # Atualizar frequência
        self.update_word_frequency(text)
    
    def count_words(self, text):
        """Conta palavras no texto"""
        if not text.strip():
            return 0
        words = re.findall(r'\b\w+\b', text)
        return len(words)
    
    def count_paragraphs(self, text):
        """Conta parágrafos"""
        if not text.strip():
            return 0
        paragraphs = re.split(r'\n\s*\n', text.strip())
        return len([p for p in paragraphs if p.strip()])
    
    def count_sentences(self, text):
        """Conta frases"""
        if not text.strip():
            return 0
        sentences = re.split(r'[.!?]+', text)
        return len([s for s in sentences if s.strip()])
    
    def calculate_reading_time(self, words):
        """Calcula tempo de leitura (200 palavras/min)"""
        if words == 0:
            return '0 min'
        minutes = max(1, words // 200)
        return f'{minutes} min'
    
    def find_longest_word(self, text):
        """Encontra palavra mais longa"""
        words = re.findall(r'\b\w+\b', text)
        if not words:
            return ''
        return max(words, key=len)
    
    def update_word_frequency(self, text):
        """Atualiza lista de palavras mais frequentes"""
        self.freq_listbox.delete(0, tk.END)
        
        if not text.strip():
            self.freq_listbox.insert(0, "Nenhuma palavra ainda...")
            return
        
        # Contar frequência
        words = re.findall(r'\b\w+\b', text.lower())
        words = [w for w in words if len(w) > 2]  # Ignorar palavras curtas
        
        if not words:
            self.freq_listbox.insert(0, "Nenhuma palavra significativa...")
            return
        
        frequency = Counter(words)
        top_10 = frequency.most_common(10)
        
        for word, count in top_10:
            self.freq_listbox.insert(tk.END, f"{word:<20} {count}x")
    
    def clear_text(self):
        """Limpa o texto"""
        self.text_area.delete('1.0', tk.END)
        self.analyze_text()
    
    def copy_stats(self):
        """Copia estatísticas para clipboard"""
        stats = f"""
📊 ESTATÍSTICAS DO TEXTO

📝 Palavras: {self.stat_cards['words'].cget('text')}
🔤 Caracteres: {self.stat_cards['chars'].cget('text')}
📄 Sem Espaços: {self.stat_cards['chars_no_space'].cget('text')}
📋 Linhas: {self.stat_cards['lines'].cget('text')}
📖 Parágrafos: {self.stat_cards['paragraphs'].cget('text')}
⏱️ Tempo de Leitura: {self.stat_cards['reading_time'].cget('text')}

📊 DETALHES AVANÇADOS

Frases: {self.detail_labels['sentences'].cget('text')}
Vogais: {self.detail_labels['vowels'].cget('text')}
Consoantes: {self.detail_labels['consonants'].cget('text')}
Palavra mais longa: {self.detail_labels['longest'].cget('text')}
        """.strip()
        
        self.root.clipboard_clear()
        self.root.clipboard_append(stats)
        messagebox.showinfo("✅ Copiado!", "Estatísticas copiadas para a área de transferência!")


def main():
    root = tk.Tk()
    app = WordCounter(root)
    root.mainloop()


if __name__ == "__main__":
    main()
