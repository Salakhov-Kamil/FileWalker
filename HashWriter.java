import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HexFormat;

public class HashWriter {
    private static final HexFormat hexFormat = HexFormat.of();

    private final Writer writer;

    private final FileHasher fileHasher;

    private final String errorHash;

    public HashWriter(Writer writer, FileHasher fileHasher, String errorHash) {
        this.writer = writer;
        this.fileHasher = fileHasher;
        this.errorHash = errorHash;
    }

    private void writeHash(String hash, String filename) throws IOException {
        writer.write(hash);
        writer.write(' ');
        writer.write(filename);
        writer.write(System.lineSeparator());
    }

    public void writeHash(Path file) throws IOException {
        writeHash(hexFormat.formatHex(fileHasher.getFileHash(file)), file.toString());
    }

    public void writeError(String path) throws IOException {
        writeHash(errorHash, path);
    }
}
