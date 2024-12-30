package io.prada.listener.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "bnb-config")
public class BinanceKeyConfig {
    private String publicKey;
    private String privateKey;
}
