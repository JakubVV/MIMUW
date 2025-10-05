package totolotek;

import java.util.ArrayList;


public class Centrala {

    private Kwota środki;
    private Kwota wpływy;
    public static int liczbaKolektur = 0;

    public ArrayList<Kolektura> kolektury;
    public ArrayList<Losowanie> losowania;
    public ArrayList<ArrayList<Kwota>> wygrane;

    public Kwota wygranaI;
    public Kwota wygranaII;
    public Kwota wygranaIII;
    public Kwota wygranaIV;
    public Kwota kumulacja;

    public Kwota wartośćI;
    public Kwota wartośćII;
    public Kwota wartośćIII;
    public Kwota wartośćIV;

    public Centrala(Kwota środki) {
        this.środki = środki;
        this.kolektury = new ArrayList<>();
        this.losowania = new ArrayList<>();
        this.wpływy = new Kwota();
        this.wygrane = new ArrayList<>();
        this.wygranaI = new Kwota();
        this.wygranaII = new Kwota();
        this.wygranaIII = new Kwota();
        this.wygranaIV = new Kwota();
        this.kumulacja = new Kwota();
    }

    public void przekażŚrodki(Kwota kwota) {
        this.wpływy = this.wpływy.plus(kwota);
    }

    public ArrayList<Integer> obliczWygrane(ArrayList<Gracz> gracze) {
        środki = środki.plus(wpływy.procent(49));
        wpływy.pobierz(wpływy.procent(49));
        int wygranychI = 0;
        int wygranychII = 0;
        int wygranychIII = 0;
        int wygranychIV = 0;
        int ile = 0;
        for (int i = 0; i < gracze.size(); i++) {
            Gracz x = gracze.get(i);
            for (int j = 0; j < x.kupony.size(); j++) {
                for (int k = 0; k < x.kupony.get(j).liczbaZakładów(); k++) {
                    ile = x.kupony.get(j).zakład(k).ileTrafień(losowania.getLast().wyniki());
                }
                if (ile == 6)
                    wygranychI++;
                if (ile == 5)
                    wygranychII++;
                if (ile == 4)
                    wygranychIII++;
                if (ile == 3)
                    wygranychIV++;
            }
        }
        wygranaI = wpływy.procent(44);
        wygranaII = wpływy.procent(8);
        wygranaIV = new Kwota(24, 0).razy(wygranychIV);
        Kwota robocza = new Kwota(wpływy.złote(), wpływy.grosze());
        robocza.pobierz(wygranaI);
        robocza.pobierz(wygranaII);
        robocza.pobierz(wygranaIV);
        wygranaIII = new Kwota(robocza.złote(), robocza.grosze());
        wygranaI = wygranaI.plus(kumulacja);
        ArrayList<Integer> wygrane = new ArrayList<>();
        wygrane.add(wygranychI);
        wygrane.add(wygranychII);
        wygrane.add(wygranychIII);
        wygrane.add(wygranychIV);
        return wygrane;
    }


    public void przeprowadźLosowanie(ArrayList<Gracz> gracze) {
        kumulacja = wygranaI;
        wpływy = wpływy.plus(wygranaII);
        wpływy = wpływy.plus(wygranaIII);
        wpływy = wpływy.plus(wygranaIV);
        Losowanie losowanie = new Losowanie();
        losowania.add(losowanie);
        ArrayList<Integer> wygrane = obliczWygrane(gracze);
        if (wygrane.get(0) > 0)
            wartośćI = new Kwota(2000000, 0).plus(kumulacja).procent(100 / wygrane.get(0));
        else
            wartośćI = new Kwota(2000000, 0).plus(kumulacja);
        if (wygrane.get(1) > 0)
            wartośćII = wygranaII.procent(100 / wygrane.get(1));
        else
            wartośćII = new Kwota(wygranaII.złote(), wygranaII.grosze());
        if (wygrane.get(2) > 0)
            wartośćIII = wygranaIII.procent(100 / wygrane.get(2));
        else
            wartośćIII = new Kwota(wygranaIII.złote(), wygranaIII.grosze());
        wartośćIV = new Kwota(24, 0);
        ArrayList<Kwota> kwoty = new ArrayList<>();
        kwoty.add(wartośćI);
        kwoty.add(wartośćII);
        kwoty.add(wartośćIII);
        kwoty.add(wartośćIV);
        this.wygrane.add(kwoty);
    }

    public void utwórzKolekturę() {
        kolektury.add(new Kolektura());
        liczbaKolektur++;
    }

    public void rozliczenieZaKupon(Kwota kwota) {
        long ileZakładów = kwota.złote() / 3;
        BudżetPaństwa.podatek(new Kwota(0,60).razy(ileZakładów));
        przekażŚrodki(new Kwota(2, 40).razy(ileZakładów));
    }

    public int ostatnieLosowanie() {
        if (losowania.isEmpty())
            return 0;
        return losowania.get(losowania.size() - 1).numer();
    }

    public void kupKupony(ArrayList<Gracz> gracze) {
        for (int i = 0; i < gracze.size(); i++)
            gracze.get(i).zakupKuponu(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ŚRODKI CENTRALI: ").append(środki.toString()).append("\n");
        sb.append("KOLEKTURY:\n ");
        for (int i = 0; i < kolektury.size(); i++)
            sb.append(kolektury.get(i).numer + " ");
        sb.append("\nWPŁYWY: " + wpływy + "\n");
        for (int i = 0; i < losowania.size(); i++) {
            sb.append(losowania.get(i).toString() + "\n");
            for (int j = 0; j < 4; j++)
                sb.append("Wygrana " + (j + 1) + ": ").append(wygrane.get(i).get(j)).append("\n");
        }
        return String.valueOf(sb);
    }
}
