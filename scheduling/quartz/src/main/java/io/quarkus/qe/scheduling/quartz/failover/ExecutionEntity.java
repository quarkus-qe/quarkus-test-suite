package io.quarkus.qe.scheduling.quartz.failover;

import javax.persistence.Entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class ExecutionEntity extends PanacheEntity {
    public Long seconds;
    public String owner;

}
