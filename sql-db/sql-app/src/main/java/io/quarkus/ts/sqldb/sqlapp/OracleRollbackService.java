package io.quarkus.ts.sqldb.sqlapp;

import java.sql.Connection;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import javax.sql.DataSource;

@ApplicationScoped
public class OracleRollbackService {

    private static final Logger LOG = Logger.getLogger(OracleRollbackService.class);
    public static final String INSERT_EXECUTED_LOG = "INSERT executed, blocking to simulate long-running operation";
    private static final String INSERT = "INSERT INTO rollback_test (value) VALUES ('should-be-rolled-back')";

    @Inject
    DataSource dataSource;

    @Transactional
    public void insertAndBlock() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            conn.prepareStatement(INSERT).executeUpdate();
            LOG.info(INSERT_EXECUTED_LOG);
            Thread.sleep(30_000);
        }
    }
}
