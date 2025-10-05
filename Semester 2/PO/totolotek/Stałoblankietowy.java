package totolotek;

import java.util.ArrayList;

public class Stałoblankietowy extends Gracz {

    private Kwota stanKonta;

    private final Blankiet blankiet;
    private final ArrayList<Integer> ulubioneKolektury;

    private int następnaKolektura;

    public Stałoblankietowy(String imię, String nazwisko, String PESEL, ArrayList<Integer> ulubioneKolektury, Blankiet blankiet) {
        super(imię, nazwisko, PESEL);
        this.ulubioneKolektury = ulubioneKolektury;
        this.blankiet = blankiet;
        następnaKolektura = 0;
        stanKonta = new Kwota(100, 0);
    }

    public void odbiór(Centrala centrala) {
        Kwota suma = super.odbierzNagrody(centrala);
        stanKonta = stanKonta.plus(suma);
    }

    @Override
    public void zakupKuponu(Centrala centrala) {
        if (centrala.ostatnieLosowanie() % ulubioneKolektury.size() != 0) // Kupuje kupon co ulubioneKolektury.size() losowań.
            return;
        Kwota cena = centrala.kolektury.get(ulubioneKolektury.get(następnaKolektura) - 1).wyceńKupon(blankiet);
        if (stanKonta.porównaj(cena) >= 0) {
            stanKonta.pobierz(cena);
            centrala.rozliczenieZaKupon(cena);
            int nrKolektury = ulubioneKolektury.get(następnaKolektura);
            następnaKolektura++;
            następnaKolektura %= ulubioneKolektury.size();
            kupony.add(centrala.kolektury.get(nrKolektury - 1).generujKupon(blankiet));
        }
    }

    @Override
    public String toString() {
        return super.toString() + stanKonta.toString();
    }
}
