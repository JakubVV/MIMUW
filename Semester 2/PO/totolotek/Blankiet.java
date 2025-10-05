package totolotek;

import java.util.ArrayList;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class Blankiet {

    private final boolean[][] zakreślenia;
    private final boolean[] liczbaLosowań;

    public Blankiet(boolean[][] zakreślenia, boolean[] liczbaLosowań) {
        this.zakreślenia = zakreślenia;
        this.liczbaLosowań = liczbaLosowań;
    }

    public Blankiet() {
        boolean[][] zakreślenia = new boolean[8][50];
        boolean[] liczbaLosowań = new boolean[10];
        Random random = new Random();
        int zakładów = random.nextInt(8) + 1;
        for (int i = 0; i < zakładów; i++) {
            SortedSet<Integer> liczby = new TreeSet<>();
            while (liczby.size() < 6) {
                int liczba = random.nextInt(49) + 1;
                liczby.add(liczba);
            }
            ArrayList<Integer> wynik = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                wynik.add(liczby.first());
                liczby.removeFirst();
            }
            for (int j = 0; j < 6; j++)
                zakreślenia[i][wynik.get(j) - 1] = true;
        }
        int losowań = random.nextInt(10) + 1;
        liczbaLosowań[losowań - 1] = true;
        this.zakreślenia = zakreślenia;
        this.liczbaLosowań = liczbaLosowań;
    }

    public boolean dajZakreślenia(int i, int j) {
        return zakreślenia[i][j];
    }

    public boolean dajLiczbaLosowań(int i) {
        return liczbaLosowań[i];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(i + 1).append("\n");
            for (int j = 0; j < 49; j++) {
                sb.append(" [ ");
                if (zakreślenia[i][j])
                    sb.append("--");
                else {
                    if (j + 1 < 10)
                        sb.append(" ");
                    sb.append(String.valueOf(j + 1));
                }

                sb.append(" ] ");
                if ((j + 1) % 10 == 0)
                    sb.append("\n");
            }
            sb.append("\n");
            sb.append(" [ ");
            if (zakreślenia[i][49])
                sb.append("--");
            else
                sb.append("  ");
            sb.append(" ] anuluj\n");
        }
        sb.append("Liczba losowań: ");
        for (int i = 0; i < 10; i++) {
            sb.append(" [ ");
            if (liczbaLosowań[i])
                sb.append("--");
            else
                sb.append(String.valueOf(i + 1));
            sb.append(" ] ");
        }
        return String.valueOf(sb);
    }
}
