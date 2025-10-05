package totolotek;

import java.util.ArrayList;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class Losowanie {

    public static int porządek = 1;

    private final int numer;

    private final ArrayList<Integer> wyniki;

    private ArrayList<Integer> losuj() {
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
        return wynik;
    }

    public ArrayList<Integer> wyniki() {
        return new ArrayList<>(wyniki);
    }

    public Losowanie() {
        this.numer = porządek;
        porządek++;
        this.wyniki = losuj();
    }

    public int numer() {
        return numer;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Losowanie nr ");
        sb.append(String.valueOf(numer) + "\n").append("Wyniki: ");
        for (int i = 0; i < 6; i++) {
            int liczba = wyniki.get(i);
            if (liczba < 10)
                sb.append(" " + liczba);
            else
                sb.append(liczba);
            sb.append(" ");
        }
        return String.valueOf(sb);
    }
}
