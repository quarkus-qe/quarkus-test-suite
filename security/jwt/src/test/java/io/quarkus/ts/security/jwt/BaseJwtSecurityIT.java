package io.quarkus.ts.security.jwt;

import static io.quarkus.ts.security.jwt.GenerateJwtResource.Invalidity;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.http.HttpStatus;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public abstract class BaseJwtSecurityIT {

    private static final String EMPTY_GROUP = "";

    @Test
    public void securedEveryoneNoGroup() {
        givenWithToken(createToken(EMPTY_GROUP))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(
                        "Hello, test-subject@example.com, "
                                + "your token was issued by https://my.auth.server/ and you are in groups []"));
    }

    @Test
    public void securedEveryoneViewGroup() {
        givenWithToken(createToken("view"))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(
                        "Hello, test-subject@example.com, "
                                + "your token was issued by https://my.auth.server/ and you are in groups [view]"));
    }

    @Test
    public void securedEveryoneAdminGroup() {
        givenWithToken(createToken("admin"))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(
                        "Hello, test-subject@example.com, "
                                + "your token was issued by https://my.auth.server/ and you are in groups [admin, superuser]"));
    }

    @Test
    public void securedEveryoneWrongIssuer() {
        givenWithToken(createToken(Invalidity.WRONG_ISSUER, EMPTY_GROUP))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void securedEveryoneWrongDate() {
        givenWithToken(createToken(Invalidity.WRONG_DATE, EMPTY_GROUP))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void securedEveryoneWrongKey() {
        givenWithToken(createToken(Invalidity.WRONG_KEY, EMPTY_GROUP))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void securedAdminNoGroup() {
        givenWithToken(createToken(EMPTY_GROUP))
                .get("/secured/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void securedAdminViewGroup() {
        givenWithToken(createToken("view"))
                .get("/secured/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void securedAdminAdminGroup() {
        givenWithToken(createToken("admin"))
                .get("/secured/admin")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Restricted area! Admin access granted!"));
    }

    @Test
    public void securedNoOneNoGroup() {
        givenWithToken(createToken(EMPTY_GROUP))
                .get("/secured/noone")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void securedNoOneViewGroup() {
        givenWithToken(createToken("view"))
                .get("/secured/noone")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void securedNoOneAdminGroup() {
        givenWithToken(createToken("admin"))
                .get("/secured/noone")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void permittedCorrectToken() {
        givenWithToken(createToken(EMPTY_GROUP))
                .get("/permitted")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello there!"));
    }

    @Test
    public void permittedWrongIssuer() {
        givenWithToken(createToken(Invalidity.WRONG_ISSUER, EMPTY_GROUP))
                .get("/permitted")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED); // in Thorntail, this is 200, but both approaches are likely valid
    }

    @Test
    public void permittedWrongDate() {
        givenWithToken(createToken(Invalidity.WRONG_DATE, EMPTY_GROUP))
                .get("/permitted")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED); // in Thorntail, this is 200, but both approaches are likely valid
    }

    @Test
    public void permittedWrongKey() {
        givenWithToken(createToken(Invalidity.WRONG_KEY, EMPTY_GROUP))
                .get("/permitted")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED); // in Thorntail, this is 200, but both approaches are likely valid
    }

    @Test
    public void deniedCorrectToken() {
        givenWithToken(createToken(EMPTY_GROUP))
                .get("/denied")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void mixedConstrained() throws Exception {
        givenWithToken(createToken(EMPTY_GROUP))
                .get("/mixed/constrained")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Constrained method"));
    }

    @Test
    public void mixedUnconstrained() throws Exception {
        givenWithToken(createToken(EMPTY_GROUP))
                .get("/mixed/unconstrained")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN); // quarkus.security.deny-unannotated-members=true
    }

    @Test
    public void contentTypesPlainPlainGroup() throws Exception {
        givenWithToken(createToken("plain"))
                .accept(ContentType.TEXT)
                .get("/content-types")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, world!"));
    }

    @Test
    public void contentTypesPlainWebGroup() throws Exception {
        givenWithToken(createToken("web"))
                .accept(ContentType.TEXT)
                .get("/content-types")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void contentTypesWebWebGroup() throws Exception {
        givenWithToken(createToken("web"))
                .accept(ContentType.HTML)
                .get("/content-types")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("<html>Hello, world!</html>"));
    }

    @Test
    public void contentTypesWebPlainGroup() throws Exception {
        givenWithToken(createToken("plain"))
                .accept(ContentType.HTML)
                .get("/content-types")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void parameterizedPathsAdminAdminGroup() throws Exception {
        givenWithToken(createToken("admin"))
                .get("/parameterized-paths/my/foo/admin")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Admin accessed foo"));
    }

    @Test
    public void parameterizedPathsAdminViewGroup() throws Exception {
        givenWithToken(createToken("view"))
                .get("/parameterized-paths/my/foo/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void parameterizedPathsViewViewGroup() throws Exception {
        givenWithToken(createToken("view"))
                .get("/parameterized-paths/my/foo/view")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("View accessed foo"));
    }

    @Test
    public void parameterizedPathsViewAdminGroup() throws Exception {
        givenWithToken(createToken("admin"))
                .get("/parameterized-paths/my/foo/view")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void tokenExpirationGracePeriod() throws Exception {
        givenWithToken(createToken("admin"))
                .get("/secured/admin")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Restricted area! Admin access granted!"));
    }

    @DisplayName("Smallrye GraphQL - Route context termination test")
    @Test
    public void testRouteContextTermination() {
        // resource accesses a route context, takes a "sub" claim and sends it back
        // thus if resource accessed incorrect route context, we would recognize it
        Stream.of("USA", "Canada", "Australia").forEach(this::executeGraphQLRequest);
    }

    private void executeGraphQLRequest(String expectedSubClaim) {
        givenWithToken(createTokenWithSubClaim(expectedSubClaim))
                .body("{\"query\":\"{\nsubject\n}\",\"variables\":null}")
                .when().post("/graphql")
                .then()
                .statusCode(200)
                .body("data.subject", CoreMatchers.equalTo(expectedSubClaim));
    }

    protected abstract RequestSpecification givenWithToken(String token);

    private static String createToken(String group) {
        return createToken(null, group);
    }

    private static String createToken(Invalidity invalidity, String group) {
        return createToken(invalidity, group, null);
    }

    private static String createTokenWithSubClaim(String subClaim) {
        return createToken(null, EMPTY_GROUP, subClaim);
    }

    private static String createToken(Invalidity invalidity, String group, String subClaim) {
        return given()
                // create token with this "sub" claim
                .queryParam("subClaim", subClaim)
                .body(group)
                .when()
                .post("/login/jwt?invalidity=" + (Objects.isNull(invalidity) ? "" : invalidity.name())).then()
                .statusCode(200)
                .extract().body().asString();
    }

    @Test
    public void verifyRSAKeypairsGenerationOnFIPS() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        assertNotNull(keyPairGenerator.genKeyPair());
    }
}
