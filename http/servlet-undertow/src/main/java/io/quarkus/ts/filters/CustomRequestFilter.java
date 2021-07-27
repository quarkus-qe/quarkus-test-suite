package io.quarkus.ts.filters;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

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
