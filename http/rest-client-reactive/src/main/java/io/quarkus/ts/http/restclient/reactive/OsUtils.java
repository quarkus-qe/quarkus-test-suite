package io.quarkus.ts.http.restclient.reactive;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;

public abstract class OsUtils {
    public static final String SIZE_2049MiB = "2148532224";
    private static final boolean hasBash = hasBash();
    private static final int PROCESS_START_TIMEOUT = 3;

    protected static Uni<Process> run(String... params) throws IOException, InterruptedException {
        ProcessBuilder bash = new ProcessBuilder(params);
        Process process = bash.start();

        return Uni.createFrom().item(process).onItem().delayIt().by(Duration.ofSeconds(PROCESS_START_TIMEOUT));
    }

    private static boolean hasBash() {
        String os = System.getProperty("os.name").toLowerCase();
        return !os.contains("windows");
    }

    public abstract Uni<String> getSum(String path);

    public abstract void createFile(String path, String size);

    public static OsUtils get() {
        return hasBash
                ? new BashUtils()
                : new CmdUtils();
    }
}

class BashUtils extends OsUtils {
    private static final Logger LOG = Logger.getLogger(OsUtils.class);

    @Override
    public Uni<String> getSum(String path) {
        try {
            return OsUtils.run("md5sum", path).onItem()
                    .transform(process -> readInputStream(process.getInputStream(), ""))
                    .invoke(result -> LOG.info("Hash of " + path + " is " + result));
        } catch (IOException | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void createFile(String path, String size) {
        try {
            OsUtils.run("truncate", "-s", size, path);
        } catch (IOException | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String readInputStream(InputStream in, String defaultIfError) {
        String result = "";
        try {
            result = new String(in.readAllBytes()).split(" ")[0];
        } catch (IOException e) {
            LOG.error(e.getMessage());
            result = defaultIfError;
        }

        return result;
    }
}

class CmdUtils extends OsUtils {
    private static final Logger LOG = Logger.getLogger(OsUtils.class);

    @Override
    public Uni<String> getSum(String path) {
        try {
            return OsUtils.run("CertUtil", "-hashfile", path, "MD5").onItem()
                    .transform(process -> readInputStream(process.getInputStream(), ""))
                    .invoke(result -> LOG.info("Hash of " + path + " is " + result));
        } catch (IOException | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void createFile(String path, String size) {
        try {
            OsUtils.run("fsutil", "file", "createnew", path, size);
        } catch (IOException | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String readInputStream(InputStream in, String defaultIfError) {
        String result = "";
        try {
            result = new String(in.readAllBytes()).split("\n")[1];
        } catch (IOException e) {
            LOG.error(e.getMessage());
            result = defaultIfError;
        }

        return result;
    }
}
