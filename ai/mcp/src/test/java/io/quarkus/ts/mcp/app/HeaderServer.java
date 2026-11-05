package io.quarkus.ts.mcp.app;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.http.McpParamHeader;

/**
 * A tool, which requires the client to mirror one of its arguments as an HTTP header (only for the HTTP transport).
 * The server rejects the call, if the header is missing or does not match the argument.
 */
@ApplicationScoped
public class HeaderServer {

    @Tool(name = "region", description = "Routes the value to a region")
    String region(@McpParamHeader("Region") @ToolArg(description = "the region") String region,
            @ToolArg(description = "the value") String value) {
        return region + "/" + value;
    }
}
