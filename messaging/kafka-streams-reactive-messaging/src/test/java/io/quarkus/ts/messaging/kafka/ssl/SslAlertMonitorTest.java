package io.quarkus.ts.messaging.kafka.ssl;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.ts.messaging.kafka.BaseKafkaStreamTest;

@QuarkusTest
@QuarkusTestResource(SslStrimziKafkaTestResource.class)
public class SslAlertMonitorTest extends BaseKafkaStreamTest {

    private static final String APP_URL = "http://localhost:8081/";

    @Override
    protected String getAppUrl() {
        return APP_URL;
    }
}
