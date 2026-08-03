package io.quarkus.ts.http.minimum.reactive;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.minimum.reactive.cancelling.SlowEndpoint;
import io.restassured.response.Response;

@QuarkusScenario
public class CancelIT {
    private static final int REQUEST_COUNT = 20;

    @QuarkusApplication(classes = SlowEndpoint.class)
    static RestService app = new RestService()
            .withProperty("quarkus.rest.output-buffer-size", "1024");

    @Test
    @Tag("QUARKUS-8419")
    public void httpServer() throws URISyntaxException, InterruptedException {
        Response response = app.given().get("/api/slow");
        assertEquals(HttpStatus.SC_OK, response.statusCode());
        assertEquals("Heeelloooo", response.body().asString());

        URI uri = new URI(app.getURI(Protocol.HTTP).withPath("/api/slow").toString());
        HttpClient client = HttpClient.newHttpClient();
        ArrayList<CompletableFuture<HttpResponse<String>>> futures = new ArrayList<>(REQUEST_COUNT);
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        for (int i = 0; i < REQUEST_COUNT; i++) {
            CompletableFuture<HttpResponse<String>> future = client.sendAsync(request,
                    HttpResponse.BodyHandlers.ofString());
            futures.add(future);
        }

        Thread.sleep(1000);

        for (CompletableFuture<HttpResponse<String>> future : futures) {
            future.cancel(true);
        }

        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            Response count = app.given().get("/api/slow/count");
            assertEquals(HttpStatus.SC_OK, count.statusCode());
            assertEquals(String.valueOf(REQUEST_COUNT + 1), count.body().asString());
        });
    }
}
