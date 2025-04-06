package io.prada.listener.dto.accounting;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.prada.listener.config.BigDecimalMathContextDeserializer;
import io.prada.listener.dto.enums.SideType;
import java.math.BigDecimal;
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
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal price;
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal avgPrice;
    @JsonProperty("origQty")
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal originalQty;
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal executedQty;
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    @JsonProperty("cumQuote") private BigDecimal cumulativeQty;
    private String timeInForce;
    private String type;
    private boolean reduceOnly;
    private boolean closePosition;
    private SideType side;
    private String positionSide;
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal stopPrice;
    private String workingType;
    private boolean priceProtect;
    @JsonProperty("origType") private String originalType;
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
}
