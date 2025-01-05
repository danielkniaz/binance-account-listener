package io.prada.listener.dto.enums;


import io.prada.listener.dto.accounting.AccountingSnapshot.Balances;
import java.math.BigDecimal;

public enum BalanceType {
    BALANCE,
    EQUITY,
    FREE_MARGIN,
    MIN_BALANCE_EQ;

    public static BigDecimal of(Balances balances, BalanceType type) {
        return switch (type) {
            case BALANCE -> balances.balance();
            case EQUITY -> balances.equity();
            case FREE_MARGIN -> balances.freeMargin();
            case MIN_BALANCE_EQ -> balances.balance().min(balances.equity());
        };
    }
}
