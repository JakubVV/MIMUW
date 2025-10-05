package totolotek;

import java.util.Random;

public class Minimalista extends Gracz {

    private final int ulubionaKolektura;

    private Kwota stanKonta;

    public Minimalista(String imię, String nazwisko, String PESEL, int ulubionaKolektura) {
        super(imię, nazwisko, PESEL);
        this.ulubionaKolektura = ulubionaKolektura;
        stanKonta = new Kwota(100, 0);
    }

    @Override
    public void zakupKuponu(Centrala centrala) {
        Kwota cena = new Kwota(3, 0);
        if (stanKonta.porównaj(cena) >= 0) {
            stanKonta.pobierz(cena);
            centrala.rozliczenieZaKupon(cena);
            kupony.add(centrala.kolektury.get(ulubionaKolektura - 1).generujKupon(1, 1));
        }
    }

    public void odbiór(Centrala centrala) {
        Kwota suma = super.odbierzNagrody(centrala);
        stanKonta = stanKonta.plus(suma);
    }

    @Override
    public String toString() {
        String wynik = super.toString();
        wynik += stanKonta.toString();
        return wynik;
    }
}
