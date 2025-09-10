package io.quarkus.ts.langchain4j;

import io.quarkus.test.bootstrap.DefaultService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.GitRepositoryQuarkusApplication;

@QuarkusScenario
public class ChatbotIT extends AbstractChatbotIT {

    @Container(image = "${redis.image}", port = 6379, expectedLog = "Ready to accept connections")
    static DefaultService redis = new DefaultService().withProperty("ALLOW_EMPTY_PASSWORD", "YES");

    @GitRepositoryQuarkusApplication(repo = "https://github.com/quarkiverse/quarkus-langchain4j.git", branch = "main", contextDir = "samples/chatbot", mavenArgs = DEFAULT_ARGS
            + " -Dsamples -Dplatform-deps")
    static final RestService app = new RestService()
            .withProperty("quarkus.redis.hosts",
                    () -> {
                        String redisHost = redis.getURI().withScheme("redis").getRestAssuredStyleUri();
                        return String.format("%s:%d", redisHost, redis.getURI().getPort());
                    })
            .withProperty("quarkus.langchain4j.openai.api-key", getKey());

    @Override
    RestService getApp() {
        return app;
    }
}
