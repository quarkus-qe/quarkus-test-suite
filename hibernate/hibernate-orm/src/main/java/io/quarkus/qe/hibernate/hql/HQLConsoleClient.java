package io.quarkus.qe.hibernate.hql;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class HQLConsoleClient {

    private final DevUIJsonRpcClient rpcClient;
    private final String persistenceUnit;

    public HQLConsoleClient(String testUrl, String persistenceUnit) {
        this.rpcClient = new DevUIJsonRpcClient("quarkus-hibernate-orm", testUrl);
        this.persistenceUnit = persistenceUnit;
    }

    public JsonNode executeQuery(String hql, int pageNumber, int pageSize) throws Exception {
        Map<String, Object> arguments = Map.of(
                "query", hql,
                "persistenceUnit", persistenceUnit,
                "pageNumber", pageNumber,
                "pageSize", pageSize);
        return rpcClient.executeJsonRPCMethod("executeHQL", arguments);
    }

    public void close() {
        rpcClient.close();
    }
}
