package io.quarkus.ts.transactions.recovery;

import static io.quarkus.ts.transactions.recovery.driver.CrashingXAResource.RECOVERY_SUBPATH;
import static io.quarkus.ts.transactions.recovery.driver.CrashingXAResource.TRANSACTION_LOGS_PATH;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.RestQuery;

import io.quarkus.arc.All;

import javax.sql.DataSource;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(TRANSACTION_LOGS_PATH)
public class TransactionLogsResource {

    private final DataSource dataSource;
    private final EnumMap<TransactionExecutor, TransactionRecoveryService> typeToSvc;

    public TransactionLogsResource(DataSource dataSource, @All List<TransactionRecoveryService> serviceList) {
        this.dataSource = dataSource;
        this.typeToSvc = new EnumMap<>(serviceList
                .stream()
                .collect(
                        Collectors.toMap(
                                TransactionRecoveryService::transactionExecutor,
                                Function.identity())));
    }

    @Path(RECOVERY_SUBPATH)
    @POST
    public int makeTransactionAndCrash(@RestQuery boolean rollback, @RestQuery TransactionExecutor executor) {
        return typeToSvc.get(executor).makeTransaction(rollback);
    }

    @POST
    public int makeTransaction(@RestQuery boolean rollback, @RestQuery TransactionExecutor executor) {
        return typeToSvc.get(executor).makeTransaction(rollback);
    }

    @Transactional
    @GET
    public int transactionCount() {
        return executeCountQuery("recovery_log");
    }

    @Path("/jdbc-object-store")
    @Transactional
    @GET
    public int jdbcObjectStoreCount() {
        return executeCountQuery("quarkus_qe_JBossTSTxTable");
    }

    @Transactional
    @DELETE
    public void deleteRecoveryLog() {
        // all transactions write into 'recovery_log' table
        try (var con = dataSource.getConnection()) {
            try (var st = con.createStatement()) {
                st.executeUpdate("DELETE FROM recovery_log");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int executeCountQuery(String tableName) {
        try (var con = dataSource.getConnection()) {
            try (var st = con.createStatement()) {
                var res = st.executeQuery("SELECT COUNT(*) FROM " + tableName);
                if (res.next()) {
                    return res.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
