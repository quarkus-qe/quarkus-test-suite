package io.quarkus.ts.mcp.app;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.mcp.server.Cancellation;
import io.quarkiverse.mcp.server.Roots;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;

/**
 * Tools used to verify client behavior that depends on the MCP protocol revision: timeouts with cancellation and
 * multi round-trip requests (2026-07-28).
 */
@ApplicationScoped
public class ProtocolServer {
    public static final String IDLE = "idle";
    public static final String RUNNING = "running";
    public static final String CANCELLED = "cancelled";
    public static final String COMPLETED = "completed";

    private final AtomicReference<String> slowStatus = new AtomicReference<>(IDLE);
    private final AtomicInteger mrtrForeverRetries = new AtomicInteger();

    @Tool(name = "slow", description = "Works for a given number of seconds, unless the client cancels the call")
    String slow(@ToolArg(description = "how long to work") int seconds, Cancellation cancellation)
            throws InterruptedException {
        slowStatus.set(RUNNING);
        long deadline = System.nanoTime() + Duration.ofSeconds(seconds).toNanos();
        while (System.nanoTime() < deadline) {
            if (cancellation.check().isRequested()) {
                slowStatus.set(CANCELLED);
                return CANCELLED;
            }
            Thread.sleep(100);
        }
        slowStatus.set(COMPLETED);
        return COMPLETED;
    }

    @Tool(name = "slow-status", description = "Reports how the last call of the 'slow' tool ended")
    String slowStatus() {
        return slowStatus.get();
    }

    @Tool(name = "slow-reset", description = "Resets the status of the 'slow' tool")
    String slowReset() {
        slowStatus.set(IDLE);
        return IDLE;
    }

    @Tool(name = "mrtr-state", description = "Asks the client to retry with a request state")
    String mrtrState(Roots roots) {
        if (roots.requestState() == null) {
            throw roots.inputRequired().setRequestState("first-round").build();
        }
        return "Completed with state " + roots.requestState();
    }

    @Tool(name = "mrtr-input", description = "Asks the client for input it cannot provide")
    String mrtrInput(Roots roots) {
        throw roots.inputRequired().addRootsRequest("roots").build();
    }

    @Tool(name = "mrtr-forever", description = "Never completes the multi round-trip request")
    String mrtrForever(Roots roots) {
        // the request state counts the retries of the client
        int retries = roots.requestState() == null ? 0 : Integer.parseInt(roots.requestState());
        mrtrForeverRetries.set(retries);
        throw roots.inputRequired().setRequestState(String.valueOf(retries + 1)).build();
    }

    @Tool(name = "mrtr-forever-retries", description = "Reports how often the client retried the last 'mrtr-forever' call")
    String mrtrForeverRetries() {
        return String.valueOf(mrtrForeverRetries.get());
    }
}
