package io.quarkus.ts.security.jpa.reactive.rest;

import java.util.List;
import java.util.Objects;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

import io.quarkus.security.PermissionChecker;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.ts.security.jpa.reactive.db.MyEntity;
import io.quarkus.vertx.http.runtime.security.annotation.BasicAuthentication;
import io.quarkus.vertx.http.runtime.security.annotation.FormAuthentication;

@Path("/crud")
public class CrudResource {

    @Inject
    EntityManager entityManager;

    @GET
    @Path("/detail/{id}")
    public MyEntity getById(@PathParam("id") long id) {
        return entityManager.find(MyEntity.class, id);
    }

    @GET
    public List<MyEntity> listAll() {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(MyEntity.class);
        var from = cq.from(MyEntity.class);
        var select = cq.select(from);
        var typedQuery = entityManager.createQuery(select);
        return typedQuery.getResultList();
    }

    @Transactional
    @RolesAllowed("admin")
    @POST
    public MyEntity create(CreateEntityRequestDto createEntityDto) {
        Objects.requireNonNull(createEntityDto);
        Objects.requireNonNull(createEntityDto.email());
        Objects.requireNonNull(createEntityDto.name());
        MyEntity myEntity = new MyEntity();
        myEntity.name = createEntityDto.name();
        myEntity.email = createEntityDto.email();
        entityManager.persist(myEntity);
        return myEntity;
    }

    @FormAuthentication
    @Transactional
    @PATCH
    public MyEntity patch(MyEntity myEntity) {
        return entityManager.merge(myEntity);
    }

    @Transactional
    @DELETE
    @Path("{id}")
    public boolean deleteById(@PathParam("id") long id) {
        MyEntity myEntity = entityManager.find(MyEntity.class, id);
        entityManager.remove(myEntity);
        return true;
    }

    @BasicAuthentication
    @PermissionsAllowed("can-update")
    @Transactional
    @PUT
    @Path("{id}")
    public MyEntity update(@PathParam("id") long id, UpdateEntityRequestDto updateEntityDto) {
        Objects.requireNonNull(updateEntityDto);
        Objects.requireNonNull(updateEntityDto.email());
        MyEntity myEntity = entityManager.find(MyEntity.class, id);
        myEntity.email = updateEntityDto.email();
        return entityManager.merge(myEntity);
    }

    @PermissionChecker("can-update")
    boolean canUpdate(UpdateEntityRequestDto updateEntityDto, SecurityIdentity securityIdentity) {
        // only admin can set email to security@quarkus.io
        return securityIdentity.hasRole("admin") || !"security@quarkus.io".equals(updateEntityDto.email());
    }
}
