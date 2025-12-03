package io.quarkus.ts.security.form.authn.csrf;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@Path("/csrf")
public class CsrfResource {
    @Inject
    Template csrfToken;

    @GET
    @Path("/csrfTokenForm")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getCsrfTokenForm() {
        return csrfToken.instance();
    }

    @POST
    @Path("/csrfTokenForm")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public String postCsrfTokenForm(@FormParam("name") String userName) {
        return userName;
    }
}
