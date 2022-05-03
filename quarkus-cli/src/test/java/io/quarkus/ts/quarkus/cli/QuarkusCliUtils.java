package io.quarkus.ts.quarkus.cli;

import static io.quarkus.test.bootstrap.QuarkusCliClient.CreateApplicationRequest.defaults;

import java.util.regex.Pattern;

import io.quarkus.builder.Version;
import io.quarkus.test.bootstrap.QuarkusCliClient;

public class QuarkusCliUtils {

    public static QuarkusCliClient.CreateApplicationRequest defaultWithFixedStream() {
        return defaults().withStream(getCurrentStreamVersion());
    }

    public static QuarkusCliClient.CreateExtensionRequest defaultNewExtensionArgsWithFixedStream() {
        return QuarkusCliClient.CreateExtensionRequest.defaults().withStream(getCurrentStreamVersion());
    }

    public static String getCurrentStreamVersion() {
        String[] version = Version.getVersion().split(Pattern.quote("."));
        return String.format("%s.%s", version[0], version[1]);
    }
}
