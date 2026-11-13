package io.quarkus.ts.mcp.app;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import org.mcpjava.server.tools.Tool;
import org.mcpjava.server.tools.ToolArg;

import io.quarkus.runtime.Startup;

@ApplicationScoped
@Startup
public class McpJavaArgumentsServer {
    @Tool(name = "renamedArgument", description = "Takes an argument declared under a different name")
    String renamedArgument(@ToolArg(name = "city", description = "city name") String someParameterName) {
        return "city=" + someParameterName;
    }

    @Tool(name = "defaultValueArgument", description = "Takes an argument with a default value")
    String defaultValueArgument(
            @ToolArg(name = "greeting", description = "greeting word", required = false, defaultValue = "Hello") String greeting,
            @ToolArg(name = "name", description = "name of person to greet") String name) {
        return greeting + ", " + name + "!";
    }

    @Tool(name = "optionalArgument", description = "Takes an optional argument")
    String optionalArgument(
            @ToolArg(name = "nickname", description = "optional nickname", required = false) Optional<String> nickname) {
        return nickname.orElse("stranger");
    }
}
