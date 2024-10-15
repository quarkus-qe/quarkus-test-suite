package io.quarkus.ts.lifecycle;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class LifecycleApplicationIT {

    static final String LOGGING_PROPERTY = "-Djava.util.logging.manager=org.jboss.logmanager.LogManager";

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    public void shouldArgumentsNotContainLoggingProperty() {
        String actualArguments = app.given().get("/args")
                .then().statusCode(HttpStatus.SC_OK).extract().asString();

        assertFalse(StringUtils.contains(actualArguments, LOGGING_PROPERTY),
                "Actual arguments contain unexpected properties: " + actualArguments);
    }

    @Test
    public void shouldPrintMessagesFromQuarkusMain() {
        String argumentsLine = app.getLogs().stream().collect(Collectors.joining());
        assertFalse(argumentsLine.contains(LOGGING_PROPERTY),
                "Pod log contain unexpected properties. Actual content: " + argumentsLine);
    }
}
