package hero;

import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.tls.TlsConfigurationRegistry;

@Path("hero-client-resource")
public class HeroClientResource {

    @RestClient
    HeroClient heroClient;

    @GET
    public Hero triggerClientToServerCommunication() {
        return heroClient.getRandomHero();
    }

    void observer(@Observes StartupEvent ev, TlsConfigurationRegistry registry) {
        try {
            var ts = registry.get("hero-client").get().getTrustStore();
            if (ts == null) {
                System.out.println("ts is null...");
            }
            ts.aliases().asIterator().forEachRemaining(alias -> {
                System.out.println("hero client alias is " + alias);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
