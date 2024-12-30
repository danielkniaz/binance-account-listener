package io.prada.listener.service;

import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

@Slf4j
public class UMFWebsocketClientImpl extends WebSocketClient {

    public UMFWebsocketClientImpl(String url) {
        super(URI.create(url));
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("onOpen...");
    }

    @Override
    public void onMessage(String message) {
        log.info("onMessage: {}", message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("WebSocket closed with code={}, isRemote={}, reason: {}", code, remote, reason);
    }

    @Override
    public void onError(Exception e) {
        log.error("WebSocket error: " + e.getMessage(), e);
    }
}
