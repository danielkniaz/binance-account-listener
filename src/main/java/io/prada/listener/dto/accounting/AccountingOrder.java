package io.prada.listener.dto.accounting;

import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.AllOrdersResponseInner;
import io.prada.listener.dto.enums.SideType;
import java.math.BigDecimal;
import java.math.MathContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class AccountingOrder {

    private Long orderId;
    private String symbol;
    private String status;
    private String clientOrderId;
    private BigDecimal price;
    private BigDecimal avgPrice;
    private BigDecimal originalQty;
    private BigDecimal executedQty;
    private String timeInForce;
    private String type;
    private boolean reduceOnly;
    private boolean closePosition;
    private SideType side;
    private String positionSide;
    private BigDecimal stopPrice;
    private String workingType;
    private boolean priceProtect;
    private String priceMatch;
    private String selfTradePreventionMode;
    private Long goodTillDate;
    private Long time;
    private Long updateTime;

    public boolean isEntry() {
        return !isExit();
    }
    public boolean isExit() {
        return closePosition;
    }
    public boolean isLimit() {
        return "LIMIT".equals(type) || "TAKE_PROFIT_MARKET".equals(type);
    }
    public boolean isStop() {
        return this.type.startsWith("STOP");
    }
    public int direction() {
        return this.side.getDir();
    }

    public AccountingOrder(AllOrdersResponseInner entity, MathContext ctx) {
        this.orderId = entity.getOrderId();
        this.symbol = entity.getSymbol();
        this.status = entity.getStatus();
        this.clientOrderId = entity.getClientOrderId();
        this.price = new BigDecimal(entity.getPrice(), ctx);
        this.avgPrice = new BigDecimal(entity.getAvgPrice(), ctx);
        this.originalQty = new BigDecimal(entity.getOrigQty(), ctx);
        this.executedQty = new BigDecimal(entity.getExecutedQty(), ctx);
        this.timeInForce = entity.getTimeInForce();
        this.type = entity.getType();
        this.reduceOnly = entity.getReduceOnly();
        this.closePosition = entity.getClosePosition();
        this.side = SideType.of(entity.getSide());
        this.positionSide = entity.getPositionSide();
        this.stopPrice = new BigDecimal(entity.getStopPrice(), ctx);
        this.workingType = entity.getWorkingType();
        this.priceProtect = entity.getPriceProtect();
        this.priceMatch = entity.getPriceMatch();
        this.selfTradePreventionMode = entity.getSelfTradePreventionMode();
        this.goodTillDate = entity.getGoodTillDate();
        this.time = entity.getTime();
        this.updateTime = entity.getUpdateTime();
    }
}
