package io.quarkus.ts.security.jwt;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;

@Path("/login")
public class GenerateJwtResource {

    public enum Invalidity {
        WRONG_ISSUER,
        WRONG_DATE,
        WRONG_KEY
    }

    private static final String DEFAULT_ISSUER = "https://my.auth.server/";
    private static final int TEN = 10;

    @POST
    @Path("/jwt")
    @PermitAll
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String login(@QueryParam("invalidity") String invalidity, String body, @QueryParam("subClaim") String subClaim)
            throws NoSuchAlgorithmException {
        Date now = new Date();
        Date expiration = new Date(TimeUnit.SECONDS.toMillis(TEN) + now.getTime());
        String issuer = DEFAULT_ISSUER;
        if (invalidity.equalsIgnoreCase(Invalidity.WRONG_ISSUER.name())) {
            issuer = "https://wrong/";
        }

        if (invalidity.equalsIgnoreCase(Invalidity.WRONG_DATE.name())) {
            now = new Date(now.getTime() - TimeUnit.DAYS.toMillis(TEN));
            expiration = new Date(now.getTime() - TimeUnit.DAYS.toMillis(TEN));
        }

        PrivateKey privateKey = null;
        if (invalidity.equalsIgnoreCase(Invalidity.WRONG_KEY.name())) {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = keyPair.getPrivate();
        }

        JwtClaimsBuilder jwtbuilder = Jwt.issuer(issuer)
                .expiresAt(expiration.getTime())
                .issuedAt(now.getTime())
                .subject(Objects.requireNonNullElse(subClaim, "test_subject_at_example_com"))
                .groups(Set.of(body))
                .claim("upn", "test-subject@example.com")
                .claim("roleMappings", Collections.singletonMap("admin", "superuser"));

        if (!Objects.isNull(privateKey)) {
            return jwtbuilder.jws().sign(privateKey);
        }

        return jwtbuilder.sign();
    }
}
