package io.quarkus.ts.spring.data.primitivetypes;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.quarkus.ts.spring.data.primitivetypes.model.Product;

@RepositoryRestResource(exported = false)
public interface ProductRepository extends CrudRepository<Product, Long> {

    List<Product> findByStatus(String status);
}
