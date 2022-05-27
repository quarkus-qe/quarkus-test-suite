package io.quarkus.ts.http.undertow.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
