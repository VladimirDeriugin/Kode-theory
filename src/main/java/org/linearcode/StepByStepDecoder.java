package org.linearcode;

import java.util.*;

public class StepByStepDecoder {
    private final Matrix H; // Parity-check matrix
    private final int n; // Code length
    private final int k; // Dimension
    private final Map<String, int[]> syndromeTable; // Map of syndromes to coset leaders
    private final Field field; // Binary field operations

    public StepByStepDecoder(Matrix controlMatrix, int n, int k) {
        this.H = controlMatrix;
        this.n = n;
        this.k = k;
        this.field = new Field();
        this.syndromeTable = buildSyndromeTable(); // Precompute syndrome table
    }

    public int[] decode(int[] received) {
        int[] r = received.clone(); // Initial received word
        int[] syndrome = calculateSyndrome(r);

        // Loop through all positions in the vector
        for (int i = 0; i < n; i++) {
            // Get the coset leader for the current syndrome
            int[] currentCosetLeader = syndromeTable.get(vectorToString(syndrome));
            int currentWeight = getWeight(currentCosetLeader);

            // If coset leader weight is 0, decoding is complete
            if (currentWeight == 0) {
                return r;
            }

            // Flip the i-th bit
            int[] ei = generateErrorVector(i);
            int[] candidate = addVectors(r, ei);

            // Calculate the new syndrome and its coset leader
            int[] newSyndrome = calculateSyndrome(candidate);
            int[] newCosetLeader = syndromeTable.get(vectorToString(newSyndrome));

            // Compare coset leader weights
            if (getWeight(newCosetLeader) < currentWeight) {
                r = candidate; // Update the vector
                syndrome = newSyndrome; // Update the syndrome
            }
        }

        return r; // Return the decoded vector
    }

    // Generate error vector with a single 1 at position i
    private int[] generateErrorVector(int i) {
        int[] ei = new int[n];
        ei[i] = 1;
        return ei;
    }

    // Build syndrome table mapping syndromes to coset leaders
    private Map<String, int[]> buildSyndromeTable() {
        Map<String, int[]> table = new HashMap<>();

        // Generate all possible error patterns
        for (int weight = 0; weight <= n - k; weight++) {
            List<int[]> patterns = generateErrorPatterns(n, weight);

            for (int[] pattern : patterns) {
                int[] syndrome = calculateSyndrome(pattern);
                String syndKey = vectorToString(syndrome);

                // Add syndrome and coset leader if not already present
                if (!table.containsKey(syndKey)) {
                    table.put(syndKey, pattern);
                }
            }
        }

        return table;
    }

    // Generate all error patterns of a given weight
    private List<int[]> generateErrorPatterns(int length, int weight) {
        List<int[]> patterns = new ArrayList<>();
        generateErrorPatternsRecursive(new int[length], 0, weight, patterns);
        return patterns;
    }

    // Recursive helper for generating error patterns
    private void generateErrorPatternsRecursive(int[] current, int pos, int remainingWeight, List<int[]> patterns) {
        if (remainingWeight == 0) {
            patterns.add(current.clone());
            return;
        }
        if (pos >= current.length || remainingWeight > current.length - pos) {
            return;
        }
        generateErrorPatternsRecursive(current, pos + 1, remainingWeight, patterns);

        current[pos] = 1;
        generateErrorPatternsRecursive(current, pos + 1, remainingWeight - 1, patterns);
        current[pos] = 0;
    }

    // Calculate syndrome by multiplying vector with H
    private int[] calculateSyndrome(int[] vector) {
        return H.multiplyVector(vector, true);
    }

    // Check if a vector is zero
    private boolean isZeroVector(int[] vector) {
        for (int value : vector) {
            if (value != 0) {
                return false;
            }
        }
        return true;
    }

    // Add two vectors in the binary field
    private int[] addVectors(int[] v1, int[] v2) {
        int[] result = new int[v1.length];
        for (int i = 0; i < v1.length; i++) {
            result[i] = field.add(v1[i], v2[i]);
        }
        return result;
    }

    // Calculate the weight of a vector
    private int getWeight(int[] vector) {
        int weight = 0;
        for (int value : vector) {
            if (value != 0) {
                weight++;
            }
        }
        return weight;
    }

    // Convert vector to string for mapping
    private String vectorToString(int[] vector) {
        StringBuilder sb = new StringBuilder();
        for (int value : vector) {
            sb.append(value);
        }
        return sb.toString();
    }
}
