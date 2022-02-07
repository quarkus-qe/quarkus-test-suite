package io.quarkus.qe.messaging.infinispan.kafka;

import org.junit.jupiter.api.Tag;

import io.quarkus.test.junit.NativeImageTest;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;

@NativeImageTest
@EnabledOnNative
@Tag("fips-incompatible")
public class SslApacheKafkaClientIT extends SslApacheKafkaClientTest {

}