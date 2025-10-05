package tablice;

import java.util.Arrays;

public class Macierz extends TablicaLiczb {

    private double[][] wyrazy;

    public Macierz(double[][] wyrazy) {
        super(2);
        this.wyrazy = wyrazy;
    }

    @Override
    public TablicaLiczb suma(Skalar składnik) {
        double[][] nowe = new double[wyrazy.length][];
        for (int i = 0; i < wyrazy.length; i++)
            nowe[i] = Arrays.copyOf(wyrazy[i], wyrazy[i].length);
        for (int i = 0; i < nowe.length; i++)
            for (int j = 0; j < nowe[0].length; j++)
                nowe[i][j] += składnik.daj();
        return new Macierz(nowe);
    }

    @Override
    public TablicaLiczb suma(Wektor składnik) throws NiezgodnośćRozmiarów {
        double[][] nowa = new double[wyrazy.length][];
        for (int i = 0; i < wyrazy.length; i++)
            nowa[i] = Arrays.copyOf(wyrazy[i], wyrazy[i].length);
        if (składnik.czyWierszowy()) {
            if (wyrazy[0].length != składnik.liczba_elementów())
                throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
            for (int i = 0; i < wyrazy.length; i++)
                for (int j = 0; j < wyrazy[0].length; j++)
                    nowa[i][j] += składnik.daj(j);
        }
        else {
            if (wyrazy.length != składnik.liczba_elementów())
                throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
            for (int i = 0; i < wyrazy[0].length; i++)
                for (int j = 0; j < wyrazy.length; j++)
                    nowa[j][i] += składnik.daj(j);
        }
        return new Macierz(nowa);
    }

    @Override
    public TablicaLiczb suma(Macierz składnik) throws NiezgodnośćRozmiarów {
        if (!Arrays.equals(kształt(), składnik.kształt()))
            throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
        double[][] nowa = new double[wyrazy.length][];
        for (int i = 0; i < wyrazy.length; i++)
            nowa[i] = Arrays.copyOf(wyrazy[i], wyrazy[i].length);
        for (int i = 0; i < wyrazy.length; i++)
            for (int j = 0; j < wyrazy[0].length; j++)
                nowa[i][j] = wyrazy[i][j] + składnik.daj(i, j);
        return new Macierz(nowa);
    }

    @Override
    public TablicaLiczb iloczyn(Skalar czynnik) {
        double[][] nowe = new double[wyrazy.length][];
        for (int i = 0; i < wyrazy.length; i++)
            nowe[i] = Arrays.copyOf(wyrazy[i], wyrazy[i].length);
        for (int i = 0; i < nowe.length; i++)
            for (int j = 0; j < nowe[0].length; j++)
                nowe[i][j] *= czynnik.daj();
        return new Macierz(nowe);
    }

    @Override
    public TablicaLiczb iloczyn(Wektor czynnik) throws NiezgodnośćRozmiarów {
        if (!czynnik.czyWierszowy()) {
            if (czynnik.liczba_elementów() != wyrazy[0].length)
                throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
            double[] nowe = new double[wyrazy.length];
            for (int i = 0; i < wyrazy.length; i++)
                for (int j = 0; j < wyrazy[0].length; j++)
                    nowe[i] += wyrazy[i][j] * czynnik.daj(j);
            return new Wektor(nowe, czynnik.czyWierszowy());
        }
        else {
            if (wyrazy[0].length != 1)
                throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
            double[] nowe = new double[wyrazy[0].length];
            for (int i = 0; i < wyrazy[0].length; i++)
                for (int j = 0; j < wyrazy.length; j++)
                    nowe[i] += wyrazy[j][i] * czynnik.daj(j);
            return new Wektor(nowe, czynnik.czyWierszowy());
        }
    }

    @Override
    public TablicaLiczb iloczyn(Macierz czynnik) throws NiezgodnośćRozmiarów {
        if (wyrazy[0].length != czynnik.kształt()[0])
            throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
        double[][] nowy = new double[wyrazy.length][czynnik.kształt()[1]];
        for (int i = 0; i < nowy.length; i++)
            for (int j = 0; j < nowy[0].length; j++)
                for (int k = 0; k < wyrazy[0].length; k++)
                    nowy[i][j] += wyrazy[i][k] * czynnik.daj(k, j);
        return new Macierz(nowy);
    }

