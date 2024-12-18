package io.quarkus.ts.http.minimum.reactive;

import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;

import org.jboss.logging.Logger;

@Provider
@ApplicationScoped
public class JakartaRestInterceptor implements ReaderInterceptor {

    private static final Logger LOG = Logger.getLogger(JakartaRestInterceptor.class);

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context)
            throws IOException, WebApplicationException {
        LOG.info("Before reading " + context.getGenericType());
        Object entity = context.proceed();
        LOG.info("After reading " + entity);
        return entity;
    }
}
