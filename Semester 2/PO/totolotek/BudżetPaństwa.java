package totolotek;

public class BudżetPaństwa {

    public static Kwota sumaWpływów = new Kwota();

    public static Kwota przekazaneSubwencje = new Kwota();

    public static void podatek(Kwota kwota) {
        sumaWpływów = sumaWpływów.plus(kwota);
    }

    public static void subwencja(Kwota kwota) {
        przekazaneSubwencje = przekazaneSubwencje.plus(kwota);
    }

    public static void wypisz() {
        StringBuilder sb = new StringBuilder();
        sb.append("SUMA WPŁYWÓW DO BUDŻETU PAŃSTWA: " + sumaWpływów + "\n");
        sb.append("SUMA PRZEKAZANYCH SUBWENCJI: " + przekazaneSubwencje + "\n");
        System.out.println(String.valueOf(sb));
    }
}
