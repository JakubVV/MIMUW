package tablice;

import java.util.Arrays;

public class Wektor extends TablicaLiczb {

    private final double[] współrzędne;

    private boolean czyWierszowy;

    public Wektor(double[] współrzędne, boolean czyWierszowy) {
        super(1);
        this.współrzędne = współrzędne;
        this.czyWierszowy = czyWierszowy;
    }

    @Override
    public TablicaLiczb suma(Skalar składnik) {
        double[] nowe = Arrays.copyOf(współrzędne, współrzędne.length);
        for (int i = 0; i < współrzędne.length; i++)
            nowe[i] += składnik.daj();
        return new Wektor(nowe, czyWierszowy);
    }

    @Override
    public TablicaLiczb suma(Wektor składnik) throws NiezgodnośćRozmiarów {
        if (liczba_elementów() != składnik.liczba_elementów())
            throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
        double[] nowe = Arrays.copyOf(współrzędne, współrzędne.length);
        for (int i = 0; i < współrzędne.length; i++)
            nowe[i] += składnik.daj(i);
        return new Wektor(nowe, czyWierszowy);
    }

    @Override
    public TablicaLiczb suma(Macierz składnik) throws NiezgodnośćRozmiarów {
        return składnik.suma(this);
    }

    @Override
    public TablicaLiczb iloczyn(Skalar czynnik) {
        double[] nowe = Arrays.copyOf(współrzędne, współrzędne.length);
        for (int i = 0; i < współrzędne.length; i++) {
            nowe[i] *= czynnik.daj();
        }
        return new Wektor(nowe, czyWierszowy);
    }

    @Override
    public TablicaLiczb iloczyn(Wektor czynnik) throws NiezgodnośćRozmiarów {
        if (liczba_elementów() != czynnik.liczba_elementów())
            throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
        if (this.czyWierszowy == czynnik.czyWierszowy()) {
            double wynik = 0;
            for (int i = 0; i < współrzędne.length; i++)
                wynik += współrzędne[i] * czynnik.daj(i);
            return new Skalar(wynik);
        }
        else if (!czyWierszowy) {
            double[][] noweWyrazy = new double[współrzędne.length][współrzędne.length];
            for (int i = 0; i < współrzędne.length; i++)
                for (int j = 0; j < współrzędne.length; j++)
                    noweWyrazy[i][j] = współrzędne[i] * czynnik.daj(j);
            return new Macierz(noweWyrazy);
        }
        else {
            double[][] noweWyrazy = new double[1][1];
            double wynik = 0;
            for (int i = 0; i < współrzędne.length; i++)
                wynik += współrzędne[i] * czynnik.daj(i);
            noweWyrazy[0][0] = wynik;
            return new Macierz(noweWyrazy);
        }
    }

    @Override
    public TablicaLiczb iloczyn(Macierz czynnik) throws NiezgodnośćRozmiarów {
        if (czyWierszowy) {
            if (liczba_elementów() != czynnik.kształt()[0])
                throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
            double[] nowe = new double[czynnik.kształt()[1]];
            for (int i = 0; i < czynnik.kształt()[1]; i++)
                for (int j = 0; j < liczba_elementów(); j++)
                    nowe[i] += współrzędne[j] * czynnik.daj(j, i);
            return new Wektor(nowe, czyWierszowy);
        }
        else {
            double[][] doMacierzy = new double[liczba_elementów()][1];
            Macierz m = new Macierz(doMacierzy);
            return m.iloczyn(czynnik);
        }
    }

    @Override
    public TablicaLiczb negacja() {
        double[] nowe = new double[liczba_elementów()];
        for (int i = 0; i < liczba_elementów(); i++)
            if (Double.compare(współrzędne[i], 0.0) != 0)
                nowe[i] = współrzędne[i] * (-1);
        return new Wektor(nowe, czyWierszowy);
    }

    @Override
    public void dodaj(Skalar składnik) {
        for (int i = 0; i < współrzędne.length; i++)
            współrzędne[i] += składnik.daj();
    }

    @Override
    public void dodaj(Wektor składnik) throws NiezgodnośćRozmiarów {
        TablicaLiczb wynik = this.suma(składnik);
        if (wynik.wymiar() <= wymiar() && Arrays.equals(wynik.kształt(), kształt()))
            if (wynik.wymiar() == 0)
                this.przypisz((Skalar) wynik);
            else if (wynik.wymiar() == 1)
                this.przypisz((Wektor) wynik);
    }

    @Override
    public void dodaj(Macierz składnik) throws NiezgodnośćRozmiarów {
        throw new NiezgodnośćRozmiarów("Nie można zapisać wyniku do Wektor.");
    }

