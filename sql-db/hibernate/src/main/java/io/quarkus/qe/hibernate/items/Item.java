package io.quarkus.qe.hibernate.items;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "item") // import script expects lower case table name, identifiers are quoted, hence case-sensitive
public class Item {

    @Id
    public Long id;

    public String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerId")
    public Customer customer;

}
