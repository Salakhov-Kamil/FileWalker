import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class HashFileVisitor extends SimpleFileVisitor<Path> {
    private final HashWriter hashWriter;

    private final WalkType walkType;

    public HashFileVisitor(HashWriter hashWriter, WalkType walkType) {
        this.hashWriter = hashWriter;
        this.walkType = walkType;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return switch (walkType) {
            case LINEAR -> {
                writeError(dir, new NoSuchFileException(
                        String.format("'%s' is directory, not a file%n", dir)
                ));
                yield FileVisitResult.TERMINATE;
            }
            case RECURSIVE -> FileVisitResult.CONTINUE;
        };
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
        try {
            hashWriter.writeHash(path);
        } catch (IOException e) {
            writeError(path, e);
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException exc) {
        writeError(path, exc);

        return FileVisitResult.CONTINUE;
    }


    private void writeError(Path path, IOException exc) {
        System.err.printf("Error when visiting file '%s': %s%n", path, exc);

        try {
            hashWriter.writeError(path.toString());
        } catch (IOException e) {
            System.err.printf("Cannot write to output file '%s': %s%n", path, e);
        }
    }
}