    @Override
    public TablicaLiczb negacja() {
        double[][] nowe = new double[wyrazy.length][];
        for (int i = 0; i < wyrazy.length; i++)
            nowe[i] = Arrays.copyOf(wyrazy[i], wyrazy[i].length);
        for (int i = 0; i < nowe.length; i++)
            for (int j = 0; j < nowe[0].length; j++)
                if (Double.compare(nowe[i][j], 0.0) != 0)
                    nowe[i][j] *= -1;
        return new Macierz(nowe);
    }

    @Override
    public void dodaj(Skalar składnik) {
        for (int i = 0; i < wyrazy.length; i++)
            for (int j = 0; j < wyrazy[0].length; j++)
                wyrazy[i][j] += składnik.daj();
    }

    @Override
    public void dodaj(Wektor składnik) throws NiezgodnośćRozmiarów {
        if (składnik.czyWierszowy()) {
            if (wyrazy[0].length != składnik.liczba_elementów())
                throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
            for (int i = 0; i < wyrazy.length; i++)
                for (int j = 0; j < wyrazy[0].length; j++)
                    wyrazy[i][j] += składnik.daj(j);
        }
        else {
            if (wyrazy.length != składnik.liczba_elementów())
                throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
            for (int i = 0; i < wyrazy.length; i++)
                for (int j = 0; j < wyrazy[0].length; j++)
                    wyrazy[j][i] += składnik.daj(j);
        }
    }

    @Override
    public void dodaj(Macierz składnik) throws NiezgodnośćRozmiarów {
        if (!Arrays.equals(kształt(), składnik.kształt()))
            throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
        for (int i = 0; i < wyrazy.length; i++)
            for (int j = 0; j < wyrazy[0].length; j++)
                wyrazy[i][j] += składnik.daj(i, j);
    }

    @Override
    public void przemnóż(Skalar czynnik) {
        for (int i = 0; i < wyrazy.length; i++)
            for (int j = 0; j < wyrazy[0].length; j++)
                wyrazy[i][j] *= czynnik.daj();
    }

    @Override
    public void przemnóż(Wektor czynnik) throws NiezgodnośćRozmiarów {
        TablicaLiczb wynik = this.iloczyn(czynnik);
        if (Arrays.equals(wynik.kształt(), kształt()))
            if (wynik.wymiar() == 0)
                this.przypisz((Skalar) wynik);
            else if (wynik.wymiar() == 0)
                this.przypisz((Wektor) wynik);
            else
                this.przypisz((Macierz) wynik);
    }

    @Override
    public void przemnóż(Macierz czynnik) throws NiezgodnośćRozmiarów {
        TablicaLiczb wynik = this.iloczyn(czynnik);
        if (Arrays.equals(wynik.kształt(), kształt()))
            this.przypisz((Macierz) wynik);
    }

    @Override
    public void zaneguj() {
        for (int i = 0; i < wyrazy.length; i++)
            for (int j = 0; j < wyrazy[0].length; j++)
                if (Double.compare(wyrazy[i][j], 0.0) != 0)
                    wyrazy[i][j] *= -1;
    }

    @Override
    public void przypisz(Skalar skalar) {
        for (int i = 0; i < wyrazy.length; i++)
            for (int j = 0; j < wyrazy[0].length; j++)
                wyrazy[i][j] = skalar.daj();
    }

    @Override
    public void przypisz(Wektor wektor) throws NiezgodnośćRozmiarów {
        if (wektor.czyWierszowy()) {
            if (wyrazy[0].length != wektor.liczba_elementów())
                throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
            for (int i = 0; i < wyrazy.length; i++)
                for (int j = 0; j < wyrazy[0].length; j++)
                    wyrazy[i][j] = wektor.daj(j);
        }
        else {
            if (wyrazy.length != wektor.liczba_elementów())
                throw new NiezgodnośćRozmiarów("Niezgodne rozmiary.");
            for (int i = 0; i < wyrazy[0].length; i++)
                for (int j = 0; j < wyrazy.length; j++)
                    wyrazy[j][i] = wektor.daj(j);
        }
    }
    @Override
    public void przypisz(Macierz macierz) throws NiezgodnośćRozmiarów {
        if (!Arrays.equals(this.kształt(), macierz.kształt()))
            throw new NiezgodnośćRozmiarów("Niezgodne rozmiary macierzy.");
        for (int i = 0; i < wyrazy.length; i++)
            for (int j = 0; j < wyrazy[0].length; j++)
                wyrazy[i][j] = macierz.daj(i, j);
    }

