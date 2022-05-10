package io.quarkus.ts.security.jwt;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;

public abstract class BaseJwtSecurityIT {

    private static final Logger LOG = Logger.getLogger(BaseJwtSecurityIT.class);

    private static final int TEN = 10;
    private static final int NINETY = 90;

    @Test
    public void securedEveryoneNoGroup() throws Exception {
        givenWithToken(createToken())
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(
                        "Hello, test-subject@example.com, "
                                + "your token was issued by https://my.auth.server/ and you are in groups []"));
    }

    @Test
    public void securedEveryoneViewGroup() throws Exception {
        givenWithToken(createToken("view"))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(
                        "Hello, test-subject@example.com, "
                                + "your token was issued by https://my.auth.server/ and you are in groups [view]"));
    }

    @Test
    public void securedEveryoneAdminGroup() throws Exception {
        givenWithToken(createToken("admin"))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(
                        "Hello, test-subject@example.com, "
                                + "your token was issued by https://my.auth.server/ and you are in groups [admin, superuser]"));
    }

    @Test
    public void securedEveryoneWrongIssuer() throws Exception {
        givenWithToken(createToken(Invalidity.WRONG_ISSUER))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void securedEveryoneWrongDate() throws Exception {
        givenWithToken(createToken(Invalidity.WRONG_DATE))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void securedEveryoneWrongKey() throws Exception {
        givenWithToken(createToken(Invalidity.WRONG_KEY))
                .get("/secured/everyone")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void securedAdminNoGroup() throws Exception {
        givenWithToken(createToken())
                .get("/secured/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void securedAdminViewGroup() throws Exception {
        givenWithToken(createToken("view"))
                .get("/secured/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void securedAdminAdminGroup() throws Exception {
        givenWithToken(createToken("admin"))
                .get("/secured/admin")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Restricted area! Admin access granted!"));
    }

    @Test
    public void securedNoOneNoGroup() throws Exception {
        givenWithToken(createToken())
                .get("/secured/noone")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void securedNoOneViewGroup() throws Exception {
        givenWithToken(createToken("view"))
                .get("/secured/noone")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void securedNoOneAdminGroup() throws Exception {
        givenWithToken(createToken("admin"))
                .get("/secured/noone")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void permittedCorrectToken() throws Exception {
        givenWithToken(createToken())
                .get("/permitted")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello there!"));
    }

    @Test
    public void permittedWrongIssuer() throws Exception {
        givenWithToken(createToken(Invalidity.WRONG_ISSUER))
                .get("/permitted")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED); // in Thorntail, this is 200, but both approaches are likely valid
    }

    @Test
    public void permittedWrongDate() throws Exception {
        givenWithToken(createToken(Invalidity.WRONG_DATE))
                .get("/permitted")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED); // in Thorntail, this is 200, but both approaches are likely valid
    }

    @Test
    public void permittedWrongKey() throws Exception {
        givenWithToken(createToken(Invalidity.WRONG_KEY))
                .get("/permitted")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED); // in Thorntail, this is 200, but both approaches are likely valid
    }

    @Test
    public void deniedCorrectToken() throws Exception {
        givenWithToken(createToken())
                .get("/denied")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void mixedConstrained() throws Exception {
        givenWithToken(createToken())
                .get("/mixed/constrained")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Constrained method"));
    }

    @Test
    public void mixedUnconstrained() throws Exception {
        givenWithToken(createToken())
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

    private static RSAPrivateKey loadPrivateKey() throws Exception {
        String key = new String(Files.readAllBytes(Paths.get("target/test-classes/private-key.pem")), Charset.defaultCharset());

        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.decodeBase64(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    private enum Invalidity {
        WRONG_ISSUER,
        WRONG_DATE,
        WRONG_KEY
    }

    private static String createToken(String... groups) throws Exception {
        return createToken(Date::new, null, groups);
    }

    private static String createToken(Invalidity invalidity, String... groups) throws Exception {
        return createToken(Date::new, invalidity, groups);
    }

    private static String createToken(Supplier<Date> clock, Invalidity invalidity, String... groups)
            throws Exception {
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

        RSAPrivateKey privateKey = loadPrivateKey();
        if (invalidity == Invalidity.WRONG_KEY) {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();
        }

        return Jwt.issuer(issuer)
                .expiresAt(expiration.getTime())
                .issuedAt(now.getTime())
                .subject("test_subject_at_example_com")
                .groups(Set.of(groups))
                .claim("upn", "test-subject@example.com")
                .claim("roleMappings", Collections.singletonMap("admin", "superuser"))
                .jws().algorithm(SignatureAlgorithm.RS256).sign(privateKey);
    }

    @Test
    public void verifyRSAKeypairsGenerationOnFIPS() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        assertNotNull(keyPairGenerator.genKeyPair());
    }
}
