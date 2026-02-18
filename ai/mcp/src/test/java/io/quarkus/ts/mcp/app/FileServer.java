package io.quarkus.ts.mcp.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkiverse.mcp.server.PromptManager;
import io.quarkiverse.mcp.server.PromptMessage;
import io.quarkiverse.mcp.server.PromptResponse;
import io.quarkiverse.mcp.server.Resource;
import io.quarkiverse.mcp.server.ResourceTemplate;
import io.quarkiverse.mcp.server.Roots;
import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolManager;
import io.quarkiverse.mcp.server.ToolResponse;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class FileServer {
    private static final Logger LOG = Logger.getLogger(FileServer.class);

    private final PromptManager promptManager;
    private final ToolManager toolManager;
    private final String folder;

    public FileServer(ToolManager toolManager,
            PromptManager promptManager,
            @ConfigProperty(name = "working.folder", defaultValue = "target") String folder) {
        this.toolManager = toolManager;
        this.promptManager = promptManager;
        this.folder = folder;
        this.toolManager.newTool("meta")
                .setDescription("Does operations on other tools")
                .addArgument("type", "what type of entity should it operate on", true, String.class)
                .addArgument("operation", "what should it do", true, String.class)
                .addArgument("name", "what entity should it change", true, String.class)
                .setHandler(args -> {
                    String type = args.args().get("type").toString();
                    String operation = args.args().get("operation").toString();
                    String name = args.args().get("name").toString();

                    return switch (operation) {
                        case "create" -> {
                            switch (type) {
                                case "tool" -> createTool(name);
                                case "prompt" -> createPrompt(name);
                            }
                            yield ToolResponse.success("Created new %s: %s".formatted(type, name));
                        }
                        case "delete" -> {
                            switch (type) {
                                case "tool" -> deleteTool(name);
                                case "prompt" -> deletePrompt(name);
                            }
                            yield ToolResponse.success("Deleted a %s %s".formatted(type, name));
                        }
                        default -> ToolResponse.error("Invalid operation: " + operation);
                    };
                })
                .register();
        this.promptManager.newPrompt("meta")
                .setDescription("Does operations on other tools")
                .addArgument("operation", "what should it do", true, "create")
                .addArgument("name", "what tool should it change", true)
                .setHandler(args -> PromptResponse.withMessages(PromptMessage.withUserRole(
                        new TextContent("This tool will %s a new tool named '%s'".formatted(
                                args.args().get("operation"),
                                args.args().get("name"))))))
                .register();
    }

    public void createTool(String name) {
        toolManager.newTool(name)
                .setDescription("Greets people")
                .addArgument("name", "Name of person to greet", true, String.class)
                .setHandler(args -> ToolResponse.success(
                        "Greetings, %s!".formatted(args.args().get("name"))))
                .register();
    }

    private void createPrompt(String name) {
        promptManager.newPrompt(name)
                .setDescription("Greets people")
                .addArgument("name", "Name of person to greet", true)
                .setHandler(args -> PromptResponse.withMessages(PromptMessage.withUserRole(
                        new TextContent("This tool will greet a person named '%s'".formatted(
                                args.args().get("name"))))))
                .register();
    }

    private void deleteTool(String name) {
        toolManager.removeTool(name);
    }

    private void deletePrompt(String name) {
        promptManager.removePrompt(name);
    }

    @Tool(name = "filereader", description = "Read a file")
    String readFileContent(@ToolArg(description = "path to the file") String file) {
        Path allowed = Path.of(folder).toAbsolutePath();
        Path path = Path.of(file).toAbsolutePath();
        if (!path.startsWith(allowed)) {
            throw new IllegalArgumentException(String.format("The file %s is not inside allowed folder %s", path, allowed));
        }
        try (Stream<String> lines = Files.lines(path)) {
            LOG.info("Reading file: " + path);
            return lines.collect(StringBuilder::new,
                    StringBuilder::append,
                    StringBuilder::append).toString();
        } catch (IOException e) {
            throw new RuntimeException("Error reading file " + path + ": " + e.getMessage());
        }
    }

    @Resource(name = "fileresource", description = "Read a file", uri = "file:///hello")
    String readHelloFile() {
        Path path = Path.of("robot-readable.txt").toAbsolutePath();
        try (Stream<String> lines = Files.lines(path)) {
            LOG.info("Reading file as resource: " + path);
            return lines.collect(StringBuilder::new,
                    StringBuilder::append,
                    StringBuilder::append).toString();
        } catch (IOException e) {
            throw new RuntimeException("Error reading file " + path + ": " + e.getMessage());
        }
    }

    @ResourceTemplate(name = "fileresource", description = "Read a file", uriTemplate = "file:///{name}")
    String readResourceFile(String name) {
        Path path = Path.of(name).toAbsolutePath();
        try (Stream<String> lines = Files.lines(path)) {
            LOG.info("Reading file as resource template: " + path);
            return lines.collect(StringBuilder::new,
                    StringBuilder::append,
                    StringBuilder::append).toString();
        } catch (IOException e) {
            throw new RuntimeException("Error reading file " + path + ": " + e.getMessage());
        }
    }

    @Tool(name = "rootchecker", description = "Get client roots")
    Uni<String> getRoots(Roots roots) {
        if (!roots.isSupported()) {
            return Uni.createFrom().item("Roots not supported");
        }

        return roots.list().map(list -> {
            if (list.isEmpty()) {
                return "No roots found";
            }
            return list.stream().map(root -> "%s (%s)".formatted(root.name(), root.uri())).collect(Collectors.joining(", "));
        });
    }
}
