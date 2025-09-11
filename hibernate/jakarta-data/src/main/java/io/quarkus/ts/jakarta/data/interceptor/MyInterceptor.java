package io.quarkus.ts.jakarta.data.interceptor;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@MyInterceptorBinding
@Interceptor
@Priority(1)
public class MyInterceptor {

    @AroundInvoke
    Object aroundInvoke(InvocationContext ctx) throws Exception {
        throw new RuntimeException("You shall not pass!");
    }

}
