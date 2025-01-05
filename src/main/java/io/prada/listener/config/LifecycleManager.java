package io.prada.listener.config;

import io.prada.listener.service.UMFWebsocketClientImpl;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LifecycleManager {
    private final UMFWebsocketClientImpl websocketClient;

    @PreDestroy
    public void shutdown() {
        websocketClient.shutdown();
    }
}
