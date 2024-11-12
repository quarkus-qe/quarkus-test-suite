package io.quarkus.ts.envinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class OsJvmEnvInfoLogger {
    private static final Path REDHAT_RELEASE_PATH = Path.of("/etc/redhat-release");
    private static final String OUTPUT_TEMPLATE = """
            <li>%s
              <ul>
                <li>Architecture
                  <ul>
                    <li>%s</li>
                  </ul>
                </li>
                <li>JVM
                  <ul>
                    <li>%s</li>
                    <li>%s</li>
                  </ul>
                </li>
                <li>MVN
                  <ul>
                    <li>%s</li>
                  </ul>
                </li>
              </ul>
            </li>
            """;

    private static final String VERSION_COMMAND = "--version";

    public static void main(String[] args) throws IOException, InterruptedException {
        final String os = getOs();
        final String architecture = System.getProperty("os.arch");
        final String javaVersionCmdOut = getJavaVersionCmdOutput();
        final List<String> mvnVersionCmdOut = getMvnVersionCmdOutput();
        final String jvmVersionShort = System.getProperty("java.vm.specification.version");

        Path output = Path.of(args[0]);
        Files.writeString(output, String.format(OUTPUT_TEMPLATE, os, architecture, javaVersionCmdOut, jvmVersionShort,
                String.join("</li>\n        <li>", mvnVersionCmdOut)));
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
        final String javaExecutablePath = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final String[] cmd = new String[] { javaExecutablePath, VERSION_COMMAND };
        Process pr = Runtime.getRuntime().exec(cmd);
        pr.waitFor(1, TimeUnit.MINUTES);
        return new BufferedReader(new InputStreamReader(pr.getInputStream())).lines().reduce((first, second) -> second)
                .orElse(null);
    }

    private static List<String> getMvnVersionCmdOutput() {
        String mavenExecutablePath = System.getProperty("maven.home") + File.separator + "bin" + File.separator + "mvn";
        final String[] cmd = new String[] { mavenExecutablePath, VERSION_COMMAND };
        try {
            Process pr = Runtime.getRuntime().exec(cmd);
            pr.waitFor(1, TimeUnit.MINUTES);
            return new BufferedReader(new InputStreamReader(pr.getInputStream())).lines().collect(Collectors.toList());
        } catch (IOException | InterruptedException e) {
            return Collections.emptyList();
        }
    }
}
