package io.quarkus.ts.hibernate.search.fruit;

import java.util.Set;

import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FieldProjection;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IdProjection;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.ProjectionConstructor;

@ProjectionConstructor
public record FruitProjection(@IdProjection Integer id, String name,
        @FieldProjection(path = "producers.name") Set<String> producers) {
}
