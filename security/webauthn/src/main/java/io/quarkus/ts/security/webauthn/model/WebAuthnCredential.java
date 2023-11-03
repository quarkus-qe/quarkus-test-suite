package io.quarkus.ts.security.webauthn.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.auth.webauthn.Authenticator;
import io.vertx.ext.auth.webauthn.PublicKeyCredential;

@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "userName", "credID" }))
@Entity
public class WebAuthnCredential extends PanacheEntity {
    /**
     * The username linked to this authenticator
     */
    public String userName;

    /**
     * The type of key (must be "public-key")
     */
    public String type = "public-key";

    /**
     * The non user identifiable id for the authenticator
     */
    public String credID;

    /**
     * The public key associated with this authenticator
     */
    public String publicKey;

    /**
     * The signature counter of the authenticator to prevent replay attacks
     */
    public long counter;

    public String aaguid;

    /**
     * The Authenticator attestation certificates object, a JSON like:
     *
     * <pre>{@code
     *   {
     *     "alg": "string",
     *     "x5c": [
     *       "base64"
     *     ]
     *   }
     * }</pre>
     */
    /**
     * The algorithm used for the public credential
     */
    public PublicKeyCredential alg;

    /**
     * The list of X509 certificates encoded as base64url.
     */
    @OneToMany(mappedBy = "webAuthnCredential")
    public List<WebAuthnCertificate> webAuthnx509Certificates = new ArrayList<>();

    public String fmt;

    // owning side
    @OneToOne
    public User user;

    public WebAuthnCredential() {
    }

    public WebAuthnCredential(Authenticator authenticator, User user) {
        aaguid = authenticator.getAaguid();
        if (authenticator.getAttestationCertificates() != null)
            alg = authenticator.getAttestationCertificates().getAlg();
        counter = authenticator.getCounter();
        credID = authenticator.getCredID();
        fmt = authenticator.getFmt();
        publicKey = authenticator.getPublicKey();
        type = authenticator.getType();
        userName = authenticator.getUserName();
        if (authenticator.getAttestationCertificates() != null
                && authenticator.getAttestationCertificates().getX5c() != null) {
            for (String x509VCertificate : authenticator.getAttestationCertificates().getX5c()) {
                WebAuthnCertificate cert = new WebAuthnCertificate();
                cert.base64X509Certificate = x509VCertificate;
                cert.webAuthnCredential = this;
                this.webAuthnx509Certificates.add(cert);
            }
        }
        this.user = user;
        user.webAuthnCredential = this;
    }

    public static Uni<WebAuthnCredential> createWebAuthnCredential(Authenticator authenticator, User user) {
        WebAuthnCredential credential = new WebAuthnCredential(authenticator, user);
        credential.persistAndFlush();
        user.webAuthnCredential = credential;
        user.persistAndFlush();
        return Uni.createFrom().item(credential);
    }

    public static Uni<List<WebAuthnCredential>> findByUserName(String userName) {
        return list("userName", userName);
    }

    public static Uni<List<WebAuthnCredential>> findByCredID(String credID) {
        return list("credID", credID);
    }

    public <T> Uni<T> fetch(T association) {
        return getSession().flatMap(session -> session.fetch(association));
    }
}
