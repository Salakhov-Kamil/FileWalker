import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class DigestFileHasher implements FileHasher {
    private static final int BUFFER_SIZE = 1 << 15;

    private static final byte[] buffer = new byte[BUFFER_SIZE];

    private final MessageDigest messageDigest;

    public DigestFileHasher(MessageDigest messageDigest) {
        this.messageDigest = messageDigest;
    }

    @Override
    public byte[] getFileHash(Path file) throws IOException {
        try (DigestInputStream dis = new DigestInputStream(
                Files.newInputStream(file),
                messageDigest
        )) {
            try {
                while (dis.read(buffer) >= 0) {
                    // do nothing
                }
            } catch (IOException e) {
                messageDigest.reset();
                throw e;
            }
            return messageDigest.digest();
        }
    }
}
