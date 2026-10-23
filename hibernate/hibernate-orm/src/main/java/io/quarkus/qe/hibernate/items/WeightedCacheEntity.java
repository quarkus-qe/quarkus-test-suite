package io.quarkus.qe.hibernate.items;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Mapped to its own 2LC region, configured in {@code application.properties} for weight-based eviction ('maximum-weight' with a
 * weigher), used to show the weight bound evicts.
 * The fields are the same as the other cache entities on purpose: only the region configuration differs.
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
public class WeightedCacheEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 4000)
    private String data;

    public Long getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
