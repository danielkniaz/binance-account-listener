package io.prada.listener.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.prada.listener.processor.TimeWindowEventProcessor;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

@Slf4j
public class UMFWebsocketClientImpl extends WebSocketClient {
    private static final String HAND_SHAKE = "onOpen";
    private static final Integer RETRY_BACKOFF_MS = 5_000;
    private static final Integer RETRY_ATTEMPTS = 5;

    private final TimeWindowEventProcessor processor;
    private final AtomicBoolean isShutdown;

    public UMFWebsocketClientImpl(String url, TimeWindowEventProcessor processor) {
        super(URI.create(url));
        this.processor = processor;
        this.isShutdown = new AtomicBoolean(false);
    }

    public void shutdown() {
        isShutdown.set(true);
        try {
            this.closeBlocking();
            log.info("ws connection closed gracefully.");
        } catch (InterruptedException e) {
            log.error("shutdown interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    public static boolean isOpen(JsonNode jsonNode) {
        return jsonNode.get(UMFWebsocketClientImpl.HAND_SHAKE) != null;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        processor.onMessage(helloWorld(serverHandshake));
    }

    @Override
    public void onMessage(String message) {
        processor.onMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket closed with code={}, isRemote={}, reason: {}", code, remote, reason);
        if (!isShutdown.get()) {
            retryConnect();
        }
    }

    @Override
    public void onError(Exception e) {
        log.error("WebSocket error: " + e.getMessage(), e);
    }

    private String helloWorld(ServerHandshake serverHandshake) {
        return new ObjectMapper().createObjectNode().put(HAND_SHAKE, serverHandshake.getHttpStatusMessage()).toString();
    }

    private void retryConnect() {
        for (int i = 0; i < RETRY_ATTEMPTS; i ++) {
            try {
                this.reconnectBlocking();
                log.info("reconnected");
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("reconnection interrupted ", e);
            } catch (Exception e) {
                log.error("reconnect attempt {} failed. ", i, e);
                try {
                    Thread.sleep(RETRY_BACKOFF_MS);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
    }
}
