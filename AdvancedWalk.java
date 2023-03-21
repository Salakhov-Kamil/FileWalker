import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AdvancedWalk {
    private static final String ALGORITHM_NAME = "SHA-256";
    private static final MessageDigest SHA_ALGORITHM;
    private static final int BYTES_IN_HASH;

    static {
        try {
            SHA_ALGORITHM = MessageDigest.getInstance(ALGORITHM_NAME);
            BYTES_IN_HASH = SHA_ALGORITHM.getDigestLength();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    private static final String ERROR_HASH = "0".repeat(BYTES_IN_HASH * 2);

    public static void startWalk(String[] args, WalkType walkType) {
        if (!isArgsSizeCorrect(args)) {
            return;
        }

        Path inputFile = getPath(args[0], "Incorrect input file");
        Path outputFile = getPath(args[1], "Incorrect output file");
        if (inputFile == null || outputFile == null) {
            return;
        }

        if (!isCorrectOutputFile(outputFile)) {
            return;
        }

        try (BufferedReader bufferedReader = Files.newBufferedReader(inputFile)) {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFile)) {
                HashWriter hashWriter = new HashWriter(bufferedWriter, new DigestFileHasher(SHA_ALGORITHM), ERROR_HASH);
                HashFileVisitor hashFileVisitor = new HashFileVisitor(hashWriter, walkType);
                String rootPath;
                while (true) {
                    try {
                        rootPath = bufferedReader.readLine();
                    } catch (IOException e) {
                        System.err.printf("Cannot read input file '%s': %s%n", inputFile, e);
                        return;
                    }

                    if (rootPath == null) {
                        break;
                    }

                    Path path = getPath(rootPath, String.format("Invalid path '%s'", rootPath));

                    if (path == null) {
                        try {
                            hashWriter.writeError(rootPath);
                        } catch (IOException e) {
                            System.err.printf("Cannot write to output file: %s%n", e);
                            return;
                        }
                    } else {
                        try {
                            Files.walkFileTree(Path.of(rootPath), hashFileVisitor);
                        } catch (IOException e) {
                            System.err.printf("Cannot write to output file '%s': %s%n", outputFile, e);
                        } catch (SecurityException e) {
                            System.err.printf("Cannot open file: %s%n", e);

                            try {
                                hashWriter.writeError(rootPath);
                            } catch (IOException exception) {
                                System.err.printf("Cannot write to output file: %s%n", exception);
                                return;
                            }
                        }
                    }

                }
            } catch (IOException e) {
                System.err.printf("Cannot create writer to output file: %s%n", e);
            }
        } catch (IOException e) {
            System.err.printf("Cannot create reader from input file: %s%n", e);
        }
    }

    private static boolean isCorrectOutputFile(Path outputFile) {
        try {
            if (Files.exists(outputFile) || outputFile.getParent() == null) {
                return true;
            }
        } catch (SecurityException e) {
            System.err.printf("Cannot access file '%s': %s%n", outputFile, e);
            return false;
        }

        try {
            Files.createDirectories(outputFile.getParent());
        } catch (FileAlreadyExistsException e) {
            // :NOTE: родитель файла файл
            return true;
        } catch (IOException e) {
            System.err.printf("File '%s' doesn't exists and cannot be created: %s%n", outputFile, e);
            return false;
        } catch (SecurityException e) {
            System.err.printf("Cannot access file '%s': %s%n", outputFile, e);
            return false;
        }
        return true;
    }

    private static boolean isArgsSizeCorrect(String[] args) {
        if (args == null) {
            System.err.println("You should provide exactly 2 arguments: " +
                    "input filename and output filename, but you gave null arguments");
            return false;
        }
        if (args.length != 2) {
            System.err.printf("You should provide exactly 2 arguments: " +
                    "input filename and output filename, but you gave %d arguments%n", args.length);
            return false;
        }
        return true;
    }

    private static Path getPath(String path, String message) {
        if (path == null) {
            System.err.printf("%s: path is null%n", message);
            return null;
        }

        try {
            return Path.of(path);
        } catch (InvalidPathException e) {
            System.err.printf("%s: %s%n", message, e);
            return null;
        }
    }
}
