package tablice;

public class WycinekSkalara extends Skalar {

    private final Skalar oryginał;

    public WycinekSkalara(double wartość, Skalar oryginał) {
        super(wartość);
        this.oryginał = oryginał;
    }

    public void aktualizuj() {
        oryginał.ustaw(this.daj());
    }

    @Override
    public void dodaj(Skalar składnik) {
        super.dodaj(składnik);
    }

    @Override
    public void dodaj(Wektor składnik) throws NiezgodnośćRozmiarów {
        super.dodaj(składnik);
    }

    @Override
    public void dodaj(Macierz składnik) throws NiezgodnośćRozmiarów {
        super.dodaj(składnik);
    }

    @Override
    public void przemnóż(Skalar czynnik) {
        super.przemnóż(czynnik);
        this.aktualizuj();
    }

    @Override
    public void przemnóż(Wektor czynnik) throws NiezgodnośćRozmiarów {
        super.przemnóż(czynnik);
    }

    @Override
    public void przemnóż(Macierz czynnik) throws NiezgodnośćRozmiarów {
        super.przemnóż(czynnik);
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
    }

    @Override
    public void przypisz(Macierz macierz) throws NiezgodnośćRozmiarów {
        super.przypisz(macierz);
    }

    @Override
    public void ustaw(double x) {
        super.ustaw(x);
        this.aktualizuj();
    }
}
