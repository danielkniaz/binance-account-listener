package io.prada.listener.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeWindowEventProcessor {
    private static final Long WINDOW_MS = 1_000L;
    private static final String EVENTS = "events";

    private final ObjectMapper mapper;

    private final Queue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean processingScheduled = new AtomicBoolean(false);

    public void onMessage(String message) {
        log.info("adding message to queue {}", message);
        messageQueue.add(message);
        if (processingScheduled.compareAndSet(false, true)) {
            executor.schedule(this::batch, WINDOW_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void batch() {
        try {
            doProcessBatch();
        } finally {
            processingScheduled.set(false);
        }
    }

    private void doProcessBatch() {
        List<String> events = drainQueue();
        if (events.isEmpty()) {
            return;
        }
        String mergedEvent = mergeEvents(events);
        log.info("merged event={}", mergedEvent);
    }

    private List<String> drainQueue() {
        List<String> result = new ArrayList<>();
        String elem;
        while ((elem = messageQueue.poll()) != null) {
            result.add(elem);
        }
        return result;
    }

    private String mergeEvents(List<String> events) {
        ArrayNode jsonNodes = mapper.createArrayNode();
        for (String event : events) {
            try {
                jsonNodes.add(mapper.readTree(event));
            } catch (JsonProcessingException e) {
                log.warn("skipping event={}",event);
            }
        }
        return mapper.createObjectNode().set(EVENTS, jsonNodes).toString();
    }
}
