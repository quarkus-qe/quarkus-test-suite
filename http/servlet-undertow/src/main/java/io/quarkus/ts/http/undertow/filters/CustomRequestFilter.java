package io.quarkus.ts.http.undertow.filters;

import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.jboss.logging.Logger;

@ApplicationScoped
public class CustomRequestFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(CustomRequestFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        LOG.info("LoggerFilter invoked.");
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
