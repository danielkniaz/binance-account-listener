package io.prada.listener.dto.accounting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AccountingConsolidatedSymbolInfo {
    private String symbol;
    private AccountingPosition position;
    private List<AccountingOrder> orders = new ArrayList<>();

    public AccountingConsolidatedSymbolInfo(AccountingConsolidatedSymbolInfo other) {
        this.symbol = other.getSymbol();
        this.position = Optional.ofNullable(other.getPosition())
            .map(pos -> pos.toBuilder().build())
            .orElse(null);
        this.orders = other.getOrders().stream().map(order -> order.toBuilder().build()).toList();
    }
}
