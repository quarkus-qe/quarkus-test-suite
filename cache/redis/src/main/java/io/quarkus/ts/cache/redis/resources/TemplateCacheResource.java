package io.quarkus.ts.cache.redis.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateException;

@Path("template")
public class TemplateCacheResource {

    @Inject
    Template cached;

    /**
     * Check for remote cache with Qute template. Qute should not use remote cache.
     * See https://github.com/quarkusio/quarkus/issues/35680#issuecomment-1711153725
     *
     * @return Should return error contains `not supported for remote caches`
     */
    @GET
    @Path("error")
    public String getQuteTemplate() {
        try {
            return cached.render();
        } catch (TemplateException e) {
            return e.getCause().getMessage();
        }
    }

}
