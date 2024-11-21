package org.linearcode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
/**
 * Pagrindinė programa, leidžianti vartotojui simuliuoti linijinį kodavimą, dekodavimą ir perdavimą.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Naudojama vartotojo įvedimui skaityti.
        boolean running = true; // Nurodo, ar programa turi tęsti veikimą.

        // Pagrindinis meniu ciklas.
        while (running) {
            System.out.println("\n=== Linear Code Simulator ===");
            System.out.println("1. Encode and decode single vector");
            System.out.println("2. Enter text message");
            System.out.println("3. Process image");
            System.out.println("4. Exit");
            System.out.print("Choose option (1-4): ");

            int choice = scanner.nextInt(); // Vartotojo pasirinkimas.
            scanner.nextLine(); // Išvalo buferį.

            switch (choice) {
                case 1:
                    handleSingleVector(scanner); // Vieno vektoriaus apdorojimas.
                    break;
                case 2:
                    handleTextMessage(scanner);
                    break;
                case 3:
                    handleImage(scanner);
                    break;
                case 4:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        }
        scanner.close();
    }

    private static void handleSingleVector(Scanner scanner) {
        try {
            // 1. Get code parameters
            System.out.println("\n=== Code Parameters ===");
            System.out.print("Enter code length (n): ");
            int n = scanner.nextInt();
            System.out.print("Enter message length (k): ");
            int k = scanner.nextInt();

            if (k >= n) {
                System.out.println("Error: k must be less than n!");
                return;
            }

            // 2. Generator matrix selection
            System.out.print("Do you want to enter generator matrix manually? (y/n): ");
            scanner.nextLine(); // Clear buffer
            String matrixChoice = scanner.nextLine();

            Matrix G;
            if (matrixChoice.equalsIgnoreCase("y")) {
                G = getManualGeneratorMatrix(scanner, k, n);
            } else {
                System.out.println("Generating standard form matrix...");
                G = Matrix.createStandardForm(k, n);
                printMatrix("Generated Generator Matrix:", G);
            }
            Matrix H = getControlMatrix(G);
            printMatrix("Generated Control Matrix:", H);

            // 3. Create code instance
            LinearCode code = new LinearCode(n, k, G);

            // 4. Get error probability
            System.out.print("Enter error probability (0.0-1.0): ");
            double pe = scanner.nextDouble();
            Channel channel = new Channel(pe);

            // 5. Get input message
            System.out.println("\nEnter message bits (space-separated, length " + k + "):");
            int[] message = new int[k];
            for (int i = 0; i < k; i++) {
                message[i] = scanner.nextInt();
            }

            // 6. Process message
            System.out.println("\n=== Processing Steps ===");
            System.out.println("Original message: " + arrayToString(message));

            // Encoding
            int[] encoded = code.encode(message);
            System.out.println("Encoded message: " + arrayToString(encoded));

            // Transmission
            int[] received = channel.transmit(encoded);
            System.out.println("Received message: " + arrayToString(received));

            System.out.print("\nDo you want to manually modify the received vector? (y/n): ");
            scanner.nextLine(); // Clear buffer
            String modifyChoice = scanner.nextLine();

            if (modifyChoice.equalsIgnoreCase("y")) {
                received = manuallyModifyVector(scanner, received);
                System.out.println("Modified received vector: " + arrayToString(received));
            }

            StepByStepDecoder decoder = new StepByStepDecoder(H, n, k);
            int[] decoded = decoder.decode(received);
            //Klausimas dėstytojui
            int[] messagePart = new int[k];
            System.arraycopy(decoded, 0, messagePart, 0, k);
            System.out.println("Decoded message: " + arrayToString(messagePart));


        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            scanner.nextLine(); // Clear buffer
        }
    }

    private static Matrix getManualGeneratorMatrix(Scanner scanner, int k, int n) {

        Matrix G = new Matrix(k, n);
        System.out.println("\nEnter generator matrix (" + k + "x" + n + ") only A where G=(I|A) row by row.");
        for(int i = 0; i < k; i++) {
            for(int j = 0; j < k; j++) {
                int value = (i == j ? 1 : 0);
                System.out.print(value + " ");
                G.setElement(i, j, value);
            }
            System.out.println();
        }
        System.out.println("Enter space-separated binary digits (0 or 1):");



        for (int i = 0; i < k; i++) {
            System.out.print("Row " + (i + 1) + ": ");
            for (int j = k; j < n; j++) {
                int value = scanner.nextInt();
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Invalid input: must be 0 or 1");
                }
                G.setElement(i, j, value);
            }
        }

        for(int i = 0; i < k; i++) {
            for (int j = 0; j < n; j++) {
                System.out.print(G.getElement(i, j) + " ");
            }
            System.out.println();
        }

        return G;
    }

    private static void printMatrix(String label, Matrix matrix) {
        System.out.println("\n" + label + ":");
        for (int i = 0; i < matrix.getRows(); i++) {
            for (int j = 0; j < matrix.getCols(); j++) {
                System.out.print(matrix.getElement(i, j) + " ");
            }
            System.out.println();
        }
    }

    public static Matrix getControlMatrix(Matrix G) {
        int k = G.getRows();
        int n = G.getCols();
        int m = n - k;

        // Extract submatrix A from G
        Matrix A = new Matrix(k, m);
        for (int i = 0; i < k; i++) {
            for (int j = k; j < n; j++) {
                A.setElement(i, j - k, G.getElement(i, j));
            }
        }

        // Transpose submatrix A to get A^T
        Matrix At = new Matrix(m, k);
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < m; j++) {
                At.setElement(j, i, A.getElement(i, j));
            }
        }

        // Construct control matrix H by concatenating A^T with identity matrix I
        Matrix H = new Matrix(m, n);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < k; j++) {
                H.setElement(i, j, At.getElement(i, j));
            }
            H.setElement(i, k + i, 1); // Identity matrix part
        }

        return H;
    }

    private static int[] manuallyModifyVector(Scanner scanner, int[] vector) {
        int[] modified = vector.clone();
        System.out.println("\nCurrent vector: " + arrayToString(modified));

        while (true) {
            System.out.print("Enter position to flip (0-" + (vector.length-1) + ") or -1 to finish: ");
            int pos = scanner.nextInt();

            if (pos == -1) {
                break;
            }

            if (pos >= 0 && pos < vector.length) {
                modified[pos] = 1 - modified[pos]; // Flip bit
                System.out.println("Updated vector: " + arrayToString(modified));
            } else {
                System.out.println("Invalid position!");
            }
        }

        return modified;
    }

    private static String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(" ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static void handleTextMessage(Scanner scanner) {
        try {
            // Get code parameters
            System.out.println("\n=== Code Parameters ===");
            System.out.print("Enter code length (n): ");
            int n = scanner.nextInt();
            System.out.print("Enter message length (k): ");
            int k = scanner.nextInt();
            scanner.nextLine(); // Clear buffer

            if (k >= n) {
                System.out.println("Error: k must be less than n!");
                return;
            }

            // Get generator matrix
            Matrix G = getGeneratorMatrix(scanner, k, n);
            Matrix H = getControlMatrix(G);
            LinearCode code = new LinearCode(n, k, G);

            // Get error probability
            System.out.print("Enter error probability (0.0-1.0): ");
            double pe = scanner.nextDouble();
            scanner.nextLine(); // Clear buffer
            Channel channel = new Channel(pe);

            // Get text input
            System.out.println("\nEnter your text message (end with empty line):");
            StringBuilder text = new StringBuilder();
            String line;
            while (!(line = scanner.nextLine()).isEmpty()) {
                text.append(line).append("\n");
            }

            // Convert text to binary
            byte[] textBytes = text.toString().getBytes(StandardCharsets.UTF_8);
            List<int[]> messageVectors = bytesToVectors(textBytes, k);

            // Process without encoding
            System.out.println("\n=== Transmission without encoding ===");
            byte[] receivedBytesNoEncoding = processWithoutEncoding(messageVectors, k, channel);
            String receivedTextNoEncoding = new String(receivedBytesNoEncoding, StandardCharsets.UTF_8);
            receivedTextNoEncoding = receivedTextNoEncoding.replaceAll("[^\\x20-\\x7E\\x0A]", "?");
            System.out.println("Received text without encoding:\n" + receivedTextNoEncoding);

            // Process with encoding
            System.out.println("\n=== Transmission with encoding ===");
            StepByStepDecoder decoder = new StepByStepDecoder(H, n, k);
            byte[] receivedBytesWithEncoding = processWithEncoding(messageVectors, code, channel, decoder);
            receivedTextNoEncoding = receivedTextNoEncoding.replaceAll("[^\\x20-\\x7E\\x0A]", "?");
            String receivedTextWithEncoding = new String(receivedBytesWithEncoding, StandardCharsets.UTF_8);
            System.out.println("Received text with encoding:\n" + receivedTextWithEncoding);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            scanner.nextLine(); // Clear buffer
        }
    }

    private static void handleImage(Scanner scanner) {
        try {
            // Get code parameters
            System.out.println("\n=== Code Parameters ===");
            System.out.print("Enter code length (n): ");
            int n = scanner.nextInt();
            System.out.print("Enter message length (k): ");
            int k = scanner.nextInt();
            scanner.nextLine(); // Clear buffer

            if (k >= n) {
                System.out.println("Error: k must be less than n!");
                return;
            }

            // Get image file
            System.out.print("Enter image path: ");
            String imagePath = scanner.nextLine();
            BufferedImage originalImage = ImageIO.read(new File(imagePath));

            // Get generator matrix
            Matrix G = getGeneratorMatrix(scanner, k, n);
            Matrix H = getControlMatrix(G);
            LinearCode code = new LinearCode(n, k, G);

            // Get error probability
            System.out.print("Enter error probability (0.0-1.0): ");
            double pe = scanner.nextDouble();
            Channel channel = new Channel(pe);

            // Convert image to vectors
            List<int[]> imageVectors = imageToVectors(originalImage, k);

            // Process without encoding
            System.out.println("\n=== Transmission without encoding ===");
            BufferedImage receivedImageNoEncoding = processImageWithoutEncoding(imageVectors, originalImage.getWidth(),
                                                                                originalImage.getHeight(), k, channel);
            ImageIO.write(receivedImageNoEncoding, "bmp", new File("received_no_encoding.bmp"));
            System.out.println("Saved as: received_no_encoding.bmp");

            // Process with encoding
            System.out.println("\n=== Transmission with encoding ===");
            StepByStepDecoder decoder = new StepByStepDecoder(H, n, k);
            BufferedImage receivedImageWithEncoding = processImageWithEncoding(imageVectors, originalImage.getWidth(),
                                                                               originalImage.getHeight(), code, channel, decoder);
            ImageIO.write(receivedImageWithEncoding, "bmp", new File("received_with_encoding.bmp"));
            System.out.println("Saved as: received_with_encoding.bmp");

        } catch (IOException e) {
            System.out.println("Error reading/writing image: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            scanner.nextLine(); // Clear buffer
        }
    }

    private static Matrix getGeneratorMatrix(Scanner scanner, int k, int n) {
        System.out.print("Do you want to enter generator matrix manually? (y/n): ");
        String matrixChoice = scanner.nextLine();

        if (matrixChoice.equalsIgnoreCase("y")) {
            return getManualGeneratorMatrix(scanner, k, n);
        } else {
            System.out.println("Generating standard form matrix...");
            Matrix G = Matrix.createStandardForm(k, n);
            printMatrix("Generated Generator Matrix:", G);
            return G;
        }
    }

    // Helper methods for text processing
    private static List<int[]> bytesToVectors(byte[] bytes, int k) {
        List<int[]> vectors = new ArrayList<>();
        int bits = bytes.length * 8;
        int vectorCount = (bits + k - 1) / k;

        for (int i = 0; i < vectorCount; i++) {
            int[] vector = new int[k];
            for (int j = 0; j < k; j++) {
                int bitPos = i * k + j;
                if (bitPos < bits) {
                    int bytePos = bitPos / 8;
                    int bitInByte = 7 - (bitPos % 8);
                    vector[j] = (bytes[bytePos] >> bitInByte) & 1;
                }
            }
            vectors.add(vector);
        }
        return vectors;
    }

    private static byte[] vectorsToBytes(List<int[]> vectors) {
        int totalBits = 0;
        for (int[] vector : vectors) {
            totalBits += vector.length;
        }
        int byteCount = (totalBits + 7) / 8;
        byte[] bytes = new byte[byteCount];

        int bitCount = 0;
        for (int[] vector : vectors) {
            for (int bit : vector) {
                int bytePos = bitCount / 8;
                int bitInByte = 7 - (bitCount % 8);
                bytes[bytePos] |= (bit << bitInByte);
                bitCount++;
            }
        }
        return bytes;
    }

    private static byte[] processWithoutEncoding(List<int[]> vectors, int k, Channel channel) {
        List<int[]> receivedVectors = new ArrayList<>();
        for (int[] vector : vectors) {
            int[] received = channel.transmit(vector);
            receivedVectors.add(received);
        }
        return vectorsToBytes(receivedVectors);
    }

    private static byte[] processWithEncoding(List<int[]> vectors, LinearCode code, Channel channel,
                                              StepByStepDecoder decoder) {
        List<int[]> decodedVectors = new ArrayList<>();
        for (int[] vector : vectors) {
            int[] encoded = code.encode(vector);
            int[] received = channel.transmit(encoded);
            int[] decoded = decoder.decode(received);
            // Extract original message from decoded codeword (first k bits)
            int[] message = new int[vector.length];
            System.arraycopy(decoded, 0, message, 0, vector.length);
            decodedVectors.add(message);
        }
        return vectorsToBytes(decodedVectors);
    }

    // Helper methods for image processing
    private static List<int[]> imageToVectors(BufferedImage image, int k) {
        List<int[]> vectors = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int[] pixelBits = new int[24]; // 8 bits each for R, G, B

                // Extract RGB components
                for (int i = 0; i < 24; i++) {
                    pixelBits[i] = (rgb >> i) & 1;
                }

                // Split into k-bit vectors
                for (int i = 0; i < 24; i += k) {
                    int[] vector = new int[k];
                    for (int j = 0; j < k && (i + j) < 24; j++) {
                        vector[j] = pixelBits[i + j];
                    }
                    vectors.add(vector);
                }
            }
        }
        return vectors;
    }

    private static BufferedImage processImageWithoutEncoding(List<int[]> vectors, int width, int height,
                                                             int k, Channel channel) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        List<int[]> receivedVectors = new ArrayList<>();

        for (int[] vector : vectors) {
            receivedVectors.add(channel.transmit(vector));
        }

        int vectorIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = 0;
                for (int i = 0; i < 24; i += k) {
                    int[] vector = receivedVectors.get(vectorIndex++);
                    for (int j = 0; j < k && (i + j) < 24; j++) {
                        rgb |= (vector[j] << (i + j));
                    }
                }
                result.setRGB(x, y, rgb);
            }
        }
        return result;
    }

    private static BufferedImage processImageWithEncoding(List<int[]> vectors, int width, int height,
                                                          LinearCode code, Channel channel, StepByStepDecoder decoder) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        List<int[]> decodedVectors = new ArrayList<>();

        for (int[] vector : vectors) {
            int[] encoded = code.encode(vector);
            int[] received = channel.transmit(encoded);
            int[] decoded = decoder.decode(received);
            int[] message = new int[vector.length];
            System.arraycopy(decoded, 0, message, 0, vector.length);
            decodedVectors.add(message);
        }

        int vectorIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = 0;
                int[] vector;
                for (int i = 0; i < 24; i += vector.length) {
                    vector = decodedVectors.get(vectorIndex++);
                    for (int j = 0; j < vector.length && (i + j) < 24; j++) {
                        rgb |= (vector[j] << (i + j));
                    }
                }
                result.setRGB(x, y, rgb);
            }
        }
        return result;
    }
}