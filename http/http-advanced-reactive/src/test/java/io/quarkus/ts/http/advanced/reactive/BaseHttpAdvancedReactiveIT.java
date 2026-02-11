package io.quarkus.ts.http.advanced.reactive;

import static io.quarkus.ts.http.advanced.reactive.MultipleResponseSerializersResource.APPLY_RESPONSE_SERIALIZER_PARAM_FLAG;
import static io.quarkus.ts.http.advanced.reactive.MultipleResponseSerializersResource.MULTIPLE_RESPONSE_SERIALIZERS_PATH;
import static io.quarkus.ts.http.advanced.reactive.mediatype.MediaTypeResource.ANY_ENCODING;
import static io.quarkus.ts.http.advanced.reactive.mediatype.MediaTypeResource.APPLICATION_YAML;
import static io.quarkus.ts.http.advanced.reactive.mediatype.MediaTypeResource.ENGLISH;
import static io.quarkus.ts.http.advanced.reactive.mediatype.MediaTypeResource.JAPANESE;
import static io.quarkus.ts.http.advanced.reactive.mediatype.MediaTypeResource.MEDIA_TYPE_PATH;
import static io.quarkus.ts.http.advanced.reactive.multipart.MultipartResource.FILE;
import static io.quarkus.ts.http.advanced.reactive.multipart.MultipartResource.MULTIPART_FORM_PATH;
import static io.quarkus.ts.http.advanced.reactive.multipart.MultipartResource.TEXT;
import static io.quarkus.ts.http.advanced.reactive.sse.SseEventUpdateResource.DATA_VALUE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static jakarta.ws.rs.core.MediaType.TEXT_XML;
import static org.apache.http.HttpHeaders.ACCEPT_ENCODING;
import static org.apache.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.htmlunit.util.MimeType.IMAGE_JPEG;
import static org.htmlunit.util.MimeType.IMAGE_PNG;
import static org.htmlunit.util.MimeType.TEXT_CSS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.core.HttpHeaders;

import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.annotations.DisabledOnNative;
import io.quarkus.test.security.certificate.CertificateBuilder;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.ext.web.client.HttpResponse;

public abstract class BaseHttpAdvancedReactiveIT {

    private static final String ROOT_PATH = "/api";
    private static final String HELLO_ENDPOINT = ROOT_PATH + "/hello";
    private static final String GREETING_ENDPOINT = ROOT_PATH + "/greeting";
    private static final int TIMEOUT_SEC = 3;
    private static final int RETRY = 3;
    private static final String PASSWORD = "password";
    private static final String UTF_8_CHARSET = ";charset=UTF-8";
    private static final String CONTENT = "content";

    protected abstract RestService getApp();

    @Test
    @DisplayName("Http/1.1 Server test")
    public void httpServer() {
        getApp().given().get(HELLO_ENDPOINT)
                .then().statusLine("HTTP/1.1 200 OK").statusCode(SC_OK)
                .body("content", is("Hello, World!"));
    }

    @Test
    @DisplayName("Test Quarkus REST abstract resource with @Path")
    @DisabledOnNative(reason = "https://github.com/quarkusio/quarkus/issues/42976")
    public void abstractResourceWithPath() {
        getApp().given().get(GREETING_ENDPOINT)
                .then().statusCode(SC_OK)
                .body(is("Hello from Quarkus REST"));
    }

    @Test
    @DisplayName("Test Quarkus REST using CDI getting the sub-resource")
    void cdiSubResourceGetRequest() {
        getApp().given()
                .when()
                .get(GREETING_ENDPOINT + "/cdi-sub-resource")
                .then()
                .statusCode(SC_OK)
                .body(is("Greeting from sub-resource using GET"));
    }

    @Test
    @DisplayName("Test Quarkus REST using CDI getting the sub-resource")
    void cdiSubResourcePostRequest() {
        getApp().given()
                .when()
                .post(GREETING_ENDPOINT + "/cdi-sub-resource")
                .then()
                .statusCode(SC_OK)
                .body(is("Greeting from sub-resource using POST"));
    }