    @Override
    public void przemnóż(Skalar składnik) {
        for (int i = 0; i < współrzędne.length; i++)
            współrzędne[i] *= składnik.daj();
    }

    @Override
    public void przemnóż(Wektor składnik) throws NiezgodnośćRozmiarów {
        TablicaLiczb wynik = this.iloczyn(składnik);
        if (wynik.wymiar() <= wymiar() && Arrays.equals(wynik.kształt(), kształt())) {
            if (wynik.wymiar() == 0)
                this.przypisz((Skalar) wynik);
            else if (wynik.wymiar() == 1)
                this.przypisz((Wektor) wynik);
        }
        else {
            throw new NiezgodnośćRozmiarów("Nie można zapisać wyniku do Wektor.");
        }
    }

    @Override
    public void przemnóż(Macierz składnik) throws NiezgodnośćRozmiarów {
        throw new NiezgodnośćRozmiarów("Nie można zapisać wyniku do Wektor.");
    }

    @Override
    public void zaneguj() {
        for (int i = 0; i < współrzędne.length; i++)
            if (Double.compare(współrzędne[i], 0.0) != 0)
                współrzędne[i] *= -1;
    }

    @Override
    public void przypisz(Skalar skalar) {
        for (int i = 0; i < współrzędne.length; i++)
            współrzędne[i] = skalar.daj();
    }

    @Override
    public void przypisz(Wektor wektor) throws NiezgodnośćRozmiarów {
        if (liczba_elementów() != wektor.liczba_elementów())
            throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
        for (int i = 0; i < liczba_elementów(); i++)
            współrzędne[i] = wektor.daj(i);
        czyWierszowy = wektor.czyWierszowy();
    }

    @Override
    public void przypisz(Macierz macierz) throws NiezgodnośćRozmiarów {
        throw new NiezgodnośćRozmiarów("Nie można zapisać wyniku do Wektor.");
    }

    @Override
    public int liczba_elementów() {
        return współrzędne.length;
    }

    @Override
    public int[] kształt() {
        int[] wynik = new int[1];
        wynik[0] = liczba_elementów();
        return wynik;
    }

    public boolean czyWierszowy() {
        return czyWierszowy;
    }

    public double daj(int indeks) throws ZłyIndeks {
        if (indeks >= 0 && indeks < liczba_elementów())
            return współrzędne[indeks];
        else
            throw new ZłyIndeks("Zły indeks.");
    }

    public void ustaw(double x, int indeks) throws ZłyIndeks {
        if (indeks >= 0 && indeks < liczba_elementów())
            współrzędne[indeks] = x;
        else throw new ZłyIndeks("Zły indeks.");
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < this.liczba_elementów(); i++) {
            s.append(współrzędne[i]);
            s.append(" ");
        }
        if (czyWierszowy)
            s.append("poziomy");
        else
            s.append("pionowy");
        return String.valueOf(s);
    }

    @Override
    public void transponuj() {
        czyWierszowy = !czyWierszowy;
    }

    @Override
    public TablicaLiczb kopia() {
        return new Wektor(Arrays.copyOf(współrzędne, współrzędne.length), czyWierszowy);
    }

    public Wektor wycinek(int indeksPoczątkowy, int indeksKońcowy) throws BłędneZakresy {
        if (!(indeksPoczątkowy >= 0 && indeksPoczątkowy < liczba_elementów() &&
            indeksKońcowy >= 0 && indeksKońcowy < liczba_elementów() &&
            indeksKońcowy > indeksPoczątkowy))
            throw new BłędneZakresy("Błędny zakres tworzenia wycinka.");
        double[] noweWyrazy = new double[indeksKońcowy - indeksPoczątkowy + 1];
        for (int i = indeksPoczątkowy; i <= indeksKońcowy; i++)
            noweWyrazy[i - indeksPoczątkowy] = współrzędne[i];
        return new WycinekWektora(indeksPoczątkowy, indeksKońcowy, this, noweWyrazy, czyWierszowy);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Wektor))
            return false;
        else {
            Wektor w = (Wektor) o;
            if (liczba_elementów() != w.liczba_elementów())
                return false;
            if (czyWierszowy != w.czyWierszowy)
                return false;
            for (int i = 0; i < liczba_elementów(); i++)
                if (Double.compare(współrzędne[i], w.daj(i)) != 0)
                    return false;
            return true;
        }
    }

    @Override
    public double daj(int ... tab) throws NiezgodnaLiczbaArgumentów {
        if (tab.length != 1)
            throw new NiezgodnaLiczbaArgumentów("Liczba argumentów nie wynosi 1.");
        else return daj(tab[0]);
    }
}