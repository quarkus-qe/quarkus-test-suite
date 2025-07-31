package io.quarkus.qe.hibernate.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.hibernate.ReadOnlyMode;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Timeouts;

@Path("/test-data")
public class TestDataResource {

    private final StatelessSession statelessSession;
    private final Session session;

    public TestDataResource(@Named("named-2") StatelessSession statelessSession, @Named("named-2") Session session) {
        this.statelessSession = statelessSession;
        this.session = session;
    }

    @GET
    @Path("/get-multiple")
    public String getMultiple(@QueryParam("ids") List<Long> ids) {
        List<TestDataEntity> testDataEntities = statelessSession.getMultiple(TestDataEntity.class, ids);
        return testDataEntities.stream()
                .map(TestDataEntity::getContent)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.joining("-"));
    }

    @Produces(MediaType.APPLICATION_JSON)
    @GET
    @Path("/json-agg-hql-function")
    public String getUsingJsonAggHqlFunction() {
        return statelessSession
                .createQuery("select json_agg(t.json) from TestDataEntity t", String.class)
                .getSingleResult();
    }

    @Transactional
    @PUT
    @Path("/upsert-enum-values")
    public void setEnumTypes(@QueryParam("enums") List<String> input) {
        Map<Long, String> entityIdToCharacter = input.stream()
                .map(i -> i.split("-"))
                .collect(Collectors.toMap(i -> Long.parseLong(i[0]), i -> i[1]));
        var entities = statelessSession.getMultiple(TestDataEntity.class, new ArrayList<>(entityIdToCharacter.keySet()));
        entities.forEach(entity -> {
            var newCharacter = entityIdToCharacter.get(entity.getId());
            entity.setCharacter(TestDataEntity.Character.valueOf(newCharacter));
            statelessSession.upsert(entity);
        });
    }

    @GET
    @Path("/get-id-by-enum-in-where-clause")
    public Long getIdByEnumInWhereClause(@QueryParam("character") TestDataEntity.Character character) {
        var entity = statelessSession
                .createQuery("from TestDataEntity  t where t.character = (:character)", TestDataEntity.class)
                .setParameter("character", character)
                .getSingleResult();
        if (entity.getCharacter() != character) {
            throw new IllegalStateException("Character " + character + " not found");
        }
        return entity.getId();
    }

    @Transactional
    @GET
    @Path("/find-with-optional-read-only")
    public String findWithOptionalReadOnly(@QueryParam("id") long id, @QueryParam("new-content") String newContent,
            @QueryParam("read-only") boolean readOnly) {
        var entity = session.find(TestDataEntity.class, id, Timeouts.WAIT_FOREVER,
                readOnly ? ReadOnlyMode.READ_ONLY : ReadOnlyMode.READ_WRITE);
        if (newContent != null && !newContent.isEmpty()) {
            entity.setContent(newContent);
        }
        return entity.getContent();
    }

    @GET
    @Path("/get-using-named-query-with-enum-constant")
    public String namedQueryWithEnumConstant(@QueryParam("character") TestDataEntity.Character character) {
        var entity = session.createNamedQuery("TestDataEntity.FindByCharacter", TestDataEntity.class)
                .setParameter("character", character)
                .getSingleResultOrNull();
        return entity.getContent();
    }
}
