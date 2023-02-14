package io.quarkus.ts.funqy.knativeevents;

import static io.quarkus.ts.funqy.knativeevents.Constants.CUSTOM_EVENT_ATTR_NAME;
import static io.quarkus.ts.funqy.knativeevents.Constants.CUSTOM_EVENT_ATTR_VALUE;
import static io.quarkus.ts.funqy.knativeevents.Constants.ENV_VAR_NAME;
import static io.quarkus.ts.funqy.knativeevents.Constants.NOT_MATCHED_NAME;
import static io.quarkus.ts.funqy.knativeevents.Constants.NOT_MATCHED_VALUE;
import static io.quarkus.ts.funqy.knativeevents.Constants.PING_RESPONSE_SOURCE;
import static io.quarkus.ts.funqy.knativeevents.Constants.PING_RESPONSE_TYPE;
import static io.quarkus.ts.funqy.knativeevents.Constants.PING_TRIGGER;
import static io.quarkus.ts.funqy.knativeevents.Constants.ULTIMATE_QUESTION;
import static io.quarkus.ts.funqy.knativeevents.ValidationResult.Functions.ENV_VAR;
import static io.quarkus.ts.funqy.knativeevents.ValidationResult.Functions.FALLBACK;
import static io.quarkus.ts.funqy.knativeevents.ValidationResult.Functions.PENG;
import static io.quarkus.ts.funqy.knativeevents.ValidationResult.Functions.PING;
import static io.quarkus.ts.funqy.knativeevents.ValidationResult.Functions.PONG;
import static io.quarkus.ts.funqy.knativeevents.ValidationResult.Functions.PUNG;
import static java.lang.String.format;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import io.quarkus.funqy.Context;
import io.quarkus.funqy.Funq;
import io.quarkus.funqy.knative.events.CloudEvent;
import io.quarkus.funqy.knative.events.CloudEventBuilder;
import io.quarkus.funqy.knative.events.CloudEventMapping;
import io.quarkus.funqy.knative.events.EventAttribute;
import io.quarkus.test.services.knative.eventing.spi.CompoundResponse;
import io.quarkus.test.services.knative.eventing.spi.ForwardResponseDTO;
import io.smallrye.mutiny.Uni;

@Singleton
public class FunctionValidationChain {
    private static final Logger LOG = Logger.getLogger(FunctionValidationChain.class);
    private final CompoundValidationResult compoundResponse;

    public FunctionValidationChain(ValidationResultRepository repository, @RestClient BrokerClient brokerClient,
            @ConfigProperty(name = ENV_VAR_NAME) String envVar) {
        this.compoundResponse = new CompoundValidationResult(repository, brokerClient);
        this.compoundResponse.recordResponse(new ValidationResult(ENV_VAR, envVar));
    }

    /**
     * Last chain.
     */
    @Funq
    public void peng(CloudEvent<Map<String, String>> event) {
        LOG.info("*** peng ***");
        final var entry = event.data().entrySet().stream().findFirst().orElseThrow();
        final String actualValue = format("%s: %s, %s: %s", entry.getKey(), event.id(), entry.getValue(), event.type());
        compoundResponse.recordResponse(new ValidationResult(PENG, actualValue));
    }

    /**
     * This is triggered by pong and triggers peng.
     */
    @Funq
    public CloudEvent<Map<String, String>> pung(PongResponse pongResponse) {
        LOG.info("*** pung ***");
        compoundResponse.recordResponse(new ValidationResult(PUNG, pongResponse.asString()));
        return CloudEventBuilder
                .create()
                .specVersion("1.0")
                .id("one-two-three")
                .type("peng")
                .build(Map.of("id", "type"));
    }

    /**
     * This is triggered by ping and is example of using application.properties to
     * map the cloud event to this function and to map response. Response will trigger
     * the pung function.
     */
    @Funq("pong")
    public PongResponse pongListenerPrimitivePayload(@Context CloudEvent event, double ignored) {
        LOG.info("*** pong *** ");
        compoundResponse.recordResponse(new ValidationResult(PONG, event.source() + "," + event.type()));
        return new PongResponse(ULTIMATE_QUESTION, 42);
    }

