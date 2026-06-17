package io.quarkus.ts.reactive.rest.data.panache;

import java.util.List;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheRepositoryResource;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.rest.data.panache.MethodProperties;
import io.quarkus.rest.data.panache.ResourceProperties;

@ResourceProperties(hal = true, halCollectionName = "user_list", path = "users")
public interface UserResource extends PanacheRepositoryResource<UserRepository, UserEntity, Long> {

    @Override
    @MethodProperties(path = "all")
    List<UserEntity> list(Page page, Sort sort);

    @Override
    @MethodProperties(exposed = false)
    UserEntity update(Long id, UserEntity entity);
}
