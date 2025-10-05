package tablice;

public class WycinekWektora extends Wektor {

    private final int indeksPoczątek;
    private final int indeksKoniec;

    private final Wektor oryginał;

    public WycinekWektora(int ip, int ik, Wektor oryginał, double[] współrzędne, boolean czyWierszowy) {
        super(współrzędne, czyWierszowy);
        indeksPoczątek = ip;
        indeksKoniec = ik;
        this.oryginał = oryginał;
    }

    public void aktualizuj() {
        for (int i = indeksPoczątek; i <= indeksKoniec; i++)
            oryginał.ustaw(this.daj(i - indeksPoczątek), i);
    }

    @Override
    public void dodaj(Skalar składnik) {
        super.dodaj(składnik);
        this.aktualizuj();
    }

    @Override
    public void dodaj(Wektor składnik) throws NiezgodnośćRozmiarów {
        super.dodaj(składnik);
        this.aktualizuj();
    }

    @Override
    public void dodaj(Macierz składnik) throws NiezgodnośćRozmiarów {
        super.dodaj(składnik);
        this.aktualizuj();
    }

    @Override
    public void przemnóż(Skalar czynnik) {
        super.przemnóż(czynnik);
        this.aktualizuj();
    }

    @Override
    public void przemnóż(Wektor czynnik) throws NiezgodnośćRozmiarów {
        super.przemnóż(czynnik);
        this.aktualizuj();
    }

    @Override
    public void przemnóż(Macierz czynnik) throws NiezgodnośćRozmiarów {
        super.przemnóż(czynnik);
        this.aktualizuj();
    }

    @Override
    public void zaneguj() {
        super.zaneguj();
        this.aktualizuj();
    }

    @Override
    public void przypisz(Skalar skalar) {
        super.przypisz(skalar);
        this.aktualizuj();
    }

    @Override
    public void przypisz(Wektor wektor) throws NiezgodnośćRozmiarów {
        super.przypisz(wektor);
        this.aktualizuj();
    }

    @Override
    public void przypisz(Macierz macierz) throws NiezgodnośćRozmiarów {
        super.przypisz(macierz);
    }

    @Override
    public void ustaw(double x, int indeks) throws ZłyIndeks {
        super.ustaw(x, indeks);
        this.aktualizuj();
    }

    @Override
    public void transponuj() throws BłądTranspozycji {
        throw new BłądTranspozycji("Nie można transponować wycinka obiektu Wektor.");
    }
}
