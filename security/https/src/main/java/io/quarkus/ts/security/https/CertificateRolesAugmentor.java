package io.quarkus.ts.security.https;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;

import javax.inject.Singleton;

import io.quarkus.security.credential.CertificateCredential;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

/**
 * Quarkus doesn't have a declarative/configuration way to assign roles when using certificate authentication.
 */
@Singleton
public class CertificateRolesAugmentor implements SecurityIdentityAugmentor {
    @Override
    public int priority() {
        return 0;
    }

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        return Uni.createFrom().item(() -> {
            QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder()
                    .setPrincipal(identity.getPrincipal())
                    .addAttributes(identity.getAttributes())
                    .addCredentials(identity.getCredentials())
                    .addRoles(identity.getRoles());
            CertificateCredential certificate = identity.getCredential(CertificateCredential.class);
            if (certificate != null) {
                builder.addRoles(extractRoles(certificate.getCertificate()));
            }
            return builder.build();
        });
    }

    private Set<String> extractRoles(X509Certificate certificate) {
        String name = certificate.getSubjectX500Principal().getName();

        switch (name) {
            case "CN=client":
                return Collections.singleton("user");
            case "CN=guest-client":
                return Collections.singleton("guest");
            default:
                return Collections.emptySet();
        }
    }
}
