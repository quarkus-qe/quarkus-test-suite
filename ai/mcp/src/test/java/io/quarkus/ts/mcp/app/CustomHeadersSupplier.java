package io.quarkus.ts.mcp.app;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import dev.langchain4j.mcp.client.McpCallContext;
import dev.langchain4j.mcp.client.McpHeadersSupplier;

@ApplicationScoped
public class CustomHeadersSupplier implements McpHeadersSupplier {
    public static final String HEADER = "X-Supplied";
    public static final String VALUE = "supplied-value";

    @Override
    public Map<String, String> apply(McpCallContext context) {
        return Map.of(HEADER, VALUE);
    }
}
