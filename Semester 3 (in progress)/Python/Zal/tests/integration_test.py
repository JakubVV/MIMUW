from Scraper.Scraper import Scraper

def __main__():
    scraper = Scraper("https://bulbapedia.bulbagarden.net/wiki", offline=True)

    ret = scraper.summary("Team Rocket")
    assert ret is not None
    assert "villainous team in pursuit of evil and the exploitation of Pokémon. The organization is based" in ret
    print("Integration test for Scraper.summary passed.")

if __name__ == "__main__":
    __main__()
