package io.quarkus.ts.http.minimum;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/minmax")
public class MinMaxResource {

    @GET
    @Path("record/{min}/{max}")
    public MinMaxRecord recordMinMax(MinMaxRecord params) {
        return params;
    }

    @GET
    @Path("class/{min}/{max}")
    public MinMaxClass classMinMax(MinMaxClass params) {
        return params;
    }

    @GET
    @Path("method/{min}/{max}")
    public String methodMinMax(@PathParam("min") String min,
            @PathParam("max") String max) {
        return min + " - " + max;
    }

    public record MinMaxRecord(
            @PathParam("min") String min,
            @PathParam("max") String max) {
    }

    public static class MinMaxClass {
        @PathParam("min")
        String min;
        @PathParam("max")
        String max;

        public String getMin() {
            return min;
        }

        public void setMin(String min) {
            this.min = min;
        }

        public String getMax() {
            return max;
        }

        public void setMax(String max) {
            this.max = max;
        }

        @Override
        public String toString() {
            return "MinMax{" +
                    "min='" + min + '\'' +
                    ", max='" + max + '\'' +
                    '}';
        }
    }
}
