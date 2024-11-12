package io.quarkus.ts.http.undertow.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

import io.vertx.ext.web.RoutingContext;

@ServletSecurity(@HttpConstraint(rolesAllowed = { "pablo", "gonzalez", "granados" }))
@WebServlet(name = "SecuredWorldServlet", urlPatterns = "/secured", initParams = {
        @WebInitParam(name = "message", value = "A secured message") })
@ApplicationScoped
public class SecuredWorld extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(SecuredWorld.class);

    @Inject
    RoutingContext routingContext;

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        LOG.info(req.getSession().getId());
        PrintWriter writer = resp.getWriter();
        writer.write(routingContext.queryParam("secured-servlet-key").get(0));
        writer.close();
    }

}
