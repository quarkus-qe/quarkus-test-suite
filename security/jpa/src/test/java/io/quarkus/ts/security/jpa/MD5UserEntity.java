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
@Table(name = "md5_entity")
@Entity
public class MD5UserEntity extends PanacheEntity {
    @Column(name = "username")
    @Username
    public String username;

    @Column(name = "password")
    @Password(value = PasswordType.CUSTOM, provider = MD5PasswordProvider.class)
    public String password;

    @Column(name = "role")
    @Roles
    public String role;

    public MD5UserEntity() {
    }

    public MD5UserEntity(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
