package io.quarkus.ts.spring.data.primitivetypes.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

//This is for regression test for https://github.com/quarkusio/quarkus/pull/13015
@MappedSuperclass
public class Animal {

    @Id
    @GeneratedValue
    private long id;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime birthDay;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime died;

    private String deathReason;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(LocalDateTime birthDay) {
        this.birthDay = birthDay;
    }

    public LocalDateTime getDied() {
        return died;
    }

    public void setDied(LocalDateTime died) {
        this.died = died;
    }

    public String getDeathReason() {
        return deathReason;
    }

    public void setDeathReason(String deathReason) {
        this.deathReason = deathReason;
    }
}
