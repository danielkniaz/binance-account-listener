package io.prada.listener.dto.accounting;

import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountingPosition {
    private static final MathContext ctx = new MathContext(8, RoundingMode.HALF_EVEN);

    private String symbol;
    private BigDecimal positionAmt;
    private BigDecimal entryPrice;

    private BigDecimal breakEvenPrice;
    private BigDecimal markPrice;
    private BigDecimal unRealizedProfit;
    private BigDecimal liquidationPrice;
    private Integer leverage;
    private Long maxNotionalValue;
    private String marginType;
    private BigDecimal isolatedMargin;
    private String isAutoAddMargin;
    private String positionSide;
    private BigDecimal notional;
    private BigDecimal isolatedWallet;
    private Long updateTime;
    private boolean isolated;
    private int adlQuantile;

    public AccountingPosition(JsonNode node) {
        this.symbol = node.get("symbol").asText();
        this.positionAmt = new BigDecimal(node.get("positionAmt").asText(), ctx);
        this.entryPrice = new BigDecimal(node.get("entryPrice").asText(), ctx);
        this.breakEvenPrice = new BigDecimal(node.get("breakEvenPrice").asText(), ctx);
        this.markPrice = new BigDecimal(ofNullable(node.get("markPrice")).map(JsonNode::asText).orElse("0"), ctx);
        this.unRealizedProfit = new BigDecimal(ofNullable(node.get("unRealizedProfit")).map(JsonNode::asText)
            .orElse(node.get("unrealizedProfit").asText()), ctx);

        this.liquidationPrice = new BigDecimal(ofNullable(node.get("liquidationPrice")).map(JsonNode::asText).orElse("0"), ctx);
        this.leverage = node.get("leverage").asInt();
        this.maxNotionalValue = ofNullable(node.get("maxNotionalValue")).orElse(node.get("maxNotional")).asLong();

        this.marginType = ofNullable(node.get("marginType")).map(JsonNode::asText).orElse("");
        this.isolatedMargin = new BigDecimal(ofNullable(node.get("isolatedMargin")).map(JsonNode::asText).orElse("0"), ctx);
        this.isAutoAddMargin = ofNullable(node.get("isAutoAddMargin")).map(JsonNode::asText).orElse("");

        this.positionSide = node.get("positionSide").asText();
        this.notional = new BigDecimal(node.get("notional").asText(), ctx);
        this.isolatedWallet = new BigDecimal(node.get("isolatedWallet").asText(), ctx);
        this.updateTime = node.get("updateTime").asLong();
        this.isolated = node.get("isolated").asBoolean();
        this.adlQuantile = ofNullable(node.get("adlQuantile")).map(JsonNode::asInt).orElse(0);
    }

    public AccountingPosition(AccountingPosition other) {
        this.symbol = other.getSymbol();
        this.positionAmt = other.positionAmt.add(ZERO);
        this.entryPrice = other.entryPrice.add(ZERO);
        this.breakEvenPrice = Optional.ofNullable(other.getBreakEvenPrice()).map(ZERO::add).orElse(null);
        this.markPrice = Optional.ofNullable(other.getMarkPrice()).map(ZERO::add).orElse(null);
        this.unRealizedProfit = Optional.ofNullable(other.getUnRealizedProfit()).map(ZERO::add).orElse(null);
        this.liquidationPrice = Optional.ofNullable(other.getLiquidationPrice()).map(ZERO::add).orElse(null);
        this.leverage = other.getLeverage();
        this.maxNotionalValue = other.getMaxNotionalValue();
        this.marginType = other.getMarginType();
        this.isolatedMargin = Optional.ofNullable(other.getIsolatedMargin()).map(ZERO::add).orElse(null);
        this.isAutoAddMargin = other.isAutoAddMargin;
        this.positionSide = other.getPositionSide();
        this.notional = Optional.ofNullable(other.getNotional()).map(ZERO::add).orElse(null);
        this.isolatedWallet = Optional.ofNullable(other.getIsolatedWallet()).map(ZERO::add).orElse(null);
        this.updateTime = other.getUpdateTime();
        this.isolated = other.isIsolated();
        this.adlQuantile = other.getAdlQuantile();
    }

    public int direction() {
        return this.positionAmt.compareTo(ZERO);
    }
}
