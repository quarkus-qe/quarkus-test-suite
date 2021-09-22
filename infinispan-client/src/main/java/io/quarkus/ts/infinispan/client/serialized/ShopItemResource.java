package io.quarkus.ts.infinispan.client.serialized;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.dsl.QueryResult;

import io.quarkus.infinispan.client.Remote;

@Path("/items")
public class ShopItemResource {

    @Inject
    @Remote("myshop")
    RemoteCache<String, ShopItem> cache_items;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ShopItem createItem(ShopItem item, @QueryParam("lifespan") String lifespan,
            @QueryParam("maxIdleTime") String maxIdleTime) {
        if (!cache_items.containsValue(item)) {
            String key = item.getTitle().replaceAll("\\s+", "").toLowerCase();
            if (lifespan != null && maxIdleTime != null) {
                cache_items.put(key, item,
                        Integer.valueOf(lifespan), TimeUnit.SECONDS, Integer.valueOf(maxIdleTime), TimeUnit.SECONDS);
            } else if (lifespan != null && maxIdleTime == null) {
                cache_items.put(key, item,
                        Integer.valueOf(lifespan), TimeUnit.SECONDS);
            } else {
                cache_items.put(key, item);
            }
        }
        return item;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ShopItem> listItems(@QueryParam("query") String query) {
        QueryFactory qf = org.infinispan.client.hotrod.Search.getQueryFactory(cache_items);
        Query<ShopItem> searchQuery;
        if (query != null) {
            searchQuery = qf.create(query);
        } else {
            searchQuery = qf.create("from quarkus_qe.ShopItem");
        }
        QueryResult<ShopItem> queryResult = searchQuery.execute();
        return queryResult.list();
    }

    @GET
    @Path("/clear-cache")
    @Produces(MediaType.APPLICATION_JSON)
    public void clearCache() {
        cache_items.clear();
    }
}
