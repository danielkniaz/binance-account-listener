package io.prada.listener.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.prada.listener.processor.TimeWindowEventProcessor;
import io.prada.listener.service.ListenKeyHolder;
import io.prada.listener.service.UMFWSHolder;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
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
