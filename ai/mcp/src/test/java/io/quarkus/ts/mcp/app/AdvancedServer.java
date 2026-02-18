package io.quarkus.ts.mcp.app;

import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.mcp.server.CompleteArg;
import io.quarkiverse.mcp.server.CompletePrompt;
import io.quarkiverse.mcp.server.Elicitation;
import io.quarkiverse.mcp.server.ElicitationRequest;
import io.quarkiverse.mcp.server.ElicitationResponse;
import io.quarkiverse.mcp.server.Prompt;
import io.quarkiverse.mcp.server.PromptArg;
import io.quarkiverse.mcp.server.PromptMessage;
import io.quarkiverse.mcp.server.Sampling;
import io.quarkiverse.mcp.server.SamplingMessage;
import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
@Startup
public class AdvancedServer {
    @Tool(description = "Ask the AI asynchronously")
    Uni<String> sampled(String question, Sampling sampling) {
        if (!sampling.isSupported()) {
            return Uni.createFrom().item("Sampling not supported");
        }

        return sampling.requestBuilder()
                .setMaxTokens(100)
                .addMessage(SamplingMessage.withUserRole("An African or European swallow?"))
                .build()
                .send()
                .map(response -> "The answer to \"%s\" is \"%s\""
                        .formatted(question,
                                response.content().asText().text()));
    }

    @Tool(description = "Ask the AI asynchronously")
    Uni<String> unsampled(String question) {
        return Uni.createFrom().item("Answer to \"%s\" is 42".formatted(question));
    }

    @Tool(description = "Greet user")
    String elicitation(Elicitation elicitation) {
        if (!elicitation.isSupported()) {
            return "Elicitation not supported";
        }

        ElicitationRequest request = elicitation.requestBuilder()
                .setMessage("Please provide your information:")
                .addSchemaProperty("name", new ElicitationRequest.StringSchema(true))
                .build();

        ElicitationResponse response = request.sendAndAwait();

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
