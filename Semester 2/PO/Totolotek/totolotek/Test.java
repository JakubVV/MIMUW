package totolotek;

import java.util.ArrayList;
import java.util.Arrays;

public class Test {

    public static void main(String[] args) {
        Centrala centrala = new Centrala(new Kwota(2000000, 0));
        for (int i = 0; i < 10; i++)
            centrala.utwórzKolekturę();
        ArrayList<Gracz> gracze = new ArrayList<>();
        for (int i = 0; i < 200; i++)
            gracze.add(new Minimalista("Jan", "Kowalski", "1234567890", i % 10 + 1));
        for (int i = 0; i < 200; i++)
            gracze.add(new Losowy("Adam", "Nowak", "0123456789"));
        for (int i = 0; i < 200; i++) {
            ArrayList<Integer> a = new ArrayList<>();
            a.add(i % 10 + 1);
            gracze.add(new Stałoliczbowy("Piotr", "Fronczewski", "2345678901", a));
        }
        for (int i = 0; i < 200; i++) {
            ArrayList<Integer> a = new ArrayList<>();
            a.add(i % 10 + 1);
            gracze.add(new Stałoblankietowy("Tomasz", "Tomkowski", "3456789012", a, new Blankiet()));
        }
        for (int i = 0; i < 20; i++) {
            centrala.kupKupony(gracze);
            centrala.przeprowadźLosowanie(gracze);
            for (int j = 0; j < gracze.size(); j++)
                gracze.get(j).odbiór(centrala);
        }
        System.out.println(centrala);
        BudżetPaństwa.wypisz();
    }
}
