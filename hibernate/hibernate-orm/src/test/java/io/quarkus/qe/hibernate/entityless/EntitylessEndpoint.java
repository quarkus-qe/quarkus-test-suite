package io.quarkus.qe.hibernate.entityless;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.hibernate.StatelessSession;

import io.quarkus.runtime.Startup;

@Path("/entityless/")
public class EntitylessEndpoint {

    @Inject
    StatelessSession statelessSession;

    @Inject
    PersonRepository repository;

    @Startup
    @Transactional
    public void init() {
        // Initiate the DB, since hibernate will not import SQL script if no entity is defined
        statelessSession.createNativeQuery("DROP TABLE IF EXISTS person;").executeUpdate();
        statelessSession.createNativeQuery("""
                CREATE TABLE person(
                        id INT,
                       name VARCHAR(255),
                        role VARCHAR(255)
                    );""").executeUpdate();

        statelessSession.createNativeQuery("INSERT INTO person(id,name,role) values(1,'John','Owner');").executeUpdate();
        statelessSession.createNativeQuery("INSERT INTO person(id,name,role) values(2,'Harry','Wizard');").executeUpdate();
        statelessSession.createNativeQuery("INSERT INTO person(id,name,role) values(3,'Sauron','Antagonist');")
                .executeUpdate();
    }

    @GET
    @Path("/native-query")
    public String nativeGet(@QueryParam("name") String name) {
        return statelessSession.createNativeQuery("SELECT role FROM person WHERE name = '" + name + "';", String.class)
                .getSingleResult();
    }

    @GET
    @Path("/repository")
    public String repositoryGet(@QueryParam("name") String name) {
        return repository.findByName(name);
    }
}
