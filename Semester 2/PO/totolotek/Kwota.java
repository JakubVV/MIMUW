package totolotek;

public class Kwota {

    private long złote;

    private long grosze;

    public Kwota() {
        this.złote = 0;
        this.grosze = 0;
    }

    public Kwota(long złote, long grosze) {
        this.złote = złote;
        this.grosze = grosze;
    }

    public Kwota plus(Kwota druga) {
        Kwota wynik = new Kwota();
        wynik.grosze = grosze + druga.grosze;
        wynik.złote += wynik.grosze / 100;
        wynik.grosze %= 100;
        wynik.złote += złote + druga.złote;
        return wynik;
    }

    public Kwota razy(long stała) {
        Kwota wynik = new Kwota();
        wynik.grosze = grosze * stała;
        wynik.złote += wynik.grosze / 100;
        wynik.grosze %= 100;
        wynik.złote += złote * stała;
        return wynik;
    }

    public int porównaj(Kwota druga) {
        if (złote == druga.złote && grosze == druga.grosze)
            return 0;
        else if (złote > druga.złote || złote == druga.złote && grosze > druga.grosze)
            return 1;
        else return -1;
    }

    public Kwota procent(long procent) {
        Kwota wynik = new Kwota(złote, grosze);
        wynik.złote *= procent;
        wynik.grosze *= procent;
        wynik.złote /= 100;
        wynik.grosze /= 100;
        wynik.złote += wynik.grosze / 100;
        wynik.grosze %= 100;
        return wynik;
    }

    public void pobierz(Kwota druga) {
        if (this.porównaj(druga) < 0) {
            Kwota kopia = new Kwota(druga.złote, druga.grosze);
            kopia.pobierz(this);
            this.złote = 0;
            this.grosze = 0;
            BudżetPaństwa.subwencja(kopia);
            return;
        }
        Kwota wynik = new Kwota();
        wynik.złote = złote - druga.złote;
        wynik.grosze = grosze - druga.grosze;
        if (wynik.grosze < 0) {
            wynik.złote--;
            wynik.grosze += 100;
        }
        this.złote = wynik.złote;
        this.grosze = wynik.grosze;
    }

    public long złote() {
        return złote;
    }

    public long grosze() {
        return grosze;
    }

    @Override
    public String toString() {
        String złoteNapis = String.valueOf(złote);
        String groszeNapis = String.valueOf(grosze);
        if (groszeNapis.length() == 1)
            groszeNapis = "0" + groszeNapis;
        return złoteNapis + " zł " + groszeNapis + " gr";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Kwota))
            return false;
        Kwota ob = (Kwota) obj;
        return ob.złote == złote && ob.grosze == grosze;
    }
}
