package io.prada.listener.service.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.prada.listener.config.BinanceKeyConfig;
import io.prada.listener.testUtils.TestFileUtils;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class RequestHelperTest {
    final RequestHelper unit = new RequestHelper(buildBinanceConfig(), new ObjectMapper());

    @Test
    void build() {
        mockInstant(1735745430211L);
        String url = unit.build("fapi/v2/account?").uri().toString();
        Assertions.assertTrue(url.startsWith("""
            https://fapi.binance.com/fapi/v2/account?timestamp=1735745430211&signature="""));
        Assertions.assertTrue(url.endsWith("=6f6642d4f24cb7de96310f0155481ed66afa1e37833459e222c687507eb32081"));
    }

    @SneakyThrows
    @Test
    void filterBalance() {
        String content = TestFileUtils.load("data/request-helper/rawBalance.json");
        String expectedContent = TestFileUtils.load("data/request-helper/mappedCleanBalance.json");
        var processed = unit.filterBalance(content);
        var expected = new ObjectMapper().readTree(expectedContent).get(RequestType.BALANCE.name());
        Assertions.assertEquals(expected.toPrettyString(), processed.toPrettyString());
    }

    @SneakyThrows
    @Test
    void filterPositions() {
        String content = TestFileUtils.load("data/request-helper/rawPositions.json");
        String expectedContent = TestFileUtils.load("data/request-helper/mappedCleanPositions.json");
        var processed = unit.filterPositions(content);
        var expected = new ObjectMapper().readTree(expectedContent);
        Assertions.assertEquals(expected.toPrettyString(), processed.toPrettyString());
    }

    private BinanceKeyConfig buildBinanceConfig() {
        BinanceKeyConfig config = new BinanceKeyConfig();
        config.setPublicKey("PUBLIC");
        config.setPrivateKey("PRIVATE");
        return config;
    }

    private void mockInstant(long time) {
        var clock = Clock.fixed(Instant.ofEpochMilli(time), ZoneOffset.UTC);
        var mockedInstant = Instant.now(clock);
        MockedStatic<Instant> instantMockedStatic = Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS);
        instantMockedStatic.when(Instant::now).thenReturn(mockedInstant);
    }
}