package org.acme;

import io.quarkus.hibernate.panache.PanacheEntity;
import io.quarkus.hibernate.panache.PanacheRepository;

public class MyPanacheEntityManaged implements PanacheEntity.Managed {

    public String name;

    public interface Repository extends PanacheRepository.Managed<Object, Long> {
    }
}
