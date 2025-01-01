package io.prada.listener.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.prada.listener.repository.EventRepository;
import io.prada.listener.repository.TradeInfoRepository;
import io.prada.listener.repository.model.EventEntity;
import io.prada.listener.repository.model.TradeInfoEntity;
import io.prada.listener.service.SkipEventService;
import io.prada.listener.service.request.RequestType;
import io.prada.listener.service.request.Requester;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeWindowEventProcessor {
    private static final Long WINDOW_MS = 1_000L;
    private static final String EVENTS = "events";

    private final ObjectMapper mapper;
    private final EventRepository eventRepository;
    private final SkipEventService skipEventService;
    private final List<Requester> requesters;
    private final TradeInfoRepository tradeInfoRepository;

    private final Queue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean processingScheduled = new AtomicBoolean(false);

    @Value("${settings.logs.bnb-events}")
    private boolean logEvents;
    @Value("${settings.logs.bnb-responses}")
    private boolean logResponses;
    @Value("${settings.use-position-request}")
    private boolean requestPositions;

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

    private void doProcessBatch() {
        List<String> events = drainQueue();
        if (events.isEmpty()) {
            return;
        }
        String mergedEvent = mergeEvents(events);
        buildResponses(mergedEvent);
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

    private List<TradeInfoEntity> buildResponses(String mergedEvent) {
        log.info("merged event={}", mergedEvent);
        List<TradeInfoEntity> responses = requestersList().stream()
            .map(this::buildNode)
            .filter(Objects::nonNull)
            .map(pair -> buildLog(pair, mergedEvent))
            .filter(Objects::nonNull)
            .toList();
        if (logResponses) {
            return tradeInfoRepository.saveAll(responses);
        }
        responses.forEach(e -> log.info("entity={}", e));
        return responses;
    }

    private List<Requester> requestersList() {
        if (requestPositions) {
            return requesters;
        }
        return requesters.stream().filter(req -> req.type() != RequestType.POSITION).toList();
    }

    private Pair<RequestType, ObjectNode> buildNode(Requester requester) {
        ObjectNode value = requester.request();
        return Objects.isNull(value) ? null : Pair.of(requester.type(), value);
    }

    private TradeInfoEntity buildLog(Pair<RequestType, ObjectNode> pair, String event) {
        try {
            return new TradeInfoEntity()
                .setData(mapper.writeValueAsString(pair.getSecond()))
                .setType(pair.getFirst())
                .setEvent(event);
        } catch (JsonProcessingException e) {
            log.warn("cannot save json {}", pair.getSecond(), e);
            return null;
        }
    }
}
