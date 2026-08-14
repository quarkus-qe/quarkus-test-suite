package io.quarkus.ts.http.graphql;

import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;

import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.dsl.NonDeletingOperation;
import io.quarkus.test.bootstrap.inject.OpenShiftClient;
import io.quarkus.test.scenarios.OpenShiftScenario;

@Tag("QUARKUS-7864")
@OpenShiftScenario
public class OpenShiftGraphQLClientTlsReloadIT extends GraphQLClientTlsReloadIT {
    private static final String CLIENT_CERTIFICATE_PATH = "/test/client-active.p12";

    private String secretName;

    @Inject
    static OpenShiftClient openShiftClient;

    @Override
    @AfterEach
    public void stopApplication() throws IOException {
        super.stopApplication();

        if (secretName != null) {
            var fabric8Client = openShiftClient.getFabric8Client();

            fabric8Client
                    .secrets()
                    .inNamespace(fabric8Client.getNamespace())
                    .withName(secretName)
                    .delete();

            secretName = null;
        }
    }

    @Override
    protected void configureCertificateAndStartClientApp(Path activeClient) {
        client.withProperty("quarkus.tls.graphql-client.key-store.p12.path", CLIENT_CERTIFICATE_PATH)
                .withProperty("certificate-secret-mount", "secret_with_destination::/test/|client-active.p12")
                .start();
    }

    @Override
    protected void replaceCertificate(Path goodClient, Path activeClient) throws IOException {
        updateCertificateOnOpenShift(goodClient, activeClient.getFileName().toString());
    }

    @Override
    protected void awaitSuccessfulReload(String endpoint) {
        await()
                .atMost(Duration.ofMinutes(4))
                .pollDelay(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    reloadTls();
                    client.given().get(endpoint).then().statusCode(200);
                });
    }

    /**
     * On OpenShift, the certificate is mounted from a Secret into the pod filesystem. We cannot simply overwrite
     * the file on disk as in the local scenario, instead we update the Secret data directly via the Kubernetes API.
     * OpenShift will then propagate the updated Secret content to the mounted volume inside the running pod.
     */
    private void updateCertificateOnOpenShift(Path source, String fileName) throws IOException {
        String namespace = openShiftClient.getFabric8Client().getNamespace();

        secretName = openShiftClient.getFabric8Client().secrets()
                .inNamespace(namespace)
                .list()
                .getItems()
                .stream()
                .filter(s -> s.getMetadata().getName() != null
                        && s.getMetadata().getName().endsWith(fileName.replace('.', '-')))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No secret matching prefix found"))
                .getMetadata()
                .getName();

        String fileContent = Base64.getEncoder().encodeToString(Files.readAllBytes(source.toAbsolutePath()));

        var secret = new SecretBuilder()
                .withNewMetadata()
                .withName(secretName)
                .withNamespace(namespace)
                .endMetadata()
                .addToData(fileName, fileContent)
                .build();

        openShiftClient.getFabric8Client().secrets().inNamespace(namespace)
                .resource(secret).unlock()
                .createOr(NonDeletingOperation::patch);
    }
}
