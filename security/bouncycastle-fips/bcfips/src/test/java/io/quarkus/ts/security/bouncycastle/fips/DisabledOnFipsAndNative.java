package io.quarkus.ts.security.bouncycastle.fips;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DisabledOnFipsAndNativeCondition.class)
public @interface DisabledOnFipsAndNative {
    /**
     * Why is the annotated test class or test method disabled.
     */
    String reason() default "";
}
