package io.prada.listener.controller;

import io.prada.listener.dto.Signal;
import io.prada.listener.dto.accounting.AccountingSnapshot;
import io.prada.listener.service.EventDetectionService;
import io.prada.listener.service.SignalService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MainController {
    private final SignalService signalService;
    private final EventDetectionService eventDetectionService;

    @GetMapping("/signals")
    public List<Signal> signals() {
        return signalService.getLastSignals();
    }

    @GetMapping("/snapshot")
    public AccountingSnapshot snapshot() {
        return eventDetectionService.requestSnapShot();
    }
}
