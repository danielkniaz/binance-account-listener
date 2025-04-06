package io.prada.listener.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "telegram")
public class TelegramConfig {
    private String botToken;
    private String chatId;
}
