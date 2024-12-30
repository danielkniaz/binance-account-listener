package io.prada.listener;

import io.prada.listener.service.ListenKeyHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Launcher {
    private final ListenKeyHolder holder;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        String key = holder.generateListenKey();
        log.debug("listen key = {}", key);
    }
}
