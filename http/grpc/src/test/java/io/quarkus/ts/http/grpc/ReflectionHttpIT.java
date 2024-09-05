package io.quarkus.ts.http.grpc;

import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;

import io.grpc.reflection.v1.FileDescriptorResponse;
import io.quarkus.ts.grpc.GreeterGrpc;
import io.quarkus.ts.grpc.HelloWorldProto;
import io.quarkus.ts.grpc.StreamingGrpc;
import io.vertx.mutiny.ext.web.client.WebClient;

public interface ReflectionHttpIT {

    WebClient getWebClient();

    @Test
    default void testReflectionServices() {
        var httpResponse = getWebClient().get("/http/reflection/service/info").sendAndAwait();
        assertEquals(SC_OK, httpResponse.statusCode());
        GrpcReflectionResponse response = httpResponse.bodyAsJson(GrpcReflectionResponse.class);

        assertEquals(3, response.getServiceCount());

        List<String> serviceList = response.getServiceList();

        assertEquals(3, serviceList.size());
        assertTrue(serviceList.stream().anyMatch(GreeterGrpc.SERVICE_NAME::equals));
        assertTrue(serviceList.stream().anyMatch(StreamingGrpc.SERVICE_NAME::equals));
        assertTrue(serviceList.stream().anyMatch("grpc.health.v1.Health"::equals));
    }

    @Test
    default void testReflectionMethods() throws InvalidProtocolBufferException {
        var httpResponse = getWebClient().get("/http/reflection/descriptor/greeting").sendAndAwait();
        assertEquals(SC_OK, httpResponse.statusCode());
        byte[] responseByteArray = httpResponse.body().getBytes();

        String fileDescriptor = FileDescriptorResponse.parseFrom(responseByteArray).toString();

        List<Descriptors.MethodDescriptor> serviceMethods = null;
        var serviceList = HelloWorldProto.getDescriptor().getServices();

        for (var service : serviceList) {
            String serviceName = service.getName();
            if (serviceName.compareTo("Streaming") == 0) {
                serviceMethods = HelloWorldProto.getDescriptor().getServices()
                        .get(service.getIndex()).getMethods();
                break;
            }
        }

        // Check if exists service "Streaming"
        assertNotNull(serviceMethods);

        for (var method : serviceMethods) {
            String methodName = method.getName();
            assertTrue(fileDescriptor.contains(methodName));
        }
    }

    @Test
    @DisplayName("GRPC reflection test - check service messages types")
    default void testReflectionMessages() throws InvalidProtocolBufferException {
        var httpResponse = getWebClient().get("/http/reflection/descriptor/greeting").sendAndAwait();
        assertEquals(SC_OK, httpResponse.statusCode());
        byte[] responseByteArray = httpResponse.body().getBytes();
        String fileDescriptor = FileDescriptorResponse.parseFrom(responseByteArray).toString();

        var messageTypes = HelloWorldProto.getDescriptor().getMessageTypes();

        for (var messageType : messageTypes) {
            String methodName = messageType.getName();
            assertTrue(fileDescriptor.contains(methodName));
        }
    }

    @Test
    @DisplayName("GRPC reflection test - check method SayHello of Greeter service exists and then call it")
    default void testReflectionCallMethod() throws InvalidProtocolBufferException {
        var httpResponse = getWebClient().get("/http/reflection/descriptor/greeting").sendAndAwait();
        assertEquals(SC_OK, httpResponse.statusCode());
        byte[] responseByteArray = httpResponse.body().getBytes();

        String fileDescriptor = FileDescriptorResponse.parseFrom(responseByteArray).toString();

        List<Descriptors.MethodDescriptor> serviceMethods = null;
        var serviceList = HelloWorldProto.getDescriptor().getServices();

        for (var service : serviceList) {
            String serviceName = service.getName();
            if (serviceName.compareTo("Greeter") == 0) {
                serviceMethods = HelloWorldProto.getDescriptor().getServices()
                        .get(service.getIndex()).getMethods();
                break;
            }
        }

        // Check if exists service "Greeter"
        assertNotNull(serviceMethods);

        // Check if exists sayHello method
        assertTrue(fileDescriptor.contains("SayHello"));

        // Call sayHello method and compare context
        httpResponse = getWebClient().get("/http/tester").sendAndAwait();
        assertEquals(SC_OK, httpResponse.statusCode());
        assertEquals("Hello tester", httpResponse.bodyAsString());
    }
}
