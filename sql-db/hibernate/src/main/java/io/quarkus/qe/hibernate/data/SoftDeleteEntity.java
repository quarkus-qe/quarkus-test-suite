package io.quarkus.qe.hibernate.data;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.EmbeddedColumnNaming;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

@SoftDelete(strategy = SoftDeleteType.TIMESTAMP)
@Entity
@Table(name = "soft_delete")
public class SoftDeleteEntity {

    public SoftDeleteEntity() {

    }

    @Id
    Long id;

    @Embedded
    @EmbeddedColumnNaming
    private Address homeAddress;

    @Embedded
    @EmbeddedColumnNaming
    private Address workAddress;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

    public Address getWorkAddress() {
        return workAddress;
    }

    public void setWorkAddress(Address workAddress) {
        this.workAddress = workAddress;
    }
}
