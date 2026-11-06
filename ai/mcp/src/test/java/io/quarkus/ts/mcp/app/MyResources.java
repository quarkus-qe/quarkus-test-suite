package io.quarkus.ts.mcp.app;

import jakarta.inject.Inject;

import io.quarkiverse.mcp.server.CacheControl;
import io.quarkiverse.mcp.server.CacheScope;
import io.quarkiverse.mcp.server.ResourceManager;
import io.quarkiverse.mcp.server.ResourceResponse;
import io.quarkiverse.mcp.server.TextResourceContents;
import io.quarkus.runtime.Startup;

public class MyResources {

    @Inject
    ResourceManager resourceManager;

    @Startup
    public void addResource() {
        resourceManager
                .newResource("file:///separate")
                .setUri("file:///separate")
                .setDescription("File from a separate class")
                // The newer-protocol tests assert these non-default TTL and scope values.
                .setCacheControl(new CacheControl(5000, CacheScope.PRIVATE))
                .setHandler(
                        args -> new ResourceResponse(TextResourceContents.create(
                                args.requestUri().value(),
                                "Hello from other resource!")))
                .register();
    }
}
