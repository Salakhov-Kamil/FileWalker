import java.io.IOException;
import java.nio.file.Path;

public interface FileHasher {
    byte[] getFileHash(Path file) throws IOException;
}
