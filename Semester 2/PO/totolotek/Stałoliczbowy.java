package totolotek;

import java.util.ArrayList;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class Stałoliczbowy extends Gracz {

    private final ArrayList<Integer> ulubioneLiczby;
    private final ArrayList<Integer> ulubioneKolektury;
    private int następnaKolektura;
    private Kwota stanKonta;

    public Stałoliczbowy(String imię, String nazwisko, String PESEL, ArrayList<Integer> ulubioneLiczby, ArrayList<Integer> ulubioneKolektury) {
        super(imię, nazwisko, PESEL);
        this.ulubioneLiczby = ulubioneLiczby;
        this.ulubioneKolektury = ulubioneKolektury;
        następnaKolektura = 0;
        stanKonta = new Kwota(100, 0);
    }

    public Stałoliczbowy(String imię, String nazwisko, String PESEL, ArrayList<Integer> ulubioneKolektury) {
        super(imię, nazwisko, PESEL);
        SortedSet<Integer> liczby = new TreeSet<>();
        Random random = new Random();
        while (liczby.size() < 6) {
            int liczba = random.nextInt(49) + 1;
            liczby.add(liczba);
        }
        ArrayList<Integer> wynik = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            wynik.add(liczby.first());
            liczby.removeFirst();
        }
        this.ulubioneKolektury = ulubioneKolektury;
        this.ulubioneLiczby = wynik;
        stanKonta = new Kwota(100, 0);
    }

    public void odbiór(Centrala centrala) {
        Kwota suma = super.odbierzNagrody(centrala);
        stanKonta = stanKonta.plus(suma);
    }

    @Override
    public void zakupKuponu(Centrala centrala) {
        if (kupony.isEmpty()) {
            boolean[][] zakreślenia = new boolean[8][50];
            for (int i = 0; i < ulubioneLiczby.size(); i++)
                zakreślenia[0][ulubioneLiczby.get(i) - 1] = true;
            for (int i = 1; i < 8; i++)
                zakreślenia[i][49] = true;
            boolean[] liczbaLosowań = new boolean[10];
            liczbaLosowań[9] = true;
            Blankiet blankiet = new Blankiet(zakreślenia, liczbaLosowań);
            Kwota cena = new Kwota(3, 0).razy(10);
            if (stanKonta.porównaj(cena) >= 0) {
                stanKonta.pobierz(cena);
                centrala.rozliczenieZaKupon(cena);
                int nrKolektury = ulubioneKolektury.get(następnaKolektura);
                następnaKolektura++;
                następnaKolektura %= ulubioneKolektury.size();
                kupony.add(centrala.kolektury.get(nrKolektury - 1).generujKupon(blankiet));
            }
        }
    }

    @Override
    public String toString() {
        return super.toString() + stanKonta.toString();
    }
}
