package io.quarkus.ts.messaging.artemisjta;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ConsumerService {

    private static final int CONSUMER_TIMEOUT = 500;
    private static final Logger LOG = Logger.getLogger(ConsumerService.class.getName());

    @Inject
    ConnectionFactory connectionFactory;

    public String readPriceOne() {
        return receiveMessagesInQueue("custom-prices-1");
    }

    public String readPriceTwo() {
        return receiveMessagesInQueue("custom-prices-2");
    }

    public String receiveAndAck(boolean ackIt) throws JMSException {
        try (JMSContext context = connectionFactory.createContext(Session.CLIENT_ACKNOWLEDGE);
                JMSConsumer consumer = context.createConsumer(context.createQueue("custom-prices-cack"))) {
            Message message = consumer.receiveNoWait();
            String messageBody = (message != null) ? message.getBody(String.class) : "";
            if (ackIt) {
                context.acknowledge();
            }
            return messageBody;
        }
    }

    private String receiveMessagesInQueue(String queueName) {
        String price = "";
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE);
                JMSConsumer consumer = context.createConsumer(context.createQueue(queueName))) {
            Message message = consumer.receive(CONSUMER_TIMEOUT);
            if (message != null) {
                price = message.getBody(String.class);
                LOG.info("Price set to " + price + " from " + queueName);
            } else {
                LOG.info("Nothing to see in queue " + queueName);
            }
        } catch (JMSException ex) {
            LOG.error("Error reading queue. ", ex);
        }

        return price;
    }
}
