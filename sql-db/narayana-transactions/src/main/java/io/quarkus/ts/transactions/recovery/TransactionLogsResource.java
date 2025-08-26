package io.quarkus.ts.transactions.recovery;

import static io.quarkus.ts.transactions.recovery.driver.CrashingXAResource.RECOVERY_SUBPATH;
import static io.quarkus.ts.transactions.recovery.driver.CrashingXAResource.TRANSACTION_LOGS_PATH;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Named;
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
import io.quarkus.datasource.common.runtime.DataSourceUtil;
import io.quarkus.datasource.common.runtime.DatabaseKind;
import io.quarkus.datasource.runtime.DataSourcesBuildTimeConfig;
import io.quarkus.runtime.StartupEvent;

import javax.sql.DataSource;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path(TRANSACTION_LOGS_PATH)
public class TransactionLogsResource {

    private final DataSource dataSource;
    private final EnumMap<TransactionExecutor, TransactionRecoveryService> typeToSvc;
    /**
     * For Oracle we use 2 separate databases because there is Oracle-specific optimization in place which detects that
     * the database is same even though we have 2 different data sources and they recognize that 2-phase commit is not
     * necessary.
     */
    private final DataSource xaDataSource2;

    public TransactionLogsResource(DataSource dataSource, @All List<TransactionRecoveryService> serviceList,
            @Named("xa-ds-2") DataSource xaDataSource2, DataSourcesBuildTimeConfig dataSourcesBuildTimeConfig) {
        this.dataSource = dataSource;
        this.typeToSvc = new EnumMap<>(serviceList
                .stream()
                .collect(
                        Collectors.toMap(
                                TransactionRecoveryService::transactionExecutor,
                                Function.identity())));
        boolean isOracleTest = dataSourcesBuildTimeConfig.dataSources().get(DataSourceUtil.DEFAULT_DATASOURCE_NAME)
                .dbKind().map(DatabaseKind::isOracle).orElse(false);
        this.xaDataSource2 = isOracleTest ? xaDataSource2 : null;
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
        int result = executeCountQuery("recovery_log");
        if (xaDataSource2 != null) {
            result += executeCountQuery("recovery_log", xaDataSource2);
        }
        return result;
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
        deleteRecoveryLog(dataSource);
        if (xaDataSource2 != null) {
            deleteRecoveryLog(xaDataSource2);
        }
    }

    void createRecoveryTableIfNecessary(@Observes StartupEvent event) {
        if (xaDataSource2 != null) {
            try (var con = xaDataSource2.getConnection()) {
                try (var statement = con.createStatement()) {
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS recovery_log (id INT)");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private int executeCountQuery(String tableName) {
        return executeCountQuery(tableName, dataSource);
    }

    private static int executeCountQuery(String tableName, DataSource dataSource) {
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

    private static void deleteRecoveryLog(DataSource dataSource) {
        try (var con = dataSource.getConnection()) {
            try (var st = con.createStatement()) {
                st.executeUpdate("DELETE FROM recovery_log");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
