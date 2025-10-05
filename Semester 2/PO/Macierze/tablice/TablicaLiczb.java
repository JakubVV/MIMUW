package tablice;

public abstract class TablicaLiczb  {

    private final int wymiar;

    public TablicaLiczb(int wymiar) {
        this.wymiar = wymiar;
    }

    public abstract TablicaLiczb suma(Skalar składnik);

    public abstract TablicaLiczb suma(Wektor składnik) throws NiezgodnośćRozmiarów;

    public abstract TablicaLiczb suma(Macierz składnik) throws NiezgodnośćRozmiarów;

    public abstract TablicaLiczb iloczyn(Skalar czynnik);

    public abstract TablicaLiczb iloczyn(Wektor czynnik) throws NiezgodnośćRozmiarów;

    public abstract TablicaLiczb iloczyn(Macierz czynnik) throws NiezgodnośćRozmiarów;

    public abstract TablicaLiczb negacja();

    public abstract void dodaj(Skalar składnik);

    public abstract void dodaj(Wektor składnik) throws NiezgodnośćRozmiarów;

    public abstract void dodaj(Macierz składnik) throws NiezgodnośćRozmiarów;

    public abstract void przemnóż(Skalar składnik);

    public abstract void przemnóż(Wektor składnik) throws NiezgodnośćRozmiarów;

    public abstract void przemnóż(Macierz składnik) throws NiezgodnośćRozmiarów;

    public abstract void zaneguj();

    public abstract void przypisz(Skalar skalar);

    public abstract void przypisz(Wektor wektor) throws NiezgodnośćRozmiarów;

    public abstract void przypisz(Macierz macierz) throws NiezgodnośćRozmiarów;

    public int wymiar() {
        return wymiar;
    }

    public abstract int[] kształt();

    public abstract int liczba_elementów();

    public abstract void transponuj() throws BłądTranspozycji;

    public abstract TablicaLiczb kopia();

    public abstract double daj(int ... tab) throws NiezgodnaLiczbaArgumentów;

}
