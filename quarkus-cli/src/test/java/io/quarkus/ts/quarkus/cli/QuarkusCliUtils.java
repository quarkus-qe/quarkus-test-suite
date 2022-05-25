package io.quarkus.ts.quarkus.cli;

import static io.quarkus.test.bootstrap.QuarkusCliClient.CreateApplicationRequest.defaults;

import java.util.regex.Pattern;

import org.jboss.logging.Logger;

import io.quarkus.builder.Version;
import io.quarkus.test.bootstrap.QuarkusCliClient;

public class QuarkusCliUtils {

    public static final String QUARKUS_UPSTREAM_VERSION = "999-SNAPSHOT";
    private static final Logger LOG = Logger.getLogger(QuarkusCliUtils.class);

    public static QuarkusCliClient.CreateApplicationRequest defaultWithFixedStream() {
        String version = getCurrentStreamVersion();
        if (isUpstream(version)) {
            LOG.warn("fixed streams are not supported on upstream");
            return defaults();
        }

        return defaults().withStream(version);
    }

    public static QuarkusCliClient.CreateExtensionRequest defaultNewExtensionArgsWithFixedStream() {
        String version = getCurrentStreamVersion();
        if (isUpstream(version)) {
            LOG.warn("fixed streams are not supported on upstream");
            return QuarkusCliClient.CreateExtensionRequest.defaults();
        }

        return QuarkusCliClient.CreateExtensionRequest.defaults().withStream(version);
    }

    public static boolean isUpstream(String version) {
        return version.equalsIgnoreCase(QUARKUS_UPSTREAM_VERSION);
    }

    public static String getCurrentStreamVersion() {
        String rawVersion = Version.getVersion();
        if (QUARKUS_UPSTREAM_VERSION.equalsIgnoreCase(rawVersion)) {
            return QUARKUS_UPSTREAM_VERSION;
        }

        String[] version = rawVersion.split(Pattern.quote("."));
        return String.format("%s.%s", version[0], version[1]);
    }
}
