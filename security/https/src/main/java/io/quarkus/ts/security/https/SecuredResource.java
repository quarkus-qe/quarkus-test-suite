package io.quarkus.ts.security.https;

import java.security.cert.X509Certificate;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.security.credential.CertificateCredential;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/secured")
public class SecuredResource {
    @Inject
    SecurityIdentity identity;

    @GET // requires 'user' role
    @Produces(MediaType.TEXT_PLAIN)
    public String getHttps() {
        return getResponse();
    }

    @Path("mtls") // requires authentication
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getMtls() {
        return getResponse();
    }

    private String getResponse() {
        X509Certificate certificate = identity.getCredential(CertificateCredential.class).getCertificate();
        return "Client certificate: " + certificate.getSubjectX500Principal().getName();
    }
}
