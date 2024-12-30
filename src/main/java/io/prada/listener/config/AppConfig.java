package io.prada.listener.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.prada.listener.service.ListenKeyHolder;
import io.prada.listener.service.UMFWebsocketClientImpl;
import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public UMFWebsocketClientImpl umfWebsocketClient(ListenKeyHolder holder) {
        String url = BnbFUMLinks.wss + holder.generateListenKey();
        return new UMFWebsocketClientImpl(url);
    }
}
