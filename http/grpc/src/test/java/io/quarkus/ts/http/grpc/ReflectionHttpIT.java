package io.quarkus.ts.http.grpc;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;

import io.grpc.Channel;
import io.grpc.reflection.v1.FileDescriptorResponse;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.ts.grpc.GreeterGrpc;
import io.quarkus.ts.grpc.HelloWorldProto;
import io.quarkus.ts.grpc.StreamingGrpc;

public interface ReflectionHttpIT {

    RestService app();

    Channel getChannel();

    @Test
    default void testReflectionServices() {
        GrpcReflectionResponse response = app().given().when().get("/http/reflection/service/info")
                .then().statusCode(SC_OK).extract().response()
                .jsonPath().getObject(".", GrpcReflectionResponse.class);

        assertEquals(3, response.getServiceCount());

        List<String> serviceList = response.getServiceList();

        assertEquals(3, serviceList.size());
        assertTrue(serviceList.stream().anyMatch(GreeterGrpc.SERVICE_NAME::equals));
        assertTrue(serviceList.stream().anyMatch(StreamingGrpc.SERVICE_NAME::equals));
        assertTrue(serviceList.stream().anyMatch("grpc.health.v1.Health"::equals));
    }

    @Test
    default void testReflectionMethods() throws InvalidProtocolBufferException {
        byte[] responseByteArray = app().given().when().get("/http/reflection/descriptor/greeting")
                .then().statusCode(SC_OK).extract().body().asByteArray();

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
        byte[] responseByteArray = app().given().when().get("/http/reflection/descriptor/greeting")
                .then().statusCode(SC_OK).extract().body().asByteArray();
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
        byte[] responseByteArray = app().given().when().get("/http/reflection/descriptor/greeting")
                .then().statusCode(SC_OK).extract().body().asByteArray();

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
        app().given().when().get("/http/tester").then().statusCode(SC_OK).body(Matchers.is("Hello tester"));
    }
}
