package io.quarkus.ts.envinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OsJvmEnvInfoLogger {
    private static final Path REDHAT_RELEASE_PATH = Path.of("/etc/redhat-release");
    private static final String OUTPUT_TEMPLATE = "<li>%s\n"
            + "  <ul>\n"
            + "    <li>Architecture\n"
            + "      <ul>\n"
            + "        <li>%s</li>\n"
            + "      </ul>\n"
            + "    </li>\n"
            + "    <li>JVM\n"
            + "      <ul>\n"
            + "        <li>%s</li>\n"
            + "        <li>%s</li>\n"
            + "      </ul>\n"
            + "    </li>\n"
            + "  </ul>\n"
            + "</li>\n";

    public static void main(String[] args) throws IOException, InterruptedException {
        final String os = getOs();
        final String architecture = System.getProperty("os.arch");
        final String javaVersionCmdOut = getJavaVersionCmdOutput();
        final String jvmVersionShort = System.getProperty("java.vm.specification.version");

        Path output = Path.of(args[0]);
        Files.writeString(output, String.format(OUTPUT_TEMPLATE, os, architecture, javaVersionCmdOut, jvmVersionShort));
        System.out.println("       OS/JVM details are available in " + output);
    }

    private static String getOs() throws IOException {
        final String genericOsName = System.getProperty("os.name");
        if (Objects.equals(genericOsName, "Linux") && Files.exists(REDHAT_RELEASE_PATH)) {
            return Files.readString(REDHAT_RELEASE_PATH).trim();
        }
        return genericOsName + " " + System.getProperty("os.version");
    }

    private static String getJavaVersionCmdOutput() throws InterruptedException, IOException {
        final String cmd = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java --version";
        Process pr = Runtime.getRuntime().exec(cmd);
        pr.waitFor(1, TimeUnit.MINUTES);
        return new BufferedReader(new InputStreamReader(pr.getInputStream())).lines().reduce((first, second) -> second)
                .orElse(null);
    }
}
