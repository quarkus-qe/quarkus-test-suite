package io.quarkus.qe.hibernate.hql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    @GeneratedValue
    Long id;

    @Column
    String productName;

    @Column
    int quantity;

    @ManyToOne
    Orders orders;
}
