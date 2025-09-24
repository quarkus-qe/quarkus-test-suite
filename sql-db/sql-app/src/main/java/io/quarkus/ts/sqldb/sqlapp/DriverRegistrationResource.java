package io.quarkus.ts.sqldb.sqlapp;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/drivers")
public class DriverRegistrationResource {

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_PLAIN)
    public String listDrivers() {
        String variableToChange = "initial value";
        List<String> drivers = new ArrayList<>();
        Enumeration<Driver> driverEnum = DriverManager.getDrivers();

        while (driverEnum.hasMoreElements()) {
            Driver driver = driverEnum.nextElement();
            drivers.add(driver.getClass().getName());
        }
        return String.join("\n", drivers);
    }

}
