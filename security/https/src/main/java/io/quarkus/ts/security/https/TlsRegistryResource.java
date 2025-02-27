package io.quarkus.ts.security.https;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.tls.TlsConfiguration;
import io.quarkus.tls.TlsConfigurationRegistry;

@Path("/tls-registry")
public class TlsRegistryResource {

    public static final String DUMMY_ENTRY_CERT = "dummy-entry-0";

    @Inject
    TlsConfigurationRegistry tlsConfigurationRegistry;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String tlsRegistryInspection() throws KeyStoreException, CertificateParsingException {
        TlsConfiguration tlsConfiguration = tlsConfigurationRegistry.getDefault().orElseThrow();

        KeyStore keyStore = tlsConfiguration.getKeyStore();
        if (keyStore == null) {
            throw new WebApplicationException("No KeyStore found in default TLS configuration.",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
        X509Certificate x509Certificate = (X509Certificate) keyStore.getCertificate(DUMMY_ENTRY_CERT);
        if (x509Certificate == null) {
            throw new WebApplicationException("No certificate found with alias " + DUMMY_ENTRY_CERT,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
        return "Subject X500 : " + x509Certificate.getSubjectX500Principal().getName()
                + "\nSubject Alternative names (SANs) : " + x509Certificate.getSubjectAlternativeNames();
    }
}
