package io.prada.listener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.prada.listener.processor.TimeWindowEventProcessor;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

@Slf4j
public class UMFWebsocketClientImpl extends WebSocketClient {
    public static final String HAND_SHAKE = "onOpen";
    private final TimeWindowEventProcessor processor;

    public UMFWebsocketClientImpl(String url, TimeWindowEventProcessor processor) {
        super(URI.create(url));
        this.processor = processor;
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
    }

    @Override
    public void onError(Exception e) {
        log.error("WebSocket error: " + e.getMessage(), e);
    }

    private String helloWorld(ServerHandshake serverHandshake) {
        return new ObjectMapper().createObjectNode().put(HAND_SHAKE, serverHandshake.getHttpStatusMessage()).toString();
    }
}
