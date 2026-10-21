package io.quarkus.ts.transactions;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import io.quarkus.test.logging.Log;
import io.vertx.core.AbstractVerticle;
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

    public SQLProxy(String backendHost, int backendPort, int proxyPort) {
        this.backendHost = backendHost;
        this.backendPort = backendPort;
        this.proxyPort = proxyPort;
    }

    @Override
    public void start() {
        proxyServer = vertx.createNetServer();
        backendClient = vertx.createNetClient();

        AtomicReference<NetSocket> backendSocketRef = new AtomicReference<>();
        AtomicReference<NetSocket> clientSocketRef = new AtomicReference<>();
        connectToDatabase(clientSocketRef, backendSocketRef);
        proxyServer.connectHandler(clientSocket -> {
            clientSocketRef.set(clientSocket);
            handleClientConnection(clientSocket, backendSocketRef);
        });

        proxyServer.listen(proxyPort, "localhost");
    }

    private void handleClientConnection(NetSocket clientSocket, AtomicReference<NetSocket> backendSocketRef) {
        setupProxying(clientSocket, backendSocketRef);
    }

    private void connectToDatabase(AtomicReference<NetSocket> clientSocketRef, AtomicReference<NetSocket> backendSocketRef) {
        backendClient.connect(backendPort, backendHost)
                .onSuccess(backendSocket -> {
                    backendSocketRef.set(backendSocket);

                    backendSocket.exceptionHandler(err -> {
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

        vertx.deployVerticle(proxy);

        // Give it a moment to start up
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

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
