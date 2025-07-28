package io.quarkus.ts.security.jpa.reactive.db;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import io.quarkus.security.jpa.RolesValue;

@Table(name = "test_role")
@Entity
public class TestRoleEntity {
    @Id
    @GeneratedValue
    public Long id;

    @ManyToMany(mappedBy = "roles")
    public List<TestUserEntity> users;

    @Column(name = "role_name")
    @RolesValue
    public String role;

}
