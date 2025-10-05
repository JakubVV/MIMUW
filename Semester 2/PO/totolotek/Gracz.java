package totolotek;

import java.util.ArrayList;

public abstract class Gracz {

    private final String imię;

    private final String nazwisko;

    private final String PESEL;

    protected ArrayList<Kupon> kupony;

    public Gracz(String imię, String nazwisko, String PESEL) {
        this.imię = imię;
        this.nazwisko = nazwisko;
        this.PESEL = PESEL;
        kupony = new ArrayList<>();
    }

    public Kwota odbierzNagrody(Centrala centrala) {
        ArrayList<Kupon> doOddania = new ArrayList<>();
        Kwota suma = new Kwota();
        for (int i = 0; i < kupony.size(); i++) {
            Kupon kupon = kupony.get(i);
            if (centrala.ostatnieLosowanie() >= kupon.pierwszeLosowanie() &&
                centrala.ostatnieLosowanie() <= kupon.pierwszeLosowanie() + kupon.liczbaLosowań() - 1) {
                Kwota należność = kupon.należność(centrala, centrala.ostatnieLosowanie());
                suma = suma.plus(należność);
                doOddania.add(kupony.get(i));
            }
        }
        for (int i = 0; i < doOddania.size(); i++)
            kupony.removeAll(doOddania);
        return suma;
    }

    public abstract void odbiór(Centrala centrala);

    public abstract void zakupKuponu(Centrala centrala);

    @Override
    public String toString() {
        StringBuilder wynik = new StringBuilder();
        wynik.append(imię + " " + nazwisko + " " + PESEL);
        wynik.append("\n");
        if (kupony.isEmpty())
            wynik.append("Brak kuponów");
        for (int i = 0; i < kupony.size(); i++)
            wynik.append(kupony.get(i).identyfikator() + " ");
        wynik.append("\n");
        return String.valueOf(wynik);
    }
}
