package io.quarkus.ts.security.jpa.reactive.db;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "my_entity")
public class MyEntity {

    @Id
    @GeneratedValue
    public Long id;

    public String name;

    public String email;

}
