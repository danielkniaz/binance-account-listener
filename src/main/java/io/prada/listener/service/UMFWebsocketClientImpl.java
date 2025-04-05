package io.prada.listener.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

@Slf4j
public class UMFWebsocketClientImpl extends WebSocketClient {
    private static final String HAND_SHAKE = "onOpen";

    private final AtomicBoolean isShutdown;
    private final UMFWSHolder wsHolder;

    public UMFWebsocketClientImpl(String url, UMFWSHolder holder) {
        super(URI.create(url));
        this.isShutdown = new AtomicBoolean(false);
        this.wsHolder = holder;
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
        wsHolder.onMessage(helloWorld(serverHandshake));
    }

    @Override
    public void onMessage(String message) {
        wsHolder.onMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket closed with code={}, isRemote={}, reason: {}", code, remote, reason);
        if (!isShutdown.get()) {
            wsHolder.onError(new RuntimeException(reason));
        }
    }

    @Override
    public void onError(Exception e) {
        log.error("WebSocket error: " + e.getMessage(), e);
        if (!isShutdown.get()) {
            wsHolder.onError(e);
        }
    }

    private String helloWorld(ServerHandshake serverHandshake) {
        return new ObjectMapper().createObjectNode().put(HAND_SHAKE, serverHandshake.getHttpStatusMessage()).toString();
    }
}
