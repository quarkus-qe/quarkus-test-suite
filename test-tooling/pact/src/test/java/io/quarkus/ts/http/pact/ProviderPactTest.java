package io.quarkus.ts.http.pact;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.junit.QuarkusTest;

import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;

// TODO: enable test once we migrate to Quarkus Pact extension, which will be possible after Quarkus 3 is released
// I believe nature of the failure is identical to https://github.com/quarkiverse/quarkus-pact/issues/73
// and failure goes down to class loading changes in Quarkus 3
@Disabled("Disabled until Quarkus Pact 3 is released and we migrate pact module to that extension")
@QuarkusTest
@Tag("QUARKUS-1024")
@Provider("TheProvider")
@PactFolder("src/test/resources/pacts")
public class ProviderPactTest {

    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new MessageTestTarget());
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void testTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @PactVerifyProvider("a message with a payload")
    public String verifyMessageForOrder() {
        return "{\"payload\": \"somepayload\"}";
    }
}
