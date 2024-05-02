package io.quarkus.ts.security.https.utils;

import javax.net.ssl.SSLHandshakeException;

public final class HttpsAssertions {

    public static final String HELLO_SIMPLE_PATH = "/hello/simple";

    private HttpsAssertions() {
        // avoid instantiation
    }

    public static void assertTlsHandshakeError(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception ex) {
            if (isNotSSLHandshakeException(ex)) {
                throw ex;
            }
        }
    }

    private static boolean isNotSSLHandshakeException(Throwable throwable) {
        if (throwable instanceof SSLHandshakeException) {
            return false;
        }
        if (throwable.getCause() == null) {
            return true;
        }
        return isNotSSLHandshakeException(throwable.getCause());
    }
}
