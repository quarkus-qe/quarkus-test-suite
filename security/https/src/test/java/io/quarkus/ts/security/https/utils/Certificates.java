package io.quarkus.ts.security.https.utils;

public final class Certificates {

    public static final String LOCATION = "src/main/resources/META-INF/resources";
    public static final String PKCS12 = "pkcs12";
    public static final String CLIENT_KEYSTORE = LOCATION + "/client-keystore." + PKCS12;
    public static final String CLIENT_TRUSTSTORE = LOCATION + "/client-truststore." + PKCS12;
    public static final String UNKNOWN_CLIENT_KEYSTORE = LOCATION + "/unknown-client-keystore." + PKCS12;
    public static final String GUESS_CLIENT_KEYSTORE = LOCATION + "/guest-client-keystore." + PKCS12;

    private Certificates() {

    }
}
