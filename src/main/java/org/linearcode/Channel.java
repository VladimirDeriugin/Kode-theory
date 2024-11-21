package org.linearcode;

import java.util.Random;

public class Channel {
    private final double errorProbability; // Klaidos tikimybė (0 <= tikimybė <= 1).
    private final Random random; // Atsitiktinių skaičių generatorius, naudojamas klaidų simuliacijai.
    private final Field field; // F2 laukas (modulio 2 operacijos).

    /**
     * Konstruktorinis metodas.
     * @param errorProbability Tikimybė, su kuria kiekvienas siunčiamas bitas gali būti klaidingas.
     * @throws IllegalArgumentException Jei klaidos tikimybė nėra intervale [0, 1].
     */
    public Channel(double errorProbability) {
        if (errorProbability < 0 || errorProbability > 1) {
            throw new IllegalArgumentException("Invalid error probability");
        }
        this.errorProbability = errorProbability;
        this.random = new Random();
        this.field = new Field();
    }

    /**
     * Siunčia bitų masyvą per kanalą su klaidomis.
     * @param input Bitų masyvas (0 arba 1), kuris bus siunčiamas per kanalą.
     * @return Naujas bitų masyvas, kurio kiekvienas bitas gali būti pakeistas pagal klaidos tikimybę.
     */
    public int[] transmit(int[] input) {
        int[] output = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            //Generuoja atsitiktinius skaičius nuo 0 iki 1
            if (random.nextDouble() < errorProbability) {
                // Keičia 0 į 1 ir 1 į 0
                output[i] = field.add(input[i], 1);
            } else {
                // Bitas paliekamas nepakeistas
                output[i] = input[i];
            }
        }
        return output; // Grąžina rezultatus po perdavimo per kanalą
    }
}