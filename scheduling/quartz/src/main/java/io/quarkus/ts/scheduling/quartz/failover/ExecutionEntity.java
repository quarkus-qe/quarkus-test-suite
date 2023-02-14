package io.quarkus.ts.scheduling.quartz.failover;

import jakarta.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class ExecutionEntity extends PanacheEntity {
    public Long seconds;
    public String owner;

}
