package io.quarkus.qe.hibernate.items;

import jakarta.persistence.Cacheable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(name = "my_cached_entity")
public class MyCachedEntity {

    @EmbeddedId
    private MyEntityId id;

    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public MyEntityId getId() {
        return id;
    }

    public void setId(MyEntityId id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "MyCachedEntity{" +
                "id=" + id +
                ", data='" + data + '\'' +
                '}';
    }
}
