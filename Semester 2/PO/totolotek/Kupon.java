package totolotek;

import java.util.ArrayList;
import java.util.Random;

public class Kupon {

    private static int porządek = 1;

    private final int numer;
    private final ArrayList<Zakład> zakłady;
    private final int numerKolektury;
    private final long liczbaLosowań;
    private final int pierwszeLosowanie;
    private final int liczbaZakładów;
    private final String identyfikator;

    public Kupon(ArrayList<Zakład> zakłady, long liczbaLosowań, int numerKolektury) {
        this.zakłady = zakłady;
        this.liczbaZakładów = zakłady.size();
        this.liczbaLosowań = liczbaLosowań;
        this.numer = porządek;
        this.numerKolektury = numerKolektury;
        porządek++;
        this.pierwszeLosowanie = Losowanie.porządek;
        StringBuilder sb = new StringBuilder(String.valueOf(numer));
        sb.append("-").append(numerKolektury).append("-");
        Random random = new Random();
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int cyfra = random.nextInt(9);
            sb.append(String.valueOf(cyfra));
            suma += cyfra;
        }
        sb.append("-");
        suma %= 100;
        if (suma < 10)
            sb.append("0");
        sb.append(suma);
        this.identyfikator = String.valueOf(sb);
    }

    public Kwota cena() {
        return new Kwota(3, 0).razy(liczbaLosowań * zakłady.size());
    }

    public boolean czyOstatnieLosowanie(int nrLosowania) {
        return pierwszeLosowanie + liczbaLosowań - 1 == nrLosowania;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("KUPON NR ");
        sb.append(identyfikator).append("\n");
        for (int i = 0; i < zakłady.size(); i++) {
            sb.append(i + 1 + ": ");
            sb.append(zakłady.get(i).toString());
            sb.append("\n");
        }
        sb.append("LICZBA LOSOWAŃ: ").append(liczbaLosowań).append("\n");
        sb.append("NUMERY LOSOWAŃ:").append("\n").append(" ");
        for (int i = 0; i < liczbaLosowań; i++)
            sb.append(this.pierwszeLosowanie + i).append(" ");
        sb.append("\nCENA: ").append(cena());
        return String.valueOf(sb);
    }

    public Zakład zakład(int numer) {
        return zakłady.get(numer);
    }

    public Kwota należność(Centrala centrala, int numerLosowania) {
        Losowanie losowanie = centrala.losowania.get(numerLosowania - 1);
        Kwota suma = new Kwota();
        for (int i = 0; i < zakłady.size(); i++) {
            int ile = 0;
            for (int j = 0; j < 6; j++) {
                if (losowanie.wyniki().contains(zakłady.get(i).daj(j)))
                    ile++;
            }
            if (ile == 6) {
                if (centrala.wartośćI.porównaj(new Kwota(2280, 0)) >= 0) {
                    BudżetPaństwa.podatek(centrala.wartośćI.procent(10));
                    suma = suma.plus(centrala.wartośćI.procent(90));

                }
                else
                    suma = suma.plus(centrala.wartośćI);
                centrala.wygranaI.pobierz(centrala.wartośćI);
            }
            if (ile == 5) {
                if (centrala.wartośćII.porównaj(new Kwota(2280, 0)) >= 0) {
                    BudżetPaństwa.podatek(centrala.wartośćII.procent(10));
                    suma = suma.plus(centrala.wartośćII.procent(90));
                }
                else
                    suma = suma.plus(centrala.wartośćII);
                centrala.wygranaII.pobierz(centrala.wartośćII);
            }
            if (ile == 4) {
                if (centrala.wartośćIII.porównaj(new Kwota(2280, 0)) >= 0) {
                    BudżetPaństwa.podatek(centrala.wartośćIII.procent(10));
                    suma = suma.plus(centrala.wartośćIII.procent(90));
                }
                else
                    suma = suma.plus(centrala.wartośćIII);
                centrala.wygranaIII.pobierz(centrala.wartośćIII);
            }
            if (ile == 3) {
                    suma = suma.plus(centrala.wartośćIV);
                centrala.wygranaIV.pobierz(centrala.wartośćIV);
            }
        }
        return suma;
    }

    public int liczbaZakładów() {
        return liczbaZakładów;
    }

    public long liczbaLosowań() {
        return liczbaLosowań;
    }

    public String identyfikator() {
        return identyfikator;
    }

    public int pierwszeLosowanie() {
        return pierwszeLosowanie;
    }
}
