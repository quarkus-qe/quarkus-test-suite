package io.quarkus.ts.security.webauthn.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;

@Table(name = "user_table")
@Entity
public class User extends PanacheEntity {

    @Column(unique = true)
    public String userName;

    @OneToOne(mappedBy = "user")
    public WebAuthnCredential webAuthnCredential;

    public static Uni<User> findByUserName(String userName) {
        return find("userName", userName).firstResult();
    }
}
