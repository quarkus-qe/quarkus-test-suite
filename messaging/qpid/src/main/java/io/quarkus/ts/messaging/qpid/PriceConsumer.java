package io.quarkus.ts.messaging.qpid;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.Session;

/**
 * A bean consuming prices from the JMS queue.
 * https://quarkus.io/guides/jms#qpid-jms-amqp.
 */
@ApplicationScoped
public class PriceConsumer {

    @Inject
    ConnectionFactory connectionFactory;

    public String getLastPrice() {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE);
                JMSConsumer consumer = context.createConsumer(context.createTopic("prices"))) {
            while (true) {
                Message message = consumer.receive();
                if (message == null) {
                    // receive returns `null` if the JMSConsumer is closed
                    return "";
                }

                return message.getBody(String.class);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
