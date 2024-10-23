package io.quarkus.ts.logging.jboss;

import java.util.function.BiConsumer;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;

@QuarkusScenario
public class LoggingInJsonIT {

    private static final String EXPECTED_JSON_MESSAGE = "\"level\":\"INFO\",\"message\":\"Profile prod activated. \"";
    private static final String MESSAGE = "messageLog";

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperties("default.properties");

    @BeforeEach
    public void setup() {
        // Reset level to INFO
        setLogLevelTo(Logger.Level.INFO);
    }

    @Test
    public void shouldBeInJsonFormatByDefault() {
        // By default, the format should be in JSON format
        app.logs().assertContains(EXPECTED_JSON_MESSAGE);

        disableJsonFormatLogging();
        app.logs().assertDoesNotContain(EXPECTED_JSON_MESSAGE);
    }

    @Test
    public void shouldLogInStaticLogger() {
        verifyLoggingRules(this::addMessageInStaticLog);
    }

    @Test
    public void shouldLogInFieldLogger() {
        verifyLoggingRules(this::addMessageInFieldLog);
    }

    @Test
    public void shouldLogInFieldWithCustomCategoryLogger() {
        verifyLoggingRules(this::addMessageInFieldWithCustomCategoryLog);
    }

    @Test
    public void shouldCategoryBeWorkingOnlyForAffectedLoggers() {
        // Write messages to several loggers
        addMessageInFieldLog(Logger.Level.DEBUG, MESSAGE + "field1");
        addMessageInFieldWithCustomCategoryLog(Logger.Level.DEBUG, MESSAGE + "category1");

        // As it's DEBUG level, none should be shown
        app.logs().assertDoesNotContain(MESSAGE + "field1");
        app.logs().assertDoesNotContain(MESSAGE + "category1");

        // Let's configure only the log with custom category to show DEBUG messages
        setPropertyTo("quarkus.log.category.\"" + LogResource.CUSTOM_CATEGORY + "\".level", Logger.Level.DEBUG.name());

        // Let's write messages again
        addMessageInFieldLog(Logger.Level.DEBUG, MESSAGE + "field2");
        addMessageInFieldWithCustomCategoryLog(Logger.Level.DEBUG, MESSAGE + "category2");

        // Now, the message should be shown only in the logger of the custom category
        app.logs().assertDoesNotContain(MESSAGE + "field2");
        app.logs().assertContains(MESSAGE + "category2");
    }

    private void verifyLoggingRules(BiConsumer<Logger.Level, String> writer) {
        // Added INFO log
        writer.accept(Logger.Level.INFO, MESSAGE + "1");
        app.logs().assertContains(MESSAGE + "1");

        // By default, DEBUG messages should not be shown
        writer.accept(Logger.Level.DEBUG, MESSAGE + "2");
        app.logs().assertDoesNotContain(MESSAGE + "2");

        // Set level to DEBUG
        setLogLevelTo(Logger.Level.DEBUG);

        // Now, DEBUG messages should be shown
        writer.accept(Logger.Level.DEBUG, MESSAGE + "3");
        app.logs().assertContains(MESSAGE + "3");
    }

    private void addMessageInStaticLog(Logger.Level level, String expectedMessage) {
        app.given().post("/log/static/" + level.name() + "?message=" + expectedMessage);
    }

    private void addMessageInFieldLog(Logger.Level level, String expectedMessage) {
        app.given().post("/log/field/" + level.name() + "?message=" + expectedMessage);
    }

    private void addMessageInFieldWithCustomCategoryLog(Logger.Level level, String expectedMessage) {
        app.given().post("/log/field-with-custom-category/" + level.name() + "?message=" + expectedMessage);
    }

    private void disableJsonFormatLogging() {
        setPropertyTo("quarkus.log.console.json", Boolean.FALSE.toString());
    }

    private void setLogLevelTo(Logger.Level info) {
        setPropertyTo("quarkus.log.level", info.toString());
    }

    private void setPropertyTo(String property, String value) {
        app.stop();
        app.withProperty(property, value);
        app.start();
    }
}
