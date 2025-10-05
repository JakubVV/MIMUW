package totolotek;

import java.util.Random;

public class Losowy extends Gracz {

    private Kwota stanKonta;

    public Losowy(String imię, String nazwisko, String PESEL) {
        super(imię, nazwisko, PESEL);
        Random random = new Random();
        long złote = random.nextInt(1000000);
        long grosze = random.nextInt(99);
        this.stanKonta = new Kwota(złote, grosze);
    }

    @Override
    public void zakupKuponu(Centrala centrala) {
        Random random = new Random();
        int nrKolektury = random.nextInt(centrala.kolektury.size()) + 1;
        int liczbaKuponów = random.nextInt(100) + 1;
        for (int i = 0; i < liczbaKuponów; i++) {
            long liczbaZakładów = random.nextInt(8) + 1;
            long liczbaLosowań = random.nextInt(10) + 1;
            Kwota cena = new Kwota(3, 0).razy(liczbaLosowań * liczbaZakładów);
            if (stanKonta.porównaj(cena) >= 0) {
                stanKonta.pobierz(cena);
                centrala.rozliczenieZaKupon(cena);
                kupony.add(centrala.kolektury.get(nrKolektury - 1).generujKupon(liczbaZakładów, liczbaLosowań));
            }
        }
    }

    public void odbiór(Centrala centrala) {
        Kwota suma = super.odbierzNagrody(centrala);
        stanKonta = stanKonta.plus(suma);
    }

    @Override
    public String toString() {
        return super.toString() + stanKonta.toString();
    }
}
