package io.prada.listener.dto.accounting;

import static java.math.BigDecimal.ZERO;

import com.binance.connector.client.derivatives_trading_usds_futures.rest.model.PositionInformationV2ResponseInner;
import java.math.BigDecimal;
import java.math.MathContext;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountingPosition implements Cloneable {
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
    private Integer adlQuantile;

    public int direction() {
        return this.positionAmt.compareTo(ZERO);
    }

    public static AccountingPosition of(AccountingPosition other) {
        try {
            return  (AccountingPosition) other.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public AccountingPosition(PositionInformationV2ResponseInner entity, MathContext ctx) {
        this.symbol = entity.getSymbol();
        this.positionAmt = new BigDecimal(entity.getPositionAmt(), ctx);
        this.entryPrice = new BigDecimal(entity.getEntryPrice(), ctx);
        this.breakEvenPrice = new BigDecimal(entity.getBreakEvenPrice(), ctx);
        this.markPrice = new BigDecimal(entity.getMarkPrice(), ctx);
        this.unRealizedProfit = new BigDecimal(entity.getUnRealizedProfit(), ctx);
        this.liquidationPrice = new BigDecimal(entity.getLiquidationPrice(), ctx);
        this.leverage = Integer.parseInt(entity.getLeverage());
        this.maxNotionalValue = Long.parseLong(entity.getMaxNotionalValue());
        this.marginType = entity.getMarginType();
        this.isolatedMargin = new BigDecimal(entity.getIsolatedMargin(), ctx);
        this.isAutoAddMargin = entity.getIsAutoAddMargin();
        this.positionSide = entity.getPositionSide();
        this.notional = new BigDecimal(entity.getNotional(), ctx);
        this.isolatedWallet = new BigDecimal(entity.getIsolatedMargin(), ctx);
        this.updateTime = entity.getUpdateTime();
    }
}
