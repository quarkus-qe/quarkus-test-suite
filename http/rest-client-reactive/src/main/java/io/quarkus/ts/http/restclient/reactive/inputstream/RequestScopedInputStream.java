package io.quarkus.ts.http.restclient.reactive.inputstream;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.enterprise.context.RequestScoped;

import io.quarkus.runtime.BlockingOperationControl;
import io.quarkus.runtime.BlockingOperationNotAllowedException;

/**
 * The REST Client reads the stream on a worker thread, so the request context must be propagated
 * and the stream must never be read on the event loop.
 */
@RequestScoped
public class RequestScopedInputStream extends InputStream {

    public static final int MAX_ITERATIONS = 100;

    private final AtomicLong count = new AtomicLong();

    @Override
    public int read() {
        if (!BlockingOperationControl.isBlockingAllowed()) {
            throw new BlockingOperationNotAllowedException("The stream was read on an event loop thread");
        }
        if (count.incrementAndGet() <= MAX_ITERATIONS) {
            return 'A';
        }
        return -1;
    }
}
