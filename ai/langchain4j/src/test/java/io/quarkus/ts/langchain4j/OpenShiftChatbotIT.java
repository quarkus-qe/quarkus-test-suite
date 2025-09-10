package io.quarkus.ts.langchain4j;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.OpenShiftDeploymentStrategy;
import io.quarkus.test.scenarios.OpenShiftScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;

// Why this test uses container registry strategy?
// It is the easiest one to debug and create a standalone reproducer, when there is no quarkus-openshift extension.
// Feel free to replace it with another.
@OpenShiftScenario(deployment = OpenShiftDeploymentStrategy.UsingContainerRegistry)
public class OpenShiftChatbotIT extends AbstractChatbotIT {
    @Container(image = "${redis.image}", port = 6379, expectedLog = "Ready to accept connections")
    static DefaultService redis = new DefaultService().withProperty("ALLOW_EMPTY_PASSWORD", "YES");

    // TODO when any of these issues is solved, change the properties code below:
    //  - https://github.com/quarkus-qe/quarkus-test-framework/issues/1737
    //  - https://github.com/quarkus-qe/quarkus-test-framework/issues/1582
    @GitRepositoryQuarkusApplication(repo = "https://github.com/quarkiverse/quarkus-langchain4j.git", branch = "1.2", contextDir = "samples/chatbot", mavenArgs = DEFAULT_ARGS
            + " -Dsamples -Dplatform-deps")
    static final RestService app = new RestService()
            .withProperty("quarkus.redis.hosts",
                    () -> {
                        String redisHost = redis.getURI().withScheme("redis").getRestAssuredStyleUri();
                        return String.format("%s:%d", redisHost, redis.getURI().getPort());
                    })
            .withProperty("_ignored", "resource_with_destination::/tmp/catalog/|catalog/standard-saving-account.txt")
            .withProperty("offerings.folder", "/tmp/catalog")
            .withProperty("quarkus.langchain4j.openai.api-key", getKey());

    @Override
    RestService getApp() {
        return app;
    }
}
