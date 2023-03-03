package io.quarkus.ts.scheduling.quartz.failover;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
public class ExecutionEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    public Long id;
    public Long seconds;
    public String owner;

}
