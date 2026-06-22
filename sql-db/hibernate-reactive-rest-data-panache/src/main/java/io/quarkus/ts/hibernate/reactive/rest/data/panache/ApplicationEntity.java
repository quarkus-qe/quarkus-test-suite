package io.quarkus.ts.hibernate.reactive.rest.data.panache;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

@Entity(name = "application_entity")
public class ApplicationEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotEmpty
    @Column(unique = true, nullable = false)
    public String name;
}
