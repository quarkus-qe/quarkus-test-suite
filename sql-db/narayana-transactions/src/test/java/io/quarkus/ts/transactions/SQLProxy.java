package io.quarkus.ts.transactions;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import io.quarkus.test.logging.Log;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

public class SQLProxy extends AbstractVerticle {

    private final String backendHost;
    private final int backendPort;
    private final int proxyPort;
    private final AtomicLong validationQueryDelayMs = new AtomicLong(0);
    private final ArrayDeque<Buffer> databaseBuffers = new ArrayDeque<>();
    private NetServer proxyServer;
    private NetClient backendClient;
    private Vertx vertxInstance;
    private boolean delayFirst = false;
    private AtomicReference<NetSocket> backendSocketRef;
    private AtomicReference<NetSocket> clientSocketRef;

    public SQLProxy(String backendHost, int backendPort, int proxyPort) {
        this.backendHost = backendHost;
        this.backendPort = backendPort;
        this.proxyPort = proxyPort;
    }

    @Override
    public void start() {
        proxyServer = vertx.createNetServer();
        backendClient = vertx.createNetClient();

        backendSocketRef = new AtomicReference<>();
        clientSocketRef = new AtomicReference<>();
        proxyServer.connectHandler(clientSocket -> {
            clientSocketRef.set(clientSocket);
            setupProxying(clientSocket, backendSocketRef);
        });

        proxyServer.listen(proxyPort, "localhost");
    }

    public boolean isConnectedToDatabase() {
        return backendSocketRef.get() != null;
    }

    public void connectToDatabase() {
        backendClient.connect(backendPort, backendHost)
                .onSuccess(backendSocket -> {
                    backendSocketRef.set(backendSocket);

                    backendSocket.exceptionHandler(err -> {
                        Log.error("Exception in SQL proxy backend socket: " + err.getMessage());
                        var clientSocket = clientSocketRef.get();
                        if (clientSocket != null) {
                            clientSocket.close();
                        }
                    });

                    backendSocket.closeHandler(v -> {
                        var clientSocket = clientSocketRef.get();
                        if (clientSocket != null) {
                            clientSocket.close();
                        }
                    });

                    // Forward data from backend to client (responses)
                    backendSocket.handler(buffer -> {
                        var clientSocket = clientSocketRef.get();
                        if (clientSocket != null) {
                            clientSocket.write(buffer);
                        } else if (!buffer.toString().contains("timeout")) {
                            databaseBuffers.offer(buffer);
                            Log.info("Adding database message to a queue: " + buffer);
                        }
                    });
                })
                .onFailure(err -> {
                    Log.error("Failed to connect to backend database " + backendHost + ":" + backendPort + ": "
                            + err.getMessage());
                    var clientSocket = clientSocketRef.get();
                    if (clientSocket != null) {
                        clientSocket.close();
                    }
                });
    }

    private void setupProxying(NetSocket clientSocket, AtomicReference<NetSocket> backendSocketRef) {
        // Forward data from client to backend
        clientSocket.handler(buffer -> {
            if (isValidationQuery(buffer) || delayFirst) {
                delayFirst = false;
                handleValidationQuery(buffer, backendSocketRef.get());
            } else {
                // Forward all traffic transparently (especially during handshake)
                backendSocketRef.get().write(buffer);
            }
        });

        // Handle socket closures
        clientSocket.closeHandler(v -> {
            backendSocketRef.get().close();
        });

        // Handle errors
        clientSocket.exceptionHandler(err -> {
            Log.error("Exception in SQL proxying: " + err.getMessage());
            backendSocketRef.get().close();
        });

        for (Buffer databaseBuffer : databaseBuffers) {
            clientSocket.write(databaseBuffer);
        }
    }

    private boolean isValidationQuery(Buffer buffer) {
        return buffer.toString().contains("SELECT 1");
    }

    private void handleValidationQuery(Buffer buffer, NetSocket backendSocket) {
        long delay = validationQueryDelayMs.get();
        if (delay > 0) {
            vertx.setTimer(delay, id -> {
                backendSocket.write(buffer);
            });
        } else {
            backendSocket.write(buffer);
        }
    }

    @Override
    public void stop() {
        if (proxyServer != null) {
            proxyServer.close();
        }
        if (backendClient != null) {
            backendClient.close();
        }
    }

    // Control methods for testing
    public void setValidationQueryDelay(long delayMs) {
        this.validationQueryDelayMs.set(delayMs);
    }

    // Static helper methods for easy usage in tests
    public static SQLProxy start(String backendHost, int backendPort, int proxyPort) {
        Vertx vertx = Vertx.vertx();
        SQLProxy proxy = new SQLProxy(backendHost, backendPort, proxyPort);
        proxy.vertxInstance = vertx;

        Future<String> proxyDeployment = vertx.deployVerticle(proxy);
        proxyDeployment.onSuccess(deploymentId -> Log.info("SQL proxy successfully deployed. ID " + deploymentId));
        proxyDeployment.onFailure(err -> Log.error("SQL proxy failed to deploy: " + err.getMessage()));

        return proxy;
    }

    public void shutdown() {
        if (vertxInstance != null) {
            vertxInstance.close();
        }
    }

    public void delayFirst(boolean delayFirst) {
        this.delayFirst = delayFirst;
    }
}
