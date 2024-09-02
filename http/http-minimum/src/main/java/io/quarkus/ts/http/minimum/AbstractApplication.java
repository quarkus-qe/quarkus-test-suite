package io.quarkus.ts.http.minimum;

//import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

// TODO uncomment when https://github.com/quarkusio/quarkus/issues/42963 is fixed
//@ApplicationPath("/rest")
public abstract class AbstractApplication extends Application {
    // Abstract jakarta.ws.rs.core.Application classes should be ignored
}
