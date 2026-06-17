package io.quarkus.ts.reactive.rest.data.panache;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity(name = "version")
public class VersionEntity extends PanacheEntityBase {
    @Id
    public String id;
}
