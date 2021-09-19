package io.quarkus.qe.messaging.infinispan.books;

import io.quarkus.test.junit.NativeImageTest;
import io.quarkus.test.scenarios.annotations.EnabledOnNative;

@NativeImageTest
@EnabledOnNative
public class NativeBookServiceIT extends BookServiceTest {

}
