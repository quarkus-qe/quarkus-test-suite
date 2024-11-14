package io.quarkus.ts.security.bouncycastle.fips.jsse;

import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.util.Arrays;
import java.util.stream.Collectors;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/api")
public class BouncyCastleFipsJsseEndpoint {
    @GET
    @Path("/listProviders")
    public String listProviders() {
        return Arrays.stream(Security.getProviders())
                .map(Provider::getName).collect(Collectors.joining(","));
    }

    @GET
    @Path("/SHA256withRSAandMGF1")
    public String checkSHA256withRSAandMGF1() throws Exception {
        // This algorithm name is only supported with BC, Java (11+) equivalent is `RSASSA-PSS`
        Signature.getInstance("SHA256withRSAandMGF1", "BCFIPS");
        return "success";
    }
}
