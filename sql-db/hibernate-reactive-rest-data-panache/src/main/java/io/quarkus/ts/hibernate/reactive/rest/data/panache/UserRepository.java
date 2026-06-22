package io.quarkus.ts.hibernate.reactive.rest.data.panache;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<UserEntity, Long> {
}
