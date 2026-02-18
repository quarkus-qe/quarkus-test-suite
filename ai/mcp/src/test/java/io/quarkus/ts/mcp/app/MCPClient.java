package io.quarkus.ts.mcp.app;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkiverse.langchain4j.mcp.runtime.McpClientName;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.McpBlobResourceContents;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.McpPrompt;
import dev.langchain4j.mcp.client.McpPromptContent;
import dev.langchain4j.mcp.client.McpReadResourceResult;
import dev.langchain4j.mcp.client.McpResourceContents;
import dev.langchain4j.mcp.client.McpRoot;
import dev.langchain4j.mcp.client.McpTextContent;
import dev.langchain4j.mcp.client.McpTextResourceContents;
import dev.langchain4j.service.tool.ToolExecutionResult;

@Path("/mcp")
@Produces(MediaType.TEXT_PLAIN)
public class MCPClient {
    @Inject
    @McpClientName("filesystem")
    McpClient filesystemClient;

    @GET
    @Path("/tools")
    public List<String> tools() {
        return filesystemClient.listTools().stream()
                .map(ToolSpecification::name)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/tools/{name}/arguments")
    public List<String> arguments(@PathParam("name") String name) {
        return filesystemClient.listTools().stream()
                .filter(spec -> spec.name().equals(name))
                .map(ToolSpecification::parameters)
                .map(jsonObjectSchema -> jsonObjectSchema.properties().keySet().toString())
                .collect(Collectors.toList());
    }

    @POST
    @Path("/tools/readFile")
    public String readFile(String file) {
        ToolExecutionResult result = filesystemClient.executeTool(ToolExecutionRequest.builder()
                .name("filereader")
                .arguments("""
                        {"file":"%s"}
                        """.formatted(file))
                .build());
        return result.resultText();
    }

    @GET
    @Path("/resources/readFile")
    public String readFile() {
        McpReadResourceResult result = filesystemClient.readResource("file:///hello");
        McpResourceContents mcpResourceContents = result.contents().get(0);
        switch (mcpResourceContents.type()) {
            case TEXT -> {
                McpTextResourceContents contents = (McpTextResourceContents) mcpResourceContents;
                return contents.text();
            }
            case BLOB -> {
                McpBlobResourceContents contents = (McpBlobResourceContents) mcpResourceContents;
                return contents.blob();
            }
            default -> throw new IllegalStateException("Unexpected value: " + mcpResourceContents.type());
        }
    }

    @GET
    @Path("/resources/readFile/{name}")
    public String readResourceFile(@PathParam("name") String name) {
        McpReadResourceResult result = filesystemClient.readResource("file:///" + name);
        McpResourceContents mcpResourceContents = result.contents().get(0);
        switch (mcpResourceContents.type()) {
            case TEXT -> {
                McpTextResourceContents contents = (McpTextResourceContents) mcpResourceContents;
                return contents.text();
            }
            case BLOB -> {
                McpBlobResourceContents contents = (McpBlobResourceContents) mcpResourceContents;
                return contents.blob();
            }
            default -> throw new IllegalStateException("Unexpected value: " + mcpResourceContents.type());
        }
    }

    @POST
    @Path("/meta/{name}")
    public String createTool(@PathParam("name") String name) {
        return runMetaOperation(name, "create").resultText();
    }

    @DELETE
    @Path("/meta/{name}")
    public String deleteTool(@PathParam("name") String name) {
        return runMetaOperation(name, "delete").resultText();
    }

    private ToolExecutionResult runMetaOperation(String name, Object operation) {
        return runMetaOperation("tool", name, operation);
    }

    private ToolExecutionResult runMetaOperation(String entity, String name, Object operation) {
        return filesystemClient.executeTool(ToolExecutionRequest.builder()
                .name("meta")
                .arguments("""
                        {"type":"%s",
                        "name":"%s",
                        "operation":"%s"}
                        """.formatted(entity, name, operation))
                .build());
    }

    @GET
    @Path("/prompt")
    public Response getPrompt() {
        McpPromptContent content = filesystemClient.getPrompt("meta", Map.of("type", "tool",
                "name", "hammer",
                "operation", "create"))
                .messages().get(0).content();
        if (content.type().equals(McpPromptContent.Type.TEXT)) {
            McpTextContent textContent = (McpTextContent) content;
            return Response.ok(textContent.text()).build();
        } else {
            return Response.status(Response.Status.EXPECTATION_FAILED.getStatusCode(),
                    "Prompt returned %s instead of text".formatted(content.getClass()))
                    .build();
        }
    }

    @GET
    @Path("/prompts")
    public List<String> getPrompts() {
        return filesystemClient.listPrompts().stream()
                .map(McpPrompt::name)
                .collect(Collectors.toList());
    }

    @POST
    @Path("/meta/prompt/{name}")
    public String createPrompt(@PathParam("name") String name) {
        return runMetaOperation("prompt", name, "create").resultText();
    }

    @DELETE
    @Path("/meta/prompt/{name}")
    public String deletePrompt(@PathParam("name") String name) {
        return runMetaOperation("prompt", name, "delete").resultText();
    }

    @GET
    @Path("/roots")
    public String roots() {
        ToolExecutionResult result = filesystemClient.executeTool(ToolExecutionRequest.builder()
                .name("rootchecker")
                .build());
        return result.resultText();
    }

    @POST
    @Path("/roots")
    public void addRoot() {
        filesystemClient.setRoots(List.of(new McpRoot("first", "example.com")));
    }

}
