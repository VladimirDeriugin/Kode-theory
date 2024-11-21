package org.linearcode;

/**
 * Klasė, reprezentuojanti matricą ir operacijas su ja dvejetainėje lauke (mod 2).
 */
public class Matrix {
    private final int[][] data; // Matricos elementų masyvas.
    private final int rows; // Eilučių skaičius.
    private final int cols; // Stulpelių skaičius.
    private final Field field; // F2 laukas, skirtas mod 2 operacijoms.

    /**
     * Konstruktorinis metodas.
     * @param rows Matricos eilučių skaičius.
     * @param cols Matricos stulpelių skaičius.
     */
    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new int[rows][cols];
        this.field = new Field();
    }

    /**
     * Nustato matricos elementą nurodytoje pozicijoje.
     * @param i Eilutės indeksas.
     * @param j Stulpelio indeksas.
     * @param value Nauja elemento reikšmė (0 arba 1).
     * @throws IllegalArgumentException Jei reikšmė nėra validi F2 lauke.
     */
    public void setElement(int i, int j, int value) {
        if (!field.isValid(value)) {
            throw new IllegalArgumentException("Invalid field element");
        }
        data[i][j] = value;
    }

    /**
     * Grąžina matricos elementą nurodytoje pozicijoje.
     * @param i Eilutės indeksas.
     * @param j Stulpelio indeksas.
     * @return Matricos elemento reikšmė.
     */
    public int getElement(int i, int j) {
        return data[i][j];
    }

    /**
     * Dauginama matrica iš vektoriaus.
     * @param vector Dauginamas vektorius.
     * @param transpose Jei `true`, matrica transponuojama prieš daugybą.
     * @return Rezultato vektorius.
     * @throws IllegalArgumentException Jei vektoriaus ilgis nesutampa su matricos dimensijomis.
     */
    public int[] multiplyVector(int[] vector, boolean transpose) {
        if(transpose)
        {
            if (this.cols != vector.length) {
                throw new IllegalArgumentException("Invalid vector length");
            }

            int[] result = new int[this.rows];
            for (int i = 0; i < this.rows; i++) {
                for(int j = 0; j < this.cols; j++) {
                    result[i] = field.add(result[i], field.multiply(this.data[i][j], vector[j]));
                }
            }
            return result;
        } else {
            if (this.rows != vector.length) {
                throw new IllegalArgumentException("Invalid vector length");
            }

            int[] result = new int[this.cols];
            for (int i = 0; i < this.rows; i++) {
                for (int j = 0; j < this.cols; j++) {
                    result[j] = field.add(result[j], field.multiply(this.data[i][j], vector[i]));
                }
            }
            return result;
        }
    }

    /**
     * Sukuria standartinės formos matricą.
     * @param k Dimensija (eilutės).
     * @param n Kodo ilgis (stulpeliai).
     * @return Standartinės formos matrica (G = [I|A]).
     */
    public static Matrix createStandardForm(int k, int n) {
        Matrix matrix = new Matrix(k, n);
        // Sukuriame tapatybės matricą pirmiems k stulpeliams.
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                matrix.data[i][j] = (i == j) ? 1 : 0;
            }
        }
        // Užpildome likusius stulpelius atsitiktiniais elementais.
        for (int i = 0; i < k; i++) {
            for (int j = k; j < n; j++) {
                matrix.data[i][j] = Math.random() < 0.5 ? 0 : 1;
            }
        }
        return matrix;
    }

    /**
     * Grąžina eilučių skaičių.
     * @return Eilučių skaičius.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Grąžina stulpelių skaičių.
     * @return Stulpelių skaičius.
     */
    public int getCols() {
        return cols;
    }


}