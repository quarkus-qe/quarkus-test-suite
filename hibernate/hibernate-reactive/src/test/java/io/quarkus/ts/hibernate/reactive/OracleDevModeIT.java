package io.quarkus.ts.hibernate.reactive;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.ts.hibernate.reactive.database.Author;
import io.quarkus.ts.hibernate.reactive.database.AuthorIdGenerator;
import io.quarkus.ts.hibernate.reactive.database.Book;
import io.quarkus.ts.hibernate.reactive.database.ISBNConverter;
import io.quarkus.ts.hibernate.reactive.database.XmlValidatedCustomer;

@QuarkusScenario
@DisabledIfSystemProperty(named = "ts.arm.missing.services.excludes", matches = "true", disabledReason = "https://github.com/quarkus-qe/quarkus-test-suite/issues/2022")
public class OracleDevModeIT {

    @DevModeQuarkusApplication(properties = "oracle.properties", classes = { Author.class, AuthorIdGenerator.class, Book.class,
            ISBNConverter.class, XmlValidatedCustomer.class })
    static RestService app = new RestService()
            .withProperty("quarkus.profile", "dev")
            .withProperty("quarkus.hibernate-orm.validation.mode", "auto")
            .withProperty("%dev.quarkus.datasource.devservices.image-name", "${oracle.image}");

    protected RestService getApp() {
        return app;
    }

    @Test
    @Tag("QUARKUS-6792")
    public void verifySchema() {
        app.logs().assertDoesNotContain("Failed to validate");
    }
}
