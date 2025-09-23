package io.quarkus.ts.jakarta.data.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.StatelessSession;

import io.quarkus.arc.InjectableInstance;
import io.quarkus.hibernate.orm.PersistenceUnit.PersistenceUnitLiteral;

/**
 * Redirect Jakarta Data repositories to the currently active named persistence unit.
 * See also <a href="https://quarkus.io/version/main/guides/hibernate-orm#persistence-unit-active">the documentation</a>.
 */
public class RepositoryBeansProducer {

    @Produces
    @ApplicationScoped
    public StatelessSession produceStatelessSession(@ConfigProperty(name = "quarkus.profile") String datasource,
            @Any InjectableInstance<StatelessSession> sessionInstance) {
        return sessionInstance.select(new PersistenceUnitLiteral(datasource)).get();
    }

    @Produces
    @ApplicationScoped
    public EntityManager produceEntityManager(@ConfigProperty(name = "quarkus.profile") String datasource,
            @Any InjectableInstance<EntityManager> entityManager) {
        return entityManager.select(new PersistenceUnitLiteral(datasource)).get();
    }
}
