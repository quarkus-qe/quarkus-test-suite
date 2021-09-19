package io.quarkus.qe.messaging.infinispan.prices;

import io.quarkus.test.junit.NativeImageTest;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;

@NativeImageTest
@EnabledOnNative
public class NativePriceServiceIT extends PriceServiceTest {
}
