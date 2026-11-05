package io.quarkus.ts.mcp.app;

import java.time.Duration;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;

import dev.langchain4j.mcp.client.McpClient;

/**
 * Closes all MCP clients, so their stdio processes are gone before the test framework stops the application. The
 * framework waits up to a minute for every child process still running at that point.
 */
@Path("/mcp-clients")
public class McpClients {
    private static final Duration PROCESS_EXIT_TIMEOUT = Duration.ofSeconds(10);

    @Inject
    BeanManager beanManager;

    @DELETE
    public void close() throws Exception {
        for (Bean<?> bean : beanManager.getBeans(McpClient.class, Any.Literal.INSTANCE)) {
            // only the clients created by the tests, creating the others would start their processes
            McpClient client = (McpClient) beanManager.getContext(bean.getScope()).get(bean);
            if (client != null) {
                client.close();
            }
        }
        // closing a stdio client only asks its process to stop
        long deadline = System.nanoTime() + PROCESS_EXIT_TIMEOUT.toNanos();
        while (ProcessHandle.current().children().anyMatch(ProcessHandle::isAlive) && System.nanoTime() < deadline) {
            Thread.sleep(100);
        }
    }
}
