package io.prada.listener.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "telegram")
public class TelegramConfig {
    private String botToken;
    private String chatId;
}
