package io.prada.listener.service;

import io.prada.listener.config.AppSettings;
import io.prada.listener.dto.enums.BalanceType;
import io.prada.listener.dto.accounting.AccountingOrder;
import io.prada.listener.dto.accounting.AccountingPosition;
import io.prada.listener.dto.accounting.AccountingSnapshot;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiskCalculationService {
    private static final BigDecimal V100 = BigDecimal.valueOf(100L);
    private static final BigDecimal LEVERAGE = BigDecimal.valueOf(20L);

    private final MathContext ctx;
    private final AppSettings settings;

    public BigDecimal computeSizePercentAdd(AccountingPosition now, AccountingPosition old) {
        BigDecimal oldUsdt = old.getPositionAmt().multiply(old.getEntryPrice(), ctx);
        BigDecimal nowUsdt = now.getPositionAmt().multiply(now.getEntryPrice(), ctx);
        return nowUsdt.subtract(oldUsdt).divide(oldUsdt, ctx).multiply(V100).setScale(0, RoundingMode.HALF_EVEN);
    }

    public BigDecimal computeEntryAdd(AccountingPosition now, AccountingPosition old) {
        BigDecimal oldUsdt = old.getPositionAmt().multiply(old.getEntryPrice(), ctx);
        BigDecimal nowUsdt = now.getPositionAmt().multiply(now.getEntryPrice(), ctx);
        return nowUsdt.subtract(oldUsdt, ctx).divide(now.getPositionAmt().subtract(old.getPositionAmt(), ctx), ctx);
    }

    public BigDecimal computeSizePartial(AccountingPosition now, AccountingPosition old) {
        return old.getPositionAmt().subtract(now.getPositionAmt(), ctx).divide(old.getPositionAmt(), ctx)
            .multiply(V100).setScale(0, RoundingMode.HALF_EVEN);
    }

    public BigDecimal computeCurrentPrice(AccountingPosition now) {
        return now.getEntryPrice().add(now.getUnRealizedProfit().divide(now.getPositionAmt(), ctx));
    }

    public BigDecimal computeEntryRisk(AccountingOrder order, AccountingSnapshot snapShot) {
        return computeRiskFromAmnt(order.getOriginalQty().multiply(order.getPrice(), ctx), snapShot);
    }

    public BigDecimal computeEntryRisk(AccountingPosition position, AccountingSnapshot snapShot) {
        return computeRiskFromAmnt(position.getNotional(), snapShot);
    }

    private BigDecimal computeRiskFromAmnt(BigDecimal amount, AccountingSnapshot snapShot) {
        return amount.multiply(V100)
            .divide(BalanceType.of(snapShot.getBalances(), settings.getBalanceType()), ctx)
            .divide(LEVERAGE, ctx)
            .setScale(2, RoundingMode.HALF_EVEN);
    }
}
