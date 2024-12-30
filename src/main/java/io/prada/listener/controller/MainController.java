package io.prada.listener.controller;

import io.prada.listener.service.ListenKeyHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MainController {
    private final ListenKeyHolder holder;

    @GetMapping("/key") //TODO: rm
    public String isKeyRunning() {
        return "key is enabled?=%s".formatted(holder.isKey());
    }
}
