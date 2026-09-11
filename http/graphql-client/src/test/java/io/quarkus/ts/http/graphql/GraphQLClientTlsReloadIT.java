package io.quarkus.ts.http.graphql;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.http.graphql.tls.TlsGraphQLClient;
import io.quarkus.ts.http.graphql.tls.TlsGraphQLClientResource;
import io.quarkus.ts.http.graphql.tls.TlsGraphQLEndpoint;
import io.quarkus.ts.http.graphql.tls.TlsReloadResource;

@Tag("QUARKUS-7864")
@QuarkusScenario
public class GraphQLClientTlsReloadIT {
    private static final String TLS_CONFIGURATION = "graphql-client";

    private Path badClient;
    private Path goodClient;
    private Path activeClient;

    @QuarkusApplication(ssl = true, properties = "server-tls.properties", classes = { TlsGraphQLEndpoint.class })
    static final RestService server = new RestService();

    @QuarkusApplication(properties = "client-tls.properties", classes = { TlsGraphQLClient.class,
            TlsGraphQLClientResource.class, TlsReloadResource.class })
    static final RestService client = new RestService()
            .setAutoStart(false)
            .withProperty("quarkus.smallrye-graphql-client.tls-client.url", () -> server.getURI(Protocol.HTTPS) + "/graphql")
            .withProperty("quarkus.smallrye-graphql-client.tls-client.tls-configuration-name", TLS_CONFIGURATION)
            .withProperty("quarkus.smallrye-graphql-client.tls-dynamic.url", () -> server.getURI(Protocol.HTTPS) + "/graphql")
            .withProperty("quarkus.smallrye-graphql-client.tls-dynamic.tls-configuration-name", TLS_CONFIGURATION);

    @BeforeEach
    public void prepareCertificate() {
        Path certificateDirectory = client.getServiceFolder().toAbsolutePath();

        badClient = certificateDirectory.resolve("client-bad.p12");
        goodClient = certificateDirectory.resolve("client-good.p12");
        activeClient = certificateDirectory.resolve("client-active.p12");
        copyFile(badClient, activeClient);
    }

    @AfterEach
    public void stopApplication() throws IOException {
        client.stop();
        Files.deleteIfExists(activeClient);
    }

    @Test
    public void typesafeClientShouldUseReloadCertificate() throws IOException {
        configureCertificateAndStartClientApp(activeClient);

        client.given()
                .when()
                .get("/client/tls/typesafe")
                .then()
                .statusCode(502);

        replaceCertificate(goodClient, activeClient);

        awaitSuccessfulReload("/client/tls/typesafe");
    }

    @Test
    public void dynamicClientShouldUseReloadedCertificate() throws IOException {
        configureCertificateAndStartClientApp(activeClient);

        client.given()
                .when()
                .get("/client/tls/dynamic")
                .then()
                .statusCode(502);

        replaceCertificate(goodClient, activeClient);

        awaitSuccessfulReload("/client/tls/dynamic");
    }

    protected void configureCertificateAndStartClientApp(Path activeClient) {
        client.withProperty("quarkus.tls.graphql-client.key-store.p12.path", activeClient.toString())
                .start();
    }

    protected void replaceCertificate(Path goodClient, Path activeClient) throws IOException {
        copyFile(goodClient, activeClient);
    }

    protected static void reloadTls() {
        client.given()
                .when()
                .post("/tls/reload/" + TLS_CONFIGURATION)
                .then()
                .statusCode(204);
    }

    protected void awaitSuccessfulReload(String endpoint) {
        reloadTls();

        client.given()
                .get(endpoint)
                .then()
                .statusCode(200);
    }

    private static void copyFile(Path source, Path destination) {
        try {
            Files.copy(source.toAbsolutePath(), destination.toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to copy file from " + source + " to " + destination, e);
        }
    }
}