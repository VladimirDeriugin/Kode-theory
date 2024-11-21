package org.linearcode;

/**
 * Klasė, apibrėžianti linijinio kodo operacijas, naudojant generatorinę matricą.
 */
public class LinearCode {
    private final Matrix generatorMatrix; // Generatorinė matrica (G), naudojama kodavimui.
    private final int k; // Kodo dimensija (pradinės žinutės ilgis).

    /**
     * Konstruktorinis metodas.
     * @param n Kodo ilgis (output vektoriaus ilgis).
     * @param k Dimensija (input žinutės ilgis).
     * @param G Generatorinė matrica.
     * @throws IllegalArgumentException Jei matricos matmenys neatitinka dimensijos ir kodo ilgio.
     */
    public LinearCode(int n, int k, Matrix G) {
        if (G.getRows() != k || G.getCols() != n) {
            throw new IllegalArgumentException("Invalid generator matrix dimensions");
        }
        // code length
        this.k = k;
        this.generatorMatrix = G;
        Field field = new Field(); // Inicijuojamas laukas F2, nors jis nenaudojamas šioje klasėje.
    }

    /**
     * Koduoja pranešimą, naudodamas generatorinę matricą.
     * @param message Bitų masyvas, reprezentuojantis žinutę, kurios ilgis turi būti lygus dimensijai (k).
     * @return Bitų masyvas, reprezentuojantis užkoduotą pranešimą.
     * @throws IllegalArgumentException Jei žinutės ilgis nesutampa su dimensija (k).
     */
    public int[] encode(int[] message) {
        if (message.length != k) {
            throw new IllegalArgumentException("Invalid message length");
        }
        // Dauginame žinutę iš generatorinės matricos, kad gautume kodinį žodį.
        return generatorMatrix.multiplyVector(message,false);
    }

    /**
     * Grąžina generatorinę matricą.
     * @return Generatorinė matrica (G).
     */
    public Matrix getGeneratorMatrix() {
        return generatorMatrix;
    }
}