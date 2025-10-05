package tablice;

public class WycinekMacierzy extends Macierz {

    private final int wierszPoczątek;
    private final int wierszKoniec;
    private final int kolumnaPoczątek;
    private final int kolumnaKoniec;

    private final Macierz oryginał;

    public WycinekMacierzy(int wp, int wk, int kp, int kk, Macierz m, double[][] wyrazy) {
        super(wyrazy);
        wierszPoczątek = wp;
        wierszKoniec = wk;
        kolumnaKoniec = kk;
        kolumnaPoczątek = kp;
        oryginał = m;
    }

    public void aktualizuj() {
        for (int i = wierszPoczątek; i <= wierszKoniec; i++)
            for (int j = kolumnaPoczątek; j <=kolumnaKoniec; j++)
                oryginał.ustaw(this.daj(i - wierszPoczątek, j - wierszPoczątek), i, j);
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
        this.aktualizuj();
    }

    @Override
    public void ustaw(double x, int wiersz, int kolumna) throws ZłyIndeks {
        super.ustaw(x, wiersz, kolumna);
        this.aktualizuj();
    }

    @Override
    public void transponuj() {
        if (kształt()[0] == kształt()[1])
            super.transponuj();
        this.aktualizuj();
    }
}
