package io.prada.listener.service;

import io.prada.listener.dto.Signal;
import io.prada.listener.repository.SignalRepository;
import io.prada.listener.repository.model.SignalEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignalService {
    private final SignalRepository repository;

    public void process(List<Signal> signals) {
        log.debug("signals = {}", signals);
        repository.saveAll(signals.stream().map(this::toEntity).toList());
    }

    public List<Signal> getLastSignals() {
        return repository.findTopByIdDescLimit10().stream().map(this::toSignal).toList();
    }

    //TODO: add lib to compile converter
    private Signal toSignal(SignalEntity entity) {
        return new Signal()
            .setSymbol(entity.getSymbol())
            .setAction(entity.getAction())
            .setType(entity.getType())
            .setDirection(entity.getDirection())
            .setIn(entity.isIn())
            .setOut(entity.isOut())
            .setPrice(entity.getPrice())
            .setRelativePrice(entity.getRelativePrice())
            .setRisk(entity.getRisk());
    }

    private SignalEntity toEntity(Signal signal) {
        return new SignalEntity()
            .setSymbol(signal.getSymbol())
            .setAction(signal.getAction())
            .setType(signal.getType())
            .setDirection(signal.isIn() ? signal.getDirection() : 0)
            .setIn(signal.isIn())
            .setOut(signal.isOut())
            .setPrice(signal.getPrice())
            .setRelativePrice(signal.getRelativePrice())
            .setRisk(signal.getRisk());
    }

}
