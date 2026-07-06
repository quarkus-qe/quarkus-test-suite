package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.dpop;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.oidc.DPoPNonceProvider;

@ApplicationScoped
@LookupIfProperty(name = "dpop.nonce.provider.enabled", stringValue = "true")
public class NonceProvider implements DPoPNonceProvider {
    public static final String NONCE = "nonce-from-provider";

    @Override
    public String getNonce() {
        return NONCE;
    }

    @Override
    public boolean isValid(String nonce) {
        return NONCE.equals(nonce);
    }
}
