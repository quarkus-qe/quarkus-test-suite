package http.sse;

import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.sse.SseEventSource;

@ApplicationScoped
public class SseClient {

    private Client client;
    private SseEventSource updateSource;

    @PostConstruct
    void init() {
        client = ClientBuilder.newClient();
    }

    @PreDestroy
    void close() {
        client.close();
        if (updateSource != null)
            updateSource.close();
    }

    public String someMethod() {
        StringBuilder response = new StringBuilder();

        WebTarget target = client.target("http://localhost:" + getQuarkusPort() + "/updates");
        updateSource = SseEventSource.target(target).build();
        updateSource.register(ev -> {
            response.append("ev.isEmpty() = ").append(ev.isEmpty());
            response.append("\n");
            response.append("event: ").append(ev.getName()).append(" ").append(ev.readData());
            response.append("\n");

        }, thr -> {
            response.append("Error in SSE updates\n");
            response.append(Arrays.toString(thr.getStackTrace()));
            //            thr.printStackTrace();
        });

        response.append("SSE opened\n");
        updateSource.open();

        //        LockSupport.parkNanos(7_000_000_000L);
        LockSupport.parkNanos(2_000_000_000L);
        return response.toString();
    }

    /**
     * Test runner assigns random ports, on which the app should run.
     * Parse this port from the CLI and return it.
     * If no parameter is specified, return the default (8080)
     *
     * @return port on which the application is running
     */
    private int getQuarkusPort() {
        String value = System.getProperty("quarkus.http.port");
        if (value == null || value.isEmpty()) {
            return 8080;
        }
        return Integer.parseInt(value);
    }
}
