package io.quarkus.ts.hibernate.reactive.openshift;

import org.junit.jupiter.api.Disabled;

import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.ts.hibernate.reactive.MsSQLDatabaseHibernateReactiveIT;

@OpenShiftScenario
@Disabled("https://github.com/microsoft/mssql-docker/issues/769")
public class OpenShiftHibernateReactiveMsSQLIT extends MsSQLDatabaseHibernateReactiveIT {
}
