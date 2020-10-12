package correcter;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.print("Write a mode: ");
        String mode = scan.nextLine().toLowerCase();
        System.out.println();


        handleMode(mode);
    }

    private static void handleMode(String mode) {
        String readFrom;
        String writeTo = "";
        byte[] bytes = new byte[0];
        switch (mode.toLowerCase()) {
            case "encode":
                readFrom = "send.txt";
                writeTo = "encoded.txt";
                bytes = encode(readFile(readFrom));
                break;
            case "send":
                readFrom = "encoded.txt";
                writeTo = "received.txt";
                bytes = simulateErrors(readFile(readFrom));
                break;
            case "decode":
                readFrom = "received.txt";
                writeTo = "decoded.txt";
                bytes = decodeString(readFile(readFrom));
                break;
            default:
                System.out.println("Unrecognized mode");
                break;
        }
        writeFile(writeTo, bytes);
    }




    private static byte[] encode(byte[] bytes) {
        byte[] encodedBytes = new  byte[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int pos = 5;
            byte currentByte = 0;
            for (int j = 7; j >= 0; j--) {
                if (j == 3) {
                    currentByte = 0;
                }
                currentByte = (byte) (currentByte | getBit(bytes[i], j) << pos);
                if (pos == 5) {
                    pos -= 2;
                } else if (pos == 1) {
                    pos = 5;
                } else {
                    pos--;
                }
                encodedBytes[(i * 2) + Math.abs((j / 4) - 1)] = currentByte;
            }
        }

        for (int i = 0; i < encodedBytes.length; i++) {
            byte b = encodedBytes[i];
            for (int j = 0; Math.pow(2, j) <= 8; j++) {
                b = (byte) (b | getParity(b, (int) Math.pow(2, j)) << 8 - (int) Math.pow(2, j));
            }
            encodedBytes[i] = b;
        }

        return encodedBytes;
    }

    private static int getParity(byte b, int index) {
        int sum = 0;
        for (int j = index - 1; j < 7; j += index * 2) {
            for (int k = j; k < j + index; k++) {
                sum += getBit(b, 7 - k);
            }
        }
        return sum % 2;
    }

    private static int getParity(int[] bins, int index) {
        int sum = 0;
        for (int j = index - 1; j < 7; j += index * 2) {
            for (int k = j; k < j + index; k++) {
                sum += bins[k];
            }
        }
        return sum % 2;
    }

    private static int getBit(byte b, int pos) {
        return 1 & b >> pos;
    }


    private static byte[] simulateErrors(byte[] bytes) {
        byte[] corruptedBytes = new byte[bytes.length];
        Random random = new Random();

        for (int i = 0; i < bytes.length; i++) {
            String bin = String.format("%8s", Integer.toBinaryString(bytes[i] ^ 1 << (random.nextInt(8)))).replaceAll(" ", "0");
            if (bin.length() > 8) {
                bin = bin.substring(bin.length() - 8);
            }
            corruptedBytes[i] = (byte) Integer.parseInt(bin, 2);
        }

        return corruptedBytes;
    }



    public static byte[] decodeString(byte[] bytes) {
        byte[] fixedBytes = new byte[bytes.length / 2];
        for (int i = 0; i < fixedBytes.length; i ++) {
            fixedBytes[i] = mergeBinaries(fixBinary(bytes[i * 2]), fixBinary(bytes[i * 2 + 1]));
        }

        return fixedBytes;
    }

    private static byte mergeBinaries(String bin1, String bin2) {
        StringBuilder mergedBinary = new StringBuilder();
        String[] strs = new String[] {bin1, bin2};
        int count = 0;
        for (String str : strs) {
            for (int j = 0; j < str.length() - 1; j++) {
                if (Math.pow(2, count) - 1 == j) {
                    count++;
                } else {
                    mergedBinary.append(str.charAt(j));
                }
            }
            count = 0;
        }
        return (byte) Integer.parseInt(mergedBinary.toString().trim(), 2);
    }

    public static String fixBinary(byte b) {
        String str = String.format("%8s", Integer.toBinaryString(b)). replaceAll(" ", "0");
        String decodedBin = Integer.toBinaryString(b);
        if (str.length() > 8) {
            str = str.substring(str.length() - 8);
        }
        int[] bits = new int[8];
        for (int i = 0; i < str.length(); i++) {
            bits[i] = Character.getNumericValue(str.charAt(i));
        }
        if (str.charAt(str.length() - 1) == '1') {
            decodedBin = Integer.toBinaryString(b ^ 1);
        } else {
            int mismatchPos = -1;
            int index;

            for (int i = 0; (index = (int) Math.pow(2, i)) < bits.length; i++) {
                int parity = bits[index - 1];
                if (parity != Math.abs(getParity(bits, index) - parity)) {
                    mismatchPos = mismatchPos == -1 ? index : mismatchPos + index;
                }
            }
            if (mismatchPos >= 0) {
                decodedBin = Integer.toBinaryString(b ^ 1 << 8 - mismatchPos);
            }
        }
        if (decodedBin.length() > 8) {
            decodedBin = decodedBin.substring(decodedBin.length() - 8);
        }
        decodedBin = String.format("%8s", decodedBin).replaceAll(" ", "0");

        return decodedBin;
    }




    private static byte[] readFile(String inputFile) {
        byte[] bytes = new byte[0];
        try (InputStream inputStream = new FileInputStream(inputFile)) {
            bytes = inputStream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    private static void writeFile(String outputFile, byte[] bytes) {
        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
