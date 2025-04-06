package io.prada.listener.dto.accounting;

import static java.math.BigDecimal.ZERO;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.prada.listener.config.BigDecimalMathContextDeserializer;
import io.prada.listener.config.LongMathDeserializer;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder(toBuilder = true)
public class AccountingPosition {
    private static final MathContext ctx = new MathContext(8, RoundingMode.HALF_EVEN);

    private String symbol;
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal positionAmt;
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal entryPrice;

    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal breakEvenPrice;
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal markPrice;
    @JsonAlias({"unRealizedProfit", "unrealizedProfit"})
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal unRealizedProfit;
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal liquidationPrice;
    private Integer leverage;
    @JsonDeserialize(using = LongMathDeserializer.class)
    @JsonAlias({"maxNotionalValue", "maxNotional"})
    private Long maxNotionalValue;
    private String marginType;
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal isolatedMargin;
    private String isAutoAddMargin;
    private String positionSide;
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal notional;
    @JsonDeserialize(using = BigDecimalMathContextDeserializer.class)
    private BigDecimal isolatedWallet;
    private Long updateTime;
    private boolean isolated;
    private Integer adlQuantile;

    public int direction() {
        return this.positionAmt.compareTo(ZERO);
    }
}
