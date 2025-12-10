package io.quarkus.qe.hibernate.entityless;

import jakarta.data.repository.Repository;

import org.hibernate.annotations.processing.SQL;

@Repository
public interface PersonRepository {

    @SQL("select role from person where name = :name")
    String findByName(String name);

}