    @Test
    @DisplayName("Test Quarkus REST interface resource with @Path")
    void interfaceResourceWithPath() {
        getApp().given()
                .when()
                .get(GREETING_ENDPOINT + "/interface-greeting")
                .then()
                .statusCode(SC_OK)
                .body(is("Greeting from interface"));
    }

    @Test
    @Tag("https://github.com/quarkusio/quarkus/issues/43422")
    @DisplayName("Test Quarkus REST abstract resource with @Path using OPTION and HEAD request")
    @DisabledOnNative(reason = "https://github.com/quarkusio/quarkus/issues/42976")
    void optionAndHeadRequestUsingAbstractResourceWithPath() {
        testOptionRequest(GREETING_ENDPOINT);
        testHeadRequest(GREETING_ENDPOINT);
    }

    @Test
    @Tag("https://github.com/quarkusio/quarkus/issues/43422")
    @DisplayName("Test Quarkus REST interface resource with @Path using OPTION and HEAD request")
    void optionAndHeadRequestUsingInterfaceResourceWithPath() {
        testOptionRequest(GREETING_ENDPOINT + "/interface-greeting");
        testHeadRequest(GREETING_ENDPOINT + "/interface-greeting");
    }

    @Test
    @Tag("https://github.com/quarkusio/quarkus/issues/43422")
    @DisplayName("Test Quarkus REST using CDI getting the sub-resource for OPTION and HEAD request")
    void cdiSubResourceOptionAndHeadRequest() {
        testOptionRequest(GREETING_ENDPOINT + "/cdi-sub-resource");
        testHeadRequest(GREETING_ENDPOINT + "/cdi-sub-resource");
    }

    @Test
    @DisplayName("GRPC Server test")
    public void testGrpc() {
        getApp().given().when().get("/api/grpc/trinity").then().statusCode(SC_OK).body(is("Hello trinity"));
    }

    @Test
    @DisplayName("Http/2 Server test")
    public void http2Server() throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        Uni<JsonObject> content = getApp().mutiny(defaultVertxHttpClientOptions())
                .getAbs(getAppEndpoint() + "/hello")
                .send()
                .map(response -> {
                    if (response.statusCode() != 200) {
                        throw new AssertionError("Wrong status code: " + response.statusCode());
                    } else if (response.version().compareTo(HttpVersion.HTTP_2) != 0) {
                        throw new AssertionError("Expected HTTP/2, but found " + response.version());
                    }
                    return response;
                })
                .map(HttpResponse::bodyAsJsonObject).ifNoItem().after(Duration.ofSeconds(TIMEOUT_SEC)).fail()
                .onFailure().retry().atMost(RETRY);

        content.subscribe().with(body -> {
            assertEquals(body.getString("content"), "Hello, World!");
            done.countDown();
        });

