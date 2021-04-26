package io.quarkus.ts.messaging.artemisjta;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Session;
import javax.transaction.Transactional;

import org.jboss.logging.Logger;

@ApplicationScoped
public class ProducerService {

    private static final Logger LOG = Logger.getLogger(ProducerService.class.getName());

    @Inject
    ConnectionFactory connectionFactory;

    @Transactional
    public void produceCustomPrice(String customPrice, boolean fail) {
        try (JMSContext context = connectionFactory.createContext(Session.SESSION_TRANSACTED)) {
            context.setAutoStart(true);
            context.acknowledge();
            JMSProducer p = context.createProducer();
            p.send(context.createQueue("custom-prices-1"), customPrice);
            LOG.info(customPrice + " sent to queue custom-prices-1");
            if (fail) {
                throw new IllegalStateException("Bad hair day");
            }
            p.send(context.createQueue("custom-prices-2"), customPrice);
            context.commit();
            LOG.info(customPrice + " sent to queue custom-prices-2");
        }
    }

    public void produceCustomPriceNoJTA(String customPrice, boolean fail) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            context.createProducer().send(context.createQueue("custom-prices-1"), customPrice);
            LOG.info(customPrice + " sent to queue custom-prices-1");
            if (fail) {
                throw new IllegalStateException("Bad hair day");
            }
            context.createProducer().send(context.createQueue("custom-prices-2"), customPrice);
            LOG.info(customPrice + " sent to queue custom-prices-2");
        }
    }

    public void produceClientAck(String customPrice) {
        try (JMSContext context = connectionFactory.createContext(Session.CLIENT_ACKNOWLEDGE)) {
            context.createProducer().send(context.createQueue("custom-prices-cack"), customPrice);
        }
    }
}
