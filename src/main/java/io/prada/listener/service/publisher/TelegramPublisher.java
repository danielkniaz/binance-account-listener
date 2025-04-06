package io.prada.listener.service.publisher;

import io.prada.listener.config.TelegramConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@ConditionalOnProperty(prefix = "settings.publisher", name = "telegram", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class TelegramPublisher implements MessagePublisher {
    private static final String URL = "https://api.telegram.org/bot%s/sendMessage";

    private final TelegramConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void send(String message) {
        try {
            String url = URL.formatted(config.getBotToken());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("chat_id", config.getChatId());
            map.add("text", message);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            String response = restTemplate.postForObject(url, request, String.class);
            log.debug("response = {} for message = {}", response, message);
        } catch (Exception e) {
            log.error("error sending message: {}", e.getMessage(), e);
        }
    }
}
