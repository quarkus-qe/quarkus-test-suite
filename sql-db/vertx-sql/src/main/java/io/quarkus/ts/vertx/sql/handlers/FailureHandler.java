package io.quarkus.ts.vertx.sql.handlers;

import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolationException;

import io.quarkus.vertx.web.Route;
import io.vertx.core.VertxException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.db2client.DB2Exception;
import io.vertx.mssqlclient.MSSQLException;
import io.vertx.mysqlclient.MySQLException;
import io.vertx.pgclient.PgException;

@ApplicationScoped
public class FailureHandler {

    @Route(path = "*", type = Route.HandlerType.FAILURE, produces = "application/json")
    void databaseConstraintFailure(PgException e, HttpServerResponse response) {
        response.setStatusCode(400).end(Json.encode(new JsonObject().put("msg", e.getMessage())));
    }

    @Route(path = "*", type = Route.HandlerType.FAILURE, produces = "application/json")
    void databaseMysqlConstraintFailure(MySQLException e, HttpServerResponse response) {
        response.setStatusCode(400).end(Json.encode(new JsonObject().put("msg", e.getMessage())));
    }

    @Route(path = "*", type = Route.HandlerType.FAILURE, produces = "application/json")
    void databaseDb2ConstraintFailure(DB2Exception e, HttpServerResponse response) {
        response.setStatusCode(400).end(Json.encode(new JsonObject().put("msg", e.getMessage())));
    }

    @Route(path = "*", type = Route.HandlerType.FAILURE, produces = "application/json")
    void databaseMssqlConstraintFailure(MSSQLException e, HttpServerResponse response) {
        response.setStatusCode(400).end(Json.encode(new JsonObject().put("msg", e.getMessage())));
    }

    // TODO: https://github.com/quarkusio/quarkus/issues/24264 - No Oracle-specific exception available
    @Route(path = "/*", type = Route.HandlerType.FAILURE, produces = "application/json")
    void databaseOracleConstraintFailure(VertxException e, HttpServerResponse response) {
        if (e.getMessage().contains("Error Msg = ORA")) {
            response.setStatusCode(400).end(Json.encode(new JsonObject().put("msg", e.getMessage())));
        }
    }

    @Route(path = "*", type = Route.HandlerType.FAILURE, produces = "application/json")
    public void exceptions(ConstraintViolationException e, HttpServerResponse res) {
        res.setStatusCode(400).end(handler -> e.getConstraintViolations().stream()
                .map(err -> String.format("%s: %s", err.getPropertyPath().toString(), err.getMessage()))
                .collect(Collectors.joining("\n")));
    }

}
