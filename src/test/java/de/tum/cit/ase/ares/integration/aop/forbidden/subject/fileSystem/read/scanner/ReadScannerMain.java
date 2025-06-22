package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ReadScannerMain {

    private ReadScannerMain() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
    }    /**
     * Access the file system using {@link Scanner} for reading.
     */
    public static void accessFileSystemViaScanner() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            StringBuilder content = new StringBuilder();
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine());
                content.append(System.lineSeparator());
            }
        }
    }

    /**
     * Access the file system using Scanner(File source, String charsetName) constructor.
     */
    public static void accessFileSystemViaScannerFileCharsetName() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), "UTF-8")) {
            while (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        }
    }

    /**
     * Access the file system using Scanner(File source, Charset charset) constructor.
     */
    public static void accessFileSystemViaScannerFileCharset() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), StandardCharsets.UTF_8)) {
            while (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        }
    }

    /**
     * Access the file system using Scanner(Path source) constructor.
     */
    public static void accessFileSystemViaScannerPath() throws IOException {
        try (Scanner scanner = new Scanner(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            while (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        }
    }

    /**
     * Access the file system using Scanner(Path source, String charsetName) constructor.
     */
    public static void accessFileSystemViaScannerPathCharsetName() throws IOException {
        try (Scanner scanner = new Scanner(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), "UTF-8")) {
            while (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        }
    }

    /**
     * Access the file system using Scanner(Path source, Charset charset) constructor.
     */
    public static void accessFileSystemViaScannerPathCharset() throws IOException {
        try (Scanner scanner = new Scanner(Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"), StandardCharsets.UTF_8)) {
            while (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        }
    }

    /**
     * Access the file system using Scanner(InputStream source) constructor.
     */
    public static void accessFileSystemViaScannerInputStream() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
             Scanner scanner = new Scanner(fis)) {
            while (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        }
    }

    /**
     * Access the file system using Scanner.next() method.
     */
    public static void accessFileSystemViaScannerNext() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            while (scanner.hasNext()) {
                scanner.next();
            }
        }
    }

    /**
     * Access the file system using Scanner.hasNext() method.
     */
    public static void accessFileSystemViaScannerHasNext() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            while (scanner.hasNext()) {
                scanner.next();
            }
        }
    }

    /**
     * Access the file system using Scanner.nextByte() method.
     */
    public static void accessFileSystemViaScannerNextByte() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {            try {
                if (scanner.hasNext()) {
                    scanner.nextByte();
                }
            } catch (InputMismatchException e) {
                // Expected for non-numeric content, but file system access is tested
            }
        }
    }

    /**
     * Access the file system using Scanner.nextShort() method.
     */
    public static void accessFileSystemViaScannerNextShort() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {            try {
                if (scanner.hasNext()) {
                    scanner.nextShort();
                }
            } catch (InputMismatchException e) {
                // Expected for non-numeric content, but file system access is tested
            }
        }
    }

    /**
     * Access the file system using Scanner.nextInt() method.
     */
    public static void accessFileSystemViaScannerNextInt() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {            try {
                if (scanner.hasNext()) {
                    scanner.nextInt();
                }
            } catch (InputMismatchException e) {
                // Expected for non-numeric content, but file system access is tested
            }
        }
    }

    /**
     * Access the file system using Scanner.nextLong() method.
     */
    public static void accessFileSystemViaScannerNextLong() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {            try {
                if (scanner.hasNext()) {
                    scanner.nextLong();
                }
            } catch (InputMismatchException e) {
                // Expected for non-numeric content, but file system access is tested
            }
        }
    }

    /**
     * Access the file system using Scanner.nextFloat() method.
     */
    public static void accessFileSystemViaScannerNextFloat() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {            try {
                if (scanner.hasNext()) {
                    scanner.nextFloat();
                }
            } catch (InputMismatchException e) {
                // Expected for non-numeric content, but file system access is tested
            }
        }
    }

    /**
     * Access the file system using Scanner.nextDouble() method.
     */
    public static void accessFileSystemViaScannerNextDouble() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {            try {
                if (scanner.hasNext()) {
                    scanner.nextDouble();
                }
            } catch (InputMismatchException e) {
                // Expected for non-numeric content, but file system access is tested
            }
        }
    }

    /**
     * Access the file system using Scanner.nextBoolean() method.
     */
    public static void accessFileSystemViaScannerNextBoolean() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {            try {
                if (scanner.hasNext()) {
                    scanner.nextBoolean();
                }
            } catch (InputMismatchException e) {
                // Expected for non-boolean content, but file system access is tested
            }
        }
    }

    /**
     * Access the file system using Scanner.nextBigDecimal() method.
     */
    public static void accessFileSystemViaScannerNextBigDecimal() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            try {
                while (scanner.hasNext()) {
                    try {
                        @SuppressWarnings("unused")
                        BigDecimal value = scanner.nextBigDecimal();
                    } catch (InputMismatchException e) {
                        // Skip if not a valid BigDecimal
                        scanner.next();
                    }
                }
            } catch (Exception e) {
                // Ignore other exceptions for test purposes
            }
        }
    }

    /**
     * Access the file system using Scanner.nextBigInteger() method.
     */
    public static void accessFileSystemViaScannerNextBigInteger() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            try {
                while (scanner.hasNext()) {
                    try {
                        @SuppressWarnings("unused")
                        BigInteger value = scanner.nextBigInteger();
                    } catch (InputMismatchException e) {
                        // Skip if not a valid BigInteger
                        scanner.next();
                    }
                }
            } catch (Exception e) {
                // Ignore other exceptions for test purposes
            }
        }
    }

    /**
     * Access the file system using Scanner.useDelimiter() method.
     */
    public static void accessFileSystemViaScannerUseDelimiter() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            scanner.useDelimiter(",");
            while (scanner.hasNext()) {
                scanner.next();
            }
        }
    }

    /**
     * Access the file system using Scanner.skip() method.
     */
    public static void accessFileSystemViaScannerSkip() throws IOException {
        try (Scanner scanner = new Scanner(new File("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"))) {
            try {
                scanner.skip(".*");  // Try to skip everything
            } catch (Exception e) {
                // Ignore exceptions for test purposes
            }
        }
    }
}