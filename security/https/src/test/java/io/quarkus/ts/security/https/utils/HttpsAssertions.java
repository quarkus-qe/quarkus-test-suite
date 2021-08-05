package io.quarkus.ts.security.https.utils;

import static org.junit.jupiter.api.Assertions.fail;

import java.net.SocketException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;

public final class HttpsAssertions {
    private HttpsAssertions() {
        // avoid instantiation
    }

    public static void assertTlsHandshakeError(Executable executable) {
        // on other OSs, JVMs, or configurations, different exceptions can be thrown
        // e.g. WildFly test suite mentions that on Windows, SocketException is common

        if (hasTls13()) {
            // Java 11+, defaults to TLSv1.3
            try {
                executable.execute();
            } catch (Throwable e) {
                if (e instanceof AssertionError || e instanceof AssertionFailedError) {
                    sneakyThrow(e);
                } else if (!(e instanceof SSLHandshakeException)
                        && !(e instanceof SSLException && e.getCause() instanceof SocketException)) {
                    fail(e);
                }
            }
        } else {
            // Java 8, defaults to TLSv1.2
            try {
                executable.execute();
            } catch (Throwable e) {
                if (e instanceof AssertionError || e instanceof AssertionFailedError) {
                    sneakyThrow(e);
                } else if (!(e instanceof SSLHandshakeException)) {
                    fail(e);
                }
            }
        }
    }

    public static void assertTls13OnlyHandshakeError(Executable executable) {
        if (hasTls13()) {
            // Java 11+, defaults to TLSv1.3
            assertTlsHandshakeError(executable);
        } else {
            // Java 8, defaults to TLSv1.2
            try {
                executable.execute();
            } catch (Throwable e) {
                throw sneakyThrow(e);
            }
        }
    }

    private static boolean hasTls13() {
        try {
            String[] protocols = SSLContext.getDefault().getDefaultSSLParameters().getProtocols();
            for (String protocol : protocols) {
                if ("TLSv1.3".equals(protocol)) {
                    return true;
                }
            }
            return false;
        } catch (NoSuchAlgorithmException e) {
            throw sneakyThrow(e);
        }
    }

    /**
     * This method can and should be used as part of a {@code throw} statement,
     * such as: {@code throw sneakyThrow(exception);}. It is guaranteed to never return normally,
     * and this style of usage makes sure that the Java compiler is aware of that.
     */
    private static <E extends Throwable> RuntimeException sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
}
