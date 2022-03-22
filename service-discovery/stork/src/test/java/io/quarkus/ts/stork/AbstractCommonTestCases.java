package io.quarkus.ts.stork;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.http.HttpStatus;

import io.quarkus.test.bootstrap.RestService;
import io.restassured.response.ValidatableResponse;

import junit.framework.AssertionFailedError;

public class AbstractCommonTestCases {

    public static final String PREFIX = "ping-";

    public ValidatableResponse makePingCall(RestService service, String subPath) {
        return service
                .given()
                .get("/ping/" + subPath).then()
                .statusCode(HttpStatus.SC_OK);
    }

    public static String getConsultEndpoint(String endpoint) {
        return endpoint.replaceFirst(":\\d+", "");
    }

    public static String getAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return String.valueOf(socket.getLocalPort());
        } catch (IOException e) {
            throw new AssertionFailedError();
        }
    }
}
