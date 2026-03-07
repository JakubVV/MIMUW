import os
import time

import numpy as np
import pandas as pd
import requests
import json
import matplotlib.pyplot as plt
from pathlib import Path
from wordfreq import zipf_frequency
from bs4 import BeautifulSoup as bs

class Scraper:

    def __get_url(self, phrase: str) -> str:
        phrase = phrase.replace(" ", "_")
        return f"{self.url}/{phrase}"
    
    def __parse_html(self, phrase: str) -> bs:
        if self.offline:
            return None
        new_url = self.__get_url(phrase)
        try:
            response = requests.get(new_url)
            response.raise_for_status()
        except requests.exceptions.RequestException as e:
            print(e)
            return None
        soup = bs(response.text, 'html.parser')
        return soup

    def __init__(self, url: str, offline: bool) -> None:
        self.url = url
        self.offline = offline

    def summary(self, phrase: str) -> str:
        if not self.offline:
            soup = self.__parse_html(phrase)
        else:
            phrase = phrase.replace(" ", "_")
            with open(f"{phrase}.html", 'r', encoding='utf-8') as f:
                soup = bs(f, 'html.parser')
        if soup is None:
            return
        first_paragraph = soup.find('p')
        # Print the first paragraph
        if first_paragraph:
            print("--- Summary ---")
            print()
            print(first_paragraph.get_text())
            print("----------------")
            return first_paragraph.get_text()
        else:
            return None
        
    
    def table(self, phrase: str, number: int, first_row_is_header: bool = False) -> None:
        if not self.offline:
            soup = self.__parse_html(phrase)
        else:
            phrase = phrase.replace(" ", "_")
            with open(f"{phrase}.html", 'r', encoding='utf-8') as f:
                soup = bs(f, 'html.parser')
        if soup is None:
            return None
        
        tables = soup.find_all('table')
        if number > len(tables):
            raise ValueError("Table number exceeds number of tables on page.")
        
        # The table exists
        table = tables[number - 1]
        
        rows = []
        for tr in table.find_all('tr'):
            cells = [td.get_text(strip=True) for td in tr.find_all(['td', 'th'])]
            if cells:
                rows.append(cells)

        if not rows:
            print("No data found in the specified table.")
            return
        
        # Max number of columns
        max_cols = max(len(row) for row in rows)

        # Fill each row to max_cols
        for i in range(len(rows)):
            while len(rows[i]) < max_cols:
                rows[i].append("")  
        
        # Create DataFrame
        if first_row_is_header:
            df = pd.DataFrame(rows[1:], columns=rows[0])
        else:
            df = pd.DataFrame(rows)

        phrase = phrase.replace(" ", "_")
        df.to_csv(f"{phrase}.csv", index=False)

        print("--- Table ---")
        print()
        print(df.to_string(index=False))
        print("----------------")
    
    def __load_word_count_json(self, path="word-counts.json") -> dict:
        if not os.path.exists(path):
            return dict()
        # Load existing data
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        return data

    def count_words(self, phrase: str) -> None:
        if not self.offline:
            soup = self.__parse_html(phrase)
        else:
            phrase = phrase.replace(" ", "_")
            with open(f"{phrase}.html", 'r', encoding='utf-8') as f:
                soup = bs(f, 'html.parser')
        if soup is None:
            return
        
        paragraphs = soup.find_all('p')
        word_count = dict()
        # Count words in paragraphs
        for p in paragraphs:
            text = p.get_text()
            words = text.split()
            for word in words:
                word = word.lower().strip('.,!?;"()[]{}')
                if word:
                    word_count[word] = word_count.get(word, 0) + 1
        
        # Load existing word counts
        dict_from_json = self.__load_word_count_json()

        # Update counts
        for word, count in word_count.items():
            dict_from_json[word] = dict_from_json.get(word, 0) + count
        with open("word-counts.json", 'w', encoding='utf-8') as f:
            json.dump(dict_from_json, f, ensure_ascii=False, indent=4)
            print(f"Word counts updated and saved to word-counts.json")

    def analyze_relative_word_frequency(self, mode: str, count: int, chart: str):
        if not mode or not count:
            return
        if mode != "article" and mode != "language":
            raise ValueError("Mode must be either 'article' or 'language'.")
        
        # Load word counts
        dict_from_json = self.__load_word_count_json()
        top_n = dict(sorted(dict_from_json.items(), key=lambda item: item[1], reverse=True)[:count])
        
        # Get language frequencies
        language_frequencies = dict()
        for word in top_n.keys():
            frequency = zipf_frequency(word, 'en')
            language_frequencies[word] = frequency
        
        # Normalize frequencies
        max_wiki_freq = max(top_n.values()) if top_n else 1
        max_lang_freq = max(language_frequencies.values()) if language_frequencies else 1

        wiki_normmalized = {w: top_n[w] / max_wiki_freq for w in top_n.keys()}
        lang_normalized = {w: language_frequencies[w] / max_lang_freq for w in top_n.keys()}

        data = []
        for w in top_n.keys():
            data.append({
                'word': w,
                'wiki_frequency': wiki_normmalized[w],
                'language_frequency': lang_normalized[w]
            })
        
        df = pd.DataFrame(data)

        if mode == "article":
            df = df.sort_values(by='wiki_frequency', ascending=False)
        else:
            df = df.sort_values(by='language_frequency', ascending=False)

        print("--- Relative Word Frequency Analysis ---")
        print()
        print(df.to_string(index=False))
        print("----------------------------------------")

        # Generate chart if requested
        if chart:
            chart_path = Path(chart)
            fig, ax = plt.subplots(figsize=(max(8, count*0.5), 6))
            x = range(len(df))
            width = 0.35
            ax.bar([i - width/2 for i in x], df['wiki_frequency'], width, label='Wiki Frequency', color='skyblue')
            ax.bar([i + width/2 for i in x], df['language_frequency'], width, label='Language Frequency', color='orange')

            ax.set_xticks(x)
            ax.set_xticklabels(df['word'], rotation=45, ha='right')
            ax.set_ylabel('Normalized Frequency')
            ax.set_title(f'Relative Word Frequency ({mode})')
            ax.legend()
            plt.tight_layout()
            plt.savefig(chart_path)
            print(f"Chart saved to {chart_path}")
            plt.close()

    # Recursive helper for auto_count_words
    def __recursive_word_count(self, phrase: str, depth: int, wait: int, cur : int) -> None:
        if cur > depth:
            return
        self.count_words(phrase)
        time.sleep(wait)
        links = [a["href"][6:] for a in self.__parse_html(phrase).find_all("a", href=True)
                 if a["href"].startswith("/wiki/") and ":" not in a["href"] and 
                 a["href"] != "/wiki/Main_Page"]
        for link in links:
            self.__recursive_word_count(link, depth, wait, cur + 1)

    def auto_count_words(self, phrase: str, depth: int = 1, wait: int = 1) -> None:
        print("Counting words... ")
        self.__recursive_word_count(phrase, depth, wait, 0)
        print("Done.")
