package site.siredvin.peripheralworks;

import java.io.OutputStream;
import java.security.MessageDigest;

public final class DigestOutputStream extends OutputStream {
    private final MessageDigest digest;

    public DigestOutputStream(MessageDigest digest) {
        this.digest = digest;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        digest.update(b, off, len);
    }

    @Override
    public void write(int b) {
        digest.update((byte) b);
    }
}
