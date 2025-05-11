package hero;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/tls-certificate")
public class TlsCertificateClientResource {

    @RestClient
    TlsCertificateClient client;

    @Path("/certificate-info")
    @GET
    public CertificateInfo getCertificateInfo() {
        return client.getCertificateInfo();
    }

    @Path("/certificate-info-url-override")
    @GET
    public CertificateInfo getCertificateInfo(@RestQuery String url) {
        return client.getCertificateInfo(url);
    }
}
