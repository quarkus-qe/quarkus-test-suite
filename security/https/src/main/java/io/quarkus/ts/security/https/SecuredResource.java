package io.quarkus.ts.security.https;

import java.security.cert.X509Certificate;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.quarkus.security.credential.CertificateCredential;
import io.quarkus.security.identity.SecurityIdentity;

@Path("/secured")
public class SecuredResource {
    @Inject
    SecurityIdentity identity;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        X509Certificate certificate = identity.getCredential(CertificateCredential.class).getCertificate();
        return "Client certificate: " + certificate.getSubjectX500Principal().getName();
    }
}
