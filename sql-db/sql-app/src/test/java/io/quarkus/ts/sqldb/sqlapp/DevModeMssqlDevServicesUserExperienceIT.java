package io.quarkus.ts.sqldb.sqlapp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.model.Image;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.utils.SocketUtils;

@Tag("QUARKUS-959")
@QuarkusScenario
public class DevModeMssqlDevServicesUserExperienceIT {

    private static final String MSSQL_VERSION = "2017-CU12";
    private static final String MSSQL_NAME = "mcr.microsoft.com/mssql/server";

    @DevModeQuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.datasource.db-kind", "mssql")
            .withProperty("quarkus.datasource.devservices.port", Integer.toString(SocketUtils.findAvailablePort()))
            .withProperty("quarkus.datasource.devservices.image-name", MSSQL_NAME + ":" + MSSQL_VERSION)
            .withProperty("quarkus.hibernate-orm.dialect", "org.hibernate.dialect.SQLServer2012Dialect")
            .withProperty("quarkus.datasource.jdbc.driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver")
            .withProperty("quarkus.hibernate-orm.database.generation", "none")
            .onPreStart(s -> DockerUtils.removeImage(MSSQL_NAME, MSSQL_VERSION));

    @Test
    public void verifyIfUserIsInformedAboutMssqlDevServicePulling() {
        app.logs().assertContains("Pulling docker image: " + MSSQL_NAME + ":" + MSSQL_VERSION);
        app.logs().assertContains("Please be patient; this may take some time but only needs to be done once");
        app.logs().assertContains("Starting to pull image");
        app.logs().assertContains("Dev Services for Microsoft SQL Server started");
    }

    @Test
    public void verifyMssqlImage() {
        Image postgresImg = DockerUtils.getImage(MSSQL_NAME, MSSQL_VERSION);
        Assertions.assertFalse(postgresImg.getId().isEmpty(), String.format("%s:%s not found. " +
                "Notice that user set his own custom image by 'quarkus.datasource.devservices.image-name' property",
                MSSQL_NAME, MSSQL_VERSION));
    }
}
