package io.quarkus.ts.security.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.PasswordType;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;

@UserDefinition
@Table(name = "sha512_entity")
@Entity
public class SHA512UserEntity extends PanacheEntity {
    @Column(name = "username")
    @Username
    public String username;

    @Column(name = "password")
    @Password(value = PasswordType.CUSTOM, provider = SHA512PasswordProvider.class)
    public String password;

    @Column(name = "role")
    @Roles
    public String role;

    public SHA512UserEntity() {
    }

    public SHA512UserEntity(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
