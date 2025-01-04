package io.prada.listener.dto.accounting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
        this.position = new AccountingPosition(other.position);
        this.orders = other.getOrders().stream().map(AccountingOrder::new).collect(Collectors.toList());
    }
}
