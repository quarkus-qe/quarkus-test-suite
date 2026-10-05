package org.acme;

import io.quarkus.hibernate.panache.PanacheEntity;
import io.quarkus.hibernate.panache.PanacheRepository;

public class MyPanacheEntityReactive implements PanacheEntity.Reactive {

    public String name;

    public interface Repository extends PanacheRepository.Reactive<Object, Long> {
    }
}
