package hero;

import java.util.random.RandomGenerator;

import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.tls.TlsConfigurationRegistry;

@Path("/api/heroes/random")
public class HeroResource {

    @GET
    public Hero getRandomHero() {
        long random = RandomGenerator.getDefault().nextLong();
        return new Hero(random, "Name-" + random, "Other-" + random, 1, "placeholder", "root");
    }

    void observer(@Observes StartupEvent ev, TlsConfigurationRegistry registry) {
        try {
            var ts = registry.get("cert-serving-test-server").get().getTrustStore();
            ts.aliases().asIterator().forEachRemaining(alias -> {
                System.out.println("hero server alias is " + alias);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
