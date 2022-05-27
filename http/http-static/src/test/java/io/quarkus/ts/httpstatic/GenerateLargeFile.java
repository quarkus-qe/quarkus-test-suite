package io.quarkus.ts.http;

import java.io.RandomAccessFile;

public final class GenerateLargeFile {
    private static final int HALF_GIGABYTE = 1024 * 1024 * 512; // avoid https://github.com/quarkusio/quarkus/issues/2917

    private GenerateLargeFile() {
    }

    public static void main(String[] args) throws Exception {
        // args[0] is expected to be something like {path to repository}/http/http-static/target/classes

        try (RandomAccessFile f = new RandomAccessFile(args[0] + "/META-INF/resources/big-file", "rw");) {
            f.setLength(HALF_GIGABYTE);
        }
    }
}
