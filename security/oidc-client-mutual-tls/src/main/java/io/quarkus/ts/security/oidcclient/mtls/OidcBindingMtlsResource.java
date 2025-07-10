package io.quarkus.ts.security.oidcclient.mtls;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.Authenticated;
import io.quarkus.security.credential.CertificateCredential;
import io.quarkus.security.identity.SecurityIdentity;

@Authenticated
@Path("/mtls-binding")
public class OidcBindingMtlsResource {

    @Inject
    SecurityIdentity mtlsIdentity;

    @Inject
    JsonWebToken accessToken;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getPrincipalsInfo() {
        var cred = mtlsIdentity.getCredential(CertificateCredential.class).getCertificate();
        return Response
                .ok("Mtls principal is: " + cred.getSubjectX500Principal().getName() +
                        " and used access token is issued for: " + accessToken.getName())
                .build();
    }
}
