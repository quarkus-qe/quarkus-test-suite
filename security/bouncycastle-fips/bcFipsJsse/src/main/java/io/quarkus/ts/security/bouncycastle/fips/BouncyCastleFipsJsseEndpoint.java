package io.quarkus.ts.security.bouncycastle.fips;

import java.security.Security;
import java.security.Signature;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/api")
public class BouncyCastleFipsJsseEndpoint {
    @GET
    @Path("/listProviders")
    public String listProviders() {
        return Arrays.asList(Security.getProviders()).stream()
                .map(p -> p.getName()).collect(Collectors.joining(","));
    }

    @GET
    @Path("/SHA256withRSAandMGF1")
    public String checkSHA256withRSAandMGF1() throws Exception {
        // This algorithm name is only supported with BC, Java (11+) equivalent is `RSASSA-PSS`
        Signature.getInstance("SHA256withRSAandMGF1", "BCFIPS");
        return "success";
    }
}
