package test;

import org.junit.jupiter.api.*;

import tablice.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Testy {

    @Test
    void testWłasnościSkalarów() {
        Skalar skalar = new Skalar(1.0);
        assertEquals(0, skalar.wymiar());
        assertArrayEquals(new int[]{}, skalar.kształt());
        assertEquals(1, skalar.liczba_elementów());
    }

    @Test
    void testWłasnościWektorów() {
        Wektor wektor1 = new Wektor(new double[]{1.0, 2.0, 1.0}, true);
        Wektor wektor2 = new Wektor(new double[]{2.0, 2.0, 3.0}, false);
        assertEquals(1, wektor1.wymiar());
        assertArrayEquals(new int[]{3}, wektor1.kształt());
        assertEquals(3, wektor1.liczba_elementów());
        assertEquals(1, wektor2.wymiar());
        assertArrayEquals(new int[]{3}, wektor2.kształt());
        assertEquals(3, wektor2.liczba_elementów());
    }

    @Test
    void testWłasnościMacierzy() {
        Macierz matrix = new Macierz(new double[][]{
                {1.0, 0.0, 2.0},
                {2.0, 1.0, 3.0},
                {1.0, 1.0, 1.0},
                {2.0, 3.0, 1.0}
        });
        assertEquals(2, matrix.wymiar());
        assertArrayEquals(new int[]{4, 3}, matrix.kształt());
        assertEquals(12, matrix.liczba_elementów());
    }

    @Test
    void testArytmetykiSkalarów() throws ZłyIndeks {
        Skalar skalar1 = new Skalar(3.5);
        Skalar skalar2 = new Skalar(11.5);
        assertEquals(new Skalar(15.0), skalar1.suma(skalar2));

        Skalar skalar3 = new Skalar(3.0);
        Skalar skalar4 = new Skalar(12.0);
        assertEquals(new Skalar(36.0), skalar3.iloczyn(skalar4));
    }

    @Test
    void testArytmetykiSkalarWektor() throws ZłyIndeks {
        for(boolean b: new boolean[]{true, false}) {
            // 3.0 + [1.0, 2.5] = [4.0, 5.5]
            Skalar skalar = new Skalar(3.0);
            Wektor wektor1 = new Wektor(new double[]{1.0, 2.5}, b);
            assertEquals(new Wektor(new double[]{4.0, 5.5}, b), skalar.suma(wektor1));

            // 4.0 * [1.5, 2.25] = [6.0, 9.0]
            Wektor wektor2 = new Wektor(new double[]{1.5, 2.25}, b);
            assertEquals(new Wektor(new double[]{6.0, 9.0}, b),
                    new Skalar(4.0).iloczyn(wektor2));
        }  // for b
    }

    @Test
    void testArytmetykiWektorSkalar() throws ZłyIndeks {
        for(boolean b: new boolean[]{true, false}) {
            // [1.0, 2.5] + 3.0 = [4.0, 5.5]
            Skalar skalar = new Skalar(3.0);
            Wektor wektor1 = new Wektor(new double[]{1.0, 2.5}, b);
            assertEquals(new Wektor(new double[]{4.0, 5.5}, b),
                    wektor1.suma(skalar));

            // [1.5, 2.25] * 4.0 = [6.0, 9.0]
            Wektor wektor2 = new Wektor(new double[]{1.5, 2.25}, b);
            assertEquals(new Wektor(new double[]{6.0, 9.0}, b),
                    wektor2.iloczyn(new Skalar(4.0)));
        }  // for b
    }

    @Test
    void testArytmetykiSkalarMacierz() throws ZłyIndeks{
        Skalar skalar = new Skalar(3.0);
        Macierz macierz = new Macierz(new double[][]{
                {1.25, 3.0, -12.0},
                {-51.0, 8.0, 3.5}
        });
        Macierz oczekiwanyWynikDodawania = new Macierz(new double[][]{
                {4.25, 6.0, -9.0},
                {-48.0, 11.0, 6.5}
        });
        assertEquals(oczekiwanyWynikDodawania, skalar.suma(macierz));

        Skalar skalar2 = new Skalar(-3.0);
        Macierz oczekiwanyWynikMnożenia = new Macierz(new double[][]{
                {-3.75, -9.0, 36.0},
                {153.0, -24.0, -10.5}
        });
        assertEquals(oczekiwanyWynikMnożenia, skalar2.iloczyn(macierz));

        // Odwrotna Kolejność
        assertEquals(oczekiwanyWynikDodawania, macierz.suma(skalar));
        assertEquals(oczekiwanyWynikMnożenia, macierz.iloczyn(skalar2));
    }

    @Test
    void testDodawaniaIMnożeniaWektorWektor() throws ZłyIndeks, NiezgodnośćRozmiarów {
        // Wektor + Wektor
        Wektor wektor1 = new Wektor(new double[]{1.0, 2.0, 3.0}, true);
        Wektor wektor2 = new Wektor(new double[]{1.0, 1.0, -2.0}, true);
        assertEquals(new Wektor(new double[]{2.0, 3.0, 1.0}, true),
                wektor1.suma(wektor2));

        Wektor wektor3 = new Wektor(new double[]{-2.0, 5.0}, false);
        Wektor wektor4 = new Wektor(new double[]{-5.0, 2.0}, false);
        assertEquals(new Wektor(new double[]{-7.0, 7.0}, false),
                wektor3.suma(wektor4));

        // Wektor * Wektor (Scalar result)
        Wektor wektor5 = new Wektor(new double[]{3.0, 2.0, -1.0}, true);
        Wektor wektor6 = new Wektor(new double[]{-2.0, 2.0, 1.0}, true);
        assertEquals(new Skalar(-3.0), wektor5.iloczyn(wektor6));

        Wektor wektor7 = new Wektor(new double[]{-2.0, -5.0, 1.0, 3.0}, false);
        Wektor wektor8 = new Wektor(new double[]{-5.0, 1.0, 2.0, -3.0}, false);
        assertEquals(new Skalar(-2.0), wektor7.iloczyn(wektor8));

        Wektor wektor9 = new Wektor(new double[]{1.0, 1.0, -2.0}, false);
        assertEquals(new Macierz(new double[][]{{-3.0}}), wektor1.iloczyn(wektor9));

        Wektor wektor10 = new Wektor(new double[]{1.0, 2.0, 3.0}, false);
        Wektor wektor11 = new Wektor(new double[]{1.0, 1.0, -2.0}, true);
        assertEquals(new Macierz(new double[][]{
                {1.0, 1.0, -2.0},
                {2.0, 2.0, -4.0},
                {3.0, 3.0, -6.0}
        }), wektor10.iloczyn(wektor11));
    }

    @Test
    void testDodawaniaWektorMacierz() throws ZłyIndeks, NiezgodnośćRozmiarów {
        // Wektor + Macierz
        Wektor wektor1 = new Wektor(new double[]{3.0, 1.5, -2.0}, true);
        Macierz macierz1 = new Macierz(new double[][]{
                {1.0, 3.5, -12.0},
                {-5.0, 8.0, 3.0}
        });
        assertEquals(new Macierz(new double[][]{
                {4.0, 5.0, -14.0},
                {-2.0, 9.5, 1.0}
        }), wektor1.suma(macierz1));

        Wektor wektor2 = new Wektor(new double[]{7.5, -5.0}, false);
        assertEquals(new Macierz(new double[][]{
                {8.5, 11.0, -4.5},
                {-10.0, 3.0, -2.0}
        }), wektor2.suma(macierz1));

        // Odwrotna Kolejność

        // Macierz + Wektor (odwrotna kolejność)
        assertEquals(new Macierz(new double[][]{
                {4.0, 5.0, -14.0},
                {-2.0, 9.5, 1.0}
        }), macierz1.suma(wektor1));

        assertEquals(new Macierz(new double[][]{
                {8.5, 11.0, -4.5},
                {-10.0, 3.0, -2.0}
        }), macierz1.suma(wektor2));

    }

    @Test
    void testMnożeniaWektorMacierz() throws ZłyIndeks, NiezgodnośćRozmiarów {
        // Wektor * Macierz
        Wektor wektor1 = new Wektor(new double[]{1.0, 2.0, 3.0}, true);
        Wektor wektor2 = new Wektor(new double[]{1.0, 1.0, -2.0}, false);
        assertEquals(new Macierz(new double[][]{{-3.0}}), wektor1.iloczyn(wektor2));

        Wektor wektor3 = new Wektor(new double[]{1.0, 2.0, 3.0}, false);
        Wektor wektor4 = new Wektor(new double[]{1.0, 1.0, -2.0}, true);
        Macierz macierz1 = new Macierz(new double[][]{
                {1.0, 1.0, -2.0},
                {2.0, 2.0, -4.0},
                {3.0, 3.0, -6.0}
        });
        assertEquals(macierz1, wektor3.iloczyn(wektor4));
    }

    @Test
    void testMnożeniaMacierzWektor() throws ZłyIndeks, NiezgodnośćRozmiarów {
        Macierz macierz1 = new Macierz(new double[][]{
                {1.0, 2.0},
                {3.0, -2.0},
                {2.0, 1.0}
        });
        Wektor wektor1 = new Wektor(new double[]{-1.0, 3.0}, false);
        Wektor oczekiwany1 = new Wektor(new double[]{5.0, -9.0, 1.0}, false);
        assertEquals(oczekiwany1, macierz1.iloczyn(wektor1));

        // [1.0, -1.0, 2.0] * [[1.0, 2.0], [3.0, -2.0], [2.0, 1.0]] = [2.0, 6.0]
        Wektor wektor2 = new Wektor(new double[]{1.0, -1.0, 2.0}, true);
        Macierz macierz2 = new Macierz(new double[][]{
                {1.0, 2.0},
                {3.0, -2.0},
                {2.0, 1.0}
        });
        Wektor oczekiwany2 = new Wektor(new double[]{2.0, 6.0}, true);
        assertEquals(oczekiwany2, wektor2.iloczyn(macierz2));
    }

    @Test
    void testDodawaniaMacierzMacierz() throws ZłyIndeks, NiezgodnośćRozmiarów {
        // [[1.0, -2.0, 3.0], [2.0, 1.0, -1.0]] + [[3.0, -1.0, 2.0], [1.0, 1.0, -2.0]] = [[4.0, -3.0, 5.0], [3.0, 2.0, -3.0]]
        Macierz macierz1 = new Macierz(new double[][]{
                {1.0, -2.0, 3.0},
                {2.0, 1.0, -1.0}
        });
        Macierz macierz2 = new Macierz(new double[][]{
                {3.0, -1.0, 2.0},
                {1.0, 1.0, -2.0}
        });
        Macierz oczekiwany = new Macierz(new double[][]{
                {4.0, -3.0, 5.0},
                {3.0, 2.0, -3.0}
        });
        assertEquals(oczekiwany, macierz1.suma(macierz2));
    }

    @Test
    void testMnożeniaMacierzMacierz() throws ZłyIndeks, NiezgodnośćRozmiarów {
        // [[2.0, 0.5], [1.0, -2.0], [-1.0, 3.0]] * [[2.0, -1.0, 5.0], [-3.0, 2.0, -1.0]] = [[2.5, -1.0, 9.5], [8.0, -5.0, 7.0], [-11.0, 7.0, -8.0]]
        Macierz macierz1 = new Macierz(new double[][]{
                {2.0, 0.5},
                {1.0, -2.0},
                {-1.0, 3.0}
        });
        Macierz macierz2 = new Macierz(new double[][]{
                {2.0, -1.0, 5.0},
                {-3.0, 2.0, -1.0}
        });
        Macierz oczekiwany = new Macierz(new double[][]{
                {2.5, -1.0, 9.5},
                {8.0, -5.0, 7.0},
                {-11.0, 7.0, -8.0}
        });
        assertEquals(oczekiwany, macierz1.iloczyn(macierz2));
    }

    @Test
    void testNegacji() {
        Skalar skalar = new Skalar(17.0);
        assertEquals(new Skalar(-17.0), skalar.negacja());

        Wektor wektor = new Wektor(new double[]{10.0, -45.0, 0.0, 29.0, -3.0}, true);
        assertEquals(new Wektor(new double[]{-10.0, 45.0, 0.0, -29.0, 3.0}, true),
                wektor.negacja());

        Macierz macierz = new Macierz(new double[][]{
                {0.0, 0.5, -1.25},
                {11.0, -71.0, -33.5},
                {-2.0, -1.75, -99.0}
        });
        Macierz oczekiwany = new Macierz(new double[][]{
                {0.0, -0.5, 1.25},
                {-11.0, 71.0, 33.5},
                {2.0, 1.75, 99.0}
        });
        assertEquals(oczekiwany, macierz.negacja());
    }

    @Test
    void testPrzypisaniaSkalarów() {
        // Przypisz skalar [0.5] do skalara [1.0]
        Skalar skalar1 = new Skalar(1.0);
        skalar1.przypisz(new Skalar(0.5));
        assertEquals(new Skalar(0.5), skalar1);

        // Przypisz skalar [0.5] do wektora [1.0, 2.0, 3.0]
        Wektor wektor1 = new Wektor(new double[]{1.0, 2.0, 3.0}, true);
        wektor1.przypisz(new Skalar(0.5));
        assertEquals(new Wektor(new double[]{0.5, 0.5, 0.5}, true), wektor1);

        // Przypisz skalar [0.5] do macierzy
        Macierz macierz1 = new Macierz(new double[][]{
                {1.0, 2.0},
                {-3.0, -4.0},
                {5.0, -6.0}
        });
        macierz1.przypisz(new Skalar(0.5));
        assertEquals(new Macierz(new double[][]{
                {0.5, 0.5},
                {0.5, 0.5},
                {0.5, 0.5}
        }), macierz1);
    }

    @Test
    void testPrzypisaniaWektorów() throws NiezgodnośćRozmiarów {
        // Przypisz wektor [1.5, 2.5, 3.5] do wektora [-1.0, 0.0, 1.0]
        Wektor wektor1 = new Wektor(new double[]{1.5, 2.5, 3.5}, false);
        Wektor wektor2 = new Wektor(new double[]{-1.0, 0.0, 1.0}, false);
        wektor2.przypisz(wektor1);
        assertEquals(new Wektor(new double[]{1.5, 2.5, 3.5}, false), wektor2);

        // Przypisz wektor [1.5, 2.5, 3.5] do wektora [-1.0, 0.0, 1.0] (wektor wierszowy i kolumnowy)
        Wektor wektor3 = new Wektor(new double[]{-1.0, 0.0, 1.0}, true);
        wektor3.przypisz(wektor1);
        assertEquals(new Wektor(new double[]{1.5, 2.5, 3.5}, false), wektor3);

        // Przypisz wektor [1.5, 2.5, 3.5] do macierzy
        Macierz macierz1 = new Macierz(new double[][]{
                {1.0, 2.0, -1.0, -2.0},
                {-3.0, -4.0, 3.0, 4.0},
                {5.0, -6.0, -5.0, 6.0}
        });
        macierz1.przypisz(wektor1);
        assertEquals(new Macierz(new double[][]{
                {1.5, 1.5, 1.5, 1.5},
                {2.5, 2.5, 2.5, 2.5},
                {3.5, 3.5, 3.5, 3.5}
        }), macierz1);
    }

    @Test
    void testPrzypisaniaMacierzy() throws NiezgodnośćRozmiarów {
        // Przypisz macierz [10.5, 20.5, 30.5; -1.5, 0.0, 1.5] do macierzy [1.0, 2.0, 3.0; 3.0, 2.0, 1.0]
        Macierz macierz1 = new Macierz(new double[][]{
                {1.0, 2.0, 3.0},
                {3.0, 2.0, 1.0}
        });
        Macierz macierz2 = new Macierz(new double[][]{
                {10.5, 20.5, 30.5},
                {-1.5, 0.0, 1.5}
        });

        macierz1.przypisz(macierz2);
        assertEquals(macierz2, macierz1);
    }

    @Test
    void testWycinków() throws BłędneZakresy, NiezgodnośćRozmiarów {
        Skalar skalar = new Skalar(13.125);
        assertEquals(skalar, skalar.wycinek());

        Wektor wektor = new Wektor(new double[]{1.0, 21.0, 32.0, 43.0, 54.0}, true);
        Wektor oczekiwanyWycinekWektora = new Wektor(new double[]{32.0, 43.0}, true);
        assertEquals(oczekiwanyWycinekWektora, wektor.wycinek(2, 3));

        Macierz macierz = new Macierz(new double[][]{
                {7.0, -21.0, 15.0, -31.0, 25.0},
                {-21.0, 15.0, -31.0, 25.0, 7.0},
                {15.0, -31.0, 25.0, -7.0, -21.0},
                {-31.0, 25.0, 7.0, -21.0, 15.0}
        });
        Macierz oczekiwanyWycinekMacierzy = new Macierz(new double[][]{
                {15.0, -31.0},
                {-31.0, 25.0},
                {25.0, 7.0}
        });
        assertEquals(oczekiwanyWycinekMacierzy, macierz.wycinek(1, 3, 1, 2));
    }

}
