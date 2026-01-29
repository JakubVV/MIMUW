import argparse

from Scraper.Scraper import Scraper

def args_to_dict() -> dict[str, any]:

    parser = argparse.ArgumentParser(description="Wiki Scraper")

    parser.add_argument('--summary', type=str, help="Get the summary of the given phrase from the wiki page.")

    parser.add_argument('--table', type=str, help="Get the specified table from the wiki page.")
    parser.add_argument('--number', type=int, help="Argument for --table: table number to extract (1-based index).")
    parser.add_argument('--first-row-is-header', action='store_true', help="Argument for --table: treat the first row as header.")

    parser.add_argument('--count-words', type=str, help="Count the frequency of each word on the wiki page for the given phrase.")

    parser.add_argument('--analyze-relative-word-frequency', action='store_true', help="Analyze relative word frequency.")
    parser.add_argument('--mode', type=str, help="Argument for --analyze-relative-word-frequency: mode of analysis.")
    parser.add_argument('--count', type=int, help="Argument for --analyze-relative-word-frequency: count of words to analyze.")
    parser.add_argument('--chart', type=str, help="Argument for --analyze-relative-word-frequency: type of chart to display.")

    parser.add_argument('--auto-count-words', type=str, help="Automatically count words starting from the given phrase.")
    parser.add_argument('--depth', type=int, help="Argument for --auto-count-words: depth of traversal.")
    parser.add_argument('--wait', type=int, help="Argument for --auto-count-words: wait time between requests in seconds.")

    args = parser.parse_args()
    return vars(args)

def __main__():
    scraper = Scraper("https://bulbapedia.bulbagarden.net/wiki", offline=False)
    args = args_to_dict()
    if args['summary']:
        scraper.summary(args['summary'])
    elif args['table'] and args['number']:
        scraper.table(args['table'], args['number'], args['first_row_is_header'])
    elif args['count_words']:
        scraper.count_words(args['count_words'])
    elif args['analyze_relative_word_frequency'] and args['mode'] and args['count']:
        scraper.analyze_relative_word_frequency(args['mode'], args['count'], args['chart'])
    elif args['auto_count_words'] and args['depth'] and args['wait']:
        scraper.auto_count_words(args['auto_count_words'], args['depth'], args['wait'])
    
if __name__ == "__main__":
    __main__()
