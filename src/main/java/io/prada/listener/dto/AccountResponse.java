package io.prada.listener.dto;

import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.AccountInformationV2Response;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.AllOrdersResponseInner;
import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.PositionInformationV2ResponseInner;
import java.util.List;

public record AccountResponse(
    AccountInformationV2Response accountInfo,
    List<PositionInformationV2ResponseInner> positions,
    List<AllOrdersResponseInner> orders
) {
}
