package io.quarkus.ts.security.jwt;

import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public abstract class BaseJwtSecurityIT {

    private static final int TEN = 10;
    private static final int NINETY = 90;

    @Test
    public void securedEveryoneNoGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken())
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(
                        "Hello, test-subject@example.com, "
                                + "your token was issued by https://my.auth.server/ and you are in groups []"));
    }

    @Test
    public void securedEveryoneViewGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("view"))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(
                        "Hello, test-subject@example.com, "
                                + "your token was issued by https://my.auth.server/ and you are in groups [view]"));
    }

    @Test
    public void securedEveryoneAdminGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("admin"))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(
                        "Hello, test-subject@example.com, "
                                + "your token was issued by https://my.auth.server/ and you are in groups [admin, superuser]"));
    }

    @Test
    public void securedEveryoneWrongIssuer() throws IOException, GeneralSecurityException {
        givenWithToken(createToken(Invalidity.WRONG_ISSUER))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void securedEveryoneWrongDate() throws IOException, GeneralSecurityException {
        givenWithToken(createToken(Invalidity.WRONG_DATE))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void securedEveryoneWrongKey() throws IOException, GeneralSecurityException {
        givenWithToken(createToken(Invalidity.WRONG_KEY))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void securedAdminNoGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken())
                .get("/secured/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void securedAdminViewGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("view"))
                .get("/secured/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void securedAdminAdminGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("admin"))
                .get("/secured/admin")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Restricted area! Admin access granted!"));
    }

    @Test
    public void securedNoOneNoGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken())
                .get("/secured/noone")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void securedNoOneViewGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("view"))
                .get("/secured/noone")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void securedNoOneAdminGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("admin"))
                .get("/secured/noone")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void permittedCorrectToken() throws IOException, GeneralSecurityException {
        givenWithToken(createToken())
                .get("/permitted")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello there!"));
    }

    @Test
    public void permittedWrongIssuer() throws IOException, GeneralSecurityException {
        givenWithToken(createToken(Invalidity.WRONG_ISSUER))
                .get("/permitted")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED); // in Thorntail, this is 200, but both approaches are likely valid
    }

    @Test
    public void permittedWrongDate() throws IOException, GeneralSecurityException {
        givenWithToken(createToken(Invalidity.WRONG_DATE))
                .get("/permitted")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED); // in Thorntail, this is 200, but both approaches are likely valid
    }

    @Test
    public void permittedWrongKey() throws IOException, GeneralSecurityException {
        givenWithToken(createToken(Invalidity.WRONG_KEY))
                .get("/permitted")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED); // in Thorntail, this is 200, but both approaches are likely valid
    }

    @Test
    public void deniedCorrectToken() throws IOException, GeneralSecurityException {
        givenWithToken(createToken())
                .get("/denied")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void mixedConstrained() throws IOException, GeneralSecurityException {
        givenWithToken(createToken())
                .get("/mixed/constrained")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Constrained method"));
    }

    @Test
    public void mixedUnconstrained() throws IOException, GeneralSecurityException {
        givenWithToken(createToken())
                .get("/mixed/unconstrained")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN); // quarkus.security.deny-unannotated-members=true
    }

    @Test
    public void contentTypesPlainPlainGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("plain"))
                .accept(ContentType.TEXT)
                .get("/content-types")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, world!"));
    }

    @Test
    public void contentTypesPlainWebGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("web"))
                .accept(ContentType.TEXT)
                .get("/content-types")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void contentTypesWebWebGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("web"))
                .accept(ContentType.HTML)
                .get("/content-types")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("<html>Hello, world!</html>"));
    }

    @Test
    public void contentTypesWebPlainGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("plain"))
                .accept(ContentType.HTML)
                .get("/content-types")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void parameterizedPathsAdminAdminGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("admin"))
                .get("/parameterized-paths/my/foo/admin")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Admin accessed foo"));
    }

    @Test
    public void parameterizedPathsAdminViewGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("view"))
                .get("/parameterized-paths/my/foo/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void parameterizedPathsViewViewGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("view"))
                .get("/parameterized-paths/my/foo/view")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("View accessed foo"));
    }

    @Test
    public void parameterizedPathsViewAdminGroup() throws IOException, GeneralSecurityException {
        givenWithToken(createToken("admin"))
                .get("/parameterized-paths/my/foo/view")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void tokenExpirationGracePeriod() throws IOException, GeneralSecurityException {
        Supplier<Date> clock = () -> {
            Date now = new Date();
            now = new Date(now.getTime() - TimeUnit.SECONDS.toMillis(NINETY));
            return now;
        };

        givenWithToken(createToken(clock, null, "admin"))
                .get("/secured/admin")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Restricted area! Admin access granted!"));
    }

    protected abstract RequestSpecification givenWithToken(String token);

    private static PrivateKey loadPrivateKey() throws IOException, GeneralSecurityException {
        byte[] bytes = Files.readAllBytes(Paths.get("target/test-classes/private-key.der"));
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    private enum Invalidity {
        WRONG_ISSUER,
        WRONG_DATE,
        WRONG_KEY
    }

    private static String createToken(String... groups) throws IOException, GeneralSecurityException {
        return createToken(Date::new, null, groups);
    }

    private static String createToken(Invalidity invalidity, String... groups) throws IOException, GeneralSecurityException {
        return createToken(Date::new, invalidity, groups);
    }

    private static String createToken(Supplier<Date> clock, Invalidity invalidity, String... groups)
            throws IOException, GeneralSecurityException {
        String issuer = "https://my.auth.server/";
        if (invalidity == Invalidity.WRONG_ISSUER) {
            issuer = "https://wrong/";
        }

        Date now = clock.get();
        Date expiration = new Date(TimeUnit.SECONDS.toMillis(TEN) + now.getTime());
        if (invalidity == Invalidity.WRONG_DATE) {
            now = new Date(now.getTime() - TimeUnit.DAYS.toMillis(TEN));
            expiration = new Date(now.getTime() - TimeUnit.DAYS.toMillis(TEN));
        }

        PrivateKey privateKey = loadPrivateKey();
        if (invalidity == Invalidity.WRONG_KEY) {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = keyPair.getPrivate();
        }

        return Jwts.builder()
                .setIssuer(issuer) // iss
                .setId(UUID.randomUUID().toString()) // jti
                .setExpiration(expiration) // exp
                .setIssuedAt(now) // iat
                .setSubject("test_subject_at_example_com") // sub
                .claim("upn", "test-subject@example.com")
                .claim("groups", Arrays.asList(groups))
                .claim("roleMappings", Collections.singletonMap("admin", "superuser"))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
