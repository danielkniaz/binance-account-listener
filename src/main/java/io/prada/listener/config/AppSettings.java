package io.prada.listener.config;

import io.prada.listener.dto.enums.BalanceType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "settings")
public class AppSettings {
    private BalanceType balanceType;
}
