package io.quarkus.qe.messaging.infinispan.quickstart;

import io.quarkus.test.junit.NativeImageTest;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;

@NativeImageTest
@EnabledOnNative
public class NativeInfinispanServerResourceIT extends InfinispanServerResourceTest {
    // Run the same tests
}
