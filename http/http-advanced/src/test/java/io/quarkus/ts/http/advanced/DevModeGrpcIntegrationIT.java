package io.quarkus.ts.http.advanced;

import static io.quarkus.test.utils.AwaitilityUtils.untilAsserted;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
public class DevModeGrpcIntegrationIT {

    private static final String NAME = "QE";

    /**
     * Expect streaming service and hello service definition from 'helloworld.proto' and communication
     * method type (UNARY, CLIENT_STREAMING, ...).
     */
    private static final String[] GRPC_SERVICE_VIEW_EXPECTED_CONTENT = {
            "helloworld.InterceptedMessage", "UNARY", "helloworld.Greeter", "SayHello", "SERVER_STREAMING",
            "BIDI_STREAMING", "CLIENT_STREAMING", "ServerStream", "BidirectionalStream", "ClientStream"
    };

    private static final String[] GRPC_SERVICE_IMPLEMENTATION_CLASSES = {
            "io.quarkus.ts.http.advanced.GrpcInterceptorsService", "io.quarkus.ts.http.advanced.GrpcService",
            "io.quarkus.ts.http.advanced.GrpcStreamingService"
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
    public void testGrpcAsClient() {
        HelloRequest request = HelloRequest.newBuilder().setName(NAME).build();
        HelloReply response = GreeterGrpc.newBlockingStub(app.grpcChannel()).sayHello(request);

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
            var grpcSvcViewGrid = page.waitForSelector("#page > qwc-grpc-services > vaadin-grid").innerText();
            for (String text : GRPC_SERVICE_VIEW_EXPECTED_CONTENT) {
                assertTrue(grpcSvcViewGrid.contains(text), "DevUI gRPC services view is incomplete: " + grpcSvcViewGrid);
            }
            // search for gRPC service implementation classes differently as they are in a shadow root and sometimes
            // (like on Windows) they cannot be accessed
            for (String implClass : GRPC_SERVICE_IMPLEMENTATION_CLASSES) {
                var locator = page.getByText(implClass);
                assertNotNull(locator, "DevUI gRPC services view is missing implementation class:" + implClass);
                assertNotNull(locator.textContent(), "DevUI gRPC services view is missing implementation class:" + implClass);
                assertTrue(locator.textContent().contains(implClass),
                        "DevUI gRPC services view is missing implementation class:" + implClass);
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
