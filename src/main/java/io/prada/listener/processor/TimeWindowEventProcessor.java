package io.prada.listener.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.prada.listener.dto.AccountResponse;
import io.prada.listener.dto.accounting.AccountingSnapshot;
import io.prada.listener.repository.EventRepository;
import io.prada.listener.repository.TradeInfoRepository;
import io.prada.listener.repository.model.EventEntity;
import io.prada.listener.repository.model.TradeInfoEntity;
import io.prada.listener.service.AccountingSnapshotBuilder;
import io.prada.listener.service.BinanceAccountDataRequester;
import io.prada.listener.service.EventDetectionService;
import io.prada.listener.service.SkipEventService;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeWindowEventProcessor {
    private static final Long WINDOW_MS = 1_000L;
    public static final String EVENTS = "events";

    private final ObjectMapper mapper;
    private final EventRepository eventRepository;
    private final SkipEventService skipEventService;
    private final TradeInfoRepository tradeInfoRepository;
    private final EventDetectionService eventDetectionService;
    private final BinanceAccountDataRequester accountDataRequester;
    private final AccountingSnapshotBuilder snapshotBuilder;

    private final Queue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean processingScheduled = new AtomicBoolean(false);

    @Value("${settings.logs.bnb-events}")
    private boolean logEvents;

    public void onMessage(String message) {
        boolean importantEvent = skipEventService.isImportantEvent(message);
        log.info("adding message to queue(?={}) {}.", importantEvent, message);
        if (!importantEvent) {
            return;
        }
        messageQueue.add(message);
        if (logEvents) {
            eventRepository.save(new EventEntity().setData(message));
        }
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

    @SneakyThrows
    private void doProcessBatch() {
        List<String> events = drainQueue();
        if (events.isEmpty()) {
            return;
        }
        String mergedEvent = mergeEvents(events);
        AccountResponse dto = accountDataRequester.collect();
        if (dto == null) {
            return; //need to know how often this happens before going further
        }
        AccountingSnapshot snapshot = snapshotBuilder.build(dto);
        log.info("event = {}, snapshot={}", mergedEvent, snapshot);
        tradeInfoRepository.save(new TradeInfoEntity().setData(mapper.writeValueAsString(snapshot)).setEvent(mergedEvent));

        eventDetectionService.analyze(snapshot, mergedEvent);
    }

    private List<String> drainQueue() {
        List<String> result = new ArrayList<>();
        String elem;
        while ((elem = messageQueue.poll()) != null) {
            result.add(elem);
        }
        return result;
    }

    @SneakyThrows
    private String mergeEvents(List<String> events) {
        ArrayNode jsonNodes = mapper.createArrayNode();
        for (String event : events) {
            jsonNodes.add(mapper.readTree(event));
        }
        return mapper.createObjectNode().set(EVENTS, jsonNodes).toString();
    }
}
