package io.prada.listener.service;

import com.binance.connector.client.derivatives_trading_usds_futures.rest.api.DerivativesTradingUsdsFuturesRestApi;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.AccountInformationV2Response;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.AllOrdersResponseInner;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.PositionInformationV2ResponseInner;
import io.prada.listener.dto.AccountResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinanceAccountDataRequester {
    private static final Long RECV_WINDOW_MS = 5000L;
    private static final String ALL_SYMBOLS = "";

    private final DerivativesTradingUsdsFuturesRestApi api;

    public AccountResponse collect() {
        try {
            AccountInformationV2Response account = api.accountInformationV2(RECV_WINDOW_MS).getData();
            List<PositionInformationV2ResponseInner> positions = api.positionInformationV2(ALL_SYMBOLS, RECV_WINDOW_MS)
                .getData().stream().toList();
            List<AllOrdersResponseInner> orders = api.currentAllOpenOrders(ALL_SYMBOLS, RECV_WINDOW_MS)
                .getData().stream().toList();
            return new AccountResponse(account, positions, orders);
        } catch (Exception e) {
            log.error("error collecting data from the exchange.", e);
            return null;
        }
    }
}
