package io.quarkus.ts.jakarta.data.security;

import jakarta.data.repository.Repository;

@Repository
public interface BookInheritedTypeSecuredRepository extends BookParentTypeRepository {
}
