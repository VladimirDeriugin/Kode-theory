package org.linearcode;

/**
 * Klasė, apibrėžianti operacijas dvejetainėje lauke (F2).
 * Šis laukas yra naudojamas mod 2 operacijoms, kurios reikalingos kodavimo ir dekodavimo algoritmuose.
 */
public class Field {
    private final int q = 2; // Dvejetainis laukas F2 (modulio bazė).

    /**
     * Atlieka dviejų elementų sudėtį dvejetainėje lauke (mod 2).
     * @param a Pirmas elementas (0 arba 1).
     * @param b Antras elementas (0 arba 1).
     * @return Sudėties rezultatas mod 2.
     */
    public int add(int a, int b) {
        return (a + b) % q;
    }

    /**
     * Atlieka dviejų elementų daugybą dvejetainėje lauke (mod 2).
     * @param a Pirmas elementas (0 arba 1).
     * @param b Antras elementas (0 arba 1).
     * @return Daugybos rezultatas mod 2.
     */
    public int multiply(int a, int b) {
        return (a * b) % q;
    }

    /**
     * Patikrina, ar elementas priklauso dvejetainės lauko reikšmėms.
     * @param element Tikrinamas elementas.
     * @return `true`, jei elementas yra 0 arba 1; kitu atveju `false`.
     */
    public boolean isValid(int element) {
        return element == 0 || element == 1;
    }
}