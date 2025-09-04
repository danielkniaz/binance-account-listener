package io.prada.listener.service;

import io.prada.listener.dto.accounting.AccountingOrder;
import io.prada.listener.dto.accounting.AccountingPosition;
import io.prada.listener.dto.accounting.AccountingSnapshot;
import io.prada.listener.dto.accounting.AccountingSnapshot.Balances;
import io.prada.listener.dto.enums.BalanceType;
import io.prada.listener.testUtils.TestFileUtils;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RiskCalculationServiceTest {
    final RiskCalculationService unit = new RiskCalculationService(
        new MathContext(8, RoundingMode.HALF_EVEN), TestFileUtils.appSettings(BalanceType.BALANCE));

    @Test
    void computeSizePercentAdd() {
        AccountingPosition old = new AccountingPosition();
        old.setEntryPrice(new BigDecimal("0.0112367"));
        old.setPositionAmt(new BigDecimal("882"));
        AccountingPosition now = new AccountingPosition();
        now.setEntryPrice(new BigDecimal("0.0109137996352"));
        now.setPositionAmt(new BigDecimal("2741"));

        var addPercent = unit.computeSizePercentAdd(now, old);
        Assertions.assertTrue(190 < addPercent.intValue());
        Assertions.assertTrue(210 > addPercent.intValue());

        var entryPrice = unit.computeEntryAdd(now, old);
        Assertions.assertEquals(0, new BigDecimal("0.0107606").compareTo(entryPrice));
    }

    @Test
    void computePartial() {
        AccountingPosition old = new AccountingPosition();
        old.setPositionAmt(new BigDecimal("18"));
        AccountingPosition now = new AccountingPosition();
        now.setPositionAmt(new BigDecimal("14"));
        var result = unit.computeSizePartial(now, old);
        Assertions.assertEquals(22, result.intValue());

        old.setPositionAmt(new BigDecimal("-18"));
        now.setPositionAmt(new BigDecimal("-14"));
        result = unit.computeSizePartial(now, old);
        Assertions.assertEquals(22, result.intValue());
    }

    @Test
    void computeCurrentPrice() {
        AccountingPosition pos = new AccountingPosition();
        pos.setEntryPrice(new BigDecimal("0.4298"));
        pos.setUnRealizedProfit(new BigDecimal("0.13571152"));
        pos.setPositionAmt(new BigDecimal("14"));
        var currentPrice = unit.computeCurrentPrice(pos);
        Assertions.assertTrue(0.4394 < currentPrice.doubleValue());
        Assertions.assertTrue(0.4395 > currentPrice.doubleValue());

        pos.setEntryPrice(new BigDecimal("1.473118450184"));
        pos.setUnRealizedProfit(new BigDecimal("-22.28365981"));
        pos.setPositionAmt(new BigDecimal("-27.1"));
        currentPrice = unit.computeCurrentPrice(pos);
        Assertions.assertTrue(2.29 < currentPrice.doubleValue());
        Assertions.assertTrue(2.30 > currentPrice.doubleValue());
    }

    @Test
    void computeEntryAdd() {
        AccountingPosition now = new AccountingPosition();
        now.setPositionAmt(new BigDecimal("13"));
        now.setEntryPrice(new BigDecimal("2.162915384615"));
        AccountingPosition old = new AccountingPosition();
        old.setPositionAmt(new BigDecimal("4"));
        old.setEntryPrice(new BigDecimal("2.311"));
        var risk = unit.computeEntryAdd(now, old);
        Assertions.assertTrue(1.9 < risk.doubleValue());
        Assertions.assertTrue(2.1 > risk.doubleValue());
    }

    @Test
    void computeEntryRiskPending() {
       AccountingOrder order = new AccountingOrder();
       order.setOriginalQty(new BigDecimal("1.6"));
       order.setPrice(new BigDecimal("6"));
       order.setStopPrice(new BigDecimal("0"));
       AccountingSnapshot snapshot = new AccountingSnapshot().setBalances(buildBalance());
       var risk = unit.computeEntryRisk(order, snapshot);
       Assertions.assertTrue(0.15 < risk.doubleValue());
       Assertions.assertTrue(0.20 > risk.doubleValue());
    }

    @Test
    void computeEntryRiskMkt() {
        AccountingPosition position = new AccountingPosition();
        position.setNotional(new BigDecimal("19.95"));
        AccountingSnapshot snapshot = new AccountingSnapshot().setBalances(buildBalance());
        var risk = unit.computeEntryRisk(position, snapshot);
        Assertions.assertTrue(0.36 <= risk.doubleValue());
        Assertions.assertTrue(0.37 > risk.doubleValue());
    }

    private Balances buildBalance() {
        return new Balances(
            new BigDecimal("275.56393903"),
            new BigDecimal("244.48095312"),
            new BigDecimal("235.29567754"));
    }
}