package io.prada.listener.service.request;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.prada.listener.config.BinanceKeyConfig;
import io.prada.listener.config.BnbFUMLinks;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestHelper {
    private static final Long TIMEOUT_MS = 5_000L;
    private static final String CRYPTO_ALGORITHM = "HmacSHA256";
    private static final Predicate<JsonNode> NON_ZERO_AMOUNT = json ->
        new BigDecimal(json.get("positionAmt").asText()).compareTo(BigDecimal.ZERO) != 0;

    private final BinanceKeyConfig config;
    private final ObjectMapper mapper;

    public HttpRequest build(String middleName) {
        return HttpRequest.newBuilder()
            .uri(URI.create(BnbFUMLinks.bnbUF + middleName + sign()))
            .GET()
            .header(BnbFUMLinks.header, config.getPublicKey())
            .timeout(Duration.ofMillis(TIMEOUT_MS))
            .build();
    }

    public ObjectNode filterBalance(String jsonObj) {
        try {
            JsonNode node = mapper.readTree(jsonObj);
            ObjectNode result = node.deepCopy();
            result.set("assets", filter((ArrayNode) node.get("assets"), "walletBalance"));
            result.set("positions", filter((ArrayNode) node.get("positions"), "entryPrice"));
            return result;
        } catch (JacksonException e) {
            log.error("error when reading json={}.", jsonObj, e);
            return null;
        }
    }

    @SneakyThrows
    public ArrayNode filterPositions(String body) {
        ArrayNode array = (ArrayNode) mapper.readTree(body);
        ArrayNode result = mapper.createArrayNode();
        array.forEach(elem -> {
            if (NON_ZERO_AMOUNT.test(elem)) {
                result.add(elem);
            }
        });
        return result;
    }

    private String sign() {
        String timestamp = "timestamp=" + Instant.now().toEpochMilli();
        return timestamp + "&signature=" + sign(timestamp);
    }

    private String sign(String timestamp) {
        try {
            Mac sha256HMAC = Mac.getInstance(CRYPTO_ALGORITHM);
            sha256HMAC.init(new SecretKeySpec(config.getPrivateKey().getBytes(StandardCharsets.UTF_8), CRYPTO_ALGORITHM));
            return bytesToHex(sha256HMAC.doFinal(timestamp.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("Failed to generate HMAC SHA 256 signature.", e);
            return null;
        }
    }

    private String bytesToHex(byte[] array) {
        return IntStream.range(0, array.length)
            .mapToObj(i -> String.format("%02x", array[i]))
            .collect(Collectors.joining());
    }

    private ArrayNode filter(ArrayNode node, String key) {
        ArrayNode result = mapper.createArrayNode();
        StreamSupport.stream(node.spliterator(), false)
            .filter(elem -> new BigDecimal(elem.get(key).asText()).compareTo(BigDecimal.ZERO) > 0)
            .forEach(result::add);
        return result;
    }
}
