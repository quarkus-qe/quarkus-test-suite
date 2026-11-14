package io.quarkus.ts.mcp.app;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import org.mcpjava.server.Role;
import org.mcpjava.server.content.TextContent;
import org.mcpjava.server.prompts.PromptResponse;

import io.quarkiverse.mcp.server.Elicitation;
import io.quarkiverse.mcp.server.ElicitationRequest;
import io.quarkiverse.mcp.server.ElicitationResponse;
import io.quarkiverse.mcp.server.McpLog;
import io.quarkiverse.mcp.server.Roots;
import io.quarkiverse.mcp.server.Sampling;
import io.quarkiverse.mcp.server.SamplingMessage;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;

/**
 * Both {@code io.quarkiverse.mcp.server} and {@code org.mcpjava.server} annotations in a single
 * application and in a single bean class.
 */
@ApplicationScoped
@Startup
public class McpJavaMixedAnnotationServer {

    @io.quarkiverse.mcp.server.Tool(name = "quarkusGreeter", description = "Greets using the Quarkus annotation")
    String quarkusGreeter(@io.quarkiverse.mcp.server.ToolArg(description = "name") String name) {
        return "Hello (quarkus), " + name + "!";
    }

    @org.mcpjava.server.tools.Tool(name = "standardGreeter", description = "Greets using the standard annotation")
    String standardGreeter(@org.mcpjava.server.tools.ToolArg(name = "name", description = "name") String personName) {
        return "Hello (standard), " + personName + "!";
    }

    @io.quarkiverse.mcp.server.Prompt(name = "quarkusOrder", description = "Quarkus food ordering")
    io.quarkiverse.mcp.server.PromptResponse quarkusOrder(
            @io.quarkiverse.mcp.server.PromptArg(description = "the food") String food) {
        return io.quarkiverse.mcp.server.PromptResponse.withMessages(io.quarkiverse.mcp.server.PromptMessage
                .withUserRole(new io.quarkiverse.mcp.server.TextContent("Here is your %s! (quarkus)".formatted(food))));
    }

    @org.mcpjava.server.prompts.Prompt(name = "standardOrder", description = "Standard food ordering")
    PromptResponse standardOrder(
            @org.mcpjava.server.prompts.PromptArg(name = "food", description = "the food") String food) {
        return PromptResponse.of(Role.USER, TextContent.of("Here is your %s! (standard)".formatted(food)));
    }

    // See AdvancedServer#completeName for the Quarkus equivalent
    @org.mcpjava.server.completion.CompletePrompt("standardOrder")
    List<String> completeStandardOrder(@org.mcpjava.server.completion.CompleteArg(name = "food") String food) {
        return Stream.of("broccoli", "pizza", "salad", "pie").filter(name -> name.startsWith(food)).toList();
    }

    // See AdvancedServer#sampled for the Quarkus equivalent
    @org.mcpjava.server.tools.Tool(name = "standardSampling", description = "Standard tool using the Quarkus-only Sampling")
    Uni<String> standardSampling(Sampling sampling, McpLog log) {
        log.info("standardSampling invoked");
        if (!sampling.isSupported()) {
            return Uni.createFrom().item("Sampling not supported");
        }
        return sampling.requestBuilder()
                .setMaxTokens(50)
                .addMessage(SamplingMessage.withUserRole("ping"))
                .build()
                .send()
                .map(response -> "standard-sampled: " + response.content().asText().text());
    }

    // See FileServer#getRoots for the Quarkus equivalent
    @org.mcpjava.server.tools.Tool(name = "standardRoots", description = "Standard tool using the Quarkus-only Roots")
    Uni<String> standardRoots(Roots roots) {
        if (!roots.isSupported()) {
            return Uni.createFrom().item("Roots not supported");
        }
        return roots.list().map(list -> list.isEmpty() ? "No roots found"
                : list.stream().map(root -> "%s (%s)".formatted(root.name(), root.uri())).collect(Collectors.joining(", ")));
    }

    // See AdvancedServer#elicitation for the Quarkus equivalent
    @org.mcpjava.server.tools.Tool(name = "standardElicitation", description = "Standard tool using the Quarkus-only Elicitation")
    String standardElicitation(Elicitation elicitation) {
        if (!elicitation.isSupported()) {
            return "Elicitation not supported";
        }
        ElicitationRequest request = elicitation.requestBuilder()
                .setMessage("Please provide your information:")
                .addSchemaProperty("name", new ElicitationRequest.StringSchema(true))
                .build();
        ElicitationResponse response = request.sendAndAwait();
        if (response.actionAccepted()) {
            return "standard-elicited: " + response.content().getString("name");
        }
        return "User cancelled the request";
    }
}
