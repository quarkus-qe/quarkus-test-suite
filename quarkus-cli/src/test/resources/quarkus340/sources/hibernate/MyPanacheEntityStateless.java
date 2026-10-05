package org.acme;

import io.quarkus.hibernate.panache.PanacheEntity;
import io.quarkus.hibernate.panache.PanacheRepository;

public class MyPanacheEntityStateless implements PanacheEntity.Stateless {

    public String name;

    public interface Repository extends PanacheRepository.Stateless<Object, Long> {
    }
}
