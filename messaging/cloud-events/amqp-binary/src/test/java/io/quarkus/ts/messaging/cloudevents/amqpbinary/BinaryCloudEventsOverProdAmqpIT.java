package io.quarkus.ts.messaging.cloudevents.amqpbinary;

import io.quarkus.test.bootstrap.AmqService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.AmqContainer;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.test.services.containers.model.AmqProtocol;

@QuarkusScenario
public class BinaryCloudEventsOverProdAmqpIT extends BaseBinaryCloudEventsOverAmqpIT {
    @AmqContainer(image = "${amqbroker.image}", protocol = AmqProtocol.AMQP)
    static AmqService amq = new AmqService();

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("amqp-host", amq::getAmqpHost)
            .withProperty("amqp-port", () -> "" + amq.getPort());
}
