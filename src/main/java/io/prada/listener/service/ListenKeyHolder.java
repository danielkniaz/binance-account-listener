package io.prada.listener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.prada.listener.config.BinanceKeyConfig;
import io.prada.listener.config.BnbFUMLinks;
import io.prada.listener.dto.BnbListenKey;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListenKeyHolder {
    private final BinanceKeyConfig binanceKeysConfig;
    private final HttpClient client;
    private final ObjectMapper mapper;

    private String key;

    public String generateListenKey() {
        if (Objects.isNull(key)) {
            key = requestKey();
        }
        return key;
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void refreshListenKey() {
        if (key == null) {
            return;
        }
        try {
            HttpResponse<String> response = client.send(buildRequest(HttpMethod.PUT), BodyHandlers.ofString());
            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                log.debug("Listen Key refreshed successfully.");
                return;
            }
            log.warn("Failed to refresh listen key: {}", response.body());
            key = null;
            generateListenKey();
            log.info("finished refreshing new key?");
        } catch (Exception e) {
            log.error("Error refreshing listen key", e);
        }
    }

    @SneakyThrows
    private String requestKey() {
        HttpResponse<String> response = client.send(buildRequest(HttpMethod.POST), BodyHandlers.ofString());
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            String listenKey = mapper.readValue(response.body(), BnbListenKey.class).getListenKey();
            log.info("Listen Key: {}", listenKey);
            return listenKey;
        }
        throw new RuntimeException("Failed to get listen key: " + response.body());
    }

    private HttpRequest buildRequest(HttpMethod method) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(BnbFUMLinks.bnbUF + BnbFUMLinks.listenKey))
            .header(BnbFUMLinks.header, binanceKeysConfig.getPublicKey());

        if (HttpMethod.POST == method) {
            requestBuilder.POST(HttpRequest.BodyPublishers.noBody());
        } else if (HttpMethod.PUT == method) {
            requestBuilder.PUT(HttpRequest.BodyPublishers.noBody());
        } else {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
        return requestBuilder.build();
    }
}