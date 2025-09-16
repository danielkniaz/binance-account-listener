package io.prada.listener.dto.accounting;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AccountingSnapshot {
    private Balances balances;
    private Map<String, AccountingConsolidatedSymbolInfo> symbolSnapShots = new HashMap<>();

    public record Balances(
        BigDecimal balance,
        BigDecimal equity,
        BigDecimal freeMargin) {

        public static Balances copy(Balances entity) {
            return new Balances(entity.balance(), entity.equity(), entity.freeMargin());
        }
    }
}
