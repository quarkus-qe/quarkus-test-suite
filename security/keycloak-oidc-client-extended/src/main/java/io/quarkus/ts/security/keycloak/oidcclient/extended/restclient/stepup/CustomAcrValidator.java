package io.quarkus.ts.security.keycloak.oidcclient.extended.restclient.stepup;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.jwt.Claims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.jwt.consumer.Validator;

import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.TenantFeature;
import io.quarkus.oidc.common.runtime.OidcConstants;
import io.quarkus.security.AuthenticationFailedException;

@Unremovable
@ApplicationScoped
@TenantFeature("custom-validator")
public class CustomAcrValidator implements Validator {

    private static final String REQUIRED_ACR_GOLD = "gold";
    private static final String REQUIRED_ACR_PLATINUM = "platinum";

    @Override
    public String validate(JwtContext jwtContext) throws MalformedClaimException {
        var jwtClaims = jwtContext.getJwtClaims();
        var acrClaimName = Claims.acr.name();

        if (!jwtClaims.hasClaim(acrClaimName)) {
            throw new AuthenticationFailedException(
                    "Token missing ACR claim",
                    Map.of(OidcConstants.ACR_VALUES, REQUIRED_ACR_GOLD + "," + REQUIRED_ACR_PLATINUM));
        }

        List<String> acrClaimValues;
        if (jwtClaims.isClaimValueStringList(acrClaimName)) {
            acrClaimValues = jwtClaims.getStringListClaimValue(acrClaimName);
        } else if (jwtClaims.isClaimValueString(acrClaimName)) {
            String singleAcr = jwtClaims.getStringClaimValue(acrClaimName);
            acrClaimValues = Collections.singletonList(singleAcr);
        } else {
            throw new MalformedClaimException("Claim '" + acrClaimName + "' is not a String or List of Strings.");
        }

        if (acrClaimValues.contains(REQUIRED_ACR_GOLD) && acrClaimValues.contains(REQUIRED_ACR_PLATINUM)) {
            return null;
        }

        throw new AuthenticationFailedException(
                "Token does not contain required ACR values: gold and platinum",
                Map.of(OidcConstants.ACR_VALUES, REQUIRED_ACR_GOLD + "," + REQUIRED_ACR_PLATINUM));
    }
}
