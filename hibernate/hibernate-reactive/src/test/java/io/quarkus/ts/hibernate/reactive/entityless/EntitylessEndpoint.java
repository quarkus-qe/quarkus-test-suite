package io.quarkus.ts.hibernate.reactive.entityless;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.hibernate.reactive.mutiny.Mutiny;

import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;

@Path("/entityless/")
public class EntitylessEndpoint {

    @Inject
    Mutiny.SessionFactory sf;

    @Startup
    @Transactional
    public void init() {
        Mutiny.StatelessSession statelessSession = sf.openStatelessSession().subscribe().asCompletionStage().join();

        statelessSession.createNativeQuery("DROP TABLE IF EXISTS person;").executeUpdate().subscribeAsCompletionStage().join();
        statelessSession.createNativeQuery("""
                CREATE TABLE person(
                        id INT,
                       name VARCHAR(255),
                        role VARCHAR(255)
                    );""").executeUpdate().subscribeAsCompletionStage().join();

        statelessSession.createNativeQuery("INSERT INTO person(id,name,role) values(1,'John','Owner');").executeUpdate()
                .subscribeAsCompletionStage().join();
        statelessSession.createNativeQuery("INSERT INTO person(id,name,role) values(2,'Harry','Wizard');").executeUpdate()
                .subscribeAsCompletionStage().join();
        statelessSession.createNativeQuery("INSERT INTO person(id,name,role) values(3,'Sauron','Antagonist');")
                .executeUpdate().subscribeAsCompletionStage().join();
    }

    @GET
    @Path("/native-query")
    public Uni<String> nativeGet(@QueryParam("name") String name) {
        return sf.withStatelessSession(statelessSession -> statelessSession
                .createNativeQuery("SELECT role FROM person WHERE name = '" + name + "';", String.class).getSingleResult());
    }
}
