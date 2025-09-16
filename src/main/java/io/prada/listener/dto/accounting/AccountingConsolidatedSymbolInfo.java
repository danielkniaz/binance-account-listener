package io.prada.listener.dto.accounting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AccountingConsolidatedSymbolInfo {
    private final String symbol;
    private AccountingPosition position;
    private final List<AccountingOrder> orders = new ArrayList<>();

    public AccountingConsolidatedSymbolInfo(AccountingConsolidatedSymbolInfo other) {
        this.symbol = other.getSymbol();
        this.position = Optional.ofNullable(other.getPosition()).map(AccountingPosition::of).orElse(null);
        this.orders.addAll(other.getOrders().stream().map(order -> order.toBuilder().build()).toList());
    }

    public AccountingConsolidatedSymbolInfo(String smb) {
        this.symbol = smb;
    }
}
