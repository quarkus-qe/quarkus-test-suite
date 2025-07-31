package io.quarkus.qe.hibernate.items;

import java.time.Instant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "customer") // import script expects lower case table name, identifiers are quoted, hence case-sensitive
public class Customer {

    @Id
    public Long id;

    @Version
    @Column(name = "version")
    public int version;

    @Column(name = "created_on")
    public Instant createdOn;

    public String[] licenses;

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE,
            CascadeType.REMOVE })
    public Account account;

}
