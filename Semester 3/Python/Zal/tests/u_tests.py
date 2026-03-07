import pytest

from Scraper.Scraper import Scraper

def test_1_init():
    scraper = Scraper("https://bulbapedia.bulbagarden.net/wiki", offline=True)
    assert scraper.url == "https://bulbapedia.bulbagarden.net/wiki"
    assert scraper.offline == True

def test_2_get_url():
    scraper = Scraper("https://bulbapedia.bulbagarden.net/wiki", offline=True)
    url = scraper._Scraper__get_url("A B C D E F G")
    assert url == "https://bulbapedia.bulbagarden.net/wiki/A_B_C_D_E_F_G"

def test_3_parse_html():
    scraper = Scraper("https://bulbapedia.bulbagarden.net/wiki", offline=True)
    soup = scraper._Scraper__parse_html("Pikachu")
    assert soup is None

def test_4_parse_html_wrong_url(capsys):
    scraper = Scraper("https://thisurldoesnotexist.tld", offline=False)
    scraper._Scraper__parse_html("Pikachu") 
    text = capsys.readouterr()
    assert "HTTPSConnectionPool(host=" in text.out

    
