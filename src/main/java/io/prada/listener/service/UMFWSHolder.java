package io.prada.listener.service;

import io.prada.listener.config.BnbFUMLinks;
import io.prada.listener.processor.TimeWindowEventProcessor;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class UMFWSHolder {
    private final TimeWindowEventProcessor processor;
    private final ListenKeyHolder holder;

    private UMFWebsocketClientImpl client;

    public void init() {
        client = new UMFWebsocketClientImpl(BnbFUMLinks.wss + holder.generateListenKey(), this);
        client.connect();
        log.info("ws client opened successfully");
    }

    @PreDestroy
    public void shutdown() {
        log.info("shutting down...");
        client.shutdown();
        log.info("client is closed");
    }

    public void onMessage(String message) {
        processor.onMessage(message);
    }

    public void onError(Exception e) {
        log.info("error received", e);
        try {
            client.close();
        } catch (Exception ex) {
            log.error("error closing existing connection ", ex);
        } finally {
            init();
        }
    }
}
