package io.quarkus.ts.http.restclient.reactive.files;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public abstract class OsUtils {
    public static final long SIZE_2049MiB = 2148532224L;

    public abstract String getSum(Path path);

    public abstract void createFile(Path path, long size);

    public static OsUtils get() {
        return new JavaUtils();
    }
}

class JavaUtils extends OsUtils {

    @Override
    public String getSum(Path path) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            try (BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(path))) {
                DigestOutputStream digestStream = new DigestOutputStream(OutputStream.nullOutputStream(), digest);
                stream.transferTo(digestStream);
                return DatatypeConverter.printHexBinary(digest.digest()).toLowerCase();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void createFile(Path path, long size) {
        try (RandomAccessFile f = new RandomAccessFile(path.toAbsolutePath().toString(), "rw");) {
            f.setLength(size);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
