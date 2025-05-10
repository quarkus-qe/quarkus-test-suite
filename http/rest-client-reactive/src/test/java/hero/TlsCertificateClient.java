package hero;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.rest.client.reactive.Url;

@RegisterRestClient(configKey = "tls-certificate-client")
@Path("/tls-certificate")
public interface TlsCertificateClient {

    @GET
    @Path("/certificate-info")
    CertificateInfo getCertificateInfo();

    @GET
    @Path("/certificate-info")
    CertificateInfo getCertificateInfo(@Url String url);

}
