package io.quarkus.ts.http.restclient.reactive;

import java.util.Base64;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.Container;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.response.Response;

@QuarkusScenario
public class ReactiveRestClientProxyIT {
    private static final String USER = "proxyuser";
    private static final String PASSWORD = "proxypassword";

    @Container(image = "docker.io/library/nginx:1-alpine", port = 8090, expectedLog = "Configuration complete; ready for start up")
    static RestService proxy = new RestService()
            .withProperty("_whatever", "resource_with_destination::/etc/nginx/|nginx.conf");

    @QuarkusApplication
    static RestService proxyApp = new RestService()
            .withProperties("proxy.properties")
            .withProperty("quarkus.rest-client.\"io.quarkus.ts.http.restclient.reactive.proxy.ProxyClient\".proxy-user", USER)
            .withProperty("quarkus.rest-client.\"io.quarkus.ts.http.restclient.reactive.proxy.ProxyClient\".proxy-password",
                    PASSWORD)
            .withProperty("quarkus.rest-client.\"io.quarkus.ts.http.restclient.reactive.proxy.ProxyClient\".proxy-address",
                    () -> proxy.getURI().withScheme(null).toString());

    @Test
    void sendRequestThroughProxy() {
        Response proxied = proxyApp.given().with().get("/proxied/");
        Assertions.assertEquals(HttpStatus.SC_OK, proxied.statusCode());
        Assertions.assertTrue(proxied.body().asString().contains("Example Domain"));
    }

    @Test
    void banned() {
        Response banned = proxyApp.given().with().get("/proxied/banned");
        Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, banned.statusCode());
        Assertions.assertEquals("Reading is prohibited by corporate policy!",
                banned.body().asString());
    }

    @Test
    /*
     * Nginx returns content of Proxy-Auth Header.
     * We check, that this content is made according to the specification.
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication#proxy_authentication
     */
    void authorization() {
        Response response = proxyApp.given().with().get("/proxied/authorization");
        Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode());
        String authorizationType = "Basic";
        String credentials = encode(USER + ":" + PASSWORD);
        String header = response.body().asString();
        Assertions.assertEquals(authorizationType + " " + credentials, header);
    }

    private static String encode(String source) {
        return Base64.getEncoder().encodeToString(source.getBytes());
    }
}
