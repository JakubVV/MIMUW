package totolotek;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class Testy {

    @Test
    public void T1() {
        Kwota x = new Kwota(100, 99);
        Kwota y = new Kwota(100, 98);
        x.pobierz(y);
        assertEquals(new Kwota(0, 1), x);
    }

    @Test
    public void T2() {
        Kwota x = new Kwota(50, 37);
        Kwota y = new Kwota(1, 99);
        assertEquals(new Kwota(52, 36), x.plus(y));
    }

    @Test
    public void T3() {
        Kwota x = new Kwota(50, 99);
        Kwota y = new Kwota(50, 98);
        Kwota z = new Kwota(50, 99);
        assertEquals(1, x.porównaj(y));
        assertEquals(-1, y.porównaj(x));
        assertEquals(0, x.porównaj(z));
        x = new Kwota(100, 99);
        assertEquals(-1, z.porównaj(x));
    }

    @Test
    public void T4() {
        Kwota x = new Kwota(100, 50);
        assertEquals(new Kwota(201, 0), x.razy(2));
        assertEquals(new Kwota(301, 50), x.razy(3));
        assertEquals(new Kwota(), x.razy(0));
    }

    @Test
    public void T5() {
        Kwota x = new Kwota(200, 0);
        assertEquals(new Kwota(2, 0), x.procent(1));
        assertEquals(new Kwota(20, 0), x.procent(10));
        assertEquals(new Kwota(200, 0), x.procent(100));
    }

    @Test
    public void T6() {
        Centrala c = new Centrala(new Kwota(3000000, 0));
        c.utwórzKolekturę();
        Gracz m = new Minimalista("Jan", "Kowalski", "1234567890", 1);
        m.zakupKuponu(c);
        assertEquals(1, m.kupony.size());
        m.zakupKuponu(c);
        m.zakupKuponu(c);
        assertEquals(1, c.kolektury.size());
        assertEquals(3, m.kupony.size());
    }

    @Test
    public void T7() {
        Centrala c = new Centrala(new Kwota(3000000, 0));
        c.utwórzKolekturę();
        Gracz l = new Losowy("Jan", "Kowalski", "1234567890");
        l.zakupKuponu(c);
        assertNotEquals(0, l.kupony.size());
    }

    @Test
    public void T8() {
        Centrala c = new Centrala(new Kwota(3000000, 0));
        c.utwórzKolekturę();
        c.utwórzKolekturę();
        boolean[][] zakreślenia = new boolean[8][50];
        for (int i = 0; i < 6; i++)
            zakreślenia[0][i] = true;
        for (int i = 10; i < 16; i++)
            zakreślenia[1][i] = true;
        boolean[] liczbaLosowań = new boolean[10];
        liczbaLosowań[4] = true;
        ArrayList<Integer> k = new ArrayList<>(Arrays.asList(1, 2));
        Gracz sbl = new Stałoblankietowy("Jan", "Kowalski", "0123456789", k,
                                          new Blankiet(zakreślenia, liczbaLosowań));
        sbl.zakupKuponu(c);
        assertNotEquals(0, sbl.kupony.size());
    }

    @Test
    public void T9() {
        Centrala c = new Centrala(new Kwota(3000000, 0));
        c.utwórzKolekturę();
        c.utwórzKolekturę();
        ArrayList<Integer> k = new ArrayList<>(Arrays.asList(1));
        ArrayList<Integer> l = new ArrayList<>(Arrays.asList(21, 23, 39, 41, 43, 45));
        Gracz sli = new Stałoliczbowy("Jan", "Kowalski", "1234567890", l, k);
        sli.zakupKuponu(c);
        assertNotEquals(0, sli.kupony.size());
    }

    @Test
    public void T10() {
        Centrala c =  new Centrala(new Kwota(3000000, 0));
        c.utwórzKolekturę();
        ArrayList<Gracz> gracze = new ArrayList<>();
        for (int i = 0; i < 1000; i++)
            gracze.add(new Losowy("A", "B", "C"));
        c.kupKupony(gracze);
        c.przeprowadźLosowanie(gracze);
        assertNotEquals(new Kwota(), c.wygranaI);
        assertNotEquals(new Kwota(), c.wygranaII);
        assertNotEquals(new Kwota(), c.wygranaIII);
        assertNotEquals(new Kwota(), c.wygranaIV);
    }
}
