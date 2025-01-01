package io.prada.listener.service.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.prada.listener.config.BnbFUMLinks;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PositionRequester implements Requester {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final RequestHelper helper;

    @Override
    public RequestType type() {
        return RequestType.POSITION;
    }

    @SneakyThrows
    @Override
    public ObjectNode request() {
        HttpResponse<String> response = client.send(helper.build(BnbFUMLinks.positions), BodyHandlers.ofString());
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            ArrayNode array = helper.filterPositions(response.body());
            log.debug("positions = {}", array);
            return mapper.createObjectNode().set(type().name(), array);
        }
        log.error("Failed to get {} response: {}, {}", type(), response.statusCode(), response.body());
        return null;
    }
}