    @Override
    public int liczba_elementów() {
        return wyrazy.length * wyrazy[0].length;
    }

    @Override
    public int[] kształt() {
        int[] wynik = new int[2];
        wynik[0] = wyrazy.length;
        wynik[1] = wyrazy[0].length;
        return wynik;
    }

    public double daj(int wiersz, int kolumna) throws ZłyIndeks {
        if (wiersz >= 0 && wiersz < wyrazy.length && kolumna >= 0 && kolumna < wyrazy[0].length)
            return wyrazy[wiersz][kolumna];
        else throw new ZłyIndeks("Błędny numer indeksu.");
    }

    public void ustaw(double x, int wiersz, int kolumna) throws ZłyIndeks {
        if (!(wiersz >= 0 && wiersz < wyrazy.length &&
            kolumna >= 0 && kolumna < wyrazy[0].length))
            throw new ZłyIndeks("Błędny numer indeksu");
        wyrazy[wiersz][kolumna] = x;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < wyrazy.length; i++) {
            for (int j = 0; j < wyrazy[0].length; j++)
                s.append(String.valueOf(wyrazy[i][j]) + " ");
            if (i != wyrazy.length - 1)
                s.append("\n");
        }
        return String.valueOf(s);
    }

    @Override
    public void transponuj() {
        double[][] nowe = new double[wyrazy[0].length][wyrazy.length];
        for (int i = 0; i < wyrazy.length; i++)
            for (int j = 0; j < wyrazy[0].length; j++)
                nowe[j][i] = wyrazy[i][j];
        wyrazy = nowe;
    }

    @Override
    public TablicaLiczb kopia() {
        double[][] nowe = new double[wyrazy.length][];
        for (int i = 0; i < wyrazy.length; i++)
            nowe[i] = Arrays.copyOf(wyrazy[i], wyrazy[i].length);
        return new Macierz(nowe);
    }

    public Macierz wycinek(int wierszPoczątkowy, int wierszKońcowy, int kolumnaPoczątkowa, int kolumnaKońcowa) throws BłędneZakresy {
        if (!(wierszPoczątkowy >= 0 && wierszPoczątkowy < wyrazy.length &&
            wierszKońcowy >= 0 && wierszKońcowy < wyrazy.length &&
            kolumnaPoczątkowa >= 0 && kolumnaPoczątkowa < wyrazy[0].length &&
            kolumnaKońcowa >= 0 && kolumnaKońcowa < wyrazy[0].length &&
            wierszKońcowy > wierszPoczątkowy && kolumnaPoczątkowa < kolumnaKońcowa))
            throw new BłędneZakresy("Niezgodne zakresy tworzenia wycinka.");
        double[][] noweWyrazy = new double[wierszKońcowy - wierszPoczątkowy + 1][kolumnaKońcowa - kolumnaPoczątkowa + 1];
        for (int i = wierszPoczątkowy; i <= wierszKońcowy; i++)
            for (int j = kolumnaPoczątkowa; j <= kolumnaKońcowa; j++)
                noweWyrazy[i - wierszPoczątkowy][j - kolumnaPoczątkowa] = wyrazy[i][j];
        return new WycinekMacierzy(wierszPoczątkowy, wierszKońcowy, kolumnaPoczątkowa, kolumnaKońcowa, this, noweWyrazy);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Macierz))
            return false;
        else {
            Macierz m = (Macierz) o;
            if (kształt()[0] != m.kształt()[0] || kształt()[1] != m.kształt()[1])
                return false;
            for (int i = 0; i < wyrazy.length; i++)
                for (int j = 0; j < wyrazy[0].length; j++)
                    if (Double.compare(wyrazy[i][j], m.daj(i, j)) != 0)
                        return false;
            return true;
        }
    }

    @Override
    public double daj(int ... tab) throws NiezgodnaLiczbaArgumentów, ZłyIndeks {
        if (tab.length != 2)
            throw new NiezgodnaLiczbaArgumentów("Liczba argumentów nie wynosi 2.");
        else return daj(tab[0], tab[1]);
    }

}