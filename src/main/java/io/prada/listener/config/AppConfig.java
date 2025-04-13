package io.prada.listener.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.prada.listener.processor.TimeWindowEventProcessor;
import io.prada.listener.service.ListenKeyHolder;
import io.prada.listener.service.socket.UMFWSHolder;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    public MathContext context() {
        return new MathContext(8, RoundingMode.HALF_EVEN);
    }

    @Bean
    public UMFWSHolder umfwsHolder(TimeWindowEventProcessor processor, ListenKeyHolder holder) {
        UMFWSHolder result = new UMFWSHolder(processor, holder);
        result.init();
        return result;
    }
}
