package io.quarkus.ts.langchain4j.auxiliary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;

public class MCPFileServer {
    private static final Logger LOG = Logger.getLogger(MCPFileServer.class);

    String folder;

    public MCPFileServer(@ConfigProperty(name = "working.folder", defaultValue = "target") String folder) {
        this.folder = folder;
    }

    @Tool(description = "Read a file")
    String readFileContent(@ToolArg(description = "path to the file") String file) {
        Path allowed = Path.of(folder).toAbsolutePath();
        Path path = Path.of(file);
        if (!path.isAbsolute()) { // we assume, that it is relative to working folder
            if (path.getNameCount() == 2 && allowed.endsWith(path.getName(0))) {
                // Special case, where working folder is applied before file name
                // (eg working folder is /tmp/playground and file is playground/file.txt)
                path = path.getName(1);
            }
            path = allowed.resolve(path).normalize().toAbsolutePath();
        }
        if (!path.startsWith(allowed)) {
            throw new IllegalArgumentException(String.format("The file %s (%s) is not inside allowed folder %s",
                    file,
                    path,
                    allowed));
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
}
