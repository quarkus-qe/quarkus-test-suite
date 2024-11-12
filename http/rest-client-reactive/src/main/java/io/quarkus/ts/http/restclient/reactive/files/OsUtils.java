package io.quarkus.ts.http.restclient.reactive.files;

import java.nio.file.Path;

public abstract class OsUtils {
    public static final long SIZE_2049MiB = 2148532224L;

    public abstract String getSum(Path path);

    public abstract void createFile(Path path, long size);

    public static OsUtils get() {
        return new JavaUtils();
    }
}
