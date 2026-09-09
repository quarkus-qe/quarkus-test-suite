package io.quarkus.ts.mcp.app;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkiverse.langchain4j.mcp.runtime.McpClientName;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolExecutionResult;

@Path("/mcp-minimal")
@Produces(MediaType.TEXT_PLAIN)
public class MinimalEndpoint {
    @Inject
    @McpClientName("minimal")
    McpClient filesystemClient;

    @GET
    @Path("/tools")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> tools() {
        return filesystemClient.listTools().stream()
                .map(ToolSpecification::name)
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
}
