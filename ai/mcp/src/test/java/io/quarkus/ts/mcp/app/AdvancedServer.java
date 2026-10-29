package io.quarkus.ts.mcp.app;

import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.mcp.server.CompleteArg;
import io.quarkiverse.mcp.server.CompletePrompt;
import io.quarkiverse.mcp.server.Elicitation;
import io.quarkiverse.mcp.server.ElicitationRequest;
import io.quarkiverse.mcp.server.ElicitationResponse;
import io.quarkiverse.mcp.server.JsonRpcErrorCodes;
import io.quarkiverse.mcp.server.McpException;
import io.quarkiverse.mcp.server.Prompt;
import io.quarkiverse.mcp.server.PromptArg;
import io.quarkiverse.mcp.server.PromptMessage;
import io.quarkiverse.mcp.server.Sampling;
import io.quarkiverse.mcp.server.SamplingMessage;
import io.quarkiverse.mcp.server.SamplingRequest;
import io.quarkiverse.mcp.server.SamplingResponse;
import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
@Startup
public class AdvancedServer {
    @Tool(description = "Ask the AI asynchronously")
    Uni<String> sampled(String question, Sampling sampling) {
        if (!sampling.isSupported() && sampling.isServerInitiatedRequestSupported()) {
            return Uni.createFrom().item("Sampling not supported");
        }

        SamplingRequest request = sampling.requestBuilder()
                .setMaxTokens(100)
                .addMessage(SamplingMessage.withUserRole("An African or European swallow?"))
                .build();
        if (!sampling.isServerInitiatedRequestSupported()) {
            if (!sampling.inputResponses().has("answer")) {
                throw sampling.inputRequired().addSamplingRequest("answer", request)
                        .setRequestState("qe-sampling-state").build();
            }
            if (!"qe-sampling-state".equals(sampling.requestState())) {
                throw new McpException("Unexpected MRTR request state",
                        JsonRpcErrorCodes.INVALID_PARAMS);
            }
            SamplingResponse response = sampling.inputResponses().getSamplingResponse("answer");
            return Uni.createFrom().item("The answer to \"%s\" is \"%s\""
                    .formatted(question, response.content().asText().text()));
        }
        return request.send().map(response -> "The answer to \"%s\" is \"%s\""
                .formatted(question,
                        response.content().asText().text()));
    }

    @Tool(description = "Ask the AI asynchronously")
    Uni<String> unsampled(String question) {
        return Uni.createFrom().item("Answer to \"%s\" is 42".formatted(question));
    }

    @Tool(description = "Greet user")
    String elicitation(Elicitation elicitation) {
        if (!elicitation.isFormModeSupported() && elicitation.isServerInitiatedRequestSupported()) {
            return "Elicitation not supported";
        }

        ElicitationRequest request = elicitation.requestBuilder()
                .setMessage("Please provide your information:")
                .addSchemaProperty("name", new ElicitationRequest.StringSchema(true))
                .build();

        ElicitationResponse response;
        if (elicitation.isServerInitiatedRequestSupported()) {
            response = request.sendAndAwait();
        } else {
            if (!elicitation.inputResponses().has("userInput")) {
                throw elicitation.inputRequired().addElicitationRequest("userInput", request)
                        .setRequestState("qe-form-state").build();
            }
            if (!"qe-form-state".equals(elicitation.requestState())) {
                throw new McpException("Unexpected MRTR request state",
                        JsonRpcErrorCodes.INVALID_PARAMS);
            }
            response = elicitation.inputResponses().getElicitationResponse("userInput");
        }

        if (response.actionAccepted()) {
            String name = response.content().getString("name");
            return "Hello " + name + "!";
        } else {
            return "User cancelled the request";
        }
    }

    @Prompt(description = "Food ordering")
    PromptMessage foodOrder(@PromptArg(description = "the food") String food) {
        return PromptMessage.withUserRole(new TextContent("Here is your %s!".formatted(food)));
    }

    @CompletePrompt("foodOrder")
    List<String> completeName(@CompleteArg(name = "food") String food) {
        return Stream.of("broccoli", "pizza", "salad", "pie")
                .filter(n -> n.startsWith(food))
                .toList();
    }
}
