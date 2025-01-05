package io.prada.listener.dto.accounting;

import static java.math.BigDecimal.ZERO;

import com.fasterxml.jackson.databind.JsonNode;
import io.prada.listener.dto.enums.SideType;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountingOrder {
    private static final MathContext ctx = new MathContext(8, RoundingMode.HALF_EVEN);

    private Long orderId;
    private String symbol;
    private String status;
    private String clientOrderId;
    private BigDecimal price;
    private BigDecimal avgPrice;
    private BigDecimal originalQty;
    private BigDecimal executedQty;
    private BigDecimal cumulativeQty;
    private String timeInForce;
    private String type;
    private boolean reduceOnly;
    private boolean closePosition;
    private SideType side;
    private String positionSide;
    private BigDecimal stopPrice;
    private String workingType;
    private boolean priceProtect;
    private String originalType;
    private String priceMatch;
    private String selfTradePreventionMode;
    private Long goodTillDate;
    private Long time;
    private Long updateTime;

    public AccountingOrder(JsonNode node) {
        this.orderId = node.get("orderId").asLong();
        this.symbol = node.get("symbol").asText();
        this.status = node.get("status").asText();
        this.clientOrderId = node.get("clientOrderId").asText();
        this.price = new BigDecimal(node.get("price").asText(), ctx);
        this.avgPrice = new BigDecimal(node.get("avgPrice").asText(), ctx);
        this.originalQty = new BigDecimal(node.get("origQty").asText(), ctx);
        this.executedQty = new BigDecimal(node.get("executedQty").asText(), ctx);
        this.cumulativeQty = new BigDecimal(node.get("cumQuote").asText(), ctx);
        this.timeInForce = node.get("timeInForce").asText();
        this.type = node.get("type").asText();
        this.reduceOnly = node.get("reduceOnly").asBoolean();
        this.closePosition = node.get("closePosition").asBoolean();
        this.side = SideType.of(node.get("side").asText());
        this.positionSide = node.get("positionSide").asText();
        this.stopPrice = new BigDecimal(node.get("stopPrice").asText(), ctx);
        this.workingType = node.get("workingType").asText();
        this.priceProtect = node.get("priceProtect").asBoolean();
        this.originalType = node.get("origType").asText();
        this.priceMatch = node.get("priceMatch").asText();
        this.selfTradePreventionMode = node.get("selfTradePreventionMode").asText();
        this.goodTillDate = node.get("goodTillDate").asLong();
        this.time = node.get("time").asLong();
        this.updateTime = node.get("updateTime").asLong();
    }

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
    
    public AccountingOrder(AccountingOrder other) {
        this.orderId = other.getOrderId();
        this.symbol = other.getSymbol();
        this.status = other.getStatus();
        this.clientOrderId = other.getClientOrderId();
        this.price = Optional.ofNullable(other.getPrice()).map(ZERO::add).orElse(null);
        this.originalQty = Optional.ofNullable(other.getOriginalQty()).map(ZERO::add).orElse(null);
        this.executedQty = Optional.ofNullable(other.getExecutedQty()).map(ZERO::add).orElse(null);
        this.cumulativeQty = Optional.ofNullable(other.getCumulativeQty()).map(ZERO::add).orElse(null);
        this.avgPrice = Optional.ofNullable(other.getAvgPrice()).map(ZERO::add).orElse(null);
        this.timeInForce = other.getTimeInForce();
        this.type = other.getType();
        this.reduceOnly = other.isReduceOnly();
        this.closePosition = other.isClosePosition();
        this.side = other.getSide();
        this.positionSide = other.getPositionSide();
        this.stopPrice = Optional.ofNullable(other.getStopPrice()).map(ZERO::add).orElse(null);
        this.workingType = other.getWorkingType();
        this.priceProtect = other.isPriceProtect();
        this.originalType = other.getOriginalType();
        this.priceMatch = other.getPriceMatch();
        this.selfTradePreventionMode = other.getSelfTradePreventionMode();
        this.goodTillDate = other.getGoodTillDate();
        this.time = other.getTime();
        this.updateTime = other.getUpdateTime();
    }
}
