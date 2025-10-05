package totolotek;

import java.util.ArrayList;

public class Kolektura {

    private static int porządek = 1;

    public final int numer;

    private ArrayList<Kupon> kupony;

    public Kolektura() {
        this.numer = porządek;
        porządek++;
        kupony = new ArrayList<>();
    }

    public Kupon generujKupon(long liczbaZakładów, long liczbaLosowań) {
        ArrayList<Zakład> zakłady = new ArrayList<>();
        for (int i = 0; i < liczbaZakładów; i++) {
            zakłady.add(new Zakład());
        }
        Kupon wynikowy = new Kupon(zakłady, liczbaLosowań, numer);
        kupony.add(wynikowy);
        return wynikowy;
    }

    public Kupon generujKupon(Blankiet blankiet) {
        ArrayList<Zakład> zakłady = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            ArrayList<Integer> typy = new ArrayList<>();
            if (blankiet.dajZakreślenia(i, 49))
                continue;
            for (int j = 0; j < 49; j++) {
                if (blankiet.dajZakreślenia(i, j)) {
                    typy.add(j + 1);
                }
            }
            if (typy.size() == 6)
                zakłady.add(new Zakład(typy));
        }
        long LiczbaLosowań = 1;
        for (int i = 9; i >= 0; i--) {
            if (blankiet.dajLiczbaLosowań(i)) {
                LiczbaLosowań = i + 1;
                break;
            }
        }
        Kupon wynikowy = new Kupon(zakłady, LiczbaLosowań, numer);
        kupony.add(wynikowy);
        return wynikowy;
    }

    public Kwota wyceńKupon(Blankiet blankiet) {
        ArrayList<Zakład> zakłady = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            ArrayList<Integer> typy = new ArrayList<>();
            if (blankiet.dajZakreślenia(i, 49))
                continue;
            for (int j = 0; j < 49; j++) {
                if (blankiet.dajZakreślenia(i, j)) {
                    typy.add(j + 1);
                }
            }
            if (typy.size() == 6)
                zakłady.add(new Zakład(typy));
        }
        long LiczbaLosowań = 1;
        for (int i = 9; i >= 0; i--) {
            if (blankiet.dajLiczbaLosowań(i)) {
                LiczbaLosowań = i + 1;
                break;
            }
        }
        return new Kwota(3, 0).razy(LiczbaLosowań * zakłady.size());
    }
    // public Kwota wypłataWygranej();
}