        done.await(TIMEOUT_SEC, TimeUnit.SECONDS);
        assertThat(done.getCount(), equalTo(0L));
    }

    @Test
    public void microprofileHttpClientRedirection() {
        Response health = getApp().given().get("api/client");
        assertEquals(HttpStatus.SC_OK, health.statusCode());
    }

    @DisplayName("Jakarta REST URI path template test")
    @Test
    public void uriPathTemplate() {
        // test parameter name starting with an alphabetic character, containing dash and with literal value
        req99BottlesOfBeer(1, SC_OK).body(is(NinetyNineBottlesOfBeerResource.FIRST_BOTTLE_RESPONSE));
        // test parameter name starting with a number and a regex range value
        req99BottlesOfBeer(2, SC_OK).body(is(NinetyNineBottlesOfBeerResource.SECOND_BOTTLE_RESPONSE));
        // test parameter name starting with an underscore, containing a dot and regex ranges disjunction
        req99BottlesOfBeer(3, SC_OK).body(is(String.format(NinetyNineBottlesOfBeerResource.OTHER_BOTTLES_RESPONSE, 3, 3, 2)));
        // test regex works correctly (matched value between 3 and 99 [inclusive])
        req99BottlesOfBeer(99, SC_OK)
                .body(is(String.format(NinetyNineBottlesOfBeerResource.OTHER_BOTTLES_RESPONSE, 99, 99, 98)));
        // test regex works correctly (no path should be matched)
        req99BottlesOfBeer(-100, SC_NOT_FOUND);
    }

    @DisplayName("RESTEasy Reactive Multipart Provider test")
    @Test
    public void multipartFormDataReader() {
        getApp().given()
                .multiPart(FILE, Paths.get("src", "test", "resources", "file.txt").toFile())
                .formParam(TEXT, TEXT)
                .post(ROOT_PATH + MULTIPART_FORM_PATH)
                .then().statusCode(SC_OK)
                .body(FILE, is("File content"))
                .body(TEXT, is(TEXT));
    }

    @Test
    @DisplayName("RESTEasy Reactive Multipart Max Parameters test")
    public void multiPartFormDataMaxParametersAllowed() {
        // We are going to reach the MAX_PARAMETERS_ALLOWED in Quarkus Multipart that it's 1000
        final int PARAMETERS_TO_ADD = 998;
        // The file itself it's one parameter to add also
        var request = getApp().given().multiPart(FILE, Paths.get("src", "test", "resources", "file.txt").toFile());

        for (int i = 0; i < PARAMETERS_TO_ADD; i++) {
            request = request.multiPart("param" + i, "value" + i);
        }
        //  now we add the parameter TEXT with formParam, so we will get the max limit 1000
        request.formParam(TEXT, TEXT);
        request
                .post(ROOT_PATH + MULTIPART_FORM_PATH)
                .then()
                .statusCode(SC_OK)
                .body(FILE, is("File content"))
                .body(TEXT, is(TEXT));

    }

    @DisplayName("RESTEasy Reactive Multipart Over the Max Parameters limit test")
    @Test
    public void exceedMultiPartParamsMaxLimit() {
        // The file itself it's one parameter to add also
        var request = getApp().given().multiPart(FILE, Paths.get("src", "test", "resources", "file.txt").toFile());
        // We test the over the max limit of parameters defined by Quarkus that is 1000.
        final int PARAMETERS_TO_ADD = 999;
        for (int i = 0; i < PARAMETERS_TO_ADD; i++) {
            request = request.multiPart("param" + i, "value" + i);
        }
        // we add the parameter TEXT with formParam so the total parameters will be 1000 + 1 so we are sending 1001
        request.formParam(TEXT, TEXT);
        request
                .post(ROOT_PATH + MULTIPART_FORM_PATH)
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    @DisplayName("Jakarta REST RouterFilter and Vert.x Web Routes integration")
    @Test
    public void multipleResponseFilter() {
        // test headers from both filters are present, that is useful content negotiation
        // scenario -> server side should be able to set multiple VARY response headers
        // so browser can identify when to serve cached response and when to send request to a server side
        var headers = getApp().given().get(ROOT_PATH + MEDIA_TYPE_PATH).headers().asList();
        assertHasHeaderWithValue(HttpHeaders.ACCEPT_LANGUAGE, ENGLISH, headers);
        assertHasHeaderWithValue(HttpHeaders.ACCEPT_LANGUAGE, JAPANESE, headers);
        assertHasHeaderWithValue(HttpHeaders.ACCEPT_ENCODING, ANY_ENCODING, headers);
        assertHasHeaderWithValue(HttpHeaders.VARY, ACCEPT_ENCODING, headers);
        assertHasHeaderWithValue(HttpHeaders.VARY, ACCEPT_LANGUAGE, headers);
    }

    @DisplayName("Several Resources share same base path test")
    @Test
    public void severalResourcesSameBasePath() {
        // following endpoints are placed in 2 different Resources with the same base path
        getApp().given().get(HELLO_ENDPOINT).then().body(CONTENT, is("Hello, World!"));
        getApp().given().get(HELLO_ENDPOINT + HelloAllResource.ALL_ENDPOINT_PATH).then().body(CONTENT, is("Hello all, World!"));
    }

    private void assertHasHeaderWithValue(String headerName, String headerValue, List<Header> headers) {
        Assertions.assertTrue(
                headers
                        .stream()
                        .filter(h -> h.getName().equalsIgnoreCase(headerName))
                        .map(Header::getValue)
                        .anyMatch(headerValue::equals));
    }

    @DisplayName("Jakarta REST MessageBodyWriter test")
    @Test
    public void messageBodyWriter() {
        // test MediaType is passed to MessageBodyWriter correctly
        String mediaTypeProperty = "mediaType";
        getApp()
                .given()
                .get(ROOT_PATH + MEDIA_TYPE_PATH)
                .then()
                .statusCode(SC_OK)
                .body(mediaTypeProperty, notNullValue())
                .body(mediaTypeProperty + ".type", is("application"))
                .body(mediaTypeProperty + ".subtype", is("json"));
    }

    @DisplayName("Jakarta REST Response Content type test")
    @Test
    public void responseContentType() {
        testResponseContentType(APPLICATION_JSON, APPLICATION_JSON + UTF_8_CHARSET);
        testResponseContentType(APPLICATION_XML, APPLICATION_XML + UTF_8_CHARSET);
        testResponseContentType(APPLICATION_YAML, APPLICATION_YAML + UTF_8_CHARSET);
        testResponseContentType(TEXT_HTML, TEXT_HTML + UTF_8_CHARSET);
        testResponseContentType(TEXT_PLAIN, TEXT_PLAIN + UTF_8_CHARSET);
        testResponseContentType(TEXT_CSS, TEXT_CSS + UTF_8_CHARSET);
        testResponseContentType(TEXT_XML, TEXT_XML + UTF_8_CHARSET);
        testResponseContentType(APPLICATION_OCTET_STREAM, APPLICATION_OCTET_STREAM);
        testResponseContentType(MULTIPART_FORM_DATA, MULTIPART_FORM_DATA);
        testResponseContentType(IMAGE_PNG, IMAGE_PNG);
        testResponseContentType(IMAGE_JPEG, IMAGE_JPEG);
    }

    @Test
    public void testMediaTypePassedToMessageBodyWriter() {
        // Accepted Media Type must be passed to 'MessageBodyWriter'
        // 'MessageBodyWriter' then returns passed Media Type for a verification
        assertAcceptedMediaTypeEqualsResponseBody(APPLICATION_JSON);
        assertAcceptedMediaTypeEqualsResponseBody(TEXT_HTML);
        assertAcceptedMediaTypeEqualsResponseBody(TEXT_PLAIN);
        assertAcceptedMediaTypeEqualsResponseBody(APPLICATION_OCTET_STREAM);
    }

    @Test
    @Tag("QUARKUS-2004")
    public void constraintsExist() throws JsonProcessingException {
        Response response = getApp().given().get("/q/openapi");
        Assertions.assertEquals(HttpStatus.SC_OK, response.statusCode());

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode body = mapper.readTree(response.body().asString());

        JsonNode validation = body.get("components").get("schemas").get("Hello").get("properties").get("content");

        Assertions.assertEquals(4, validation.get("maxLength").asInt());
        Assertions.assertEquals(1, validation.get("minLength").asInt());
        Assertions.assertEquals("^[A-Za-z]+$", validation.get("pattern").asText());
    }

    @Test
    @Tag("QUARKUS-3672")
    public void interceptedTest() {
        // make server to generate a response so interceptors might intercept it
        // ignore response, we will read interceptors result later
        getApp().given()
                .get(ROOT_PATH + "/intercepted")
                .thenReturn();

        String response = getApp().given()
                .get(ROOT_PATH + "/intercepted/messages")
                .thenReturn().getBody().asString();

        Assertions.assertTrue(response.contains("Unconstrained"), "Unconstrained interceptor should be invoked");
        Assertions.assertTrue(response.contains("Server"), "Server interceptor should be invoked");
        Assertions.assertFalse(response.contains("Client"), "Client interceptor should not be invoked");
    }

    @DisplayName("SSE check for event responses values containing empty data")
    @Test
    @Tag("QUARKUS-3701")
    void testSseResponseForEmptyData() {
        getApp().given()
                .get(ROOT_PATH + "/sse/client-update")
                .then().statusCode(SC_OK)
                .body(containsString(String.format("event: name=NON EMPTY data={%s} and is empty: false", DATA_VALUE)),
                        containsString("event: name=EMPTY data={} and is empty: true"));
    }

    @Test
    void testGetOnRecordWithQuarkusAnnotations() {
        callGet(ROOT_PATH + "/cheese/quarkus/my-cheese;variant=goat");
    }

    @Test
    void testGetOnRecordWithSpecAnnotations() {
        callGet(ROOT_PATH + "/cheese/spec/my-cheese;variant=goat");
    }

    @Test
    void testGetOnClassWithQuarkusAnnotations() {
        callGet(ROOT_PATH + "/cheese/class/my-cheese;variant=goat");
    }

    @Test
    void testPostOnRecordWithQuarkusAnnotations() {
        callPost(ROOT_PATH + "/cheese/quarkus/my-cheese;variant=goat");
    }

    @Test
    void testPostOnRecordWithSpecAnnotations() {
        callPost(ROOT_PATH + "/cheese/spec/my-cheese;variant=goat");
    }

    @Test
    void testPostOnClassWithQuarkusAnnotations() {
        callPost(ROOT_PATH + "/cheese/class/my-cheese;variant=goat");
    }

    @Test
    void testInvalidPostOnRecordWithQuarkusAnnotations() {
        callInvalidPost(ROOT_PATH + "/cheese/quarkus/my-cheese;variant=goat");
    }

    @Test
    void testInvalidPostOnRecordWithSpecAnnotations() {
        callInvalidPost(ROOT_PATH + "/cheese/spec/my-cheese;variant=goat");
    }

    @Test
    void testInvalidPostOnClassWithQuarkusAnnotations() {
        callInvalidPost(ROOT_PATH + "/cheese/class/my-cheese;variant=goat");
    }

    private void callGet(String path) {
        getApp().given()
                .urlEncodingEnabled(false) // https://stackoverflow.com/a/59990214 matrix-parameters workaround
                .header("X-Cheese-Secret-Handshake", "s8cr8t")
                .header("Header-Param", "foo")
                .cookie("level", "hardcore")
                .queryParam("age", "2")
                .get(path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(CoreMatchers.containsString("foo/"))
                .body(CoreMatchers.containsString(
                        "type=my-cheese, variant=goat, age=2, level=hardcore, secretHandshake=s8cr8t, smell=null, ar=AgeRecord[age=2], ac=AgeClass{age=2}"));
    }

    private void callPost(String path) {
        getApp().given()
                .urlEncodingEnabled(false) // https://stackoverflow.com/a/59990214 matrix-parameters workaround
                .header("X-Cheese-Secret-Handshake", "s8cr8t")
                .header("Header-Param", "foo")
                .cookie("level", "hardcore")
                .queryParam("age", "2")
                .formParams("degree", "aging", "smell", "strong")
                .post(path)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(CoreMatchers.containsString("foo/"))
                .body(CoreMatchers.containsString(
                        "type=my-cheese, variant=goat, age=2, level=hardcore, secretHandshake=s8cr8t, smell=strong, ar=AgeRecord[age=2], ac=AgeClass{age=2}"));
    }

    private void callInvalidPost(String path) {
        getApp().given()
                .urlEncodingEnabled(false) // https://stackoverflow.com/a/59990214 matrix-parameters workaround
                .header("X-Cheese-Secret-Handshake", "s8cr8t")
                .header("Header-Param", "foo")
                .queryParam("age", "2")
                .formParams("degree", "aging", "smell", "strong")
                .post(path)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(CoreMatchers.containsString("Level may not be blank"));
    }

    @Test
    void testOpenAPISchemasForParametersContainers() throws JsonProcessingException {
        Response response = getApp().given().get("/q/openapi");
        assertEquals(HttpStatus.SC_OK, response.statusCode());

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonNode body = mapper.readTree(response.body().asString());

        JsonNode quarkusSchema = body.get("components").get("schemas").get("ParametersQuarkus");
        JsonNode specSchema = body.get("components").get("schemas").get("ParametersSpec");
        JsonNode classSchema = body.get("components").get("schemas").get("ParametersClass");

        checkParametersContainersSchema(quarkusSchema);
        checkParametersContainersSchema(specSchema);
        checkParametersContainersSchema(classSchema);
    }

    private static void checkParametersContainersSchema(JsonNode schema) {
        assertNotNull(schema);
        assertEquals("object", schema.get("type").asText());

        JsonNode schemaProperties = schema.get("properties");

        assertNotNull(schemaProperties.get("type"));
        assertEquals("string", schemaProperties.get("type").get("type").asText());

        assertNotNull(schemaProperties.get("variant"));
        assertEquals("string", schemaProperties.get("variant").get("type").asText());

        assertNotNull(schemaProperties.get("age"));
        assertEquals("string", schemaProperties.get("age").get("type").asText());

        assertNotNull(schemaProperties.get("level"));
        assertEquals("string", schemaProperties.get("level").get("type").asText());

        assertNotNull(schemaProperties.get("secretHandshake"));
        assertEquals("string", schemaProperties.get("secretHandshake").get("type").asText());

        assertNotNull(schemaProperties.get("smell"));
        assertEquals("string", schemaProperties.get("smell").get("type").asText());

        assertNotNull(schemaProperties.get("ar"));
        assertEquals("#/components/schemas/AgeRecord", schemaProperties.get("ar").get("$ref").asText());

        assertNotNull(schemaProperties.get("ac"));
        assertEquals("#/components/schemas/AgeClass", schemaProperties.get("ac").get("$ref").asText());
    }

    private void assertAcceptedMediaTypeEqualsResponseBody(String acceptedMediaType) {
        getApp()
                .given()
                .accept(acceptedMediaType)
                .queryParam(APPLY_RESPONSE_SERIALIZER_PARAM_FLAG, Boolean.TRUE)
                .get(ROOT_PATH + MULTIPLE_RESPONSE_SERIALIZERS_PATH)
                .then()
                .body(is(acceptedMediaType));
    }

    private void testResponseContentType(String acceptedContentType, String expectedContentType) {
        getApp().given()
                .accept(acceptedContentType)
                .get(ROOT_PATH + MEDIA_TYPE_PATH)
                .then().header(CONTENT_TYPE, expectedContentType);
    }

    private void testOptionRequest(String path) {
        getApp().given()
                .when()
                .options(path)
                .then()
                .statusCode(SC_OK)
                .header(HttpHeaders.ALLOW, allOf(containsString("HEAD"),
                        containsString("OPTIONS"), containsString("GET")));
    }

    private void testHeadRequest(String path) {
        getApp().given()
                .when()
                .head(path)
                .then()
                .statusCode(SC_OK);
    }

    private ValidatableResponse req99BottlesOfBeer(int bottleNumber, int httpStatusCode) {
        return getApp().given()
                .get(ROOT_PATH + NinetyNineBottlesOfBeerResource.PATH + "/" + bottleNumber)
                .then().statusCode(httpStatusCode);
    }

    protected Protocol getProtocol() {
        return Protocol.HTTPS;
    }

    private String getAppEndpoint() {
        return getApp().getURI(getProtocol()).withPath(ROOT_PATH).toString();
    }

    private WebClientOptions defaultVertxHttpClientOptions() {
        return new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_2).setSsl(true).setVerifyHost(false)
                .setUseAlpn(true)
                .setTrustStoreOptions(new JksOptions().setPassword(PASSWORD).setPath(defaultTruststore()));
    }

    private String defaultTruststore() {
        return getApp()
                .<CertificateBuilder> getPropertyFromContext(CertificateBuilder.INSTANCE_KEY)
                .certificates()
                .get(0)
                .truststorePath();
    }
}
