package io.prada.listener.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkipEventService {
    private static final String ACCOUNT_UPDATE_EVENT = "ACCOUNT_UPDATE";
    private static final String FUNDING_FEE_VALUE = "FUNDING_FEE";
    private static final String EVENT_TYPE_KEY = "e";
    private static final String EVENT_DETAILS_KEY = "a";
    private static final String EVENT_DETAILS_DESCRIPTION_KEY = "m";

    private final ObjectMapper mapper;

    public boolean isImportantEvent(String message) {
        try {
            JsonNode node = mapper.readTree(message);
            return !isFundingFee(node);
        } catch (JsonProcessingException e) {
            log.warn("failed to parse {} as json", message, e);
            return true;
        }
    }

    private boolean isFundingFee(JsonNode json) {
        if (!Objects.equals(ACCOUNT_UPDATE_EVENT,
                Optional.ofNullable(json.findValue(EVENT_TYPE_KEY)).map(JsonNode::asText).orElse(null))) {
            return false;
        }
        return Objects.equals(FUNDING_FEE_VALUE,
            Optional.ofNullable(json.findValue(EVENT_DETAILS_KEY))
                .map(j -> j.findValue(EVENT_DETAILS_DESCRIPTION_KEY))
                .map(JsonNode::asText).orElse(null));
    }
}
