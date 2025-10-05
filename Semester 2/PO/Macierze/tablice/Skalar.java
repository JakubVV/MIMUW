package tablice;

public class Skalar extends TablicaLiczb {

    private double wartość;

    public Skalar(double wartość) {
        super(0);
        this.wartość = wartość;
    }

    @Override
    public TablicaLiczb suma(Skalar składnik) {
        return new Skalar(wartość + składnik.daj());
    }

    @Override
    public TablicaLiczb suma(Wektor składnik) {
        return składnik.suma(this);
    }

    @Override
    public TablicaLiczb suma(Macierz składnik) {
        return składnik.suma(this);
    }

    @Override
    public TablicaLiczb iloczyn(Skalar czynnik) {
        return new Skalar(wartość * czynnik.daj());
    }

    @Override
    public TablicaLiczb iloczyn(Wektor czynnik) {
        return czynnik.iloczyn(this);
    }

    @Override
    public TablicaLiczb iloczyn(Macierz czynnik) {
        return czynnik.iloczyn(this);
    }

    @Override
    public TablicaLiczb negacja() {
        if (Double.compare(wartość, 0.0) != 0)
            return new Skalar(wartość * (-1));
        else
            return new Skalar(wartość);
    }

    @Override
    public void dodaj(Skalar składnik) {
        wartość += składnik.daj();;
    }

    @Override
    public void dodaj(Wektor składnik) throws NiezgodnośćRozmiarów {
        throw new NiezgodnośćRozmiarów("Nie można zapisać wyniku do Skalar.");
    }

    @Override
    public void dodaj(Macierz składnik) throws NiezgodnośćRozmiarów {
        throw new NiezgodnośćRozmiarów("Nie można zapisać wyniku do Skalar.");
    }

    @Override
    public void przemnóż(Skalar składnik) {
        wartość *= składnik.daj();
    }

    @Override
    public void przemnóż(Wektor składnik) throws NiezgodnośćRozmiarów {
        throw new NiezgodnośćRozmiarów("Nie można zapisać wyniku do Skalar.");
    }

    @Override
    public void przemnóż(Macierz składnik) throws NiezgodnośćRozmiarów {
        throw new NiezgodnośćRozmiarów("Nie można zapisać wyniku do Skalar.");
    }

    @Override
    public void zaneguj() {
        if (Double.compare(wartość, 0.0) != 0)
            wartość *= -1;
    }

    @Override
    public void przypisz(Skalar skalar) {
        wartość = skalar.daj();
    }

    @Override
    public void przypisz(Wektor wektor) throws NiezgodnośćRozmiarów {
        throw new NiezgodnośćRozmiarów("Nie można przypisać Wektor do Skalar.");
    }

    @Override
    public void przypisz(Macierz macierz) throws NiezgodnośćRozmiarów {
        throw new NiezgodnośćRozmiarów("Nie można przypisać Macierz do Skalar.");
    }

    @Override
    public int liczba_elementów() {
        return 1;
    }

    public double daj() {
        return wartość;
    }

    public void ustaw(double x) {
        wartość = x;
    }

    public int[] kształt() {
        int[] wynik = new int[0];
        return wynik;
    }

    @Override
    public String toString() {
        return String.valueOf(wartość);
    }

    public void transponuj() {
        return;
    }

    public TablicaLiczb kopia() {
        return new Skalar(wartość);
    }

    public Skalar wycinek() {
        return new WycinekSkalara(wartość, this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Skalar))
            return false;
        else {
            Skalar s = (Skalar) o;
            if (Double.compare(wartość, s.daj()) == 0)
                return true;
            return false;
        }
    }

    @Override
    public double daj(int ... tab) throws NiezgodnaLiczbaArgumentów {
        if (tab.length != 0)
            throw new NiezgodnaLiczbaArgumentów("Liczba argumentów nie wynosi 0.");
        else return this.daj();
    }

}
