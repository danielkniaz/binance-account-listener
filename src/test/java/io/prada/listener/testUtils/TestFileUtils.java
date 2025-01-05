package io.prada.listener.testUtils;

import io.prada.listener.config.AppSettings;
import io.prada.listener.dto.enums.BalanceType;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;

@UtilityClass
public class TestFileUtils {

    @SneakyThrows
    public String load(String path) {
        return new String(new ClassPathResource(path).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public AppSettings appSettings(BalanceType type) {
        AppSettings settings = new AppSettings();
        settings.setBalanceType(type);
        return settings;
    }
}
