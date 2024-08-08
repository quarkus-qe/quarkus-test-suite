package org.acme;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAndGroup2;
import io.smallrye.mutiny.groups.UniMemoize;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logmanager.Level;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;
import java.util.logging.LogRecord;

/**
 * This class is used for "quarkus update" command, to check if methods are renamed and works correctly
 */
@Path("/rename")
public class MethodRenameResource {
    UniMemoize<String> uniMemoize;

    UniAndGroup2<String, String> uniAndGroup;

    LogRecord logRecord;

    @GET
    @Path("/uni/memoize")
    @Produces(MediaType.TEXT_PLAIN)
    public String memoizedResource() throws ExecutionException, InterruptedException {
        uniMemoize = Uni.createFrom().item(() -> "Hello Uni").memoize();
        return uniMemoize.atLeast(Duration.of(2, ChronoUnit.MINUTES)).subscribe().asCompletionStage().get();
    }

    @GET
    @Path("/uni/group")
    @Produces(MediaType.TEXT_PLAIN)
    public String group() {
        Uni<String> uni1 = Uni.createFrom().item(() -> "Hello");
        Uni<String> uni2 = Uni.createFrom().item(() -> "group");

        uniAndGroup = new UniAndGroup2<>(uni1, uni2);
        return uniAndGroup.combinedWith(collection -> collection.get(0) + " " + collection.get(1)).await().indefinitely();
    }

    @GET
    @Path("/thread")
    public String threadId() {
        logRecord = new LogRecord(Level.ERROR, "synthetic error");

        logRecord.setThreadID(42);

        return String.valueOf(logRecord.getThreadID());
    }
}
