package io.quarkus.ts.mcp.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkiverse.mcp.server.FeatureManager;
import io.quarkiverse.mcp.server.Icon;
import io.quarkiverse.mcp.server.Icons;
import io.quarkiverse.mcp.server.IconsProvider;
import io.quarkiverse.mcp.server.McpLog;
import io.quarkiverse.mcp.server.Progress;
import io.quarkiverse.mcp.server.ProgressTracker;
import io.quarkiverse.mcp.server.PromptManager;
import io.quarkiverse.mcp.server.PromptMessage;
import io.quarkiverse.mcp.server.PromptResponse;
import io.quarkiverse.mcp.server.Resource;
import io.quarkiverse.mcp.server.ResourceManager;
import io.quarkiverse.mcp.server.ResourceResponse;
import io.quarkiverse.mcp.server.ResourceTemplate;
import io.quarkiverse.mcp.server.Roots;
import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.TextResourceContents;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.ToolManager;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkus.runtime.Startup;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
@Startup
public class FileServer {
    private static final Logger LOG = Logger.getLogger(FileServer.class);
    public static final String DYNAMIC_FILE = "file:///number";

    private final PromptManager promptManager;
    private final ToolManager toolManager;
    private final String folder;
    private int number = 1;

    public FileServer(ToolManager toolManager,
            PromptManager promptManager,
            ResourceManager resourceManager,
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
                        case "update" -> {
                            if (!type.equals("resource") || !name.equals(DYNAMIC_FILE)) {
                                yield ToolResponse.error("Update is implemented only for " + DYNAMIC_FILE);
                            }
                            number += 1;
                            resourceManager.getResource(DYNAMIC_FILE).sendUpdateAndForget();
                            yield ToolResponse.success("Updated %s %s".formatted(type, name));
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
        resourceManager.newResource(DYNAMIC_FILE)
                .setDescription("A number")
                .setUri(DYNAMIC_FILE)
                .setHandler(args -> new ResourceResponse(TextResourceContents.create(
                        args.requestUri().value(),
                        String.valueOf(this.number))))
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
        Path path = resolveFilePath(file);
        try (Stream<String> lines = Files.lines(path)) {
            LOG.info("Reading file: " + path);
            return lines.collect(StringBuilder::new,
                    StringBuilder::append,
                    StringBuilder::append).toString();
        } catch (IOException e) {
            throw new RuntimeException("Error reading file " + path + ": " + e.getMessage());
        }
    }

    private Path resolveFilePath(String file) {
        Path workingFolder = Path.of(folder).toAbsolutePath();
        Path path = Path.of(file);
        if (path.getNameCount() == 1) {
            path = workingFolder.resolve(path);
        } else if (!path.startsWith(workingFolder)) {
            throw new IllegalArgumentException(
                    String.format("The file %s is not inside working folder %s", path, workingFolder));
        }
        return path.toAbsolutePath();
    }

    @Resource(name = "fileresource", description = "Read a file", uri = "file:///hello")
    String readHelloFile() {
        Path path = resolveFilePath("robot-readable.txt");
        try (Stream<String> lines = Files.lines(path)) {
            LOG.infof("Reading file as a resource: %s", path);
            return lines.collect(StringBuilder::new,
                    StringBuilder::append,
                    StringBuilder::append).toString();
        } catch (IOException e) {
            throw new RuntimeException("Error reading file " + path + ": " + e.getMessage());
        }
    }

    @ResourceTemplate(name = "fileresource", description = "Read a file", uriTemplate = "file:///{name}")
    String readResourceFile(String name, McpLog log) {
        Path path = resolveFilePath(name);
        try (Stream<String> lines = Files.lines(path)) {
            log.info("Reading provided file: %s", path);
            return lines.collect(StringBuilder::new,
                    StringBuilder::append,
                    StringBuilder::append).toString();
        } catch (IOException e) {
            throw new RuntimeException("Error reading file " + path + ": " + e.getMessage());
        }
    }

    @Tool(name = "wait", description = "Long-running operation with progress")
    Uni<String> longOperation(String input, boolean autoLog, Progress progress) {
        if (progress.token().isEmpty()) {
            return Uni.createFrom().item("Progress tracking not available");
        }
        ProgressTracker tracker = progress.trackerBuilder()
                .setTotal(3)
                .setDefaultStep(1)
                .setMessageBuilder(current -> "Processing item #" + current)
                .build();
        return Uni.createFrom().item("ok")
                .onItem().transform(item -> {
                    String result = input + ": ";
                    for (int i = 1; i <= 3; i++) {
                        // Report progress
                        if (autoLog) {
                            tracker.advanceAndForget();
                        } else {
                            progress.notificationBuilder()
                                    .setProgress(i)
                                    .setTotal(3)
                                    .setMessage("Processing step " + i + " of 3")
                                    .build()
                                    .sendAndForget();
                        }
                        // Perform some work
                        result += processStep(i);
                    }
                    return result;
                });
    }

    private String processStep(int step) {
        // Simulate work
        try {
            Thread.sleep(1_000);
            return String.valueOf(step);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    @Icons(IconProvider.class)
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

    @ApplicationScoped
    public static class IconProvider implements IconsProvider {
        @Override
        public List<Icon> get(FeatureManager.FeatureInfo feature) {
            return List.of(new Icon("file://quarkus_icon_black.svg", "image/svg"));
        }
    }
}
