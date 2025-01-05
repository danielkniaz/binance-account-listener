package io.prada.listener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.prada.listener.dto.Signal;
import io.prada.listener.dto.accounting.AccountingSnapshot;
import io.prada.listener.processor.AccountDiffProcessor;
import io.prada.listener.processor.TimeWindowEventProcessor;
import io.prada.listener.service.publisher.MessagePublisher;
import io.prada.listener.service.request.RequestType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventDetectionService {
    private final ObjectMapper mapper;
    private final AccountingSnapshotBuilder builder;
    private final AccountDiffProcessor processor;
    private final SignalService signalService;
    private final List<MessagePublisher> publishers;

    private AccountingSnapshot snapshot;

    public AccountingSnapshot requestSnapShot() {
        return builder.copy(snapshot);
    }

    public void analyze(List<Pair<RequestType, ObjectNode>> input, String messages) {
        AccountingSnapshot now = null;
        try {
            now = builder.build(input);
            if (isOnConnect(messages)) {
                log.debug("skipping initial message");
                this.snapshot = now;
                return;
            }
            List<Signal> signals = processor.diff(now, snapshot);
            signals = processor.suppress(signals);
            if (!signals.isEmpty()) {
                signalService.process(signals);
                signals.forEach(this::sendSignal);
            }
        } catch (Exception e) {
            log.warn("exception {}", e.getMessage(), e);
        } finally {
            this.snapshot = now;
        }
    }

    @SneakyThrows
    private boolean isOnConnect(String message) {
        ArrayNode json = (ArrayNode) mapper.readTree(message).get(TimeWindowEventProcessor.EVENTS);
        return json.size() == 1 && UMFWebsocketClientImpl.isOpen(json.get(0));
    }

    @SneakyThrows
    private void sendSignal(Signal signal) {
        for (MessagePublisher publisher : publishers) {
            if (publisher.isEnabled()) {
                publisher.send(mapper.writeValueAsString(signal));
            }
        }
    }
}
