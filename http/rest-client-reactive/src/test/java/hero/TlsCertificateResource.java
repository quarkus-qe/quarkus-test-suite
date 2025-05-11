package hero;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;

import io.quarkus.logging.Log;
import io.vertx.ext.web.RoutingContext;

import javax.net.ssl.SSLPeerUnverifiedException;

@Path("/tls-certificate")
public class TlsCertificateResource {

    public static final String HERO_CLIENT_CN = "hero-client";
    public static final String HERO_CLIENT_CN_2 = "hero-client-2";

    @GET
    @Path("certificate-info")
    public CertificateInfo getCertificateInfo(@Context RoutingContext routingContext) throws SSLPeerUnverifiedException {
        var sslSession = routingContext.request().sslSession();
        for (Certificate peerCertificate : sslSession.getPeerCertificates()) {
            X509Certificate certificate = (X509Certificate) peerCertificate;
            String principalName = certificate.getSubjectX500Principal().getName();
            if (isExpectedClientCnValue(principalName)) {
                Log.debug("Found match. Principal name is " + principalName + ". Peer certificate: " + peerCertificate);
                return new CertificateInfo(principalName, certificate.getSerialNumber());
            }
        }
        throw new RuntimeException("Client peer certificate not found. Available certificates: "
                + Arrays.toString(sslSession.getPeerCertificates()));
    }

    private static boolean isExpectedClientCnValue(String principalName) {
        // 'client.namespace.svc' (where the 'client' is K8 service name) is expected CN for the OpenShift
        // and 'hero-client' for the bare-metal
        return principalName.startsWith("CN=client.") || principalName.startsWith("CN=hero-client");
    }
}
