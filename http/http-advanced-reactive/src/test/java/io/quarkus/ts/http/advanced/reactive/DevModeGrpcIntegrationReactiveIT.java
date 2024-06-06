package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import io.quarkus.example.GreeterGrpc;
import io.quarkus.example.HelloReply;
import io.quarkus.example.HelloRequest;
import io.quarkus.test.bootstrap.GrpcService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.DevModeQuarkusApplication;
import io.quarkus.test.services.URILike;

@Tag("QUARKUS-1026")
@Tag("QUARKUS-1094")
@QuarkusScenario
public class DevModeGrpcIntegrationReactiveIT {

    private static final String NAME = "QE";

    /**
     * Expect streaming service and hello service definition from 'helloworld.proto'
     * as well as full generated service names and communication method type (UNARY, CLIENT_STREAMING, ...).
     */
    private static final String[] GRPC_SERVICE_VIEW_EXPECTED_CONTENT = {
            "UNARY", "helloworld.Greeter", "io.quarkus.ts.http.advanced.reactive.GrpcService", "SayHello", "SERVER_STREAMING",
            "io.quarkus.ts.http.advanced.reactive.GrpcStreamingService", "BIDI_STREAMING", "CLIENT_STREAMING", "ServerStream",
            "BidirectionalStream", "ClientStream"
    };

    @DevModeQuarkusApplication(grpc = true)
    static final GrpcService app = (GrpcService) new GrpcService() {
        @Override
        public URILike getGrpcHost() {
            // TODO: make app.grpcChannel() support gRPC on same HTTP server
            return super.getGrpcHost().withPort(app.getURI().getPort());
        }
    }
            .withProperty("quarkus.oidc.enabled", "false")
            .withProperty("quarkus.keycloak.policy-enforcer.enable", "false")
            .withProperty("quarkus.keycloak.devservices.enabled", "false");

    @Test
    public void testGrpcAsClient() throws ExecutionException, InterruptedException {
        HelloRequest request = HelloRequest.newBuilder().setName(NAME).build();
        HelloReply response = GreeterGrpc.newFutureStub(app.grpcChannel()).sayHello(request).get();

        assertEquals("Hello " + NAME, response.getMessage());
    }

    @Test
    public void testGrpcViaRest() {
        app.given().when().get("/api/grpc/trinity").then().statusCode(HttpStatus.SC_OK).body(is("Hello trinity"));
    }

    @Test
    public void testGrpcDevUISocket() {
        // TODO: reimplement as part of the https://github.com/quarkus-qe/quarkus-test-suite/issues/1832
    }

    @Test
    public void testGrpcDevUIServicesView() {
        assertOnGrpcServicePage(page -> {
            var grpcSvcView = page.waitForSelector("#page > qwc-grpc-services > vaadin-grid").innerText();
            for (String text : GRPC_SERVICE_VIEW_EXPECTED_CONTENT) {
                assertTrue(grpcSvcView.contains(text), "DevUI gRPC services view is incomplete: " + grpcSvcView);
            }
        });
    }

    private static void assertOnGrpcServicePage(Consumer<Page> assertion) {
        try (Playwright playwright = Playwright.create()) {
            try (Browser browser = playwright.chromium().launch()) {
                Page page = browser.newContext().newPage();
                var grpcServicesViewUrl = app
                        .getURI(Protocol.HTTP)
                        .withPath("/q/dev-ui/io.quarkus.quarkus-grpc/services")
                        .toString();
                page.navigate(grpcServicesViewUrl);

                untilAsserted(() -> assertion.accept(page));
            }
        }
    }

}
