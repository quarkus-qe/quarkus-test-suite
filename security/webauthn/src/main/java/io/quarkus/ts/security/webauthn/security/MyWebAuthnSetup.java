package io.quarkus.ts.security.webauthn.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.security.webauthn.WebAuthnUserProvider;
import io.quarkus.ts.security.webauthn.model.User;
import io.quarkus.ts.security.webauthn.model.WebAuthnCredential;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.auth.webauthn.AttestationCertificates;
import io.vertx.ext.auth.webauthn.Authenticator;

@ApplicationScoped
public class MyWebAuthnSetup implements WebAuthnUserProvider {

    @WithTransaction
    @Override
    public Uni<List<Authenticator>> findWebAuthnCredentialsByUserName(String userName) {
        return WebAuthnCredential.findByUserName(userName)
                .flatMap(MyWebAuthnSetup::toAuthenticators);
    }

    @WithTransaction
    @Override
    public Uni<List<Authenticator>> findWebAuthnCredentialsByCredID(String credID) {
        return WebAuthnCredential.findByCredID(credID)
                .flatMap(MyWebAuthnSetup::toAuthenticators);
    }

    @WithTransaction
    @Override
    public Uni<Void> updateOrStoreWebAuthnCredentials(Authenticator authenticator) {
        return User.findByUserName(authenticator.getUserName())
                .flatMap(user -> {
                    // new user
                    if (user == null) {
                        User newUser = new User();
                        newUser.userName = authenticator.getUserName();
                        WebAuthnCredential credential = new WebAuthnCredential(authenticator, newUser);
                        return credential.persist()
                                .flatMap(c -> newUser.persist())
                                .onItem().ignore().andContinueWithNull();
                    } else {

                        // existing user
                        user.webAuthnCredential.counter = authenticator.getCounter();
                        return Uni.createFrom().nullItem();
                    }
                });
    }

    private static Uni<List<Authenticator>> toAuthenticators(List<WebAuthnCredential> dbs) {
        // can't call combine/uni on empty list
        if (dbs.isEmpty())
            return Uni.createFrom().item(Collections.emptyList());
        List<Uni<Authenticator>> ret = new ArrayList<>(dbs.size());
        for (WebAuthnCredential db : dbs) {
            ret.add(toAuthenticator(db));
        }
        return Uni.combine().all().unis(ret).with(f -> (List) f);
    }

    private static Uni<Authenticator> toAuthenticator(WebAuthnCredential credential) {
        return credential.fetch(credential.webAuthnx509Certificates)
                .map(x5c -> {
                    Authenticator ret = new Authenticator();
                    ret.setAaguid(credential.aaguid);
                    AttestationCertificates attestationCertificates = new AttestationCertificates();
                    attestationCertificates.setAlg(credential.alg);
                    ret.setAttestationCertificates(attestationCertificates);
                    ret.setCounter(credential.counter);
                    ret.setCredID(credential.credID);
                    ret.setFmt(credential.fmt);
                    ret.setPublicKey(credential.publicKey);
                    ret.setType(credential.type);
                    ret.setUserName(credential.userName);
                    return ret;
                });
    }

    @Override
    public Set<String> getRoles(String userId) {
        if (userId.equals("admin")) {
            Set<String> ret = new HashSet<>();
            ret.add("user");
            ret.add("admin");
            return ret;
        }
        return Collections.singleton("user");
    }
}
