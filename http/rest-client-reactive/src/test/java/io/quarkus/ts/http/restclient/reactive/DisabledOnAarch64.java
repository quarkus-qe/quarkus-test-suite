package io.quarkus.ts.http.restclient.reactive;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

@Inherited
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisabledOnAarch64Conditions.class)
public @interface DisabledOnAarch64 {

    /**
     * Why is the annotated test class or test method disabled.
     */
    String reason();
}