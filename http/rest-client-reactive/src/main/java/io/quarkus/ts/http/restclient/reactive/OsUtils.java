package io.quarkus.ts.http.restclient.reactive;

import java.io.IOException;

import org.jboss.logging.Logger;

public abstract class OsUtils {
    public static final String SIZE_2049MiB = "2148532224";
    private static final boolean hasBash = hasBash();

    protected static Process run(String... params) throws IOException, InterruptedException {
        ProcessBuilder bash = new ProcessBuilder(params);
        Process process = bash.start();

        process.waitFor();
        return process;
    }

    private static boolean hasBash() {
        String os = System.getProperty("os.name").toLowerCase();
        return !os.contains("windows");
    }

    public abstract String getSum(String path);

    public abstract void createFile(String path, String size);

    public static OsUtils get() {
        return hasBash
                ? new BashUtils()
                : new CmdUtils();
    }
}

class BashUtils extends OsUtils {
    private static final Logger LOG = Logger.getLogger(OsUtils.class);

    public String getSum(String path) {
        try {
            Process process = OsUtils.run("md5sum", path);
            String result = new String(process.getInputStream().readAllBytes()).split(" ")[0];
            LOG.info("Hash of " + path + " is " + result);
            return result;
        } catch (IOException | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void createFile(String path, String size) {
        try {
            OsUtils.run("truncate", "-s", size, path);
        } catch (IOException | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

class CmdUtils extends OsUtils {
    private static final Logger LOG = Logger.getLogger(OsUtils.class);

    @Override
    public String getSum(String path) {
        try {
            Process process = OsUtils.run("CertUtil", "-hashfile", path, "MD5");
            String output = new String(process.getInputStream().readAllBytes());
            String result = output.split("\n")[1];
            LOG.info("Hash of " + path + " is " + result);
            return result;
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
}
