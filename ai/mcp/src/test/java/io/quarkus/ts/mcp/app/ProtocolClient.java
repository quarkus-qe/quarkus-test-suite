package io.quarkus.ts.mcp.app;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import io.quarkiverse.langchain4j.mcp.runtime.McpClientName;
import io.quarkiverse.langchain4j.mcp.runtime.McpResourceUpdatedEvent;
import io.quarkus.arc.ClientProxy;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.McpPrompt;
import dev.langchain4j.mcp.client.McpResource;
import dev.langchain4j.mcp.client.McpRoot;
import dev.langchain4j.service.tool.ToolExecutionResult;

/**
 * Exposes operations of any configured MCP client, selected by its name, so tests can check behavior which depends on the
 * negotiated protocol revision.
 */
@ApplicationScoped
@Path("/protocol/{client}")
@Produces(MediaType.TEXT_PLAIN)
public class ProtocolClient {
    private static final Logger LOG = Logger.getLogger(ProtocolClient.class);
    private static final String DYNAMIC_FILE = "file:///number";

    private final List<String> resourceUpdates = new CopyOnWriteArrayList<>();

    @Inject
    @Any
    Instance<McpClient> clients;

    @GET
    @Path("/modern")
    public boolean modern(@PathParam("client") String client) {
        return ((DefaultMcpClient) ClientProxy.unwrap(client(client))).isModernProtocol();
    }

    @GET
    @Path("/tools")
    public String tools(@PathParam("client") String client) {
        return client(client).listTools().stream()
                .map(ToolSpecification::name)
                .collect(Collectors.joining(","));
    }

    @POST
    @Path("/tools/{tool}")
    public String executeTool(@PathParam("client") String client, @PathParam("tool") String tool, String arguments) {
        ToolExecutionResult result = client(client).executeTool(ToolExecutionRequest.builder()
                .name(tool)
                .arguments(arguments)
                .build());
        return result.isError() ? "Error: " + result.resultText() : result.resultText();
    }

    @GET
    @Path("/resources")
    public String resources(@PathParam("client") String client) {
        return client(client).listResources().stream()
                .map(McpResource::uri)
                .collect(Collectors.joining(","));
    }

    @GET
    @Path("/prompts")
    public String prompts(@PathParam("client") String client) {
        return client(client).listPrompts().stream()
                .map(McpPrompt::name)
                .collect(Collectors.joining(","));
    }

    @POST
    @Path("/subscriptions")
    public long subscribe(@PathParam("client") String client, String uris) {
        return client(client).subscribeToResources(Arrays.asList(uris.split(",")));
    }

    @DELETE
    @Path("/subscriptions/{id}")
    public void unsubscribe(@PathParam("client") String client, @PathParam("id") long id) {
        client(client).unsubscribeFromResources(id);
    }

    @GET
    @Path("/updates")
    public String updates(@PathParam("client") String client) {
        return resourceUpdates.stream()
                .filter(update -> update.startsWith(client + ":"))
                .map(update -> update.substring(client.length() + 1))
                .collect(Collectors.joining(","));
    }

    @DELETE
    @Path("/updates")
    public void clearUpdates(@PathParam("client") String client) {
        resourceUpdates.removeIf(update -> update.startsWith(client + ":"));
    }

    @POST
    @Path("/deprecated/{operation}")
    @SuppressWarnings("deprecation")
    public void deprecated(@PathParam("client") String client, @PathParam("operation") String operation) {
        McpClient mcpClient = client(client);
        switch (operation) {
            case "subscribeToResource" -> mcpClient.subscribeToResource(DYNAMIC_FILE);
            case "unsubscribeFromResource" -> mcpClient.unsubscribeFromResource(DYNAMIC_FILE);
            case "setRoots" -> mcpClient.setRoots(List.of(new McpRoot("first", "example.com")));
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }

    void onResourceUpdated(@Observes @Any McpResourceUpdatedEvent event) {
        resourceUpdates.add(event.mcpClientKey() + ":" + event.uri());
    }

    @ServerExceptionMapper
    public Response failure(Exception exception) {
        LOG.error("MCP client operation failed", exception);
        StringBuilder messages = new StringBuilder();
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            messages.append(cause.getClass().getName()).append(": ").append(cause.getMessage()).append('\n');
        }
        return Response.serverError().entity(messages.toString()).type(MediaType.TEXT_PLAIN).build();
    }

    private McpClient client(String name) {
        return clients.select(McpClientName.Literal.of(name)).get();
    }
}
