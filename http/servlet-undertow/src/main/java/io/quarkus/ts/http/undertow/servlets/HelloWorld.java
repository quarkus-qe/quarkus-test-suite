package io.quarkus.ts.http.undertow.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

@ApplicationScoped
public class HelloWorld extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(HelloWorld.class);

    private static final String MESSAGE = "Hello World";

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        LOG.info(req.getSession().getId());
        PrintWriter writer = resp.getWriter();
        writer.write(MESSAGE);
        writer.close();
    }

}
