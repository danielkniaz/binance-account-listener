package io.prada.listener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.prada.listener.dto.Signal;
import io.prada.listener.dto.accounting.AccountingSnapshot;
import io.prada.listener.processor.AccountDiffProcessor;
import io.prada.listener.processor.TimeWindowEventProcessor;
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
                return;
            }
            List<Signal> signals = processor.diff(now, snapshot);
            signals = processor.suppress(signals);
            signalService.process(signals);
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
}
