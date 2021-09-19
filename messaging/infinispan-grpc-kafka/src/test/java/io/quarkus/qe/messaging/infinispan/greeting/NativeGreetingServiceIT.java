package io.quarkus.qe.messaging.infinispan.greeting;

import io.quarkus.test.junit.NativeImageTest;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;

@NativeImageTest
@EnabledOnNative
public class NativeGreetingServiceIT extends GreetingServiceTest {

}