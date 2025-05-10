package hero;

import jakarta.inject.Inject;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import io.smallrye.common.annotation.Identifier;

@Path("/tls-certificate")
public class TlsCertificateUpdateResource {

    @Identifier("tls-certificate-client")
    @Inject
    InMemoryClientKeyStoreProvider keyStoreProvider;

    @Path("/update")
    @PUT
    public void update(CertificateReloadDto certificateReloadDto) {
        keyStoreProvider.updateKeyStore(certificateReloadDto.tlsKey(), certificateReloadDto.tlsCert());
    }
}