    /**
     * Expect Knative Event of type "ping". Creates event of type "ping-response-type".
     *
     * This function is triggered after PingSource has triggered {@link #fallback(byte[])}.
     */
    @CloudEventMapping(trigger = PING_TRIGGER, responseType = PING_RESPONSE_TYPE, responseSource = PING_RESPONSE_SOURCE, attributes = @EventAttribute(name = CUSTOM_EVENT_ATTR_NAME, value = CUSTOM_EVENT_ATTR_VALUE))
    @Funq
    public double matchedPing(Boolean pingData) {
        LOG.info("*** ping ***");
        compoundResponse.recordResponse(new ValidationResult(PING, pingData));
        return Math.random();
    }

    /**
     * Expect Knative Event of type "ping". This function should never be invoked by broker as attribute filter should
     * choose {@link #matchedPing(Boolean)}. However, it may be used for http requests.
     */
    @CloudEventMapping(trigger = PING_TRIGGER, responseType = PING_RESPONSE_TYPE, responseSource = PING_RESPONSE_SOURCE, attributes = @EventAttribute(name = NOT_MATCHED_NAME, value = NOT_MATCHED_VALUE))
    @Funq
    public double notMatchedPing(Boolean pingData) {
        LOG.info("*** notMatchedPing ***");
        return 1.5;
    }

    /**
     * Triggered when no other function is matched.
     *
     * Knative Eventing PingSource sends a ping message every minute, and we catch it here.
     */
    @Funq
    @CloudEventMapping(trigger = "*")
    public void fallback(byte[] data) {
        // we don't need multiple messages from PingSource (as they are all same)
        if (!compoundResponse.isProcessingFinished()) {
            LOG.info("*** fallback ***");

            // decode response and add validation result
            int i = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).getInt();
            byte[] result = new byte[4];
            ByteBuffer.wrap(result).order(ByteOrder.BIG_ENDIAN).putInt(i * 2);

            // record response and send broker cloud event for ping trigger
            compoundResponse.recordResponse(new ValidationResult(FALLBACK, result));
            compoundResponse.triggerPingFunction();
        }
    }

    /**
     * Collect results from triggerred functions. If results are not ready (functions haven't been triggerred yet),
     * empty result is returned.
     */
    @Funq
    public Uni<ForwardResponseDTO<Set<ValidationResult>>> clusterEntrypoint() {
        LOG.info("*** clusterEntrypoint ***");
        return Uni.createFrom().item(compoundResponse.getAllOrNothing());
    }

    /**
     * Serves for vanilla http request.
     */
    @Funq
    public String toUpperCase(String val) {
        return val.toUpperCase();
    }

    private static final class CompoundValidationResult extends CompoundResponse<Set<ValidationResult>> {

        private static final int EXPECTED_NUMBER_OF_RESPONSES = 6;
        private final ValidationResultRepository repository;
        private final BrokerClient brokerClient;

        private CompoundValidationResult(ValidationResultRepository repository, BrokerClient brokerClient) {
            super(EXPECTED_NUMBER_OF_RESPONSES);
            this.repository = repository;
            this.brokerClient = brokerClient;
        }

        @Override
        protected void addResponse(Set<ValidationResult> response) {
            // ignored
        }

        @Override
        protected Set<ValidationResult> getJoinedResponse() {
            return repository.getAll();
        }

        private ForwardResponseDTO<Set<ValidationResult>> getAllOrNothing() {
            if (isProcessingFinished()) {
                return join();
            }
            return new ForwardResponseDTO<>();
        }

        private boolean isProcessingFinished() {
            return repository.getAll().size() == EXPECTED_NUMBER_OF_RESPONSES;
        }

        private void recordResponse(ValidationResult validationResult) {
            repository.add(validationResult);
            recordVisit();
        }

        private void triggerPingFunction() {
            brokerClient.forwardEventToBroker(PING_TRIGGER, Boolean.TRUE.toString());
        }

    }

    public static class PongResponse {

        private String question;
        private Integer answer;

        public PongResponse(String question, Integer answer) {
            this.question = question;
            this.answer = answer;
        }

        public Integer getAnswer() {
            return answer;
        }

        public void setAnswer(Integer answer) {
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String asString() {
            return format("%s is %d.", question, answer);
        }
    }
}
