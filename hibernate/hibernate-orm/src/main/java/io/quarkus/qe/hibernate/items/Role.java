package io.quarkus.qe.hibernate.items;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "role") // import script expects lower case table name, identifiers are quoted, hence case-sensitive
public class Role {

    @Id
    public Long id;

    @Column
    public String name;

}