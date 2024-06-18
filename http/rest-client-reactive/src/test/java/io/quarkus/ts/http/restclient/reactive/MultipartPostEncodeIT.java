package io.quarkus.ts.http.restclient.reactive;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

@QuarkusScenario
public class MultipartPostEncodeIT {

    private static final Logger LOGGER = Logger.getLogger(MultipartPostEncodeIT.class);
    private static final String FILE = "file";
    private static final String TEXT = "text";
    private static Vertx vertx;
    private static String capturedRequestBody = "";
    private static CountDownLatch latch;

    @QuarkusApplication(properties = "test.properties")
    static RestService app = new RestService()
            .withProperty("quarkus.rest-client.multipart-post-encoder-mode", "HTML5");

    @BeforeAll
    public static void setUp() {
        latch = new CountDownLatch(1);
        vertx = Vertx.vertx();
        createHttpServer(vertx);
    }

    private static void createHttpServer(Vertx vertx) {
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post("/encode/upload").handler(ctx -> {
            ctx.request().setExpectMultipart(true);
            ctx.request().bodyHandler(buffer -> {
                capturedRequestBody = buffer.toString();
                latch.countDown();
                String contentType = ctx.request().getHeader("Content-Type");
                String filename = "example.txt";

                String expectedContentDisposition;
                if (contentType.contains("multipart/form-data; boundary=")) {
                    expectedContentDisposition = "Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"";
                } else {
                    expectedContentDisposition = "Content-Disposition: form-data; name=\"file\"; filename=" + filename;
                }

                if (capturedRequestBody.contains(expectedContentDisposition)) {
                    ctx.response().setStatusCode(200).end("{\"status\": \"received\"}");
                } else {
                    ctx.response().setStatusCode(400).end("{\"error\": \"Code mode incorrect\"}");
                }
            });
        });
        httpServer.requestHandler(router).listen(8443)
                .onSuccess(server -> LOGGER.info("Vert.x HTTP server started on port 8443"))
                .onFailure(err -> LOGGER.error("Failed to start Vert.x HTTP server", err));

    }

    @AfterAll
    public static void tearDown() {
        vertx.close();
    }

    @ParameterizedTest
    @ValueSource(strings = { "HTML5", "RFC1738", "RFC3986" })
    public void testMultipartEncodeMode(String encoderMode) throws InterruptedException {

        app.given()
                .multiPart(FILE, Paths.get("src", "test", "resources", "example.txt").toFile(), "text/plain")
                .multiPart(TEXT, "This is an example.")
                .when()
                .post("/encode/upload")
                .then()
                .statusCode(200)
                .body("fileContent", is("This is an example."));
        latch.await(10, TimeUnit.SECONDS);
        String filename = "example.txt";
        String expectedContentDisposition;

        if (encoderMode.equals("HTML5")) {
            expectedContentDisposition = "Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"";
        } else {
            expectedContentDisposition = "Content-Disposition: form-data; name=\"file\"; filename=" + filename;
        }

        assertTrue(capturedRequestBody.contains(expectedContentDisposition));
        assertTrue(capturedRequestBody.contains("Content-Type: text/plain"));
    }

}
