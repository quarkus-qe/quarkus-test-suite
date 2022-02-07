package io.quarkus.qe.messaging.infinispan.kafka;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.junit.DisabledOnNativeImage;
import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
@DisabledOnNativeImage("Github Ref: https://github.com/quarkusio/quarkus/issues/18000")
@Tag("fips-incompatible")
public class SaslApacheKafkaClientTestIT extends SaslApacheKafkaClientTest {
}
