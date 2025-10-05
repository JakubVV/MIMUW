package totolotek;

import java.util.ArrayList;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class Zakład {

    private final ArrayList<Integer> typ;

    public Zakład(ArrayList<Integer> typ) {
        this.typ = typ;
    }

    public Zakład() {
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
        this.typ = wynik;
    }

    public int daj(int indeks) {
        return typ.get(indeks);
    }

    public int ileTrafień(ArrayList<Integer> wyniki) {
        int ile = 0;
        for (int i = 0; i < typ.size(); i++) {
            if (wyniki.contains(typ.get(i)))
                ile++;
        }
        return ile;
    }

    public ArrayList<Integer> typ() {
        return new ArrayList<>(typ);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (typ.get(i) < 10)
                sb.append(" ");
            sb.append(typ.get(i)).append(" ");
        }
        return String.valueOf(sb);
    }
}
