package io.prada.listener.dto.accounting;

import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AccountingSnapshot {
    private Balances balances;
    private Map<String, AccountingConsolidatedSymbolInfo> symbolSnapShots;

    public record Balances(
        BigDecimal balance,
        BigDecimal equity,
        BigDecimal freeMargin) {
    }
}
